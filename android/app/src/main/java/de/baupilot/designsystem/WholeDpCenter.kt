package de.baupilot.designsystem

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Vertical centring that lands on whole dp. Where CSS centres a text box at k + 0.5 dp, Chrome paints it at k + 1 dp;
 * `Alignment.CenterVertically` rounds the pixel position (3k + 1.5 → 3k + 2) and puts the text one pixel higher.
 * Boxes (rows, icon circles, cards) already match, so only text uses this.
 */
internal class WholeDpCenter(private val density: Float) : Alignment.Vertical {
    override fun align(size: Int, space: Int): Int =
        (floor((space - size) / (2f * density) + 0.5f) * density).roundToInt()
}

/** `Modifier.align` for a text (or a column of texts) in a centred `Row`, in whole dp. */
@Composable
fun RowScope.wholeDpCentered(): Modifier = Modifier.align(WholeDpCenter(LocalDensity.current.density))
