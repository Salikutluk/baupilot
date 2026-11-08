package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.compare.ComparisonWidths
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.wholeDpCentered

/** Column header of [ComparisonTable]: the [badge] sits above the [name]; without [onClick] the header is not a button. */
data class ComparisonColumn(
    val name: String,
    val badge: String? = null,
    val onClick: (() -> Unit)? = null,
    val tag: String? = null,
)

/** One table row; [strong] values are drawn at 500, [highlighted] rows are tinted. */
data class ComparisonRow(
    val label: String,
    val a: String,
    val b: String,
    val strong: Boolean,
    val highlighted: Boolean,
)

/**
 * Comparison table (AndVergleich:22): `surfaceContainer`, radius 16, clipped. The header (padding 12 16 10, gap 10,
 * bottom-aligned) holds an empty label cell and the two [columns]: [ComparisonColumn.badge] as 12 / 500 `okFg`
 * above the 14 / 500 name (line height 18, gap 4). Each row has a 1 `divider` line above it, a [rowMinHeight]
 * minimum, padding 0 16, a 13 `muted` label and 14 values at 500 ([ComparisonRow.strong]) or 400, on [highlight]
 * when [ComparisonRow.highlighted]. All widths come from [widths].
 */
@Composable
fun ComparisonTable(
    columns: List<ComparisonColumn>,
    rows: List<ComparisonRow>,
    widths: ComparisonWidths,
    contentDescription: String,
    modifier: Modifier = Modifier,
    rowMinHeight: Dp = 44.dp,
    highlight: ColorToken = Tokens.diff,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Tokens.Radius.card))
            .background(Tokens.surfaceContainer.color())
            .semantics { this.contentDescription = contentDescription },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(Modifier.width(widths.label.dp))
            columns.forEach { HeaderCell(it, Modifier.width(widths.value.dp)) }
        }
        rows.forEach { TableRow(it, widths, rowMinHeight, highlight) }
    }
}

@Composable
private fun HeaderCell(column: ComparisonColumn, modifier: Modifier) {
    val tagged = if (column.tag != null) modifier.testTag(column.tag) else modifier
    val frame = if (column.onClick != null) {
        tagged.clickable(role = Role.Button, onClick = column.onClick)
    } else {
        tagged
    }
    Column(frame, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (column.badge != null) {
            BaupilotText(text = column.badge, size = 12.sp, weight = 500, color = Tokens.okFg)
        }
        BaupilotText(text = column.name, size = 14.sp, weight = 500, color = Tokens.text, lineHeight = 18.sp)
    }
}

@Composable
private fun TableRow(row: ComparisonRow, widths: ComparisonWidths, minHeight: Dp, highlight: ColorToken) {
    val weight = if (row.strong) 500 else 400
    val fill = if (row.highlighted) Modifier.background(highlight.color()) else Modifier
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Tokens.divider.color()))
        Row(
            modifier = Modifier.fillMaxWidth().then(fill).heightIn(min = minHeight).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BaupilotText(
                text = row.label, size = 13.sp, color = Tokens.muted,
                modifier = Modifier.width(widths.label.dp).then(wholeDpCentered()),
            )
            BaupilotText(
                text = row.a, size = 14.sp, weight = weight, color = Tokens.text,
                modifier = Modifier.width(widths.value.dp).then(wholeDpCentered()),
            )
            BaupilotText(
                text = row.b, size = 14.sp, weight = weight, color = Tokens.text,
                modifier = Modifier.width(widths.value.dp).then(wholeDpCentered()),
            )
        }
    }
}
