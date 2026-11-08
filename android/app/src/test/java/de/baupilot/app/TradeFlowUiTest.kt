package de.baupilot.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import de.baupilot.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * AC2 flows of M3 on the JVM under Robolectric (D12 stage b), same display as [AppNavigationUiTest]. Identifiers are the
 * §5.4 test tags of the real screens; the cases stay red until those screens exist.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class TradeFlowUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun exists(tag: String): Boolean = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun awaitScreen(tag: String) {
        rule.waitUntil(timeoutMillis = 5_000) { exists(tag) }
        rule.onNodeWithTag(tag).assertExists()
    }

    private fun logIn() {
        awaitScreen("login.submit")
        rule.onNodeWithTag("login.submit").performClick()
        awaitScreen("screen.home")
    }

    private fun systemBack() {
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
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

    private fun openEstrich() {
        logIn()
        rule.onNode(hasText("Estrich") and hasClickAction()).performClick()
        awaitScreen("screen.trade.screed")
    }

    private fun openCompare() {
        openEstrich()
        tap("trade.compare")
        awaitScreen("screen.compare.screed")
    }

    private fun openDocumentsTab() {
        logIn()
        tap("tab.documents")
        awaitScreen("screen.documents")
    }

    private fun openNotifications() {
        logIn()
        rule.onNodeWithContentDescription("Mitteilungen, 2 neu").performClick()
        awaitScreen("screen.notifications")
    }

    @Test
    fun gewerkCompareDocumentAndBack() {
        openCompare()
        tap("compare.column.offer-wagner")
        awaitScreen("screen.document.doc-offer-wagner")
        tap("nav.back")
        awaitScreen("screen.compare.screed")
        tap("nav.back")
        awaitScreen("screen.trade.screed")
        tap("nav.back")
        awaitScreen("screen.home")
        rule.onNodeWithTag("tab.home").assertExists()
    }

    @Test
    fun offerRowOpensDocument() {
        openEstrich()
        tap("trade.offer.offer-wagner")
        awaitScreen("screen.document.doc-offer-wagner")
        tap("nav.back")
        awaitScreen("screen.trade.screed")
        tap("trade.offer.offer-lorenz")
        awaitScreen("screen.trade.screed")
        rule.onAllNodesWithTag("screen.document.doc-offer-lorenz").assertCountEquals(0)
    }

    @Test
    fun docViewerCompareReturnsToExistingCompare() {
        openCompare()
        tap("compare.column.offer-wagner")
        awaitScreen("screen.document.doc-offer-wagner")
        tap("document.compare")
        awaitScreen("screen.compare.screed")
        tap("nav.back")
        awaitScreen("screen.trade.screed")
    }

    @Test
    fun bellOpensNotificationsAndCompareTarget() {
        openNotifications()
        tap("notification.notif-offer-hahn")
        awaitScreen("screen.compare.screed")
        tap("nav.back")
        awaitScreen("screen.notifications")
    }

    @Test
    fun notificationOpensTrade() {
        openNotifications()
        tap("notification.notif-offer-lorenz-missing")
        awaitScreen("screen.trade.screed")
    }

    @Test
    fun notificationSwitchesToDocumentsTab() {
        openNotifications()
        tap("notification.notif-invoice-due")
        awaitScreen("screen.documents")
        rule.onNodeWithTag("tab.documents").assertExists()
    }

    @Test
    fun notificationDefectsOpensInspectionsTab() {
        openNotifications()
        tap("notification.notif-defect-overdue")
        awaitScreen("screen.inspections")
    }

    @Test
    fun notificationWithoutTargetDoesNothing() {
        openNotifications()
        tap("notification.notif-electrical-confirmed")
        awaitScreen("screen.notifications")
        rule.onAllNodesWithTag("nav.back").assertCountEquals(1)
    }

    @Test
    fun documentsRowOpensViewerAndBack() {
        openDocumentsTab()
        tap("documents.row.doc-offer-wagner")
        awaitScreen("screen.document.doc-offer-wagner")
        tap("nav.back")
        awaitScreen("screen.documents")
        rule.onNodeWithTag("tab.documents").assertIsSelected()
    }

    @Test
    fun documentsChipsScrollAndFilter() {
        openDocumentsTab()
        scrollAndTap("documents.filter.plan")
        awaitScreen("documents.row.doc-plan-floor-og")
        assertTrue(!exists("documents.row.doc-offer-wagner"))
        scrollAndTap("documents.filter.all")
        awaitScreen("documents.row.doc-plan-floor-og")
        awaitScreen("documents.row.doc-offer-wagner")
    }

    @Test
    fun documentsFilterSurvivesViewerRoundTrip() {
        openDocumentsTab()
        scrollAndTap("documents.filter.offer")
        awaitScreen("documents.row.doc-offer-wagner")
        tap("documents.row.doc-offer-wagner")
        awaitScreen("screen.document.doc-offer-wagner")
        tap("nav.back")
        awaitScreen("screen.documents")
        awaitScreen("documents.row.doc-offer-hahn")
        assertTrue(!exists("documents.row.doc-contract-electrical"))
    }

    @Test
    fun documentsFilterSurvivesTabSwitch() {
        openDocumentsTab()
        scrollAndTap("documents.filter.offer")
        awaitScreen("documents.row.doc-offer-hahn")
        tap("tab.home")
        awaitScreen("screen.home")
        tap("tab.documents")
        awaitScreen("screen.documents")
        awaitScreen("documents.row.doc-offer-hahn")
        assertTrue(!exists("documents.row.doc-contract-electrical"))
    }

    @Test
    fun systemBackWalksNestedStack() {
        openCompare()
        tap("compare.column.offer-wagner")
        awaitScreen("screen.document.doc-offer-wagner")
        systemBack()
        awaitScreen("screen.compare.screed")
        systemBack()
        awaitScreen("screen.trade.screed")
        systemBack()
        awaitScreen("screen.home")
    }
}
