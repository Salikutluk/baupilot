package de.baupilot.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken

/**
 * Ordered list of instructions (AndElVerteiler:45): `padding-left: 20`, [size] 14 on a [lineHeight] of 20, gap 4.
 * The "N." marker is right-aligned so that it ends at the [indent]; the item text wraps inside the hanging indent.
 */
@Composable
fun NumberedList(
    items: List<String>,
    color: ColorToken,
    modifier: Modifier = Modifier,
    size: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp,
    gap: Dp = 4.dp,
    indent: Dp = 20.dp,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        items.forEachIndexed { index, item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.width(indent), contentAlignment = Alignment.TopEnd) {
                    BaupilotText(text = "${index + 1}.\u00A0", size = size, color = color, lineHeight = lineHeight)
                }
                BaupilotText(text = item, size = size, color = color, lineHeight = lineHeight, modifier = Modifier.weight(1f))
            }
        }
    }
}
