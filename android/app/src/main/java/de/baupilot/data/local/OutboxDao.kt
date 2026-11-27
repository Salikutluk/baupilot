package de.baupilot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class OutboxDao {
    @Insert
    abstract suspend fun insert(entity: OutboxEntity): Long

    @Query(
        "DELETE FROM outbox WHERE kind = 'UPSERT' AND tableId = :table AND rowId = :id " +
            "AND state IN ('PENDING', 'PARKED')",
    )
    protected abstract suspend fun deleteOpenUpsert(table: String, id: String)

    /** Replaces an open (PENDING or PARKED) UPSERT of the same row with a new PENDING one at the tail. */
    @Transaction
    open suspend fun enqueueUpsert(
        table: String,
        id: String,
        projectId: String,
        createdAt: Long = System.currentTimeMillis(),
    ) {
        deleteOpenUpsert(table, id)
        insert(
            OutboxEntity(
                kind = OutboxKind.UPSERT,
                tableId = table,
                rowId = id,
                projectId = projectId,
                payload = "{}",
                state = OutboxState.PENDING,
                attempts = 0,
                lastError = null,
                createdAt = createdAt,
            ),
        )
    }

    /** The lowest-`seq` PENDING op across all projects. */
    @Query("SELECT * FROM outbox WHERE state = 'PENDING' ORDER BY seq LIMIT 1")
    abstract suspend fun head(): OutboxEntity?

    @Query("SELECT * FROM outbox ORDER BY seq")
    abstract suspend fun all(): List<OutboxEntity>

    @Query("DELETE FROM outbox WHERE seq = :seq")
    abstract suspend fun deleteBySeq(seq: Long)

    @Query("UPDATE outbox SET state = 'FAILED', lastError = :error, attempts = attempts + 1 WHERE seq = :seq")
    abstract suspend fun markFailed(seq: Long, error: String)

    @Query("UPDATE outbox SET state = 'PARKED' WHERE seq = :seq")
    abstract suspend fun park(seq: Long)

    @Query("UPDATE outbox SET state = 'PENDING' WHERE state = 'PARKED'")
    abstract suspend fun requeueParked()

    @Query("DELETE FROM outbox WHERE projectId = :projectId")
    abstract suspend fun deleteForProject(projectId: String)
}
