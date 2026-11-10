package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens

/**
 * Check row with a trailing note (AndEsBelegreife:55-60): at least 44 tall, gap 16, a 20 box with radius 4 and a 2
 * border. A [done] row fills the box with `primary` and a 14 tick in `onPrimary`; an open row has an empty `outline` box.
 * [title] is 15 / 400 and takes the remaining width, [meta] is 13 in `muted`.
 */
@Composable
fun CheckMetaRow(title: String, meta: String, done: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(4.dp)
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 44.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val box = Modifier.size(20.dp)
        if (done) {
            Box(
                box.background(Tokens.primary.color(), shape).border(2.dp, Tokens.primary.color(), shape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(BaupilotIcon.CheckW35, 14.dp, Tokens.onPrimary.color())
            }
        } else {
            Box(box.border(2.dp, Tokens.outline.color(), shape))
        }
        BaupilotText(text = title, size = 15.sp, color = Tokens.text, modifier = Modifier.weight(1f))
        BaupilotText(text = meta, size = 13.sp, color = Tokens.muted)
    }
}
