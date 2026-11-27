package de.baupilot.data.local

import androidx.room.withTransaction
import de.baupilot.data.Fixture
import de.baupilot.data.FixtureJson
import de.baupilot.data.RepositoryException
import de.baupilot.sync.FieldStamp
import de.baupilot.sync.SyncRecord
import de.baupilot.sync.SyncTable
import java.security.MessageDigest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/** The bundled fixture, decoded strictly once, with its raw JSON tree for seeding. */
internal class FixtureSource(val text: String) {
    val fixture: Fixture
    val root: JsonObject
    val sha: String = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    init {
        try {
            fixture = FixtureJson.decodeFromString(Fixture.serializer(), text)
            root = Json.parseToJsonElement(text).jsonObject
        } catch (e: SerializationException) {
            throw RepositoryException.Decoding(e.message ?: e.toString())
        } catch (e: IllegalArgumentException) {
            throw RepositoryException.Decoding(e.message ?: e.toString())
        }
    }
}

/** Seeds the D3 records (project, trades, offers, documents, inspection rows, defects, notifications). */
internal object FixtureSeeder {
    const val SHA_KEY = "seededFixtureSha"

    /**
     * Inserts all records when nothing is seeded; for a different fixture sha it replaces only records that
     * were never touched or pulled (all stamps ZERO or absent) and inserts missing ones. Idempotent.
     */
    suspend fun seed(db: BaupilotDatabase, source: FixtureSource) {
        if (db.state().get(SHA_KEY) == source.sha) return
        val records = records(source)
        db.withTransaction {
            if (db.state().get(SHA_KEY) == source.sha) return@withTransaction
            for (record in records) {
                val existing = db.records().get(record.tableId, record.id)
                if (existing == null || isUntouched(existing)) db.records().upsert(record)
            }
            db.state().put(SyncStateEntity(SHA_KEY, source.sha))
        }
    }

    private fun isUntouched(entity: RecordEntity): Boolean = try {
        Converters.stringToStamps(entity.stamps).values.all { it == FieldStamp.ZERO }
    } catch (e: SerializationException) {
        false
    }

    private fun records(source: FixtureSource): List<RecordEntity> {
        val projectId = source.fixture.project.id
        val root = source.root
        val out = ArrayList<RecordEntity>()

        fun add(table: SyncTable, id: String, sortKey: Int, fields: JsonObject) {
            out += Converters.toEntity(
                SyncRecord(table, id, projectId, sortKey.toLong(), fields, emptyMap(), deleted = false),
            )
        }

        fun rows(element: JsonElement?, table: SyncTable, idKey: String) {
            (element as? JsonArray ?: JsonArray(emptyList())).forEachIndexed { index, row ->
                val fields = row.jsonObject
                add(table, (fields.getValue(idKey) as JsonPrimitive).content, index, fields)
            }
        }

        add(SyncTable.Projects, projectId, 0, root.getValue("project").jsonObject)
        rows(root["trades"], SyncTable.Trades, "id")
        rows(root["offers"], SyncTable.Offers, "id")
        rows(root.getValue("documents").jsonObject["items"], SyncTable.Documents, "id")
        rows(
            root.getValue("inspections").jsonObject.getValue("overview").jsonObject["rows"],
            SyncTable.Inspections,
            "tradeId",
        )
        rows(root["defects"], SyncTable.Defects, "id")

        var position = 0
        for (group in root.getValue("notifications").jsonArray) {
            val title = group.jsonObject.getValue("title")
            for (item in group.jsonObject.getValue("items").jsonArray) {
                val fields = JsonObject(item.jsonObject + ("group" to title))
                add(SyncTable.Notifications, (fields.getValue("id") as JsonPrimitive).content, position++, fields)
            }
        }
        return out
    }
}
