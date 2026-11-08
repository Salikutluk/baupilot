package de.baupilot.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import de.baupilot.designsystem.components.ActionRow
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.ExtendedFab
import de.baupilot.designsystem.components.FabIcon
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.TextButton
import de.baupilot.designsystem.components.TextButtonSize
import de.baupilot.designsystem.components.TonalButton
import de.baupilot.designsystem.components.TonalIconButton
import de.baupilot.snapshot.NoopSnapshotHandler
import de.baupilot.snapshot.SNAPSHOT_WIDTH
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

    @Test
    fun tonalButtonIs40() {
        paparazzi.snapshot {
            Column {
                TonalButton(label = "Bearbeiten", onClick = {}, modifier = Modifier.record("tonal"))
            }
        }

        assertEquals(40 * 3, sizes.getValue("tonal").second)
    }

    @Test
    fun wideTextButtonIs44() {
        paparazzi.snapshot {
            Column {
                TextButton(
                    label = "Neues Bauvorhaben anlegen",
                    size = TextButtonSize.Wide44,
                    modifier = Modifier.record("wide"),
                )
                TextButton(
                    label = "Alle",
                    size = TextButtonSize.Compact40,
                    modifier = Modifier.record("compact"),
                )
            }
        }

        assertEquals(44 * 3, sizes.getValue("wide").second)
        assertEquals(SNAPSHOT_WIDTH, sizes.getValue("wide").first)
        assertEquals(40 * 3, sizes.getValue("compact").second)
    }

    @Test
    fun tonalIconButtonIs56() {
        paparazzi.snapshot {
            Column {
                TonalIconButton(
                    icon = BaupilotIcon.Paperclip,
                    contentDescription = "Dokument hinzufügen",
                    modifier = Modifier.record("tonal"),
                )
            }
        }

        assertEquals(56 * 3 to 56 * 3, sizes.getValue("tonal"))
    }

    @Test
    fun actionRowMinHeight56() {
        paparazzi.snapshot {
            Column {
                ActionRow("Bodenbau Lorenz", "Angefragt am 05.12. · noch offen", "Erinnern", modifier = Modifier.record("action"))
            }
        }

        assertEquals(56 * 3, sizes.getValue("action").second)
    }
}
