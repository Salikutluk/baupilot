package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens

/**
 * Mitteilung (AndMitteilungen:30): top-aligned row, padding 12 16, gap 16. 40 round icon circle with a 20 icon.
 * Title 16 (700 when [unread], else 400) and [whenLabel] 12 `muted` share the first line (gap 8, [whenLabel] right-aligned); [text] is
 * 14 / 20 `muted`. The `unread` token fills the row when [unread]. Without [onClick] the row is not a button;
 * the screen reaches it through its own test tag.
 */
@Composable
fun NotificationListItem(
    icon: BaupilotIcon,
    iconBackground: ColorToken,
    iconForeground: ColorToken,
    title: String,
    whenLabel: String,
    text: String,
    unread: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var row = modifier.fillMaxWidth()
    if (unread) row = row.background(Tokens.unread.color())
    if (onClick != null) row = row.clickable(role = Role.Button, onClick = onClick)
    Row(
        modifier = row.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NotificationCircle(icon, iconBackground, iconForeground)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BaupilotText(
                    text = title,
                    size = 16.sp,
                    weight = if (unread) 700 else 400,
                    color = Tokens.text,
                    modifier = Modifier.weight(1f),
                )
                BaupilotText(text = whenLabel, size = 12.sp, color = Tokens.muted, maxLines = 1)
            }
            BaupilotText(text = text, size = 14.sp, lineHeight = 20.sp, color = Tokens.muted)
        }
    }
}

/** 40 round circle with a 20 icon. */
@Composable
internal fun NotificationCircle(
    icon: BaupilotIcon,
    background: ColorToken,
    foreground: ColorToken,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(40.dp).background(background.color(), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, 20.dp, foreground.color())
    }
}
