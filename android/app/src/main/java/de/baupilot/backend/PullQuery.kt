package de.baupilot.backend

import de.baupilot.sync.PullCursor
import io.appwrite.Query

/**
 * Pure builder of the `listRows` queries: the project filter, the keyset on the VALUES of `$updatedAt` and
 * `$id` when a cursor is given, both orderings and the limit. It never emits `cursorAfter`: a document cursor
 * follows its row's current position, so a row updated or deleted between two drains would skip rows or fail.
 */
object PullQuery {
    private const val UPDATED_AT = "\$updatedAt"
    private const val ID = "\$id"

    fun build(projectId: String, after: PullCursor?, limit: Int): List<String> {
        val queries = mutableListOf(Query.equal("projectId", projectId))
        if (after != null) {
            queries += Query.or(
                listOf(
                    Query.greaterThan(UPDATED_AT, after.updatedAt),
                    Query.and(
                        listOf(
                            Query.equal(UPDATED_AT, after.updatedAt),
                            Query.greaterThan(ID, after.id),
                        ),
                    ),
                ),
            )
        }
        queries += Query.orderAsc(UPDATED_AT)
        queries += Query.orderAsc(ID)
        queries += Query.limit(limit)
        return queries
    }

    /** The member row of [userId] in [projectId] (unique index `projectId, userId`). */
    fun member(projectId: String, userId: String): List<String> =
        listOf(Query.equal("projectId", projectId), Query.equal("userId", userId), Query.limit(1))
}
