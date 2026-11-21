package de.baupilot.snapshot

import app.cash.paparazzi.Snapshot
import app.cash.paparazzi.SnapshotHandler
import java.awt.image.BufferedImage
import java.io.File
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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

/** Verdict of one screen snapshot as written to `<Name>_<mode>.result.json`. */
data class SnapshotResult(val passed: Boolean, val message: String, val value: Double?)

/**
 * Compares each frame with `design/reference/android/<name>_<mode>.png`.
 *
 * Thin wrapper over [StagedSnapshotHandler]: the reference is staged under
 * `build/paparazzi-reference/images`, the comparison uses [MAX_PERCENT_DIFFERENCE]. Nothing is
 * written into `design/`.
 *
 * When an artifact is written (not for the canary) the verdict is also written next to it as
 * `<name>_<mode>.result.json`. The verdict is `passed`; the failure message is only parsed for the
 * reported value and never decides anything. The original error is rethrown unchanged.
 */
class ReferenceSnapshotHandler(
    private val name: String,
    private val mode: SnapshotMode,
    writeArtifact: Boolean = true,
    private val repoRoot: File = File(requireNotNull(System.getProperty("baupilot.repoRoot")) {
        "System property baupilot.repoRoot fehlt"
    }),
    /** Test seam: wraps the comparing delegate, e.g. to record what it throws. Identity by default. */
    decorate: (SnapshotHandler) -> SnapshotHandler = { it },
) : SnapshotHandler {
    private val reference = File(repoRoot, "design/reference/android/${name}_${mode.fileSuffix}.png")
    private val artifact: File? =
        if (writeArtifact) File(repoRoot, "build/snapshots/android/${name}_${mode.fileSuffix}.png") else null
    private val delegate = decorate(
        StagedSnapshotHandler(
            referenceFile = { reference },
            artifactFile = { artifact },
            stagedRoot = File("build/paparazzi-reference"),
            missingMessage = "Referenz fehlt: ${reference.path}",
        ),
    )

    override fun newFrameHandler(
        snapshot: Snapshot,
        frameCount: Int,
        fps: Int,
    ): SnapshotHandler.FrameHandler {
        val frameHandler = delegate.newFrameHandler(snapshot, frameCount, fps)
        return object : SnapshotHandler.FrameHandler {
            override fun handle(image: BufferedImage) {
                try {
                    frameHandler.handle(image)
                } catch (t: Throwable) {
                    write(result(passed = false, message = t.message))
                    throw t
                }
                write(result(passed = true, message = null))
            }

            override fun close() = frameHandler.close()
        }
    }

    override fun close() = delegate.close()

    private fun write(result: SnapshotResult) {
        val png = artifact ?: return
        val sidecar = File(png.parentFile, png.name.removeSuffix(".png") + ".result.json")
        val json = buildJsonObject {
            put("platform", "android")
            put("name", name)
            put("mode", mode.fileSuffix)
            put("passed", result.passed)
            put("message", result.message)
            put("value", result.value)
            put("required", MAX_PERCENT_DIFFERENCE)
            put("unit", "percent")
        }
        sidecar.parentFile?.mkdirs()
        sidecar.writeText(json.toString())
    }

    companion object {
        private val DIFFER = Regex("""Images differ \(by ([0-9]+[.,][0-9]+)%\)""")

        /**
         * Pure: `passed` is taken as given. `value` is the percentage from Paparazzi's failure
         * message (decimal comma accepted, the JVM default locale may be de_DE); null on a pass or
         * when the message has no such number.
         */
        fun result(passed: Boolean, message: String?): SnapshotResult {
            val value = if (passed) null else message
                ?.let { DIFFER.find(it)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull() }
            return SnapshotResult(passed, message.orEmpty(), value)
        }
    }
}
