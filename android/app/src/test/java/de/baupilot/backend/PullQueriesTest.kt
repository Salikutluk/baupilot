package de.baupilot.backend

import de.baupilot.sync.PullCursor
import io.appwrite.Query
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PullQueriesTest {
    private val updatedAt = "\$updatedAt"
    private val id = "\$id"

    @Test
    fun withoutCursorItFiltersOrdersAndLimits() {
        assertEquals(
            listOf(
                Query.equal("projectId", "p1"),
                Query.orderAsc(updatedAt),
                Query.orderAsc(id),
                Query.limit(100),
            ),
            PullQuery.build("p1", null, 100),
        )
    }

    @Test
    fun withCursorItAddsTheValueKeyset() {
        val keyset = Query.or(
            listOf(
                Query.greaterThan(updatedAt, "2026-01-01T00:00:00.000+00:00"),
                Query.and(
                    listOf(
                        Query.equal(updatedAt, "2026-01-01T00:00:00.000+00:00"),
                        Query.greaterThan(id, "row-9"),
                    ),
                ),
            ),
        )
        assertEquals(
            listOf(
                Query.equal("projectId", "p1"),
                keyset,
                Query.orderAsc(updatedAt),
                Query.orderAsc(id),
                Query.limit(25),
            ),
            PullQuery.build("p1", PullCursor("2026-01-01T00:00:00.000+00:00", "row-9"), 25),
        )
    }

    @Test
    fun neverUsesADocumentCursor() {
        val queries = PullQuery.build("p1", PullCursor("2026-01-01T00:00:00.000+00:00", "row-9"), 100)
        assertFalse(queries.any { it.contains("cursorAfter") || it.contains("cursorBefore") })
    }
}
