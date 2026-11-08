package de.baupilot.snapshot

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.SnapshotHandler
import app.cash.paparazzi.detectEnvironment
import com.android.resources.Density
import com.android.resources.NightMode

/** Tolerance of every Android snapshot comparison, in percent. Never loosen. */
const val MAX_PERCENT_DIFFERENCE = 0.5

enum class SnapshotMode(val fileSuffix: String, val nightMode: NightMode) {
    LIGHT("light", NightMode.NOTNIGHT),
    DARK("dark", NightMode.NIGHT),
}

/** 1170 x 2532 px at 480 dpi, rendered at true device resolution (not downscaled to 1000 px). */
fun baupilotPaparazzi(
    handler: SnapshotHandler,
    mode: SnapshotMode = SnapshotMode.LIGHT,
): Paparazzi = Paparazzi(
    environment = detectEnvironment(),
    deviceConfig = DeviceConfig(
        screenHeight = SNAPSHOT_HEIGHT,
        screenWidth = SNAPSHOT_WIDTH,
        xdpi = 480,
        ydpi = 480,
        density = Density.XXHIGH,
        nightMode = mode.nightMode,
    ),
    theme = "android:Theme.Material.NoActionBar.Fullscreen",
    showSystemUi = false,
    useDeviceResolution = true,
    maxPercentDifference = MAX_PERCENT_DIFFERENCE,
    snapshotHandler = handler,
)

const val SNAPSHOT_WIDTH = 1170
const val SNAPSHOT_HEIGHT = 2532
