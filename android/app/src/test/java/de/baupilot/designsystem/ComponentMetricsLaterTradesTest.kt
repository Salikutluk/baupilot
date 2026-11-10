package de.baupilot.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import de.baupilot.designsystem.components.BaupilotVerticalStepper
import de.baupilot.designsystem.components.ObjectCheckRow
import de.baupilot.designsystem.components.ObjectChip
import de.baupilot.designsystem.components.ObjectChipState
import de.baupilot.designsystem.components.VerdictToggle
import de.baupilot.designsystem.components.InfoCallout
import de.baupilot.designsystem.components.PhaseCard
import de.baupilot.designsystem.components.PhaseCardState
import de.baupilot.designsystem.components.SlopeResult
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant
import de.baupilot.designsystem.components.TimerRing
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

    /** AndSanDruck:50: the ring is 72 x 72 (the 58 hole is inside it). */
    @Test
    fun timerRingIs72Square() {
        paparazzi.snapshot { TimerRing(0.58f, "12:40", "Prüfzeit zu 58 Prozent abgelaufen", Modifier.record("ring")) }
        assertEquals(72 * 3, widths.getValue("ring"))
        assertEquals(72 * 3, heights.getValue("ring"))
    }

    private fun currentStep(body: Modifier) = Step(
        number = 3,
        title = "Manometer",
        state = StepState.Current,
        body = { Box(body.width(100.dp).height(40.dp)) },
    )

    /** AndSanDruck:36: the rows of the current card are 14 apart; the default stays 12. */
    @Test
    fun compactCurrentCardUsesItsSpacing() {
        paparazzi.snapshot {
            Column {
                BaupilotVerticalStepper(
                    listOf(currentStep(Modifier)), StepperVariant.Compact, Modifier.record("spaced"), currentSpacing = 14.dp,
                )
                BaupilotVerticalStepper(listOf(currentStep(Modifier)), StepperVariant.Compact, Modifier.record("default"))
            }
        }
        // padding 16 + header 24 + gap + body 40 + padding 16
        assertEquals((16 + 24 + 14 + 40 + 16) * 3, heights.getValue("spaced"))
        assertEquals((16 + 24 + 12 + 40 + 16) * 3, heights.getValue("default"))
    }

    /** FlBad / MaRaum open rows are 40 tall; the default stays 44 (AndSanDruck:90). */
    @Test
    fun compactOpenRowsUseTheirMinimumHeight() {
        val open = List(2) { Step(number = 4 + it, title = "Offen", state = StepState.Open) }
        paparazzi.snapshot {
            Column {
                BaupilotVerticalStepper(open, StepperVariant.Compact, Modifier.record("forty"), openMinHeight = 40.dp)
                BaupilotVerticalStepper(open, StepperVariant.Compact, Modifier.record("default"))
            }
        }
        assertEquals(2 * 40 * 3, heights.getValue("forty"))
        assertEquals(2 * 44 * 3, heights.getValue("default"))
    }

    /** AndSanBad:46-62: the row is at least 64 tall (+1 divider), a divider adds 1. */
    @Test
    fun objectCheckRowHasItsMinimumHeightAndThePair() {
        paparazzi.snapshot {
            Column(Modifier.width(358.dp)) {
                ObjectCheckRow("Warm links, kalt rechts", "Hebel ganz nach links: Wasser wird warm.", ok = true, showsDivider = false, modifier = Modifier.record("first"))
                ObjectCheckRow("Wasser läuft zügig ab", "Becken voll laufen lassen, Stopfen ziehen.", ok = false, showsDivider = true, modifier = Modifier.record("second"))
            }
        }
        assertEquals(64 * 3, heights.getValue("first"))
        assertEquals((64 + 1) * 3, heights.getValue("second"))
    }

    /** The pair: 1 `outline` border round two 48 wide cells, 40 inner, so 98 x 42 outside. */
    @Test
    fun verdictToggleIs98By42() {
        paparazzi.snapshot { VerdictToggle(ok = true, modifier = Modifier.record("pair")) }
        assertEquals(98 * 3, widths.getValue("pair"))
        assertEquals(42 * 3, heights.getValue("pair"))
    }

    /** The chip keeps its 32 height whether it is selected (`Now`) or not. */
    @Test
    fun objectChipKeepsItsHeightWhenSelected() {
        paparazzi.snapshot {
            Column {
                ObjectChip("Waschtisch", ObjectChipState.Now, selected = true, modifier = Modifier.record("now"))
                ObjectChip("Dusche", ObjectChipState.Open, modifier = Modifier.record("open"))
            }
        }
        assertEquals(32 * 3, heights.getValue("now"))
        assertEquals(32 * 3, heights.getValue("open"))
    }
}
