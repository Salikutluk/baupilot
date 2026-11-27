package de.baupilot.sync

import androidx.room.Room
import de.baupilot.backend.DemoAuth
import de.baupilot.capture.FileStore
import de.baupilot.data.local.BaupilotDatabase
import de.baupilot.data.local.Converters
import de.baupilot.data.local.LocalRepository
import de.baupilot.data.local.OutboxEntity
import de.baupilot.data.local.OutboxKind
import de.baupilot.data.local.OutboxState
import java.io.File
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SyncEngineTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val fixture: String =
        File(System.getProperty("baupilot.repoRoot"), "fixtures/baupilot.json").readText()
    private val remote = FakeRemoteGateway()
    private val clock = Clock.systemUTC()
    private val stampClock = StampClock(clock, "devA") { 0L }
    private lateinit var db: BaupilotDatabase
    private lateinit var files: FileStore
    private lateinit var repo: LocalRepository
    private lateinit var projectId: String

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), BaupilotDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        files = FileStore(tmp.newFolder())
        repo = LocalRepository(db, { fixture }, files, stampClock, clock) {}
        repo.ensureSeeded()
        projectId = db.records().projectIds().first()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun engine(
        auth: DemoAuth = DemoAuth("user", "bauherr"),
        gateway: RemoteGateway? = remote,
        pageSize: Int = 100,
    ) = SyncEngine(db, repo, gateway, auth, files, stampClock, pageSize)

    private suspend fun record(table: SyncTable, id: String): SyncRecord =
        Converters.toRecord(db.records().get(table.id, id)!!)

    private suspend fun tradeIds(): List<String> = db.records().list(SyncTable.Trades.id, projectId).map { it.id }

    /** A local edit of one field with a fresh stamp, queued like the repository does. */
    private suspend fun edit(table: SyncTable, id: String, key: String, value: String) {
        val current = record(table, id)
        val touched = FieldMerge.touch(current, mapOf(key to JsonPrimitive(value)), stampClock.next())
        db.records().upsert(Converters.toEntity(touched))
        db.outbox().enqueueUpsert(table.id, id, current.projectId)
    }

    private suspend fun putRemoteProject() {
        remote.put(SyncTable.Projects, record(SyncTable.Projects, projectId).toRemoteRow())
    }

    private suspend fun state(key: String): String? = db.state().get(key)

    private fun defect(id: String, extra: Map<String, String> = emptyMap(), stamps: Map<String, FieldStamp> = emptyMap()) =
        RemoteRow(
            id = id,
            projectId = projectId,
            sortKey = 0,
            fields = JsonObject(
                mapOf("id" to JsonPrimitive(id), "title" to JsonPrimitive("Title $id")) +
                    extra.mapValues { JsonPrimitive(it.value) },
            ),
            stamps = stamps,
            deleted = false,
            updatedAt = null,
        )

    private fun text(row: SyncRecord, key: String): String? = (row.fields[key] as? JsonPrimitive)?.content

    private suspend fun outbox(): List<OutboxEntity> = db.outbox().all()

    @Test
    fun classifyErrorCodes() {
        val table = mapOf(
            null to RemoteErrorKind.Transient,
            401 to RemoteErrorKind.Auth,
            403 to RemoteErrorKind.Forbidden,
            404 to RemoteErrorKind.Permanent,
            409 to RemoteErrorKind.Conflict,
            408 to RemoteErrorKind.Transient,
            429 to RemoteErrorKind.Transient,
            500 to RemoteErrorKind.Transient,
            503 to RemoteErrorKind.Transient,
            400 to RemoteErrorKind.Permanent,
            413 to RemoteErrorKind.Permanent,
            422 to RemoteErrorKind.Permanent,
        )
        for ((code, kind) in table) assertEquals("code $code", kind, RemoteErrors.classify(code, null))
        assertEquals(RemoteErrorKind.Transient, RemoteErrors.classify(null, java.io.IOException("down")))
    }

    @Test
    fun classifyUsesTheAppwriteErrorType() {
        // Only a missing row is "gone"; a missing table or database must never purge a project (C2, D19).
        assertEquals(RemoteErrorKind.NotFound, RemoteErrors.classify(404, null, "row_not_found"))
        for (type in listOf("database_not_found", "table_not_found", "project_not_found", "storage_bucket_not_found")) {
            assertEquals(type, RemoteErrorKind.Transient, RemoteErrors.classify(404, null, type))
        }
        assertEquals(RemoteErrorKind.Permanent, RemoteErrors.classify(404, null, "general_route_not_found"))
        // A missing permission is not an expired session (M3, R16).
        assertEquals(RemoteErrorKind.Forbidden, RemoteErrors.classify(401, null, "user_unauthorized"))
        for (type in listOf("user_session_not_found", "general_unauthorized_scope", "user_jwt_invalid", null)) {
            assertEquals(type, RemoteErrorKind.Auth, RemoteErrors.classify(401, null, type))
        }
    }

    @Test
    fun rowCodecRoundTripsJsonColumns() {
        val spec = SyncSchema.tables.getValue(SyncTable.Defects)
        val row = RemoteRow(
            id = "d1",
            projectId = "p1",
            sortKey = 7,
            fields = JsonObject(
                mapOf(
                    "id" to JsonPrimitive("d1"),
                    "title" to JsonPrimitive("Riss"),
                    "photos" to JsonPrimitive(2),
                    "notifyByEmail" to JsonPrimitive(true),
                    "trade" to JsonObject(mapOf("de" to JsonPrimitive("Maurer"), "en" to JsonPrimitive("Mason"))),
                ),
            ),
            stamps = mapOf("title" to FieldStamp(5, "devA"), "deleted" to FieldStamp(6, "devB")),
            deleted = true,
            updatedAt = "2026-01-01T00:00:00.000+00:00",
        )
        val data = RowCodec.toData(row, spec)
        assertTrue(data["trade"] is String)
        assertEquals("p1", data["projectId"])
        assertEquals(7L, data["sortKey"])
        assertEquals(true, data["deleted"])
        assertNull(data["description"])
        assertEquals(row, RowCodec.fromData("d1", data, row.updatedAt, spec))

        val doc = SyncSchema.tables.getValue(SyncTable.Documents)
        val viewer = JsonObject(mapOf("pages" to JsonPrimitive("a\"b")))
        val docRow = row.copy(fields = JsonObject(mapOf("viewer" to viewer, "title" to JsonPrimitive("x"))))
        assertEquals(docRow.fields, RowCodec.fromData("d1", RowCodec.toData(docRow, doc), null, doc).fields)

        val hostile = data + mapOf("photos" to "many", "bogus" to 1, "\$id" to "d1", "title" to null)
        val decoded = RowCodec.fromData("d1", hostile, null, spec)
        assertFalse(decoded.fields.containsKey("photos"))
        assertFalse(decoded.fields.containsKey("bogus"))
        assertFalse(decoded.fields.containsKey("title"))
        assertEquals(JsonPrimitive(2), RowCodec.fromData("d1", data + ("photos" to 2.0), null, spec).fields["photos"])
    }

    @Test
    fun pushSendsMergedRowAndClearsOp() = runTest {
        putRemoteProject()
        val id = tradeIds().first()
        edit(SyncTable.Trades, id, "statusLabel", "Fertig")

        val outcome = engine().drain()

        assertEquals(SyncOutcome.Done(pushed = 1, pulled = 0, failed = 0, parked = 0), outcome)
        assertEquals(JsonPrimitive("Fertig"), remote.row(SyncTable.Trades, id)!!.fields["statusLabel"])
        assertTrue(outbox().isEmpty())
        assertEquals("1", state("known:$projectId"))
    }

    @Test
    fun remoteNewerFieldSurvivesLocalPush() = runTest {
        putRemoteProject()
        val id = tradeIds().first()
        val base = record(SyncTable.Trades, id).toRemoteRow()
        remote.put(
            SyncTable.Trades,
            base.copy(
                fields = JsonObject(base.fields + ("statusLabel" to JsonPrimitive("Remote"))),
                stamps = mapOf("statusLabel" to FieldStamp(50, "devB")),
            ),
        )
        edit(SyncTable.Trades, id, "weekLabel", "KW 12")

        engine().drain()

        val sent = remote.row(SyncTable.Trades, id)!!
        assertEquals(JsonPrimitive("KW 12"), sent.fields["weekLabel"])
        assertEquals(JsonPrimitive("Remote"), sent.fields["statusLabel"])
        assertEquals("Remote", text(record(SyncTable.Trades, id), "statusLabel"))
        assertEquals("KW 12", text(record(SyncTable.Trades, id), "weekLabel"))
    }

    @Test
    fun editDuringInFlightUpsertIsNotLost() = runTest {
        putRemoteProject()
        val id = tradeIds().first()
        edit(SyncTable.Trades, id, "statusLabel", "v0")
        var n = 0
        remote.beforeUpsert = { table, _ -> if (table == SyncTable.Trades) edit(table, id, "statusLabel", "v${++n}") }

        engine().drain()

        assertEquals("v3", text(record(SyncTable.Trades, id), "statusLabel"))
        assertEquals(JsonPrimitive("v2"), remote.row(SyncTable.Trades, id)!!.fields["statusLabel"])
        val pending = outbox()
        assertEquals(1, pending.size)
        assertEquals(id, pending.single().rowId)

        remote.beforeUpsert = null
        engine().drain()

        assertEquals(JsonPrimitive("v3"), remote.row(SyncTable.Trades, id)!!.fields["statusLabel"])
        assertTrue(outbox().isEmpty())
    }

    @Test
    fun transientErrorStopsAndKeepsQueue() = runTest {
        putRemoteProject()
        val (a, b) = tradeIds()
        edit(SyncTable.Trades, a, "statusLabel", "A")
        edit(SyncTable.Trades, b, "statusLabel", "B")
        remote.failure = { op, _, _ -> if (op == "upsertRow") RemoteException(RemoteErrorKind.Transient, 503, "busy") else null }

        val outcome = engine().drain()

        assertTrue(outcome is SyncOutcome.Retry)
        assertEquals(2, outbox().size)
        assertTrue(outbox().all { it.state == OutboxState.PENDING })

        remote.failure = null
        remote.online = false
        assertTrue(engine().drain() is SyncOutcome.Retry)
        remote.online = true
        assertEquals(SyncOutcome.Done(2, 0, 0, 0), engine().drain())
        assertTrue(outbox().isEmpty())
    }

    @Test
    fun authErrorStopsAndClearsCachedUser() = runTest {
        putRemoteProject()
        edit(SyncTable.Trades, tradeIds().first(), "statusLabel", "A")
        remote.failure = { op, _, _ -> if (op == "upsertRow") RemoteException(RemoteErrorKind.Auth, 401, "expired") else null }
        val auth = DemoAuth("user", "bauherr")

        assertEquals(SyncOutcome.AuthRequired, engine(auth).drain())

        assertNull(auth.cachedUserId)
        assertEquals(1, outbox().size)
        assertEquals(SyncOutcome.NotLoggedIn, engine(auth).drain())
    }

    @Test
    fun forbiddenParksOpAndContinues() = runTest {
        putRemoteProject()
        val (a, b) = tradeIds()
        edit(SyncTable.Trades, a, "statusLabel", "A")
        edit(SyncTable.Trades, b, "statusLabel", "B")
        remote.failure = { op, _, id -> if (op == "upsertRow" && id == a) RemoteException(RemoteErrorKind.Forbidden, 403, "no") else null }

        val outcome = engine().drain()

        assertEquals(SyncOutcome.Done(pushed = 1, pulled = 0, failed = 0, parked = 1), outcome)
        assertEquals(listOf(a to OutboxState.PARKED), outbox().map { it.rowId to it.state })
        assertNotNull(remote.row(SyncTable.Trades, b))
    }

    @Test
    fun requeueParkedRetriesAfterLogin() = runTest {
        putRemoteProject()
        val a = tradeIds().first()
        edit(SyncTable.Trades, a, "statusLabel", "A")
        remote.failure = { op, _, _ -> if (op == "upsertRow") RemoteException(RemoteErrorKind.Forbidden, 403, "no") else null }
        engine().drain()
        assertEquals(OutboxState.PARKED, outbox().single().state)

        remote.failure = null
        assertEquals(SyncOutcome.Done(0, 0, 0, 0), engine().drain())
        assertNull(remote.row(SyncTable.Trades, a))

        db.outbox().requeueParked()
        assertEquals(SyncOutcome.Done(1, 0, 0, 0), engine().drain())
        assertNotNull(remote.row(SyncTable.Trades, a))
        assertTrue(outbox().isEmpty())
    }

    @Test
    fun permanentErrorDeadLettersAndContinues() = runTest {
        putRemoteProject()
        val (a, b) = tradeIds()
        edit(SyncTable.Trades, a, "statusLabel", "A")
        edit(SyncTable.Trades, b, "statusLabel", "B")
        remote.failure = { op, _, id -> if (op == "upsertRow" && id == a) RemoteException(RemoteErrorKind.Permanent, 400, "bad") else null }

        val outcome = engine().drain()

        assertEquals(SyncOutcome.Done(pushed = 1, pulled = 0, failed = 1, parked = 0), outcome)
        val dead = outbox().single()
        assertEquals(a, dead.rowId)
        assertEquals(OutboxState.FAILED, dead.state)
        assertEquals("bad", dead.lastError)
        assertEquals(1, dead.attempts)
        assertNotNull(remote.row(SyncTable.Trades, b))
        assertEquals(SyncOutcome.Done(0, 0, 0, 0), engine().drain())
    }

    @Test
    fun missingFileDropsUploadOp() = runTest {
        putRemoteProject()
        val present = files.newPhotoFile(projectId, "ph1").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        fun upload(fileId: String, file: File) = OutboxEntity(
            kind = OutboxKind.UPLOAD_FILE,
            tableId = null,
            rowId = fileId,
            projectId = projectId,
            payload = JsonObject(
                mapOf(
                    "bucket" to JsonPrimitive("photos"),
                    "fileId" to JsonPrimitive(fileId),
                    "path" to JsonPrimitive(files.relative(file)),
                    "name" to JsonPrimitive("$projectId/photos/$fileId.jpg"),
                    "mime" to JsonPrimitive("image/jpeg"),
                    "sha256" to JsonPrimitive("x"),
                ),
            ).toString(),
            state = OutboxState.PENDING,
            attempts = 0,
            lastError = null,
            createdAt = 1,
        )
        db.outbox().insert(upload("gone", File(present.parentFile, "gone.jpg")))
        db.outbox().insert(upload("ph1", present))

        val outcome = engine().drain()

        assertEquals(SyncOutcome.Done(pushed = 2, pulled = 0, failed = 0, parked = 0), outcome)
        assertEquals(mapOf("photos/ph1" to "$projectId/photos/ph1.jpg"), remote.uploads)
        assertTrue(outbox().isEmpty())
    }

    @Test
    fun pullPagesTerminateWith250RowsSameTimestamp() = runTest {
        putRemoteProject()
        val at = Instant.parse("2026-02-01T00:00:00Z")
        repeat(250) { remote.put(SyncTable.Defects, defect("bulk-%03d".format(it)), at) }

        val outcome = engine(pageSize = 100).drain()

        assertEquals(250, (outcome as SyncOutcome.Done).pulled)
        assertEquals(250, db.records().list(SyncTable.Defects.id, projectId).count { it.id.startsWith("bulk-") })
        assertEquals(3, remote.listCursors.count { it.first == SyncTable.Defects })

        remote.listCursors.clear()
        assertEquals(0, (engine(pageSize = 100).drain() as SyncOutcome.Done).pulled)
        assertEquals(3, remote.listCursors.count { it.first == SyncTable.Defects })
    }

    @Test
    fun pullDoesNotSkipRowsWhenCursorRowIsUpdatedOrDeletedBetweenDrains() = runTest {
        putRemoteProject()
        val t = Instant.parse("2026-02-01T00:00:00Z")
        remote.put(SyncTable.Defects, defect("r1"), t)
        remote.put(SyncTable.Defects, defect("r2"), t.plusSeconds(60))
        remote.put(SyncTable.Defects, defect("r3"), t.plusSeconds(120))
        engine(pageSize = 2).drain()
        assertEquals(setOf("r1", "r2", "r3"), localDefectIds("r"))

        remote.put(SyncTable.Defects, defect("r4"), t.plusSeconds(180))
        remote.put(
            SyncTable.Defects,
            defect("r3", mapOf("title" to "Moved"), mapOf("title" to FieldStamp(10, "devB"))),
            t.plusSeconds(240),
        )
        remote.listCursors.clear()
        engine(pageSize = 2).drain()
        val first = remote.listCursors.first { it.first == SyncTable.Defects }.second!!
        assertEquals("", first.id)
        assertEquals(t.plusSeconds(115), FakeRemoteGateway.instant(first.updatedAt))
        assertEquals(setOf("r1", "r2", "r3", "r4"), localDefectIds("r"))
        assertEquals("Moved", text(record(SyncTable.Defects, "r3"), "title"))

        remote.remove(SyncTable.Defects, "r3")
        remote.put(SyncTable.Defects, defect("r5"), t.plusSeconds(300))
        engine(pageSize = 2).drain()
        assertTrue("r5" in localDefectIds("r"))
    }

    private suspend fun localDefectIds(prefix: String): Set<String> =
        db.records().list(SyncTable.Defects.id, projectId).map { it.id }.filter { it.startsWith(prefix) }.toSet()

    @Test
    fun pullRereadsOverlapWindow() = runTest {
        putRemoteProject()
        val t = Instant.parse("2026-02-01T00:10:00Z")
        remote.put(SyncTable.Defects, defect("x"), t)
        engine().drain()

        remote.put(SyncTable.Defects, defect("late"), t.minusSeconds(3))
        remote.put(SyncTable.Defects, defect("tooLate"), t.minusSeconds(10))
        engine().drain()

        assertEquals(setOf("x", "late"), localDefectIds("") - fixtureDefectIds())
    }

    private suspend fun fixtureDefectIds(): Set<String> =
        db.records().list(SyncTable.Defects.id, projectId).map { it.id }
            .filter { it !in setOf("x", "late", "tooLate") }.toSet()

    @Test
    fun pullDropsUnknownRemoteFields() = runTest {
        putRemoteProject()
        remote.put(SyncTable.Defects, defect("u1", mapOf("evil" to "x", "bogus" to "y")))

        engine().drain()

        val fields = record(SyncTable.Defects, "u1").fields
        assertEquals(setOf("id", "title"), fields.keys)
    }

    @Test
    fun pullMergesAndBumpsDataVersionAndMaxSeen() = runTest {
        putRemoteProject()
        remote.put(SyncTable.Defects, defect("m1", stamps = mapOf("title" to FieldStamp(4_000, "devB"))))
        remote.put(SyncTable.Defects, defect("m2", stamps = mapOf("title" to FieldStamp(5_000, "devB"))))
        val before = state("dataVersion")?.toLong() ?: 0L

        val outcome = engine().drain()

        assertEquals(2, (outcome as SyncOutcome.Done).pulled)
        assertEquals(before + 1, state("dataVersion")!!.toLong())
        assertEquals("5000", state("maxSeenAt"))
        assertEquals(FieldStamp(4_000, "devB"), record(SyncTable.Defects, "m1").stamps["title"])

        engine().drain()
        assertEquals(before + 1, state("dataVersion")!!.toLong())
        assertNotNull(state("cursor:$projectId:defects"))
    }

    @Test
    fun bootstrapEnqueuesAllRecordsOnceWhenRemoteProjectMissing() = runTest {
        val total = SyncTable.entries.sumOf { db.records().list(it.id, projectId).size }

        val outcome = engine(DemoAuth("user", "bauherr")).drain()

        assertEquals(total, (outcome as SyncOutcome.Done).pushed)
        assertEquals(total, remote.upserts().size)
        assertEquals("upsertRow:projects/$projectId", remote.upserts().first())
        assertEquals("1", state("known:$projectId"))
        assertTrue(outbox().isEmpty())
        assertEquals(total, SyncTable.entries.sumOf { remote.rows(it).size })

        engine(DemoAuth("user", "architekt")).drain()
        assertEquals(total, remote.upserts().size)
    }

    @Test
    fun handwerkerNeverBootstraps() = runTest {
        for (role in listOf("handwerker", null)) {
            val outcome = engine(DemoAuth("user", role)).drain()

            assertEquals(SyncOutcome.Done(0, 0, 0, 0), outcome)
            assertTrue(remote.upserts().isEmpty())
            assertTrue(outbox().isEmpty())
            assertNull(state("known:$projectId"))
            assertEquals(projectId, db.records().projectIds().first())
        }
    }

    @Test
    fun failedBootstrapIsNotTreatedAsDeletion() = runTest {
        remote.failure = { op, table, _ ->
            if (op == "upsertRow" && table == SyncTable.Projects) RemoteException(RemoteErrorKind.Permanent, 400, "rejected") else null
        }

        val first = engine().drain() as SyncOutcome.Done
        assertEquals(1, first.failed)
        assertNull("after first drain", state("known:$projectId"))

        remote.failure = null
        engine().drain()

        assertEquals(listOf(projectId), db.records().projectIds())
        assertNull("after second drain", state("known:$projectId"))
        assertEquals(1, outbox().count { it.tableId == "projects" && it.state == OutboxState.FAILED })
        assertNull(remote.row(SyncTable.Projects, projectId))
    }

    @Test
    fun remoteProjectDeletionPurgesLocalProject() = runTest {
        putRemoteProject()
        engine().drain()
        assertEquals("1", state("known:$projectId"))
        edit(SyncTable.Trades, tradeIds().first(), "statusLabel", "A")
        files.newPhotoFile(projectId, "ph9").writeBytes(byteArrayOf(1))
        remote.remove(SyncTable.Projects, projectId)
        val before = state("dataVersion")?.toLong() ?: 0L

        val outcome = engine().drain()

        assertEquals(SyncOutcome.Done(0, 0, 0, 0), outcome)
        assertTrue(db.records().projectIds().isEmpty())
        assertEquals(0, SyncTable.entries.sumOf { db.records().list(it.id, projectId).size })
        assertTrue(outbox().isEmpty())
        assertFalse(files.projectDir(projectId).exists())
        assertTrue(state("dataVersion")!!.toLong() > before)
        assertTrue(remote.calls.none { it.startsWith("deleteRow") })
    }

    @Test
    fun missingTableOrDatabaseNeverPurgesAKnownProject() = runTest {
        putRemoteProject()
        engine().drain()
        edit(SyncTable.Trades, tradeIds().first(), "statusLabel", "A")
        // C2: a raw 404 `table_not_found` goes through the real classification and must keep everything.
        remote.failure = { op, table, _ ->
            if (op == "getRow" && table == SyncTable.Projects) {
                RemoteException(RemoteErrors.classify(404, null, "table_not_found"), 404, "table_not_found")
            } else {
                null
            }
        }

        val outcome = engine().drain()

        assertTrue(outcome is SyncOutcome.Retry)
        assertEquals(listOf(projectId), db.records().projectIds())
        assertEquals(1, outbox().count { it.tableId == "trades" })
    }

    @Test
    fun aRowNotFoundForARemovedMemberNeverPurges() = runTest {
        putRemoteProject()
        engine().drain()
        edit(SyncTable.Trades, tradeIds().first(), "statusLabel", "A")
        files.newPhotoFile(projectId, "ph9").writeBytes(byteArrayOf(1))
        // The user is out of the team: the project row exists but answers 404, and the member row is not readable.
        remote.remove(SyncTable.Projects, projectId)
        remote.access = false

        engine().drain()

        assertEquals(listOf(projectId), db.records().projectIds())
        assertEquals("1", state("known:$projectId"))
        assertEquals("A", text(record(SyncTable.Trades, tradeIds().first()), "statusLabel"))
        assertTrue(files.projectDir(projectId).exists())
    }

    @Test
    fun aRowNotFoundForAnExpiredSessionNeverPurgesAndAsksForALogin() = runTest {
        putRemoteProject()
        engine().drain()
        edit(SyncTable.Trades, tradeIds().first(), "statusLabel", "A")
        remote.remove(SyncTable.Projects, projectId)
        // A revoked session is a guest: account.get() answers 401 general_unauthorized_scope.
        remote.failure = { op, _, _ ->
            if (op == "hasAccess") {
                RemoteException(RemoteErrors.classify(401, null, "general_unauthorized_scope"), 401, "guest")
            } else {
                null
            }
        }
        val auth = DemoAuth("user", "bauherr")

        val outcome = engine(auth).drain()

        assertEquals(SyncOutcome.AuthRequired, outcome)
        assertNull(auth.cachedUserId)
        assertEquals(listOf(projectId), db.records().projectIds())
        assertEquals(1, outbox().count { it.tableId == "trades" })
    }

    @Test
    fun remoteDeletedFlagOnTheProjectRowIsNotMergedIntoAPush() = runTest {
        putRemoteProject()
        engine().drain()
        val tombstone = remote.row(SyncTable.Projects, projectId)!!
            .copy(deleted = true, stamps = mapOf("deleted" to FieldStamp(9_000_000_000_000L, "other")))
        remote.put(SyncTable.Projects, tombstone)
        edit(SyncTable.Projects, projectId, "address", "Neue Straße 1")

        engine().drain()

        assertFalse(record(SyncTable.Projects, projectId).deleted)
        assertFalse(remote.row(SyncTable.Projects, projectId)!!.deleted)
        assertEquals("Neue Straße 1", text(record(SyncTable.Projects, projectId), "address"))
    }

    @Test
    fun remoteDeletedFlagOnTheProjectRowIsIgnored() = runTest {
        // M4/I17: a project is deleted by a 404 only; a remote `deleted` flag would leave a project nothing can show.
        val tombstone = record(SyncTable.Projects, projectId).toRemoteRow()
            .copy(deleted = true, stamps = mapOf("deleted" to FieldStamp(9_000_000_000_000L, "other")))
        remote.put(SyncTable.Projects, tombstone)

        engine().drain()

        assertEquals(listOf(projectId), db.records().projectIds())
        assertFalse(record(SyncTable.Projects, projectId).deleted)
        assertFalse(record(SyncTable.Projects, projectId).stamps.containsKey("deleted"))
    }

    @Test
    fun pullDropsStampsOfUnknownKeys() = runTest {
        putRemoteProject()
        remote.put(
            SyncTable.Defects,
            defect("u2", stamps = mapOf("title" to FieldStamp(5, "x"), "junk" to FieldStamp(6, "x"))),
        )

        engine().drain()

        assertEquals(setOf("title"), record(SyncTable.Defects, "u2").stamps.keys)
    }

    @Test
    fun deleteProjectOpDeletesRemoteRowAfterLocalRecordsAreGone() = runTest {
        putRemoteProject()
        engine().drain()

        repo.deleteProject(projectId)
        assertTrue(db.records().projectIds().isEmpty())
        assertEquals(listOf(OutboxKind.DELETE_PROJECT), outbox().map { it.kind })

        val outcome = engine().drain()

        assertEquals(SyncOutcome.Done(1, 0, 0, 0), outcome)
        assertNull(remote.row(SyncTable.Projects, projectId))
        assertTrue(outbox().isEmpty())
    }

    @Test
    fun opsWrittenDuringDrainAreSentInSameRun() = runTest {
        putRemoteProject()
        val (a, b) = tradeIds()
        edit(SyncTable.Trades, a, "statusLabel", "A")
        var fired = false
        remote.beforeUpsert = { _, _ ->
            if (!fired) {
                fired = true
                edit(SyncTable.Trades, b, "statusLabel", "B")
            }
        }

        val outcome = engine().drain()

        assertEquals(SyncOutcome.Done(pushed = 2, pulled = 0, failed = 0, parked = 0), outcome)
        assertEquals(JsonPrimitive("B"), remote.row(SyncTable.Trades, b)!!.fields["statusLabel"])
        assertTrue(outbox().isEmpty())
    }

    @Test
    fun notConfiguredAndNotLoggedInAreNoOps() = runTest {
        edit(SyncTable.Trades, tradeIds().first(), "statusLabel", "A")

        assertEquals(SyncOutcome.NotConfigured, engine(gateway = null).drain())
        assertEquals(SyncOutcome.NotLoggedIn, engine(DemoAuth(null, null)).drain())

        assertTrue(remote.calls.isEmpty())
        assertEquals(1, outbox().size)
    }
}
