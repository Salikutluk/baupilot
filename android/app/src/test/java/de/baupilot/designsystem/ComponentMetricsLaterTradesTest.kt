package de.baupilot.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.components.PhaseCard
import de.baupilot.designsystem.components.PhaseCardState
import de.baupilot.snapshot.NoopSnapshotHandler
import de.baupilot.snapshot.baupilotPaparazzi
import org.junit.Assert.assertEquals
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
}
