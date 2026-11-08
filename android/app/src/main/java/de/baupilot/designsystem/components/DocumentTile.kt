package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens

/**
 * Document tile (AndHome:63): height 56, radius 12, `surfaceContainer`, padding 0 16, gap 12, 22 `muted` icon,
 * 14 / 500 label and a 14 `muted` count. A null [onClick] draws the tile and does nothing.
 */
@Composable
fun DocumentTile(
    icon: BaupilotIcon,
    label: String,
    count: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val press = if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(Tokens.surfaceContainer.color(), shape)
            .then(press)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, 22.dp, Tokens.muted.color())
        BaupilotText(
            text = label,
            size = 14.sp,
            weight = 500,
            color = Tokens.text,
            modifier = Modifier.weight(1f),
        )
        BaupilotText(text = count, size = 14.sp, color = Tokens.muted)
    }
}
