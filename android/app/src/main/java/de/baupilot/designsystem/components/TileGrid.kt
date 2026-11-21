package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * One tile of a [TileGrid]. A hollow tile has the [label] ("hohl"). [modifier] carries the caller's test tag.
 */
class TileItem(
    val id: String,
    val hollow: Boolean,
    val label: String?,
    val contentDescription: String,
    val modifier: Modifier = Modifier,
)

private val TileShape = RoundedCornerShape(2.dp)

/**
 * Floor tile grid of the room check (AndFlBad:51-53): [columns] equal columns in a `grout` box with radius 8, padding 8
 * and gap 4, tiles 44 tall with radius 2. A hollow tile is `errBg` with a 2 `err` border inside and its label in 10 / 500
 * `errFg`. The tiles have no action and no semantics of their own (M7 D8): the grid is one node tagged `tiles.grid`,
 * described by [accessibilityLabel], whose state is the descriptions of the hollow tiles joined with ", ". Laid out by [TileRows], with no subcomposition (M4 R5).
 */
@Composable
fun TileGrid(
    tiles: List<TileItem>,
    columns: Int,
    modifier: Modifier = Modifier,
    accessibilityLabel: String? = null,
) {
    val hollow = tiles.filter { it.hollow }.joinToString(", ") { it.contentDescription }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tiles.grid")
            .clearAndSetSemantics {
                if (accessibilityLabel != null) contentDescription = accessibilityLabel
                if (hollow.isNotEmpty()) stateDescription = hollow
            }
            .background(Tokens.grout.color(), RoundedCornerShape(8.dp))
            .padding(8.dp),
    ) {
        TileRows(columns) { tiles.forEach { tile -> Tile(tile, Modifier) } }
    }
}

/**
 * The tiles in [columns] equal columns with gap 4 both ways. Chrome snaps every column edge of the CSS grid to a whole
 * dp (`AndFlBad.dc.html:51`: tracks 35.25 dp, edges at round(k x 39.25) and round(k x 39.25 + 35.25), half up), where
 * weighted [Row]s round each width to a pixel. Measured on the reference PNG: tile edges 0 / 39 / 79 / 118 / 157 / 196
 * / 236 / 275 dp from the first one, which is this rounding.
 */
@Composable
private fun TileRows(columns: Int, content: @Composable () -> Unit) {
    Layout(content = content, modifier = Modifier.fillMaxWidth()) { measurables, constraints ->
        require(constraints.hasBoundedWidth) { "TileGrid needs a bounded width" }
        val density = this.density
        val gap = 4f
        val gapPx = gap * density
        val widthDp = constraints.maxWidth / density
        val trackDp = (widthDp - gap * (columns - 1)) / columns
        fun wholeDp(value: Float) = floor(value + 0.5f)
        fun edgePx(dp: Float) = (wholeDp(dp) * density).roundToInt()
        val placeables = measurables.mapIndexed { index, measurable ->
            val column = index % columns
            val left = edgePx(column * (trackDp + gap))
            val right = edgePx(column * (trackDp + gap) + trackDp)
            measurable.measure(Constraints.fixedWidth(right - left)) to left
        }
        val rows = placeables.chunked(columns)
        val rowHeights = rows.map { row -> row.maxOf { it.first.height } }
        var y = 0
        val tops = rowHeights.map { height -> y.also { y += height + gapPx.roundToInt() } }
        val totalHeight = if (rowHeights.isEmpty()) 0 else y - gapPx.roundToInt()
        layout(constraints.maxWidth, totalHeight) {
            rows.forEachIndexed { r, row -> row.forEach { (placeable, left) -> placeable.placeRelative(left, tops[r]) } }
        }
    }
}

@Composable
private fun Tile(tile: TileItem, modifier: Modifier) {
    var frame = modifier.then(tile.modifier).heightIn(min = 44.dp)
    frame = if (tile.hollow) {
        frame.background(Tokens.errBg.color(), TileShape).border(2.dp, Tokens.err.color(), TileShape)
    } else {
        frame.background(Tokens.tile.color(), TileShape)
    }
    Box(
        modifier = frame,
        contentAlignment = Alignment.Center,
    ) {
        tile.label?.let { BaupilotText(text = it, size = 10.sp, weight = 500, color = Tokens.errFg) }
    }
}
