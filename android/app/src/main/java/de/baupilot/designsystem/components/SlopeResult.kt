package de.baupilot.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.RobotoMetrics
import de.baupilot.designsystem.Tokens

/**
 * Live result of the drain slope calculator (AndSanGefaelle:62): radius 12, padding 14, gap 14, `okBg` / `okFg` when
 * [sufficient], else `errBg` / `errFg`. Left a 92 x 44 schema in viewBox units (1 unit = 1 dp): a dashed level line
 * with the [levelLabel] (8 / 700; Chrome draws the prototype's 600 as 700), the tilted pipe ending at [schemaY2] and
 * the drop mark with [deltaLabel] (9 / 700). Right the [percent] (26 / 500) over the one-line [verdict] (13 / 500).
 */
@Composable
fun SlopeResult(
    percent: String,
    verdict: String,
    sufficient: Boolean,
    schemaY2: Double,
    levelLabel: String,
    deltaLabel: String,
    modifier: Modifier = Modifier,
    percentTag: String? = null,
    verdictTag: String? = null,
) {
    val background: ColorToken = if (sufficient) Tokens.okBg else Tokens.errBg
    val foreground: ColorToken = if (sufficient) Tokens.okFg else Tokens.errFg
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background.color())
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SlopeSchema(foreground, schemaY2.toFloat(), levelLabel, deltaLabel)
        // The prototype's gap is 2. Measured on the reference: the verdict sits 1 lower and the box is 1 taller than
        // with 2, most likely the fallback font Chrome uses for "≥" growing that line box above the baseline.
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            BaupilotText(
                text = percent,
                size = 26.sp,
                weight = 500,
                color = foreground,
                modifier = if (percentTag != null) Modifier.testTag(percentTag) else Modifier,
            )
            BaupilotText(
                text = verdict,
                size = 13.sp,
                weight = 500,
                color = foreground,
                maxLines = 1,
                modifier = if (verdictTag != null) Modifier.testTag(verdictTag) else Modifier,
            )
        }
    }
}

/** The 92 x 44 SVG: strokes on a canvas, the two labels as text placed on the SVG baselines (y = 8 and y = 42). */
@Composable
private fun SlopeSchema(color: ColorToken, y2: Float, levelLabel: String, deltaLabel: String) {
    val tint = color.color()
    Box(Modifier.size(92.dp, 44.dp)) {
        Canvas(Modifier.size(92.dp, 44.dp)) {
            val d = density
            drawLine(
                color = tint.copy(alpha = 0.6f),
                start = Offset(2 * d, 12 * d),
                end = Offset(90 * d, 12 * d),
                strokeWidth = d,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3 * d, 3 * d)),
            )
            drawLine(
                color = tint,
                start = Offset(4 * d, 12 * d),
                end = Offset(78 * d, y2 * d),
                strokeWidth = 7 * d,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = tint,
                start = Offset(86 * d, 12 * d),
                end = Offset(86 * d, y2 * d),
                strokeWidth = 1.5f * d,
            )
        }
        SvgText(levelLabel, x = 2.dp, baseline = 8f, size = 8f, alpha = 0.8f, color = color)
        SvgText(deltaLabel, x = 76.dp, baseline = 42f, size = 9f, alpha = 1f, color = color)
    }
}

/** Bold text whose baseline sits at [baseline] dp from the top of the SVG (`<text x y>`). */
@Composable
private fun SvgText(text: String, x: Dp, baseline: Float, size: Float, alpha: Float, color: ColorToken) {
    BaupilotText(
        text = text,
        size = size.sp,
        weight = 700,
        color = color.color().copy(alpha = alpha),
        maxLines = 1,
        modifier = Modifier.offset(x = x, y = (baseline - RobotoMetrics.baselineOffset(size)).dp),
    )
}
