package de.baupilot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordDao {
    @Query("SELECT * FROM records WHERE tableId = :table AND id = :id")
    suspend fun get(table: String, id: String): RecordEntity?

    @Query("SELECT * FROM records WHERE tableId = :table AND projectId = :projectId ORDER BY sortKey, id")
    suspend fun list(table: String, projectId: String): List<RecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecordEntity)

    @Query("DELETE FROM records WHERE tableId = :table AND id = :id")
    suspend fun delete(table: String, id: String)

    @Query("DELETE FROM records WHERE projectId = :projectId")
    suspend fun deleteProject(projectId: String)

    /** Projects that have a non-deleted `projects` record. */
    @Query("SELECT id FROM records WHERE tableId = 'projects' AND deleted = 0 ORDER BY sortKey, id")
    suspend fun projectIds(): List<String>
}
