package de.baupilot.sync

import android.util.Log
import androidx.room.withTransaction
import de.baupilot.backend.AuthGateway
import de.baupilot.capture.FileStore
import de.baupilot.data.local.BaupilotDatabase
import de.baupilot.data.local.Converters
import de.baupilot.data.local.LocalRepository
import de.baupilot.data.local.OutboxEntity
import de.baupilot.data.local.OutboxKind
import de.baupilot.data.local.SyncStateEntity
import java.io.IOException
import java.time.DateTimeException
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

sealed interface SyncOutcome {
    data class Done(val pushed: Int, val pulled: Int, val failed: Int, val parked: Int) : SyncOutcome
    data class Retry(val cause: String) : SyncOutcome
    data object AuthRequired : SyncOutcome
    data object NotLoggedIn : SyncOutcome
    data object NotConfigured : SyncOutcome
}

/**
 * Pushes the outbox and pulls the remote state (plan §5.8). One drain at a time; the whole outbox and every local
 * project are covered by a drain.
 */
class SyncEngine(
    private val db: BaupilotDatabase,
    private val repo: LocalRepository,
    private val remote: RemoteGateway?,
    private val auth: AuthGateway,
    private val files: FileStore,
    @Suppress("unused") private val stamps: StampClock,
    private val pageSize: Int = 100,
) {
    private val mutex = Mutex()

    /** Raised inside a drain to end it with [outcome]. */
    private class Stop(val outcome: SyncOutcome) : Exception()

    /** Raised by [guarded] for a failure that skips one project or table without ending the drain. */
    private class Skip : Exception()

    private class Counts(var pushed: Int = 0, var pulled: Int = 0, var failed: Int = 0, var parked: Int = 0)

    suspend fun drain(): SyncOutcome = mutex.withLock {
        val gateway = remote ?: return@withLock SyncOutcome.NotConfigured
        if (auth.cachedUserId == null) return@withLock SyncOutcome.NotLoggedIn
        val counts = Counts()
        try {
            val purged = checkProjects(gateway)
            var round = 0
            do {
                pushAll(gateway, counts)
                pullAll(gateway, counts, purged)
                round++
            } while (round < MAX_ROUNDS && db.outbox().head() != null)
            SyncOutcome.Done(counts.pushed, counts.pulled, counts.failed, counts.parked)
        } catch (stop: Stop) {
            stop.outcome
        }
    }

    // --- project check (D19) ---

    /** Sets, bootstraps or purges per project; returns the ids purged during this drain. */
    private suspend fun checkProjects(gateway: RemoteGateway): Set<String> {
        val purged = HashSet<String>()
        for (id in db.records().projectIds()) {
            val row = try {
                guarded { gateway.getRow(SyncTable.Projects, id) }
            } catch (skip: Skip) {
                continue
            }
            val known = db.state().get(knownKey(id)) != null
            when {
                row != null -> if (!known) db.state().put(SyncStateEntity(knownKey(id), FLAG))
                known -> if (stillMember(gateway, id)) {
                    repo.purgeProject(id)
                    purged += id
                }
                auth.cachedRole in BOOTSTRAP_ROLES -> bootstrap(id)
            }
        }
        return purged
    }

    /**
     * A `row_not_found` for the projects row proves deletion only for a caller who can read the project: a removed member
     * or an expired session gets the same 404 (reads are granted to the team). So the purge needs a live session and the
     * user's own member row; a failed proof keeps the project and its unsynced captures.
     */
    private suspend fun stillMember(gateway: RemoteGateway, projectId: String): Boolean {
        val userId = auth.cachedUserId ?: return false
        return try {
            guarded { gateway.hasAccess(projectId, userId) }
        } catch (skip: Skip) {
            false
        }
    }

    /**
     * Queues an UPSERT of every record of the project, projects first, once: any projects op already in the outbox
     * (pending, parked or dead-lettered) means the bootstrap was done.
     */
    private suspend fun bootstrap(projectId: String) {
        val queued = db.outbox().all().any {
            it.kind == OutboxKind.UPSERT && it.tableId == SyncTable.Projects.id && it.rowId == projectId
        }
        if (queued) return
        db.withTransaction {
            for (table in SyncTable.entries) {
                for (row in db.records().list(table.id, projectId)) {
                    db.outbox().enqueueUpsert(table.id, row.id, projectId)
                }
            }
        }
    }

    // --- push ---

    /** Sends the ops queued when the round starts; ops written meanwhile (also re-queued edits) wait for the next round. */
    private suspend fun pushAll(gateway: RemoteGateway, counts: Counts) {
        val last = db.outbox().all().maxOfOrNull { it.seq } ?: return
        while (true) {
            val op = db.outbox().head()?.takeIf { it.seq <= last } ?: return
            try {
                push(gateway, op)
                counts.pushed++
            } catch (e: RemoteException) {
                when (e.kind) {
                    RemoteErrorKind.Transient -> throw Stop(SyncOutcome.Retry(e.message ?: "transient"))
                    RemoteErrorKind.Auth -> throw authRequired()
                    RemoteErrorKind.Forbidden -> {
                        db.outbox().park(op.seq)
                        counts.parked++
                    }
                    RemoteErrorKind.NotFound, RemoteErrorKind.Conflict, RemoteErrorKind.Permanent -> {
                        db.outbox().markFailed(op.seq, e.message ?: e.kind.name)
                        counts.failed++
                    }
                }
            } catch (e: IOException) {
                throw Stop(SyncOutcome.Retry(e.message ?: "io"))
            } catch (e: SerializationException) {
                db.outbox().markFailed(op.seq, e.message ?: "bad data")
                counts.failed++
            } catch (e: IllegalArgumentException) {
                db.outbox().markFailed(op.seq, e.message ?: "bad data")
                counts.failed++
            }
        }
    }

    private suspend fun push(gateway: RemoteGateway, op: OutboxEntity) {
        when (op.kind) {
            OutboxKind.UPSERT -> pushUpsert(gateway, op)
            OutboxKind.UPLOAD_FILE -> pushUpload(gateway, op)
            OutboxKind.DELETE_PROJECT -> {
                gateway.deleteRow(SyncTable.Projects, op.rowId)
                db.outbox().deleteBySeq(op.seq)
            }
        }
    }

    private suspend fun pushUpsert(gateway: RemoteGateway, op: OutboxEntity) {
        val table = tableOf(op.tableId)
        val local = db.records().get(table.id, op.rowId)?.let(Converters::toRecord)
        if (local == null) {
            db.outbox().deleteBySeq(op.seq)
            return
        }
        val spec = SyncSchema.tables.getValue(table)
        val remoteRow = gateway.getRow(table, op.rowId)?.restrictedTo(table, spec)
        val sent = remoteRow?.let { FieldMerge.merge(local, it.toSyncRecord(table)) } ?: local
        gateway.upsertRow(table, sent.toRemoteRow())
        db.withTransaction {
            val current = db.records().get(table.id, op.rowId)?.let(Converters::toRecord)
            if (current != null) {
                val merged = FieldMerge.merge(current, sent)
                if (merged != current) {
                    db.records().upsert(Converters.toEntity(merged))
                    bumpDataVersion()
                }
            }
            db.outbox().deleteBySeq(op.seq)
            if (table == SyncTable.Projects) db.state().put(SyncStateEntity(knownKey(op.rowId), FLAG))
        }
    }

    private suspend fun pushUpload(gateway: RemoteGateway, op: OutboxEntity) {
        val payload = Json.parseToJsonElement(op.payload).jsonObject
        val file = try {
            files.resolve(payload.text("path")).takeIf { it.isFile }
        } catch (e: IllegalArgumentException) {
            null
        }
        if (file == null) {
            Log.w(TAG, "Dropping upload ${op.rowId}: the file is gone")
            db.outbox().deleteBySeq(op.seq)
            return
        }
        val bucket = SyncBucket.entries.firstOrNull { it.id == payload.text("bucket") }
            ?: throw IllegalArgumentException("Unknown bucket ${payload.text("bucket")}")
        gateway.uploadFile(bucket, payload.text("fileId"), payload.text("name"), file, payload.text("mime"))
        db.outbox().deleteBySeq(op.seq)
    }

    // --- pull ---

    private suspend fun pullAll(gateway: RemoteGateway, counts: Counts, purged: Set<String>) {
        for (projectId in db.records().projectIds()) {
            if (projectId in purged || db.state().get(knownKey(projectId)) == null) continue
            for (table in SyncTable.entries) pullTable(gateway, counts, projectId, table)
        }
    }

    private suspend fun pullTable(gateway: RemoteGateway, counts: Counts, projectId: String, table: SyncTable) {
        val spec = SyncSchema.tables.getValue(table)
        val key = cursorKey(projectId, table)
        var cursor = db.state().get(key)?.let(::decodeCursor)?.let(::withOverlap)
        while (true) {
            val page = try {
                guarded { gateway.listRows(table, projectId, cursor, pageSize) }
            } catch (skip: Skip) {
                return
            }
            val last = page.lastOrNull()
            val next = last?.updatedAt?.let { PullCursor(it, last.id) }
            counts.pulled += storePage(table, projectId, spec, page, next, key)
            if (page.size < pageSize || next == null || next == cursor) return
            cursor = next
        }
    }

    /** Merges [page] into the local records and stores the cursor, in one transaction; returns the rows changed. */
    private suspend fun storePage(
        table: SyncTable,
        projectId: String,
        spec: TableSpec,
        page: List<RemoteRow>,
        next: PullCursor?,
        cursorKey: String,
    ): Int = db.withTransaction {
        var changed = 0
        var maxSeen = db.state().get(MAX_SEEN_KEY)?.toLongOrNull() ?: 0L
        for (raw in page) {
            val row = raw.restrictedTo(table, spec)
            val incoming = row.toSyncRecord(table).let {
                if (it.projectId.isBlank()) it.copy(projectId = projectId) else it
            }
            maxSeen = maxOf(maxSeen, row.stamps.values.maxOfOrNull { it.at } ?: 0L)
            val local = db.records().get(table.id, row.id)?.let(Converters::toRecord)
            val merged = local?.let { FieldMerge.merge(it, incoming) } ?: incoming
            if (merged != local) {
                db.records().upsert(Converters.toEntity(merged))
                changed++
            }
        }
        db.state().put(SyncStateEntity(MAX_SEEN_KEY, maxSeen.toString()))
        if (next != null) db.state().put(SyncStateEntity(cursorKey, encodeCursor(next)))
        if (changed > 0) bumpDataVersion()
        changed
    }

    // --- helpers ---

    /**
     * Runs a remote call of the project check or the pull: transient and auth failures end the drain, any other
     * failure (a 403 for a project the user is not in a team of, a malformed request) raises [Skip].
     */
    private suspend fun <T> guarded(block: suspend () -> T): T = try {
        block()
    } catch (e: RemoteException) {
        when (e.kind) {
            RemoteErrorKind.Transient -> throw Stop(SyncOutcome.Retry(e.message ?: "transient"))
            RemoteErrorKind.Auth -> throw authRequired()
            else -> {
                Log.w(TAG, "Skipping after ${e.kind}: ${e.message}")
                throw Skip()
            }
        }
    } catch (e: IOException) {
        throw Stop(SyncOutcome.Retry(e.message ?: "io"))
    }

    private fun authRequired(): Stop {
        auth.clearCachedUser()
        return Stop(SyncOutcome.AuthRequired)
    }

    private suspend fun bumpDataVersion() {
        val current = db.state().get(DATA_VERSION_KEY)?.toLongOrNull() ?: 0L
        db.state().put(SyncStateEntity(DATA_VERSION_KEY, (current + 1).toString()))
    }

    private fun tableOf(id: String?): SyncTable =
        SyncTable.entries.firstOrNull { it.id == id } ?: throw IllegalArgumentException("Unknown table $id")

    private fun JsonObject.text(name: String): String =
        (this[name] as? JsonPrimitive)?.contentOrNull ?: throw IllegalArgumentException("Missing $name")

    /**
     * Remote rows are untrusted: only the columns of the table's spec (and their stamps) survive. A project is deleted
     * only by a 404 after it was observed (D19), never by a `deleted` flag on its row: such a flag would leave a project
     * record that no screen can show and that is never checked again, so it is dropped with its stamp (I17).
     */
    private fun RemoteRow.restrictedTo(table: SyncTable, spec: TableSpec): RemoteRow {
        val allowed = spec.fixtureFields.keys + spec.extraFields.keys
        val keptStamps = stamps.filterKeys { it in allowed || it == FieldMerge.DELETED }
        val row = copy(fields = JsonObject(fields.filterKeys { it in allowed }), stamps = keptStamps)
        return if (table == SyncTable.Projects) row.copy(deleted = false, stamps = keptStamps - FieldMerge.DELETED) else row
    }

    private fun encodeCursor(cursor: PullCursor): String = JsonObject(
        mapOf("updatedAt" to JsonPrimitive(cursor.updatedAt), "id" to JsonPrimitive(cursor.id)),
    ).toString()

    private fun decodeCursor(text: String): PullCursor? = try {
        val obj = Json.parseToJsonElement(text).jsonObject
        PullCursor(obj.text("updatedAt"), obj.text("id"))
    } catch (e: SerializationException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }

    /** The stored cursor moved back by the overlap, so rows committed late with an earlier `$updatedAt` are re-read. */
    private fun withOverlap(cursor: PullCursor): PullCursor? = try {
        val back = OffsetDateTime.parse(cursor.updatedAt).minusSeconds(OVERLAP_SECONDS)
        PullCursor(back.withOffsetSameInstant(ZoneOffset.UTC).format(TIMESTAMP_FORMAT), "")
    } catch (e: DateTimeException) {
        null
    }

    private fun knownKey(projectId: String) = "known:$projectId"

    private fun cursorKey(projectId: String, table: SyncTable) = "cursor:$projectId:${table.id}"

    private companion object {
        const val TAG = "SyncEngine"
        const val FLAG = "1"
        const val DATA_VERSION_KEY = "dataVersion"
        const val MAX_SEEN_KEY = "maxSeenAt"
        const val MAX_ROUNDS = 3
        const val OVERLAP_SECONDS = 5L
        val BOOTSTRAP_ROLES = setOf("bauherr", "architekt")
        val TIMESTAMP_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx")
    }
}
