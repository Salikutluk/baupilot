package de.baupilot.protocol

import java.io.ByteArrayOutputStream

/** Encodes text as Windows-1252 (PDF `WinAnsiEncoding`). Characters outside it use a documented fallback. */
object WinAnsi {
    private val extra: Map<Int, Int> = mapOf(
        0x20AC to 0x80, 0x201A to 0x82, 0x0192 to 0x83, 0x201E to 0x84, 0x2026 to 0x85, 0x2020 to 0x86,
        0x2021 to 0x87, 0x02C6 to 0x88, 0x2030 to 0x89, 0x0160 to 0x8A, 0x2039 to 0x8B, 0x0152 to 0x8C,
        0x017D to 0x8E, 0x2018 to 0x91, 0x2019 to 0x92, 0x201C to 0x93, 0x201D to 0x94, 0x2022 to 0x95,
        0x2013 to 0x96, 0x2014 to 0x97, 0x02DC to 0x98, 0x2122 to 0x99, 0x0161 to 0x9A, 0x203A to 0x9B,
        0x0153 to 0x9C, 0x017E to 0x9E, 0x0178 to 0x9F,
    )

    private val fallbacks: Map<Int, String> = mapOf(
        0x2265 to ">=", 0x2264 to "<=", 0x0394 to "Delta", 0x2192 to "->",
    )

    /** The WinAnsi byte for [codePoint], or null when it has no byte. */
    fun byteFor(codePoint: Int): Int? = when {
        codePoint in 0x20..0x7E -> codePoint
        codePoint in 0xA0..0xFF -> codePoint
        else -> extra[codePoint]
    }

    fun encode(text: String): ByteArray {
        val out = ByteArrayOutputStream()
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            i += Character.charCount(cp)
            val b = byteFor(cp)
            if (b != null) {
                out.write(b)
            } else {
                val fb = fallbacks[cp] ?: "?"
                fb.forEach { out.write(it.code) }
            }
        }
        return out.toByteArray()
    }
}
