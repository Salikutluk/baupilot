package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken

/** Fill, text colour and optional 2 dp inside border of a [SocketTile]. */
class SocketTileStyle(
    val background: ColorToken,
    val foreground: ColorToken,
    val border: ColorToken? = null,
)

/** One socket of a [SocketGrid]. */
class SocketItem(
    val id: String,
    val label: String,
    val style: SocketTileStyle,
    val contentDescription: String,
)

/**
 * One socket of the room check (AndElRaum:49-52): 52 tall, radius 12, id 14 / 500 over a 11 / 400 state label, gap 2.
 * A border (the `now` tile) is drawn inside the box. The tile has no action: it is a button for accessibility only.
 * The width comes from the caller, [SocketGrid] gives it a weight.
 */
@Composable
fun SocketTile(
    id: String,
    label: String,
    style: SocketTileStyle,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    var frame = modifier.fillMaxWidth().height(52.dp).background(style.background.color(), shape)
    if (style.border != null) frame = frame.border(2.dp, style.border.color(), shape)
    Column(
        modifier = frame.clearAndSetSemantics {
            this.contentDescription = contentDescription
            role = Role.Button
        },
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BaupilotText(text = id, size = 14.sp, weight = 500, color = style.foreground)
        BaupilotText(text = label, size = 11.sp, color = style.foreground)
    }
}

/**
 * Tiles in [columns] equal columns with [gap] both ways (`repeat(4, minmax(0, 1fr))`). Built from weighted [Row]s,
 * with no subcomposition: a grid that subcomposes crashes under the `IntrinsicSize.Min` rows of the step rail.
 * A last row with fewer tiles keeps the column width.
 */
@Composable
fun SocketGrid(
    tiles: List<SocketItem>,
    columns: Int = 4,
    gap: Dp = 8.dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        tiles.chunked(columns).forEach { rowTiles ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                rowTiles.forEach { tile ->
                    SocketTile(
                        tile.id, tile.label, tile.style, tile.contentDescription,
                        Modifier.weight(1f),
                    )
                }
                repeat(columns - rowTiles.size) { Spacer(Modifier.weight(1f).height(52.dp)) }
            }
        }
    }
}
