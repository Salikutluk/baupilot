package de.baupilot.sync

import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Maps a [RemoteRow] to the flat column map of a remote table and back. Json and long-text columns hold the
 * value as JSON text, so a string, an object and an array all round-trip. Remote data is untrusted: unknown
 * columns, null values and values of the wrong type are dropped on the way in.
 */
object RowCodec {
    private val stampsSerializer = MapSerializer(String.serializer(), FieldStamp.serializer())

    fun toData(row: RemoteRow, spec: TableSpec): Map<String, Any?> {
        val data = linkedMapOf<String, Any?>(
            "projectId" to row.projectId,
            "sortKey" to row.sortKey,
            "stamps" to Json.encodeToString(stampsSerializer, row.stamps),
            "deleted" to row.deleted,
        )
        for ((name, kind) in columns(spec)) data[name] = encode(row.fields[name], kind)
        return data
    }

    fun fromData(id: String, data: Map<String, Any?>, updatedAt: String?, spec: TableSpec): RemoteRow {
        val fields = linkedMapOf<String, JsonElement>()
        for ((name, kind) in columns(spec)) {
            val value = decode(data[name], kind)
            if (value != null) fields[name] = value
        }
        return RemoteRow(
            id = id,
            projectId = data["projectId"] as? String ?: "",
            sortKey = (data["sortKey"] as? Number)?.toLong() ?: 0L,
            fields = JsonObject(fields),
            stamps = decodeStamps(data["stamps"]),
            deleted = data["deleted"] as? Boolean ?: false,
            updatedAt = updatedAt,
        )
    }

    private fun columns(spec: TableSpec): Map<String, ColumnKind> = spec.fixtureFields + spec.extraFields

    private fun encode(value: JsonElement?, kind: ColumnKind): Any? {
        if (value == null || value is JsonNull) return null
        val primitive = value as? JsonPrimitive
        return when (kind) {
            is ColumnKind.Text -> primitive?.content ?: value.toString()
            ColumnKind.Long_ -> primitive?.longOrNull
            ColumnKind.Double_ -> primitive?.doubleOrNull
            ColumnKind.Bool -> primitive?.booleanOrNull
            is ColumnKind.Json, is ColumnKind.LongText -> value.toString()
        }
    }

    private fun decode(raw: Any?, kind: ColumnKind): JsonElement? = when (kind) {
        is ColumnKind.Text -> (raw as? String)?.let { JsonPrimitive(it) }
        ColumnKind.Long_ -> (raw as? Number)?.takeIf { it.toDouble() == it.toLong().toDouble() }
            ?.let { JsonPrimitive(it.toLong()) }
        ColumnKind.Double_ -> (raw as? Number)?.let { JsonPrimitive(it.toDouble()) }
        ColumnKind.Bool -> (raw as? Boolean)?.let { JsonPrimitive(it) }
        is ColumnKind.Json, is ColumnKind.LongText -> (raw as? String)?.let(::parseJson)
    }

    private fun parseJson(text: String): JsonElement? = try {
        Json.parseToJsonElement(text).takeIf { it !is JsonNull }
    } catch (e: SerializationException) {
        null
    }

    private fun decodeStamps(raw: Any?): Map<String, FieldStamp> {
        val text = raw as? String ?: return emptyMap()
        return try {
            Json.decodeFromString(stampsSerializer, text)
        } catch (e: SerializationException) {
            emptyMap()
        } catch (e: IllegalArgumentException) {
            emptyMap()
        }
    }
}
