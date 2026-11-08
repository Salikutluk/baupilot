package de.baupilot.designsystem

import de.baupilot.designsystem.components.WholeDpGrid
import org.junit.Assert.assertEquals
import org.junit.Test

class WholeDpCenterTest {
    /** 56 dp row, 19 dp text: CSS centres at 18.5 dp and Chrome paints it at 19 dp (57 px); CenterVertically gives 56 px. */
    @Test
    fun halfDpCentreRoundsUpToWholeDp() {
        assertEquals(57, WholeDpCenter(3f).align(size = 19 * 3, space = 56 * 3))
    }

    @Test
    fun wholeDpCentreIsUnchanged() {
        assertEquals(16 * 3, WholeDpCenter(3f).align(size = 24 * 3, space = 56 * 3))
    }

    /** Page-top offsets of the preview table rules in AndDokViewer's reference: round(86.55 + 18.45 k). */
    @Test
    fun previewGridPutsTheRulesOnChromesWholeDp() {
        val grid = WholeDpGrid()
        var top = 0f
        fun block(height: Float): Float {
            val start = top
            top += grid.next(height).value
            return start
        }
        block(18f)
        block(10 * 1.35f + 1f + 7 * 1.35f)
        block(9f)
        block(4f + 9 * 1.35f + 1f + 7 * 1.35f)
        block(9f)
        val rules = mutableListOf(block(1f))
        repeat(5) {
            block(7 * 1.35f + 8f)
            rules += block(1f)
        }
        assertEquals(listOf(87f, 105f, 123f, 142f, 160f, 179f), rules)
    }
}
