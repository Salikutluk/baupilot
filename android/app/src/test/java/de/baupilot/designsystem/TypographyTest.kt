package de.baupilot.designsystem

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TypographyTest {
    @Test
    fun unsupportedWeightIsRejected() {
        val error = assertThrows(IllegalArgumentException::class.java) { fontWeightFor(300) }

        assertEquals("Gewicht 300 nicht eingebettet", error.message)
    }

    @Test
    fun weight600MapsToBold() {
        assertEquals(FontWeight.Bold, fontWeightFor(600))
        assertEquals(FontWeight.Bold, fontWeightFor(700))
        assertEquals(FontWeight.Normal, fontWeightFor(400))
        assertEquals(FontWeight.Medium, fontWeightFor(500))
    }
}
