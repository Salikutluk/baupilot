package de.baupilot.data.local

import androidx.room.Entity
import androidx.room.Index

/**
 * One syncable row. `fields` is the fixture DTO's own JSON object (plus sync-only extra fields),
 * `stamps` the per-field last-write-wins timestamps as a JSON object.
 */
@Entity(
    tableName = "records",
    primaryKeys = ["tableId", "id"],
    indices = [Index("tableId", "projectId", "sortKey")],
)
data class RecordEntity(
    val tableId: String,
    val id: String,
    val projectId: String,
    val sortKey: Long,
    val fields: String,
    val stamps: String,
    val deleted: Boolean,
)
