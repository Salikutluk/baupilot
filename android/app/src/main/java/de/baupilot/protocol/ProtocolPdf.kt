package de.baupilot.protocol

/** Lays a [ProtocolDocument] out on A4 pages and returns the PDF bytes. */
object ProtocolPdf {
    private const val PAGE_H = 842.0
    private const val MARGIN = 56.0
    private const val CONTENT_W = 595.0 - 2 * MARGIN
    private const val BOTTOM = 64.0
    private const val SIGNATURE_W = 240.0
    private const val SIGNATURE_H = 80.0
    private const val NO_SIGNATURE_NOTE = "(keine Unterschrift auf diesem Gerät erfasst)"

    private class Layout {
        val pages = mutableListOf(mutableListOf<PageScope.() -> Unit>())
        var y = PAGE_H - 84.0

        fun ensure(height: Double) {
            if (y - height < BOTTOM) {
                pages += mutableListOf<PageScope.() -> Unit>()
                y = PAGE_H - 84.0
            }
        }

        fun draw(op: PageScope.() -> Unit) {
            pages.last() += op
        }

        fun text(text: String, size: Double, bold: Boolean = false, x: Double = MARGIN, gapAfter: Double = 0.0) {
            for (line in PdfWriter.wrap(text, size, bold, CONTENT_W - (x - MARGIN))) {
                ensure(size * 1.35)
                y -= size * 1.35
                val at = y
                draw { text(x, at, size, line, bold) }
            }
            y -= gapAfter
        }
    }

    fun render(doc: ProtocolDocument, contentSha256: String): ByteArray {
        val layout = Layout()
        with(layout) {
            text(doc.title, 18.0, bold = true, gapAfter = 6.0)
            text("Projekt: ${doc.address}", 11.0)
            text("Gewerk: ${doc.tradeName}", 11.0)
            text("Erstellt: ${doc.createdLocal}", 11.0, gapAfter = 12.0)

            if (doc.stats.isNotEmpty()) {
                doc.stats.forEach { text(it, 11.0) }
                y -= 12.0
            }
            if (doc.defects.isNotEmpty()) {
                text("Mängel", 13.0, bold = true, gapAfter = 2.0)
                doc.defects.forEach { text("– $it", 11.0, x = MARGIN + 8.0) }
                y -= 12.0
            }
            if (!doc.reservation.isNullOrBlank()) {
                text("Vorbehalt", 13.0, bold = true, gapAfter = 2.0)
                text(doc.reservation, 11.0, gapAfter = 12.0)
            }
            doc.signatures.forEach { signature(it) }
        }

        val total = layout.pages.size
        val writer = PdfWriter(title = doc.title, createdAt = doc.createdAt)
        layout.pages.forEachIndexed { i, ops ->
            writer.page {
                text(MARGIN, PAGE_H - 40.0, 8.0, "${doc.title} · ${doc.createdLocal}")
                ops.forEach { it() }
                text(MARGIN, 32.0, 8.0, "Seite ${i + 1}/$total · SHA-256 Inhalt: $contentSha256")
            }
        }
        return writer.bytes(contentSha256)
    }

    private fun Layout.signature(block: SignatureBlock) {
        val strokes = block.strokes
        ensure(if (strokes != null) SIGNATURE_H + 70.0 else 70.0)
        text(block.label, 11.0, bold = true)
        text(block.name, 11.0)
        block.signedAt?.let { text("Unterschrieben: $it", 11.0) }
        if (strokes == null) {
            text(NO_SIGNATURE_NOTE, 9.0, gapAfter = 12.0)
            return
        }
        y -= 6.0
        val top = y
        val bottom = top - SIGNATURE_H
        draw { rect(MARGIN, bottom, SIGNATURE_W, SIGNATURE_H, 0.5) }
        if (strokes.w > 0 && strokes.h > 0) {
            val scale = minOf(SIGNATURE_W / strokes.w, SIGNATURE_H / strokes.h)
            val offX = MARGIN + (SIGNATURE_W - strokes.w * scale) / 2
            val offY = top - (SIGNATURE_H - strokes.h * scale) / 2
            for (stroke in strokes.strokes) {
                val points = stroke.chunked(2).filter { it.size == 2 }.map { (x, y) -> (offX + x * scale) to (offY - y * scale) }
                draw { polyline(points, 1.0) }
            }
        }
        y = bottom - 18.0
    }
}
