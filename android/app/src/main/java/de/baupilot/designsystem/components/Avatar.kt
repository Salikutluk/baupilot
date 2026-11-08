package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken

/** Size of a [BaupilotAvatar]: diameter and initials size, both at weight 500. */
enum class AvatarSize(val diameter: Dp, val textSize: TextUnit) {
    S40(40.dp, 16.sp),
    S56(56.dp, 20.sp),
}

/** Round avatar with centred initials: [container] fills the circle, the text uses [content]. */
@Composable
fun BaupilotAvatar(
    initials: String,
    size: AvatarSize,
    container: ColorToken,
    content: ColorToken,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(size.diameter).background(container.color(), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        BaupilotText(text = initials, size = size.textSize, weight = 500, color = content)
    }
}
