package de.baupilot.designsystem.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.cssShadow

/** Leading icon of an [ExtendedFab]. */
enum class FabIcon(internal val icon: BaupilotIcon) {
    Plus(BaupilotIcon.PlusW22),
    Play(BaupilotIcon.PlayW22),
}

/**
 * Extended FAB: height 56, radius 16, padding 0 20 0 16, gap 10, 15 / 500 label and a 22 icon
 * (AndHome:73). The screen places it (right 16, bottom 96 or 24).
 */
@Composable
fun ExtendedFab(
    label: String,
    icon: FabIcon,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    background: ColorToken = Tokens.primaryContainer,
    contentColor: ColorToken = Tokens.onPrimaryContainer,
) {
    val shape = RoundedCornerShape(Tokens.Radius.fab)
    val press = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Row(
        modifier = modifier
            .cssShadow(offsetY = 3.dp, blur = 8.dp, color = Tokens.fabShadow, shape = shape)
            .height(56.dp)
            .clip(shape)
            .background(background.color(), shape)
            .then(press)
            .padding(PaddingValues(start = 16.dp, end = 20.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon.icon, 22.dp, contentColor.color())
        BaupilotText(text = label, size = 15.sp, weight = 500, color = contentColor)
    }
}
