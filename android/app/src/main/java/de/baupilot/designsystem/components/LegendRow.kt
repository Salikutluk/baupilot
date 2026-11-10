package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens

/**
 * Legend line above the tile grid (AndFlBad:50): [direction] on the left, the swatch (12 square, radius 2, `errBg`
 * with a 2 `err` border inside) and [mark] on the right, gap 6. Text 12 / 600 `muted` (renders Bold). The row takes
 * 6 less height than it draws, the prototype's `margin-bottom: -6px`, so the grid sits 6 closer. The row is 15 tall:
 * Roboto has no up arrow, Chrome draws it from a fallback font whose line box is 1 taller than Roboto's 14
 * (measured on the reference: swatch 2 below the row top, grid 9 below the row top).
 */
@Composable
fun LegendRow(direction: String, mark: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val overlap = 6.dp.roundToPx()
                layout(placeable.width, placeable.height - overlap) { placeable.place(0, 0) }
            }
            .heightIn(min = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BaupilotText(text = direction, size = 12.sp, weight = 600, color = Tokens.muted)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val shape = RoundedCornerShape(2.dp)
            Row(
                Modifier
                    .size(12.dp)
                    .background(Tokens.errBg.color(), shape)
                    .border(2.dp, Tokens.err.color(), shape),
            ) {}
            BaupilotText(text = mark, size = 12.sp, weight = 600, color = Tokens.muted)
        }
    }
}
