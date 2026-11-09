package de.baupilot.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
import de.baupilot.inspection.SketchCircle
import de.baupilot.inspection.SketchRect
import de.baupilot.inspection.WindowLevelSketch as Sketch

/**
 * The "So prüfen" sketch (AndAbnPruefung.dc.html:57): a window frame with a spirit level on top and one at the
 * side, 56 x 44 dp, drawn from [Sketch.Geometry] in viewBox units (1 unit = 1 dp). SVG defaults: butt caps.
 * [stroke] colours the frame and the mullion, [level] the two spirit levels, [bubble] the two bubbles.
 */
@Composable
fun WindowLevelSketch(
    modifier: Modifier = Modifier,
    stroke: ColorToken = Tokens.muted,
    level: ColorToken = Tokens.primary,
    bubble: ColorToken = Tokens.surfaceContainerHigh,
) {
    val strokeColor = stroke.color()
    val levelColor = level.color()
    val bubbleColor = bubble.color()
    val box = Sketch.Geometry.box
    Canvas(modifier.size(box.width.dp, box.height.dp)) {
        val g = Sketch.Geometry
        strokeRoundRect(g.frame, g.frameStrokeWidth, strokeColor)
        drawLine(
            color = strokeColor,
            start = Offset(g.mullion.from.x * density, g.mullion.from.y * density),
            end = Offset(g.mullion.to.x * density, g.mullion.to.y * density),
            strokeWidth = g.mullionStrokeWidth * density,
        )
        fillRoundRect(g.topLevel, levelColor)
        fillCircle(g.topBubble, bubbleColor)
        fillRoundRect(g.sideLevel, levelColor)
        fillCircle(g.sideBubble, bubbleColor)
    }
}

private fun DrawScope.strokeRoundRect(rect: SketchRect, width: Float, color: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(rect.x * density, rect.y * density),
        size = Size(rect.width * density, rect.height * density),
        cornerRadius = CornerRadius(rect.rx * density),
        style = Stroke(width = width * density),
    )
}

private fun DrawScope.fillRoundRect(rect: SketchRect, color: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(rect.x * density, rect.y * density),
        size = Size(rect.width * density, rect.height * density),
        cornerRadius = CornerRadius(rect.rx * density),
    )
}

private fun DrawScope.fillCircle(circle: SketchCircle, color: Color) {
    drawCircle(color = color, radius = circle.radius * density, center = Offset(circle.center.x * density, circle.center.y * density))
}
