package de.baupilot.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import de.baupilot.designsystem.Tokens
import de.baupilot.inspection.CssGradient
import de.baupilot.inspection.Extent
import de.baupilot.inspection.PhotoScene
import de.baupilot.inspection.SliceTransform

/**
 * The photo tile's placeholder picture (AndAbnPruefung.dc.html:58): the CSS gradient `linear-gradient(160deg, …)`
 * of the tile box, then the window scene in viewBox units with `preserveAspectRatio="xMidYMid slice"`
 * (frame, mullion, spirit level, in this order). Fills the tile; the caller clips it to the tile's shape.
 */
@Composable
fun PhotoPlaceholder(modifier: Modifier = Modifier) {
    val top = Tokens.photoSkyTop.color()
    val mid = Tokens.photoSkyMid.color()
    val bottom = Tokens.photoSkyBottom.color()
    val frame = Tokens.photoFrame.color()
    val level = Tokens.photoLevel.color()
    Canvas(modifier.fillMaxSize()) {
        val d = density
        val width = size.width / d
        val height = size.height / d
        val line = CssGradient.endpoints(PhotoScene.gradientAngleDegrees, width, height)
        val stops = PhotoScene.gradientStops
        drawRect(
            brush = Brush.linearGradient(
                stops[0] to top,
                stops[1] to mid,
                stops[2] to bottom,
                start = Offset(line.start.x * d, line.start.y * d),
                end = Offset(line.end.x * d, line.end.y * d),
            ),
        )
        val fit = SliceTransform.fit(PhotoScene.viewBox, Extent(width, height))
        withTransform({
            translate(fit.offsetX * d, fit.offsetY * d)
            scale(fit.scale * d, fit.scale * d, pivot = Offset.Zero)
        }) {
            val f = PhotoScene.frame
            drawRect(
                color = frame,
                topLeft = Offset(f.x, f.y),
                size = Size(f.width, f.height),
                style = Stroke(width = PhotoScene.frameStrokeWidth),
            )
            val m = PhotoScene.mullion
            drawLine(
                color = frame,
                start = Offset(m.from.x, m.from.y),
                end = Offset(m.to.x, m.to.y),
                strokeWidth = PhotoScene.mullionStrokeWidth,
            )
            val l = PhotoScene.level
            drawRoundRect(
                color = level,
                topLeft = Offset(l.x, l.y),
                size = Size(l.width, l.height),
                cornerRadius = CornerRadius(l.rx),
            )
        }
    }
}
