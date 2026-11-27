package de.baupilot.protocol

import de.baupilot.app.CountTone
import de.baupilot.app.NavigationTarget
import de.baupilot.app.SignOffDefect
import de.baupilot.app.SignOffModel
import de.baupilot.app.SignOffStat
import de.baupilot.capture.FileStore
import de.baupilot.capture.SignOffCapture
import de.baupilot.data.Owner
import de.baupilot.data.Phase
import de.baupilot.data.Project
import de.baupilot.inspection.Extent
import de.baupilot.inspection.Point
import de.baupilot.inspection.SignatureStrokes
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProtocolExporterTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val clock = Clock.fixed(Instant.parse("2026-11-26T09:30:15.123Z"), ZoneOffset.UTC)

    private fun project() = Project(
        id = "pr1", buildingType = "Einfamilienhaus", address = "Hauptstr. 1 (Haus)", constructionStart = "03/2026",
        moveIn = "03/2027", phase = Phase(3, 12, "rohbau", 25), unreadNotifications = 0, owner = Owner("Familie Berger", "FB", "b@example.de", "Bauherr"),
    )

    private fun signOff(reservation: String = "Vorbehalt: 500 € bis zur Nachbesserung") = SignOffModel(
        inspectionId = "elektro", title = "Abnahme Elektro", subtitle = "Sub",
        stats = listOf(SignOffStat("12", "Punkte geprüft", CountTone.Ok), SignOffStat("2", "Mängel", CountTone.Error)),
        reservedHeading = "Vorbehalt", defects = listOf(SignOffDefect("Steckdose schief", "Flur", "Frist 03.12.2026")),
        reservation = reservation, craftsmanLabel = "Handwerker", craftsmanName = "Max Mustermann", craftsmanStatus = "signiert",
        ownerLabel = "Bauherr", signHereLabel = "hier", signatureLabel = "Unterschrift", finishLabel = "Fertig", note = "n",
        finishTarget = NavigationTarget.Defects,
    )

    private fun capture(): SignOffCapture {
        val strokes = SignatureStrokes(Extent(300f, 100f))
            .begin(Point(10f, 10.3f)).extend(Point(100f, 50f)).extend(Point(200f, 90f)).end()
        return SignOffCapture("elektro", "Familie Berger", strokes)
    }

    private var folders = 0

    private fun exporter() = ProtocolExporter(FileStore(tmp.newFolder("files${folders++}")), clock)

    private fun sha(bytes: ByteArray) =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    @Test
    fun exporterWritesFileWhoseShaMatches() {
        val out = exporter().export(signOff(), project(), "Elektro", capture())
        assertTrue(out.file.isFile)
        assertEquals(sha(out.file.readBytes()), out.fileSha256)
        assertEquals(ProtocolHash.contentSha256(out.doc), out.contentSha256)
        assertEquals(ProtocolHash.canonicalJson(out.doc), out.contentJson)
        assertEquals(sha(out.contentJson.toByteArray(Charsets.UTF_8)), out.contentSha256)
        assertTrue(out.file.path.endsWith("projects/pr1/protocols/${out.doc.protocolId}.pdf"))
        assertTrue(String(out.file.readBytes(), Charsets.ISO_8859_1).contains("SHA-256 Inhalt: ${out.contentSha256}"))
    }

    @Test
    fun documentCarriesModelFieldsAndBerlinTime() {
        val doc = exporter().export(signOff(), project(), "Elektro", capture()).doc
        assertEquals("2026-11-26T09:30:15Z", doc.createdAt)
        assertEquals("26.11.2026 10:30", doc.createdLocal)
        assertEquals("pr1", doc.projectId)
        assertEquals("Hauptstr. 1 (Haus)", doc.address)
        assertEquals("elektro", doc.tradeId)
        assertEquals("Elektro", doc.tradeName)
        assertEquals("Abnahme Elektro", doc.title)
        assertEquals(listOf("12 Punkte geprüft", "2 Mängel"), doc.stats)
        assertEquals(listOf("Steckdose schief · Flur · Frist 03.12.2026"), doc.defects)
        assertEquals("Vorbehalt: 500 € bis zur Nachbesserung", doc.reservation)
        assertNull(exporter().export(signOff(reservation = ""), project(), "Elektro", capture()).doc.reservation)
    }

    @Test
    fun ownerDrawsCapturedStrokesAndCraftsmanIsTextOnly() {
        val out = exporter().export(signOff(), project(), "Elektro", capture())
        val (owner, craftsman) = out.doc.signatures
        assertEquals(listOf("owner", "craftsman"), out.doc.signatures.map { it.role })
        assertEquals("Familie Berger", owner.name)
        assertEquals("Bauherr", owner.label)
        assertEquals(StrokesDto(300.0, 100.0, listOf(listOf(10.0, 10.3, 100.0, 50.0, 200.0, 90.0))), owner.strokes)
        assertEquals("owner", owner.role)
        assertEquals("26.11.2026 10:30", owner.signedAt)
        assertEquals(SignatureBlock("craftsman", "Max Mustermann", "Handwerker", null, null), craftsman)
        val text = String(out.file.readBytes(), Charsets.ISO_8859_1)
        assertTrue(text.contains("\\(keine Unterschrift auf diesem Gerät erfasst\\)"))
        assertTrue(text.contains("Max Mustermann"))
    }

    @Test
    fun sameInputGivesIdenticalBytes() {
        val a = exporter().export(signOff(), project(), "Elektro", capture())
        val b = exporter().export(signOff(), project(), "Elektro", capture())
        assertEquals(a.doc.protocolId, b.doc.protocolId)
        assertArrayEquals(a.file.readBytes(), b.file.readBytes())
        assertEquals(a.fileSha256, b.fileSha256)
    }
}
