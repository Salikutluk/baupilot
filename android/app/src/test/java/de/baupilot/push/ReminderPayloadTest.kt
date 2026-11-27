package de.baupilot.push

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderPayloadTest {
    @Test
    fun parsesDataAndNotificationText() {
        val p = ReminderPayloads.parse(
            mapOf("target" to "defects", "projectId" to "p1"),
            "Fristen-Erinnerung",
            "2 Mängel mit Frist bis 20.12.2026",
        )
        assertEquals(ReminderPayload("Fristen-Erinnerung", "2 Mängel mit Frist bis 20.12.2026", "defects", "p1"), p)
    }

    @Test
    fun missingTargetFallsBackToDefects() {
        val p = ReminderPayloads.parse(mapOf("projectId" to "p1"), "T", "B")
        assertEquals("defects", p?.target)
    }

    @Test
    fun targetOutsideAllowListBecomesDefects() {
        assertEquals("defects", ReminderPayloads.parse(mapOf("target" to "trade/windows"), "T", "B")?.target)
    }

    @Test
    fun unknownKeysAreIgnored() {
        val p = ReminderPayloads.parse(
            mapOf("target" to "defects", "defectTitle" to "Riss", "address" to "Musterweg 1"),
            "T",
            "B",
        )
        assertEquals(ReminderPayload("T", "B", "defects", null), p)
    }

    @Test
    fun titleAndBodyFallBackToDataKeys() {
        val p = ReminderPayloads.parse(mapOf("title" to "DT", "body" to "DB"), null, null)
        assertEquals("DT", p?.title)
        assertEquals("DB", p?.body)
    }

    @Test
    fun blankProjectIdIsNull() {
        assertNull(ReminderPayloads.parse(mapOf("projectId" to " "), "T", "B")?.projectId)
    }

    @Test
    fun missingTextYieldsNull() {
        assertNull(ReminderPayloads.parse(emptyMap(), null, "B"))
        assertNull(ReminderPayloads.parse(emptyMap(), "T", ""))
    }
}
