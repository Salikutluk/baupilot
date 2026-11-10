package de.baupilot.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.components.InfoCallout
import de.baupilot.designsystem.components.PhaseCard
import de.baupilot.designsystem.components.PhaseCardState
import de.baupilot.designsystem.components.SlopeResult
import de.baupilot.designsystem.components.ValueStepper
import de.baupilot.snapshot.NoopSnapshotHandler
import de.baupilot.snapshot.baupilotPaparazzi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Geometry of the later-trade parts, measured from the layout (px = dp x 3). */
class ComponentMetricsLaterTradesTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(handler = NoopSnapshotHandler)

    private val heights = mutableMapOf<String, Int>()
    private val widths = mutableMapOf<String, Int>()

    private fun Modifier.record(key: String): Modifier = onGloballyPositioned {
        heights[key] = it.size.height
        widths[key] = it.size.width
    }

    /** AndSanUebersicht:30-39: the active and the planned card share one height, 2 x 175 wide. */
    @Test
    fun plannedPhaseCardHasTheHeightOfItsActiveNeighbour() {
        paparazzi.snapshot {
            Row(
                Modifier.padding(horizontal = 16.dp).height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PhaseCard(
                    "1 · Rohinstallation", "3 von 6 geprüft", PhaseCardState.Active(0.5f, Tokens.track.sanUebersicht),
                    Modifier.weight(1f).fillMaxHeight().record("active"),
                )
                PhaseCard(
                    "2 · Fertigmontage", "Geplant KW 14", PhaseCardState.Planned,
                    Modifier.weight(1f).fillMaxHeight().record("planned"),
                )
            }
        }
        assertEquals(heights.getValue("active"), heights.getValue("planned"))
        assertEquals(175 * 3, widths.getValue("planned"))
    }

    /** Planned is laid out like Done: 1 + 12 + label + 4 + value + 12 + 1. */
    @Test
    fun plannedPhaseCardHasTheHeightOfADoneCard() {
        paparazzi.snapshot {
            androidx.compose.foundation.layout.Column {
                PhaseCard("1 · Rohinstallation", "Abgenommen 29.01.", PhaseCardState.Done, Modifier.record("done"))
                PhaseCard("2 · Fertigmontage", "Geplant KW 14", PhaseCardState.Planned, Modifier.record("planned"))
            }
        }
        assertEquals(heights.getValue("done"), heights.getValue("planned"))
    }

    /** AndSanGefaelle:41-55: two 158 wide steppers (50 tall) side by side with a gap of 10 in a 326 wide card body. */
    @Test
    fun fillingValueSteppersSplitTheRowInto158AndGap10() {
        paparazzi.snapshot {
            Row(Modifier.width(326.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ValueStepper("Länge", "2,0 m", null, null, Modifier.weight(1f).record("left"), fillsWidth = true)
                ValueStepper(
                    "Höhenunterschied", "2,5 cm", null, null, Modifier.weight(1f).record("right"), fillsWidth = true,
                )
            }
        }
        assertEquals(158 * 3, widths.getValue("left"))
        assertEquals(158 * 3, widths.getValue("right"))
        assertEquals(50 * 3, heights.getValue("left"))
    }

    /** AndSanGefaelle:62: the result box is 14 + max(44, 30 + 2 + 15) + 14 tall; the schema is 92 x 44. */
    @Test
    fun slopeResultHasTheSchemaAndTheTextColumn() {
        paparazzi.snapshot {
            SlopeResult(
                percent = "1,25 %", verdict = "Ausreichend · Soll ≥ 1,0 %", sufficient = true, schemaY2 = 24.5,
                levelLabel = "waagerecht", deltaLabel = "Δh",
                modifier = Modifier.width(326.dp).record("result"),
            )
        }
        assertEquals(326 * 3, widths.getValue("result"))
        assertTrue(heights.getValue("result") >= (14 + 44 + 14) * 3)
    }

    /** AndSanGefaelle:78-81: the note has padding 4, a 20 icon and 13 / 18 text: two lines are 4 + 36 + 4 tall. */
    @Test
    fun plainInfoCalloutUsesItsPaddingAndLineHeight() {
        paparazzi.snapshot {
            Column {
                InfoCallout(
                    text = "Jetzt prüfen: Nach Estrich und Verkleidung lässt sich fehlendes Gefälle kaum noch korrigieren.",
                    modifier = Modifier.width(358.dp).record("note"),
                    container = null,
                    content = Tokens.muted,
                    contentPadding = PaddingValues(4.dp),
                    textSize = 13.sp,
                    lineHeight = 18.sp,
                )
                InfoCallout("Text", modifier = Modifier.record("default"))
            }
        }
        assertEquals((4 + 36 + 4) * 3, heights.getValue("note"))
        assertEquals((12 + 20 + 12) * 3, heights.getValue("default"))
    }
}
