package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens

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
 * `errFg`. The tiles have no action: each is a button for accessibility only. The grid is one group described by
 * [accessibilityLabel]. Built from weighted [Row]s, with no subcomposition (M4 R5).
 */
@Composable
fun TileGrid(
    tiles: List<TileItem>,
    columns: Int,
    modifier: Modifier = Modifier,
    accessibilityLabel: String? = null,
) {
    var frame = modifier.fillMaxWidth()
    if (accessibilityLabel != null) frame = frame.semantics { contentDescription = accessibilityLabel }
    Column(
        modifier = frame
            .background(Tokens.grout.color(), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tiles.chunked(columns).forEach { rowTiles ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rowTiles.forEach { tile -> Tile(tile, Modifier.weight(1f)) }
                repeat(columns - rowTiles.size) { Spacer(Modifier.weight(1f).height(44.dp)) }
            }
        }
    }
}

@Composable
private fun Tile(tile: TileItem, modifier: Modifier) {
    var frame = modifier.then(tile.modifier).height(44.dp)
    frame = if (tile.hollow) {
        frame.background(Tokens.errBg.color(), TileShape).border(2.dp, Tokens.err.color(), TileShape)
    } else {
        frame.background(Tokens.tile.color(), TileShape)
    }
    Box(
        modifier = frame.clearAndSetSemantics {
            contentDescription = tile.contentDescription
            role = Role.Button
        },
        contentAlignment = Alignment.Center,
    ) {
        tile.label?.let { BaupilotText(text = it, size = 10.sp, weight = 500, color = Tokens.errFg) }
    }
}
