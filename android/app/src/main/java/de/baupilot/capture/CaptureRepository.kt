package de.baupilot.capture

import de.baupilot.protocol.ExportedProtocol
import java.io.File

/**
 * Write side of the local store. Every successful write is one transaction: records with stamps, outbox
 * entries and a `dataVersion` increment (I3). A refusal changes nothing (I4).
 */
interface CaptureRepository {
    suspend fun recordStep(capture: StepCapture): CaptureResult
    suspend fun addPhoto(owner: PhotoOwner, photoId: String, file: File): CaptureResult
    suspend fun discardDraft(draftId: String)
    suspend fun saveDefect(draft: DefectDraft): CaptureResult
    suspend fun saveSignOff(capture: SignOffCapture, protocol: ExportedProtocol): CaptureResult
    suspend fun importDocument(source: ImportSource, request: ImportRequest): CaptureResult
    suspend fun ensureMember(userId: String, role: String)
    suspend fun deleteProject(projectId: String): CaptureResult
}
