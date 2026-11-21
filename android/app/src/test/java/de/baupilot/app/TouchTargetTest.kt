package de.baupilot.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import de.baupilot.snapshot.ScreenCatalog
import de.baupilot.snapshot.TestAppData
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Touch-target walk over all 34 screens (plan 5.9, D19): every node with a click action, in the unmerged tree, has touch
 * bounds of at least 48 x 48 dp. The window is 3000 dp tall so the scroll viewport does not clip `touchBoundsInRoot` (R9).
 * Light only: the size does not depend on the mode.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h3000dp")
class TouchTargetTest(private val screen: String) {
    @get:Rule
    val rule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun everyClickableReaches48dp() {
        val state = ScreenCatalog.state(screen)
        val data = TestAppData.load()
        rule.setContent { BaupilotAppContent(state, onStateChange = {}, data = data) }
        rule.waitForIdle()
        val density = rule.density.density
        val minPx = 48f * density - 0.5f
        val misses = rule.onAllNodes(hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes().mapNotNull { node ->
            val b = node.touchBoundsInRoot
            if (b.width >= minPx && b.height >= minPx) {
                null
            } else {
                val tag = node.config.getOrNull(SemanticsProperties.TestTag)
                val label = node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString(" ")
                    ?: node.config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }
                "${tag ?: label ?: "<unnamed>"}: ${b.width / density} x ${b.height / density} dp"
            }
        }
        assertTrue("$screen touch targets below 48 dp:\n" + misses.joinToString("\n"), misses.isEmpty())
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenCatalog.all.map { (name, _) -> arrayOf<Any>(name) }
    }
}
