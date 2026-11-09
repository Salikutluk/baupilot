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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Roboto
import de.baupilot.designsystem.Tokens

/** Border look of a [BaupilotOutlinedTextField]: 2 `primary` when `Focused`, 1 `outline` when `Idle`. */
enum class FieldLook { Focused, Idle }

/**
 * Outlined text field (AndLogin:34-41): height 56, radius 4, text inset 16 from the outer edge (CSS padding plus
 * border), 16 text, placeholder in `Tokens.placeholder`. The 12 label sits at top -8, left 12 with padding 0 4 on
 * [notch], which hides the border behind it. [look] is static: the prototype draws it regardless of real focus (D15).
 * A [trailingIcon] is a 24 `muted` icon at end 14, top 16 (AndAbnMangel:67-69); the text then ends 48 before the border
 * (CSS padding-right 48 plus the border). [fieldModifier] sits on the editable text node itself, so a test tag
 * there finds the node that carries the text.
 */
@Composable
fun BaupilotOutlinedTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    look: FieldLook = FieldLook.Idle,
    isPassword: Boolean = false,
    notch: ColorToken = Tokens.bg,
    trailingIcon: BaupilotIcon? = null,
    fieldModifier: Modifier = Modifier,
    readOnly: Boolean = false,
) {
    val focused = look == FieldLook.Focused
    val shape = RoundedCornerShape(4.dp)
    Box(modifier.height(56.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = if (focused) 2.dp else 1.dp,
                    color = (if (focused) Tokens.primary else Tokens.outline).color(),
                    shape = shape,
                )
                .padding(start = 16.dp, end = if (trailingIcon != null) (if (focused) 50.dp else 49.dp) else 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                readOnly = readOnly,
                textStyle = TextStyle(
                    color = Tokens.text.color(),
                    fontSize = 16.sp,
                    fontFamily = Roboto,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                cursorBrush = SolidColor(Tokens.primary.color()),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = fieldModifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) BaupilotText(text = placeholder, size = 16.sp, color = Tokens.placeholder)
                    inner()
                },
            )
        }
        if (trailingIcon != null) {
            Box(Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 14.dp)) {
                Icon(trailingIcon, 24.dp, Tokens.muted.color())
            }
        }
        Box(
            Modifier
                .offset(x = 12.dp, y = (-8).dp)
                .background(notch.color())
                .padding(horizontal = 4.dp),
        ) {
            BaupilotText(text = label, size = 12.sp, color = if (focused) Tokens.primary else Tokens.muted)
        }
    }
}
