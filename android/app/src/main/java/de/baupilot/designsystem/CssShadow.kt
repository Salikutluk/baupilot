package de.baupilot.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * CSS `box-shadow: 0 <offsetY> <blur> <color>` behind the content, drawn with
 * `android.graphics.Paint.setShadowLayer`.
 *
 * CSS blur B is a Gaussian with σ = B / 2; Skia's blur radius is `(σ − 0.5) / 0.57735` for
 * σ > 0.5 (otherwise σ itself is passed through unchanged). The mapping is reasoned, not measured.
 *
 * The [color] token is resolved through [composed], i.e. inside a composable scope, so
 * [ColorToken.color] reads the current light/dark theme.
 *
 * Known gap: on API 26–27 hardware layers do not draw shadow layers on shapes, so the FAB shows
 * no shadow there.
 */
fun Modifier.cssShadow(offsetY: Dp, blur: Dp, color: ColorToken, shape: Shape): Modifier = composed {
    val resolved = color.color().toArgb()
    val density = LocalDensity.current
    drawBehind {
        val sigma = blur.toPx() / 2f
        val radius = if (sigma > 0.5f) (sigma - 0.5f) / 0.57735f else sigma
        val path = when (val outline = shape.createOutline(size, layoutDirection, density)) {
            is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
            is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
            is Outline.Generic -> outline.path
        }
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                this.color = android.graphics.Color.TRANSPARENT
                setShadowLayer(radius, 0f, offsetY.toPx(), resolved)
            }
            canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
        }
    }
}
