package de.baupilot.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.ExtendedFab
import de.baupilot.designsystem.components.FabIcon
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.snapshot.NoopSnapshotHandler
import de.baupilot.snapshot.baupilotPaparazzi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Geometry of the button family and the extended FAB, measured from the layout (px = dp x 3). */
class ComponentMetricsButtonsFabTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(handler = NoopSnapshotHandler)

    private val sizes = mutableMapOf<String, Pair<Int, Int>>()

    private fun Modifier.record(key: String): Modifier =
        onGloballyPositioned { sizes[key] = it.size.width to it.size.height }

    @Test
    fun filledAndOutlinedButtonsAre48() {
        paparazzi.snapshot {
            Column {
                FilledButton(
                    label = "Speichern",
                    size = ButtonSize.Regular,
                    container = FilledContainer.Primary,
                    onClick = {},
                    modifier = Modifier.record("filledRegular"),
                )
                FilledButton(
                    label = "Melden",
                    size = ButtonSize.Compact,
                    container = FilledContainer.Error,
                    onClick = {},
                    modifier = Modifier.record("filledCompact"),
                )
                OutlinedButton(
                    label = "Abbrechen",
                    onClick = {},
                    modifier = Modifier.record("outlined"),
                )
            }
        }

        assertEquals(48 * 3, sizes.getValue("filledRegular").second)
        assertEquals(48 * 3, sizes.getValue("filledCompact").second)
        assertEquals(48 * 3, sizes.getValue("outlined").second)
    }

    @Test
    fun outlinedButtonWithBorderOutsideIs50() {
        paparazzi.snapshot {
            Column {
                OutlinedButton(
                    label = "Vergleichen",
                    borderOutside = true,
                    onClick = null,
                    modifier = Modifier.record("outside"),
                )
            }
        }

        assertEquals(50 * 3, sizes.getValue("outside").second)
    }

    @Test
    fun extendedFabIs56Tall() {
        paparazzi.snapshot {
            Column {
                ExtendedFab(
                    label = "Neu",
                    icon = FabIcon.Plus,
                    onClick = {},
                    modifier = Modifier.record("fab"),
                )
            }
        }

        assertEquals(56 * 3, sizes.getValue("fab").second)
    }
}
