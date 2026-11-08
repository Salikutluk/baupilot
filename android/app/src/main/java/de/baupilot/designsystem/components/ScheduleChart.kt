package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
import de.baupilot.schedule.ScheduleLayout
import de.baupilot.schedule.ScheduleRowLayout
import de.baupilot.schedule.ScheduleState

/** Marker shape of a legend entry: a 10 square with radius 3, or the 8 diamond of the ask marker. */
enum class LegendShape { Swatch, Diamond }

data class LegendItem(val label: String, val color: ColorToken, val shape: LegendShape = LegendShape.Swatch)

/** Legend row (AndZeitplan:38): gap 14, 12 `muted` labels, 5 between marker and label. */
@Composable
fun ScheduleLegend(items: List<LegendItem>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                when (item.shape) {
                    LegendShape.Swatch -> Box(
                        Modifier.size(10.dp).background(item.color.color(), RoundedCornerShape(3.dp)),
                    )
                    LegendShape.Diamond -> Diamond(8.dp, item.color)
                }
                BaupilotText(text = item.label, size = 12.sp, color = Tokens.muted)
            }
        }
    }
}

private val HEADER_HEIGHT = 20.dp
private val ROW_CONTENT = 33.dp
private val BAR_TOP = 11.dp
private val BAR_HEIGHT = 12.dp
private val DIAMOND_SIDE = 8.dp
private val DIAMOND_TOP = 13.dp

/**
 * Zeitplan chart (AndZeitplan:46): month header (20), one row per trade (pitch 34: 1 divider + 33), the 2 wide
 * today line over the rows and the [todayLabel] below. Boxes are drawn from the snapped `draw*` values of
 * [layout], text from the unsnapped ones (D22); no position is computed here (I9). The whole chart is one
 * semantics node described by [contentDescription].
 */
@Composable
fun ScheduleChart(
    layout: ScheduleLayout,
    todayLabel: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val rowsHeight = ScheduleLayout.ROW_PITCH.dp * layout.rows.size
    Column(
        modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                this.contentDescription = contentDescription
                role = Role.Image
            },
    ) {
        Box(Modifier.fillMaxWidth().height(HEADER_HEIGHT)) {
            layout.monthLabels.forEach { month ->
                BaupilotText(
                    text = month.label,
                    size = 11.sp,
                    weight = 500,
                    color = Tokens.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier.offset(x = month.x.dp),
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(rowsHeight)) {
            layout.rows.forEachIndexed { index, row -> ChartRow(row, index) }
            Box(
                Modifier
                    .offset(x = layout.todayDrawX.dp)
                    .width(2.dp)
                    .height(rowsHeight)
                    .background(Tokens.text.color()),
            )
        }
        Box(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            BaupilotText(
                text = todayLabel,
                size = 11.sp,
                weight = 700,
                color = Tokens.text,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier.offset(x = layout.todayLabelX.dp),
            )
        }
    }
}

@Composable
private fun ChartRow(row: ScheduleRowLayout, index: Int) {
    val top = ScheduleLayout.ROW_PITCH.dp * index
    Box(Modifier.fillMaxWidth().offset(y = top).height(ScheduleLayout.ROW_PITCH.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Tokens.divider.color()))
        Box(
            Modifier.padding(top = 1.dp).width(ScheduleLayout.NAME_COLUMN.dp).height(ROW_CONTENT),
            contentAlignment = Alignment.CenterStart,
        ) {
            BaupilotText(
                text = row.name,
                size = 13.sp,
                weight = if (row.state == ScheduleState.Running) 700 else 400,
                color = Tokens.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val barColor = when (row.state) {
            ScheduleState.Done -> Tokens.done
            ScheduleState.Running -> Tokens.primary
            ScheduleState.Planned -> Tokens.plan
        }
        Box(
            Modifier
                .offset(x = row.drawX.dp, y = 1.dp + BAR_TOP)
                .size(row.drawWidth.dp, BAR_HEIGHT)
                .background(barColor.color(), RoundedCornerShape(6.dp)),
        )
        row.diamondDrawCenterX?.let { centre ->
            Diamond(
                size = DIAMOND_SIDE,
                color = Tokens.primary,
                halo = Tokens.bg,
                modifier = Modifier.offset(x = centre.dp - DIAMOND_SIDE / 2, y = 1.dp + DIAMOND_TOP),
            )
        }
    }
}
