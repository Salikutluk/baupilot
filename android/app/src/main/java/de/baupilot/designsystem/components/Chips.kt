package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens
import kotlin.math.floor
import kotlin.math.roundToInt

/** Outlined filter or assist chip frame: height 32 border-box, radius 8. */
@Composable
private fun ChipFrame(
    modifier: Modifier,
    background: ColorToken?,
    border: ColorToken?,
    padding: PaddingValues,
    wholeDpWidth: Boolean = false,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(Tokens.Radius.chip)
    var frame = modifier.height(32.dp)
    if (wholeDpWidth) frame = frame.wholeDpWidth()
    if (background != null) frame = frame.background(background.color(), shape)
    if (border != null) frame = frame.border(1.dp, border.color(), shape)
    // CSS border-box: the padding sits inside the border, so the content starts a border width further in.
    if (border != null) frame = frame.padding(1.dp)
    Row(
        modifier = frame.padding(padding).then(if (wholeDpWidth) Modifier.unboundedContent() else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        content()
    }
}

/**
 * Chrome paints a chip's edges on whole dp and its text advances are narrower than Layoutlib's whole-pixel ones
 * (a 14 sp label is up to 1 dp wider here), so the chip is the whole dp at or below its natural width. Measured on the
 * Dokumente filter row: the reference chips are 67, 98 and 91 dp wide, the natural ones 67.67, 98.33 and 92.
 */
private fun Modifier.wholeDpWidth(): Modifier = layout { measurable, constraints ->
    val density = this.density
    val natural = measurable.maxIntrinsicWidth(constraints.maxHeight)
    val width = (floor(natural / density) * density).roundToInt()
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(width, placeable.height) { placeable.place(0, 0) }
}

/** Lets the chip's content keep its natural width when the frame is a fraction of a dp narrower, so the label never wraps. */
private fun Modifier.unboundedContent(): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(maxWidth = Constraints.Infinity))
    layout(minOf(placeable.width, constraints.maxWidth), placeable.height) { placeable.place(0, 0) }
}

/** Assist chip: 1 `outline` border, `muted` 14 / 500 text, optional leading check (AndLogin:27). */
@Composable
fun AssistChip(
    label: String,
    leadingCheck: Boolean = false,
    modifier: Modifier = Modifier,
    borderColor: ColorToken = Tokens.outline,
    textColor: ColorToken = Tokens.muted,
    checkColor: ColorToken = Tokens.primary,
) {
    ChipFrame(
        modifier = modifier,
        background = null,
        border = borderColor,
        padding = PaddingValues(horizontal = 12.dp),
    ) {
        if (leadingCheck) Icon(BaupilotIcon.CheckW22, 16.dp, checkColor.color())
        BaupilotText(text = label, size = 14.sp, weight = 500, color = textColor)
    }
}

/**
 * Filter chip. Selected: `secondaryContainer` fill, no border. Unselected: transparent with a
 * 1 `outline` border. [showCheck] draws the check and uses padding 0 12 0 8 (AndAbnElemente:45).
 */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    showCheck: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    selectedBackground: ColorToken = Tokens.secondaryContainer,
    selectedTextColor: ColorToken = Tokens.onSecondaryContainer,
    unselectedTextColor: ColorToken = Tokens.muted,
    borderColor: ColorToken = Tokens.outline,
) {
    val clickable = if (onClick != null) {
        Modifier
            .clip(RoundedCornerShape(Tokens.Radius.chip))
            .clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    ChipFrame(
        modifier = modifier.then(clickable).semantics { this.selected = selected },
        background = if (selected) selectedBackground else null,
        border = if (selected) null else borderColor,
        padding = if (showCheck) {
            PaddingValues(start = 8.dp, end = 12.dp)
        } else {
            PaddingValues(horizontal = 12.dp)
        },
        wholeDpWidth = true,
    ) {
        val textColor = if (selected) selectedTextColor else unselectedTextColor
        if (showCheck) Icon(BaupilotIcon.CheckW26, 16.dp, textColor.color())
        BaupilotText(text = label, size = 14.sp, weight = 500, color = textColor)
    }
}

/** Look of a [TagChip]. */
enum class TagChipStyle { Filled, Outlined }

/**
 * Static tag chip (AndGewerk:25): 32 high, radius 8, padding 0 12, 14 / 500. `Filled` is `secondaryContainer`,
 * `Outlined` has a 1 `outline` border and `muted` text. It has no selection state.
 */
@Composable
fun TagChip(label: String, style: TagChipStyle, modifier: Modifier = Modifier) {
    val filled = style == TagChipStyle.Filled
    ChipFrame(
        modifier = modifier,
        background = if (filled) Tokens.secondaryContainer else null,
        border = if (filled) null else Tokens.outline,
        padding = PaddingValues(horizontal = 12.dp),
        wholeDpWidth = true,
    ) {
        BaupilotText(
            text = label,
            size = 14.sp,
            weight = 500,
            color = if (filled) Tokens.onSecondaryContainer else Tokens.muted,
        )
    }
}

/** State of an [ObjectChip] (AndSanBad:30). */
enum class ObjectChipState { Done, Now, Open }

/** Object chip: `Done` is green with a check, `Now` is `secondaryContainer`, `Open` is outlined. */
@Composable
fun ObjectChip(
    label: String,
    state: ObjectChipState,
    modifier: Modifier = Modifier,
    doneBackground: ColorToken = Tokens.okBg,
    doneForeground: ColorToken = Tokens.okFg,
    nowBackground: ColorToken = Tokens.secondaryContainer,
    nowForeground: ColorToken = Tokens.onSecondaryContainer,
    openForeground: ColorToken = Tokens.muted,
    borderColor: ColorToken = Tokens.outline,
) {
    val foreground = when (state) {
        ObjectChipState.Done -> doneForeground
        ObjectChipState.Now -> nowForeground
        ObjectChipState.Open -> openForeground
    }
    ChipFrame(
        modifier = modifier,
        background = when (state) {
            ObjectChipState.Done -> doneBackground
            ObjectChipState.Now -> nowBackground
            ObjectChipState.Open -> null
        },
        border = if (state == ObjectChipState.Open) borderColor else null,
        padding = PaddingValues(horizontal = 12.dp),
    ) {
        if (state == ObjectChipState.Done) Icon(BaupilotIcon.CheckW30, 16.dp, foreground.color())
        BaupilotText(text = label, size = 14.sp, weight = 500, color = foreground)
    }
}

/** Size of a [StatusChip]: height, horizontal padding, text size and corner radius. */
enum class StatusChipSize(
    val height: Dp?,
    val padding: PaddingValues,
    val textSize: TextUnit,
    val radius: Dp,
) {
    /** AndHome:54. */
    H32(32.dp, PaddingValues(horizontal = 12.dp), 13.sp, 8.dp),

    /** AndEsUebersicht:49. */
    H28(28.dp, PaddingValues(horizontal = 10.dp), 12.sp, 8.dp),

    /** Padding 4 x 8, height from content (AndAbnMaengel:33). */
    Auto(null, PaddingValues(horizontal = 8.dp, vertical = 4.dp), 12.sp, 8.dp),

    /** AndEsBelegreife:52. */
    H24(24.dp, PaddingValues(horizontal = 8.dp), 12.sp, 6.dp),
}

/** Fill, text and border of a [StatusChip]. A null [background] or [border] is not drawn. */
sealed class StatusChipColors(
    val background: ColorToken?,
    val foreground: ColorToken,
    val border: ColorToken? = null,
) {
    /** [background] varies per screen (`Tokens.okBg.gewerk`). */
    class Ok(background: ColorToken = Tokens.okBg, foreground: ColorToken = Tokens.okFg) :
        StatusChipColors(background, foreground)
    object Info : StatusChipColors(Tokens.infoBg, Tokens.infoFg)
    object Error : StatusChipColors(Tokens.errBg, Tokens.errFg)
    class Warn(fg: ColorToken = Tokens.warnFg) : StatusChipColors(Tokens.warnBg, fg)
    object Running : StatusChipColors(Tokens.primaryContainer, Tokens.onPrimaryContainer)
    object Open : StatusChipColors(Tokens.planBg, Tokens.muted)
    object Pending : StatusChipColors(Tokens.openBg, Tokens.text)
    object Neutral : StatusChipColors(Tokens.surfaceContainerHigh, Tokens.muted)
    object Outline : StatusChipColors(null, Tokens.primary, Tokens.outline)
}

/** The only status chip. Text weight is 500; the size sets height, padding, text size and radius. */
@Composable
fun StatusChip(
    text: String,
    colors: StatusChipColors,
    size: StatusChipSize = StatusChipSize.H32,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(size.radius)
    var frame = modifier
    if (size.height != null) frame = frame.height(size.height)
    if (colors.background != null) frame = frame.background(colors.background.color(), shape)
    if (colors.border != null) frame = frame.border(1.dp, colors.border.color(), shape)
    Row(
        modifier = frame.padding(size.padding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BaupilotText(text = text, size = size.textSize, weight = 500, color = colors.foreground)
    }
}
