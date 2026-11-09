package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken

/**
 * Count tile (AndAbnSignatur.dc.html:26-29): radius 12, padding 12, gap 2, [value] 24 / 500 above the 13 [label].
 * Three tiles share a row as equal columns (114 each on a 390 screen, gap 8); the caller sets the width, for example
 * with `Modifier.weight(1f)`. [container] and [content] are the tone's fill and ink.
 */
@Composable
fun CountTile(
    value: String,
    label: String,
    container: ColorToken,
    content: ColorToken,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(container.color(), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BaupilotText(text = value, size = 24.sp, weight = 500, color = content)
        BaupilotText(text = label, size = 13.sp, color = content)
    }
}
