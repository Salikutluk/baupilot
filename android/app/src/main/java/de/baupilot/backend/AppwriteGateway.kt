package de.baupilot.backend

import de.baupilot.sync.PullCursor
import de.baupilot.sync.RemoteErrorKind
import de.baupilot.sync.RemoteErrors
import de.baupilot.sync.RemoteException
import de.baupilot.sync.RemoteGateway
import de.baupilot.sync.RemoteRow
import de.baupilot.sync.RowCodec
import de.baupilot.sync.SyncBucket
import de.baupilot.sync.SyncSchema
import de.baupilot.sync.SyncTable
import de.baupilot.sync.TableSpec
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.InputFile
import io.appwrite.models.Row
import io.appwrite.services.Account
import io.appwrite.services.Storage
import io.appwrite.services.TablesDB
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CancellationException

/**
 * Thin call-through to the Appwrite `TablesDB` and `Storage` services. The queries are built by [PullQuery],
 * the system attributes are dropped by [AppwriteMapping], the column mapping is [RowCodec]. Every SDK failure
 * is classified with [RemoteErrors.classify] (by HTTP code and Appwrite error type). File names follow
 * `<projectId>/photos/<photoId>.jpg`, `<projectId>/documents/<id>.pdf` and `<projectId>/protocols/<id>.pdf`; the caller
 * passes the name and it is sent as the stored file name (the cleanup backstop matches on the `<projectId>/` prefix).
 */
class AppwriteGateway(private val client: Client) : RemoteGateway {
    private val tables = TablesDB(client)
    private val storage = Storage(client)
    private val account = Account(client)

    override suspend fun hasAccess(projectId: String, userId: String): Boolean {
        val user = call { account.get() }
        if (user.id != userId) return false
        val queries = PullQuery.member(projectId, userId)
        val members = call { tables.listRows(SyncSchema.DATABASE_ID, SyncTable.Members.id, queries) }
        return members.rows.any { !toRemoteRow(SyncTable.Members, it).deleted }
    }

    override suspend fun getRow(table: SyncTable, id: String): RemoteRow? = try {
        toRemoteRow(table, call { tables.getRow(SyncSchema.DATABASE_ID, table.id, id) })
    } catch (e: RemoteException) {
        if (e.kind == RemoteErrorKind.NotFound) null else throw e
    }

    override suspend fun upsertRow(table: SyncTable, row: RemoteRow): RemoteRow {
        val data = RowCodec.toData(row, spec(table))
        return toRemoteRow(table, call { tables.upsertRow(SyncSchema.DATABASE_ID, table.id, row.id, data) })
    }

    override suspend fun listRows(
        table: SyncTable,
        projectId: String,
        after: PullCursor?,
        limit: Int,
    ): List<RemoteRow> {
        val queries = PullQuery.build(projectId, after, limit)
        val list = call { tables.listRows(SyncSchema.DATABASE_ID, table.id, queries) }
        return list.rows.map { toRemoteRow(table, it) }
    }

    override suspend fun deleteRow(table: SyncTable, id: String) {
        try {
            call { tables.deleteRow(SyncSchema.DATABASE_ID, table.id, id) }
        } catch (e: RemoteException) {
            if (e.kind != RemoteErrorKind.NotFound) throw e
        }
    }

    override suspend fun uploadFile(bucket: SyncBucket, fileId: String, name: String, file: File, mime: String) {
        val input = InputFile.fromFile(file).apply {
            filename = name
            mimeType = mime
        }
        try {
            call {
                if (file.length() >= CHUNK_SIZE) createLargeFile(bucket, fileId, input) else storage.createFile(bucket.id, fileId, input)
            }
        } catch (e: RemoteException) {
            if (e.kind != RemoteErrorKind.Conflict) throw e
        }
    }

    /**
     * `Storage.createFile` sends a file of [CHUNK_SIZE] or more through `Client.chunkedUpload` with an id parameter name,
     * and that first asks the server for the file by id, which answers 404 for a new id and aborts the upload (SDK 25.2.0).
     * Calling `chunkedUpload` without the id parameter name skips that look-up; the request is otherwise the SDK's own.
     */
    private suspend fun createLargeFile(bucket: SyncBucket, fileId: String, input: InputFile) {
        client.chunkedUpload(
            path = "/storage/buckets/${bucket.id}/files",
            headers = mutableMapOf(
                "X-Appwrite-Project" to (client.config["project"] ?: ""),
                "content-type" to "multipart/form-data",
                "accept" to "application/json",
            ),
            params = mutableMapOf<String, Any?>("fileId" to fileId, "file" to input),
            responseType = io.appwrite.models.File::class.java,
            converter = { io.appwrite.models.File.from(@Suppress("UNCHECKED_CAST") (it as Map<String, Any>)) },
            paramName = "file",
            idParamName = null,
            onProgress = null,
        )
    }

    private fun spec(table: SyncTable): TableSpec =
        SyncSchema.tables[table] ?: throw IllegalArgumentException("No schema for ${table.id}")

    private fun toRemoteRow(table: SyncTable, row: Row<Map<String, Any>>): RemoteRow =
        RowCodec.fromData(row.id, AppwriteMapping.columns(row.data), row.updatedAt, spec(table))

    private suspend fun <T> call(block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: AppwriteException) {
        throw RemoteException(RemoteErrors.classify(e.code, e, e.type), e.code, e.message ?: "Appwrite error", e)
    } catch (e: IOException) {
        throw RemoteException(RemoteErrors.classify(null, e), null, e.message ?: "Network error", e)
    }

    private companion object {
        /** The SDK's own chunk size: files of this size or more are uploaded in chunks. */
        const val CHUNK_SIZE = 5_242_880L
    }
}
