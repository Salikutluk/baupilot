package de.baupilot.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTabRow
import de.baupilot.designsystem.components.BottomActionBar
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

    @Test
    fun tabRowIs48() {
        paparazzi.snapshot {
            Column {
                BaupilotTabRow(
                    tabs = listOf("Angebote (3)", "Termine", "Notizen"),
                    selectedIndex = 0,
                    contentDescription = "Bereiche",
                    modifier = Modifier.record("row"),
                    tabModifier = Modifier.record("tab"),
                )
            }
        }

        assertEquals(48 * 3, sizes.getValue("tab").second)
        assertEquals(49 * 3, sizes.getValue("row").second)
    }

    @Test
    fun tabIndicatorWidthFollowsTheParameter() {
        paparazzi.snapshot {
            Column {
                BaupilotTabRow(
                    tabs = listOf("Gewerke", "Offene Mängel (2)"),
                    selectedIndex = 0,
                    contentDescription = "Ansicht",
                    indicatorWidth = 64.dp,
                    indicatorModifier = Modifier.record("narrow"),
                )
                BaupilotTabRow(
                    tabs = listOf("Gewerke", "Offene Mängel (2)"),
                    selectedIndex = 1,
                    contentDescription = "Ansicht",
                    indicatorWidth = 96.dp,
                    indicatorModifier = Modifier.record("wide"),
                )
            }
        }

        assertEquals(64 * 3 to 3 * 3, sizes.getValue("narrow"))
        assertEquals(96 * 3 to 3 * 3, sizes.getValue("wide"))
    }

    @Test
    fun modalBarPutsTheTitleAfterNavButtonAndGap() {
        var titleX = 0f
        paparazzi.snapshot {
            Column {
                baupilotTopAppBarMeasured(
                    titleModifier = Modifier.onGloballyPositioned { titleX = it.positionInRoot().x },
                    title = "Mangel erfassen",
                    padding = BarPadding.Modal,
                    navigation = Nav.Close(),
                )
            }
        }

        // start 4 + 48 nav button + gap 4
        assertEquals(56 * 3f, titleX)
    }

    @Test
    fun bottomActionBarPadding() {
        paparazzi.snapshot {
            Column {
                BottomActionBar(modifier = Modifier.record("bar")) {
                    Box(Modifier.height(56.dp).record("content"))
                }
            }
        }

        assertEquals(sizes.getValue("content").second + (12 + 24) * 3, sizes.getValue("bar").second)
        assertEquals(56 * 3, sizes.getValue("content").second)
    }
}
