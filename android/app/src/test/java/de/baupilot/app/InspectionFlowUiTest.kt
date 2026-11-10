package de.baupilot.app

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.geometry.Offset
import de.baupilot.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Abnahme flow of M4 on the JVM under Robolectric, same display as [TradeFlowUiTest] (plan section 9 UI). Identifiers are
 * the section 5.4 test tags. Buttons in a pinned bottom bar or the FAB slot (`step.defect`, `sign.finish`,
 * `inspection.next`) are tapped directly: they sit outside the scrolling column, so `performScrollTo` finds no scroll parent.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class InspectionFlowUiTest {
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

    private fun logIn() {
        awaitScreen("login.submit")
        rule.onNodeWithTag("login.submit").performClick()
        awaitScreen("screen.home")
    }

    private fun tap(tag: String) {
        awaitScreen(tag)
        rule.onNodeWithTag(tag).performClick()
        rule.waitForIdle()
    }

    private fun scrollAndTap(tag: String) {
        awaitScreen(tag)
        rule.onNodeWithTag(tag).performScrollTo().performClick()
        rule.waitForIdle()
    }

    private fun openInspections() {
        logIn()
        tap("tab.inspections")
        awaitScreen("screen.inspections")
        awaitScreen("inspections.view.trades")
    }

    private fun openElements() {
        openInspections()
        tap("inspections.trade.windows")
        awaitScreen("screen.inspection.windows")
    }

    private fun openStep() {
        openElements()
        scrollAndTap("inspection.element.F8")
        awaitScreen("screen.inspection.windows.F8.step.3")
    }

    private fun openDefectFormFromStep() {
        openStep()
        tap("step.defect")
        awaitScreen("screen.defect.new")
    }

    private fun toggleState(tag: String) =
        rule.onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.ToggleableState]

    private fun drawOnOwnerTile() {
        rule.onNodeWithTag("sign.owner").performScrollTo().performTouchInput {
            swipe(Offset(width * 0.2f, height * 0.5f), Offset(width * 0.8f, height * 0.6f), durationMillis = 200)
        }
        rule.waitForIdle()
    }

    /** AC2: the window acceptance flow from the trade list to the open defects, nothing written back (I8). */
    @Test
    fun windowAcceptanceFlowEndsOnOpenDefects() {
        openDefectFormFromStep()
        tap("defect.severity.2")
        tap("defect.deadline.0")
        tap("defect.save")
        awaitScreen("screen.inspection.windows.sign")
        rule.onAllNodesWithText("Zum Unterschreiben tippen").assertCountEquals(1)
        drawOnOwnerTile()
        rule.onAllNodesWithText("Zum Unterschreiben tippen").assertCountEquals(0)
        tap("sign.finish")
        awaitScreen("screen.inspections")
        awaitScreen("inspections.view.defects")
        rule.onNodeWithTag("defect.defect-window-f2").assertExists()
        rule.onNode(hasTestTag("defect.defect-window-f2") and hasAnyDescendant(hasText("Frist 15.01.2027 · 2 Fotos")))
            .assertExists()
        rule.onNodeWithTag("tab.inspections").assertIsSelected()
    }

    @Test
    fun segmentsSwitchViews() {
        openInspections()
        tap("inspections.segment.defects")
        awaitScreen("inspections.view.defects")
        assertAbsent("inspections.view.trades")
        tap("inspections.segment.trades")
        awaitScreen("inspections.view.trades")
        assertAbsent("inspections.view.defects")
        tap("inspections.segment.defects")
        awaitScreen("inspections.view.defects")
        tap("tab.documents")
        awaitScreen("screen.documents")
        tap("tab.inspections")
        awaitScreen("inspections.view.defects")
    }

    @Test
    fun tradeRowsFollowFixtureRoutes() {
        openInspections()
        tap("inspections.trade.earthworks")
        awaitScreen("screen.inspection.earthworks")
        awaitScreen("protocol.banner")
        tap("nav.back")
        awaitScreen("screen.inspections")
        tap("inspections.trade.plumbing")
        awaitScreen("screen.inspection.plumbing")
        awaitScreen("overview.phase.0")
        assertAbsent("protocol.banner")
        tap("nav.back")
        awaitScreen("screen.inspections")
        tap("inspections.trade.roofing")
        awaitScreen("screen.inspections")
        assertAbsent("screen.inspection.roofing")
    }

    @Test
    fun cancelDefectReturnsToStep() {
        openDefectFormFromStep()
        tap("nav.close")
        awaitScreen("screen.inspection.windows.F8.step.3")
    }

    @Test
    fun cancelDefectFromF2ReturnsToElements() {
        openElements()
        scrollAndTap("inspection.element.F2")
        awaitScreen("screen.defect.new")
        tap("nav.close")
        awaitScreen("screen.inspection.windows")
    }

    @Test
    fun defectFormControls() {
        openDefectFormFromStep()
        tap("defect.severity.2")
        rule.onNodeWithTag("defect.severity.2").assertIsSelected()
        tap("defect.deadline.0")
        rule.onNode(hasTestTag("defect.deadline.value") and hasText("01.01.2027")).assertExists()
        val before = toggleState("defect.notify")
        tap("defect.notify")
        assertNotEquals(before, toggleState("defect.notify"))
    }

    @Test
    fun signOffBackReturnsToElements() {
        openDefectFormFromStep()
        tap("defect.save")
        awaitScreen("screen.inspection.windows.sign")
        tap("nav.back")
        awaitScreen("screen.inspection.windows")
    }

    @Test
    fun elementsNextOpensStep() {
        openElements()
        tap("inspection.next")
        awaitScreen("screen.inspection.windows.F8.step.3")
    }

    @Test
    fun elementF2OpensDefectForm() {
        openElements()
        scrollAndTap("inspection.element.F2")
        awaitScreen("screen.defect.new")
    }
}
