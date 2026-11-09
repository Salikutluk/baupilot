package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens

/** What a [PhotoTile] shows. */
sealed interface PhotoTileKind {
    /** A hint tile: [illustration] above the 11 / 500 `muted` [label] (AndAbnPruefung.dc.html:57). */
    data class Guide(val label: String, val illustration: @Composable () -> Unit) : PhotoTileKind

    /** The photo placeholder with the caption bar at the bottom and, when [checked], the green tick (:58). */
    data class Scene(val caption: String, val checked: Boolean) : PhotoTileKind

    /** A taken photo, shown as a 22 `muted` icon (AndAbnMangel.dc.html:43). */
    data class Icon(val icon: BaupilotIcon) : PhotoTileKind

    /** The camera button with a 1 `outline` border; [label] null draws the icon only (:59, AndAbnMangel:49). */
    data class Add(val label: String?) : PhotoTileKind
}

/**
 * Photo tile of the inspection screens: [height] high, corners [radius], background [fill] (Guide and Icon).
 * The width comes from [modifier] (weighted grid cell or fixed). [contentDescription] is the aria-label;
 * a non-null [onClick] makes the tile a button.
 */
@Composable
fun PhotoTile(
    kind: PhotoTileKind,
    height: Dp,
    modifier: Modifier = Modifier,
    radius: Dp = 12.dp,
    fill: ColorToken = Tokens.surfaceContainerHigh,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(radius)
    var frame = modifier.height(height).clip(shape)
    frame = when (kind) {
        is PhotoTileKind.Guide, is PhotoTileKind.Icon -> frame.background(fill.color(), shape)
        is PhotoTileKind.Add -> frame.border(1.dp, Tokens.outline.color(), shape)
        is PhotoTileKind.Scene -> frame
    }
    if (contentDescription != null) frame = frame.semantics { this.contentDescription = contentDescription }
    if (onClick != null) frame = frame.clickable(role = Role.Button, onClick = onClick)
    Box(modifier = frame, contentAlignment = Alignment.Center) {
        when (kind) {
            is PhotoTileKind.Guide -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            ) {
                kind.illustration()
                BaupilotText(text = kind.label, size = 11.sp, weight = 500, color = Tokens.muted)
            }

            is PhotoTileKind.Scene -> SceneContent(kind)
            is PhotoTileKind.Icon -> Icon(kind.icon, 22.dp, Tokens.muted.color())
            is PhotoTileKind.Add -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            ) {
                Icon(BaupilotIcon.Camera, 22.dp, Tokens.primary.color())
                kind.label?.let { BaupilotText(text = it, size = 12.sp, weight = 500, color = Tokens.primary) }
            }
        }
    }
}

@Composable
private fun SceneContent(kind: PhotoTileKind.Scene) {
    Box(Modifier.fillMaxSize()) {
        PhotoPlaceholder()
        if (kind.checked) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 6.dp)
                    .size(20.dp)
                    .background(Tokens.photoBadge.color(), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(BaupilotIcon.CheckW35, 11.dp, Tokens.onPhoto.color())
            }
        }
        // 11 at weight 700 on purpose: the prototype asks for 600, Roboto has no 600 cut and Chrome renders 700 (R4).
        BaupilotText(
            text = kind.caption,
            size = 11.sp,
            weight = 700,
            color = Tokens.onPhoto,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Tokens.photoCaption.color())
                .padding(horizontal = 6.dp, vertical = 4.dp),
        )
    }
}
