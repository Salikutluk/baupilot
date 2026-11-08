package de.baupilot.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.wholeDpCentered

/** Fact row: 56 high, padding 0 16, gap 16, 22 `muted` icon, 14 `muted` label, 16 `text` value (AndGewerk:26). */
@Composable
fun FactRow(icon: BaupilotIcon, label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, 22.dp, Tokens.muted.color())
        BaupilotText(
            text = label, size = 14.sp, color = Tokens.muted,
            modifier = Modifier.weight(1f).then(wholeDpCentered()),
        )
        BaupilotText(text = value, size = 16.sp, color = Tokens.text, modifier = wholeDpCentered())
    }
}
