package de.baupilot.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens

/**
 * Ask row (AndZeitplan:27): min height 52, gap 12, 10 `primary` diamond, a 16 title over a 13 subtitle (gap 1)
 * and an 18 chevron, all `onPrimaryContainer`. A null [onClick] draws the row and does nothing.
 */
@Composable
fun AskRow(title: String, subtitle: String, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    val press = if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp).then(press),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Diamond(10.dp, Tokens.primary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            BaupilotText(
                text = title,
                size = 16.sp,
                color = Tokens.onPrimaryContainer,
            )
            BaupilotText(
                text = subtitle,
                size = 13.sp,
                color = Tokens.onPrimaryContainer,
            )
        }
        Icon(BaupilotIcon.ChevronRightW22, 18.dp, Tokens.onPrimaryContainer.color())
    }
}

/**
 * Square of side [size] turned 45 degrees about its centre; the layout box stays [size] square. A [halo] adds a
 * 2 dp ring of that colour around the square (CSS `outline: 2px solid`).
 */
@Composable
fun Diamond(size: Dp, color: ColorToken, halo: ColorToken? = null, modifier: Modifier = Modifier) {
    val fill = color.color()
    val ring = halo?.color()
    Canvas(modifier.size(size)) {
        val side = this.size.width
        val ringWidth = 2.dp.toPx()
        rotate(45f) {
            if (ring != null) {
                drawRect(ring, Offset(-ringWidth, -ringWidth), Size(side + 2 * ringWidth, side + 2 * ringWidth))
            }
            drawRect(fill, Offset.Zero, Size(side, side))
        }
    }
}
