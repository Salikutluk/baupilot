package de.baupilot.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.ColorToken
import de.baupilot.inspection.SegmentLayout

/**
 * Row of equal progress segments (AndAbnElemente:23-25): one [segments] colour each, [height] high, [gap] apart,
 * every segment rounded with [radius]. The edges come from [SegmentLayout] (cumulative positions snapped to
 * device pixels), so a fractional segment width does not drift. [contentDescription] is the accessibility label.
 */
@Composable
fun SegmentedProgress(
    segments: List<ColorToken>,
    height: Dp,
    contentDescription: String,
    modifier: Modifier = Modifier,
    gap: Dp = 3.dp,
    radius: Dp = 3.dp,
) {
    val colors = segments.map { it.color() }
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val edges = SegmentLayout.edges(segments.size, size.width / density, gap.value, density)
        val corner = CornerRadius(radius.toPx())
        edges.forEachIndexed { i, edge ->
            drawRoundRect(
                color = colors[i],
                topLeft = Offset(edge.start * density, 0f),
                size = Size((edge.end - edge.start) * density, size.height),
                cornerRadius = corner,
            )
        }
    }
}
