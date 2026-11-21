package de.baupilot.app

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import de.baupilot.designsystem.components.TimerRing
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** D10 (A1): the ring is one image node; its "12:40" label is not a separate stop in the merged tree. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class TimerRingSemanticsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun oneImageNodeAndNoReachableChildText() {
        rule.setContent { TimerRing(0.58f, "12:40", "Prüfzeit zu 58 Prozent abgelaufen", Modifier) }
        val described = rule.onAllNodesWithContentDescription("Prüfzeit zu 58 Prozent abgelaufen")
        assertEquals(1, described.fetchSemanticsNodes().size)
        val config = rule.onNodeWithContentDescription("Prüfzeit zu 58 Prozent abgelaufen").fetchSemanticsNode().config
        assertEquals(Role.Image, config[SemanticsProperties.Role])
        assertEquals(0, rule.onAllNodesWithText("12:40").fetchSemanticsNodes().size)
        assertEquals(0, rule.onAllNodes(hasText("12:40")).fetchSemanticsNodes().size)
    }
}
