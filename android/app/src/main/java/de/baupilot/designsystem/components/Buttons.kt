package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens

/** Size of a [FilledButton] or [OutlinedButton]: `Regular` fills the width, `Compact` wraps with padding 16. */
enum class ButtonSize { Regular, Compact }

/** Container of a [FilledButton]: primary or the error button colours. */
enum class FilledContainer { Primary, Error }

private val ButtonShape = RoundedCornerShape(Tokens.Radius.pillButton)

private fun Modifier.pressable(onClick: (() -> Unit)?): Modifier =
    if (onClick != null) clickable(role = Role.Button, onClick = onClick) else this

private fun Modifier.widthFor(size: ButtonSize): Modifier =
    if (size == ButtonSize.Regular) fillMaxWidth() else this

@Composable
private fun ButtonContent(
    label: String,
    size: ButtonSize,
    icon: BaupilotIcon?,
    contentColor: ColorToken,
    letterSpacing: TextUnit = 0.sp,
    leading: (@Composable () -> Unit)? = null,
    iconSize: Dp = 18.dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (leading != null) 10.dp else 8.dp, Alignment.CenterHorizontally),
    ) {
        if (leading != null) leading()
        if (icon != null) Icon(icon, iconSize, contentColor.color())
        BaupilotText(
            text = label,
            size = if (size == ButtonSize.Regular) 15.sp else 14.sp,
            weight = 500,
            color = contentColor,
            letterSpacing = letterSpacing,
        )
    }
}

/**
 * Filled pill button, height 48, radius 24. `Regular` 15 / 500 full width, `Compact` 14 / 500 with padding-x 16.
 * [letterSpacing] is the prototype's `letter-spacing` (AndLogin:44 uses 0.1). [horizontalPadding] replaces the padding-x
 * of the size (the "Tag 6 eintragen" button has 20, AndEsBelegreife:44).
 */
@Composable
fun FilledButton(
    label: String,
    size: ButtonSize,
    container: FilledContainer,
    modifier: Modifier = Modifier,
    icon: BaupilotIcon? = null,
    onClick: (() -> Unit)? = null,
    primaryBackground: ColorToken = Tokens.primary,
    primaryContent: ColorToken = Tokens.onPrimary,
    errorBackground: ColorToken = Tokens.errBtn,
    errorContent: ColorToken = Tokens.onErrBtn,
    letterSpacing: TextUnit = 0.sp,
    horizontalPadding: Dp? = null,
) {
    val background = if (container == FilledContainer.Primary) primaryBackground else errorBackground
    val content = if (container == FilledContainer.Primary) primaryContent else errorContent
    Box(
        modifier = modifier
            .widthFor(size)
            .height(48.dp)
            .clip(ButtonShape)
            .background(background.color(), ButtonShape)
            .pressable(onClick)
            .padding(horizontal = horizontalPadding ?: if (size == ButtonSize.Compact) 16.dp else 0.dp),
        contentAlignment = Alignment.Center,
    ) {
        ButtonContent(label, size, icon, content, letterSpacing)
    }
}

/**
 * Outlined pill button: 1 border, transparent background, radius 24. Height 48 border-box, or
 * 50 when [borderOutside] is set (the prototype's `<a>` buttons draw the border outside the box).
 * [leading] is drawn before the label with a gap of 10 (the Google mark, AndLogin:46). [iconSize] is the edge of the
 * [icon] (the Druck photo button draws 20, AndSanDruck:71).
 */
@Composable
fun OutlinedButton(
    label: String,
    modifier: Modifier = Modifier,
    contentColor: ColorToken = Tokens.primary,
    borderColor: ColorToken = Tokens.outline,
    size: ButtonSize = ButtonSize.Regular,
    icon: BaupilotIcon? = null,
    borderOutside: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    iconSize: Dp = 18.dp,
) {
    Box(
        modifier = modifier
            .widthFor(size)
            .height(if (borderOutside) 50.dp else 48.dp)
            .clip(ButtonShape)
            .border(1.dp, borderColor.color(), ButtonShape)
            .pressable(onClick)
            .padding(horizontal = if (size == ButtonSize.Compact) 16.dp else 0.dp),
        contentAlignment = Alignment.Center,
    ) {
        ButtonContent(label, size, icon, contentColor, leading = leading, iconSize = iconSize)
    }
}

/** Round 48 icon button with a 1 `outline` border and a 22 icon (AndRohbauMasse:47). */
@Composable
fun OutlinedIconButton(
    modifier: Modifier = Modifier,
    icon: BaupilotIcon = BaupilotIcon.Camera,
    label: String = "Foto aufnehmen",
    onClick: (() -> Unit)? = null,
    borderColor: ColorToken = Tokens.outline,
    iconColor: ColorToken = Tokens.primary,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(1.dp, borderColor.color(), CircleShape)
            .semantics { contentDescription = label }
            .pressable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, 22.dp, iconColor.color())
    }
}

/** Height and width rule of a [TextButton]. */
enum class TextButtonSize {
    /** Minimum height 48, padding-x 12, 14 / 500. */
    Default,

    /** Height 40, padding 10 x 12, 14 / 500 (the Home "Alle" link). */
    Compact40,

    /** Height 44, full width, radius 22, 15 / 500 (AndLogin:49). */
    Wide44,
}

/** Text button, 14 / 500, radius 24. [size] sets the height and width, see [TextButtonSize]. */
@Composable
fun TextButton(
    label: String,
    modifier: Modifier = Modifier,
    color: ColorToken = Tokens.primary,
    size: TextButtonSize = TextButtonSize.Default,
    onClick: (() -> Unit)? = null,
) {
    val shape = if (size == TextButtonSize.Wide44) RoundedCornerShape(22.dp) else ButtonShape
    val sized = when (size) {
        TextButtonSize.Default -> modifier.heightIn(min = 48.dp)
        TextButtonSize.Compact40 -> modifier.height(40.dp)
        TextButtonSize.Wide44 -> modifier.fillMaxWidth().height(44.dp)
    }
    Box(
        modifier = sized
            .clip(shape)
            .pressable(onClick)
            .padding(
                horizontal = 12.dp,
                vertical = if (size == TextButtonSize.Compact40) 10.dp else 0.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        BaupilotText(
            text = label,
            size = if (size == TextButtonSize.Wide44) 15.sp else 14.sp,
            weight = 500,
            color = color,
        )
    }
}

/** Tonal pill button: height 40, radius 20, padding-x 16, `secondaryContainer`, 14 / 500. */
@Composable
fun TonalButton(label: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(Tokens.secondaryContainer.color(), shape)
            .pressable(onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        BaupilotText(text = label, size = 14.sp, weight = 500, color = Tokens.onSecondaryContainer)
    }
}

/** Tonal icon button: 56 x 56, radius 16, `secondaryContainer` with a 24 `onSecondaryContainer` icon (AndGewerk:67). */
@Composable
fun TonalIconButton(
    icon: BaupilotIcon,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(Tokens.Radius.fab)
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(shape)
            .background(Tokens.secondaryContainer.color(), shape)
            .semantics { this.contentDescription = contentDescription }
            .pressable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, 24.dp, Tokens.onSecondaryContainer.color())
    }
}

/** Round 20 social mark: 2 `muted` border, [mark] in 11 / 700 `muted` (AndLogin:47). */
@Composable
fun GoogleMark(mark: String = "G", modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(20.dp)
            .border(2.dp, Tokens.muted.color(), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        BaupilotText(text = mark, size = 11.sp, weight = 700, color = Tokens.muted)
    }
}
