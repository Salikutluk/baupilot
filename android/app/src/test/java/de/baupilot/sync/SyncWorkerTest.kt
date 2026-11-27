package de.baupilot.sync

import android.content.Context
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import de.baupilot.backend.DemoAuth
import de.baupilot.capture.FileStore
import de.baupilot.data.local.BaupilotDatabase
import de.baupilot.data.local.LocalRepository
import java.io.File
import java.time.Clock
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SyncWorkerTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val context: Context get() = RuntimeEnvironment.getApplication()
    private lateinit var db: BaupilotDatabase

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build(),
        )
        db = Room.inMemoryDatabaseBuilder(context, BaupilotDatabase::class.java).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        SyncEngineRegistry.provider = null
        db.close()
    }

    private fun engine(remote: RemoteGateway?): SyncEngine {
        val clock = Clock.systemUTC()
        val stamps = StampClock(clock, "devA") { 0L }
        val files = FileStore(tmp.newFolder())
        val repo = LocalRepository(db, { "{}" }, files, stamps, clock) {}
        return SyncEngine(db, repo, remote, DemoAuth("u1", "bauherr"), files, stamps)
    }

    private fun workSpec(name: String) = WorkManager.getInstance(context).getWorkInfosForUniqueWork(name).get()

    @Test
    fun workerMapsOutcomes() {
        val results = mapOf(
            SyncOutcome.Done(1, 2, 0, 0) to ListenableWorker.Result.success(),
            SyncOutcome.NotLoggedIn to ListenableWorker.Result.success(),
            SyncOutcome.NotConfigured to ListenableWorker.Result.success(),
            SyncOutcome.Retry("offline") to ListenableWorker.Result.retry(),
            SyncOutcome.AuthRequired to ListenableWorker.Result.failure(),
        )
        for ((outcome, expected) in results) {
            assertEquals(outcome.toString(), expected, SyncWorker.resultFor(outcome))
        }
    }

    @Test
    fun workerDrainsTheRegisteredEngine() = runBlocking {
        val worker = TestListenableWorkerBuilder<SyncWorker>(context).build()

        SyncEngineRegistry.provider = null
        assertEquals(ListenableWorker.Result.retry(), worker.doWork())

        SyncEngineRegistry.provider = { engine(null) }
        assertEquals(ListenableWorker.Result.success(), worker.doWork())

        val offline = FakeRemoteGateway().apply { online = false }
        SyncEngineRegistry.provider = { engine(offline) }
        assertEquals(ListenableWorker.Result.success(), worker.doWork())
    }

    @Test
    fun requestSyncUsesConnectedConstraint() {
        val scheduler = WorkManagerSyncScheduler(context)
        scheduler.requestSync()
        scheduler.requestSync()

        val infos = workSpec("baupilot-sync")
        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.ENQUEUED, infos.single().state)
        assertEquals(NetworkType.CONNECTED, infos.single().constraints.requiredNetworkType)

        val spec = (WorkManager.getInstance(context) as androidx.work.impl.WorkManagerImpl)
            .workDatabase.workSpecDao().getWorkSpec(infos.single().id.toString())!!
        assertEquals(androidx.work.BackoffPolicy.EXPONENTIAL, spec.backoffPolicy)
        assertEquals(TimeUnit.SECONDS.toMillis(30), spec.backoffDelayDuration)
    }

    @Test
    fun schedulePeriodicKeepsOneSixHourWork() {
        val scheduler = WorkManagerSyncScheduler(context)
        scheduler.schedulePeriodic()
        scheduler.schedulePeriodic()

        val info = workSpec("baupilot-sync-periodic").single()
        assertEquals(NetworkType.CONNECTED, info.constraints.requiredNetworkType)
        assertEquals(TimeUnit.HOURS.toMillis(6), info.periodicityInfo!!.repeatIntervalMillis)

        scheduler.cancelAll()
        assertTrue(workSpec("baupilot-sync-periodic").single().state == WorkInfo.State.CANCELLED)
    }
}
