package de.baupilot.protocol

import java.security.MessageDigest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolPdfTest {
    private val owner = SignatureBlock(
        role = "owner", name = "Familie Berger", label = "Bauherr", signedAt = "26.11.2026 10:15",
        strokes = StrokesDto(100.0, 40.0, listOf(listOf(0.0, 0.0, 50.0, 20.0, 100.0, 40.0), listOf(10.0, 10.0))),
    )
    private val craftsman = SignatureBlock("craftsman", "Max Mustermann", "Handwerker", null, null)

    private fun doc(defectCount: Int = 2, title: String = "Abnahmeprotokoll Elektro") = ProtocolDocument(
        protocolId = "p1", projectId = "pr1", address = "Hauptstr. 1 (Haus)", tradeId = "t1", tradeName = "Elektro",
        title = title, stats = listOf("12 Punkte geprüft", "≥ 3 Mängel · Δ 2"),
        defects = (1..defectCount).map { "Mangel $it: Steckdose im Flur sitzt schief und ist nicht bündig" },
        reservation = "Vorbehalt: 500 € bis zur Nachbesserung", signatures = listOf(owner, craftsman),
        createdAt = "2026-11-26T09:30:00Z", createdLocal = "26.11.2026 10:30",
    )

    private fun render(d: ProtocolDocument = doc()): ByteArray = ProtocolPdf.render(d, ProtocolHash.contentSha256(d))
    private fun latin1(b: ByteArray) = String(b, Charsets.ISO_8859_1)

    @Test
    fun canonicalJsonSortsKeysAndEscapes() {
        val element = JsonObject(
            mapOf(
                "b" to JsonPrimitive("a\"b\\c\nd\u0001é"),
                "a" to JsonArray(listOf(JsonPrimitive(1), JsonNull, JsonPrimitive(true), JsonPrimitive(1.5))),
                "c" to JsonObject(mapOf("z" to JsonPrimitive(0), "y" to JsonPrimitive("x"))),
            ),
        )
        assertEquals(
            "{\"a\":[1,null,true,1.5],\"b\":\"a\\\"b\\\\c\\nd\\u0001é\",\"c\":{\"y\":\"x\",\"z\":0}}",
            CanonicalJson.encode(element),
        )
    }

    @Test
    fun contentHashMatchesIndependentDigest() {
        val d = doc()
        val json = ProtocolHash.canonicalJson(d)
        assertTrue(json.startsWith("{\"address\":"))
        assertFalse(json.contains(" \"") || json.contains("\n"))
        val expected = MessageDigest.getInstance("SHA-256").digest(json.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        assertEquals(expected, ProtocolHash.contentSha256(d))
        assertEquals(64, expected.length)
    }

    @Test
    fun winAnsiMapsGermanAndFallbacks() {
        val expected = mapOf('ä' to 0xE4, 'ö' to 0xF6, 'ü' to 0xFC, 'ß' to 0xDF, '€' to 0x80, '–' to 0x96, '·' to 0xB7, '„' to 0x84, '“' to 0x93)
        for ((c, b) in expected) assertArrayEquals("$c", byteArrayOf(b.toByte()), WinAnsi.encode(c.toString()))
        assertEquals(">=", String(WinAnsi.encode("≥"), Charsets.ISO_8859_1))
        assertEquals("<=", String(WinAnsi.encode("≤"), Charsets.ISO_8859_1))
        assertEquals("Delta", String(WinAnsi.encode("Δ"), Charsets.ISO_8859_1))
        assertEquals("->", String(WinAnsi.encode("→"), Charsets.ISO_8859_1))
        assertEquals("?", String(WinAnsi.encode("字"), Charsets.ISO_8859_1))
        assertEquals("?", String(WinAnsi.encode("😀"), Charsets.ISO_8859_1))
    }

    @Test
    fun pdfHasValidXrefAndTrailer() {
        val bytes = render()
        val text = latin1(bytes)
        assertTrue(text.startsWith("%PDF-1.4"))
        assertTrue(text.endsWith("%%EOF\n"))
        val startxref = Regex("startxref\n(\\d+)\n%%EOF\n$").find(text)!!.groupValues[1].toInt()
        assertTrue(text.substring(startxref).startsWith("xref\n"))
        val lines = text.substring(startxref).split('\n')
        val count = lines[1].split(' ')[1].toInt()
        assertEquals("0000000000 65535 f ", lines[2])
        for (n in 1 until count) {
            val entry = lines[2 + n]
            assertEquals(19, entry.length)
            assertTrue(entry.endsWith(" 00000 n "))
            val offset = entry.substring(0, 10).toInt()
            assertTrue("object $n", text.substring(offset).startsWith("$n 0 obj\n"))
        }
        assertTrue(text.contains("/Size $count"))
        assertTrue(text.contains("/CreationDate (D:20261126093000Z)"))
    }

    @Test
    fun pdfContainsTitleTimestampAndHashOnEveryPage() {
        val d = doc(defectCount = 60)
        val hash = ProtocolHash.contentSha256(d)
        val text = latin1(ProtocolPdf.render(d, hash))
        val pageCount = Regex("/Type /Page /").findAll(text).count()
        assertTrue(pageCount >= 2)
        assertTrue(text.contains("/Count $pageCount"))
        val streams = Regex("stream\n(.*?)endstream", RegexOption.DOT_MATCHES_ALL).findAll(text).map { it.groupValues[1] }.toList()
        assertEquals(pageCount, streams.size)
        streams.forEachIndexed { i, s ->
            assertTrue(s.contains("(Abnahmeprotokoll Elektro · 26.11.2026 10:30)"))
            assertTrue(s.contains("(Seite ${i + 1}/$pageCount · SHA-256 Inhalt: $hash)"))
        }
    }

    @Test
    fun ownerSignatureDrawnAsPaths() {
        val text = latin1(render())
        assertTrue(Regex("\\d+(\\.\\d+)? \\d+(\\.\\d+)? m\n").containsMatchIn(text))
        assertTrue(Regex("\\d+(\\.\\d+)? \\d+(\\.\\d+)? l\n").containsMatchIn(text))
        assertTrue(text.contains("240 80 re S"))
        assertTrue(text.contains("(Unterschrieben: 26.11.2026 10:15)"))
    }

    @Test
    fun craftsmanTextOnly() {
        val d = doc().copy(signatures = listOf(craftsman))
        val text = latin1(render(d))
        assertTrue(text.contains("(Handwerker)"))
        assertTrue(text.contains("(Max Mustermann)"))
        assertTrue(text.contains("(\\(keine Unterschrift auf diesem Gerät erfasst\\))"))
        assertFalse(Regex(" m\n").containsMatchIn(text))
        assertFalse(Regex(" l\n").containsMatchIn(text))
        assertFalse(text.contains(" re S"))
    }

    @Test
    fun bytesAreDeterministic() {
        assertArrayEquals(render(), render())
        assertFalse(latin1(render()).contains("2026-11-26T"))
        assertFalse(render().contentEquals(render(doc(title = "Anderer Titel"))))
    }

    @Test
    fun numbersUseRootLocaleAndTwoDecimals() {
        assertEquals("12.35", PdfWriter.num(12.3456))
        assertEquals("0", PdfWriter.num(-0.001))
        assertEquals("5", PdfWriter.num(5.0))
        assertEquals("0.5", PdfWriter.num(0.5))
    }

    @Test
    fun wrapBreaksGreedilyWithinWidth() {
        val lines = PdfWriter.wrap("Dies ist ein längerer Satz der umbrochen werden muss", 11.0, false, 100.0)
        assertTrue(lines.size > 1)
        lines.forEach { assertTrue(PdfWriter.textWidth(it, 11.0, false) <= 100.0) }
        assertEquals("Dies ist ein längerer Satz der umbrochen werden muss", lines.joinToString(" "))
    }
}
