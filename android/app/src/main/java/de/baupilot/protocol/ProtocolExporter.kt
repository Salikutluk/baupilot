package de.baupilot.protocol

import de.baupilot.app.SignOffModel
import de.baupilot.capture.FileStore
import de.baupilot.capture.SignOffCapture
import de.baupilot.data.Project
import java.io.File
import java.time.Clock
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** A protocol PDF on disk together with the document it was rendered from and both hashes (D17). */
data class ExportedProtocol(
    val doc: ProtocolDocument,
    val file: File,
    val contentSha256: String,
    val fileSha256: String,
    val contentJson: String,
)

/** Builds the [ProtocolDocument] of a sign-off and writes its PDF through [FileStore]. */
class ProtocolExporter(
    private val files: FileStore,
    private val clock: Clock,
    private val zone: ZoneId = ZoneId.of("Europe/Berlin"),
) {
    fun export(signOff: SignOffModel, project: Project, tradeName: String, owner: SignOffCapture): ExportedProtocol {
        val now = clock.instant().truncatedTo(ChronoUnit.SECONDS)
        val createdAt = now.toString()
        val createdLocal = LOCAL_FORMAT.format(now.atZone(zone))
        val protocolId = "pr_" + ProtocolHash.sha256Hex("${signOff.inspectionId}/$createdAt".toByteArray(Charsets.UTF_8)).take(32)
        val ownerBlock = SignatureBlock(
            role = "owner",
            name = owner.signerName,
            label = signOff.ownerLabel,
            strokes = strokesOf(owner),
            signedAt = createdLocal,
        )
        // D18: the craftsman is text only; no strokes are claimed for a signature that was not captured.
        val craftsmanBlock = SignatureBlock(
            role = "craftsman",
            name = signOff.craftsmanName,
            label = signOff.craftsmanLabel,
            strokes = null,
            signedAt = null,
        )
        val doc = ProtocolDocument(
            protocolId = protocolId,
            projectId = project.id,
            address = project.address,
            tradeId = signOff.inspectionId, // the sign-off route carries the trade id
            tradeName = tradeName,
            title = signOff.title,
            stats = signOff.stats.map { "${it.value} ${it.label}" },
            defects = signOff.defects.map { "${it.title} · ${it.meta} · ${it.due}" },
            reservation = signOff.reservation.takeIf { it.isNotBlank() },
            signatures = listOf(ownerBlock, craftsmanBlock),
            createdAt = createdAt,
            createdLocal = createdLocal,
        )
        val contentJson = ProtocolHash.canonicalJson(doc)
        val contentSha256 = ProtocolHash.contentSha256(doc)
        val file = files.protocolFile(project.id, protocolId)
        file.writeBytes(ProtocolPdf.render(doc, contentSha256))
        return ExportedProtocol(doc, file, contentSha256, files.sha256(file), contentJson)
    }

    private fun strokesOf(owner: SignOffCapture): StrokesDto {
        val strokes = owner.strokes
        return StrokesDto(
            w = strokes.bounds.width.toShortDouble(),
            h = strokes.bounds.height.toShortDouble(),
            strokes = strokes.strokes.map { stroke -> stroke.flatMap { listOf(it.x.toShortDouble(), it.y.toShortDouble()) } },
        )
    }

    /** The decimal a float prints as, so `12.3f` becomes `12.3` and not `12.300000190734863`. */
    private fun Float.toShortDouble(): Double = toString().toDouble()

    private companion object {
        val LOCAL_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    }
}
