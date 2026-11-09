package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Roboto
import de.baupilot.designsystem.Tokens

/**
 * Multi-line outlined field in its focused look (AndAbnMangel.dc.html:35-38): 2 `primary` border, radius 4, text 16
 * with line height 22 and CSS padding 14 (16 from the outer edge), so [rows] lines are `rows * 22 + 32` tall
 * (98 for three). The 12 `primary` label sits at top -8, left 12 on a [notch] that hides the border behind it.
 * Like [BaupilotOutlinedTextField] the look is static: the prototype draws it regardless of real focus.
 */
@Composable
fun BaupilotOutlinedTextArea(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    rows: Int = 3,
    notch: ColorToken = Tokens.bg,
) {
    val shape = RoundedCornerShape(4.dp)
    Box(modifier.fillMaxWidth().height((rows * 22 + 32).dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((rows * 22 + 32).dp)
                .border(2.dp, Tokens.primary.color(), shape)
                .padding(16.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                minLines = rows,
                maxLines = rows,
                textStyle = TextStyle(
                    color = Tokens.text.color(),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = Roboto,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None,
                    ),
                ),
                cursorBrush = SolidColor(Tokens.primary.color()),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(
            Modifier
                .offset(x = 12.dp, y = (-8).dp)
                .background(notch.color())
                .padding(horizontal = 4.dp),
        ) {
            BaupilotText(text = label, size = 12.sp, color = Tokens.primary)
        }
    }
}
