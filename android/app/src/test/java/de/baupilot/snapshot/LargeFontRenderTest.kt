package de.baupilot.snapshot

import app.cash.paparazzi.Snapshot
import app.cash.paparazzi.SnapshotHandler
import de.baupilot.app.BaupilotAppContent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Writes every frame to [file]; compares nothing. Like [NoopSnapshotHandler], but with an artifact. */
class ArtifactSnapshotHandler(private val file: File) : SnapshotHandler {
    var written: BufferedImage? = null
        private set

    override fun newFrameHandler(
        snapshot: Snapshot,
        frameCount: Int,
        fps: Int,
    ): SnapshotHandler.FrameHandler = object : SnapshotHandler.FrameHandler {
        override fun handle(image: BufferedImage) {
            file.parentFile?.mkdirs()
            ImageIO.write(image, "png", file)
            written = image
        }

        override fun close() = Unit
    }

    override fun close() = Unit
}

/**
 * Large-text artifacts (plan 5.10, D15): every screen at font scale 1.3, light, written to
 * `build/snapshots/android-large/<Name>_130.png`. Not compared with any reference; only the size is asserted.
 */
@RunWith(Parameterized::class)
class LargeFontRenderTest(private val screen: String) {
    private val handler = ArtifactSnapshotHandler(
        File(requireNotNull(System.getProperty("baupilot.repoRoot")), "build/snapshots/android-large/${screen}_130.png"),
    )

    @get:Rule
    val paparazzi = baupilotPaparazzi(handler, SnapshotMode.LIGHT, fontScale = 1.3f)

    @Test
    fun rendersAtFontScale130() {
        val data = TestAppData.load()
        val state = ScreenCatalog.state(screen)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
        val image = requireNotNull(handler.written) { "$screen: no frame written" }
        assertEquals("$screen width", SNAPSHOT_WIDTH, image.width)
        assertEquals("$screen height", SNAPSHOT_HEIGHT, image.height)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenCatalog.all.map { (name, _) -> arrayOf<Any>(name) }
    }
}
