package de.baupilot.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Keys: `seededFixtureSha`, `dataVersion`, `maxSeenAt`, `known:<projectId>`,
 * `cursor:<projectId>:<table>` (value `{updatedAt, id}`).
 */
@Entity(tableName = "sync_state")
data class SyncStateEntity(@PrimaryKey val key: String, val value: String)
