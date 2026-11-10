package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens

/** One row of a [StatusList]: name, note and the status chip's text and colours. */
class StatusRow(
    val title: String,
    val subtitle: String,
    val status: String,
    val statusColors: StatusChipColors,
)

/**
 * Rows of name, small note and an `H28` status chip on a [background] container, radius 12 (AndElVerteiler:50-56).
 * Row min-height 56, padding 0 12, gap 12, 15 over 12 `muted` (gap 2); a 1 dp [divider] sits between the rows.
 */
@Composable
fun StatusList(
    rows: List<StatusRow>,
    background: ColorToken,
    divider: ColorToken,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(modifier = modifier.fillMaxWidth().clip(shape).background(background.color())) {
        rows.forEachIndexed { index, row ->
            if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(divider.color()))
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BaupilotText(text = row.title, size = 15.sp, color = Tokens.text)
                    BaupilotText(text = row.subtitle, size = 12.sp, color = Tokens.muted)
                }
                StatusChip(row.status, row.statusColors, StatusChipSize.H28)
            }
        }
    }
}
