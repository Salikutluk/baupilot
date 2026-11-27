package de.baupilot.app

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.core.app.ActivityScenario
import de.baupilot.BaupilotApplication
import de.baupilot.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** I14: the login state at start mirrors the cached session. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class SessionStartUiTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun exists(tag: String): Boolean = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun cachedSessionStartsLoggedIn() {
        val services = (RuntimeEnvironment.getApplication() as BaupilotApplication).services
        runBlocking { services.auth.logIn("", "") }
        ActivityScenario.launch(MainActivity::class.java).use {
            rule.waitUntil(timeoutMillis = 5_000) { exists("screen.home") }
            assertFalse(exists("login.submit"))
        }
    }
}
