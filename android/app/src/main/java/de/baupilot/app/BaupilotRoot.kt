package de.baupilot.app

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import de.baupilot.data.BackendRepository
import de.baupilot.data.MockRepository
import de.baupilot.data.RepositoryException
import de.baupilot.designsystem.Tokens
import de.baupilot.screens.login.LoginScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "BaupilotRoot"

/**
 * App root (D11, D17): loads [AppData] once from the bundled fixture and keeps the navigation state across
 * configuration changes. Back is consumed only while a pushed route is open (R14).
 */
@Composable
fun BaupilotRoot() {
    val context = LocalContext.current
    val data by produceState<AppData?>(initialValue = null) {
        value = loadOrNull { MockRepository.fromAssets(context) }
    }
    var state by rememberSaveable(stateSaver = AppNavigationState.Saver) { mutableStateOf(AppNavigationState()) }

    BackHandler(enabled = state.canPop) { state = state.pop() }

    val loaded = data
    if (loaded == null) {
        Box(Modifier.fillMaxSize().background(Tokens.bg.color()))
    } else {
        BaupilotAppContent(state = state, onStateChange = { state = it }, data = loaded)
    }
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
) {
    if (state.isLoggedIn) {
        AppShell(state = state, onStateChange = onStateChange, data = data)
    } else {
        LoginScreen(model = data.login, onSubmit = { onStateChange(state.logIn()) })
    }
}
