package de.baupilot.app

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.lifecycle.Lifecycle
import de.baupilot.BaupilotApplication
import de.baupilot.MainActivity
import de.baupilot.capture.CaptureResult
import de.baupilot.capture.DefectDraft
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** D5: the data reloads on the next foreground, only when the stored version changed, and never blanks the UI. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class RootReloadUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val services get() = (RuntimeEnvironment.getApplication() as BaupilotApplication).services

    private fun exists(tag: String): Boolean = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun awaitScreen(tag: String) = rule.waitUntil(timeoutMillis = 5_000) { exists(tag) }

    private val defectRow = SemanticsMatcher("defect row") {
        it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("defect.") == true
    }

    private fun defectCount(): Int = rule.onAllNodes(defectRow).fetchSemanticsNodes().size

    private fun openDefects() {
        awaitScreen("login.submit")
        rule.onNodeWithTag("login.submit").performClick()
        awaitScreen("screen.home")
        rule.onNodeWithTag("tab.inspections").performClick()
        awaitScreen("inspections.view.trades")
        rule.onNodeWithTag("inspections.segment.defects").performClick()
        awaitScreen("inspections.view.defects")
    }

    private fun foreground() {
        val scenario = rule.activityRule.scenario
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    private fun write(): String {
        val id = "df_" + "1".repeat(32)
        val draft = DefectDraft(
            id = id, tradeId = "windows", stepKey = null, elementId = null, title = "Kratzer im Glas",
            description = "Ca. 3 cm lang.", severity = "Mittel", deadline = LocalDate.of(2027, 1, 15),
            notifyByEmail = false, responsible = "Fensterbau Weber GmbH",
        )
        assertEquals(CaptureResult.Ok(id), runBlocking { services.repository.saveDefect(draft) })
        return id
    }

    @Test
    fun dataReloadsOnForegroundOnlyWhenVersionChanged() {
        openDefects()
        val before = defectCount()
        assertTrue(before > 0)

        foreground()
        rule.waitForIdle()
        assertTrue(exists("screen.inspections"))
        assertEquals(before, defectCount())

        val id = write()
        assertEquals("a write never reloads by itself", before, defectCount())

        foreground()
        rule.waitUntil(timeoutMillis = 5_000) {
            assertTrue("screen.inspections must stay during a reload", exists("screen.inspections"))
            exists("defect.$id")
        }
        assertEquals(before + 1, defectCount())
    }
}
