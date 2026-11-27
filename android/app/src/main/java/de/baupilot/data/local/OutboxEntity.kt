package de.baupilot.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class OutboxKind { UPSERT, UPLOAD_FILE, DELETE_PROJECT }

/** FAILED = permanent dead letter; PARKED = 403, re-queued on login. */
enum class OutboxState { PENDING, FAILED, PARKED }

@Entity(tableName = "outbox", indices = [Index("kind", "tableId", "rowId")])
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val seq: Long = 0,
    val kind: OutboxKind,
    val tableId: String?,
    val rowId: String,
    val projectId: String,
    val payload: String,
    val state: OutboxState,
    val attempts: Int,
    val lastError: String?,
    val createdAt: Long,
)
