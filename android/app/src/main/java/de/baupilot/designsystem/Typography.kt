package de.baupilot.designsystem

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.floor
import kotlin.math.max

/**
 * Maps a CSS weight to an embedded Roboto weight.
 *
 * 600 maps to Bold: the prototype's Google Fonts request did not load 600, so Chrome drew Bold.
 */
internal fun fontWeightFor(w: Int): FontWeight = when (w) {
    400 -> FontWeight.Normal
    500 -> FontWeight.Medium
    600, 700 -> FontWeight.Bold
    else -> throw IllegalArgumentException("Gewicht $w nicht eingebettet")
}

/** Roboto v3.015 hhea metrics and the rounded line box Chrome draws for "normal" line height (D1). */
object RobotoMetrics {
    const val ascender = 1900
    const val descender = 500
    const val unitsPerEm = 2048

    /** Height of the line box: rounded ascent plus rounded descent, half up. */
    fun lineBox(sizeSp: Float): Float =
        roundHalfUp(ascender * sizeSp / unitsPerEm) + roundHalfUp(descender * sizeSp / unitsPerEm)

    /**
     * Distance from the top of the line box to the baseline: the rounded ascent. Where rounding shrinks the
     * descent, Blink's Linux font metrics (the references' renderer) move one unit from ascent to descent
     * so the glyphs are not cut off; the box keeps its height and the baseline rises by one.
     */
    fun baselineOffset(sizeSp: Float): Float {
        val ascent = roundHalfUp(ascender * sizeSp / unitsPerEm)
        val shrunk = roundHalfUp(descender * sizeSp / unitsPerEm) < descender * sizeSp / unitsPerEm
        return if (shrunk && ascent >= 1f) ascent - 1f else ascent
    }

    private fun roundHalfUp(value: Float): Float = floor(value + 0.5f)
}

/**
 * Text for design-system components, built on [BasicText] instead of Material 3 `Text` (which
 * would apply `bodyLarge`: line height 24 sp, letter spacing 0.5 sp). The font padding is off.
 */
@Composable
fun BaupilotText(
    text: String,
    size: TextUnit,
    weight: Int = 400,
    color: ColorToken,
    modifier: Modifier = Modifier,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
) {
    BaupilotText(
        text = text,
        size = size,
        weight = weight,
        color = color.color(),
        modifier = modifier,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
    )
}

/** Same as the [ColorToken] overload, for an already resolved [Color]. */
@Composable
fun BaupilotText(
    text: String,
    size: TextUnit,
    weight: Int = 400,
    color: Color,
    modifier: Modifier = Modifier,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
) {
    BaupilotText(
        text = AnnotatedString(text),
        size = size,
        weight = weight,
        color = color,
        modifier = modifier,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
    )
}

/**
 * Text with inline styles, for a line that changes weight or colour midway (ElRaum: "5 von 8 getestet · " plus a
 * 500 `err` "S4: Mangel erfasst"). [weight] and [color] are the defaults; spans override them.
 */
@Composable
fun BaupilotText(
    text: AnnotatedString,
    size: TextUnit,
    weight: Int = 400,
    color: ColorToken,
    modifier: Modifier = Modifier,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
) {
    BaupilotText(
        text = text,
        size = size,
        weight = weight,
        color = color.color(),
        modifier = modifier,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
    )
}

/** Same as the annotated [ColorToken] overload, for an already resolved [Color]. */
@Composable
fun BaupilotText(
    text: AnnotatedString,
    size: TextUnit,
    weight: Int = 400,
    color: Color,
    modifier: Modifier = Modifier,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
) {
    val unspecified = lineHeight == TextUnit.Unspecified
    val lineCount = remember { intArrayOf(1) }
    BasicText(
        text = text,
        modifier = if (unspecified) {
            modifier.robotoLineBox(size.value, lineCount)
        } else {
            modifier.cssLineHeightBaseline(size.value, lineHeight.value)
        },
        style = TextStyle(
            color = color,
            fontSize = size,
            fontWeight = fontWeightFor(weight),
            fontFamily = Roboto,
            letterSpacing = letterSpacing,
            lineHeight = if (unspecified) RobotoMetrics.lineBox(size.value).sp else lineHeight,
            textAlign = textAlign ?: TextAlign.Unspecified,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            // Without a style Compose ignores a line height on one-line text. Top keeps the extra leading below
            // each line; `cssLineHeightBaseline` then seats the first baseline where CSS puts it.
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Top,
                trim = LineHeightStyle.Trim.None,
            ),
        ),
        overflow = overflow,
        maxLines = maxLines,
        onTextLayout = { lineCount[0] = it.lineCount },
    )
}

/**
 * Gives text the line box of [RobotoMetrics] (D1). Android grows a line to `lineHeight` but never
 * shrinks it below the font's own height, which is up to 2 px taller than Chrome's rounded box.
 * This trims the excess from the bottom and puts the first baseline at [RobotoMetrics.baselineOffset].
 */
private fun Modifier.robotoLineBox(sizeSp: Float, lineCount: IntArray): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val lines = max(1, lineCount[0])
    val boxPx = RobotoMetrics.lineBox(sizeSp).sp.roundToPx()
    val baselinePx = RobotoMetrics.baselineOffset(sizeSp).sp.roundToPx()
    val excess = max(0, placeable.height / lines - boxPx) * lines
    val height = (placeable.height - excess).coerceIn(constraints.minHeight, constraints.maxHeight)
    val first = placeable[FirstBaseline]
    val shift = if (first == AlignmentLine.Unspecified) 0 else baselinePx - first
    val alignmentLines = buildMap<AlignmentLine, Int> {
        if (first != AlignmentLine.Unspecified) put(FirstBaseline, baselinePx)
        val last = placeable[LastBaseline]
        if (last != AlignmentLine.Unspecified) put(LastBaseline, last + shift)
    }
    layout(placeable.width, height, alignmentLines) { placeable.placeRelative(0, shift) }
}

/**
 * Puts the first baseline where Chrome does for an explicit CSS `line-height` of [lineHeightSp]: half the leading
 * (line height minus [RobotoMetrics.lineBox]) floored, above the ascent of [RobotoMetrics.baselineOffset]. Compose
 * would centre the unrounded font height instead, up to half a pixel off.
 */
private fun Modifier.cssLineHeightBaseline(sizeSp: Float, lineHeightSp: Float): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val halfLeading = floor((lineHeightSp - RobotoMetrics.lineBox(sizeSp)) / 2f)
    val baselinePx = (RobotoMetrics.baselineOffset(sizeSp) + halfLeading).sp.roundToPx()
    val first = placeable[FirstBaseline]
    val shift = if (first == AlignmentLine.Unspecified) 0 else baselinePx - first
    val alignmentLines = buildMap<AlignmentLine, Int> {
        if (first != AlignmentLine.Unspecified) put(FirstBaseline, baselinePx)
        val last = placeable[LastBaseline]
        if (last != AlignmentLine.Unspecified) put(LastBaseline, last + shift)
    }
    layout(placeable.width, placeable.height, alignmentLines) { placeable.placeRelative(0, shift) }
}
