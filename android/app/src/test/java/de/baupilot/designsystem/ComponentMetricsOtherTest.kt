package de.baupilot.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import de.baupilot.designsystem.components.BaupilotSearchBar
import de.baupilot.designsystem.components.BaupilotLinearProgress
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant
import de.baupilot.designsystem.components.baupilotSwitchMeasured
import de.baupilot.designsystem.components.baupilotVerticalStepperMeasured
import de.baupilot.snapshot.NoopSnapshotHandler
import de.baupilot.snapshot.baupilotPaparazzi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Geometry of search bar, switch, stepper and progress bar, measured from the layout (px = dp x 3). */
class ComponentMetricsOtherTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(handler = NoopSnapshotHandler)

    private val sizes = mutableMapOf<String, Pair<Int, Int>>()
    private val offsets = mutableMapOf<String, Int>()

    private fun Modifier.record(key: String): Modifier = onGloballyPositioned {
        sizes[key] = it.size.width to it.size.height
        offsets[key] = it.positionInRoot().x.toInt()
    }

    @Test
    fun searchBarIs56() {
        paparazzi.snapshot {
            Column {
                BaupilotSearchBar(
                    query = "",
                    placeholder = "Suchen",
                    trailingText = "26",
                    onQueryChange = {},
                    modifier = Modifier.record("search"),
                )
            }
        }

        assertEquals(56 * 3, sizes.getValue("search").second)
    }

    @Test
    fun switchIs52x32AndThumbPositions() {
        paparazzi.snapshot {
            Column {
                baupilotSwitchMeasured(
                    checked = true,
                    contentDescription = "an",
                    onCheckedChange = {},
                    modifier = Modifier.record("on"),
                    thumbModifier = Modifier.record("onThumb"),
                )
                baupilotSwitchMeasured(
                    checked = false,
                    contentDescription = "aus",
                    onCheckedChange = {},
                    modifier = Modifier.record("off"),
                    thumbModifier = Modifier.record("offThumb"),
                )
            }
        }

        assertEquals(52 * 3 to 32 * 3, sizes.getValue("on"))
        assertEquals(52 * 3 to 32 * 3, sizes.getValue("off"))
        assertEquals(24 * 3 to 24 * 3, sizes.getValue("onThumb"))
        assertEquals(16 * 3 to 16 * 3, sizes.getValue("offThumb"))
        assertEquals(22 * 3, offsets.getValue("onThumb") - offsets.getValue("on"))
        assertEquals(6 * 3, offsets.getValue("offThumb") - offsets.getValue("off"))
    }

    @Test
    fun stepperCircleIs24() {
        paparazzi.snapshot {
            Column {
                baupilotVerticalStepperMeasured(
                    steps = listOf(
                        Step(number = 1, title = "Fenster", state = StepState.Done, meta = "ok"),
                        Step(number = 2, title = "Tür", state = StepState.Open),
                    ),
                    variant = StepperVariant.Rail,
                    circleModifier = Modifier.record("circle"),
                )
            }
        }

        assertEquals(24 * 3 to 24 * 3, sizes.getValue("circle"))
    }

    @Test
    fun linearProgressIs4() {
        paparazzi.snapshot {
            Column {
                BaupilotLinearProgress(progress = 0.42f, modifier = Modifier.record("bar"))
            }
        }

        assertEquals(4 * 3, sizes.getValue("bar").second)
    }
}
