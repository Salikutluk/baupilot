package de.baupilot.app

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.baupilot.BaupilotApplication
import de.baupilot.MainActivity
import de.baupilot.push.ReminderNotifier
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The `de.baupilot.target` extra is untrusted: `defects` is applied once logged in, anything else is ignored. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class DeepLinkUiTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun exists(tag: String): Boolean = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun launch(target: String?): ActivityScenario<MainActivity> {
        val intent = Intent(context, MainActivity::class.java)
        if (target != null) intent.putExtra(ReminderNotifier.EXTRA_TARGET, target)
        return ActivityScenario.launch(intent)
    }

    private fun logInThroughForm() {
        rule.waitUntil(timeoutMillis = 5_000) { exists("login.submit") }
        rule.onNodeWithTag("login.submit").performClick()
    }

    @Test
    fun targetAppliedAfterLogin() {
        launch("defects").use {
            logInThroughForm()
            rule.waitUntil(timeoutMillis = 5_000) { exists("inspections.view.defects") }
        }
    }

    @Test
    fun targetWhileLoggedOutWaitsForLogin() {
        launch("defects").use {
            rule.waitUntil(timeoutMillis = 5_000) { exists("login.submit") }
            rule.waitForIdle()
            assertFalse(exists("inspections.view.defects"))
            rule.onNodeWithTag("login.submit").performClick()
            rule.waitUntil(timeoutMillis = 5_000) { exists("inspections.view.defects") }
        }
    }

    @Test
    fun targetWithCachedSessionIsApplied() {
        runBlocking { (context as BaupilotApplication).services.auth.logIn("", "") }
        launch("defects").use {
            rule.waitUntil(timeoutMillis = 5_000) { exists("inspections.view.defects") }
        }
    }

    @Test
    fun newIntentWhileLoggedInIsApplied() {
        runBlocking { (context as BaupilotApplication).services.auth.logIn("", "") }
        launch(null).use { scenario ->
            rule.waitUntil(timeoutMillis = 5_000) { exists("screen.home") }
            val next = Intent(context, MainActivity::class.java).putExtra(ReminderNotifier.EXTRA_TARGET, "defects")
            scenario.onActivity { it.onNewIntent(next) }
            rule.waitUntil(timeoutMillis = 5_000) { exists("inspections.view.defects") }
        }
    }

    @Test
    fun pushDrawnBySystemInBackgroundOpensDefectsThroughThePlainTargetExtra() {
        runBlocking { (context as BaupilotApplication).services.auth.logIn("", "") }
        val intent = Intent(context, MainActivity::class.java).putExtra(MainActivity.FCM_TARGET, "defects")
        ActivityScenario.launch<MainActivity>(intent).use {
            rule.waitUntil(timeoutMillis = 5_000) { exists("inspections.view.defects") }
        }
        val other = Intent(context, MainActivity::class.java).putExtra(MainActivity.FCM_TARGET, "trade/windows")
        ActivityScenario.launch<MainActivity>(other).use {
            rule.waitUntil(timeoutMillis = 5_000) { exists("screen.home") }
            assertFalse(exists("inspections.view.defects"))
        }
    }

    @Test
    fun unknownTargetIsIgnored() {
        runBlocking { (context as BaupilotApplication).services.auth.logIn("", "") }
        for (value in listOf("trade/windows", "x", "", "Defects")) {
            launch(value).use {
                rule.waitUntil(timeoutMillis = 5_000) { exists("screen.home") }
                assertFalse(exists("inspections.view.defects"))
            }
        }
    }

    @Test
    fun extraOfAnotherTypeIsIgnored() {
        runBlocking { (context as BaupilotApplication).services.auth.logIn("", "") }
        val intent = Intent(context, MainActivity::class.java).putExtra(ReminderNotifier.EXTRA_TARGET, 42)
        ActivityScenario.launch<MainActivity>(intent).use {
            rule.waitUntil(timeoutMillis = 5_000) { exists("screen.home") }
            assertFalse(exists("inspections.view.defects"))
        }
    }

    @Test
    fun notificationPermissionOnlyForFreshLoginWithPushOnApi33() {
        val tiramisu = Build.VERSION_CODES.TIRAMISU
        assertTrue(shouldRequestNotifications(true, tiramisu, wasLoggedIn = false, isLoggedIn = true))
        assertFalse(shouldRequestNotifications(false, tiramisu, wasLoggedIn = false, isLoggedIn = true))
        assertFalse(shouldRequestNotifications(true, tiramisu - 1, wasLoggedIn = false, isLoggedIn = true))
        assertFalse(shouldRequestNotifications(true, tiramisu, wasLoggedIn = true, isLoggedIn = true))
        assertFalse(shouldRequestNotifications(true, tiramisu, wasLoggedIn = true, isLoggedIn = false))
    }
}
