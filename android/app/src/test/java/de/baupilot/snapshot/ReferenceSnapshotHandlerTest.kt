package de.baupilot.snapshot

import app.cash.paparazzi.Snapshot
import app.cash.paparazzi.SnapshotHandler
import app.cash.paparazzi.TestName
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.util.Date
import javax.imageio.ImageIO
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceSnapshotHandlerTest {
    private fun snapshot(name: String?) = Snapshot(
        name = name,
        testName = TestName(
            packageName = "de.baupilot.snapshot",
            className = "ReferenceCanarySnapshotTest",
            methodName = "canaryMatchesLoginLight",
        ),
        timestamp = Date(0),
        tags = emptyList(),
        file = null,
    )

    @Test
    fun missingReferenceThrowsIllegalStateBeforeAnyComparison() {
        val handler = ReferenceSnapshotHandler(name = "GibtEsNicht", mode = SnapshotMode.LIGHT)

        val error = assertThrows(IllegalStateException::class.java) {
            handler.newFrameHandler(snapshot("Gibt Es Nicht"), 1, 1)
        }

        assertTrue(error.message, error.message!!.startsWith("Referenz fehlt: "))
        assertTrue(error.message, error.message!!.endsWith("GibtEsNicht_light.png"))
    }

    @Test
    fun wrongFrameSizeThrowsIllegalState() {
        val frameHandler = ReferenceSnapshotHandler("Login", SnapshotMode.LIGHT, writeArtifact = false)
            .newFrameHandler(snapshot("Login Light"), 1, -1)

        val error = assertThrows(Throwable::class.java) {
            frameHandler.handle(BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB))
        }

        assertTrue(error.toString(), error is IllegalStateException)
        assertFalse(error.toString(), error is AssertionError)
        assertTrue(error.message, error.message!!.startsWith("Größe 10x10"))
    }

    @Test
    fun goldenFileNameMirrorsPaparazziScheme() {
        assertEquals(
            "de.baupilot.snapshot_ReferenceCanarySnapshotTest_canaryMatchesLoginLight_login_light.png",
            goldenFileName(snapshot("Login Light")),
        )
    }

    @Test
    fun goldenFileNameWithoutSnapshotNameHasNoSuffix() {
        assertEquals(
            "de.baupilot.snapshot_ReferenceCanarySnapshotTest_canaryMatchesLoginLight.png",
            goldenFileName(snapshot(null)),
        )
    }

    @Test
    fun modeFileSuffixIsLowercase() {
        assertEquals("light", SnapshotMode.LIGHT.fileSuffix)
        assertEquals("dark", SnapshotMode.DARK.fileSuffix)
    }

    private fun solid(rgb: Int) = BufferedImage(SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT, BufferedImage.TYPE_INT_ARGB).apply {
        for (y in 0 until height) for (x in 0 until width) setRGB(x, y, rgb)
    }

    /** A temp repo root holding `design/reference/android/Probe_light.png` in the given colour. */
    private fun rootWithReference(rgb: Int): File {
        val root = Files.createTempDirectory("baupilot-reference").toFile()
        val reference = File(root, "design/reference/android/Probe_light.png")
        reference.parentFile!!.mkdirs()
        ImageIO.write(solid(rgb), "png", reference)
        return root
    }

    private fun sidecar(root: File) = File(root, "build/snapshots/android/Probe_light.result.json")

    private fun readSidecar(root: File): JsonObject =
        Json.parseToJsonElement(sidecar(root).readText()).jsonObject

    /** Wraps [delegate] so every Throwable its frame handler throws is appended to [thrown]. */
    private fun recordingThrown(delegate: SnapshotHandler, thrown: MutableList<Throwable>) = object : SnapshotHandler {
        override fun newFrameHandler(snapshot: Snapshot, frameCount: Int, fps: Int): SnapshotHandler.FrameHandler {
            val inner = delegate.newFrameHandler(snapshot, frameCount, fps)
            return object : SnapshotHandler.FrameHandler {
                override fun handle(image: BufferedImage) {
                    try {
                        inner.handle(image)
                    } catch (t: Throwable) {
                        thrown += t
                        throw t
                    }
                }

                override fun close() = inner.close()
            }
        }

        override fun close() = delegate.close()
    }

    @Test
    fun sidecarWrittenOnFailureAndSameErrorRethrown() {
        val root = rootWithReference(0xFFFFFFFF.toInt()) // lint-hex: allow synthetic test pixel
        val thrownByDelegate = mutableListOf<Throwable>()
        val frameHandler = ReferenceSnapshotHandler(
            "Probe",
            SnapshotMode.LIGHT,
            repoRoot = root,
            decorate = { delegate -> recordingThrown(delegate, thrownByDelegate) },
        ).newFrameHandler(snapshot("Probe Light"), 1, -1)

        val error = assertThrows(AssertionError::class.java) { frameHandler.handle(solid(0xFF000000.toInt())) } // lint-hex: allow synthetic test pixel

        assertEquals(1, thrownByDelegate.size)
        assertSame(thrownByDelegate.single(), error)
        assertTrue(error.message, error.message!!.startsWith("Images differ"))
        val json = readSidecar(root)
        assertEquals("android", json.getValue("platform").jsonPrimitive.content)
        assertEquals("Probe", json.getValue("name").jsonPrimitive.content)
        assertEquals("light", json.getValue("mode").jsonPrimitive.content)
        assertFalse(json.getValue("passed").jsonPrimitive.boolean)
        assertEquals(error.message, json.getValue("message").jsonPrimitive.content)
        assertEquals(MAX_PERCENT_DIFFERENCE, json.getValue("required").jsonPrimitive.double, 0.0)
        assertEquals("percent", json.getValue("unit").jsonPrimitive.content)
        assertTrue(json.getValue("value").jsonPrimitive.double > MAX_PERCENT_DIFFERENCE)
        assertTrue(File(root, "build/snapshots/android/Probe_light.png").isFile)
    }

    @Test
    fun sidecarOnPassHasNullValue() {
        val root = rootWithReference(0xFFFFFFFF.toInt()) // lint-hex: allow synthetic test pixel
        val frameHandler = ReferenceSnapshotHandler("Probe", SnapshotMode.LIGHT, repoRoot = root)
            .newFrameHandler(snapshot("Probe Light"), 1, -1)

        frameHandler.handle(solid(0xFFFFFFFF.toInt())) // lint-hex: allow synthetic test pixel

        val json = readSidecar(root)
        assertTrue(json.getValue("passed").jsonPrimitive.boolean)
        assertEquals("", json.getValue("message").jsonPrimitive.content)
        assertSame(JsonNull, json.getValue("value"))
    }

    @Test
    fun sidecarOfWrongSizeFrameHasNullValue() {
        val root = rootWithReference(0xFFFFFFFF.toInt()) // lint-hex: allow synthetic test pixel
        val frameHandler = ReferenceSnapshotHandler("Probe", SnapshotMode.LIGHT, repoRoot = root)
            .newFrameHandler(snapshot("Probe Light"), 1, -1)

        assertThrows(IllegalStateException::class.java) {
            frameHandler.handle(BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB))
        }

        val json = readSidecar(root)
        assertFalse(json.getValue("passed").jsonPrimitive.boolean)
        assertSame(JsonNull, json.getValue("value"))
    }

    @Test
    fun sidecarParsesDecimalComma() {
        val comma = ReferenceSnapshotHandler.result(false, "Images differ (by 0,612345%) - see details in file://x")
        val dot = ReferenceSnapshotHandler.result(false, "Images differ (by 12.5%) - see details")

        assertEquals(0.612345, comma.value!!, 0.0)
        assertEquals(12.5, dot.value!!, 0.0)
        assertNull(ReferenceSnapshotHandler.result(false, "Größe 10x10, erwartet 1170x2532").value)
        assertNull(ReferenceSnapshotHandler.result(false, null).value)
        assertNull(ReferenceSnapshotHandler.result(true, "Images differ (by 0,6%)").value)
        assertFalse(ReferenceSnapshotHandler.result(false, "Images differ (by 0,6%)").passed)
        assertTrue(ReferenceSnapshotHandler.result(true, null).passed)
    }

    @Test
    fun canaryWithoutArtifactWritesNoSidecar() {
        val root = rootWithReference(0xFFFFFFFF.toInt()) // lint-hex: allow synthetic test pixel
        val frameHandler = ReferenceSnapshotHandler("Probe", SnapshotMode.LIGHT, writeArtifact = false, repoRoot = root)
            .newFrameHandler(snapshot("Probe Light"), 1, -1)

        assertThrows(AssertionError::class.java) { frameHandler.handle(solid(0xFF000000.toInt())) } // lint-hex: allow synthetic test pixel

        assertFalse(File(root, "build").exists())
    }
}
