package de.baupilot.app

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import de.baupilot.designsystem.components.ScheduleChart
import de.baupilot.schedule.MonthLabel
import de.baupilot.schedule.ScheduleLayout
import de.baupilot.schedule.ScheduleRowLayout
import de.baupilot.schedule.ScheduleState
import de.baupilot.snapshot.TestAppData
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** D9: the Zeitplan chart stays one image node with the fixture's aria label, read from the fixture (pin, no change to the chart). */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class ScheduleChartSemanticsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun chartIsOneImageNodeWithTheAriaLabel() {
        val rows = List(3) { ScheduleRowLayout("t$it", "Gewerk $it", ScheduleState.Planned, 126f, 20f, null) }
        val layout = ScheduleLayout(rows, 200f, 186f, listOf(MonthLabel(126f, "Mär 26")))
        val ariaLabel = TestAppData.load().schedule.ariaLabel
        rule.setContent { ScheduleChart(layout, "Heute", ariaLabel, Modifier) }
        val images = rule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
        val nodes = images.fetchSemanticsNodes()
        assertEquals(1, nodes.size)
        assertEquals(listOf(ariaLabel), nodes[0].config[SemanticsProperties.ContentDescription])
    }
}
