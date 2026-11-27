package de.baupilot.capture

import androidx.exifinterface.media.ExifInterface
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileStoreTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private fun store() = FileStore(tmp.newFolder("files"))

    private fun write(file: File, text: String = "x", modified: Long? = null): File {
        file.parentFile!!.mkdirs()
        file.writeText(text)
        if (modified != null) file.setLastModified(modified)
        return file
    }

    @Test
    fun pathsFollowTheLayoutAndCreateParents() {
        val s = store()
        val photo = s.newPhotoFile("p1", "ph1")
        assertTrue(photo.parentFile!!.isDirectory)
        assertEquals("projects/p1/photos/ph1.jpg", s.relative(photo))
        assertEquals("projects/p1/documents/d1.pdf", s.relative(s.documentFile("p1", "d1")))
        assertEquals("projects/p1/protocols/r1.pdf", s.relative(s.protocolFile("p1", "r1")))
        assertEquals(photo.canonicalFile, s.resolve("projects/p1/photos/ph1.jpg"))
    }

    @Test
    fun resolveRefusesEscapingPaths() {
        val s = store()
        for (bad in listOf("../outside.txt", "projects/../../outside.txt", "/etc/passwd", "")) {
            val failed = runCatching { s.resolve(bad) }.exceptionOrNull()
            assertTrue("$bad should be refused", failed is IllegalArgumentException)
        }
        assertTrue(runCatching { s.relative(File(tmp.root, "outside.txt")) }.exceptionOrNull() is IllegalArgumentException)
        assertTrue(runCatching { s.newPhotoFile("../x", "a") }.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun sha256KnownVector() {
        val f = write(File(tmp.root, "abc.txt"), "abc")
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            store().sha256(f),
        )
    }

    @Test
    fun deleteProjectRemovesTree() {
        val s = store()
        write(s.newPhotoFile("p1", "a"))
        write(s.documentFile("p1", "d"))
        write(s.newPhotoFile("p2", "b"))
        assertTrue(s.deleteProject("p1"))
        assertFalse(s.projectDir("p1").exists())
        assertTrue(s.projectDir("p2").exists())
        assertTrue(s.deleteProject("p1"))
    }

    @Test
    fun sweepRemovesUnknownProjectDirsAndOldOrphansButKeepsFreshFiles() {
        val s = store()
        val now = 1_700_000_000_000L
        val hour = 3_600_000L
        val referenced = write(s.newPhotoFile("p1", "kept"), modified = now - 5 * hour)
        val oldOrphan = write(s.newPhotoFile("p1", "old"), modified = now - 2 * hour)
        val fresh = write(s.newPhotoFile("p1", "fresh"), modified = now - 1000)
        val gone = write(s.newPhotoFile("gone", "x"), modified = now)

        val removed = s.sweepOrphans(setOf("p1", s.relative(referenced)), hour, now)

        assertEquals(2, removed)
        assertTrue(referenced.exists())
        assertTrue(fresh.exists())
        assertFalse(oldOrphan.exists())
        assertFalse(gone.exists())
        assertFalse(s.projectDir("gone").exists())
    }

    @Test
    fun stripLocationRemovesGpsKeepsOrientation() {
        val jpeg = File(tmp.root, "geo.jpg").apply { writeBytes(MINIMAL_JPEG) }
        ExifInterface(jpeg).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
            setAttribute(ExifInterface.TAG_GPS_LATITUDE, "52/1,31/1,0/1")
            setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N")
            setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "13/1,24/1,0/1")
            setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E")
            saveAttributes()
        }
        assertEquals("N", ExifInterface(jpeg).getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF))

        ExifScrubber.stripLocation(jpeg)

        val after = ExifInterface(jpeg)
        assertNull(after.getAttribute(ExifInterface.TAG_GPS_LATITUDE))
        assertNull(after.getAttribute(ExifInterface.TAG_GPS_LONGITUDE))
        assertNull(after.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF))
        assertNull(after.latLong)
        assertEquals(ExifInterface.ORIENTATION_ROTATE_90, after.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0))
    }

    @Test
    fun stripLocationIsNoOpForNonJpeg() {
        val pdf = write(File(tmp.root, "a.pdf"), "%PDF-1.4 not a jpeg")
        val before = pdf.readBytes()
        ExifScrubber.stripLocation(pdf)
        assertTrue(before.contentEquals(pdf.readBytes()))
        ExifScrubber.stripLocation(File(tmp.root, "missing.jpg"))
    }

    private companion object {
        /** SOI, JFIF APP0, baseline SOF0 (1x1, one component), SOS, one scan byte, EOI. */
        val MINIMAL_JPEG: ByteArray = intArrayOf(
            0xFF, 0xD8,
            0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
            0xFF, 0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x11, 0x00,
            0xFF, 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00,
            0x00,
            0xFF, 0xD9,
        ).map { it.toByte() }.toByteArray()
    }
}
