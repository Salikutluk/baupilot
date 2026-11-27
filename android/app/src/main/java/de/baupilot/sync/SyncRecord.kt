package de.baupilot.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class FieldStamp(val at: Long, val by: String) : Comparable<FieldStamp> {
    override fun compareTo(other: FieldStamp): Int {
        val byAt = at.compareTo(other.at)
        return if (byAt != 0) byAt else by.compareTo(other.by)
    }

    companion object {
        val ZERO = FieldStamp(0, "")
    }
}

data class SyncRecord(
    val table: SyncTable,
    val id: String,
    val projectId: String,
    val sortKey: Long,
    val fields: JsonObject,
    val stamps: Map<String, FieldStamp>,
    val deleted: Boolean,
)
