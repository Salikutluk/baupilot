package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.wholeDpCentered

/**
 * Hint card on `primaryContainer` (AndDokumente:31): radius 16, padding 12 16, gap 16, 24 icon, title 16 / 500
 * over a 13 subtitle (gap 2), amount 16 / 500 on one line. Without [onClick] the card is not a button.
 */
@Composable
fun HintCard(
    icon: BaupilotIcon,
    title: String,
    subtitle: String,
    amount: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(Tokens.Radius.card)
    var frame = modifier.fillMaxWidth().clip(shape).background(Tokens.primaryContainer.color())
    if (onClick != null) frame = frame.clickable(role = Role.Button, onClick = onClick)
    Row(
        modifier = frame.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, 24.dp, Tokens.onPrimaryContainer.color())
        Column(Modifier.weight(1f).then(wholeDpCentered()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BaupilotText(text = title, size = 16.sp, weight = 500, color = Tokens.onPrimaryContainer)
            BaupilotText(text = subtitle, size = 13.sp, color = Tokens.onPrimaryContainer)
        }
        BaupilotText(
            text = amount, size = 16.sp, weight = 500, color = Tokens.onPrimaryContainer, maxLines = 1,
            modifier = wholeDpCentered(),
        )
    }
}
