package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon

/** Trailing element of a [ProtocolBanner]. */
sealed interface BannerTrailing {
    data object None : BannerTrailing

    /** Outlined pill button, 36 content + 1 `foreground` border outside (38), padding 0 14, r18, 14 / 500. */
    class Document(val label: String, val onClick: (() -> Unit)? = null) : BannerTrailing

    /** `ChevronRightW22` at 18 in the foreground colour. */
    data object Chevron : BannerTrailing
}

/** The document button's corner radius: exactly 18, so the 36 dp content is not clamped to a semicircle by 19. */
internal val ProtocolDocumentShape = RoundedCornerShape(18.dp)

/**
 * Banner on top of a protocol screen (AndErd:25-34): radius 12, padding 12 16, gap 12, a 22 [icon], a 15 / 500 title
 * over a 13 text (gap 1) and an optional [trailing] element. With [onClick] the whole banner is one button.
 */
@Composable
fun ProtocolBanner(
    icon: BaupilotIcon,
    title: String,
    text: String,
    background: ColorToken,
    foreground: ColorToken,
    modifier: Modifier = Modifier,
    trailing: BannerTrailing = BannerTrailing.None,
    onClick: (() -> Unit)? = null,
) {
    var row = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(background.color())
    if (onClick != null) row = row.clickable(onClick = onClick)
    Row(
        modifier = row.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, 22.dp, foreground.color())
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            BaupilotText(text = title, size = 15.sp, weight = 500, color = foreground)
            BaupilotText(text = text, size = 13.sp, color = foreground)
        }
        when (trailing) {
            is BannerTrailing.Document -> ProtocolDocumentButton(trailing.label, foreground, trailing.onClick)
            BannerTrailing.Chevron -> Icon(BaupilotIcon.ChevronRightW22, 18.dp, foreground.color())
            BannerTrailing.None -> Unit
        }
    }
}

@Composable
internal fun ProtocolDocumentButton(
    label: String,
    foreground: ColorToken,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var button = modifier
        .clip(ProtocolDocumentShape)
        .border(1.dp, foreground.color(), ProtocolDocumentShape)
    if (onClick != null) button = button.clickable(onClick = onClick)
    Box(
        modifier = button.height(38.dp).padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        BaupilotText(text = label, size = 14.sp, weight = 500, color = foreground)
    }
}
