package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.wholeDpCentered

/** Minimum row height. 56 is the single-line default, 72 is Dokumente, 64 is Home. */
enum class ListItemHeight(val dp: Dp) {
    H48(48.dp),
    H56(56.dp),
    H60(60.dp),
    H64(64.dp),
    H72(72.dp),
}

/** Leading element of a list row. */
sealed interface Leading {
    /** 40 round avatar with initials (16 / 500) or an id text (14 / 500) or an icon (20). */
    class Avatar private constructor(
        val text: String?,
        val icon: BaupilotIcon?,
        val textSize: TextUnit,
        val background: ColorToken,
        val foreground: ColorToken,
    ) : Leading {
        companion object {
            /** Initials, 16 / 500. */
            fun initials(text: String, background: ColorToken, foreground: ColorToken) =
                Avatar(text, null, 16.sp, background, foreground)

            /** Identifier such as "EG-2", 14 / 500. */
            fun id(text: String, background: ColorToken, foreground: ColorToken) =
                Avatar(text, null, 14.sp, background, foreground)

            /** Icon, 20. */
            fun icon(icon: BaupilotIcon, background: ColorToken, foreground: ColorToken) =
                Avatar(null, icon, 16.sp, background, foreground)
        }
    }

    /** 40 round colour swatch with a 1 `outlineVariant` border. */
    class Swatch(
        val color: ColorToken,
        val borderColor: ColorToken = Tokens.outlineVariant,
    ) : Leading

    /** Plain icon, 22. */
    class Icon(val icon: BaupilotIcon, val tint: ColorToken = Tokens.muted) : Leading
}

/** Trailing element of a list row. */
sealed interface Trailing {
    class StatusChip(
        val text: String,
        val colors: StatusChipColors,
        val size: StatusChipSize = StatusChipSize.H32,
    ) : Trailing

    /** Text with size 14, 13 or 12 at weight 500. */
    class Text(
        val text: String,
        val size: TextUnit = 14.sp,
        val color: ColorToken = Tokens.muted,
    ) : Trailing

    /** A switch; pass the BaupilotSwitch call as [content] so the row does not own its state. */
    class Switch(val content: @Composable () -> Unit) : Trailing
}

/** List row: gap 16, title 16 `text`, subtitle 14 `muted` (text gap 2), no divider. */
@Composable
fun BaupilotListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: Leading? = null,
    trailing: Trailing? = null,
    minHeight: ListItemHeight = ListItemHeight.H64,
    horizontalPadding: Dp = 16.dp,
    background: ColorToken? = null,
    titleWeight: Int = 400,
    ellipsis: Boolean = false,
    onClick: (() -> Unit)? = null,
    titleColor: ColorToken = Tokens.text,
    subtitleColor: ColorToken = Tokens.muted,
) {
    var row = modifier.fillMaxWidth()
    if (background != null) row = row.background(background.color())
    if (onClick != null) row = row.clickable(onClick = onClick)
    Row(
        modifier = row
            .heightIn(min = minHeight.dp)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (leading != null) LeadingContent(leading)
        Column(
            modifier = Modifier.weight(1f).then(wholeDpCentered()),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            BaupilotText(
                text = title,
                size = 16.sp,
                weight = titleWeight,
                color = titleColor,
                maxLines = if (ellipsis) 1 else Int.MAX_VALUE,
                overflow = if (ellipsis) TextOverflow.Ellipsis else TextOverflow.Clip,
            )
            if (subtitle != null) {
                BaupilotText(
                    text = subtitle,
                    size = 14.sp,
                    color = subtitleColor,
                    maxLines = if (ellipsis) 1 else Int.MAX_VALUE,
                    overflow = if (ellipsis) TextOverflow.Ellipsis else TextOverflow.Clip,
                )
            }
        }
        if (trailing != null) TrailingContent(trailing)
    }
}

@Composable
private fun LeadingContent(leading: Leading) {
    when (leading) {
        is Leading.Avatar -> Box(
            modifier = Modifier.size(40.dp).background(leading.background.color(), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (leading.icon != null) {
                Icon(leading.icon, 20.dp, leading.foreground.color())
            } else if (leading.text != null) {
                BaupilotText(
                    text = leading.text,
                    size = leading.textSize,
                    weight = 500,
                    color = leading.foreground,
                )
            }
        }

        is Leading.Swatch -> Box(
            Modifier
                .size(40.dp)
                .background(leading.color.color(), CircleShape)
                .border(1.dp, leading.borderColor.color(), CircleShape),
        )

        is Leading.Icon -> Icon(leading.icon, 22.dp, leading.tint.color())
    }
}

@Composable
private fun TrailingContent(trailing: Trailing) {
    when (trailing) {
        is Trailing.StatusChip -> StatusChip(trailing.text, trailing.colors, trailing.size)
        is Trailing.Text -> BaupilotText(
            text = trailing.text,
            size = trailing.size,
            weight = 500,
            color = trailing.color,
        )

        is Trailing.Switch -> trailing.content()
    }
}
