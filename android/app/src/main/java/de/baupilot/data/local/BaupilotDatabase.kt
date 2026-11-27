package de.baupilot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecordEntity::class, OutboxEntity::class, SyncStateEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class BaupilotDatabase : RoomDatabase() {
    abstract fun records(): RecordDao
    abstract fun outbox(): OutboxDao
    abstract fun state(): SyncStateDao
}
