package de.baupilot.app

import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import de.baupilot.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * M6 later trades flows on the JVM under Robolectric, same display as [EarlyTradesUiTest] (plan section 9 UI).
 * Identifiers are the section 5.5 test tags. Nothing is written back.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class LaterTradesUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun exists(tag: String): Boolean = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun awaitScreen(tag: String) {
        rule.waitUntil(timeoutMillis = 5_000) { exists(tag) }
        rule.onNodeWithTag(tag).assertExists()
    }

    private fun assertAbsent(tag: String) {
        rule.waitForIdle()
        assertEquals("$tag must not be in the composition", 0, rule.onAllNodesWithTag(tag).fetchSemanticsNodes().size)
    }

    private fun tap(tag: String) {
        awaitScreen(tag)
        rule.onNodeWithTag(tag).performClick()
        rule.waitForIdle()
    }

    private fun openInspections() {
        awaitScreen("login.submit")
        rule.onNodeWithTag("login.submit").performClick()
        awaitScreen("screen.home")
        tap("tab.inspections")
        awaitScreen("inspections.view.trades")
    }

    private fun openTrade(tradeId: String) {
        openInspections()
        tap("inspections.trade.$tradeId")
        awaitScreen("screen.inspection.$tradeId")
    }

    private fun openSlope() {
        openTrade("plumbing")
        tap("overview.row.0.0")
        awaitScreen("screen.inspection.plumbing.slope.A3")
    }

    private fun openFlatness() {
        openTrade("screed")
        tap("overview.row.0.2")
        awaitScreen("screen.inspection.screed.flatness")
    }

    private fun openTilesBath() {
        openTrade("tiles")
        tap("overview.row.1.0")
        awaitScreen("screen.inspection.tiles.room.bath")
    }

    private fun assertText(tag: String, text: String) {
        rule.onNode(hasTestTag(tag) and hasAnyDescendant(hasText(text)) or (hasTestTag(tag) and hasText(text))).assertExists()
    }

    /** The count label is "Gemessen (n)" on Android and "GEMESSEN (n)" on iOS: compare the number case-insensitively. */
    private fun assertCount(tag: String, count: Int) {
        val text = "($count)"
        rule.onNode(
            hasTestTag(tag) and hasAnyDescendant(hasText(text, substring = true, ignoreCase = true)) or
                (hasTestTag(tag) and hasText(text, substring = true, ignoreCase = true)),
        ).assertExists()
    }

    @Test
    fun slopeCalculatorUpdatesLive() {
        openSlope()
        tap("slope.drop.decrement")
        tap("slope.drop.decrement")
        assertText("slope.drop.value", "1,5 cm")
        assertText("slope.percent", "0,75 %")
        assertText("slope.verdict", "Zu flach · Soll ≥ 1,0 %")
        tap("slope.drop.increment")
        assertText("slope.percent", "1,00 %")
        assertText("slope.verdict", "Ausreichend · Soll ≥ 1,0 %")
    }

    @Test
    fun flatnessSaveAddsPoint() {
        openFlatness()
        tap("flatness.gap.increment")
        tap("flatness.gap.increment")
        assertText("flatness.result", "Über Grenzwert – als Mangel erfassen")
        tap("flatness.save")
        assertText("flatness.point.3", "M4 · 5 mm")
        assertCount("flatness.count", 4)
        assertText("flatness.defect", "2 Messpunkte über Grenzwert → Mangel erfassen")
    }

    @Test
    fun plumbingLinks() {
        openTrade("plumbing")
        tap("overview.row.0.0")
        awaitScreen("screen.inspection.plumbing.slope.A3")
        tap("nav.back")
        awaitScreen("screen.inspection.plumbing")
        tap("overview.row.0.1")
        awaitScreen("screen.inspection.plumbing.pressure")
        tap("nav.close")
        awaitScreen("screen.inspection.plumbing")
        tap("overview.row.2.0")
        awaitScreen("screen.inspection.plumbing.room.bathUpper")
        tap("nav.back")
        awaitScreen("screen.inspection.plumbing")
        tap("overview.row.0.2")
        awaitScreen("screen.inspection.plumbing")
        assertAbsent("screen.inspection.plumbing.slope.A3")
    }

    @Test
    fun screedLinks() {
        openTrade("screed")
        tap("overview.row.0.2")
        awaitScreen("screen.inspection.screed.flatness")
        tap("nav.back")
        awaitScreen("screen.inspection.screed")
        tap("overview.row.1.0")
        awaitScreen("screen.inspection.screed.readiness")
        tap("nav.back")
        awaitScreen("screen.inspection.screed")
        tap("overview.row.1.1")
        awaitScreen("screen.inspection.screed.readiness")
        tap("nav.back")
        awaitScreen("screen.inspection.screed")
        tap("overview.row.0.3")
        awaitScreen("screen.inspection.screed")
        assertAbsent("screen.inspection.screed.readiness")
    }

    @Test
    fun tilesAndPaintingLinks() {
        openTilesBath()
        tap("tiles.tile.tile-3")
        awaitScreen("screen.inspection.tiles.room.bath")
        tap("nav.close")
        awaitScreen("screen.inspection.tiles")
        tap("nav.back")
        awaitScreen("screen.inspections")
        tap("inspections.trade.painting")
        awaitScreen("screen.inspection.painting")
        tap("overview.row.0.0")
        awaitScreen("screen.inspection.painting.room.living")
        tap("nav.close")
        awaitScreen("screen.inspection.painting")
    }

    @Test
    fun defectFromM6ScreensReturnsToOpener() {
        openSlope()
        tap("slope.defect")
        awaitScreen("screen.defect.new")
        tap("defect.save")
        awaitScreen("screen.inspection.plumbing.slope.A3")
        tap("nav.back")
        awaitScreen("screen.inspection.plumbing")
        tap("nav.back")
        awaitScreen("screen.inspections")
        tap("inspections.trade.tiles")
        awaitScreen("screen.inspection.tiles")
        tap("overview.row.1.0")
        awaitScreen("screen.inspection.tiles.room.bath")
        tap("tiles.defect")
        awaitScreen("screen.defect.new")
        tap("nav.close")
        awaitScreen("screen.inspection.tiles.room.bath")
    }
}
