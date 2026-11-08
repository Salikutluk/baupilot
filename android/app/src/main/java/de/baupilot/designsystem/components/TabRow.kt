package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens

/**
 * Three equal tab columns, 48 high, with a 1 `divider` line below (AndGewerk:46). The selected tab is
 * `primary` with a 56 x 3 indicator (radius 3 3 0 0), the others are `muted`. Tabs have no action.
 * [tabModifier] is applied to every tab cell.
 */
@Composable
fun BaupilotTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tabModifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().semantics { this.contentDescription = contentDescription }) {
        Row(Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, label ->
                val on = index == selectedIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .then(tabModifier)
                        .height(48.dp)
                        .semantics { selected = on },
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BaupilotText(
                        text = label,
                        size = 14.sp,
                        weight = 500,
                        color = if (on) Tokens.primary else Tokens.muted,
                    )
                    val indicator = Modifier.size(width = 56.dp, height = 3.dp)
                    Box(
                        if (on) {
                            indicator.background(
                                Tokens.primary.color(),
                                RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                            )
                        } else {
                            indicator
                        },
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Tokens.divider.color()))
    }
}
