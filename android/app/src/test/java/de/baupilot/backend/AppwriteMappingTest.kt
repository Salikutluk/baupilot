package de.baupilot.backend

import org.junit.Assert.assertEquals
import org.junit.Test

class AppwriteMappingTest {
    @Test
    fun dropsSystemAttributesAndKeepsTheRest() {
        val data = mapOf(
            "\$id" to "r1",
            "\$updatedAt" to "2026-01-01T00:00:00.000+00:00",
            "\$permissions" to listOf("read(\"any\")"),
            "projectId" to "p1",
            "sortKey" to 3,
            "deleted" to false,
            "note" to null,
        )
        assertEquals(
            mapOf("projectId" to "p1", "sortKey" to 3, "deleted" to false, "note" to null),
            AppwriteMapping.columns(data),
        )
    }

    @Test
    fun emptyDataStaysEmpty() {
        assertEquals(emptyMap<String, Any?>(), AppwriteMapping.columns(emptyMap()))
    }
}
