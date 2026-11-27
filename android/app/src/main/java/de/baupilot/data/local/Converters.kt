package de.baupilot.data.local

import de.baupilot.sync.FieldStamp
import de.baupilot.sync.SyncRecord
import de.baupilot.sync.SyncTable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/** JSON codecs between the Room columns (`fields`, `stamps`) and their typed forms. */
internal object Converters {
    private val stampsSerializer = MapSerializer(String.serializer(), FieldStamp.serializer())

    fun fieldsToString(fields: JsonObject): String = Json.encodeToString(JsonObject.serializer(), fields)

    fun stringToFields(text: String): JsonObject = Json.decodeFromString(JsonObject.serializer(), text)

    fun stampsToString(stamps: Map<String, FieldStamp>): String = Json.encodeToString(stampsSerializer, stamps)

    fun stringToStamps(text: String): Map<String, FieldStamp> = Json.decodeFromString(stampsSerializer, text)

    fun toEntity(record: SyncRecord): RecordEntity = RecordEntity(
        tableId = record.table.id,
        id = record.id,
        projectId = record.projectId,
        sortKey = record.sortKey,
        fields = fieldsToString(record.fields),
        stamps = stampsToString(record.stamps),
        deleted = record.deleted,
    )

    fun toRecord(entity: RecordEntity): SyncRecord = SyncRecord(
        table = SyncTable.entries.first { it.id == entity.tableId },
        id = entity.id,
        projectId = entity.projectId,
        sortKey = entity.sortKey,
        fields = stringToFields(entity.fields),
        stamps = stringToStamps(entity.stamps),
        deleted = entity.deleted,
    )
}
