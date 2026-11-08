package de.baupilot.snapshot

import app.cash.paparazzi.Snapshot
import app.cash.paparazzi.TestName
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.util.Date
import javax.imageio.ImageIO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GoldenSnapshotHandlerTest {
    private fun snapshot(name: String?) = Snapshot(
        name = name,
        testName = TestName(
            packageName = "de.baupilot.snapshot",
            className = "GoldenSnapshotHandlerTest",
            methodName = "probe",
        ),
        timestamp = Date(0),
        tags = emptyList(),
        file = null,
    )

    private fun tempRoot(): File = Files.createTempDirectory("baupilot-golden").toFile()

    private fun golden(root: File, name: String) =
        File(root, "android/app/src/test/goldens/$name.png")

    @Test
    fun missingGoldenFailsWithIllegalState() {
        val handler = GoldenSnapshotHandler(SnapshotMode.LIGHT, record = false, repoRoot = tempRoot())

        val error = assertThrows(IllegalStateException::class.java) {
            handler.newFrameHandler(snapshot("Probe"), 1, -1)
        }

        assertTrue(error.message, error.message!!.startsWith("Golden fehlt: "))
        assertTrue(error.message, error.message!!.contains("Probe_light.png"))
        assertTrue(error.message, error.message!!.contains("-Pbaupilot.recordGoldens=true"))
    }

    @Test
    fun missingSnapshotNameFails() {
        val handler = GoldenSnapshotHandler(SnapshotMode.LIGHT, record = false, repoRoot = tempRoot())

        val error = assertThrows(IllegalStateException::class.java) {
            handler.newFrameHandler(snapshot(null), 1, -1)
        }

        assertEquals("Snapshot-Name fehlt", error.message)
    }

    @Test
    fun recordModeWritesGoldenThenThrowsAssertion() {
        val root = tempRoot()
        val handler = GoldenSnapshotHandler(SnapshotMode.DARK, record = true, repoRoot = root)
        val frame = handler.newFrameHandler(snapshot("Probe"), 1, -1)

        val error = assertThrows(AssertionError::class.java) {
            frame.handle(BufferedImage(SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT, BufferedImage.TYPE_INT_ARGB))
        }

        val target = golden(root, "Probe_dark")
        assertTrue(error.message, error.message!!.startsWith("Golden aufgenommen: "))
        assertTrue(target.isFile)
        val written = ImageIO.read(target)
        assertEquals(SNAPSHOT_WIDTH, written.width)
        assertEquals(SNAPSHOT_HEIGHT, written.height)
    }

    @Test
    fun recordModeRejectsWrongSizeWithoutWriting() {
        val root = tempRoot()
        val frame = GoldenSnapshotHandler(SnapshotMode.LIGHT, record = true, repoRoot = root)
            .newFrameHandler(snapshot("Probe"), 1, -1)

        val error = assertThrows(Throwable::class.java) {
            frame.handle(BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB))
        }

        assertTrue(error.toString(), error is IllegalStateException)
        assertFalse(golden(root, "Probe_light").exists())
    }

    @Test
    fun wrongSizeIsIllegalState() {
        val root = tempRoot()
        val target = golden(root, "Probe_light")
        target.parentFile!!.mkdirs()
        ImageIO.write(BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB), "png", target)
        val frame = GoldenSnapshotHandler(SnapshotMode.LIGHT, record = false, repoRoot = root)
            .newFrameHandler(snapshot("Probe"), 1, -1)

        val error = assertThrows(Throwable::class.java) {
            frame.handle(BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB))
        }

        assertTrue(error.toString(), error is IllegalStateException)
        assertFalse(error.toString(), error is AssertionError)
        assertTrue(error.message, error.message!!.startsWith("Größe 10x10"))
    }
}
