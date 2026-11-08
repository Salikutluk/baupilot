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
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        if (icon != null) Icon(icon, 18.dp, contentColor.color())
        BaupilotText(
            text = label,
            size = if (size == ButtonSize.Regular) 15.sp else 14.sp,
            weight = 500,
            color = contentColor,
        )
    }
}

/** Filled pill button, height 48, radius 24. `Regular` 15 / 500 full width, `Compact` 14 / 500 with padding-x 16. */
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
            .padding(horizontal = if (size == ButtonSize.Compact) 16.dp else 0.dp),
        contentAlignment = Alignment.Center,
    ) {
        ButtonContent(label, size, icon, content)
    }
}

/**
 * Outlined pill button: 1 border, transparent background, radius 24. Height 48 border-box, or
 * 50 when [borderOutside] is set (the prototype's `<a>` buttons draw the border outside the box).
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
    onClick: (() -> Unit)? = null,
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
        ButtonContent(label, size, icon, contentColor)
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

/** Text button: minimum height 48, padding-x 12, 14 / 500, radius 24. */
@Composable
fun TextButton(
    label: String,
    modifier: Modifier = Modifier,
    color: ColorToken = Tokens.primary,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(ButtonShape)
            .pressable(onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        BaupilotText(text = label, size = 14.sp, weight = 500, color = color)
    }
}
