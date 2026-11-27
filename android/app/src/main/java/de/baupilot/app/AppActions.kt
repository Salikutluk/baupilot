package de.baupilot.app

import de.baupilot.capture.StepStatus
import de.baupilot.inspection.SignatureStrokes
import kotlinx.serialization.json.JsonObject

/**
 * Everything the screens can ask the app to do beyond navigating (D6). [None] reproduces the M7 behaviour, so
 * snapshot and UI tests need no backend.
 */
interface AppActions {
    fun logIn(email: String, password: String, onSuccess: () -> Unit)
    fun signOut(onDone: () -> Unit)
    fun recordStep(route: AppRoute, part: String?, status: StepStatus?, values: JsonObject)
    fun capturePhoto(route: AppRoute, part: String?)
    fun saveDefect(draft: DefectFormValues, below: AppRoute?)
    fun finishSignOff(inspectionId: String, strokes: SignatureStrokes, onSuccess: () -> Unit)
    fun importDocument(tradeId: String?, kind: String?)

    object None : AppActions {
        override fun logIn(email: String, password: String, onSuccess: () -> Unit) = onSuccess()
        override fun signOut(onDone: () -> Unit) = Unit
        override fun recordStep(route: AppRoute, part: String?, status: StepStatus?, values: JsonObject) = Unit
        override fun capturePhoto(route: AppRoute, part: String?) = Unit
        override fun saveDefect(draft: DefectFormValues, below: AppRoute?) = Unit
        override fun finishSignOff(inspectionId: String, strokes: SignatureStrokes, onSuccess: () -> Unit) = onSuccess()
        override fun importDocument(tradeId: String?, kind: String?) = Unit
    }
}
