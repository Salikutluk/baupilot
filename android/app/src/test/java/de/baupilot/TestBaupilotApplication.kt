package de.baupilot

import androidx.room.Room
import de.baupilot.backend.BackendConfig
import de.baupilot.backend.DemoAuth
import de.baupilot.backend.NoPush
import de.baupilot.capture.FileStore
import de.baupilot.data.local.BaupilotDatabase
import de.baupilot.data.local.LocalRepository
import de.baupilot.protocol.ProtocolExporter
import de.baupilot.sync.FakeRemoteGateway
import de.baupilot.sync.StampClock
import de.baupilot.sync.SyncEngine
import de.baupilot.sync.SyncScheduler
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Counts the calls and never touches WorkManager (R3). */
class RecordingSyncScheduler : SyncScheduler {
    var requests = 0
        private set
    var periodicScheduled = false
        private set
    var cancelled = 0
        private set

    override fun requestSync() {
        requests++
    }

    override fun schedulePeriodic() {
        periodicScheduled = true
    }

    override fun cancelAll() {
        cancelled++
    }
}

/**
 * Robolectric's application (`robolectric.properties`, D23): an in-memory database, `DemoAuth`, no push, a fixed
 * clock and an in-memory remote. No Firebase, no Appwrite, no WorkManager.
 */
class TestBaupilotApplication : BaupilotApplication() {
    override fun createServices(): AppServices {
        val clock = Clock.fixed(Instant.parse("2026-12-18T09:00:00Z"), ZoneOffset.UTC)
        val database = Room.inMemoryDatabaseBuilder(this, BaupilotDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val files = FileStore(filesDir.apply { mkdirs() })
        val stamps = StampClock(clock, DEVICE_ID) { 0L }
        val scheduler = RecordingSyncScheduler()
        val repository = LocalRepository(
            db = database,
            fixtureJson = { assets.open("baupilot.json").bufferedReader(Charsets.UTF_8).use { it.readText() } },
            files = files,
            stamps = stamps,
            clock = clock,
            onOutboxChanged = scheduler::requestSync,
        )
        val auth = DemoAuth()
        val remote = FakeRemoteGateway()
        return AppServices(
            config = BackendConfig(projectId = "", fcm = null),
            clock = clock,
            deviceId = DEVICE_ID,
            database = database,
            repository = repository,
            auth = auth,
            remote = remote,
            sync = scheduler,
            engine = SyncEngine(database, repository, remote, auth, files, stamps),
            files = files,
            protocols = ProtocolExporter(files, clock),
            push = NoPush,
            io = Dispatchers.IO,
            appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        )
    }

    private companion object {
        const val DEVICE_ID = "test-device"
    }
}
