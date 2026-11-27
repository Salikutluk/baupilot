package de.baupilot.sync

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object FieldMerge {
    const val DELETED = "deleted"

    fun merge(a: SyncRecord, b: SyncRecord): SyncRecord {
        require(a.table == b.table && a.id == b.id) {
            "Cannot merge ${a.table.id}/${a.id} with ${b.table.id}/${b.id}"
        }
        val keys = linkedSetOf<String>()
        keys += a.fields.keys
        keys += b.fields.keys
        keys += a.stamps.keys
        keys += b.stamps.keys
        keys += DELETED

        val fields = linkedMapOf<String, JsonElement>()
        val stamps = linkedMapOf<String, FieldStamp>()
        var deleted = false
        for (key in keys.sorted()) {
            val winner = pick(
                Candidate(stampOf(a, key), valueOf(a, key)),
                Candidate(stampOf(b, key), valueOf(b, key)),
            )
            if (winner.stamp != FieldStamp.ZERO) stamps[key] = winner.stamp
            if (key == DELETED) {
                deleted = (winner.value as? JsonPrimitive)?.content == "true"
            } else if (winner.value != null) {
                fields[key] = winner.value
            }
        }
        return SyncRecord(
            table = a.table,
            id = a.id,
            projectId = listOf(a.projectId, b.projectId).filter { it.isNotBlank() }.minOrNull() ?: "",
            sortKey = listOf(a.sortKey, b.sortKey).filter { it != 0L }.minOrNull() ?: 0L,
            fields = JsonObject(fields),
            stamps = stamps,
            deleted = deleted,
        )
    }

    fun touch(
        r: SyncRecord,
        changes: Map<String, JsonElement>,
        stamp: FieldStamp,
        deleted: Boolean? = null,
    ): SyncRecord {
        val fields = r.fields.toMutableMap()
        val stamps = r.stamps.toMutableMap()
        for ((key, value) in changes) {
            fields[key] = value
            stamps[key] = stamp
        }
        if (deleted != null) stamps[DELETED] = stamp
        return r.copy(fields = JsonObject(fields), stamps = stamps, deleted = deleted ?: r.deleted)
    }

    private class Candidate(val stamp: FieldStamp, val value: JsonElement?)

    private fun stampOf(r: SyncRecord, key: String): FieldStamp = r.stamps[key] ?: FieldStamp.ZERO

    private fun valueOf(r: SyncRecord, key: String): JsonElement? =
        if (key == DELETED) JsonPrimitive(r.deleted) else r.fields[key]

    /** Greater stamp wins; on an equal stamp the lexicographically greater canonical JSON wins (absent sorts first). */
    private fun pick(x: Candidate, y: Candidate): Candidate {
        val byStamp = x.stamp.compareTo(y.stamp)
        if (byStamp != 0) return if (byStamp > 0) x else y
        return if (canonical(x.value) >= canonical(y.value)) x else y
    }

    private fun canonical(value: JsonElement?): String = when (value) {
        null -> ""
        is JsonObject -> value.entries.sortedBy { it.key }
            .joinToString(",", "{", "}") { (k, v) -> JsonPrimitive(k).toString() + ":" + canonical(v) }
        is JsonArray -> value.joinToString(",", "[", "]") { canonical(it) }
        else -> value.toString()
    }
}
