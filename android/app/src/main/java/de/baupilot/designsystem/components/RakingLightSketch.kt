package de.baupilot.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.Tokens
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * The raking-light sketch of the wall check (AndMaRaum:34-38): 88 tall, r12, a 100 degree gradient from `lit` to `wall`
 * at 70 percent, a lamp (34 x 18, r4, `text`) at 14 / 34 with its 8 x 8 `lampGlow` bulb at 48 / 39, and a 2 x 44
 * `shadow` line at 62 percent of the width, 22 from the top, at 0.6 opacity. It fills the width of its parent (326).
 */
@Composable
fun RakingLightSketch(modifier: Modifier = Modifier) {
    val lit = Tokens.lit.color()
    val wall = Tokens.wall.color()
    val lamp = Tokens.text.color()
    val glow = Tokens.lampGlow.color()
    val shadow = Tokens.shadow.color()
    Canvas(modifier.fillMaxWidth().height(88.dp).clip(RoundedCornerShape(12.dp))) {
        // CSS linear-gradient(100deg): the gradient line has the length |w sin a| + |h cos a| and runs through the centre.
        val angle = Math.toRadians(100.0)
        val dx = sin(angle).toFloat()
        val dy = -cos(angle).toFloat()
        val half = (abs(size.width * sin(angle)) + abs(size.height * cos(angle))).toFloat() / 2f
        val centre = Offset(size.width / 2f, size.height / 2f)
        drawRect(
            Brush.linearGradient(
                0f to lit,
                0.7f to wall,
                1f to wall,
                start = Offset(centre.x - dx * half, centre.y - dy * half),
                end = Offset(centre.x + dx * half, centre.y + dy * half),
            ),
        )
        drawRoundRect(lamp, Offset(14.dp.toPx(), 34.dp.toPx()), Size(34.dp.toPx(), 18.dp.toPx()), CornerRadius(4.dp.toPx()))
        drawRoundRect(glow, Offset(48.dp.toPx(), 39.dp.toPx()), Size(8.dp.toPx(), 8.dp.toPx()), CornerRadius(4.dp.toPx()))
        drawRect(shadow, Offset(size.width * 0.62f, 22.dp.toPx()), Size(2.dp.toPx(), 44.dp.toPx()), alpha = 0.6f)
    }
}
