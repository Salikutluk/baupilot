package de.baupilot.snapshot

import app.cash.paparazzi.Snapshot
import app.cash.paparazzi.SnapshotHandler
import app.cash.paparazzi.SnapshotVerifier
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

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
 * The reference is staged under `build/paparazzi-reference/images` with the golden file name,
 * (the directory is emptied before each staging), then [SnapshotVerifier] does the comparison with [MAX_PERCENT_DIFFERENCE]. Nothing is
 * written into `design/`.
 */
class ReferenceSnapshotHandler(
    private val name: String,
    private val mode: SnapshotMode,
    private val writeArtifact: Boolean = true,
) : SnapshotHandler {
    private val repoRoot = File(requireNotNull(System.getProperty("baupilot.repoRoot")) {
        "System property baupilot.repoRoot fehlt"
    })
    private val stagedRoot = File("build/paparazzi-reference")

    override fun newFrameHandler(
        snapshot: Snapshot,
        frameCount: Int,
        fps: Int,
    ): SnapshotHandler.FrameHandler {
        val reference = File(repoRoot, "design/reference/android/${name}_${mode.fileSuffix}.png")
        check(reference.isFile) { "Referenz fehlt: ${reference.path}" }

        // Wipe first: a stale golden from an earlier run would hide a wrong staged file name.
        val images = File(stagedRoot, "images").apply {
            deleteRecursively()
            mkdirs()
        }
        reference.copyTo(File(images, goldenFileName(snapshot)), overwrite = true)

        val verifier = SnapshotVerifier(MAX_PERCENT_DIFFERENCE, stagedRoot)
            .newFrameHandler(snapshot, frameCount, fps)
        return object : SnapshotHandler.FrameHandler {
            override fun handle(image: BufferedImage) {
                if (writeArtifact) writeArtifact(image)
                check(image.width == SNAPSHOT_WIDTH && image.height == SNAPSHOT_HEIGHT) {
                    "Größe ${image.width}x${image.height}, erwartet ${SNAPSHOT_WIDTH}x$SNAPSHOT_HEIGHT"
                }
                verifier.handle(image)
            }

            override fun close() = verifier.close()
        }
    }

    private fun writeArtifact(image: BufferedImage) {
        val target = File(repoRoot, "build/snapshots/android/${name}_${mode.fileSuffix}.png")
        target.parentFile?.mkdirs()
        ImageIO.write(image, "png", target)
    }

    override fun close() = Unit
}
