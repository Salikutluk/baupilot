package de.baupilot.app

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import de.baupilot.snapshot.ScreenCatalog
import de.baupilot.snapshot.TestAppData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * One allowlisted audit result (plan D14): exact match on screen, rule and element (test tag, else label). [dark] limits
 * the entry to one mode; null covers both.
 */
data class AllowedFinding(val screen: String, val rule: String, val element: String, val reason: String, val dark: Boolean? = null)

/**
 * Allowlist of the accessibility audit (plan 5.8, D14). [entries] holds findings that cannot be fixed without breaking a
 * project rule; AC3 counts as met only while it is empty. [staged] suppresses a whole rule until the step that fixes it
 * (plan: Android touch-target checks are staged like the iOS audit types; Step 10 removes them).
 */
object AuditAllowlist {
    val entries: List<AllowedFinding> = emptyList()

    /** rule -> reason */
    val staged: Map<String, String> = emptyMap()

    fun element(finding: Finding): String = finding.testTag ?: finding.label ?: finding.boundsDp

    fun isAllowed(
        screen: String,
        dark: Boolean,
        finding: Finding,
        entries: List<AllowedFinding> = this.entries,
    ): Boolean =
        finding.rule in staged || entries.any {
            it.screen == screen && (it.dark == null || it.dark == dark) && it.rule == finding.rule && it.element == element(finding)
        }

    /**
     * G-11: the entries for ([screen], [dark]) that match none of [findings] (stale entries). Checked per screen test,
     * not in `@AfterClass`: that runs once per parameter and `forkEvery = 1` splits Light and Dark into separate JVMs.
     */
    fun unmatched(
        screen: String,
        dark: Boolean,
        findings: List<Finding>,
        entries: List<AllowedFinding> = this.entries,
    ): List<AllowedFinding> =
        entries.filter { it.screen == screen && (it.dark == null || it.dark == dark) }
            .filter { entry -> findings.none { it.rule == entry.rule && element(it) == entry.element } }
}

/**
 * Accessibility audit over all 34 screens (plan 5.8, fallback path: [SemanticsAudit], since ATF does not run under
 * Robolectric). The two mode classes differ only by the class-level `@Config` qualifier; each test asserts the mode it
 * really runs in.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
abstract class AccessibilityAuditBase(private val screen: String, private val dark: Boolean) {
    @get:Rule
    val rule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun screenHasNoFindings() {
        val state = ScreenCatalog.state(screen)
        val data = TestAppData.load()
        var uiNight = -1
        rule.setContent {
            uiNight = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK
            BaupilotAppContent(state, onStateChange = {}, data = data)
        }
        rule.waitForIdle()
        assertEquals(
            "$screen must be audited in ${if (dark) "dark" else "light"} mode",
            if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO,
            uiNight,
        )
        val mode = if (dark) "dark" else "light"
        val all = SemanticsAudit.check(rule.onRoot().fetchSemanticsNode())
        val findings = all.filterNot { AuditAllowlist.isAllowed(screen, dark, it) }
        assertTrue("$screen ($mode) findings:\n" + findings.joinToString("\n"), findings.isEmpty())
        val stale = AuditAllowlist.unmatched(screen, dark, all)
        assertTrue("$screen ($mode) stale allowlist entries:\n" + stale.joinToString("\n"), stale.isEmpty())
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenCatalog.all.map { (name, _) -> arrayOf<Any>(name) }
    }
}

@Config(qualifiers = "w411dp-h3000dp")
class AccessibilityAuditLightTest(screen: String) : AccessibilityAuditBase(screen, dark = false)

@Config(qualifiers = "w411dp-h3000dp-night")
class AccessibilityAuditDarkTest(screen: String) : AccessibilityAuditBase(screen, dark = true)
