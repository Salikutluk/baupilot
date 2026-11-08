package de.baupilot.snapshot

import app.cash.paparazzi.Snapshot
import app.cash.paparazzi.SnapshotHandler
import java.awt.image.BufferedImage

/** Ignores every frame. For tests that measure through the view tree instead of pixels. */
object NoopSnapshotHandler : SnapshotHandler {
    override fun newFrameHandler(
        snapshot: Snapshot,
        frameCount: Int,
        fps: Int,
    ): SnapshotHandler.FrameHandler = object : SnapshotHandler.FrameHandler {
        override fun handle(image: BufferedImage) = Unit

        override fun close() = Unit
    }

    override fun close() = Unit
}
