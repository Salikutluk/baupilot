package de.baupilot.sync

import java.io.File
import kotlinx.serialization.json.JsonObject

/**
 * One row of a remote table. [fields] holds the fixture and extra columns as JSON, [stamps] the per-field
 * last-write-wins stamps, [updatedAt] the server's `$updatedAt` (null for a row that was never stored).
 */
data class RemoteRow(
    val id: String,
    val projectId: String,
    val sortKey: Long,
    val fields: JsonObject,
    val stamps: Map<String, FieldStamp>,
    val deleted: Boolean,
    val updatedAt: String?,
)

/** A VALUE pair of `$updatedAt` and `$id`, never a document cursor: it stays valid when its row moves or vanishes. */
data class PullCursor(val updatedAt: String, val id: String)

interface RemoteGateway {
    /**
     * True when the session is authenticated as [userId] and [userId] has a live member row of [projectId]. A removed
     * member or an expired session (answered as a guest) can no longer read the project, and Appwrite then answers
     * `row_not_found` for rows that exist, so a 404 alone is no proof of deletion (D19). An unauthenticated session
     * throws a [RemoteException] of kind Auth.
     */
    suspend fun hasAccess(projectId: String, userId: String): Boolean

    /** `null` when the row does not exist (404). */
    suspend fun getRow(table: SyncTable, id: String): RemoteRow?

    suspend fun upsertRow(table: SyncTable, row: RemoteRow): RemoteRow

    /** Rows of [projectId] strictly after [after] in `($updatedAt, $id)` order, at most [limit]. */
    suspend fun listRows(table: SyncTable, projectId: String, after: PullCursor?, limit: Int): List<RemoteRow>

    /** A missing row (404) is success. */
    suspend fun deleteRow(table: SyncTable, id: String)

    /** An existing file id (409) is success. */
    suspend fun uploadFile(bucket: SyncBucket, fileId: String, name: String, file: File, mime: String)
}

internal fun SyncRecord.toRemoteRow(): RemoteRow = RemoteRow(
    id = id,
    projectId = projectId,
    sortKey = sortKey,
    fields = fields,
    stamps = stamps,
    deleted = deleted,
    updatedAt = null,
)

internal fun RemoteRow.toSyncRecord(table: SyncTable): SyncRecord = SyncRecord(
    table = table,
    id = id,
    projectId = projectId,
    sortKey = sortKey,
    fields = fields,
    stamps = stamps,
    deleted = deleted,
)
