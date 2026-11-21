package de.baupilot.app

import de.baupilot.designsystem.components.PAGE_TEXT_TAG
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure JVM test of [AuditAllowlist.unmatched] (G-11) against a fixture list, not the real entries. */
class AuditAllowlistTest {
    private val entries = listOf(
        AllowedFinding("Home", SemanticsAudit.TOUCH_TARGET, "tile", "both modes"),
        AllowedFinding("Home", SemanticsAudit.NO_LABEL, "close", "light only", dark = false),
        AllowedFinding("Home", SemanticsAudit.NO_LABEL, "menu", "dark only", dark = true),
        AllowedFinding("Login", SemanticsAudit.TOUCH_TARGET, "tile", "other screen"),
    )

    private fun finding(rule: String, tag: String) = Finding(rule, tag, null, "0,0,10,10")

    @Test
    fun unmatchedListsEntriesNoFindingUsed() {
        val findings = listOf(finding(SemanticsAudit.TOUCH_TARGET, "tile"), finding(SemanticsAudit.NO_LABEL, "close"))
        // light: "tile" and "close" are used; the dark-only "menu" and the Login entry are not for this (screen, mode).
        assertEquals(emptyList<AllowedFinding>(), AuditAllowlist.unmatched("Home", dark = false, findings = findings, entries = entries))
        // dark: "close" is light-only (out of scope); "menu" is in scope and unused.
        assertEquals(
            listOf(entries[2]),
            AuditAllowlist.unmatched("Home", dark = true, findings = listOf(finding(SemanticsAudit.TOUCH_TARGET, "tile")), entries = entries),
        )
    }

    @Test
    fun ruleMismatchLeavesEntryUnmatched() {
        val findings = listOf(finding(SemanticsAudit.NO_LABEL, "tile"), finding(SemanticsAudit.NO_LABEL, "close"))
        assertEquals(listOf(entries[0]), AuditAllowlist.unmatched("Home", dark = false, findings = findings, entries = entries))
    }

    @Test
    fun isAllowedHonoursTheModeOfAnEntry() {
        val menu = finding(SemanticsAudit.NO_LABEL, "menu")
        assertEquals(false, AuditAllowlist.isAllowed("Home", dark = false, finding = menu, entries = entries))
        assertEquals(true, AuditAllowlist.isAllowed("Home", dark = true, finding = menu, entries = entries))
        val tile = finding(SemanticsAudit.TOUCH_TARGET, "tile")
        assertEquals(true, AuditAllowlist.isAllowed("Home", dark = false, finding = tile, entries = entries))
        assertEquals(true, AuditAllowlist.isAllowed("Home", dark = true, finding = tile, entries = entries))
    }

    @Test
    fun emptyAllowlistHasNothingUnmatched() {
        assertEquals(emptyList<AllowedFinding>(), AuditAllowlist.unmatched("Home", dark = true, findings = emptyList(), entries = emptyList()))
    }

    @Test
    fun fixedGeometryGapAllowsOnlyTheMeasuredHeightOverflowOfTheDokViewerPageText() {
        assertTrue(LargeFontAllowlist.allowsGap("DokViewer", PAGE_TEXT_TAG, 1.0))
        assertFalse(LargeFontAllowlist.allowsGap("DokViewer", PAGE_TEXT_TAG, 1.01))
        assertFalse(LargeFontAllowlist.allowsGap("Home", PAGE_TEXT_TAG, 1.0))
        assertFalse(LargeFontAllowlist.allowsGap("DokViewer", null, 1.0))
    }
}
