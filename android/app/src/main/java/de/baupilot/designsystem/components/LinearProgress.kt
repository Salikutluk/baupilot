package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens

/**
 * Linear progress bar: height 4, radius 2, `primary` indicator, no gap and no stop dot. The stock
 * Material 3 indicator draws both, so this one is built from two boxes.
 */
@Composable
fun BaupilotLinearProgress(
    progress: Float,
    track: ColorToken = Tokens.track,
    modifier: Modifier = Modifier,
    indicator: ColorToken = Tokens.primary,
) {
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(shape)
            .background(track.color()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(shape)
                .background(indicator.color()),
        )
    }
}
