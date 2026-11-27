package de.baupilot

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import de.baupilot.backend.AppwriteAuth
import de.baupilot.backend.AppwriteClientFactory
import de.baupilot.backend.AppwriteGateway
import de.baupilot.backend.AppwritePushRegistrar
import de.baupilot.backend.AuthSelector
import de.baupilot.backend.BackendConfig
import de.baupilot.backend.FcmConfig
import de.baupilot.backend.NoPush
import de.baupilot.backend.PushRegistrar
import de.baupilot.capture.FileStore
import de.baupilot.data.RepositoryException
import de.baupilot.data.local.BaupilotDatabase
import de.baupilot.data.local.LocalRepository
import de.baupilot.protocol.ProtocolExporter
import de.baupilot.push.ReminderNotifier
import de.baupilot.sync.StampClock
import de.baupilot.sync.SyncEngine
import de.baupilot.sync.SyncEngineRegistry
import de.baupilot.sync.WorkManagerSyncScheduler
import io.appwrite.Client
import io.appwrite.services.Account
import java.io.FileNotFoundException
import java.time.Clock
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Builds the services once and hands WorkManager its configuration on demand (D23). The default
 * `WorkManagerInitializer` is removed in the manifest, so a Robolectric subclass can replace [createServices]
 * without starting the real stack.
 */
open class BaupilotApplication : Application(), Configuration.Provider {
    val services: AppServices by lazy { createServices() }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()
        val current = services
        current.config.fcm?.let { initFirebase(it) }
        ReminderNotifier(this).ensureChannel()
        SyncEngineRegistry.provider = { current.engine }
        current.appScope.launch(current.io) {
            // A failing sweep (SQLite, a symlink out of the files directory) must never crash the start.
            try {
                current.sweepOrphans()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Orphan sweep failed", e)
            }
        }
    }

    /** The database is not opened here; Room opens it on first use, off the main thread. */
    protected open fun createServices(): AppServices {
        val config = BackendConfig.fromBuildConfig()
        val clock = Clock.systemUTC()
        val io = Dispatchers.IO
        val appScope = CoroutineScope(SupervisorJob() + io)
        val deviceId = deviceId()
        val database = Room.databaseBuilder(this, BaupilotDatabase::class.java, DATABASE_NAME).build()
        val files = FileStore(filesDir)

        val maxSeen = AtomicLong(0L)
        appScope.launch {
            database.state().observe(MAX_SEEN_KEY).collect { maxSeen.set(it?.toLongOrNull() ?: 0L) }
        }
        val stamps = StampClock(clock, deviceId) { maxSeen.get() }

        val scheduler = WorkManagerSyncScheduler(this)
        val repository = LocalRepository(
            db = database,
            fixtureJson = ::readFixture,
            files = files,
            stamps = stamps,
            clock = clock,
            onOutboxChanged = { scheduler.requestSync() },
        )

        val client: Client? = if (config.isConfigured) AppwriteClientFactory.create(this, config) else null
        val push: PushRegistrar = if (client != null && config.fcm != null) {
            AppwritePushRegistrar(Account(client), getSharedPreferences(PUSH_PREFS, MODE_PRIVATE), ::fcmToken)
        } else {
            NoPush
        }
        val auth = AuthSelector.select(config, BuildConfig.DEBUG) {
            AppwriteAuth(client!!, getSharedPreferences(AUTH_PREFS, MODE_PRIVATE), push)
        }
        val remote = client?.let { AppwriteGateway(it) }
        val engine = SyncEngine(database, repository, remote, auth, files, stamps)

        return AppServices(
            config = config,
            clock = clock,
            deviceId = deviceId,
            database = database,
            repository = repository,
            auth = auth,
            remote = remote,
            sync = scheduler,
            engine = engine,
            files = files,
            protocols = ProtocolExporter(files, clock),
            push = push,
            io = io,
            appScope = appScope,
        )
    }

    private fun readFixture(): String = try {
        assets.open(FIXTURE_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
    } catch (e: FileNotFoundException) {
        throw RepositoryException.FixtureMissing(FIXTURE_ASSET)
    }

    /** A random id per installation, not a hardware id. */
    private fun deviceId(): String {
        val prefs = getSharedPreferences(DEVICE_PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }
    }

    private fun initFirebase(fcm: FcmConfig) {
        if (FirebaseApp.getApps(this).isNotEmpty()) return
        val options = FirebaseOptions.Builder()
            .setApplicationId(fcm.applicationId)
            .setApiKey(fcm.apiKey)
            .setProjectId(fcm.projectId)
            .setGcmSenderId(fcm.senderId)
            .build()
        FirebaseApp.initializeApp(this, options)
    }

    private suspend fun fcmToken(): String? = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
    }

    private companion object {
        const val TAG = "BaupilotApplication"
        const val DATABASE_NAME = "baupilot.db"
        const val FIXTURE_ASSET = "baupilot.json"
        const val DEVICE_PREFS = "baupilot.device"
        const val AUTH_PREFS = "baupilot.auth"
        const val PUSH_PREFS = "baupilot.push"
        const val KEY_DEVICE_ID = "deviceId"

        /** The sync state key of the highest stamp seen from the server (the engine writes it). */
        const val MAX_SEEN_KEY = "maxSeenAt"
    }
}
