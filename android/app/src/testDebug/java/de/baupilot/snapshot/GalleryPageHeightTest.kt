package de.baupilot.snapshot

import de.baupilot.gallery.checkPageHeight
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** T-AGAL-2: the page-height guard trips only above the screen height. */
class GalleryPageHeightTest {
    @Test
    fun acceptsPageUpToTheLimit() {
        checkPageHeight("GalleryBars", 2532, 2532)
    }

    @Test
    fun rejectsPageAboveTheLimit() {
        val error = assertThrows(IllegalStateException::class.java) {
            checkPageHeight("GalleryBars", 2533, 2532)
        }
        assertTrue(error.message, error.message!!.contains("GalleryBars"))
    }
}
