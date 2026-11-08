package de.baupilot.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.wholeDpCentered

/**
 * Row with a trailing text action (AndVergleich:46): 56 minimum, gap 16, title 16 over a 14 `muted` subtitle
 * (gap 2), action 14 / 500 `primary` with padding 0 4. Without [onClick] the row is not a button.
 */
@Composable
fun ActionRow(
    title: String,
    subtitle: String,
    actionLabel: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    var frame = modifier.fillMaxWidth().heightIn(min = 56.dp)
    if (onClick != null) frame = frame.clickable(role = Role.Button, onClick = onClick)
    Row(
        modifier = frame,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f).then(wholeDpCentered()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BaupilotText(text = title, size = 16.sp, color = Tokens.text)
            BaupilotText(text = subtitle, size = 14.sp, color = Tokens.muted)
        }
        BaupilotText(
            text = actionLabel,
            size = 14.sp,
            weight = 500,
            color = Tokens.primary,
            modifier = wholeDpCentered().padding(horizontal = 4.dp),
        )
    }
}
