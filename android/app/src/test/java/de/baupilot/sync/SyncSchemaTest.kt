package de.baupilot.sync

import de.baupilot.data.Defect
import de.baupilot.data.DocumentItem
import de.baupilot.data.MemberRecord
import de.baupilot.data.NotificationItem
import de.baupilot.data.Offer
import de.baupilot.data.OverviewRow
import de.baupilot.data.PhotoRecord
import de.baupilot.data.Project
import de.baupilot.data.ProtocolRecord
import de.baupilot.data.SignatureRecord
import de.baupilot.data.StepRecord
import de.baupilot.data.Trade
import java.io.File
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncSchemaTest {
    private val dtoSerializers: Map<SyncTable, KSerializer<*>> = mapOf(
        SyncTable.Projects to Project.serializer(),
        SyncTable.Members to MemberRecord.serializer(),
        SyncTable.Trades to Trade.serializer(),
        SyncTable.Offers to Offer.serializer(),
        SyncTable.Documents to DocumentItem.serializer(),
        SyncTable.Inspections to OverviewRow.serializer(),
        SyncTable.Steps to StepRecord.serializer(),
        SyncTable.Defects to Defect.serializer(),
        SyncTable.Signatures to SignatureRecord.serializer(),
        SyncTable.Notifications to NotificationItem.serializer(),
        SyncTable.Photos to PhotoRecord.serializer(),
        SyncTable.Protocols to ProtocolRecord.serializer(),
    )

    @Test
    fun schemaCoversEveryTable() {
        assertEquals(SyncTable.entries.toSet(), SyncSchema.tables.keys)
        assertEquals(SyncTable.entries.toSet(), dtoSerializers.keys)
    }

    @Test
    fun fixtureFieldsMatchSerialNames() {
        for (table in SyncTable.entries) {
            val descriptor = dtoSerializers.getValue(table).descriptor
            val serialNames = (0 until descriptor.elementsCount).map(descriptor::getElementName)
            assertEquals(table.id, serialNames, SyncSchema.tables.getValue(table).fixtureFields.keys.toList())
        }
    }

    @Test
    fun extraFieldsDisjointFromFixtureFields() {
        for ((table, spec) in SyncSchema.tables) {
            assertEquals(table.id, emptySet<String>(), spec.fixtureFields.keys intersect spec.extraFields.keys)
            val common = SyncSchema.commonColumns.keys
            assertEquals(table.id, emptySet<String>(), (spec.fixtureFields.keys + spec.extraFields.keys) intersect common)
        }
    }

    @Test
    fun configDeclaresEverySchemaColumn() {
        val file = File(System.getProperty("baupilot.repoRoot"), "backend/appwrite/appwrite.config.json")
        val config = Json.parseToJsonElement(file.readText()).jsonObject
        val tables = config.getValue("tables").jsonArray.map { it.jsonObject }.associateBy { it.getValue("\$id").jsonPrimitive.content }
        for ((table, spec) in SyncSchema.tables) {
            val declared = tables[table.id]
            assertNotNull("table ${table.id} missing in appwrite.config.json", declared)
            assertEquals(SyncSchema.DATABASE_ID, declared!!.getValue("databaseId").jsonPrimitive.content)
            val columns = (declared.getValue("columns") as JsonArray).map { it.jsonObject }.associateBy { it.getValue("key").jsonPrimitive.content }
            val expected = SyncSchema.commonColumns + spec.fixtureFields + spec.extraFields
            for ((key, kind) in expected) {
                val column = columns[key]
                assertNotNull("column ${table.id}.$key missing", column)
                assertCompatible("${table.id}.$key", kind, column!!)
            }
        }
    }

    private fun assertCompatible(label: String, kind: ColumnKind, column: JsonObject) {
        val type = column.getValue("type").jsonPrimitive.content
        val size = column["size"]?.jsonPrimitive?.int ?: 0
        when (kind) {
            is ColumnKind.Text -> {
                assertEquals("$label type", "varchar", type)
                assertTrue("$label size $size < ${kind.size}", size >= kind.size)
            }
            is ColumnKind.Json -> {
                assertEquals("$label type", "varchar", type)
                assertTrue("$label size $size < ${kind.size}", size >= kind.size)
            }
            ColumnKind.Long_ -> assertEquals("$label type", "integer", type)
            ColumnKind.Double_ -> assertEquals("$label type", "double", type)
            ColumnKind.Bool -> assertEquals("$label type", "boolean", type)
            is ColumnKind.LongText -> assertEquals("$label type", "longtext", type)
        }
    }
}
