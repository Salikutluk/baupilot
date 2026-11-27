package de.baupilot.sync

import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import de.baupilot.backend.DemoAuth
import de.baupilot.capture.CaptureResult
import de.baupilot.capture.FileStore
import de.baupilot.capture.PhotoOwner
import de.baupilot.capture.StepCapture
import de.baupilot.capture.StepKeys
import de.baupilot.capture.StepStatus
import de.baupilot.data.local.BaupilotDatabase
import de.baupilot.data.local.LocalRepository
import de.baupilot.data.local.OutboxKind
import java.io.File
import java.time.Clock
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/** Acceptance A2 (plan step 11): work done offline reaches the remote once the network is back. */
@RunWith(RobolectricTestRunner::class)
class OfflineSyncTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val context get() = RuntimeEnvironment.getApplication()
    private val fixture: String =
        File(System.getProperty("baupilot.repoRoot"), "fixtures/baupilot.json").readText()
    private val remote = FakeRemoteGateway()
    private lateinit var db: BaupilotDatabase
    private lateinit var files: FileStore
    private lateinit var repo: LocalRepository
    private lateinit var scheduler: WorkManagerSyncScheduler
    private lateinit var projectId: String

    @Before
    fun setUp() = runBlocking {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build(),
        )
        scheduler = WorkManagerSyncScheduler(context)
        db = Room.inMemoryDatabaseBuilder(context, BaupilotDatabase::class.java).allowMainThreadQueries().build()
        val clock = Clock.systemUTC()
        val stamps = StampClock(clock, "devA") { 0L }
        files = FileStore(tmp.newFolder())
        repo = LocalRepository(db, { fixture }, files, stamps, clock) { scheduler.requestSync() }
        repo.ensureSeeded()
        projectId = db.records().projectIds().first()
        // Without a cached user the drain returns NotLoggedIn -> success and the test would pass vacuously.
        val engine = SyncEngine(db, repo, remote, DemoAuth(initialUser = "u1", role = "bauherr"), files, stamps)
        SyncEngineRegistry.provider = { engine }
    }

    @After
    fun tearDown() {
        SyncEngineRegistry.provider = null
        db.close()
    }

    private fun work(): WorkInfo =
        WorkManager.getInstance(context).getWorkInfosForUniqueWork("baupilot-sync").get().single()

    /** The worker runs on a coroutine dispatcher, so the state settles shortly after the driver releases the work. */
    private fun settled(): WorkInfo {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20)
        while (work().state == WorkInfo.State.RUNNING && System.nanoTime() < deadline) Thread.sleep(10)
        return work()
    }

    @Test
    fun offlineCaptureSyncsOnceOnline() = runBlocking {
        remote.online = false
        val stepKey = "shell/element/W1/step/1"
        val step = repo.recordStep(StepCapture(stepKey, "shell", StepStatus.OK))
        assertTrue(step is CaptureResult.Ok)
        val photoId = "ph_" + "a".repeat(32)
        val photo = files.newPhotoFile(projectId, photoId).apply { writeBytes(JPEG) }
        assertTrue(repo.addPhoto(PhotoOwner.Step(stepKey, "shell"), photoId, photo) is CaptureResult.Ok)

        // The repository's outbox callback requested the sync; the worker runs offline and backs off.
        val driver = WorkManagerTestInitHelper.getTestDriver(context)!!
        driver.setAllConstraintsMet(work().id)
        assertEquals(WorkInfo.State.ENQUEUED, settled().state)
        assertEquals(1, work().runAttemptCount)
        assertEquals(
            listOf("UPLOAD_FILE photos/$photoId", "UPSERT photos/$photoId", "UPSERT steps/${StepKeys.recordId(projectId, stepKey)}"),
            db.outbox().all().map {
                if (it.kind == OutboxKind.UPLOAD_FILE) "UPLOAD_FILE photos/${it.rowId}" else "${it.kind} ${it.tableId}/${it.rowId}"
            },
        )

        remote.online = true
        driver.setAllConstraintsMet(work().id)
        assertEquals(WorkInfo.State.SUCCEEDED, settled().state)

        val stepId = StepKeys.recordId(projectId, stepKey)
        val stepRow = remote.row(SyncTable.Steps, stepId)
        assertNotNull(stepRow)
        assertEquals("ok", stepRow!!.fields.getValue("status").jsonPrimitive.content)
        val photoRow = remote.row(SyncTable.Photos, photoId)
        assertNotNull(photoRow)
        assertEquals(photoId, photoRow!!.fields.getValue("fileId").jsonPrimitive.content)
        assertTrue(remote.uploads.containsKey("photos/$photoId"))
        assertEquals(files.sha256(photo), photoRow.fields.getValue("sha256").jsonPrimitive.content)
        assertTrue(db.outbox().all().isEmpty())
    }

    private companion object {
        /** SOI, JFIF APP0, baseline SOF0 (1x1, one component), SOS, one scan byte, EOI. */
        val JPEG: ByteArray = intArrayOf(
            0xFF, 0xD8,
            0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
            0xFF, 0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x11, 0x00,
            0xFF, 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00,
            0x00,
            0xFF, 0xD9,
        ).map { it.toByte() }.toByteArray()
    }
}
