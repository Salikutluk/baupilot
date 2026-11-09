package de.baupilot.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.compare.columns
import de.baupilot.designsystem.components.AssistChip
import de.baupilot.designsystem.components.ComparisonColumn
import de.baupilot.designsystem.components.ComparisonRow
import de.baupilot.designsystem.components.ComparisonTable
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.FilterChip
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.ObjectChip
import de.baupilot.designsystem.components.NotificationCircle
import de.baupilot.designsystem.components.NotificationListItem
import de.baupilot.designsystem.components.ObjectChipState
import de.baupilot.designsystem.components.OfferItem
import de.baupilot.designsystem.components.FactRow
import de.baupilot.designsystem.components.TagChip
import de.baupilot.designsystem.components.TagChipStyle
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

    @Test
    fun tagChipIs32() {
        paparazzi.snapshot {
            Column {
                TagChip("Angebote einholen", TagChipStyle.Filled, Modifier.record("filled"))
                TagChip("Phase 8 · Estrich", TagChipStyle.Outlined, Modifier.record("outlined"))
            }
        }

        assertEquals(32 * 3, heights.getValue("filled"))
        assertEquals(32 * 3, heights.getValue("outlined"))
    }

    @Test
    fun factRowIs56() {
        paparazzi.snapshot {
            Column {
                FactRow(BaupilotIcon.CalendarW22, "Zeitfenster", "KW 8–9", Modifier.record("fact"))
            }
        }

        assertEquals(56 * 3, heights.getValue("fact"))
    }

    @Test
    fun offerItemPadding() {
        paparazzi.snapshot {
            Column {
                OfferItem(
                    firm = "Estrichbau Wagner",
                    meta = "PDF · 10.12.2026",
                    price = "9.800 €",
                    best = true,
                    bestLabel = "Günstigstes",
                    pending = false,
                    bestBackground = Tokens.okBg.gewerk,
                    modifier = Modifier.record("best"),
                )
                OfferItem(
                    firm = "Estrichbau Wagner",
                    meta = "PDF · 10.12.2026",
                    price = "9.800 €",
                    best = false,
                    bestLabel = "Günstigstes",
                    pending = false,
                    bestBackground = Tokens.okBg.gewerk,
                    modifier = Modifier.record("plain"),
                )
                Column(Modifier.record("texts"), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BaupilotText("Estrichbau Wagner", size = 16.sp, color = Tokens.text)
                    BaupilotText("PDF · 10.12.2026", size = 14.sp, color = Tokens.muted)
                }
            }
        }

        // padding 12 + 12, border 1 + 1: the border is part of the box in every state.
        val expected = heights.getValue("texts") + (24 + 2) * 3
        assertEquals(expected, heights.getValue("plain"))
        assertEquals(expected, heights.getValue("best"))
    }

    @Test
    fun notificationItemCircle40() {
        paparazzi.snapshot {
            Column {
                NotificationCircle(
                    BaupilotIcon.AlertTriangle, Tokens.errBg, Tokens.errFg, Modifier.record("circle"),
                )
                NotificationListItem(
                    icon = BaupilotIcon.AlertTriangle,
                    iconBackground = Tokens.errBg,
                    iconForeground = Tokens.errFg,
                    title = "Frist überschritten",
                    whenLabel = "08:10",
                    text = "Rohbau · Riss in Kellerwand",
                    unread = true,
                    onClick = null,
                    modifier = Modifier.record("item"),
                )
                Column(Modifier.record("texts"), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BaupilotText("Frist überschritten", size = 16.sp, weight = 700, color = Tokens.text)
                    BaupilotText(
                        "Rohbau · Riss in Kellerwand",
                        size = 14.sp, lineHeight = 20.sp, color = Tokens.muted,
                    )
                }
            }
        }

        assertEquals(40 * 3, heights.getValue("circle"))
        // Single-line texts: padding 12 + 12 around the larger of circle (40) and text column.
        val expected = maxOf(40 * 3, heights.getValue("texts")) + 24 * 3
        assertEquals(expected, heights.getValue("item"))
    }

    @Test
    fun comparisonRowMinHeight44() {
        val cols = listOf(ComparisonColumn("Estrichbau Wagner", "Günstigstes"), ComparisonColumn("Fließestrich Hahn"))
        val widths = columns(cardWidth = 358f, padding = 16f, gap = 10f, labelColumn = 104f)
        val row = ComparisonRow("Dicke", "65 mm", "50 mm", strong = false, highlighted = true)
        paparazzi.snapshot {
            Column {
                ComparisonTable(cols, listOf(row), widths, contentDescription = "eine", modifier = Modifier.record("one"))
                ComparisonTable(cols, listOf(row, row), widths, contentDescription = "zwei", modifier = Modifier.record("two"))
            }
        }

        // One extra row adds the 44 minimum plus its 1 top border.
        assertEquals((44 + 1) * 3, heights.getValue("two") - heights.getValue("one"))
    }
}
