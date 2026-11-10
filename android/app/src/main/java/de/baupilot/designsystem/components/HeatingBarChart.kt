package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.baupilot.app.HeatingBar
import de.baupilot.app.HeatingBarKind
import de.baupilot.designsystem.ColorToken

/**
 * Heating bar chart (AndEsBelegreife:30-34): 80 tall, the bars share the width equally with a gap of 3 and sit on the
 * bottom edge. Each bar has radius 3 on its top corners, its [HeatingBar.height] (dp) and the colour of its kind:
 * [recorded], [today] or [planned]. [topPadding] is extra room above the tallest bar (0 on Android). The chart is one
 * image for accessibility, described by [accessibilityLabel].
 */
@Composable
fun HeatingBarChart(
    bars: List<HeatingBar>,
    accessibilityLabel: String,
    recorded: ColorToken,
    today: ColorToken,
    planned: ColorToken,
    modifier: Modifier = Modifier,
    topPadding: Dp = 0.dp,
) {
    val shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(top = topPadding)
            .clearAndSetSemantics {
                role = Role.Image
                contentDescription = accessibilityLabel
            },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEach { bar ->
            val color = when (bar.kind) {
                HeatingBarKind.Recorded -> recorded
                HeatingBarKind.Today -> today
                HeatingBarKind.Planned -> planned
            }
            Box(Modifier.weight(1f).height(bar.height.dp).background(color.color(), shape))
        }
    }
}
