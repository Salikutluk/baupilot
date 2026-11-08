package de.baupilot.designsystem

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

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
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = size,
            fontWeight = fontWeightFor(weight),
            fontFamily = Roboto,
            letterSpacing = letterSpacing,
            lineHeight = lineHeight,
            textAlign = textAlign ?: TextAlign.Unspecified,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
        ),
        overflow = overflow,
        maxLines = maxLines,
    )
}
