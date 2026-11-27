package de.baupilot.data.local

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.room.withTransaction
import de.baupilot.app.DefectUrgency
import de.baupilot.app.InspectionStatus
import de.baupilot.app.NotificationKind
import de.baupilot.app.RouteParser
import de.baupilot.capture.CaptureRefusal
import de.baupilot.capture.CaptureRepository
import de.baupilot.capture.CaptureResult
import de.baupilot.capture.DefectDraft
import de.baupilot.capture.DueLabels
import de.baupilot.capture.ExifScrubber
import de.baupilot.capture.FileStore
import de.baupilot.capture.ImportRequest
import de.baupilot.capture.ImportSource
import de.baupilot.capture.PhotoOwner
import de.baupilot.capture.SignOffCapture
import de.baupilot.capture.StepCapture
import de.baupilot.capture.StepKeys
import de.baupilot.data.Comparison
import de.baupilot.data.Defect
import de.baupilot.data.DocumentCategory
import de.baupilot.data.DocumentCounts
import de.baupilot.data.DocumentItem
import de.baupilot.data.Documents
import de.baupilot.data.FixtureJson
import de.baupilot.data.PlatformText
import de.baupilot.data.NotificationGroup
import de.baupilot.data.NotificationItem
import de.baupilot.data.Offer
import de.baupilot.data.OverviewRow
import de.baupilot.data.Project
import de.baupilot.data.RepositoryException
import de.baupilot.data.ResolvedDefects
import de.baupilot.data.Schedule
import de.baupilot.data.Trade
import de.baupilot.data.BackendRepository
import de.baupilot.inspection.DeadlineMath
import de.baupilot.protocol.ExportedProtocol
import de.baupilot.protocol.ProtocolHash
import de.baupilot.screens.ScreenCopy
import de.baupilot.sync.FieldMerge
import de.baupilot.sync.StampClock
import de.baupilot.sync.SyncBucket
import de.baupilot.sync.SyncRecord
import de.baupilot.sync.SyncSchema
import de.baupilot.sync.SyncTable
import java.io.File
import java.io.IOException
import java.time.Clock
import java.time.DateTimeException
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

/**
 * Room-backed store. Static design content comes from the bundled fixture (D3); records are decoded with
 * the fixture DTO serializers on `fixtureFields` only (I7), and a record that fails to decode is skipped (D26).
 */
class LocalRepository(
    private val db: BaupilotDatabase,
    private val fixtureJson: () -> String,
    private val files: FileStore,
    private val stamps: StampClock,
    private val clock: Clock,
    private val onOutboxChanged: () -> Unit,
) : BackendRepository, CaptureRepository {
    private var cachedSource: FixtureSource? = null
    private val skipped: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /** Number of records the reads currently skip because they fail strict decoding (D26). */
    val skippedRecords: Int get() = skipped.size

    /** Bumped whenever the stored data changes; the UI reloads on a new value. */
    val dataVersion: Flow<Long> = db.state().observe(DATA_VERSION_KEY).map { it?.toLongOrNull() ?: 0L }

    /** Seeds the D3 content from the bundled fixture. Idempotent; see [FixtureSeeder]. */
    suspend fun ensureSeeded() {
        val source = source()
        storage { FixtureSeeder.seed(db, source) }
    }

    override suspend fun now(): LocalDate = try {
        LocalDate.parse(source().fixture.meta.now)
    } catch (e: DateTimeException) {
        throw RepositoryException.Decoding("meta.now")
    }

    /**
     * The decodable project record. A project whose record cannot be decoded, or whose phase names a trade that is
     * missing, falls back to the seeded fixture project (I17: remote data must not blank the app). Its id is always the
     * record id: `projects.id` is a synced column a remote client can rewrite, and the id keys the project's file
     * directory and the protocol documents.
     */
    override suspend fun project(): Project {
        val projectId = activeProjectId()
        val decoded = decodeRecords(SyncTable.Projects, Project.serializer()).firstOrNull()?.value?.copy(id = projectId)
        val fallback = source().fixture.project.copy(id = projectId)
        val project = decoded ?: fallback
        val tradeIds = trades().map { it.id }
        if (project.phase.tradeId in tradeIds) return project
        return if (decoded != null && fallback.phase.tradeId in tradeIds) decoded.copy(phase = fallback.phase) else project
    }

    /**
     * The trades the app can show. A trade that claims a detail the screens cannot lay out (no status label, more facts
     * than icons) is skipped and counted. The trade named by the fixture's phase always exists: when a remote edit
     * deleted or broke it, the fixture's own trade stands in, so the project's phase title never names nothing (I17).
     */
    override suspend fun trades(): List<Trade> {
        val live = decodeRecords(SyncTable.Trades, Trade.serializer()) { record ->
            val detail = record.value.detail
            when {
                detail == null -> null
                record.value.statusLabel == null -> "trade detail without status label"
                detail.facts.size > ScreenCopy.Gewerk.FACT_ICONS.size -> "trade detail with too many facts"
                else -> null
            }
        }.map { it.value }
        val phaseTrade = source().fixture.project.phase.tradeId
        if (live.any { it.id == phaseTrade }) return live
        val standIn = source().fixture.trades.firstOrNull { it.id == phaseTrade } ?: return live
        return live + standIn
    }

    override suspend fun schedule(): Schedule = source().fixture.schedule

    override suspend fun offers(tradeId: String?): List<Offer> {
        val all = liveOffers()
        return if (tradeId == null) all else all.filter { it.tradeId == tradeId }
    }

    /** The fixture comparison, or `null` when one of its offers is gone or skipped (the Vergleich link then disappears). */
    override suspend fun comparison(tradeId: String): Comparison? {
        val comparison = source().fixture.comparisons.firstOrNull { it.tradeId == tradeId } ?: return null
        val ids = liveOffers().map { it.id }.toSet()
        return comparison.takeIf { c -> c.offerIds.all { it in ids } }
    }

    /** Offers the app can show: an offer without Android meta text is skipped and counted (D26). */
    private suspend fun liveOffers(): List<Offer> =
        decodeRecords(SyncTable.Offers, Offer.serializer()) { record ->
            if (record.value.meta.value == null) "offer without android meta" else null
        }.map { it.value }

    override suspend fun documents(): Documents {
        val base = source().fixture.documents
        val items = decodeRecords(SyncTable.Documents, DocumentItem.serializer()) { record ->
            val viewer = record.value.viewer
            when {
                record.value.meta.value == null -> "document without android meta"
                record.value.kind !in DOCUMENT_KINDS -> "document of an unknown kind"
                viewer != null && viewer.facts.size < VIEWER_FACTS -> "viewer with fewer than $VIEWER_FACTS facts"
                viewer != null && viewer.subtitle.value == null -> "viewer without android subtitle"
                else -> null
            }
        }
        val imported = items.filter { it.fields["origin"].asContent() == ORIGIN_IMPORT }.map { it.value.kind }
        fun importedOf(kind: String) = imported.count { it == kind }
        val counts = DocumentCounts(
            all = base.counts.all + imported.size,
            offer = base.counts.offer + importedOf("offer"),
            contract = base.counts.contract + importedOf("contract"),
            invoice = base.counts.invoice + importedOf("invoice"),
            plan = base.counts.plan + importedOf("plan"),
            trades = base.counts.trades,
        )
        val categories = base.categories.map { category ->
            val sum = when (category.kind) {
                "offer" -> counts.offer
                "contract" -> counts.contract
                "invoice" -> counts.invoice
                "plan" -> counts.plan
                else -> return@map category
            }
            DocumentCategory(category.kind, category.label, sum.toString())
        }
        return Documents(counts, categories, items.map { it.value })
    }

    override suspend fun defects(): List<Defect> =
        decodeRecords(SyncTable.Defects, Defect.serializer()) { record ->
            when {
                record.value.trade.value == null -> "defect without android trade"
                !decodes { DefectUrgency.of(record.value.urgency, "defects.urgency") } -> "defect with an unknown urgency"
                else -> null
            }
        }.map { it.value }

    override suspend fun resolvedDefects(): ResolvedDefects = source().fixture.resolvedDefects

    override suspend fun notifications(): List<NotificationGroup> {
        val groups = LinkedHashMap<String, MutableList<NotificationItem>>()
        val accepted = decodeRecords(SyncTable.Notifications, NotificationItem.serializer()) { record ->
            when {
                record.fields["group"].asContent() == null -> "missing group"
                !decodes { NotificationKind.of(record.value.kind, "notifications.items.kind") } -> "unknown notification kind"
                !decodes { RouteParser.target(record.value.target, "notifications.items.target") } -> "malformed notification target"
                else -> null
            }
        }
        for (record in accepted) {
            groups.getOrPut(record.fields["group"].asContent()!!) { ArrayList() } += record.value
        }
        return groups.map { (title, items) -> NotificationGroup(title, items) }
    }

    override suspend fun inspections(): JsonElement {
        val base = source().fixture.inspections.jsonObject
        val overview = base["overview"]?.jsonObject ?: throw RepositoryException.Decoding("inspections.overview")
        val statusLabels = overview["statusLabels"] as? JsonObject
        val fixtureRows = (overview["rows"] as? JsonArray).orEmpty().map { it.jsonObject }
        val live = decodeRecords(SyncTable.Inspections, OverviewRow.serializer()) { record ->
            val row = record.value
            when {
                row.sub.value == null -> "overview row without android sub"
                !decodes { InspectionStatus.of(row.status, "overview.status") } -> "overview row with an unknown status"
                statusLabels?.get(row.status) == null -> "overview row whose status has no label"
                !decodes { RouteParser.route(row.route, "overview.route") } -> "overview row with a malformed route"
                else -> null
            }
        }.map { it.fixtureFields }
        // The protocol screens need an accepted or reserved row of their trade: another status, a missing or a skipped
        // row is replaced by the fixture's row of that trade (I17).
        val rows = ArrayList(live)
        for (tradeId in ScreenCopy.Protocol.TRADES) {
            val usable = rows.any { it["tradeId"].asContent() == tradeId && hasBanner(it["status"].asContent()) }
            if (usable) continue
            val fixtureRow = fixtureRows.firstOrNull { it["tradeId"].asContent() == tradeId } ?: continue
            val at = rows.indexOfFirst { it["tradeId"].asContent() == tradeId }
            rows.removeAll { it["tradeId"].asContent() == tradeId }
            rows.add(if (at >= 0) minOf(at, rows.size) else minOf(fixtureRows.indexOf(fixtureRow), rows.size), fixtureRow)
        }
        return JsonObject(base + ("overview" to JsonObject(overview + ("rows" to JsonArray(rows)))))
    }

    private fun hasBanner(status: String?): Boolean =
        status != null && decodes { InspectionStatus.of(status, "overview.status") } &&
            ScreenCopy.Protocol.bannerTone(InspectionStatus.of(status, "overview.status")) != null

    /** True when [check] passes a decoder that signals a malformed value with [RepositoryException.Decoding]. */
    private inline fun decodes(check: () -> Any?): Boolean = try {
        check()
        true
    } catch (e: RepositoryException.Decoding) {
        false
    }

    override suspend fun measurements(): JsonElement = source().fixture.measurements

    override suspend fun screen(name: String): JsonElement? =
        (source().fixture.screens as? JsonObject)?.get(name)

    // ---- write side (CaptureRepository): one Room transaction per write (I3) ----

    override suspend fun recordStep(capture: StepCapture): CaptureResult = refusable {
        val projectId = activeProjectId()
        val id = StepKeys.recordId(projectId, capture.stepKey)
        transaction {
            requireProject(projectId)
            val changes = linkedMapOf<String, JsonElement>(
                "stepKey" to JsonPrimitive(capture.stepKey),
                "tradeId" to JsonPrimitive(capture.tradeId),
                "capturedAt" to JsonPrimitive(clock.instant().toString()),
            )
            capture.status?.let { changes["status"] = JsonPrimitive(it.name.lowercase(Locale.ROOT)) }
            if (capture.values.isNotEmpty()) changes["values"] = capture.values
            put(SyncTable.Steps, id, projectId, changes)
            enqueueUpsert(SyncTable.Steps, id, projectId)
            bumpDataVersion()
        }
        onOutboxChanged()
        CaptureResult.Ok(id)
    }

    override suspend fun addPhoto(owner: PhotoOwner, photoId: String, file: File): CaptureResult = refusable { created ->
        created += file
        if (!file.isFile || file.length() == 0L) return@refusable CaptureResult.Refused(CaptureRefusal.StorageFailed)
        val projectId = activeProjectId()
        ExifScrubber.stripLocation(file)
        val sha = files.sha256(file)
        val path = try {
            files.relative(file)
        } catch (e: IllegalArgumentException) {
            return@refusable CaptureResult.Refused(CaptureRefusal.StorageFailed)
        }
        // The shot must lie in this project's directory, or the orphan sweep would delete it as unreferenced.
        if (!path.startsWith("projects/$projectId/")) return@refusable CaptureResult.Refused(CaptureRefusal.StorageFailed)
        val takenAt = clock.instant().toString()
        val enqueues = transaction {
            requireProject(projectId)
            val (ownerTable, ownerId) = when (owner) {
                is PhotoOwner.Step -> {
                    val stepId = StepKeys.recordId(projectId, owner.stepKey)
                    if (db.records().get(SyncTable.Steps.id, stepId) == null) {
                        put(
                            SyncTable.Steps, stepId, projectId,
                            mapOf(
                                "stepKey" to JsonPrimitive(owner.stepKey),
                                "tradeId" to JsonPrimitive(owner.tradeId),
                                "capturedAt" to JsonPrimitive(takenAt),
                            ),
                        )
                    }
                    SyncTable.Steps.id to stepId
                }
                is PhotoOwner.DefectDraft -> {
                    // A shot that arrives after Abbrechen belongs to nobody: refuse it, the file is deleted (M5).
                    if (db.state().get(DISCARDED_PREFIX + owner.draftId) != null) return@transaction null
                    SyncTable.Defects.id to owner.draftId
                }
            }
            put(
                SyncTable.Photos, photoId, projectId,
                mapOf(
                    "ownerTable" to JsonPrimitive(ownerTable),
                    "ownerId" to JsonPrimitive(ownerId),
                    "fileId" to JsonPrimitive(photoId),
                    "sha256" to JsonPrimitive(sha),
                    "takenAt" to JsonPrimitive(takenAt),
                    "bytes" to JsonPrimitive(file.length()),
                ),
                sortKey = nextSortKey(SyncTable.Photos, projectId),
            )
            // A draft that Sichern already saved owns this photo as a defect: queue it like the others (M5).
            val savedDefect = if (owner is PhotoOwner.DefectDraft) {
                db.records().get(SyncTable.Defects.id, ownerId)?.takeIf { !it.deleted }
            } else {
                null
            }
            val sync = owner is PhotoOwner.Step || savedDefect != null
            if (sync) {
                enqueueUpload(SyncBucket.Photos, photoId, projectId, path, remoteName(projectId, "photos", photoId, "jpg"), "image/jpeg", sha)
                enqueueUpsert(SyncTable.Photos, photoId, projectId)
            }
            if (owner is PhotoOwner.Step) enqueueUpsert(SyncTable.Steps, ownerId, projectId)
            if (savedDefect != null) {
                val label = Converters.stringToFields(savedDefect.fields)["deadlineLabel"].asContent().orEmpty()
                val count = draftPhotos(projectId, ownerId).size
                put(
                    SyncTable.Defects, ownerId, projectId,
                    mapOf("photos" to JsonPrimitive(count), "meta" to JsonPrimitive(defectMeta(label, count))),
                )
                enqueueUpsert(SyncTable.Defects, ownerId, projectId)
            }
            bumpDataVersion()
            sync
        }
        if (enqueues == null) return@refusable CaptureResult.Refused(CaptureRefusal.UnknownProject)
        if (enqueues) onOutboxChanged()
        CaptureResult.Ok(photoId)
    }

    override suspend fun discardDraft(draftId: String) {
        val projectId = try {
            activeProjectId()
        } catch (e: RepositoryException.NoProject) {
            return
        }
        val removed = transaction {
            // A draft that was saved owns its photos as a defect; they are no longer discardable.
            if (db.records().get(SyncTable.Defects.id, draftId) != null) return@transaction emptyList()
            val rows = draftPhotos(projectId, draftId).map { it.first }
            rows.forEach { db.records().delete(it.tableId, it.id) }
            // Remembered, so a photo whose EXIF scrub was still running is refused when it arrives (M5).
            db.state().put(SyncStateEntity(DISCARDED_PREFIX + draftId, "1"))
            if (rows.isNotEmpty()) bumpDataVersion()
            rows
        }
        removed.forEach { files.newPhotoFile(projectId, it.id).delete() }
    }

    override suspend fun saveDefect(draft: DefectDraft): CaptureResult = refusable {
        val projectId = activeProjectId()
        val days = ChronoUnit.DAYS.between(now(), draft.deadline)
        transaction {
            requireProject(projectId)
            val photos = draftPhotos(projectId, draft.id)
            val deadlineLabel = "Frist " + DeadlineMath.label(draft.deadline, 0)
            val count = photos.size.takeIf { it > 0 }
            val defect = Defect(
                id = draft.id,
                tradeId = draft.tradeId,
                trade = tradeText(draft.tradeId),
                title = draft.title,
                deadline = draft.deadline.toString(),
                deadlineLabel = deadlineLabel,
                dueLabel = DueLabels.dueLabel(days),
                urgency = DueLabels.urgency(days),
                meta = defectMeta(deadlineLabel, count ?: 0),
                photos = count,
                severity = draft.severity,
                description = draft.description,
                elementId = draft.elementId,
                responsible = draft.responsible,
                notifyByEmail = draft.notifyByEmail,
            )
            val changes = LinkedHashMap(FixtureJson.encodeToJsonElement(Defect.serializer(), defect).jsonObject)
            draft.stepKey?.let { changes["stepKey"] = JsonPrimitive(it) }
            changes["origin"] = JsonPrimitive(ORIGIN_APP)
            put(SyncTable.Defects, draft.id, projectId, changes, nextSortKey(SyncTable.Defects, projectId))
            for ((row, fields) in photos) {
                val fileId = fields["fileId"].asContent() ?: row.id
                enqueueUpload(
                    SyncBucket.Photos, fileId, projectId, files.relative(files.newPhotoFile(projectId, fileId)),
                    remoteName(projectId, "photos", fileId, "jpg"), "image/jpeg", fields["sha256"].asContent().orEmpty(),
                )
                enqueueUpsert(SyncTable.Photos, row.id, projectId)
            }
            enqueueUpsert(SyncTable.Defects, draft.id, projectId)
            bumpDataVersion()
        }
        onOutboxChanged()
        CaptureResult.Ok(draft.id)
    }

    override suspend fun saveSignOff(capture: SignOffCapture, protocol: ExportedProtocol): CaptureResult =
        refusable { created ->
            created += protocol.file
            if (capture.strokes.strokes.isEmpty()) return@refusable CaptureResult.Refused(CaptureRefusal.EmptySignature)
            val doc = protocol.doc
            val projectId = doc.projectId
            val path = files.relative(protocol.file)
            val signatureId = newId("sg_")
            var existingProtocol: String? = null
            val known = transaction {
                val inspection = db.records().get(SyncTable.Inspections.id, capture.inspectionId)
                if (inspection == null || inspection.deleted || inspection.projectId != projectId) {
                    return@transaction false
                }
                // Signed already (e.g. the first sign-off finished while its screen was recreated): nothing more is written.
                val fields = Converters.stringToFields(inspection.fields)
                val signedId = fields["protocolId"].asContent()
                if (!fields["signedAt"].asContent().isNullOrBlank() && !signedId.isNullOrBlank()) {
                    existingProtocol = signedId
                    return@transaction true
                }
                put(
                    SyncTable.Signatures, signatureId, projectId,
                    mapOf(
                        "inspectionId" to JsonPrimitive(capture.inspectionId),
                        "role" to JsonPrimitive("owner"),
                        "name" to JsonPrimitive(capture.signerName),
                        "strokes" to strokesJson(capture),
                        "signedAt" to JsonPrimitive(doc.createdAt),
                    ),
                )
                put(
                    SyncTable.Protocols, doc.protocolId, projectId,
                    mapOf(
                        "inspectionId" to JsonPrimitive(capture.inspectionId),
                        "fileId" to JsonPrimitive(doc.protocolId),
                        "fileSha256" to JsonPrimitive(protocol.fileSha256),
                        "contentSha256" to JsonPrimitive(protocol.contentSha256),
                        "contentJson" to JsonPrimitive(protocol.contentJson),
                        "createdAt" to JsonPrimitive(doc.createdAt),
                    ),
                )
                put(
                    SyncTable.Inspections, capture.inspectionId, projectId,
                    mapOf("signedAt" to JsonPrimitive(doc.createdAt), "protocolId" to JsonPrimitive(doc.protocolId)),
                )
                enqueueUpload(
                    SyncBucket.Pdfs, doc.protocolId, projectId, path,
                    remoteName(projectId, "protocols", doc.protocolId, "pdf"), PDF_MIME, protocol.fileSha256,
                )
                enqueueUpsert(SyncTable.Signatures, signatureId, projectId)
                enqueueUpsert(SyncTable.Protocols, doc.protocolId, projectId)
                enqueueUpsert(SyncTable.Inspections, capture.inspectionId, projectId)
                bumpDataVersion()
                true
            }
            if (!known) return@refusable CaptureResult.Refused(CaptureRefusal.UnknownProject)
            existingProtocol?.let { signed ->
                if (signed != doc.protocolId) protocol.file.delete()
                return@refusable CaptureResult.Ok(signed)
            }
            onOutboxChanged()
            CaptureResult.Ok(doc.protocolId)
        }

    override suspend fun importDocument(source: ImportSource, request: ImportRequest): CaptureResult = refusable { created ->
        val projectId = activeProjectId()
        val id = newId("dc_")
        val target = files.documentFile(projectId, id)
        val temp = File(target.parentFile, "$id.part")
        created += temp
        created += target
        copyPdf(source, temp)?.let { return@refusable CaptureResult.Refused(it) }
        if (!temp.renameTo(target)) throw IOException("Cannot move ${temp.name} to ${target.name}")
        val sha = files.sha256(target)
        val size = target.length()
        val path = files.relative(target)
        val importedAt = clock.instant()
        val item = DocumentItem(
            id = id,
            kind = request.kind,
            tradeId = request.tradeId,
            title = titleOf(source.displayName),
            meta = PlatformText(null, "PDF · " + DeadlineMath.label(LocalDate.ofInstant(importedAt, BERLIN), 0)),
        )
        transaction {
            requireProject(projectId)
            val changes = LinkedHashMap(FixtureJson.encodeToJsonElement(DocumentItem.serializer(), item).jsonObject)
            changes["fileId"] = JsonPrimitive(id)
            changes["fileSha256"] = JsonPrimitive(sha)
            changes["sizeBytes"] = JsonPrimitive(size)
            changes["origin"] = JsonPrimitive(ORIGIN_IMPORT)
            changes["importedAt"] = JsonPrimitive(importedAt.toString())
            put(SyncTable.Documents, id, projectId, changes, nextSortKey(SyncTable.Documents, projectId))
            enqueueUpload(SyncBucket.Pdfs, id, projectId, path, remoteName(projectId, "documents", id, "pdf"), PDF_MIME, sha)
            enqueueUpsert(SyncTable.Documents, id, projectId)
            bumpDataVersion()
        }
        onOutboxChanged()
        CaptureResult.Ok(id)
    }

    override suspend fun ensureMember(userId: String, role: String) {
        val projectId = try {
            activeProjectId()
        } catch (e: RepositoryException.NoProject) {
            return
        }
        val id = "m_" + ProtocolHash.sha256Hex("$projectId/$userId".toByteArray(Charsets.UTF_8)).take(32)
        val changed = try {
            transaction { ensureMemberIn(projectId, id, userId, role) }
        } catch (e: RepositoryException.NoProject) {
            false
        }
        if (changed) onOutboxChanged()
    }

    private suspend fun ensureMemberIn(projectId: String, id: String, userId: String, role: String): Boolean {
        requireProject(projectId)
        val existing = db.records().get(SyncTable.Members.id, id)
        val fields = existing?.takeIf { !it.deleted }?.let { Converters.stringToFields(it.fields) }
        if (fields != null && fields["userId"].asContent() == userId && fields["role"].asContent() == role) return false
        put(SyncTable.Members, id, projectId, mapOf("userId" to JsonPrimitive(userId), "role" to JsonPrimitive(role)))
        enqueueUpsert(SyncTable.Members, id, projectId)
        bumpDataVersion()
        return true
    }

    override suspend fun deleteProject(projectId: String): CaptureResult = refusable {
        val known = transaction {
            val project = db.records().get(SyncTable.Projects.id, projectId)
            if (project == null || project.deleted) return@transaction false
            removeProjectRows(projectId)
            db.outbox().insert(
                OutboxEntity(
                    kind = OutboxKind.DELETE_PROJECT,
                    tableId = null,
                    rowId = projectId,
                    projectId = projectId,
                    payload = "{}",
                    state = OutboxState.PENDING,
                    attempts = 0,
                    lastError = null,
                    createdAt = clock.millis(),
                ),
            )
            true
        }
        if (!known) return@refusable CaptureResult.Refused(CaptureRefusal.UnknownProject)
        onOutboxChanged()
        removeProjectFiles(projectId)
        CaptureResult.Ok(projectId)
    }

    /** Like [deleteProject] but enqueues nothing: the project was already deleted remotely (D19). */
    internal suspend fun purgeProject(projectId: String) {
        transaction { removeProjectRows(projectId) }
        removeProjectFiles(projectId)
    }

    private suspend fun removeProjectRows(projectId: String) {
        db.records().deleteProject(projectId)
        db.outbox().deleteForProject(projectId)
        bumpDataVersion()
    }

    /** A failure here is left to the startup `sweepOrphans`: no record references the directory any more (I9). */
    private fun removeProjectFiles(projectId: String) {
        try {
            files.deleteProject(projectId)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Cannot delete the files of $projectId: ${e.message}")
        }
    }

    /**
     * Runs a write that returns a [CaptureResult]. Files added to `created` are deleted when the write is refused or
     * fails (I3, I4); a storage failure is a refusal, not an exception.
     */
    private suspend fun refusable(block: suspend (MutableList<File>) -> CaptureResult): CaptureResult {
        val created = ArrayList<File>()
        val result = try {
            block(created)
        } catch (e: RepositoryException.NoProject) {
            CaptureResult.Refused(CaptureRefusal.UnknownProject)
        } catch (e: RepositoryException) {
            CaptureResult.Refused(CaptureRefusal.StorageFailed)
        } catch (e: IOException) {
            CaptureResult.Refused(CaptureRefusal.StorageFailed)
        } catch (e: SecurityException) {
            CaptureResult.Refused(CaptureRefusal.StorageFailed)
        } catch (e: IllegalArgumentException) {
            CaptureResult.Refused(CaptureRefusal.StorageFailed)
        }
        if (result is CaptureResult.Refused) created.forEach { it.delete() }
        return result
    }

    private suspend fun <T> transaction(block: suspend () -> T): T = storage { db.withTransaction { block() } }

    /** Writes [changes] to the record with one fresh stamp for all of them; a new record gets [sortKey] and [projectId]. */
    private suspend fun put(
        table: SyncTable,
        id: String,
        projectId: String,
        changes: Map<String, JsonElement>,
        sortKey: Long = 0L,
    ) {
        val base = db.records().get(table.id, id)?.let(Converters::toRecord)
            ?: SyncRecord(table, id, projectId, sortKey, JsonObject(emptyMap()), emptyMap(), deleted = false)
        // A write to a tombstoned record brings it back (a later stamp for `deleted` wins the merge).
        db.records().upsert(Converters.toEntity(FieldMerge.touch(base, changes, stamps.next(), deleted = if (base.deleted) false else null)))
    }

    private suspend fun nextSortKey(table: SyncTable, projectId: String): Long =
        (db.records().list(table.id, projectId).maxOfOrNull { it.sortKey } ?: -1L) + 1L

    private suspend fun enqueueUpsert(table: SyncTable, id: String, projectId: String) =
        db.outbox().enqueueUpsert(table.id, id, projectId, clock.millis())

    private suspend fun enqueueUpload(
        bucket: SyncBucket,
        fileId: String,
        projectId: String,
        path: String,
        name: String,
        mime: String,
        sha256: String,
    ) {
        val payload = JsonObject(
            mapOf(
                "bucket" to JsonPrimitive(bucket.id),
                "fileId" to JsonPrimitive(fileId),
                "path" to JsonPrimitive(path),
                "name" to JsonPrimitive(name),
                "mime" to JsonPrimitive(mime),
                "sha256" to JsonPrimitive(sha256),
            ),
        )
        db.outbox().insert(
            OutboxEntity(
                kind = OutboxKind.UPLOAD_FILE,
                tableId = null,
                rowId = fileId,
                projectId = projectId,
                payload = payload.toString(),
                state = OutboxState.PENDING,
                attempts = 0,
                lastError = null,
                createdAt = clock.millis(),
            ),
        )
    }

    private suspend fun bumpDataVersion() {
        val current = db.state().get(DATA_VERSION_KEY)?.toLongOrNull() ?: 0L
        db.state().put(SyncStateEntity(DATA_VERSION_KEY, (current + 1).toString()))
    }

    /** Photo records of a defect draft (`ownerTable = defects`, `ownerId = draftId`) with their fields. */
    private suspend fun draftPhotos(projectId: String, draftId: String): List<Pair<RecordEntity, JsonObject>> =
        db.records().list(SyncTable.Photos.id, projectId).filter { !it.deleted }.mapNotNull { row ->
            val fields = try {
                Converters.stringToFields(row.fields)
            } catch (e: SerializationException) {
                return@mapNotNull null
            }
            if (fields["ownerTable"].asContent() == SyncTable.Defects.id && fields["ownerId"].asContent() == draftId) {
                row to fields
            } else {
                null
            }
        }

    /** The trade text of an existing defect of that trade, else the fixture convention (`Rohbau` / `ROHBAU`). */
    private suspend fun tradeText(tradeId: String): PlatformText {
        decodeRecords(SyncTable.Defects, Defect.serializer()).firstOrNull { it.value.tradeId == tradeId }
            ?.let { return it.value.trade }
        val name = decodeRecords(SyncTable.Trades, Trade.serializer()).firstOrNull { it.value.id == tradeId }?.value?.name
            ?: tradeId
        return PlatformText(name, name.uppercase(Locale.GERMAN))
    }

    private fun strokesJson(capture: SignOffCapture): JsonObject {
        val strokes = capture.strokes
        return JsonObject(
            mapOf(
                "w" to JsonPrimitive(strokes.bounds.width.toShortDouble()),
                "h" to JsonPrimitive(strokes.bounds.height.toShortDouble()),
                "strokes" to JsonArray(
                    strokes.strokes.map { stroke ->
                        JsonArray(stroke.flatMap { listOf(it.x, it.y) }.map { JsonPrimitive(it.toShortDouble()) })
                    },
                ),
            ),
        )
    }

    /** The decimal a float prints as, so `12.3f` becomes `12.3` (the same form the protocol PDF hashes). */
    private fun Float.toShortDouble(): Double = toString().toDouble()

    /** Copies [source] into [temp]; returns the refusal when it is not a PDF or exceeds 50 MiB, `null` when copied. */
    private fun copyPdf(source: ImportSource, temp: File): CaptureRefusal? = source.open().use copy@{ input ->
        val header = ByteArray(PDF_MAGIC.size)
        var read = 0
        while (read < header.size) {
            val n = input.read(header, read, header.size - read)
            if (n < 0) break
            read += n
        }
        if (read < header.size || !header.contentEquals(PDF_MAGIC)) return@copy CaptureRefusal.NotPdf
        temp.outputStream().use { out ->
            out.write(header)
            var total = header.size.toLong()
            val buffer = ByteArray(COPY_BUFFER)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                total += n
                if (total > MAX_IMPORT_BYTES) return@copy CaptureRefusal.TooLarge
                out.write(buffer, 0, n)
            }
        }
        null
    }

    private fun titleOf(displayName: String?): String {
        val trimmed = displayName?.trim().orEmpty()
        val bare = (if (trimmed.endsWith(".pdf", ignoreCase = true)) trimmed.dropLast(4) else trimmed).trim()
        return bare.take(TITLE_MAX).ifEmpty { "Dokument" }
    }

    /** The stored file name `<projectId>/<kind>/<id>.<ext>`: the project-cleanup backstop matches on the `<projectId>/` prefix. */
    private fun remoteName(projectId: String, kind: String, id: String, ext: String) = "$projectId/$kind/$id.$ext"

    /** Inside a transaction: the project must still exist; a purge or delete that ran in between refuses the write (M6). */
    private suspend fun requireProject(projectId: String) {
        val project = db.records().get(SyncTable.Projects.id, projectId)
        if (project == null || project.deleted) throw RepositoryException.NoProject()
    }

    private fun defectMeta(deadlineLabel: String, photos: Int): String = when {
        photos <= 0 -> deadlineLabel
        photos == 1 -> "$deadlineLabel · 1 Foto"
        else -> "$deadlineLabel · $photos Fotos"
    }

    private fun newId(prefix: String): String = prefix + UUID.randomUUID().toString().replace("-", "")

    private class Decoded<T>(val key: String, val fields: JsonObject, val fixtureFields: JsonObject, val value: T)

    private fun source(): FixtureSource {
        val text = fixtureJson()
        cachedSource?.takeIf { it.text == text }?.let { return it }
        return FixtureSource(text).also { cachedSource = it }
    }

    private suspend fun activeProjectId(): String =
        storage { db.records().projectIds().firstOrNull() } ?: throw RepositoryException.NoProject()

    /**
     * The live, decodable records of [table]; [reject] returns the reason a record that decodes must still be left out
     * because a screen could not show it (I17). Rejected and undecodable records are counted in [skippedRecords].
     */
    private suspend fun <T> decodeRecords(
        table: SyncTable,
        serializer: KSerializer<T>,
        reject: (Decoded<T>) -> String? = { null },
    ): List<Decoded<T>> {
        val projectId = activeProjectId()
        val rows = storage { db.records().list(table.id, projectId) }
        val allowed = SyncSchema.tables.getValue(table).fixtureFields.keys
        return rows.mapNotNull { row ->
            val key = "${row.tableId}/${row.id}"
            if (row.deleted) {
                skipped.remove(key)
                return@mapNotNull null
            }
            try {
                val fields = Converters.stringToFields(row.fields)
                val fixtureFields = JsonObject(fields.filterKeys { it in allowed })
                val value = FixtureJson.decodeFromJsonElement(serializer, fixtureFields)
                val decoded = Decoded(key, fields, fixtureFields, value)
                val reason = reject(decoded)
                if (reason != null) {
                    markSkipped(key, reason)
                    null
                } else {
                    skipped.remove(key)
                    decoded
                }
            } catch (e: SerializationException) {
                markSkipped(key, e.message)
                null
            } catch (e: IllegalArgumentException) {
                markSkipped(key, e.message)
                null
            }
        }
    }

    private fun markSkipped(key: String, reason: String?) {
        if (skipped.add(key)) Log.w(TAG, "Skipping undecodable record $key: $reason")
    }

    private fun JsonElement?.asContent(): String? = (this as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull

    private suspend fun <T> storage(block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        // Room cancels its own scope when the database is closed; only our caller's cancellation propagates.
        currentCoroutineContext().ensureActive()
        throw RepositoryException.Storage(e.message ?: e.toString())
    } catch (e: RepositoryException) {
        throw e
    } catch (e: SQLiteException) {
        throw RepositoryException.Storage(e.message ?: e.toString())
    } catch (e: androidx.sqlite.SQLiteException) {
        throw RepositoryException.Storage(e.message ?: e.toString())
    } catch (e: IllegalStateException) {
        throw RepositoryException.Storage(e.message ?: e.toString())
    }

    private companion object {
        const val TAG = "LocalRepository"
        const val DATA_VERSION_KEY = "dataVersion"
        const val DISCARDED_PREFIX = "discarded:"
        const val VIEWER_FACTS = 3
        val DOCUMENT_KINDS = setOf("offer", "contract", "invoice", "plan")
        const val ORIGIN_IMPORT = "import"
        const val ORIGIN_APP = "app"
        const val PDF_MIME = "application/pdf"
        const val MAX_IMPORT_BYTES = 52_428_800L
        const val COPY_BUFFER = 8192
        const val TITLE_MAX = 255
        val PDF_MAGIC = "%PDF-".toByteArray(Charsets.US_ASCII)
        val BERLIN: ZoneId = ZoneId.of("Europe/Berlin")
    }
}
