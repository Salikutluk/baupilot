package de.baupilot

import androidx.room.Room
import de.baupilot.backend.AuthGateway
import de.baupilot.backend.BackendConfig
import de.baupilot.backend.LoginRefusal
import de.baupilot.backend.LoginResult
import de.baupilot.backend.NoPush
import de.baupilot.capture.FileStore
import de.baupilot.data.local.BaupilotDatabase
import de.baupilot.data.local.LocalRepository
import de.baupilot.protocol.ProtocolExporter
import de.baupilot.sync.FakeRemoteGateway
import de.baupilot.sync.StampClock
import de.baupilot.sync.SyncEngine
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** An [AuthGateway] that never signs anyone in: every login is refused with [reason]. */
class RefusingAuth(private val reason: LoginRefusal) : AuthGateway {
    override val cachedUserId: String? = null
    override val cachedRole: String? = null

    override suspend fun logIn(email: String, password: String): LoginResult = LoginResult.Refused(reason)

    override suspend fun logOut() = Unit

    override fun clearCachedUser() = Unit
}

/**
 * [TestBaupilotApplication] with an auth that refuses every login (`InvalidCredentials`). Use it through
 * `@Config(application = TestRefusingApplication::class)` in the refusal test only.
 */
class TestRefusingApplication : BaupilotApplication() {
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
        val auth = RefusingAuth(LoginRefusal.InvalidCredentials)
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
