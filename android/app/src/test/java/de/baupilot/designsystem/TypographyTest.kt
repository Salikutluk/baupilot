package de.baupilot.designsystem

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import java.io.File
import java.nio.ByteBuffer
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

    @Test
    fun lineBoxMatchesChromeRounding() {
        val expected = mapOf(11f to 13f, 12f to 14f, 13f to 15f, 14f to 16f, 16f to 19f, 18f to 21f, 22f to 25f)
        for ((size, box) in expected) {
            assertEquals("lineBox($size)", box, RobotoMetrics.lineBox(size), 0f)
        }
    }

    @Test
    fun baselineOffsetIsRoundedAscent() {
        assertEquals(15f, RobotoMetrics.baselineOffset(16f), 0f)
        assertEquals(14f, RobotoMetrics.baselineOffset(15f), 0f)
    }

    /** Where the descent rounds down, Blink moves one unit from ascent to descent: the box stays, the baseline rises. */
    @Test
    fun baselineRisesWhereDescentRoundsDown() {
        assertEquals(12f, RobotoMetrics.baselineOffset(14f), 0f)
        assertEquals(16f, RobotoMetrics.lineBox(14f), 0f)
        assertEquals(11f, RobotoMetrics.baselineOffset(13f), 0f)
    }

    @Test
    fun robotoHheaMatchesConstants() {
        val root = File(System.getProperty("baupilot.repoRoot"))
        for (name in listOf("regular", "medium", "bold")) {
            val file = File(root, "android/app/src/main/res/font/roboto_$name.ttf")
            val hhea = readHhea(file)
            assertEquals("${file.name} ascender", RobotoMetrics.ascender, hhea.ascender)
            assertEquals("${file.name} descender", RobotoMetrics.descender, Math.abs(hhea.descender))
            assertEquals("${file.name} unitsPerEm", RobotoMetrics.unitsPerEm, hhea.unitsPerEm)
        }
    }

    private class Hhea(val ascender: Int, val descender: Int, val unitsPerEm: Int)

    private fun readHhea(file: File): Hhea {
        val buf = ByteBuffer.wrap(file.readBytes())
        val tables = buf.getShort(4).toInt() and 0xFFFF
        val offsets = mutableMapOf<String, Int>()
        for (i in 0 until tables) {
            val entry = 12 + i * 16
            val tag = String(ByteArray(4) { buf.get(entry + it) }, Charsets.US_ASCII)
            offsets[tag] = buf.getInt(entry + 8)
        }
        val hhea = offsets.getValue("hhea")
        val head = offsets.getValue("head")
        return Hhea(
            ascender = buf.getShort(hhea + 4).toInt(),
            descender = buf.getShort(hhea + 6).toInt(),
            unitsPerEm = buf.getShort(head + 18).toInt() and 0xFFFF,
        )
    }
}
