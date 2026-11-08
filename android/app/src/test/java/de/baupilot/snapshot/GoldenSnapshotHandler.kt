package de.baupilot.snapshot

import app.cash.paparazzi.Snapshot
import app.cash.paparazzi.SnapshotHandler
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Verifies each frame against `android/app/src/test/goldens/<name>_<mode>.png`.
 *
 * Always compares through [StagedSnapshotHandler] (and so [MAX_PERCENT_DIFFERENCE]), which means
 * plain `testDebugUnitTest` compares and Paparazzi's own record/verify tasks are not used. With
 * [record] (`-Pbaupilot.recordGoldens=true`) the frame is written as the golden instead, and the
 * test then fails with an [AssertionError] so a recording run is never mistaken for a green one.
 * The snapshot name is required; it must be plain ASCII letters (the file name is not validated).
 */
class GoldenSnapshotHandler(
    private val mode: SnapshotMode,
    private val record: Boolean = System.getProperty("baupilot.recordGoldens") == "true",
    private val repoRoot: File = File(requireNotNull(System.getProperty("baupilot.repoRoot")) {
        "System property baupilot.repoRoot fehlt"
    }),
) : SnapshotHandler {
    override fun newFrameHandler(
        snapshot: Snapshot,
        frameCount: Int,
        fps: Int,
    ): SnapshotHandler.FrameHandler {
        val name = checkNotNull(snapshot.name) { "Snapshot-Name fehlt" }
        val golden = File(repoRoot, "android/app/src/test/goldens/${name}_${mode.fileSuffix}.png")
        return if (record) recorder(golden) else verifier(name, golden, snapshot, frameCount, fps)
    }

    private fun verifier(
        name: String,
        golden: File,
        snapshot: Snapshot,
        frameCount: Int,
        fps: Int,
    ): SnapshotHandler.FrameHandler = StagedSnapshotHandler(
        referenceFile = { golden },
        artifactFile = { File(repoRoot, "build/snapshots/android-gallery/${name}_${mode.fileSuffix}.png") },
        stagedRoot = File("build/paparazzi-golden"),
        missingMessage = "Golden fehlt: ${golden.path} – mit -Pbaupilot.recordGoldens=true aufnehmen",
    ).newFrameHandler(snapshot, frameCount, fps)

    private fun recorder(golden: File) = object : SnapshotHandler.FrameHandler {
        override fun handle(image: BufferedImage) {
            check(image.width == SNAPSHOT_WIDTH && image.height == SNAPSHOT_HEIGHT) {
                "Größe ${image.width}x${image.height}, erwartet ${SNAPSHOT_WIDTH}x$SNAPSHOT_HEIGHT"
            }
            golden.parentFile?.mkdirs()
            ImageIO.write(image, "png", golden)
            throw AssertionError("Golden aufgenommen: ${golden.path} – ohne Record erneut ausführen")
        }

        override fun close() = Unit
    }

    override fun close() = Unit
}
