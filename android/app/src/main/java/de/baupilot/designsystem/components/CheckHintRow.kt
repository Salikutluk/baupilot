package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens

/**
 * A check with a hint (AndElRauchmelder:44-52) on a `primaryContainer` card: a 22 square, radius 4, with a 2 [border],
 * margin-top 1, gap 12, top-aligned with the text (15 / 500 title, 13 on 18 hint, gap 2). Done fills the square with
 * [fill] and draws a 14 tick in [tick]. The text takes [textColor] (the card's content colour).
 */
@Composable
fun CheckHintRow(
    title: String,
    hint: String,
    done: Boolean,
    border: ColorToken,
    fill: ColorToken,
    tick: ColorToken,
    modifier: Modifier = Modifier,
    textColor: ColorToken = Tokens.onPrimaryContainer,
) {
    checkHintRowMeasured(title, hint, done, border, fill, tick, modifier, textColor = textColor)
}

/** Same row with a layout hook for the metric tests: [boxModifier] sits on the 22 square. */
@Composable
internal fun checkHintRowMeasured(
    title: String,
    hint: String,
    done: Boolean,
    border: ColorToken,
    fill: ColorToken,
    tick: ColorToken,
    modifier: Modifier = Modifier,
    boxModifier: Modifier = Modifier,
    textColor: ColorToken = Tokens.onPrimaryContainer,
) {
    val shape = RoundedCornerShape(4.dp)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        var box = Modifier.padding(top = 1.dp).then(boxModifier).size(22.dp)
        if (done) box = box.background(fill.color(), shape)
        Box(modifier = box.border(2.dp, border.color(), shape), contentAlignment = Alignment.Center) {
            if (done) Icon(BaupilotIcon.CheckW35, 14.dp, tick.color())
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BaupilotText(text = title, size = 15.sp, weight = 500, color = textColor)
            BaupilotText(text = hint, size = 13.sp, color = textColor, lineHeight = 18.sp)
        }
    }
}
