package de.baupilot.snapshot

import app.cash.paparazzi.Snapshot
import app.cash.paparazzi.TestName
import java.awt.image.BufferedImage
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
}
