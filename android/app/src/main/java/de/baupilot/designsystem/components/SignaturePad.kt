package de.baupilot.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
import de.baupilot.inspection.CubicSegment
import de.baupilot.inspection.Extent
import de.baupilot.inspection.Point
import de.baupilot.inspection.SignatureSample
import de.baupilot.inspection.SignatureStrokes

private val STROKE_WIDTH = 2.dp

/**
 * Drawing surface of the owner's signature (D10): a drag collects a stroke, a tap a dot. The pad fills the size its
 * caller gives it; strokes are kept in dp and clamped to that size. Lines are 2 wide with round cap and join.
 * The semantic state is "strokes: n" so tests can tell an empty pad from a drawn one.
 */
@Composable
fun SignaturePad(
    strokes: SignatureStrokes,
    onStrokesChange: (SignatureStrokes) -> Unit,
    modifier: Modifier = Modifier,
    color: ColorToken = Tokens.text,
) {
    // Events arrive faster than recomposition, so the latest value lives here and not in the composition.
    val latest = remember { arrayOf(strokes) }
    SideEffect { latest[0] = strokes }
    val stroke = color.color()

    fun update(next: SignatureStrokes) {
        latest[0] = next
        onStrokesChange(next)
    }

    fun sized(size: IntSize, density: Density): SignatureStrokes =
        latest[0].copy(bounds = Extent(size.width / density.density, size.height / density.density))

    Canvas(
        modifier = modifier
            .semantics { stateDescription = "strokes: ${strokes.strokes.size}" }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    update(sized(size, this).begin(offset.toPoint(this)).end())
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> update(sized(size, this).begin(offset.toPoint(this))) },
                    onDrag = { change, _ ->
                        change.consume()
                        update(latest[0].extend(change.position.toPoint(this)))
                    },
                    onDragEnd = { update(latest[0].end()) },
                    onDragCancel = { update(latest[0].end()) },
                )
            },
    ) {
        val width = STROKE_WIDTH.toPx()
        val style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)
        strokes.strokes.forEach { points ->
            if (points.size == 1) {
                drawCircle(stroke, radius = width / 2, center = points[0].toOffset(density))
            } else {
                val path = Path()
                points.forEachIndexed { i, p ->
                    val o = p.toOffset(density)
                    if (i == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y)
                }
                drawPath(path, stroke, style = style)
            }
        }
    }
}

/**
 * The craftsman's sample signature (AndAbnSignatur.dc.html:60): the 120 × 48 box of [SignatureSample], stroke 2,
 * round cap and join, drawn from its nine cubic segments.
 */
@Composable
fun SignatureSampleCanvas(modifier: Modifier = Modifier, color: ColorToken = Tokens.text) {
    val ink = color.color()
    Canvas(modifier = modifier) {
        val path = Path()
        path.moveTo(SignatureSample.start.x * density, SignatureSample.start.y * density)
        SignatureSample.segments.forEach { segment -> path.cubicTo(segment, density) }
        drawPath(
            path,
            ink,
            style = Stroke(
                width = SignatureSample.strokeWidth * density,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

private fun Path.cubicTo(segment: CubicSegment, scale: Float) = cubicTo(
    segment.control1.x * scale, segment.control1.y * scale,
    segment.control2.x * scale, segment.control2.y * scale,
    segment.end.x * scale, segment.end.y * scale,
)

private fun Offset.toPoint(density: Density) = Point(x / density.density, y / density.density)

private fun Point.toOffset(density: Float) = Offset(x * density, y * density)
