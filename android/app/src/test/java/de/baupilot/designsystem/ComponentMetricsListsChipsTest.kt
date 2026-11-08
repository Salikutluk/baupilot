package de.baupilot.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import de.baupilot.designsystem.components.AssistChip
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.FilterChip
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.ObjectChip
import de.baupilot.designsystem.components.ObjectChipState
import de.baupilot.designsystem.components.StatusChip
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.designsystem.components.StatusChipSize
import de.baupilot.snapshot.NoopSnapshotHandler
import de.baupilot.snapshot.baupilotPaparazzi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Geometry of chips and list rows, measured from the layout (px = dp x 3 at 480 dpi). */
class ComponentMetricsListsChipsTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(handler = NoopSnapshotHandler)

    private val heights = mutableMapOf<String, Int>()

    private fun Modifier.record(key: String): Modifier =
        onGloballyPositioned { heights[key] = it.size.height }

    @Test
    fun chipIs32() {
        paparazzi.snapshot {
            Column {
                AssistChip("Angemeldet bleiben", leadingCheck = true, modifier = Modifier.record("assist"))
                FilterChip("Offen", selected = true, showCheck = true, onClick = {}, modifier = Modifier.record("filterOn"))
                FilterChip("Alle", selected = false, onClick = {}, modifier = Modifier.record("filterOff"))
                ObjectChip("Bad", ObjectChipState.Done, modifier = Modifier.record("done"))
                ObjectChip("Küche", ObjectChipState.Now, modifier = Modifier.record("now"))
                ObjectChip("Flur", ObjectChipState.Open, modifier = Modifier.record("open"))
            }
        }

        listOf("assist", "filterOn", "filterOff", "done", "now", "open").forEach {
            assertEquals(it, 32 * 3, heights.getValue(it))
        }
    }

    @Test
    fun statusChipHeights() {
        paparazzi.snapshot {
            Column {
                StatusChip("Fertig", StatusChipColors.Ok(), StatusChipSize.H32, Modifier.record("h32"))
                StatusChip("Fertig", StatusChipColors.Ok(), StatusChipSize.H28, Modifier.record("h28"))
                StatusChip("Fertig", StatusChipColors.Ok(), StatusChipSize.H24, Modifier.record("h24"))
            }
        }

        assertEquals(32 * 3, heights.getValue("h32"))
        assertEquals(28 * 3, heights.getValue("h28"))
        assertEquals(24 * 3, heights.getValue("h24"))
    }

    @Test
    fun listItemMinHeights() {
        paparazzi.snapshot {
            Column {
                BaupilotListItem(title = "Einzeilig", minHeight = ListItemHeight.H56, modifier = Modifier.record("h56"))
                BaupilotListItem(title = "Dokument", minHeight = ListItemHeight.H72, modifier = Modifier.record("h72"))
            }
        }

        assertEquals(56 * 3, heights.getValue("h56"))
        assertEquals(72 * 3, heights.getValue("h72"))
    }
}
