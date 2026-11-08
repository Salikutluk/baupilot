package de.baupilot.designsystem

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IconsTest {
    private val drawables = File("src/main/res/drawable")

    @Test
    fun everyIconResourceIsAVectorDrawable() {
        val files = drawables.listFiles { f -> f.name.startsWith("ic_") && f.name.endsWith(".xml") }.orEmpty()
        assertTrue("no ic_*.xml in ${drawables.path}", files.isNotEmpty())
        for (file in files) {
            val xml = file.readText()
            assertTrue("${file.name} is not a <vector>", xml.contains("<vector "))
            assertTrue("${file.name} has no <path>", xml.contains("<path"))
        }
        assertEquals(files.size, BaupilotIcon.entries.size)
    }
}
