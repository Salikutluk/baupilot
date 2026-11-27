package de.baupilot.sync

import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * In-memory remote tables and buckets. `$updatedAt` is a fake clock that ticks one second per write; listing
 * implements the value keyset of plan §5.7, so it follows a row's value, not its position.
 */
class FakeRemoteGateway : RemoteGateway {
    var online = true

    /** Failure injection: return an exception to throw for `(operation, table, id)`, or null to let the call pass. */
    var failure: ((String, SyncTable?, String) -> RemoteException?)? = null

    /** Runs inside `upsertRow`, before the row is stored. */
    var beforeUpsert: (suspend (SyncTable, RemoteRow) -> Unit)? = null

    var clock: Instant = Instant.parse("2026-01-01T00:00:00Z")
    val calls = mutableListOf<String>()
    val listCursors = mutableListOf<Pair<SyncTable, PullCursor?>>()
    val uploads = mutableMapOf<String, String>()
    private val tables = mutableMapOf<SyncTable, MutableMap<String, RemoteRow>>()

    fun rows(table: SyncTable): Map<String, RemoteRow> = tables[table].orEmpty()

    fun row(table: SyncTable, id: String): RemoteRow? = tables[table]?.get(id)

    /** Stores [row] as if it had been written at [at] (default: one tick after the previous write). */
    fun put(table: SyncTable, row: RemoteRow, at: Instant? = null) {
        val stamp = at ?: tick()
        tables.getOrPut(table) { linkedMapOf() }[row.id] = row.copy(updatedAt = format(stamp))
    }

    fun remove(table: SyncTable, id: String) {
        tables[table]?.remove(id)
    }

    fun upserts(): List<String> = calls.filter { it.startsWith("upsertRow:") }

    private fun tick(): Instant {
        clock = clock.plusSeconds(1)
        return clock
    }

    private fun enter(op: String, table: SyncTable?, id: String) {
        calls += "$op:${table?.id}/$id"
        if (!online) throw RemoteException(RemoteErrorKind.Transient, null, "offline")
        failure?.invoke(op, table, id)?.let { throw it }
    }

    /** Whether the session may read the project's rows; a test sets false for a removed member. */
    var access = true

    override suspend fun hasAccess(projectId: String, userId: String): Boolean {
        enter("hasAccess", SyncTable.Members, projectId)
        return access
    }

    override suspend fun getRow(table: SyncTable, id: String): RemoteRow? {
        enter("getRow", table, id)
        return row(table, id)
    }

    override suspend fun upsertRow(table: SyncTable, row: RemoteRow): RemoteRow {
        enter("upsertRow", table, row.id)
        beforeUpsert?.invoke(table, row)
        put(table, row)
        return row(table, row.id)!!
    }

    override suspend fun listRows(table: SyncTable, projectId: String, after: PullCursor?, limit: Int): List<RemoteRow> {
        enter("listRows", table, projectId)
        listCursors += table to after
        return rows(table).values
            .filter { it.projectId == projectId }
            .filter { after == null || isAfter(it, after) }
            .sortedWith(compareBy<RemoteRow>({ instant(it.updatedAt!!) }, { it.id }))
            .take(limit)
    }

    override suspend fun deleteRow(table: SyncTable, id: String) {
        enter("deleteRow", table, id)
        remove(table, id)
    }

    override suspend fun uploadFile(bucket: SyncBucket, fileId: String, name: String, file: File, mime: String) {
        enter("uploadFile", null, fileId)
        uploads.putIfAbsent("${bucket.id}/$fileId", name)
    }

    private fun isAfter(row: RemoteRow, cursor: PullCursor): Boolean {
        val byTime = instant(row.updatedAt!!).compareTo(instant(cursor.updatedAt))
        return byTime > 0 || (byTime == 0 && row.id > cursor.id)
    }

    companion object {
        private val FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx")

        fun format(at: Instant): String = at.atOffset(ZoneOffset.UTC).format(FORMAT)

        fun instant(text: String): Instant = OffsetDateTime.parse(text).toInstant()
    }
}
