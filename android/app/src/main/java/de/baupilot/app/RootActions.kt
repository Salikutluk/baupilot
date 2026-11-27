package de.baupilot.app

import android.content.ActivityNotFoundException
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import de.baupilot.AppServices
import de.baupilot.backend.LoginRefusal
import de.baupilot.backend.LoginResult
import de.baupilot.capture.CaptureRefusal
import de.baupilot.capture.CaptureResult
import de.baupilot.capture.DefectDraft
import de.baupilot.capture.ImportRequest
import de.baupilot.capture.ImportSource
import de.baupilot.capture.PhotoOwner
import de.baupilot.capture.SignOffCapture
import de.baupilot.capture.StepCapture
import de.baupilot.capture.StepKeys
import de.baupilot.capture.StepStatus
import de.baupilot.data.RepositoryException
import de.baupilot.inspection.SignatureStrokes
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

private const val TAG = "RootActions"

/** What the activity-result launchers need to bring back (D12, §5.10): a camera shot or a picked document. */
sealed interface PendingCapture {
    data class Photo(val photoId: String, val projectId: String, val owner: PhotoOwner) : PendingCapture
    data class Import(val tradeId: String?, val kind: String) : PendingCapture
}

/**
 * State that must outlive an activity recreation while the camera or the document picker is open: the pending
 * capture and the id of the draft defect whose photos are not saved yet (I11). Kept in `rememberSaveable`.
 */
class CaptureSession {
    var pending: PendingCapture? = null
    var draftId: String? = null

    /** A photo control was tapped and its project lookup is running; not saved, a recreation cancels the lookup (M8). */
    var resolving: Boolean = false

    /** Set by the defect save, read and cleared by the next navigation change (see [RootActions.onNavigation]). */
    var draftSaved: Boolean = false

    companion object {
        private const val STEP = "step"
        private const val DRAFT = "draft"
        private const val IMPORT = "import"

        /** `[type, a, b, c, d, draftId]`, empty strings for null and unused slots. */
        val Saver: Saver<CaptureSession, Any> = listSaver<CaptureSession, Any>(
            save = { session ->
                val draft = session.draftId.orEmpty()
                when (val pending = session.pending) {
                    is PendingCapture.Photo -> when (val owner = pending.owner) {
                        is PhotoOwner.Step ->
                            listOf(STEP, pending.photoId, pending.projectId, owner.stepKey, owner.tradeId, draft)
                        is PhotoOwner.DefectDraft ->
                            listOf(DRAFT, pending.photoId, pending.projectId, owner.draftId, "", draft)
                    }
                    is PendingCapture.Import -> listOf(IMPORT, pending.tradeId.orEmpty(), pending.kind, "", "", draft)
                    null -> listOf("", "", "", "", "", draft)
                }
            },
            restore = { values ->
                val saved = values.map { it as String }
                CaptureSession().apply {
                    draftId = saved[5].ifEmpty { null }
                    pending = when (saved[0]) {
                        STEP -> PendingCapture.Photo(saved[1], saved[2], PhotoOwner.Step(saved[3], saved[4]))
                        DRAFT -> PendingCapture.Photo(saved[1], saved[2], PhotoOwner.DefectDraft(saved[3]))
                        IMPORT -> PendingCapture.Import(saved[1].ifEmpty { null }, saved[2])
                        else -> null
                    }
                }
            },
        )
    }
}

/** Android's side of [RootActions]: Toasts, the FileProvider, the two launchers and the haptic confirmation. */
interface RootEnvironment {
    /** Safe to call from any thread. */
    fun toast(text: String)

    /** Throws [IllegalArgumentException] for a file outside the provider's paths. */
    fun uriFor(file: File): Uri

    /** Throws [ActivityNotFoundException] when no camera app exists. */
    fun launchCamera(target: Uri)

    /** Throws [ActivityNotFoundException] when no document app exists. */
    fun launchDocument()

    fun source(uri: Uri): ImportSource

    fun confirm()
}

/** The fixed German refusal texts (D12); not part of the prototype. */
object RootToasts {
    const val INVALID_CREDENTIALS = "Anmeldung fehlgeschlagen. E-Mail und Passwort prüfen."
    const val OFFLINE = "Keine Verbindung. Die Anmeldung braucht Internet."
    const val RATE_LIMITED = "Zu viele Versuche. Bitte später erneut versuchen."
    const val NOT_CONFIGURED = "Backend nicht eingerichtet."
    const val UNKNOWN = "Anmeldung fehlgeschlagen."
    const val EMPTY_SIGNATURE = "Bitte zuerst unterschreiben."
    const val NOT_PDF = "Nur PDF-Dateien können importiert werden."
    const val TOO_LARGE = "Die Datei ist größer als 50 MB."
    const val STORAGE_FAILED = "Speichern fehlgeschlagen."

    fun of(reason: LoginRefusal): String = when (reason) {
        LoginRefusal.InvalidCredentials -> INVALID_CREDENTIALS
        LoginRefusal.Offline -> OFFLINE
        LoginRefusal.RateLimited -> RATE_LIMITED
        LoginRefusal.NotConfigured -> NOT_CONFIGURED
        LoginRefusal.Unknown -> UNKNOWN
    }

    fun of(reason: CaptureRefusal): String = when (reason) {
        CaptureRefusal.EmptySignature -> EMPTY_SIGNATURE
        CaptureRefusal.NotPdf -> NOT_PDF
        CaptureRefusal.TooLarge -> TOO_LARGE
        CaptureRefusal.UnknownProject, CaptureRefusal.StorageFailed -> STORAGE_FAILED
    }
}

/**
 * The real [AppActions] (D6, §5.10). Repository writes, `logIn`, `signOut` and `finishSignOff` run on
 * `services.appScope`, so navigation is immediate and a recreation does not cancel them; the composition's [uiScope]
 * only awaits them for the Toast and the navigation. They start without a dispatch hop, so a non-suspending auth
 * completes within the click, and `services.inFlight` ignores a second tap while one runs. Every refusal becomes a
 * Toast; successful captures only confirm haptically (D12).
 */
class RootActions(
    private val services: AppServices,
    private val data: () -> AppData?,
    private val session: CaptureSession,
    private val uiScope: CoroutineScope,
    private val environment: RootEnvironment,
) : AppActions {
    override fun logIn(email: String, password: String, onSuccess: () -> Unit) {
        val work = inBackground(LOGIN) {
            val result = services.auth.logIn(email, password)
            if (result is LoginResult.Success) afterLogin(result.userId)
            result
        } ?: return
        startOnMain {
            try {
                when (val result = work.await()) {
                    is LoginResult.Success -> onSuccess()
                    is LoginResult.Refused -> environment.toast(RootToasts.of(result.reason))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Login failed", e)
                environment.toast(RootToasts.UNKNOWN)
            }
        }
    }

    /** The follow-up work of D22/D25 runs in the background; one failing step never blocks the next. */
    private fun afterLogin(userId: String) {
        services.appScope.launch {
            guarded("requeueParked") { services.database.outbox().requeueParked() }
            services.auth.cachedRole?.let { role -> guarded("ensureMember") { services.repository.ensureMember(userId, role) } }
            guarded("push register") { services.push.register(userId) }
            services.sync.schedulePeriodic()
            services.sync.requestSync()
        }
    }

    /** D22: the session, cached user and push target go, local data and the outbox stay. */
    override fun signOut(onDone: () -> Unit) {
        val work = inBackground(SIGN_OUT) {
            try {
                services.push.unregister()
                services.auth.logOut()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Remote sign-out failed", e)
            }
            services.auth.clearCachedUser()
            services.sync.cancelAll()
        } ?: return
        startOnMain {
            work.await()
            onDone()
        }
    }

    override fun recordStep(route: AppRoute, part: String?, status: StepStatus?, values: JsonObject) {
        val stepKey = StepKeys.of(route, part) ?: return
        val tradeId = tradeIdOf(route) ?: return
        write { services.repository.recordStep(StepCapture(stepKey, tradeId, status, values)) }
    }

    override fun capturePhoto(route: AppRoute, part: String?) {
        val owner = ownerOf(route, part) ?: return
        // Taps while a camera is open or its target is being looked up would overwrite the pending capture (M8).
        if (session.pending != null || session.resolving) return
        session.resolving = true
        startOnMain {
            try {
                val projectId = try {
                    withContext(services.io) { services.repository.project().id }
                } catch (e: RepositoryException) {
                    Log.w(TAG, "No project for the photo", e)
                    environment.toast(RootToasts.STORAGE_FAILED)
                    return@startOnMain
                }
                val photoId = newId("ph_")
                val target = services.files.newPhotoFile(projectId, photoId)
                launchPending(PendingCapture.Photo(photoId, projectId, owner)) { environment.launchCamera(environment.uriFor(target)) }
            } finally {
                session.resolving = false
            }
        }
    }

    /** The camera's answer; a cancelled or failed shot leaves no file behind. */
    fun onPhotoResult(success: Boolean) {
        val pending = session.pending as? PendingCapture.Photo ?: return
        session.pending = null
        val file = services.files.newPhotoFile(pending.projectId, pending.photoId)
        if (!success) {
            file.delete()
            return
        }
        environment.confirm()
        write { services.repository.addPhoto(pending.owner, pending.photoId, file) }
    }

    override fun saveDefect(draft: DefectFormValues, below: AppRoute?) {
        val model = data()?.defectForm ?: return
        val severity = model.severities.getOrNull(draft.severityIndex) ?: return
        val offset = model.deadlineOptions.getOrNull(draft.deadlineIndex)?.offsetDays
            ?: model.deadlineOptions.getOrNull(model.initialDeadline)?.offsetDays
            ?: 0
        val id = session.draftId ?: newId("df_").also { session.draftId = it }
        session.draftSaved = true
        val record = DefectDraft(
            id = id,
            tradeId = draft.tradeId,
            stepKey = below?.let { StepKeys.of(it) },
            elementId = (below as? AppRoute.InspectionStep)?.elementId,
            title = model.finding,
            description = draft.description,
            severity = severity,
            deadline = model.deadlineBase.plusDays(offset.toLong()),
            notifyByEmail = draft.notify,
            responsible = model.responsible,
        )
        write { services.repository.saveDefect(record) }
    }

    /**
     * Keeps the draft defect in step with navigation (I11): a draft id starts when the form opens, the draft's photos
     * are discarded when the form closes without a save, and kept when it closes after one.
     */
    fun onNavigation(before: AppNavigationState, after: AppNavigationState) {
        val saved = session.draftSaved
        session.draftSaved = false
        when (DefectDraftTracker.onTransition(before, after, saved)) {
            DraftEffect.Start -> session.draftId = newId("df_")
            DraftEffect.Keep -> session.draftId = null
            DraftEffect.Discard -> {
                val id = session.draftId
                session.draftId = null
                if (id != null) {
                    services.appScope.launch { guarded("discardDraft") { services.repository.discardDraft(id) } }
                }
            }
            DraftEffect.None -> Unit
        }
    }

    override fun finishSignOff(inspectionId: String, strokes: SignatureStrokes, onSuccess: () -> Unit) {
        if (strokes.isEmpty) {
            environment.toast(RootToasts.EMPTY_SIGNATURE)
            return
        }
        val current = data() ?: return
        val model = current.signOffs[inspectionId] ?: return
        // The export and the commit must not be cut off by a recreation (an orphan PDF, or a second protocol on retry).
        val work = inBackground(SIGN_OFF) {
            withContext(services.io) {
                val project = services.repository.project()
                val owner = SignOffCapture(inspectionId, project.owner.name, strokes)
                val tradeName = current.tradeNames[inspectionId] ?: inspectionId
                val exported = services.protocols.export(model, project, tradeName, owner)
                services.repository.saveSignOff(owner, exported)
            }
        } ?: return
        startOnMain {
            try {
                when (val result = work.await()) {
                    is CaptureResult.Ok -> onSuccess()
                    is CaptureResult.Refused -> environment.toast(RootToasts.of(result.reason))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: RepositoryException) {
                Log.w(TAG, "Sign-off failed", e)
                environment.toast(RootToasts.STORAGE_FAILED)
            } catch (e: IOException) {
                Log.w(TAG, "Protocol could not be written", e)
                environment.toast(RootToasts.STORAGE_FAILED)
            } catch (e: Exception) {
                Log.w(TAG, "Sign-off failed unexpectedly", e)
                environment.toast(RootToasts.STORAGE_FAILED)
            }
        }
    }

    override fun importDocument(tradeId: String?, kind: String?) {
        if (session.pending != null || session.resolving) return
        launchPending(PendingCapture.Import(tradeId, kind ?: "plan")) { environment.launchDocument() }
    }

    /** The picker's answer; no pick (null) imports nothing. */
    fun onDocumentResult(uri: Uri?) {
        val pending = session.pending as? PendingCapture.Import ?: return
        session.pending = null
        if (uri == null) return
        write { services.repository.importDocument(environment.source(uri), ImportRequest(pending.tradeId, pending.kind)) }
    }

    /** Keeps [pending] for the result callback; a missing camera or document app leaves nothing pending. */
    private fun launchPending(pending: PendingCapture, launch: () -> Unit) {
        session.pending = pending
        try {
            launch()
        } catch (e: ActivityNotFoundException) {
            session.pending = null
            environment.toast(RootToasts.STORAGE_FAILED)
        } catch (e: IllegalArgumentException) {
            // The FileProvider refuses a target outside its configured paths.
            session.pending = null
            Log.w(TAG, "Capture target is not shareable", e)
            environment.toast(RootToasts.STORAGE_FAILED)
        }
    }

    private fun ownerOf(route: AppRoute, part: String?): PhotoOwner? {
        if (route == AppRoute.NewDefect) {
            return PhotoOwner.DefectDraft(session.draftId ?: newId("df_").also { session.draftId = it })
        }
        val stepKey = StepKeys.of(route, part) ?: return null
        return PhotoOwner.Step(stepKey, tradeIdOf(route) ?: return null)
    }

    private fun tradeIdOf(route: AppRoute): String? = when (route) {
        is AppRoute.InspectionStep -> route.tradeId
        is AppRoute.InspectionCheck -> route.tradeId
        is AppRoute.InspectionRoom -> route.tradeId
        is AppRoute.InspectionCheckItem -> route.tradeId
        else -> null
    }

    /** A repository write off the main thread; a refusal or a storage failure becomes a Toast. */
    private fun write(block: suspend () -> CaptureResult) {
        services.appScope.launch {
            val result = try {
                block()
            } catch (e: RepositoryException) {
                Log.w(TAG, "Write failed", e)
                CaptureResult.Refused(CaptureRefusal.StorageFailed)
            }
            if (result is CaptureResult.Refused) environment.toast(RootToasts.of(result.reason))
        }
    }

    private fun startOnMain(block: suspend CoroutineScope.() -> Unit) {
        uiScope.launch(Dispatchers.Main.immediate, CoroutineStart.UNDISPATCHED, block)
    }

    /**
     * Starts [block] on `appScope` without a dispatch hop, unless [name] is already running (then `null`: the tap is
     * ignored). The name is released when the block ends, however it ends.
     */
    private fun <T> inBackground(name: String, block: suspend () -> T): Deferred<T>? {
        if (!services.inFlight.add(name)) return null
        return services.appScope.async(start = CoroutineStart.UNDISPATCHED) {
            try {
                block()
            } finally {
                services.inFlight.remove(name)
            }
        }
    }

    private suspend fun guarded(step: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "$step failed", e)
        }
    }

    private fun newId(prefix: String): String = prefix + UUID.randomUUID().toString().replace("-", "")

    private companion object {
        const val LOGIN = "login"
        const val SIGN_OUT = "signOut"
        const val SIGN_OFF = "signOff"
    }
}
