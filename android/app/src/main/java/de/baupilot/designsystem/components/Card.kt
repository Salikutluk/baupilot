package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens

/**
 * Card surface: [container] fill, [radius], optional 1 [border], [padding] inside. Built on
 * foundation modifiers (no Material 3 Card).
 */
@Composable
fun BaupilotCard(
    modifier: Modifier = Modifier,
    container: ColorToken = Tokens.surfaceContainer,
    radius: Dp = Tokens.Radius.card,
    border: ColorToken? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    var frame = modifier.background(container.color(), shape)
    if (border != null) frame = frame.border(1.dp, border.color(), shape)
    Column(modifier = frame.padding(padding), content = content)
}
