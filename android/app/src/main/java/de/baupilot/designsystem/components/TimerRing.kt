package de.baupilot.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens

/**
 * Timer ring of the Druckprobe (AndSanDruck:50-52): a 72 disc whose [fraction] is `primary` from the top clockwise
 * (the prototype's conic gradient) on the core `track`, with a 58 `surfaceContainer` hole that holds the [label]
 * (14 / 500). [accessibilityLabel] is the aria label of the image.
 */
@Composable
fun TimerRing(
    fraction: Float,
    label: String,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
) {
    val track = Tokens.track.color()
    val primary = Tokens.primary.color()
    val hole = Tokens.surfaceContainer.color()
    Box(
        modifier = modifier
            .size(72.dp)
            .semantics {
                role = Role.Image
                contentDescription = accessibilityLabel
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(72.dp)) {
            drawCircle(track)
            drawArc(
                color = primary,
                startAngle = -90f,
                sweepAngle = 360f * fraction.coerceIn(0f, 1f),
                useCenter = true,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
            )
            drawCircle(hole, radius = 29.dp.toPx())
        }
        BaupilotText(text = label, size = 14.sp, weight = 500, color = Tokens.text)
    }
}
