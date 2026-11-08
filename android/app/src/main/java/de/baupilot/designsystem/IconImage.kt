package de.baupilot.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

/**
 * Draws a generated [BaupilotIcon] at [size], tinted with [tint]. Built on foundation `Image`
 * (no Material 3). Icons are decorative by default: [contentDescription] stays null.
 */
@Composable
fun Icon(
    icon: BaupilotIcon,
    size: Dp,
    tint: Color,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(icon.res),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.size(size),
    )
}
