package de.baupilot

import de.baupilot.backend.AuthGateway
import de.baupilot.backend.BackendConfig
import de.baupilot.backend.PushRegistrar
import de.baupilot.capture.FileStore
import de.baupilot.data.local.BaupilotDatabase
import de.baupilot.data.local.Converters
import de.baupilot.data.local.LocalRepository
import de.baupilot.protocol.ProtocolExporter
import de.baupilot.sync.RemoteGateway
import de.baupilot.sync.SyncEngine
import de.baupilot.sync.SyncScheduler
import de.baupilot.sync.SyncTable
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive

/** Everything the app wires once per process (plan §5.9, §5.10). Tests replace it through `createServices()`. */
class AppServices(
    val config: BackendConfig,
    val clock: Clock,
    val deviceId: String,
    val database: BaupilotDatabase,
    val repository: LocalRepository,
    val auth: AuthGateway,
    val remote: RemoteGateway?,
    val sync: SyncScheduler,
    val engine: SyncEngine,
    val files: FileStore,
    val protocols: ProtocolExporter,
    val push: PushRegistrar,
    val io: CoroutineDispatcher,
    val appScope: CoroutineScope,
) {
    /** Names of user actions that are running right now; a second tap of the same action is ignored (M7, M8). */
    val inFlight: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /**
     * Deletes files no record points to (I9): directories of projects without a record, and stale files inside
     * known projects. Files younger than a day survive, so a photo still being captured is never swept.
     */
    suspend fun sweepOrphans(): Int {
        val known = HashSet<String>()
        for (projectId in database.records().projectIds()) {
            known += projectId
            for ((table, extension, folder) in FILE_TABLES) {
                for (entity in database.records().list(table.id, projectId)) {
                    val fileId = fileIdOf(entity.fields) ?: entity.id
                    known += "projects/$projectId/$folder/$fileId.$extension"
                }
            }
        }
        return files.sweepOrphans(known, TimeUnit.DAYS.toMillis(1), clock.millis())
    }

    private fun fileIdOf(fields: String): String? = try {
        (Converters.stringToFields(fields)["fileId"] as? JsonPrimitive)?.takeIf { it.isString }?.content
    } catch (e: SerializationException) {
        null
    }

    private companion object {
        val FILE_TABLES = listOf(
            Triple(SyncTable.Photos, "jpg", "photos"),
            Triple(SyncTable.Documents, "pdf", "documents"),
            Triple(SyncTable.Protocols, "pdf", "protocols"),
        )
    }
}
