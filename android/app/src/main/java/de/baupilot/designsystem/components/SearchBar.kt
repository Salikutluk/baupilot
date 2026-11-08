package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Roboto
import de.baupilot.designsystem.Tokens

/**
 * Search bar: height 56, radius 28, padding-x 16, gap 16. Leading search icon, 16 sp input with
 * the `placeholder` token while empty, optional 13 sp trailing text. Built on [BasicTextField]
 * with a decoration box (no Material 3 SearchBar). [contentDescription] is the aria-label
 * (AndDokumente:21).
 */
@Composable
fun BaupilotSearchBar(
    query: String,
    placeholder: String,
    trailingText: String? = null,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    container: ColorToken = Tokens.surfaceContainerHigh,
    iconColor: ColorToken = Tokens.muted,
    textColor: ColorToken = Tokens.text,
    placeholderColor: ColorToken = Tokens.placeholder,
    trailingColor: ColorToken = Tokens.muted,
    contentDescription: String = placeholder,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(container.color(), RoundedCornerShape(Tokens.Radius.search))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(BaupilotIcon.Search, 24.dp, iconColor.color())
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(
                color = textColor.color(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = Roboto,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
            cursorBrush = SolidColor(textColor.color()),
            modifier = Modifier.weight(1f).semantics { this.contentDescription = contentDescription },
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        BaupilotText(text = placeholder, size = 16.sp, color = placeholderColor)
                    }
                    innerTextField()
                }
            },
        )
        if (trailingText != null) {
            BaupilotText(text = trailingText, size = 13.sp, color = trailingColor)
        }
    }
}
