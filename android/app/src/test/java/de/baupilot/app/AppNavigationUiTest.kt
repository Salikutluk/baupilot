package de.baupilot.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.baupilot.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Runs on the JVM under Robolectric (D12 stage b). Identifiers are the §5.6 test tags. The display is the 411 x 891 dp
 * phone the screens are laid out for (Robolectric's default 320 x 470 dp pushes the pinned bottom controls off screen).
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class AppNavigationUiTest {
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

    private val tabScreens = listOf(
        "tab.home" to "screen.home",
        "tab.schedule" to "screen.schedule",
        "tab.inspections" to "screen.inspections",
        "tab.documents" to "screen.documents",
        "tab.profile" to "screen.profile",
    )

    @Test
    fun loginLeadsToOverview() {
        awaitScreen("login.submit")
        rule.onNodeWithTag("login.submit").performClick()
        awaitScreen("screen.home")
        rule.onAllNodesWithTag("screen.login").assertCountEquals(0)
    }

    @Test
    fun tabBarSwitchesAllFiveTabs() {
        logIn()
        // Visit every tab, then return to the first one, so each switch is checked from a different tab.
        (tabScreens + tabScreens.first()).forEach { (tab, screen) ->
            rule.onNodeWithTag(tab).performClick()
            awaitScreen(screen)
            tabScreens.map { it.second }.filter { it != screen }.forEach { other ->
                rule.onAllNodesWithTag(other).assertCountEquals(0)
            }
        }
    }

    @Test
    fun pushHidesTabBarAndBackReturnsToRoot() {
        logIn()
        rule.onNodeWithContentDescription("Mitteilungen, 2 neu").performClick()
        awaitScreen("screen.notifications")
        tabScreens.forEach { (tab, _) -> rule.onAllNodesWithTag(tab).assertCountEquals(0) }
        rule.onNodeWithTag("nav.back").performClick()
        awaitScreen("screen.home")
        tabScreens.forEach { (tab, _) -> rule.onNodeWithTag(tab).assertExists() }
    }

    @Test
    fun systemBackPopsPushedRoute() {
        logIn()
        rule.onNodeWithContentDescription("Mitteilungen, 2 neu").performClick()
        awaitScreen("screen.notifications")
        systemBack()
        awaitScreen("screen.home")
        rule.onAllNodesWithTag("screen.notifications").assertCountEquals(0)
    }

    @Test
    fun documentTileOpensDocumentsTab() {
        logIn()
        rule.onNode(hasText("Angebote")).performClick()
        awaitScreen("screen.documents")
        rule.onNodeWithTag("tab.documents").assertIsSelected()
        rule.onAllNodesWithTag("screen.home").assertCountEquals(0)
    }

    @Test
    fun profileSwitchSurvivesTabSwitch() {
        logIn()
        rule.onNodeWithTag("tab.profile").performClick()
        awaitScreen("screen.profile")
        rule.onNodeWithContentDescription("Wochenrückblick").assertIsOff()
        rule.onNodeWithContentDescription("Wochenrückblick").performClick()
        rule.onNodeWithContentDescription("Wochenrückblick").assertIsOn()
        rule.onNodeWithTag("tab.home").performClick()
        awaitScreen("screen.home")
        rule.onNodeWithTag("tab.profile").performClick()
        awaitScreen("screen.profile")
        rule.onNodeWithContentDescription("Wochenrückblick").assertIsOn()
    }

    @Test
    fun backAtTabRootIsNotConsumed() {
        logIn()
        var consumed = true
        rule.activityRule.scenario.onActivity { consumed = it.onBackPressedDispatcher.hasEnabledCallbacks() }
        assertFalse("back must not be consumed at a tab root (R14)", consumed)
        var finishing = false
        rule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
            finishing = it.isFinishing
        }
        assertTrue("the system default finishes the activity", finishing)
    }

    @Test
    fun stateSurvivesRecreation() {
        logIn()
        rule.onNodeWithTag("tab.schedule").performClick()
        awaitScreen("screen.schedule")
        rule.activityRule.scenario.recreate()
        awaitScreen("screen.schedule")
        rule.onAllNodesWithTag("screen.login").assertCountEquals(0)
        rule.onNodeWithTag("tab.schedule").assertExists()
    }

    @Test
    fun placeholderLinksHaveNoAction() {
        logIn()
        rule.onNodeWithText("Alle").performClick()
        rule.waitForIdle()
        assertEquals(true, exists("screen.home"))
        rule.onNodeWithText("Dokument").performClick()
        rule.waitForIdle()
        assertEquals(true, exists("screen.home"))
        tabScreens.drop(1).forEach { (_, screen) -> rule.onAllNodesWithTag(screen).assertCountEquals(0) }
        rule.onAllNodesWithTag("screen.notifications").assertCountEquals(0)
    }
}
