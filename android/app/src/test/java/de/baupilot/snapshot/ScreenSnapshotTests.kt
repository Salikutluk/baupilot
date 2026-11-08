package de.baupilot.snapshot

import de.baupilot.app.AppNavigationState
import de.baupilot.app.AppTab
import de.baupilot.app.BaupilotAppContent
import org.junit.Rule
import org.junit.Test

/** Login while logged out, compared with `design/reference/android/Login_light.png`. */
class LoginLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Login", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun login() {
        val data = TestAppData.load()
        paparazzi.snapshot { BaupilotAppContent(state = AppNavigationState(), onStateChange = {}, data = data) }
    }
}

/** Login while logged out, compared with `design/reference/android/Login_dark.png`. */
class LoginDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Login", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun login() {
        val data = TestAppData.load()
        paparazzi.snapshot { BaupilotAppContent(state = AppNavigationState(), onStateChange = {}, data = data) }
    }
}

/** Home tab, logged in, compared with `design/reference/android/Home_light.png`. */
class HomeLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Home", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun home() {
        val data = TestAppData.load()
        paparazzi.snapshot { BaupilotAppContent(state = AppNavigationState().logIn(), onStateChange = {}, data = data) }
    }
}

/** Home tab, logged in, compared with `design/reference/android/Home_dark.png`. */
class HomeDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Home", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun home() {
        val data = TestAppData.load()
        paparazzi.snapshot { BaupilotAppContent(state = AppNavigationState().logIn(), onStateChange = {}, data = data) }
    }
}

/** Zeitplan tab, logged in, compared with `design/reference/android/Zeitplan_light.png`. */
class ZeitplanLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Zeitplan", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun zeitplan() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Schedule)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Zeitplan tab, logged in, compared with `design/reference/android/Zeitplan_dark.png`. */
class ZeitplanDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Zeitplan", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun zeitplan() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Schedule)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Profil tab, logged in, compared with `design/reference/android/Profil_light.png`. */
class ProfilLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Profil", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun profil() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Profile)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Profil tab, logged in, compared with `design/reference/android/Profil_dark.png`. */
class ProfilDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Profil", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun profil() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Profile)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}
