package de.baupilot.protocol

import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.text.Normalizer
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Standard Helvetica advance widths (1000 units per em) for printable ASCII 0x20..0x7E. */
private object HelveticaWidths {
    private val regular = intArrayOf(
        278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
        1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
        333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
        556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584,
    )
    private val bold = intArrayOf(
        278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611,
        975, 722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 333, 278, 333, 584, 556,
        333, 556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611,
        611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500, 389, 280, 389, 584,
    )

    /** Widths of non-ASCII WinAnsi characters as (regular, bold); accented letters follow their base letter. */
    private val special: Map<Int, Pair<Int, Int>> = mapOf(
        0xDF to (611 to 611), 0x20AC to (556 to 556), 0x2013 to (556 to 556), 0x2014 to (1000 to 1000),
        0xB7 to (278 to 278), 0x201E to (333 to 500), 0x201C to (333 to 500),
        0x201D to (333 to 500), 0x2018 to (222 to 278), 0x2019 to (222 to 278), 0x2022 to (350 to 350),
        0x2026 to (1000 to 1000), 0xC6 to (1000 to 1000), 0xE6 to (889 to 889), 0xD8 to (778 to 778),
        0xF8 to (611 to 611), 0xD7 to (584 to 584), 0xF7 to (584 to 584), 0xA0 to (278 to 278),
    )

    fun width(codePoint: Int, bold: Boolean): Int {
        if (codePoint in 0x20..0x7E) return (if (bold) this.bold else regular)[codePoint - 0x20]
        special[codePoint]?.let { return if (bold) it.second else it.first }
        val base = Normalizer.normalize(String(Character.toChars(codePoint)), Normalizer.Form.NFD).first().code
        if (base in 0x20..0x7E) return width(base, bold)
        return if (bold) 611 else 556
    }
}

class PageScope internal constructor(private val content: ByteArrayOutputStream) {
    private fun put(s: String) = content.write(s.toByteArray(Charsets.ISO_8859_1))

    /** Draws [text] with its baseline starting at ([x], [y]), in points from the bottom left. */
    fun text(x: Double, y: Double, size: Double, text: String, bold: Boolean = false) {
        put("BT /${if (bold) "F2" else "F1"} ${PdfWriter.num(size)} Tf ${PdfWriter.num(x)} ${PdfWriter.num(y)} Td (")
        content.write(escape(WinAnsi.encode(text)))
        put(") Tj ET\n")
    }

    fun rect(x: Double, y: Double, w: Double, h: Double, lineWidth: Double = 0.5) {
        put("${PdfWriter.num(lineWidth)} w ${PdfWriter.num(x)} ${PdfWriter.num(y)} ${PdfWriter.num(w)} ${PdfWriter.num(h)} re S\n")
    }

    /** Strokes the open path through [points] (`m`, then `l` for each further point). */
    fun polyline(points: List<Pair<Double, Double>>, lineWidth: Double = 1.0) {
        if (points.isEmpty()) return
        val pts = if (points.size == 1) listOf(points[0], points[0]) else points
        put("1 J 1 j ${PdfWriter.num(lineWidth)} w\n")
        put("${PdfWriter.num(pts[0].first)} ${PdfWriter.num(pts[0].second)} m\n")
        for (p in pts.drop(1)) put("${PdfWriter.num(p.first)} ${PdfWriter.num(p.second)} l\n")
        put("S\n")
    }

    private fun escape(bytes: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        for (b in bytes) {
            if (b == '('.code.toByte() || b == ')'.code.toByte() || b == '\\'.code.toByte()) out.write('\\'.code)
            out.write(b.toInt())
        }
        return out.toByteArray()
    }
}

/** Minimal deterministic PDF 1.4 writer: A4, Helvetica (`F1`) and Helvetica-Bold (`F2`), uncompressed streams. */
class PdfWriter(private val title: String = "", private val createdAt: String? = null) {
    private val pages = mutableListOf<ByteArray>()

    fun page(build: PageScope.() -> Unit) {
        val content = ByteArrayOutputStream()
        PageScope(content).build()
        pages += content.toByteArray()
    }

    /** The whole file. [id] seeds the trailer `/ID`, so equal input gives equal bytes. */
    fun bytes(id: String): ByteArray {
        val out = ByteArrayOutputStream()
        val offsets = mutableListOf<Int>()
        fun ascii(s: String) = out.write(s.toByteArray(Charsets.ISO_8859_1))
        fun obj(n: Int, body: () -> Unit) {
            offsets += out.size()
            ascii("$n 0 obj\n")
            body()
            ascii("endobj\n")
        }

        ascii("%PDF-1.4\n")
        out.write(byteArrayOf('%'.code.toByte(), 0xE2.toByte(), 0xE3.toByte(), 0xCF.toByte(), 0xD3.toByte(), '\n'.code.toByte()))
        val firstPage = 6
        obj(1) { ascii("<< /Type /Catalog /Pages 2 0 R >>\n") }
        obj(2) {
            val kids = pages.indices.joinToString(" ") { "${firstPage + it * 2} 0 R" }
            ascii("<< /Type /Pages /Kids [$kids] /Count ${pages.size} >>\n")
        }
        obj(3) { ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\n") }
        obj(4) { ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\n") }
        obj(5) {
            val date = createdAt?.let { pdfDate(it) }
            ascii("<< /Title ${hexString(title)}")
            if (date != null) ascii(" /CreationDate ($date)")
            ascii(" /Producer (Baupilot) >>\n")
        }
        pages.forEachIndexed { i, content ->
            val pageNo = firstPage + i * 2
            obj(pageNo) {
                ascii(
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] " +
                        "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents ${pageNo + 1} 0 R >>\n",
                )
            }
            obj(pageNo + 1) {
                ascii("<< /Length ${content.size} >>\nstream\n")
                out.write(content)
                ascii("endstream\n")
            }
        }

        val xref = out.size()
        ascii("xref\n0 ${offsets.size + 1}\n")
        ascii("0000000000 65535 f \n")
        offsets.forEach { ascii(String.format(Locale.ROOT, "%010d 00000 n \n", it)) }
        val digest = MessageDigest.getInstance("SHA-256").digest(id.toByteArray(Charsets.UTF_8))
        val idHex = digest.take(16).joinToString("") { String.format(Locale.ROOT, "%02x", it) }
        ascii("trailer\n<< /Size ${offsets.size + 1} /Root 1 0 R /Info 5 0 R /ID [<$idHex> <$idHex>] >>\n")
        ascii("startxref\n$xref\n%%EOF\n")
        return out.toByteArray()
    }

    private fun pdfDate(iso: String): String? = try {
        "D:" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ROOT).withZone(ZoneOffset.UTC).format(Instant.parse(iso)) + "Z"
    } catch (e: java.time.format.DateTimeParseException) {
        null
    }

    /** UTF-16BE hex string with BOM, the PDF text-string form for non-ASCII. */
    private fun hexString(s: String): String =
        "<FEFF" + s.toByteArray(Charsets.UTF_16BE).joinToString("") { String.format(Locale.ROOT, "%02X", it) } + ">"

    companion object {
        /** At most 2 decimals, no trailing zeros, always `.` as separator. */
        fun num(d: Double): String {
            var s = String.format(Locale.ROOT, "%.2f", d)
            if (s.contains('.')) s = s.trimEnd('0').trimEnd('.')
            return if (s == "-0") "0" else s
        }

        /** Width of [text] in points at [size]. */
        fun textWidth(text: String, size: Double, bold: Boolean): Double {
            var units = 0
            var i = 0
            while (i < text.length) {
                val cp = text.codePointAt(i)
                i += Character.charCount(cp)
                val encoded = WinAnsi.byteFor(cp)
                units += if (encoded != null) HelveticaWidths.width(cp, bold) else WinAnsi.encode(String(Character.toChars(cp))).sumOf { HelveticaWidths.width(it.toInt() and 0xFF, bold) }
            }
            return units * size / 1000.0
        }

        /** Greedy line breaking of [text] into lines no wider than [maxWidth]; honours `\n`. */
        fun wrap(text: String, size: Double, bold: Boolean, maxWidth: Double): List<String> {
            val lines = mutableListOf<String>()
            for (paragraph in text.split('\n')) {
                var line = ""
                for (word in paragraph.split(' ').filter { it.isNotEmpty() }) {
                    val candidate = if (line.isEmpty()) word else "$line $word"
                    if (line.isEmpty() || textWidth(candidate, size, bold) <= maxWidth) line = candidate
                    else {
                        lines += line
                        line = word
                    }
                }
                lines += line
            }
            return lines
        }
    }
}
