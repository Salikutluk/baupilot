package de.baupilot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncStateDao {
    @Query("SELECT value FROM sync_state WHERE key = :key")
    suspend fun get(key: String): String?

    @Query("SELECT value FROM sync_state WHERE key = :key")
    fun observe(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: SyncStateEntity)

    @Query("DELETE FROM sync_state WHERE key = :key")
    suspend fun delete(key: String)
}
