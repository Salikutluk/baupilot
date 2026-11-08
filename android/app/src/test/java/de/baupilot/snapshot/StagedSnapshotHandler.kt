package de.baupilot.snapshot

import app.cash.paparazzi.Snapshot
import app.cash.paparazzi.SnapshotHandler
import app.cash.paparazzi.SnapshotVerifier
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Shared core of the reference and golden handlers.
 *
 * The file from [referenceFile] is staged under `<stagedRoot>/images` with Paparazzi's golden
 * file name (the directory is emptied before each staging), then [SnapshotVerifier] compares
 * with [MAX_PERCENT_DIFFERENCE]. The frame is also written to [artifactFile] when that returns a
 * file. A missing reference fails with [missingMessage] before any comparison.
 */
class StagedSnapshotHandler(
    private val referenceFile: (Snapshot) -> File,
    private val artifactFile: (Snapshot) -> File?,
    private val stagedRoot: File,
    private val missingMessage: String,
) : SnapshotHandler {
    override fun newFrameHandler(
        snapshot: Snapshot,
        frameCount: Int,
        fps: Int,
    ): SnapshotHandler.FrameHandler {
        val reference = referenceFile(snapshot)
        check(reference.isFile) { missingMessage }

        // Wipe first: a stale golden from an earlier run would hide a wrong staged file name.
        val images = File(stagedRoot, "images").apply {
            deleteRecursively()
            mkdirs()
        }
        reference.copyTo(File(images, goldenFileName(snapshot)), overwrite = true)

        val verifier = SnapshotVerifier(MAX_PERCENT_DIFFERENCE, stagedRoot)
            .newFrameHandler(snapshot, frameCount, fps)
        val artifact = artifactFile(snapshot)
        return object : SnapshotHandler.FrameHandler {
            override fun handle(image: BufferedImage) {
                if (artifact != null) {
                    artifact.parentFile?.mkdirs()
                    ImageIO.write(image, "png", artifact)
                }
                check(image.width == SNAPSHOT_WIDTH && image.height == SNAPSHOT_HEIGHT) {
                    "Größe ${image.width}x${image.height}, erwartet ${SNAPSHOT_WIDTH}x$SNAPSHOT_HEIGHT"
                }
                verifier.handle(image)
            }

            override fun close() = verifier.close()
        }
    }

    override fun close() = Unit
}
