package de.baupilot.app

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertTextEquals
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
 * M5 early trades and electrical flows on the JVM under Robolectric, same display as [InspectionFlowUiTest]
 * (plan section 9 UI). Identifiers are the section 5.5 test tags. Nothing is written back (I6).
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class EarlyTradesUiTest {
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

    private fun openShell() {
        openInspections()
        tap("inspections.trade.shell")
        awaitScreen("screen.inspection.shell")
        awaitScreen("protocol.banner")
    }

    private fun openMeasure() {
        openShell()
        tap("protocol.row.1.0")
        awaitScreen("screen.inspection.shell.measure")
    }

    private fun openElectrical() {
        openInspections()
        tap("inspections.trade.electrical")
        awaitScreen("screen.inspection.electrical")
    }

    private fun toggleState(tag: String) =
        rule.onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.ToggleableState]

    private fun assertResult(text: String) {
        rule.onNode(hasTestTag("measure.result") and hasAnyDescendant(hasText(text))).assertExists()
    }

    @Test
    fun shellMeasureStepperUpdatesResultLive() {
        openMeasure()
        tap("measure.height.increment")
        rule.onNodeWithTag("measure.height.diff").assertTextEquals("+15 mm")
        assertResult("Außerhalb der Toleranz – Mangel erfassen")
        tap("measure.height.decrement")
        rule.onNodeWithTag("measure.height.diff").assertTextEquals("+10 mm")
        assertResult("Innerhalb der Toleranz")
    }

    @Test
    fun shellLinks() {
        openShell()
        tap("protocol.banner")
        awaitScreen("inspections.view.defects")
        assertAbsent("screen.inspection.shell")
        tap("inspections.segment.trades")
        awaitScreen("inspections.view.trades")
        tap("inspections.trade.shell")
        awaitScreen("screen.inspection.shell")
        tap("protocol.row.0.2")
        awaitScreen("inspections.view.defects")
        assertAbsent("screen.inspection.shell")
        tap("inspections.segment.trades")
        tap("inspections.trade.shell")
        awaitScreen("screen.inspection.shell")
        tap("protocol.row.0.0")
        tap("protocol.row.1.1")
        awaitScreen("screen.inspection.shell")
        assertAbsent("screen.defect.new")
        assertAbsent("inspections.view.defects")
    }

    @Test
    fun electricalLinks() {
        openElectrical()
        listOf(
            Triple("electrical.row.0.0", "screen.inspection.electrical.panel", "nav.close"),
            Triple("electrical.row.0.1", "screen.inspection.electrical.smoke", "nav.back"),
            Triple("electrical.row.1.0", "screen.inspection.electrical.room.kitchen", "nav.close"),
        ).forEach { (row, screen, leave) ->
            tap(row)
            awaitScreen(screen)
            tap(leave)
            awaitScreen("screen.inspection.electrical")
            assertAbsent(screen)
        }
        listOf("electrical.row.1.1", "electrical.row.2.0", "electrical.row.2.1").forEach { row ->
            tap(row)
            awaitScreen("screen.inspection.electrical")
            assertAbsent("screen.defect.new")
            assertAbsent("screen.inspection.electrical.panel")
            assertAbsent("screen.inspection.electrical.smoke")
            assertAbsent("screen.inspection.electrical.room.kitchen")
        }
    }

    @Test
    fun defectFromM5ScreensReturnsToOpener() {
        openMeasure()
        tap("measure.defect")
        awaitScreen("screen.defect.new")
        tap("defect.save")
        awaitScreen("screen.inspection.shell.measure")

        tap("nav.back")
        awaitScreen("screen.inspection.shell")
        tap("nav.back")
        awaitScreen("screen.inspections")
        awaitScreen("inspections.view.trades")
        tap("inspections.trade.electrical")
        awaitScreen("screen.inspection.electrical")
        tap("electrical.row.1.0")
        awaitScreen("screen.inspection.electrical.room.kitchen")
        tap("step.defect")
        awaitScreen("screen.defect.new")
        tap("nav.close")
        awaitScreen("screen.inspection.electrical.room.kitchen")
    }

    @Test
    fun smokeReminderToggles() {
        openElectrical()
        tap("electrical.row.0.1")
        awaitScreen("screen.inspection.electrical.smoke")
        assertEquals(ToggleableState.On, toggleState("smoke.reminder"))
        tap("smoke.reminder")
        assertEquals(ToggleableState.Off, toggleState("smoke.reminder"))
        tap("smoke.reminder")
        assertEquals(ToggleableState.On, toggleState("smoke.reminder"))
        tap("smoke.reminder")
        tap("step.defect")
        awaitScreen("screen.defect.new")
        tap("nav.close")
        awaitScreen("screen.inspection.electrical.smoke")
        assertEquals(ToggleableState.Off, toggleState("smoke.reminder"))
    }
}
