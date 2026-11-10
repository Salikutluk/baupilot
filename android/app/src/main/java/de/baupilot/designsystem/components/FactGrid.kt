package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens

/** One cell of a [FactGrid]: the [label] above the [value]. [modifier] carries the caller's test tag. */
class FactItem(val label: String, val value: String, val modifier: Modifier = Modifier)

private const val FACT_COLUMNS = 3

/**
 * Fact card of the Maler overview (AndMaUebersicht:25-32): `surfaceContainer`, radius 12, padding 14 16, three equal
 * columns with gap 8 in both directions. Each cell stacks the label (12 / 500 `muted`) and the value (16 / 500 `text`)
 * with gap 2. Built from weighted [Row]s, with no subcomposition (M4 R5); a short last row keeps the column width.
 */
@Composable
fun FactGrid(items: List<FactItem>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Tokens.surfaceContainer.color(), RoundedCornerShape(12.dp))
            .padding(PaddingValues(horizontal = 16.dp, vertical = 14.dp)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.chunked(FACT_COLUMNS).forEach { line ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(FACT_COLUMNS) { column ->
                    val item = line.getOrNull(column)
                    if (item == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        Column(Modifier.weight(1f).then(item.modifier), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            BaupilotText(text = item.label, size = 12.sp, weight = 500, color = Tokens.muted)
                            BaupilotText(text = item.value, size = 16.sp, weight = 500, color = Tokens.text)
                        }
                    }
                }
            }
        }
    }
}
