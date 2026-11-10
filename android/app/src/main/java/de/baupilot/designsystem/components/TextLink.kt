package de.baupilot.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken

/**
 * Text link (AndEsEbenheit:56): at least 44 tall in layout, [size] / [weight] text in [color], aligned at the start,
 * no underline. The text sits 1 below the centre of the box, as measured on the reference (Chrome rounds the line box
 * differently from Compose). A null [onClick] is a visible placeholder link. The layout stays 44; with a click action Compose
 * widens the hit area to the minimum touch target (48 dp) without changing the size (R11).
 */
@Composable
fun TextLink(
    text: String,
    color: ColorToken,
    modifier: Modifier = Modifier,
    size: TextUnit = 14.sp,
    weight: Int = 500,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
        contentAlignment = Alignment.CenterStart,
    ) {
        BaupilotText(text = text, size = size, weight = weight, color = color, modifier = Modifier.offset(y = 1.dp))
    }
}
