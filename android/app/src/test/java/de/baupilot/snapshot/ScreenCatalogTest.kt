package de.baupilot.snapshot

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ScreenCatalogTest {
    private val repoRoot = File(requireNotNull(System.getProperty("baupilot.repoRoot")) { "System property baupilot.repoRoot fehlt" })

    /** Screen names in `design/screens.md` table order, taken from the reference column. */
    private fun screensMdOrder(): List<String> =
        File(repoRoot, "design/screens.md").readLines().mapNotNull { line ->
            Regex("reference/\\{ios,android}/(\\w+)_\\{light,dark}\\.png").find(line)?.groupValues?.get(1)
        }

    @Test
    fun has34NamesMatchingReferences() {
        val stems = File(repoRoot, "design/reference/android").listFiles().orEmpty()
            .map { it.name }
            .filter { it.endsWith("_light.png") }
            .map { it.removeSuffix("_light.png") }
            .toSet()
        val expected = screensMdOrder()
        assertEquals(34, expected.size)
        assertEquals(stems, expected.toSet())
        assertEquals(expected, ScreenCatalog.all.map { it.first })
    }

    @Test
    fun unknownNameThrows() {
        val error = assertThrows(IllegalArgumentException::class.java) { ScreenCatalog.state("GibtEsNicht") }
        assertEquals("unknown screen GibtEsNicht", error.message)
    }
}
