package de.baupilot.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.Tab
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.baupilotNavigationBarMeasured
import de.baupilot.designsystem.components.baupilotTopAppBarMeasured
import de.baupilot.snapshot.NoopSnapshotHandler
import de.baupilot.snapshot.baupilotPaparazzi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Geometry of the top and navigation bars, measured from the layout (px = dp x 3 at 480 dpi). */
class ComponentMetricsBarsTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(handler = NoopSnapshotHandler)

    private val sizes = mutableMapOf<String, Pair<Int, Int>>()

    private fun Modifier.record(key: String): Modifier =
        onGloballyPositioned { sizes[key] = it.size.width to it.size.height }

    @Test
    fun topAppBarIs32Plus64() {
        paparazzi.snapshot {
            Column {
                Box(Modifier.record("bar")) {
                    baupilotTopAppBarMeasured(
                        headerModifier = Modifier.record("header"),
                        title = "Übersicht",
                        navigation = Nav.Back(),
                        titleStyle = TitleStyle.Large,
                        padding = BarPadding.Nav,
                    )
                }
            }
        }

        assertEquals(96 * 3, sizes.getValue("bar").second)
        assertEquals(64 * 3, sizes.getValue("header").second)
    }

    @Test
    fun navigationBarIs80AndPillIs56x32() {
        paparazzi.snapshot {
            Column {
                baupilotNavigationBarMeasured(
                    selected = Tab.Overview,
                    onSelect = {},
                    modifier = Modifier.record("bar"),
                    activePillModifier = Modifier.record("pill"),
                )
            }
        }

        assertEquals(80 * 3, sizes.getValue("bar").second)
        assertEquals(56 * 3 to 32 * 3, sizes.getValue("pill"))
    }
}
