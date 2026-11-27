package de.baupilot.capture

import java.io.InputStream
import java.time.LocalDate
import kotlinx.serialization.json.JsonObject

enum class StepStatus { OK, DEFECT }

/** One captured Prüfschritt: [status] and [values] are written only when set. */
data class StepCapture(
    val stepKey: String,
    val tradeId: String,
    val status: StepStatus?,
    val values: JsonObject = JsonObject(emptyMap()),
)

sealed interface PhotoOwner {
    data class Step(val stepKey: String, val tradeId: String) : PhotoOwner
    data class DefectDraft(val draftId: String) : PhotoOwner
}

/** The "Mangel erfassen" form as saved; [id] becomes the defect record id and the owner of its draft photos. */
data class DefectDraft(
    val id: String,
    val tradeId: String,
    val stepKey: String?,
    val elementId: String?,
    val title: String,
    val description: String,
    val severity: String,
    val deadline: LocalDate,
    val notifyByEmail: Boolean,
    val responsible: String,
)

data class ImportRequest(val tradeId: String?, val kind: String)

/** A document the user picked; [open] is called once and the stream closed by the repository. */
interface ImportSource {
    val displayName: String?
    fun open(): InputStream
}

enum class CaptureRefusal { EmptySignature, NotPdf, TooLarge, UnknownProject, StorageFailed }

sealed interface CaptureResult {
    data class Ok(val id: String) : CaptureResult
    data class Refused(val reason: CaptureRefusal) : CaptureResult
}
