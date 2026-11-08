package de.baupilot.snapshot

import app.cash.paparazzi.Snapshot
import app.cash.paparazzi.SnapshotHandler
import java.io.File

/**
 * Mirrors Paparazzi's golden file name `<package>_<Class>_<method>[_<name>].png`.
 *
 * Paparazzi builds it in the internal `Snapshot.toFileName(delimiter = "_", extension = "png")`,
 * see `Snapshot.kt` in https://github.com/cashapp/paparazzi/tree/2.0.0-alpha05.1 (used by
 * `SnapshotVerifier.newFrameHandler`). A mismatch makes the verifier compare against a blank
 * image, which the canary test in this package catches.
 */
fun goldenFileName(snapshot: Snapshot): String {
    val suffix = snapshot.name?.let { "_" + it.lowercase().replace(Regex("\\s"), "_") } ?: ""
    val test = snapshot.testName
    return "${test.packageName}_${test.className}_${test.methodName.replace(Regex("\\s"), "_")}$suffix.png"
}

/**
 * Compares each frame with `design/reference/android/<name>_<mode>.png`.
 *
 * Thin wrapper over [StagedSnapshotHandler]: the reference is staged under
 * `build/paparazzi-reference/images`, the comparison uses [MAX_PERCENT_DIFFERENCE]. Nothing is
 * written into `design/`.
 */
class ReferenceSnapshotHandler(
    name: String,
    mode: SnapshotMode,
    writeArtifact: Boolean = true,
) : SnapshotHandler {
    private val repoRoot = File(requireNotNull(System.getProperty("baupilot.repoRoot")) {
        "System property baupilot.repoRoot fehlt"
    })
    private val reference = File(repoRoot, "design/reference/android/${name}_${mode.fileSuffix}.png")
    private val delegate = StagedSnapshotHandler(
        referenceFile = { reference },
        artifactFile = {
            if (writeArtifact) File(repoRoot, "build/snapshots/android/${name}_${mode.fileSuffix}.png") else null
        },
        stagedRoot = File("build/paparazzi-reference"),
        missingMessage = "Referenz fehlt: ${reference.path}",
    )

    override fun newFrameHandler(
        snapshot: Snapshot,
        frameCount: Int,
        fps: Int,
    ): SnapshotHandler.FrameHandler = delegate.newFrameHandler(snapshot, frameCount, fps)

    override fun close() = delegate.close()
}
