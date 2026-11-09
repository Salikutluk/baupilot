package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens

/**
 * Bottom action bar with a caption under the action (AndAbnSignatur.dc.html:69-72): padding 12 16 24,
 * `surfaceContainer` fill, column gap 6, [content] (the button) above the 12 `muted` centred [caption].
 */
@Composable
fun CaptionedActionBar(
    caption: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Tokens.surfaceContainer.color())
            .padding(Tokens.Layout.bottomBarPadding),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        content()
        BaupilotText(
            text = caption,
            size = 12.sp,
            color = Tokens.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
