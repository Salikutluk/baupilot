package de.baupilot.sync

enum class RemoteErrorKind { Transient, Auth, Forbidden, NotFound, Conflict, Permanent }

class RemoteException(
    val kind: RemoteErrorKind,
    val code: Int?,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

object RemoteErrors {
    private const val HTTP_UNAUTHORIZED = 401
    private const val HTTP_FORBIDDEN = 403
    private const val HTTP_NOT_FOUND = 404
    private const val HTTP_REQUEST_TIMEOUT = 408
    private const val HTTP_CONFLICT = 409
    private const val HTTP_TOO_MANY_REQUESTS = 429
    private const val HTTP_SERVER_ERROR = 500

    /** Appwrite types of a 404 that mean a deploy or console mistake, not a missing row: retry once the operator fixed it. */
    private val MISCONFIGURED = setOf(
        "database_not_found", "table_not_found", "collection_not_found", "project_not_found",
        "storage_bucket_not_found", "bucket_not_found",
    )

    /**
     * No code (a network failure, [cause] being the IOException) is transient; so are 408, 429 and 5xx. Any other 4xx is
     * permanent. [type] is the Appwrite error type: a 404 is [RemoteErrorKind.NotFound] only for `row_not_found` (any other
     * 404 must never be taken for "the row is gone", D19), and a 401 `user_unauthorized` is a missing permission
     * ([RemoteErrorKind.Forbidden], R16), not an expired session.
     */
    fun classify(code: Int?, cause: Throwable?, type: String? = null): RemoteErrorKind = when {
        code == null -> RemoteErrorKind.Transient
        code == HTTP_UNAUTHORIZED -> if (type == TYPE_UNAUTHORIZED) RemoteErrorKind.Forbidden else RemoteErrorKind.Auth
        code == HTTP_FORBIDDEN -> RemoteErrorKind.Forbidden
        code == HTTP_NOT_FOUND -> when {
            type == TYPE_ROW_NOT_FOUND -> RemoteErrorKind.NotFound
            type in MISCONFIGURED -> RemoteErrorKind.Transient
            else -> RemoteErrorKind.Permanent
        }
        code == HTTP_CONFLICT -> RemoteErrorKind.Conflict
        code == HTTP_REQUEST_TIMEOUT || code == HTTP_TOO_MANY_REQUESTS || code >= HTTP_SERVER_ERROR ->
            RemoteErrorKind.Transient
        else -> RemoteErrorKind.Permanent
    }

    private const val TYPE_UNAUTHORIZED = "user_unauthorized"
    private const val TYPE_ROW_NOT_FOUND = "row_not_found"
}
