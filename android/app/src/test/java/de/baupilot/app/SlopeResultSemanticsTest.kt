package de.baupilot.app

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import de.baupilot.designsystem.components.SlopeResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** D7: the slope card is one merged node whose label holds the percentage and the verdict, never the sketch labels. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class SlopeResultSemanticsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun mergedNodeCarriesPercentAndVerdictButNoSketchLabel() {
        rule.setContent {
            SlopeResult(
                percent = "0,75 %",
                verdict = "Zu flach · Soll ≥ 1,0 %",
                sufficient = false,
                schemaY2 = 30.0,
                levelLabel = "waagerecht",
                deltaLabel = "Δh",
                modifier = androidx.compose.ui.Modifier,
            )
        }
        val texts = rule.onNodeWithTag("slope.result").fetchSemanticsNode()
            .config[SemanticsProperties.Text].map { it.text }
        assertEquals(listOf("0,75 %", "Zu flach · Soll ≥ 1,0 %"), texts)
        assertFalse(texts.contains("waagerecht"))
        assertFalse(texts.contains("Δh"))
    }
}
