package de.baupilot.snapshot

import de.baupilot.RootScreen
import org.junit.Rule
import org.junit.Test

/** Expected red until M2 replaces the placeholder `RootScreen` with the Login screen. */
class LoginExampleSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(
        handler = ReferenceSnapshotHandler(name = "Login", mode = SnapshotMode.LIGHT),
    )

    @Test
    fun loginLightAgainstReference() {
        expectRedUntil("M2") {
            paparazzi.snapshot { RootScreen() }
        }
    }
}
