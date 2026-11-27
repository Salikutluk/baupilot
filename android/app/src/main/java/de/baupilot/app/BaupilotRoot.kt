package de.baupilot.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import de.baupilot.AppServices
import de.baupilot.BaupilotApplication
import de.baupilot.capture.ImportSource
import de.baupilot.data.BackendRepository
import de.baupilot.data.RepositoryException
import de.baupilot.data.local.LocalRepository
import de.baupilot.designsystem.Tokens
import de.baupilot.screens.login.LoginScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

private const val TAG = "BaupilotRoot"

/**
 * App root (D5, D11, D17): seeds and loads [AppData] from the local repository, and reloads it on the activity's
 * `ON_START` only when the stored data changed since the last load. The previous data stays on screen during a
 * reload and after a failed one. Navigation state survives configuration changes; a cached session starts logged
 * in (I14). Back is consumed only while a pushed route is open (R14). A [pendingTarget] (deep link) is applied once
 * logged in and then cleared. After the first successful login the notification permission is requested, when
 * push is configured and the platform asks for it.
 */
@Composable
fun BaupilotRoot(
    services: AppServices = (LocalContext.current.applicationContext as BaupilotApplication).services,
    pendingTarget: MutableStateFlow<NavigationTarget?> = remember { MutableStateFlow(null) },
) {
    var data by remember(services) { mutableStateOf<AppData?>(null) }
    var loadedVersion by remember(services) { mutableStateOf<Long?>(null) }
    var state by rememberSaveable(stateSaver = AppNavigationState.Saver) {
        mutableStateOf(AppNavigationState(isLoggedIn = services.auth.cachedUserId != null))
    }
    val session = rememberSaveable(saver = CaptureSession.Saver) { CaptureSession() }
    val context = LocalContext.current
    val view = LocalView.current
    val environment = remember(context, view) { AndroidRootEnvironment(context, view) }
    val uiScope = rememberCoroutineScope()
    val actions = remember(services, session, environment, uiScope) {
        RootActions(services, { data }, session, uiScope, environment)
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { actions.onPhotoResult(it) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { actions.onDocumentResult(it) }
    environment.camera = camera
    environment.picker = picker
    val notificationPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val change: (AppNavigationState) -> Unit = { next ->
        actions.onNavigation(state, next)
        if (shouldRequestNotifications(services.config.fcm != null, Build.VERSION.SDK_INT, state.isLoggedIn, next.isLoggedIn) &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        state = next
    }

    val pending by pendingTarget.collectAsState()
    LaunchedEffect(state.isLoggedIn, pending) {
        val target = pending
        if (target != null && state.isLoggedIn) {
            change(state.go(target))
            pendingTarget.value = null
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(services, lifecycleOwner) {
        var seeded = false
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val repository = services.repository
            if (!seeded) seeded = seedOrFalse(repository)
            if (!seeded) return@repeatOnLifecycle
            val version = repository.dataVersion.first()
            if (data == null || version != loadedVersion) {
                val fresh = loadOrNull { repository }
                if (fresh != null) {
                    data = fresh
                    loadedVersion = version
                }
            }
        }
    }

    BackHandler(enabled = state.canPop) { change(state.pop()) }

    val loaded = data
    if (loaded == null) {
        Box(Modifier.fillMaxSize().background(Tokens.bg.color()))
    } else {
        BaupilotAppContent(state = state, onStateChange = change, data = loaded, actions = actions)
    }
}

/** The permission is asked once the user has just logged in, only with push configured and on API 33+. */
internal fun shouldRequestNotifications(pushConfigured: Boolean, sdk: Int, wasLoggedIn: Boolean, isLoggedIn: Boolean): Boolean =
    pushConfigured && sdk >= Build.VERSION_CODES.TIRAMISU && !wasLoggedIn && isLoggedIn

/** Seeds the local store from the bundled fixture; a repository failure is logged and retried on the next start. */
private suspend fun seedOrFalse(repository: LocalRepository): Boolean = try {
    repository.ensureSeeded()
    true
} catch (e: RepositoryException) {
    Log.e(TAG, "Fixture could not be seeded", e)
    false
}

/**
 * D11: builds the repository and loads [AppData] off the main thread. A fixture or decoding error is logged and
 * yields null, so the root keeps its plain background instead of crashing.
 */
internal suspend fun loadOrNull(source: () -> BackendRepository): AppData? = withContext(Dispatchers.Default) {
    try {
        AppData.load(source())
    } catch (e: RepositoryException) {
        Log.e(TAG, "Fixture could not be loaded", e)
        null
    }
}

/** Snapshot entry point (I10): Login while logged out, else the tab shell. */
@Composable
fun BaupilotAppContent(
    state: AppNavigationState,
    onStateChange: (AppNavigationState) -> Unit,
    data: AppData,
    actions: AppActions = AppActions.None,
) {
    val latestState by rememberUpdatedState(state)
    val latestChange by rememberUpdatedState(onStateChange)
    if (state.isLoggedIn) {
        AppShell(state = state, onStateChange = onStateChange, data = data, actions = actions)
    } else {
        LoginScreen(
            model = data.login,
            onSubmit = { email, password -> actions.logIn(email, password) { latestChange(latestState.logIn()) } },
        )
    }
}

/** FileProvider authority of the manifest; the camera writes into the app-private photo store through it. */
private const val FILE_AUTHORITY = "de.baupilot.files"

/** The Android side of [RootActions]; the launchers are set by [BaupilotRoot] on every composition. */
private class AndroidRootEnvironment(private val context: Context, private val view: View) : RootEnvironment {
    var camera: ActivityResultLauncher<Uri>? = null
    var picker: ActivityResultLauncher<Array<String>>? = null
    private val main = Handler(Looper.getMainLooper())

    override fun toast(text: String) {
        val show = { Toast.makeText(context, text, Toast.LENGTH_SHORT).show() }
        if (Looper.myLooper() == Looper.getMainLooper()) show() else main.post(show)
    }

    override fun uriFor(file: File): Uri = FileProvider.getUriForFile(context, FILE_AUTHORITY, file)

    override fun launchCamera(target: Uri) {
        (camera ?: throw ActivityNotFoundException("No camera launcher")).launch(target)
    }

    override fun launchDocument() {
        (picker ?: throw ActivityNotFoundException("No document launcher")).launch(arrayOf("application/pdf"))
    }

    override fun source(uri: Uri): ImportSource = object : ImportSource {
        override val displayName: String? = try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        } catch (e: RuntimeException) {
            null
        }

        override fun open(): InputStream =
            context.contentResolver.openInputStream(uri) ?: throw FileNotFoundException(uri.toString())
    }

    override fun confirm() {
        val kind = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.VIRTUAL_KEY
        }
        view.performHapticFeedback(kind)
    }
}
