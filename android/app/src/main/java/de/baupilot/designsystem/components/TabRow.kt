package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens

/** Where the label of an unselected tab sits in its 48 high cell. */
enum class TabLabelAlignment {
    /** Above the (invisible) indicator slot, like the selected tab (AndGewerk:46). */
    Bottom,

    /** Vertically centred, no indicator slot (AndAbnGewerke:25). */
    Center,
}

/**
 * Equal tab columns, 48 high, with a 1 `divider` line below (AndGewerk:46). The selected tab is
 * `primary` with an [indicatorWidth] x 3 indicator (radius 3 3 0 0), the others are `muted` with the label
 * placed by [unselectedAlignment]. Tabs have no action unless [onSelect] is given; it receives the tapped index.
 * [tabModifier] is applied to every tab cell; [tabTags] (one per tab, optional) are test tags of the cells.
 */
@Composable
fun BaupilotTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tabModifier: Modifier = Modifier,
    tabTags: List<String> = emptyList(),
    indicatorWidth: Dp = 56.dp,
    unselectedAlignment: TabLabelAlignment = TabLabelAlignment.Bottom,
    onSelect: ((Int) -> Unit)? = null,
) {
    Column(modifier.fillMaxWidth().semantics { this.contentDescription = contentDescription }) {
        Row(Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, label ->
                val on = index == selectedIndex
                val centred = !on && unselectedAlignment == TabLabelAlignment.Center
                var cell = Modifier.weight(1f).then(tabModifier).height(48.dp)
                tabTags.getOrNull(index)?.let { cell = cell.testTag(it) }
                if (onSelect != null) cell = cell.clickable { onSelect(index) }
                Column(
                    modifier = cell.semantics { selected = on },
                    verticalArrangement = if (centred) {
                        Arrangement.Center
                    } else {
                        Arrangement.spacedBy(12.dp, Alignment.Bottom)
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BaupilotText(
                        text = label,
                        size = 14.sp,
                        weight = 500,
                        color = if (on) Tokens.primary else Tokens.muted,
                    )
                    if (!centred) {
                        val indicator = Modifier.size(width = indicatorWidth, height = 3.dp)
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
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Tokens.divider.color()))
    }
}
