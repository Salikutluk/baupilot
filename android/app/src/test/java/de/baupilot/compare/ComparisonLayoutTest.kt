package de.baupilot.compare

import org.junit.Assert.assertEquals
import org.junit.Test

class ComparisonLayoutTest {
    private val delta = 0.0005f

    @Test
    fun columnsAndroid() {
        val widths = columns(cardWidth = 358f, padding = 16f, gap = 10f, labelColumn = 104f)

        assertEquals(104f, widths.label, delta)
        assertEquals(101f, widths.value, delta)
    }

    @Test
    fun columnsAreNonNegative() {
        val widths = columns(cardWidth = 100f, padding = 16f, gap = 10f, labelColumn = 104f)

        assertEquals(104f, widths.label, delta)
        assertEquals(0f, widths.value, delta)
    }
}
