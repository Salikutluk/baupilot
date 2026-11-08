package de.baupilot.snapshot

import app.cash.paparazzi.Paparazzi
import de.baupilot.gallery.GalleryPage
import de.baupilot.gallery.GalleryPageContent
import org.junit.Rule
import org.junit.Test

/** Renders every gallery page and compares it with its golden. Missing goldens throw immediately. */
private fun Paparazzi.snapshotPages() {
    val failures = mutableListOf<String>()
    GalleryPage.entries.forEach { page ->
        try {
            snapshot(name = page.goldenName) { GalleryPageContent(page) }
        } catch (e: AssertionError) {
            failures += "${page.goldenName}: ${e.message}"
        }
    }
    if (failures.isNotEmpty()) {
        throw AssertionError("Galerie-Seiten weichen ab:\n" + failures.joinToString("\n"))
    }
}

class GalleryLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(GoldenSnapshotHandler(SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun allPages() = paparazzi.snapshotPages()
}

class GalleryDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(GoldenSnapshotHandler(SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun allPages() = paparazzi.snapshotPages()
}
