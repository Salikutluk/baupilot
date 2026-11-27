package de.baupilot.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.core.content.FileProvider
import de.baupilot.AppServices
import de.baupilot.BaupilotApplication
import de.baupilot.MainActivity
import de.baupilot.RecordingSyncScheduler
import de.baupilot.TestRefusingApplication
import de.baupilot.data.local.BaupilotDatabase
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

/**
 * Plan step 13, end to end: the wired app (`MainActivity` with `TestBaupilotApplication`) through the camera, the
 * photo, the PDF import, the sign-off, the defect form, sign-out and the login refusal. The system camera and the
 * document picker are played by the test through Robolectric's started-activity-for-result queue. Refusals change
 * nothing (I4): no record, op, file or navigation.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class CaptureFlowUiTest {
    /**
     * `FileProvider` caches its path strategy per authority in a static map, and Robolectric gives every test its own
     * `filesDir`: without a reset, the second test resolves camera targets against the first test's directory.
     */
    @get:Rule(order = 0)
    val freshFileProvider = object : ExternalResource() {
        override fun before() {
            val cache = FileProvider::class.java.getDeclaredField("sCache").apply { isAccessible = true }
            @Suppress("UNCHECKED_CAST")
            (cache.get(null) as MutableMap<String, Any>).clear()
        }
    }

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    private val services: AppServices
        get() = (RuntimeEnvironment.getApplication() as BaupilotApplication).services

    private val db: BaupilotDatabase get() = services.database

    // ---- navigation ----

    /**
     * Waits for [condition] while draining the main looper: the app posts Toasts and coroutine resumptions to it from
     * its IO threads, and the compose rule's own idling does not run those posts.
     */
    private fun await(condition: () -> Boolean) {
        rule.waitUntil(timeoutMillis = 5_000) {
            shadowOf(Looper.getMainLooper()).idle()
            condition()
        }
    }

    private fun exists(tag: String): Boolean = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun awaitScreen(tag: String) {
        await { exists(tag) }
        rule.onNodeWithTag(tag).assertExists()
    }

    private fun tap(tag: String) {
        awaitScreen(tag)
        rule.onNodeWithTag(tag).performClick()
        rule.waitForIdle()
    }

    private fun scrollAndTap(tag: String) {
        awaitScreen(tag)
        rule.onNodeWithTag(tag).performScrollTo().performClick()
        rule.waitForIdle()
    }

    private fun logIn() {
        awaitScreen("login.submit")
        rule.onNodeWithTag("login.submit").performClick()
        awaitScreen("screen.home")
    }

    private fun openStep() {
        logIn()
        tap("tab.inspections")
        awaitScreen("inspections.view.trades")
        tap("inspections.trade.windows")
        awaitScreen("screen.inspection.windows")
        scrollAndTap("inspection.element.F8")
        awaitScreen("screen.inspection.windows.F8.step.3")
    }

    private fun openDefectFormFromStep() {
        openStep()
        tap("step.defect")
        awaitScreen("screen.defect.new")
    }

    private fun openSignOff() {
        openDefectFormFromStep()
        tap("defect.save")
        awaitScreen("screen.inspection.windows.sign")
        awaitRows("SELECT COUNT(*) FROM records WHERE tableId = 'defects' AND id LIKE 'df\\_%' ESCAPE '\\'", 1)
    }

    private fun drawOnOwnerTile() {
        rule.onNodeWithTag("sign.owner").performScrollTo().performTouchInput {
            swipe(Offset(width * 0.2f, height * 0.5f), Offset(width * 0.8f, height * 0.6f), durationMillis = 200)
        }
        rule.waitForIdle()
    }

    // ---- database ----

    private fun count(sql: String): Long = db.query(sql, emptyArray()).use { it.moveToFirst(); it.getLong(0) }

    /** Repository writes run on the app scope off the main thread, so a test waits for the row it expects. */
    private fun awaitRows(sql: String, expected: Long) {
        await { count(sql) == expected }
    }

    private fun photoRows() = count("SELECT COUNT(*) FROM records WHERE tableId = 'photos'")

    private fun documentRows() = count("SELECT COUNT(*) FROM records WHERE tableId = 'documents'")

    private fun protocolRows() = count("SELECT COUNT(*) FROM records WHERE tableId = 'protocols'")

    private fun outboxRows() = count("SELECT COUNT(*) FROM outbox")

    /** The ops of one photo: its upload and the upserts of the `photos` and `steps` tables only. */
    private fun photoStepOps(photoId: String) = count(
        "SELECT COUNT(*) FROM outbox WHERE tableId IN ('photos', 'steps') OR (kind = 'UPLOAD_FILE' AND rowId = '$photoId')",
    )

    private fun fieldsOf(table: String, id: String) = db.query(
        "SELECT fields FROM records WHERE tableId = '$table' AND id = '$id'",
        emptyArray(),
    ).use { cursor ->
        assertTrue("$table/$id must exist", cursor.moveToFirst())
        Json.parseToJsonElement(cursor.getString(0)).jsonObject
    }

    /** The id of the one record the app created in [table]; the fixture's own ids carry no [prefix]. */
    private fun createdId(table: String, prefix: String): String = db.query(
        "SELECT id FROM records WHERE tableId = '$table' AND id LIKE '$prefix\\_%' ESCAPE '\\'",
        emptyArray(),
    ).use { cursor ->
        assertEquals("exactly one new $table record", 1, cursor.count)
        cursor.moveToFirst()
        cursor.getString(0)
    }

    // ---- the system camera and document picker ----

    private fun awaitStarted(): org.robolectric.shadows.ShadowActivity.IntentForResult {
        val activity = shadowOf(rule.activity)
        await { activity.peekNextStartedActivityForResult() != null }
        return activity.nextStartedActivityForResult
    }

    private fun targetOf(request: org.robolectric.shadows.ShadowActivity.IntentForResult): Uri {
        @Suppress("DEPRECATION")
        val uri = request.intent.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)
        assertNotNull("the camera intent carries EXTRA_OUTPUT", uri)
        return uri!!
    }

    /** The FileProvider path `/projects/<project>/photos/<id>.jpg` is the file store's relative path. */
    private fun fileOf(uri: Uri): File = services.files.resolve(uri.path!!.removePrefix("/"))

    private fun photoIdOf(file: File): String = file.nameWithoutExtension

    private fun writeJpeg(file: File) {
        file.parentFile!!.mkdirs()
        file.writeBytes(JPEG)
    }

    private fun toastText(): String? = ShadowToast.getTextOfLatestToast()

    private fun awaitToast(text: String) {
        await { toastText() == text }
        assertEquals(text, toastText())
    }

    private fun pickPdf(name: String, bytes: ByteArray) {
        val uri = Uri.parse("content://de.baupilot.test.documents/$name")
        shadowOf(RuntimeEnvironment.getApplication().contentResolver).registerInputStream(uri, ByteArrayInputStream(bytes))
        val request = awaitStarted()
        assertEquals(Intent.ACTION_OPEN_DOCUMENT, request.intent.action)
        shadowOf(rule.activity).receiveResult(request.intent, Activity.RESULT_OK, Intent().setData(uri))
    }

    // ---- photos ----

    @Test
    fun stepPhotoLaunchesCameraWithFileProviderUri() {
        openStep()
        tap("step.photo.add")
        val request = awaitStarted()
        assertEquals(MediaStore.ACTION_IMAGE_CAPTURE, request.intent.action)
        val target = targetOf(request)
        assertEquals("content", target.scheme)
        assertTrue("FileProvider authority: $target", target.toString().startsWith("content://de.baupilot.files/"))
        assertTrue("the shot lands in the photo store: $target", target.path!!.endsWith(".jpg"))
        assertEquals(0L, photoRows())
    }

    @Test
    fun cameraResultOkRecordsPhoto() {
        openStep()
        tap("step.photo.add")
        val request = awaitStarted()
        val file = fileOf(targetOf(request))
        writeJpeg(file)

        shadowOf(rule.activity).receiveResult(request.intent, Activity.RESULT_OK, null)

        awaitRows("SELECT COUNT(*) FROM records WHERE tableId = 'photos'", 1)
        val photoId = photoIdOf(file)
        val fields = fieldsOf("photos", photoId)
        assertEquals("steps", fields.getValue("ownerTable").jsonPrimitive.content)
        assertEquals(photoId, fields.getValue("fileId").jsonPrimitive.content)
        assertEquals(services.files.sha256(file), fields.getValue("sha256").jsonPrimitive.content)
        assertTrue("the recorded photo stays on disk", file.isFile)
        awaitRows(
            "SELECT COUNT(*) FROM outbox WHERE tableId IN ('photos', 'steps') OR (kind = 'UPLOAD_FILE' AND rowId = '$photoId')",
            3,
        )
        assertEquals(3L, photoStepOps(photoId))
    }

    @Test
    fun missingCameraAppShowsToast() {
        openStep()
        shadowOf(RuntimeEnvironment.getApplication()).checkActivities(true)
        tap("step.photo.add")
        awaitToast(RootToasts.STORAGE_FAILED)
        assertNull("nothing was launched", shadowOf(rule.activity).peekNextStartedActivityForResult())
        assertEquals(0L, photoRows())
        assertEquals(0L, photoStepOps("none"))
        awaitScreen("screen.inspection.windows.F8.step.3")
    }

    @Test
    fun cameraCancelledDeletesFile() {
        openStep()
        tap("step.photo.add")
        val request = awaitStarted()
        val file = fileOf(targetOf(request))
        writeJpeg(file)
        assertTrue(file.isFile)

        shadowOf(rule.activity).receiveResult(request.intent, Activity.RESULT_CANCELED, null)

        await { !file.exists() }
        rule.waitForIdle()
        assertFalse("a cancelled shot leaves no file", file.exists())
        assertEquals(0L, photoRows())
        assertEquals(0L, photoStepOps(photoIdOf(file)))
    }

    // ---- documents ----

    @Test
    fun importPdfFromGewerkAssignsTrade() {
        logIn()
        scrollAndTap("home.trade.screed")
        awaitScreen("screen.trade.screed")
        val before = documentRows()
        tap("ph.Gewerk.add")

        pickPdf("angebot.pdf", PDF)

        awaitRows("SELECT COUNT(*) FROM records WHERE tableId = 'documents'", before + 1)
        val id = createdId("documents", "dc")
        val fields = fieldsOf("documents", id)
        assertEquals("screed", fields.getValue("tradeId").jsonPrimitive.content)
        assertEquals("offer", fields.getValue("kind").jsonPrimitive.content)
        val file = services.files.documentFile(PROJECT, id)
        assertTrue("the imported PDF is stored", file.isFile)
        assertEquals(services.files.sha256(file), fields.getValue("fileSha256").jsonPrimitive.content)
    }

    @Test
    fun importNonPdfShowsToastAndWritesNothing() {
        logIn()
        tap("tab.documents")
        awaitScreen("screen.documents")
        rule.waitForIdle()
        val documents = documentRows()
        val ops = outboxRows()
        val files = services.files.projectDir(PROJECT).walkTopDown().filter { it.isFile }.count()
        tap("ph.Dokumente.add")

        pickPdf("notiz.txt", "just some text".toByteArray())

        awaitToast(RootToasts.NOT_PDF)
        rule.waitForIdle()
        assertEquals(documents, documentRows())
        assertEquals(ops, outboxRows())
        assertEquals(files, services.files.projectDir(PROJECT).walkTopDown().filter { it.isFile }.count())
        awaitScreen("screen.documents")
    }

    // ---- sign-off ----

    @Test
    fun finishWithoutSignatureShowsToastAndStays() {
        openSignOff()
        val ops = outboxRows()
        val protocols = protocolRows()

        tap("sign.finish")

        awaitToast(RootToasts.EMPTY_SIGNATURE)
        rule.waitForIdle()
        awaitScreen("screen.inspection.windows.sign")
        assertEquals(protocols, protocolRows())
        assertEquals(0L, count("SELECT COUNT(*) FROM records WHERE tableId = 'signatures'"))
        assertEquals(ops, outboxRows())
    }

    @Test
    fun finishWithSignatureWritesProtocolAndNavigatesToDefects() {
        openSignOff()
        drawOnOwnerTile()

        tap("sign.finish")

        awaitScreen("screen.inspections")
        awaitScreen("inspections.view.defects")
        assertEquals(1L, protocolRows())
        val id = createdId("protocols", "pr")
        val fields = fieldsOf("protocols", id)
        val fileSha = fields.getValue("fileSha256").jsonPrimitive.content
        val contentSha = fields.getValue("contentSha256").jsonPrimitive.content
        val pdf = services.files.protocolFile(PROJECT, fields.getValue("fileId").jsonPrimitive.content)
        assertTrue("the protocol PDF exists: $pdf", pdf.isFile)
        assertEquals(fileSha, services.files.sha256(pdf))
        assertTrue("the PDF carries the content hash", pdf.readText(Charsets.ISO_8859_1).contains(contentSha))
        assertEquals(1L, count("SELECT COUNT(*) FROM records WHERE tableId = 'signatures'"))
    }

    // ---- defect form ----

    @Test
    fun defectSaveThenCancelDraftCleanup() {
        openDefectFormFromStep()
        tap("defect.photo.add")
        val saved = awaitStarted()
        val savedFile = fileOf(targetOf(saved))
        writeJpeg(savedFile)
        shadowOf(rule.activity).receiveResult(saved.intent, Activity.RESULT_OK, null)
        awaitRows("SELECT COUNT(*) FROM records WHERE tableId = 'photos'", 1)

        tap("defect.save")
        awaitScreen("screen.inspection.windows.sign")
        val defectId = createdId("defects", "df")
        awaitRows("SELECT COUNT(*) FROM outbox WHERE tableId = 'defects' AND rowId = '$defectId'", 1)
        assertEquals(defectId, fieldsOf("photos", photoIdOf(savedFile)).getValue("ownerId").jsonPrimitive.content)
        assertTrue("a saved defect keeps its photo", savedFile.isFile)

        tap("nav.back")
        awaitScreen("screen.inspection.windows")
        scrollAndTap("inspection.element.F2")
        awaitScreen("screen.defect.new")
        tap("defect.photo.add")
        val discarded = awaitStarted()
        val discardedFile = fileOf(targetOf(discarded))
        writeJpeg(discardedFile)
        shadowOf(rule.activity).receiveResult(discarded.intent, Activity.RESULT_OK, null)
        awaitRows("SELECT COUNT(*) FROM records WHERE tableId = 'photos'", 2)

        tap("nav.close")
        awaitScreen("screen.inspection.windows")

        awaitRows("SELECT COUNT(*) FROM records WHERE tableId = 'photos'", 1)
        await { !discardedFile.exists() }
        assertFalse("the cancelled draft's photo file is gone", discardedFile.exists())
        assertTrue("the saved defect's photo stays", savedFile.isFile)
        assertEquals(1L, count("SELECT COUNT(*) FROM records WHERE tableId = 'defects' AND id LIKE 'df\\_%' ESCAPE '\\'"))
    }

    // ---- sign-out and login ----

    @Test
    fun signOutReturnsToLogin() {
        logIn()
        val records = count("SELECT COUNT(*) FROM records")
        tap("tab.profile")
        awaitScreen("screen.profile")

        scrollAndTap("ph.Profil.row.6")

        awaitScreen("login.submit")
        assertNull("the cached session is gone", services.auth.cachedUserId)
        assertTrue("pending syncs are cancelled", (services.sync as RecordingSyncScheduler).cancelled >= 1)
        assertEquals("local data stays", records, count("SELECT COUNT(*) FROM records"))
        assertFalse(exists("screen.home"))
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp", application = TestRefusingApplication::class)
    fun loginRefusalShowsToastAndStaysLoggedOut() {
        awaitScreen("login.submit")
        rule.onNodeWithTag("login.submit").performClick()

        awaitToast(RootToasts.INVALID_CREDENTIALS)
        rule.waitForIdle()
        awaitScreen("login.submit")
        assertFalse(exists("screen.home"))
        assertNull(services.auth.cachedUserId)
        assertEquals(0L, outboxRows())
    }

    private companion object {
        const val PROJECT = "musterweg-12"

        val PDF: ByteArray = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog >>\nendobj\n%%EOF\n".toByteArray(Charsets.ISO_8859_1)

        /** The smallest JPEG `ExifInterface` accepts. */
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
