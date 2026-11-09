package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens

/**
 * Segmented buttons (AndAbnMangel.dc.html:55-61): 40 high inside a 1 `outline` border (42 outer), radius 20, equal
 * cells separated by 1 `outline` lines. The selected cell is `secondaryContainer` / `onSecondaryContainer` and its
 * label is [selectedPrefix] + item (the "✓ " of the prototype, R2); all labels are 14 / 500. [identifiers] gives each
 * cell its test tag. The cells are radio buttons: the selection is a semantic property.
 */
@Composable
fun SegmentedButtons(
    items: List<String>,
    selectedIndex: Int,
    selectedPrefix: String,
    identifiers: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .border(1.dp, Tokens.outline.color(), shape)
            .padding(1.dp)
            .clip(RoundedCornerShape(19.dp)),
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(if (selected) Modifier.background(Tokens.secondaryContainer.color()) else Modifier)
                    .testTag(identifiers[index])
                    .semantics { this.selected = selected }
                    .clickable(role = Role.RadioButton) { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                // Only the inner cells carry borders, on both sides, as the middle button of the prototype does.
                if (index > 0 && index < items.lastIndex) {
                    Separator(Modifier.align(Alignment.CenterStart))
                    Separator(Modifier.align(Alignment.CenterEnd))
                }
                BaupilotText(
                    text = if (selected) selectedPrefix + item else item,
                    size = 14.sp,
                    weight = 500,
                    color = if (selected) Tokens.onSecondaryContainer else Tokens.text,
                )
            }
        }
    }
}

/** The 1 `outline` line on the inner side of a cell (CSS `border-left` / `border-right` of the middle button). */
@Composable
private fun Separator(modifier: Modifier) {
    Box(modifier.width(1.dp).fillMaxHeight().background(Tokens.outline.color()))
}
