package de.baupilot.protocol

import java.security.MessageDigest
import java.util.Locale
import kotlinx.serialization.json.Json

object ProtocolHash {
    private val json = Json { encodeDefaults = true }

    /** Canonical JSON of [doc]: sorted keys, no whitespace. */
    fun canonicalJson(doc: ProtocolDocument): String =
        CanonicalJson.encode(json.encodeToJsonElement(ProtocolDocument.serializer(), doc))

    /** Lower-case hex SHA-256 of the UTF-8 canonical JSON of [doc]. */
    fun contentSha256(doc: ProtocolDocument): String = sha256Hex(canonicalJson(doc).toByteArray(Charsets.UTF_8))

    fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { String.format(Locale.ROOT, "%02x", it) }
}
