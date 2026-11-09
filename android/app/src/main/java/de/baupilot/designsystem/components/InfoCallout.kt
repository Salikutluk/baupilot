package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens

/**
 * Info callout (AndVergleich:52): `primaryContainer` with `onPrimaryContainer` content, radius 12, padding 12 16,
 * gap 12, a 20 [icon] and 14 text at line height 20.
 */
@Composable
fun InfoCallout(text: String, modifier: Modifier = Modifier, icon: BaupilotIcon = BaupilotIcon.Info) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Tokens.Radius.listCard))
            .background(Tokens.primaryContainer.color())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, 20.dp, Tokens.onPrimaryContainer.color())
        BaupilotText(
            text = text,
            size = 14.sp,
            color = Tokens.onPrimaryContainer,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f),
        )
    }
}
