package de.baupilot.capture

import androidx.room.Room
import de.baupilot.app.AppData
import de.baupilot.app.AppRoute
import de.baupilot.data.Defect
import de.baupilot.data.FixtureJson
import de.baupilot.data.PlatformText
import de.baupilot.data.RepositoryException
import de.baupilot.data.local.BaupilotDatabase
import de.baupilot.data.local.LocalRepository
import de.baupilot.data.local.OutboxEntity
import de.baupilot.data.local.OutboxKind
import de.baupilot.inspection.Extent
import de.baupilot.inspection.Point
import de.baupilot.inspection.SignatureStrokes
import de.baupilot.protocol.ExportedProtocol
import de.baupilot.protocol.ProtocolDocument
import de.baupilot.sync.StampClock
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class CaptureRepositoryTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val fixture: String =
        File(System.getProperty("baupilot.repoRoot"), "fixtures/baupilot.json").readText()
    private val clock: Clock = Clock.fixed(Instant.parse("2026-12-18T09:30:00Z"), ZoneOffset.UTC)
    private lateinit var db: BaupilotDatabase
    private lateinit var files: FileStore
    private lateinit var repo: LocalRepository
    private var outboxCalls = 0
    private val project = "musterweg-12"

    /** Runs once, right before the next Room transaction is dispatched: the window of a purge between check and write. */
    private var beforeTransaction: (() -> Unit)? = null
    private val transactionThread = Executors.newSingleThreadExecutor()

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), BaupilotDatabase::class.java)
            .allowMainThreadQueries()
            .setTransactionExecutor { task ->
                beforeTransaction?.let { hook ->
                    beforeTransaction = null
                    hook()
                }
                transactionThread.execute(task)
            }
            .build()
        files = FileStore(tmp.newFolder("files"))
        repo = LocalRepository(db, { fixture }, files, StampClock(clock, "dev") { 0L }, clock) { outboxCalls++ }
        repo.ensureSeeded()
    }

    @After
    fun tearDown() {
        db.close()
        transactionThread.shutdownNow()
    }

    // ---- helpers ----

    private fun scalar(sql: String, vararg args: Any): Long =
        db.query(sql, arrayOf(*args)).use { it.moveToFirst(); it.getLong(0) }

    private fun dump(): String = db.query(
        "SELECT tableId, id, projectId, sortKey, fields, stamps, deleted FROM records ORDER BY tableId, id",
        emptyArray(),
    ).use { c ->
        buildString { while (c.moveToNext()) append((0 until 7).joinToString("|") { c.getString(it) }).append('\n') }
    }

    private suspend fun ops(): List<OutboxEntity> = db.outbox().all()

    private suspend fun opSummary(): List<String> = ops().map {
        when (it.kind) {
            OutboxKind.UPSERT -> "UPSERT ${it.tableId}/${it.rowId}"
            OutboxKind.UPLOAD_FILE -> "UPLOAD ${Json.parseToJsonElement(it.payload).jsonObject.getValue("bucket").jsonPrimitive.content}/${it.rowId}"
            OutboxKind.DELETE_PROJECT -> "DELETE_PROJECT ${it.rowId}"
        }
    }

    private suspend fun record(table: String, id: String) = db.records().get(table, id)

    private suspend fun fieldsOf(table: String, id: String): JsonObject =
        Json.parseToJsonElement(record(table, id)!!.fields).jsonObject

    private suspend fun stampsOf(table: String, id: String): JsonObject =
        Json.parseToJsonElement(record(table, id)!!.stamps).jsonObject

    private fun dataVersion(): String? = scalar("SELECT COUNT(*) FROM sync_state WHERE key = 'dataVersion'").let {
        if (it == 0L) null else db.query("SELECT value FROM sync_state WHERE key = 'dataVersion'", emptyArray())
            .use { c -> c.moveToFirst(); c.getString(0) }
    }

    private fun photoFile(photoId: String, content: String = "jpeg-bytes"): File =
        files.newPhotoFile(project, photoId).also { it.writeText(content) }

    private fun source(name: String?, bytes: ByteArray) = object : ImportSource {
        override val displayName: String? = name
        override fun open(): InputStream = ByteArrayInputStream(bytes)
    }

    private val pdfBytes = "%PDF-1.7 hello".toByteArray()

    private fun draft(id: String = "df_" + "1".repeat(32), tradeId: String = "windows", deadline: LocalDate = LocalDate.of(2027, 1, 15)) =
        DefectDraft(
            id = id, tradeId = tradeId, stepKey = "windows/element/F2/step/1", elementId = "F2", title = "Kratzer im Glas",
            description = "Ca. 3 cm lang.", severity = "Mittel", deadline = deadline, notifyByEmail = true,
            responsible = "Fensterbau Weber GmbH",
        )

    private fun strokes() = SignatureStrokes(Extent(300f, 150f), listOf(listOf(Point(1f, 2f), Point(3.5f, 4f))))

    private fun exported(tradeId: String = "windows"): ExportedProtocol {
        val id = "pr_" + "a".repeat(32)
        val file = files.protocolFile(project, id).also { it.writeText("%PDF-1.4 protocol") }
        val doc = ProtocolDocument(
            protocolId = id, projectId = project, address = "Musterweg 12", tradeId = tradeId, tradeName = "Fenster & Türen",
            title = "Abnahme", stats = emptyList(), defects = emptyList(), reservation = null, signatures = emptyList(),
            createdAt = "2026-12-18T09:30:00Z", createdLocal = "18.12.2026 10:30",
        )
        return ExportedProtocol(doc, file, "c".repeat(64), files.sha256(file), "{}")
    }

    private val installFault = "CREATE TRIGGER boom BEFORE INSERT ON outbox BEGIN SELECT RAISE(ABORT, 'boom'); END"

    private fun withFault(block: suspend () -> Unit) = runTest {
        db.openHelper.writableDatabase.execSQL(installFault)
        block()
        db.openHelper.writableDatabase.execSQL("DROP TRIGGER boom")
    }

    // ---- StepKeys ----

    @Test
    fun stepKeysPerRoute() {
        assertEquals("shell/element/W1/step/2", StepKeys.of(AppRoute.InspectionStep("shell", "W1", 2)))
        assertEquals("plumbing/check/slope", StepKeys.of(AppRoute.InspectionCheck("plumbing", "slope")))
        assertEquals("tiles/room/bath", StepKeys.of(AppRoute.InspectionRoom("tiles", "bath")))
        assertEquals("painting/check/walls/w1", StepKeys.of(AppRoute.InspectionCheckItem("painting", "walls", "w1")))
        assertEquals("plumbing/check/slope/gefaelle", StepKeys.of(AppRoute.InspectionCheck("plumbing", "slope"), "gefaelle"))
        assertEquals("shell/element/W1/step/2/foto", StepKeys.of(AppRoute.InspectionStep("shell", "W1", 2), "foto"))
        for (route in listOf(
            AppRoute.Trade("a"), AppRoute.Compare("a"), AppRoute.Document("a"), AppRoute.Notifications,
            AppRoute.Inspection("a"), AppRoute.NewDefect, AppRoute.SignOff("a"),
        )) {
            assertNull(route.toString(), StepKeys.of(route))
            assertNull(route.toString(), StepKeys.of(route, "part"))
        }
    }

    @Test
    fun recordIdIs36CharsMaxAndProjectScoped() {
        val a = StepKeys.recordId("musterweg-12", "shell/element/W1/step/2")
        val b = StepKeys.recordId("other-project", "shell/element/W1/step/2")
        assertTrue(a.startsWith("st_"))
        assertEquals(35, a.length)
        assertTrue(a.length <= 36)
        assertNotEquals(a, b)
        assertEquals(a, StepKeys.recordId("musterweg-12", "shell/element/W1/step/2"))
        assertTrue(a.drop(3).all { it in "0123456789abcdef" })
    }

    // ---- steps ----

    @Test
    fun recordStepWritesRecordStampsAndOneOutboxOp() = runTest {
        val key = "shell/element/W1/step/2"
        val values = JsonObject(mapOf("note" to JsonPrimitive("ok")))
        val result = repo.recordStep(StepCapture(key, "shell", StepStatus.DEFECT, values))
        val id = StepKeys.recordId(project, key)
        assertEquals(CaptureResult.Ok(id), result)
        val fields = fieldsOf("steps", id)
        assertEquals(key, fields.getValue("stepKey").jsonPrimitive.content)
        assertEquals("shell", fields.getValue("tradeId").jsonPrimitive.content)
        assertEquals("defect", fields.getValue("status").jsonPrimitive.content)
        assertEquals(values, fields.getValue("values"))
        assertEquals("2026-12-18T09:30:00Z", fields.getValue("capturedAt").jsonPrimitive.content)
        assertEquals(project, record("steps", id)!!.projectId)
        assertEquals(setOf("stepKey", "tradeId", "status", "values", "capturedAt"), stampsOf("steps", id).keys)
        assertEquals(listOf("UPSERT steps/$id"), opSummary())
        assertEquals("1", dataVersion())
        assertEquals(1, outboxCalls)
    }

    @Test
    fun nullStatusAndEmptyValuesAreNotWritten() = runTest {
        val key = "shell/element/W1/step/3"
        repo.recordStep(StepCapture(key, "shell", StepStatus.OK, JsonObject(mapOf("a" to JsonPrimitive(1)))))
        repo.recordStep(StepCapture(key, "shell", null))
        val id = StepKeys.recordId(project, key)
        val fields = fieldsOf("steps", id)
        assertEquals("ok", fields.getValue("status").jsonPrimitive.content)
        assertTrue(fields.getValue("values").jsonObject.containsKey("a"))
    }

    @Test
    fun secondEditCoalescesToTail() = runTest {
        val a = StepCapture("shell/element/W1/step/1", "shell", StepStatus.OK)
        val b = StepCapture("shell/element/W1/step/2", "shell", StepStatus.OK)
        repo.recordStep(a)
        repo.recordStep(b)
        val idA = StepKeys.recordId(project, a.stepKey)
        val firstStamp = stampsOf("steps", idA).getValue("status").jsonObject.getValue("at").jsonPrimitive.content.toLong()
        repo.recordStep(a.copy(status = StepStatus.DEFECT))
        assertEquals(listOf("UPSERT steps/${StepKeys.recordId(project, b.stepKey)}", "UPSERT steps/$idA"), opSummary())
        val secondStamp = stampsOf("steps", idA).getValue("status").jsonObject.getValue("at").jsonPrimitive.content.toLong()
        assertTrue(secondStamp > firstStamp)
        assertEquals("defect", fieldsOf("steps", idA).getValue("status").jsonPrimitive.content)
    }

    // ---- photos ----

    @Test
    fun stepPhotoEnqueuesUploadThenRowsInOrder() = runTest {
        val key = "shell/element/W1/step/2"
        val photoId = "ph_" + "2".repeat(32)
        val file = photoFile(photoId)
        val result = repo.addPhoto(PhotoOwner.Step(key, "shell"), photoId, file)
        assertEquals(CaptureResult.Ok(photoId), result)
        val stepId = StepKeys.recordId(project, key)
        assertEquals(listOf("UPLOAD photos/$photoId", "UPSERT photos/$photoId", "UPSERT steps/$stepId"), opSummary())
        val upload = Json.parseToJsonElement(ops().first().payload).jsonObject
        assertEquals("photos", upload.getValue("bucket").jsonPrimitive.content)
        assertEquals(photoId, upload.getValue("fileId").jsonPrimitive.content)
        assertEquals("projects/$project/photos/$photoId.jpg", upload.getValue("path").jsonPrimitive.content)
        assertEquals("$project/photos/$photoId.jpg", upload.getValue("name").jsonPrimitive.content)
        assertEquals("image/jpeg", upload.getValue("mime").jsonPrimitive.content)
        assertEquals(files.sha256(file), upload.getValue("sha256").jsonPrimitive.content)
        val photo = fieldsOf("photos", photoId)
        assertEquals("steps", photo.getValue("ownerTable").jsonPrimitive.content)
        assertEquals(stepId, photo.getValue("ownerId").jsonPrimitive.content)
        assertEquals(photoId, photo.getValue("fileId").jsonPrimitive.content)
        assertEquals(files.sha256(file), photo.getValue("sha256").jsonPrimitive.content)
        assertEquals(file.length(), photo.getValue("bytes").jsonPrimitive.content.toLong())
        assertEquals("2026-12-18T09:30:00Z", photo.getValue("takenAt").jsonPrimitive.content)
        assertEquals(key, fieldsOf("steps", stepId).getValue("stepKey").jsonPrimitive.content)
        assertEquals("1", dataVersion())
    }

    @Test
    fun missingOrEmptyPhotoFileIsRefusedAndDeleted() = runTest {
        val empty = photoFile("ph_" + "3".repeat(32), "")
        assertEquals(
            CaptureResult.Refused(CaptureRefusal.StorageFailed),
            repo.addPhoto(PhotoOwner.Step("shell/element/W1/step/1", "shell"), "ph_" + "3".repeat(32), empty),
        )
        assertFalse(empty.exists())
        val missing = files.newPhotoFile(project, "ph_" + "4".repeat(32))
        assertEquals(
            CaptureResult.Refused(CaptureRefusal.StorageFailed),
            repo.addPhoto(PhotoOwner.DefectDraft("df_x"), "ph_" + "4".repeat(32), missing),
        )
        assertTrue(ops().isEmpty())
        assertNull(dataVersion())
        assertNull(record("photos", "ph_" + "3".repeat(32)))
    }

    @Test
    fun draftPhotosNeverEnqueueUntilSave() = runTest {
        val draft = draft()
        val p1 = "ph_" + "5".repeat(32)
        val p2 = "ph_" + "6".repeat(32)
        repo.addPhoto(PhotoOwner.DefectDraft(draft.id), p1, photoFile(p1))
        repo.addPhoto(PhotoOwner.DefectDraft(draft.id), p2, photoFile(p2))
        assertTrue(ops().isEmpty())
        assertEquals(0, outboxCalls)
        val photo = fieldsOf("photos", p1)
        assertEquals("defects", photo.getValue("ownerTable").jsonPrimitive.content)
        assertEquals(draft.id, photo.getValue("ownerId").jsonPrimitive.content)

        assertEquals(CaptureResult.Ok(draft.id), repo.saveDefect(draft))
        assertEquals(
            listOf(
                "UPLOAD photos/$p1", "UPSERT photos/$p1", "UPLOAD photos/$p2", "UPSERT photos/$p2",
                "UPSERT defects/${draft.id}",
            ),
            opSummary(),
        )
        assertEquals(1, outboxCalls)
    }

    @Test
    fun discardDraftRemovesRecordsAndFiles() = runTest {
        val p1 = "ph_" + "5".repeat(32)
        val keep = "ph_" + "7".repeat(32)
        val f1 = photoFile(p1)
        val fKeep = photoFile(keep)
        repo.addPhoto(PhotoOwner.DefectDraft("df_a"), p1, f1)
        repo.addPhoto(PhotoOwner.DefectDraft("df_b"), keep, fKeep)
        repo.discardDraft("df_a")
        assertNull(record("photos", p1))
        assertFalse(f1.exists())
        assertTrue(record("photos", keep) != null)
        assertTrue(fKeep.exists())
        assertTrue(ops().isEmpty())
    }

    // ---- defects ----

    @Test
    fun savedDefectDecodesStrictlyAndAppDataStillLoads() = runTest {
        val draft = draft()
        val p1 = "ph_" + "5".repeat(32)
        repo.addPhoto(PhotoOwner.DefectDraft(draft.id), p1, photoFile(p1))
        repo.saveDefect(draft)
        val fields = fieldsOf("defects", draft.id)
        assertEquals("app", fields.getValue("origin").jsonPrimitive.content)
        assertEquals("windows/element/F2/step/1", fields.getValue("stepKey").jsonPrimitive.content)
        val fixtureOnly = JsonObject(fields.filterKeys { it != "origin" && it != "stepKey" })
        FixtureJson.decodeFromJsonElement(Defect.serializer(), fixtureOnly)
        val defects = repo.defects()
        assertEquals(4, defects.size)
        assertEquals(draft.id, defects.last().id)
        assertEquals(0, repo.skippedRecords)
        AppData.load(repo)
        assertEquals(0, repo.skippedRecords)
    }

    @Test
    fun savedDefectMatchesFixtureShape() = runTest {
        val withPhotos = draft()
        val p1 = "ph_" + "5".repeat(32)
        repo.addPhoto(PhotoOwner.DefectDraft(withPhotos.id), p1, photoFile(p1))
        repo.saveDefect(withPhotos)
        val saved = repo.defects().last()
        assertEquals(PlatformText("Fenster & Türen", "FENSTER & TÜREN"), saved.trade)
        assertEquals("windows", saved.tradeId)
        assertEquals("2027-01-15", saved.deadline)
        assertEquals("Frist 15.01.2027", saved.deadlineLabel)
        assertEquals("noch 28 Tage", saved.dueLabel)
        assertEquals("later", saved.urgency)
        assertEquals(1, saved.photos)
        assertEquals("Frist 15.01.2027 · 1 Foto", saved.meta)
        assertEquals("Mittel", saved.severity)
        assertEquals("Ca. 3 cm lang.", saved.description)
        assertEquals("F2", saved.elementId)
        assertEquals("Fensterbau Weber GmbH", saved.responsible)
        assertEquals(true, saved.notifyByEmail)
        assertEquals("Kratzer im Glas", saved.title)
        assertEquals(3L, record("defects", withPhotos.id)!!.sortKey)

        val noPhotos = draft(id = "df_" + "9".repeat(32), tradeId = "plumbing").copy(elementId = null, stepKey = null)
        repo.saveDefect(noPhotos)
        val second = repo.defects().last()
        assertEquals(PlatformText("Sanitär & Heizung", "SANITÄR & HEIZUNG"), second.trade)
        assertNull(second.photos)
        assertEquals("Frist 15.01.2027", second.meta)
        assertNull(second.elementId)
        assertEquals(record("defects", withPhotos.id)!!.sortKey + 1, record("defects", noPhotos.id)!!.sortKey)

        val two = draft(id = "df_" + "8".repeat(32))
        for (n in listOf("a", "b")) {
            val id = "ph_" + n.repeat(32)
            repo.addPhoto(PhotoOwner.DefectDraft(two.id), id, photoFile(id))
        }
        repo.saveDefect(two)
        assertEquals("Frist 15.01.2027 · 2 Fotos", repo.defects().last().meta)
    }

    @Test
    fun dueLabelAndUrgencyByDeadline() = runTest {
        val expected = mapOf(
            -3L to Pair("3 Tage überfällig", "late"),
            -1L to Pair("1 Tag überfällig", "late"),
            0L to Pair("heute fällig", "soon"),
            1L to Pair("noch 1 Tag", "soon"),
            21L to Pair("noch 21 Tage", "soon"),
            22L to Pair("noch 22 Tage", "later"),
            28L to Pair("noch 28 Tage", "later"),
        )
        for ((days, pair) in expected) {
            assertEquals("days $days", pair.first, DueLabels.dueLabel(days))
            assertEquals("days $days", pair.second, DueLabels.urgency(days))
        }
        // the three fixture defects, reproduced through saveDefect against meta.now (2026-12-18)
        val fixtureDefects = repo.defects()
        for (existing in fixtureDefects) {
            val copy = draft(id = "df_" + existing.id.hashCode().toUInt().toString(16).padStart(32, '0'), tradeId = existing.tradeId)
                .copy(deadline = LocalDate.parse(existing.deadline))
            repo.saveDefect(copy)
            val saved = repo.defects().first { it.id == copy.id }
            assertEquals(existing.dueLabel, saved.dueLabel)
            assertEquals(existing.urgency, saved.urgency)
            assertEquals(existing.deadlineLabel, saved.deadlineLabel)
        }
    }

    // ---- sign-off ----

    @Test
    fun emptySignatureRefusedWritesNothing() = runTest {
        val before = dump()
        val protocol = exported()
        val empty = SignOffCapture("windows", "Max Muster", SignatureStrokes(Extent(300f, 150f)))
        assertEquals(CaptureResult.Refused(CaptureRefusal.EmptySignature), repo.saveSignOff(empty, protocol))
        assertEquals(before, dump())
        assertTrue(ops().isEmpty())
        assertNull(dataVersion())
        assertFalse(protocol.file.exists())
        assertEquals(0, outboxCalls)
    }

    @Test
    fun signOffWritesSignatureProtocolInspectionAndFourOps() = runTest {
        val protocol = exported()
        val result = repo.saveSignOff(SignOffCapture("windows", "Max Muster", strokes()), protocol)
        assertEquals(CaptureResult.Ok(protocol.doc.protocolId), result)
        val signatureId = db.records().list("signatures", project).single().id
        assertTrue(signatureId.startsWith("sg_"))
        assertEquals(35, signatureId.length)
        val signature = fieldsOf("signatures", signatureId)
        assertEquals("windows", signature.getValue("inspectionId").jsonPrimitive.content)
        assertEquals("owner", signature.getValue("role").jsonPrimitive.content)
        assertEquals("Max Muster", signature.getValue("name").jsonPrimitive.content)
        assertEquals("2026-12-18T09:30:00Z", signature.getValue("signedAt").jsonPrimitive.content)
        val strokes = signature.getValue("strokes").jsonObject
        assertEquals(300.0, strokes.getValue("w").jsonPrimitive.content.toDouble(), 0.0)
        assertEquals(150.0, strokes.getValue("h").jsonPrimitive.content.toDouble(), 0.0)
        assertEquals(
            JsonArray(listOf(JsonArray(listOf(1.0, 2.0, 3.5, 4.0).map { JsonPrimitive(it) }))),
            strokes.getValue("strokes").jsonArray.let { s ->
                JsonArray(s.map { JsonArray(it.jsonArray.map { v -> JsonPrimitive(v.jsonPrimitive.content.toDouble()) }) })
            },
        )
        val protocolFields = fieldsOf("protocols", protocol.doc.protocolId)
        assertEquals("windows", protocolFields.getValue("inspectionId").jsonPrimitive.content)
        assertEquals(protocol.doc.protocolId, protocolFields.getValue("fileId").jsonPrimitive.content)
        assertEquals(protocol.fileSha256, protocolFields.getValue("fileSha256").jsonPrimitive.content)
        assertEquals(protocol.contentSha256, protocolFields.getValue("contentSha256").jsonPrimitive.content)
        assertEquals(protocol.contentJson, protocolFields.getValue("contentJson").jsonPrimitive.content)
        assertEquals("2026-12-18T09:30:00Z", protocolFields.getValue("createdAt").jsonPrimitive.content)
        val inspection = fieldsOf("inspections", "windows")
        assertEquals("2026-12-18T09:30:00Z", inspection.getValue("signedAt").jsonPrimitive.content)
        assertEquals(protocol.doc.protocolId, inspection.getValue("protocolId").jsonPrimitive.content)
        assertEquals("windows", inspection.getValue("tradeId").jsonPrimitive.content)
        assertEquals(
            listOf(
                "UPLOAD pdfs/${protocol.doc.protocolId}", "UPSERT signatures/$signatureId",
                "UPSERT protocols/${protocol.doc.protocolId}", "UPSERT inspections/windows",
            ),
            opSummary(),
        )
        val upload = Json.parseToJsonElement(ops().first().payload).jsonObject
        assertEquals("pdfs", upload.getValue("bucket").jsonPrimitive.content)
        assertEquals("projects/$project/protocols/${protocol.doc.protocolId}.pdf", upload.getValue("path").jsonPrimitive.content)
        assertEquals("$project/protocols/${protocol.doc.protocolId}.pdf", upload.getValue("name").jsonPrimitive.content)
        assertEquals("application/pdf", upload.getValue("mime").jsonPrimitive.content)
        assertEquals(protocol.fileSha256, upload.getValue("sha256").jsonPrimitive.content)
        assertTrue(protocol.file.exists())
        assertEquals("1", dataVersion())
        repo.inspections()
        assertEquals(0, repo.skippedRecords)
    }

    @Test
    fun failedSignOffTransactionDeletesPdf() = withFault {
        val protocol = exported()
        val before = dump()
        assertEquals(
            CaptureResult.Refused(CaptureRefusal.StorageFailed),
            repo.saveSignOff(SignOffCapture("windows", "Max Muster", strokes()), protocol),
        )
        assertFalse(protocol.file.exists())
        assertEquals(before, dump())
        assertTrue(ops().isEmpty())
    }

    // ---- import ----

    @Test
    fun importRefusesNonPdf() = runTest {
        val before = dump()
        val result = repo.importDocument(source("x.pdf", "hello world".toByteArray()), ImportRequest(null, "plan"))
        assertEquals(CaptureResult.Refused(CaptureRefusal.NotPdf), result)
        assertEquals(CaptureResult.Refused(CaptureRefusal.NotPdf), repo.importDocument(source("e.pdf", ByteArray(0)), ImportRequest(null, "plan")))
        assertEquals(CaptureResult.Refused(CaptureRefusal.NotPdf), repo.importDocument(source("s.pdf", "%PDF".toByteArray()), ImportRequest(null, "plan")))
        assertEquals(before, dump())
        assertTrue(ops().isEmpty())
        assertNull(dataVersion())
        assertTrue(files.projectDir(project).walkTopDown().none { it.isFile })
    }

    @Test
    fun importRefusesOver50MiB() = runTest {
        val limit = 52_428_800L
        val big = object : ImportSource {
            override val displayName: String? = "big.pdf"
            override fun open(): InputStream = object : InputStream() {
                private var position = 0L
                private val header = "%PDF-".toByteArray()
                override fun read(): Int {
                    if (position >= limit + 1) return -1
                    val byte = if (position < header.size) header[position.toInt()].toInt() else 0
                    position++
                    return byte
                }
            }
        }
        val before = dump()
        assertEquals(CaptureResult.Refused(CaptureRefusal.TooLarge), repo.importDocument(big, ImportRequest(null, "plan")))
        assertEquals(before, dump())
        assertTrue(ops().isEmpty())
        assertTrue(files.projectDir(project).walkTopDown().none { it.isFile })
    }

    @Test
    fun importStreamFailureLeavesNothing() = runTest {
        val failing = object : ImportSource {
            override val displayName: String? = "x.pdf"
            override fun open(): InputStream = object : InputStream() {
                private var n = 0
                override fun read(): Int = if (n < 5) "%PDF-"[n++].code else throw IOException("gone")
            }
        }
        assertEquals(CaptureResult.Refused(CaptureRefusal.StorageFailed), repo.importDocument(failing, ImportRequest(null, "plan")))
        assertTrue(ops().isEmpty())
        assertTrue(files.projectDir(project).walkTopDown().none { it.isFile })
    }

    @Test
    fun importFromGewerkAssignsTrade() = runTest {
        val result = repo.importDocument(source("Angebot Weber.PDF", pdfBytes), ImportRequest("windows", "offer"))
        val id = (result as CaptureResult.Ok).id
        assertTrue(id.startsWith("dc_"))
        assertEquals(35, id.length)
        val item = repo.documents().items.last()
        assertEquals(id, item.id)
        assertEquals("offer", item.kind)
        assertEquals("windows", item.tradeId)
        assertEquals("Angebot Weber", item.title)
        assertEquals(PlatformText(null, "PDF · 18.12.2026"), item.meta)
        assertNull(item.amount)
        assertNull(item.due)
        assertNull(item.statusLabel)
        assertNull(item.viewer)
        val fields = fieldsOf("documents", id)
        assertEquals(id, fields.getValue("fileId").jsonPrimitive.content)
        val file = files.documentFile(project, id)
        assertEquals(files.sha256(file), fields.getValue("fileSha256").jsonPrimitive.content)
        assertEquals(pdfBytes.size.toLong(), fields.getValue("sizeBytes").jsonPrimitive.content.toLong())
        assertEquals("import", fields.getValue("origin").jsonPrimitive.content)
        assertEquals("2026-12-18T09:30:00Z", fields.getValue("importedAt").jsonPrimitive.content)
        assertEquals(listOf("UPLOAD pdfs/$id", "UPSERT documents/$id"), opSummary())
        val upload = Json.parseToJsonElement(ops().first().payload).jsonObject
        assertEquals("projects/$project/documents/$id.pdf", upload.getValue("path").jsonPrimitive.content)
        assertEquals("$project/documents/$id.pdf", upload.getValue("name").jsonPrimitive.content)
        assertEquals("application/pdf", upload.getValue("mime").jsonPrimitive.content)
        assertTrue(file.isFile)
        assertTrue(file.parentFile!!.listFiles()!!.all { it.extension == "pdf" })
        assertEquals(0, repo.skippedRecords)

        val untitled = repo.importDocument(source(null, pdfBytes), ImportRequest(null, "plan")) as CaptureResult.Ok
        val second = repo.documents().items.first { it.id == untitled.id }
        assertEquals("Dokument", second.title)
        assertNull(second.tradeId)
    }

    @Test
    fun importedDocumentsRaiseCounts() = runTest {
        val base = repo.documents()
        repo.importDocument(source("a.pdf", pdfBytes), ImportRequest("windows", "offer"))
        repo.importDocument(source("b.pdf", pdfBytes), ImportRequest(null, "plan"))
        val after = repo.documents()
        assertEquals(base.counts.all + 2, after.counts.all)
        assertEquals(base.counts.offer + 1, after.counts.offer)
        assertEquals(base.counts.plan + 1, after.counts.plan)
        assertEquals(base.counts.contract, after.counts.contract)
        assertEquals(base.counts.trades, after.counts.trades)
        assertEquals((base.counts.offer + 1).toString(), after.categories.first { it.kind == "offer" }.count)
        assertEquals((base.counts.plan + 1).toString(), after.categories.first { it.kind == "plan" }.count)
        // The frozen filter chip text ("Angebote 7") must not make the whole app fail to load after an import.
        AppData.load(repo)
    }

    // ---- members ----

    @Test
    fun ensureMemberIdempotent() = runTest {
        repo.ensureMember("user-1", "bauherr")
        val id = "m_" + de.baupilot.protocol.ProtocolHash.sha256Hex("$project/user-1".toByteArray()).take(32)
        val fields = fieldsOf("members", id)
        assertEquals("user-1", fields.getValue("userId").jsonPrimitive.content)
        assertEquals("bauherr", fields.getValue("role").jsonPrimitive.content)
        assertEquals(listOf("UPSERT members/$id"), opSummary())
        val version = dataVersion()
        val calls = outboxCalls
        val stamps = stampsOf("members", id)
        repo.ensureMember("user-1", "bauherr")
        assertEquals(listOf("UPSERT members/$id"), opSummary())
        assertEquals(version, dataVersion())
        assertEquals(calls, outboxCalls)
        assertEquals(stamps, stampsOf("members", id))
        repo.ensureMember("user-1", "architekt")
        assertEquals("architekt", fieldsOf("members", id).getValue("role").jsonPrimitive.content)
        assertNotEquals(version, dataVersion())
    }

    // ---- deleting projects ----

    private suspend fun populate() {
        repo.recordStep(StepCapture("shell/element/W1/step/1", "shell", StepStatus.OK))
        val p = "ph_" + "5".repeat(32)
        repo.addPhoto(PhotoOwner.Step("shell/element/W1/step/1", "shell"), p, photoFile(p))
        repo.importDocument(source("a.pdf", pdfBytes), ImportRequest(null, "plan"))
        repo.saveSignOff(SignOffCapture("windows", "Max", strokes()), exported())
    }

    @Test
    fun deleteProjectRemovesRecordsFilesAndLeavesOnlyDeleteOp() = runTest {
        populate()
        assertTrue(files.projectDir(project).exists())
        assertEquals(CaptureResult.Ok(project), repo.deleteProject(project))
        assertEquals(0L, scalar("SELECT COUNT(*) FROM records WHERE projectId = ?", project))
        assertFalse(files.projectDir(project).exists())
        val remaining = ops()
        assertEquals(1, remaining.size)
        assertEquals(OutboxKind.DELETE_PROJECT, remaining.single().kind)
        assertEquals(project, remaining.single().rowId)
        assertEquals(project, remaining.single().projectId)
        assertNull(remaining.single().tableId)
        assertTrue(outboxCalls > 0)
    }

    @Test
    fun deleteUnknownProjectRefused() = runTest {
        populate()
        val before = dump()
        val opsBefore = opSummary()
        assertEquals(CaptureResult.Refused(CaptureRefusal.UnknownProject), repo.deleteProject("nope"))
        assertEquals(before, dump())
        assertEquals(opsBefore, opSummary())
        assertTrue(files.projectDir(project).exists())
    }

    @Test
    fun purgeProjectEnqueuesNothing() = runTest {
        populate()
        val calls = outboxCalls
        repo.purgeProject(project)
        assertEquals(0L, scalar("SELECT COUNT(*) FROM records WHERE projectId = ?", project))
        assertTrue(ops().isEmpty())
        assertFalse(files.projectDir(project).exists())
        assertEquals(calls, outboxCalls)
    }

    // ---- atomicity ----

    @Test
    fun everyWriteIsAtomic() = withFault {
        val stepKey = "shell/element/W1/step/1"
        val photoId = "ph_" + "5".repeat(32)
        val photo = photoFile(photoId)
        val draftPhoto = "ph_" + "6".repeat(32)
        // a draft photo needs no op, so it can be written under the fault; saving the defect then fails
        repo.addPhoto(PhotoOwner.DefectDraft("df_f"), draftPhoto, photoFile(draftPhoto))
        val before = dump()
        val version = dataVersion()
        val failed = CaptureResult.Refused(CaptureRefusal.StorageFailed)

        assertEquals(failed, repo.recordStep(StepCapture(stepKey, "shell", StepStatus.OK)))
        assertEquals(failed, repo.addPhoto(PhotoOwner.Step(stepKey, "shell"), photoId, photo))
        assertEquals(failed, repo.saveDefect(draft(id = "df_f")))
        assertEquals(failed, repo.importDocument(source("a.pdf", pdfBytes), ImportRequest(null, "plan")))
        assertEquals(failed, repo.deleteProject(project))
        val thrown = runCatching { repo.ensureMember("user-1", "bauherr") }.exceptionOrNull()
        assertTrue("expected Storage but got $thrown", thrown is RepositoryException.Storage)

        assertEquals(before, dump())
        assertEquals(version, dataVersion())
        assertTrue(ops().isEmpty())
        assertEquals(0, outboxCalls)
        assertTrue(files.projectDir(project).exists())
        assertTrue(files.newPhotoFile(project, draftPhoto).exists())
        assertTrue(files.projectDir(project).walkTopDown().none { it.extension == "pdf" })
    }

    @Test
    fun dataVersionFlowAdvancesWithEachWrite() = runTest {
        assertEquals(0L, repo.dataVersion.first())
        repo.recordStep(StepCapture("shell/element/W1/step/1", "shell", StepStatus.OK))
        repo.recordStep(StepCapture("shell/element/W1/step/2", "shell", StepStatus.OK))
        assertEquals(2L, repo.dataVersion.first())
    }

    // ---- races (M5, M6) and idempotence (M7) ----

    /** A repository whose clock runs [hook] once, at the `clock.instant()` that `addPhoto` calls before its transaction. */
    private fun repoWithHook(hook: suspend () -> Unit): LocalRepository {
        var fired = false
        val hooked = object : Clock() {
            override fun getZone() = clock.zone
            override fun withZone(zone: java.time.ZoneId): Clock = this
            override fun instant(): Instant {
                if (!fired) {
                    fired = true
                    runBlocking { hook() }
                }
                return clock.instant()
            }
        }
        return LocalRepository(db, { fixture }, files, StampClock(clock, "dev") { 0L }, hooked) { outboxCalls++ }
    }

    @Test
    fun photoRacingSaveDefectIsQueuedWithItsDefect() = runTest {
        val d = draft()
        val racing = repoWithHook { repo.saveDefect(d) }
        val photoId = "ph_" + "7".repeat(32)
        val result = racing.addPhoto(PhotoOwner.DefectDraft(d.id), photoId, photoFile(photoId))
        assertEquals(CaptureResult.Ok(photoId), result)
        assertTrue(opSummary().containsAll(listOf("UPLOAD photos/$photoId", "UPSERT photos/$photoId", "UPSERT defects/${d.id}")))
        val fields = fieldsOf("defects", d.id)
        assertEquals(1, fields.getValue("photos").jsonPrimitive.content.toInt())
        assertTrue(fields.getValue("meta").jsonPrimitive.content.endsWith(" · 1 Foto"))
    }

    @Test
    fun photoArrivingAfterDiscardIsRefusedAndItsFileDeleted() = runTest {
        val d = draft()
        val racing = repoWithHook { repo.discardDraft(d.id) }
        val photoId = "ph_" + "8".repeat(32)
        val file = photoFile(photoId)
        val result = racing.addPhoto(PhotoOwner.DefectDraft(d.id), photoId, file)
        assertEquals(CaptureResult.Refused(CaptureRefusal.UnknownProject), result)
        assertFalse(file.exists())
        assertNull(record("photos", photoId))
        assertTrue(ops().isEmpty())
    }

    @Test
    fun captureRacingAPurgeIsRefusedAndWritesNothing() = runTest {
        val racing = repoWithHook { repo.purgeProject(project) }
        val photoId = "ph_" + "9".repeat(32)
        val file = photoFile(photoId)
        val result = racing.addPhoto(PhotoOwner.Step("shell/element/W1/step/1", "shell"), photoId, file)
        assertEquals(CaptureResult.Refused(CaptureRefusal.UnknownProject), result)
        assertEquals(0L, scalar("SELECT COUNT(*) FROM records WHERE projectId = ?", project))
        assertTrue(ops().isEmpty())
        assertFalse(file.exists())
    }

    /** The project's rows vanish after the write passed its first check, as a purge by the sync engine would do. */
    private fun purgeBeforeNextTransaction() {
        beforeTransaction = {
            runBlocking {
                db.records().deleteProject(project)
                db.outbox().deleteForProject(project)
            }
        }
    }

    private suspend fun assertNothingWritten() {
        assertEquals(0L, scalar("SELECT COUNT(*) FROM records WHERE projectId = ?", project))
        assertTrue(ops().isEmpty())
    }

    @Test
    fun recordStepRacingAPurgeIsRefusedAndWritesNothing() = runTest {
        purgeBeforeNextTransaction()
        val result = repo.recordStep(StepCapture("shell/element/W1/step/1", "shell", StepStatus.OK))
        assertEquals(CaptureResult.Refused(CaptureRefusal.UnknownProject), result)
        assertNothingWritten()
    }

    @Test
    fun saveDefectRacingAPurgeIsRefusedAndWritesNothing() = runTest {
        purgeBeforeNextTransaction()
        assertEquals(CaptureResult.Refused(CaptureRefusal.UnknownProject), repo.saveDefect(draft()))
        assertNothingWritten()
    }

    @Test
    fun importDocumentRacingAPurgeIsRefusedAndLeavesNoFile() = runTest {
        purgeBeforeNextTransaction()
        val result = repo.importDocument(source("a.pdf", pdfBytes), ImportRequest("windows", "offer"))
        assertEquals(CaptureResult.Refused(CaptureRefusal.UnknownProject), result)
        assertNothingWritten()
        assertEquals(emptyList<String>(), File(files.projectDir(project), "documents").listFiles().orEmpty().map { it.name })
    }

    @Test
    fun ensureMemberRacingAPurgeWritesNothing() = runTest {
        purgeBeforeNextTransaction()
        repo.ensureMember("user-1", "bauherr")
        assertNothingWritten()
        assertEquals(0, outboxCalls)
    }

    @Test
    fun photoOutsideTheProjectDirectoryIsRefusedAndDeleted() = runTest {
        val photoId = "ph_" + "5".repeat(32)
        val stray = files.newPhotoFile("another-project", photoId).also { it.writeText("jpeg-bytes") }
        val result = repo.addPhoto(PhotoOwner.Step("shell/element/W1/step/1", "shell"), photoId, stray)
        assertEquals(CaptureResult.Refused(CaptureRefusal.StorageFailed), result)
        assertFalse(stray.exists())
        assertNull(record("photos", photoId))
        assertTrue(ops().isEmpty())
    }

    @Test
    fun aRemoteProjectIdChangeDoesNotMoveCapturedPhotosOutOfTheSweep() = runTest {
        val entity = record("projects", project)!!
        val fields = de.baupilot.data.local.Converters.stringToFields(entity.fields)
        db.records().upsert(entity.copy(fields = JsonObject(fields + ("id" to JsonPrimitive("elsewhere"))).toString()))
        val photoId = "ph_" + "4".repeat(32)
        val file = files.newPhotoFile(repo.project().id, photoId).also { it.writeText("jpeg-bytes") }
        assertEquals(CaptureResult.Ok(photoId), repo.addPhoto(PhotoOwner.Step("shell/element/W1/step/1", "shell"), photoId, file))
        files.sweepOrphans(setOf(project, files.relative(file)), 0L, Long.MAX_VALUE / 2)
        assertTrue(file.exists())
    }

    @Test
    fun secondSignOffOfASignedInspectionWritesNothingMore() = runTest {
        val first = exported()
        assertEquals(CaptureResult.Ok(first.doc.protocolId), repo.saveSignOff(SignOffCapture("windows", "Max", strokes()), first))
        val before = dump()
        val opsBefore = ops().size
        val again = exported()
        assertEquals(CaptureResult.Ok(first.doc.protocolId), repo.saveSignOff(SignOffCapture("windows", "Max", strokes()), again))
        assertEquals(before, dump())
        assertEquals(opsBefore, ops().size)
        assertEquals(1, db.records().list("signatures", project).size)
    }

    @Test
    fun writeToATombstonedRecordBringsItBack() = runTest {
        repo.ensureMember("user-1", "bauherr")
        val id = "m_" + de.baupilot.protocol.ProtocolHash.sha256Hex("$project/user-1".toByteArray()).take(32)
        val tomb = de.baupilot.data.local.Converters.toRecord(record("members", id)!!).copy(deleted = true)
        db.records().upsert(de.baupilot.data.local.Converters.toEntity(tomb))
        repo.ensureMember("user-1", "bauherr")
        assertFalse(record("members", id)!!.deleted)
    }
}
