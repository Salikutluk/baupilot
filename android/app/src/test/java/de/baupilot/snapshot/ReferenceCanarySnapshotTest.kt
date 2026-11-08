package de.baupilot.snapshot

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File
import javax.imageio.ImageIO
import org.junit.Rule
import org.junit.Test

/**
 * Strict canary: renders the Login reference itself and must match it. This proves the golden
 * file name mirror, the true device resolution (1170 x 2532) and the verifier wiring. Without it
 * a mis-staged reference would be compared against a blank image.
 */
class ReferenceCanarySnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(
        handler = ReferenceSnapshotHandler(name = "Login", mode = SnapshotMode.LIGHT, writeArtifact = false),
    )

    @Test
    fun canaryMatchesLoginLight() {
        val reference = File(System.getProperty("baupilot.repoRoot"), "design/reference/android/Login_light.png")
        paparazzi.snapshot("Login Light") {
            Image(
                bitmap = decode(reference).asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                filterQuality = FilterQuality.None,
            )
        }
    }

    /** `BitmapFactory.decodeFile` returns null in layoutlib, so decode with ImageIO. */
    private fun decode(file: File): Bitmap {
        val image = requireNotNull(ImageIO.read(file)) { "Referenz nicht lesbar: ${file.path}" }
        val pixels = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
        return Bitmap.createBitmap(pixels, image.width, image.height, Bitmap.Config.ARGB_8888)
    }
}
