package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens

/**
 * Info callout (AndVergleich:52): `primaryContainer` with `onPrimaryContainer` content, radius 12, padding 12 16,
 * gap 12, a 20 [icon] and 14 text at line height 20. [container] and [content] recolour it (ElVerteiler: `warnBg` /
 * `warnFg`); a null [container] draws no fill (the Gefälle note, AndSanGefaelle:78). [contentPadding], [textSize],
 * [lineHeight], [iconSize], [gap] and [verticalAlignment] default to the values above.
 */
@Composable
fun InfoCallout(
    text: String,
    modifier: Modifier = Modifier,
    icon: BaupilotIcon = BaupilotIcon.Info,
    container: ColorToken? = Tokens.primaryContainer,
    content: ColorToken = Tokens.onPrimaryContainer,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    textSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp,
    iconSize: Dp = 20.dp,
    gap: Dp = 12.dp,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
) {
    val shape = RoundedCornerShape(Tokens.Radius.listCard)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (container != null) Modifier.background(container.color()) else Modifier)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = verticalAlignment,
    ) {
        Icon(icon, iconSize, content.color())
        BaupilotText(
            text = text,
            size = textSize,
            color = content,
            lineHeight = lineHeight,
            modifier = Modifier.weight(1f),
        )
    }
}
