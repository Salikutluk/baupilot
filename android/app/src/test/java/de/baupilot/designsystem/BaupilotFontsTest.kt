package de.baupilot.designsystem

import androidx.compose.ui.text.font.FontListFontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.ResourceFont
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BaupilotFontsTest {
    private val trueTypeMagic = byteArrayOf(0x00, 0x01, 0x00, 0x00)

    @Test
    fun robotoFilesExistAndAreTrueType() {
        for (name in listOf("regular", "medium", "bold")) {
            val file = File("src/main/res/font/roboto_$name.ttf")
            assertTrue("${file.path} missing", file.isFile)
            val head = file.inputStream().use { it.readNBytes(4) }
            assertTrue("${file.path} is not TrueType", head.contentEquals(trueTypeMagic))
        }
    }

    @Test
    fun robotoFamilyHoldsThreeResourceFonts() {
        val family = Roboto as FontListFontFamily
        assertEquals(3, family.fonts.size)
        assertTrue(family.fonts.all { it is ResourceFont })
        assertEquals(
            listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.Bold).map { it.weight },
            family.fonts.map { it.weight.weight },
        )
        assertEquals(listOf(400, 500, 700), family.fonts.map { it.weight.weight })
    }
}
