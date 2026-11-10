package de.baupilot.app

import de.baupilot.data.MockRepository
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Figures that appear twice in the fixture (milestone tasks M5): the screens must not contradict each other. */
class M5ConsistencyTest {
    private val fixture: String =
        File(System.getProperty("baupilot.repoRoot"), "fixtures/baupilot.json").readText()

    private val repository = MockRepository(fixture)

    private val kitchen = AppRoute.InspectionRoom("electrical", "kitchen")
    private val panel = AppRoute.InspectionCheck("electrical", "panel")
    private val smoke = AppRoute.InspectionCheck("electrical", "smoke")
    private val measure = AppRoute.InspectionCheck("shell", "measure")

    private fun leadingNumber(text: String): Int = Regex("^(\\d+)").find(text)!!.groupValues[1].toInt()

    /** The iOS side of a platform text member of the raw `inspections.byTrade.electrical`. */
    private fun JsonElement.ios(key: String): String =
        jsonObject.getValue(key).jsonObject.getValue("ios").jsonPrimitive.content

    private suspend fun rawElectrical(): JsonObject =
        repository.inspections().jsonObject.getValue("byTrade").jsonObject.getValue("electrical").jsonObject

    @Test
    fun electricalFiguresAreConsistent() = runTest {
        val data = AppData.load(repository)
        val el = data.electricalOverviews.getValue("electrical")

        // Termin: the Android subtitle has no weekday; the date parsed from it is a Monday, like the iOS "Mo".
        val (day, month, year) = el.subtitle.substringAfterLast(" · ").split(".").map { it.toInt() }
        assertEquals(DayOfWeek.MONDAY, LocalDate.of(year, month, day).dayOfWeek)
        assertEquals(LocalDate.of(2027, 4, 26), LocalDate.of(year, month, day))
        val raw = rawElectrical()
        assertTrue(raw.ios("subtitle").startsWith("Elektro Schmitt GmbH · Mo, "))
        assertTrue(raw.ios("subtitle").endsWith(el.subtitle.substringAfterLast(" · ")))

        // Rohinstallation: iOS "Abgenommen" + "am 29.01.2027" and Android "Abgenommen 29.01." name the same day.
        val phase = raw.getValue("phases").jsonArray[0]
        val iosDate = phase.ios("dateLabel").removePrefix("am ")
        val done = el.phases[0].kind as PhaseKind.Done
        assertEquals("${phase.ios("statusLabel")} ${iosDate.removeSuffix("2027")}", done.status)
        assertEquals("Abgenommen am 29.01.2027", "${phase.ios("statusLabel")} ${phase.ios("dateLabel")}")

        // Fertiginstallation: 4 of 11 checked.
        val active = el.phases[1].kind as PhaseKind.Active
        assertEquals("4 von 11 geprüft", active.progressLabel)
    }

    @Test
    fun overviewRowsMatchTheirChecks() = runTest {
        val data = AppData.load(repository)
        val rows = data.electricalOverviews.getValue("electrical").groups.flatMap { it.rows }.associateBy { it.name }

        val room = data.roomChecks.getValue(kitchen)
        val steps = room.done.size + 1 + room.next.size
        assertEquals(5, steps)
        assertTrue(rows.getValue("Küche").sub.endsWith("Schritt ${room.current.n} von $steps"))
        assertEquals("${room.current.n} / $steps", room.counter)
        assertEquals(room.done.size + 1, room.current.n)

        val p = data.panelChecks.getValue(panel)
        val panelSteps = p.done.size + 1 + p.next.size
        assertEquals("$panelSteps Prüfschritte", rows.getValue("Zählerschrank & FI-Test").sub)
        assertEquals("${p.current.n} / $panelSteps", p.counter)
        assertEquals(p.done.size + 1, p.current.n)

        val s = data.smokeChecks.getValue(smoke)
        val tested = s.rooms.count { it.status == SmokeRoomStatus.Ok }
        assertEquals("$tested von ${s.rooms.size} getestet", rows.getValue("Rauchwarnmelder").sub)
        assertEquals("${tested} / ${s.rooms.size}", s.counter)
    }

    @Test
    fun socketsAndFisAreConsistent() = runTest {
        val data = AppData.load(repository)
        val room = data.roomChecks.getValue(kitchen)
        val tested = room.sockets.count { it.status == SocketStatus.Ok || it.status == SocketStatus.Defect }
        assertTrue(room.progressLabel.startsWith("$tested von ${room.sockets.size} getestet"))
        assertEquals("${room.sockets.size} von ${room.sockets.size} Steckdosen vorhanden", room.done.single().meta)
        assertTrue(room.defectNote.startsWith(room.sockets.single { it.status == SocketStatus.Defect }.id))
        assertTrue(room.okNote.startsWith(room.sockets.single { it.status == SocketStatus.Now }.id))

        val p = data.panelChecks.getValue(panel)
        val openFi = p.fis.single { it.tone == FiTone.Open }
        assertTrue(openFi.name.startsWith(p.outcomes[1].substringBefore(" ausgelöst")))
        assertTrue(p.fis.single { it.tone == FiTone.Ok }.name != openFi.name)
    }

    @Test
    fun protocolFiguresMatchOverview() = runTest {
        val data = AppData.load(repository)
        val overview = data.inspections.rows.associateBy { it.tradeId }
        for ((tradeId, expected) in mapOf("earthworks" to 9, "shell" to 9, "carpentry" to 8)) {
            val rows = data.tradeProtocols.getValue(tradeId).groups.sumOf { it.rows.size }
            assertEquals(tradeId, expected, rows)
            assertEquals(tradeId, rows, leadingNumber(overview.getValue(tradeId).subtitle))
        }

        val shell = data.tradeProtocols.getValue("shell")
        val defects = shell.groups.flatMap { it.rows }.count { it.mark == RowMark.Defect }
        assertEquals(defects, leadingNumber(shell.banner.text))

        val m = data.measureChecks.getValue(measure)
        val total = leadingNumber(shell.groups.flatMap { it.rows }.single { it.target == NavigationTarget.Push(measure) }.meta)
        val index = leadingNumber(m.counter)
        assertEquals("$index / $total", m.counter)
        assertEquals(index - 1, m.measured.size)
        assertTrue(m.title.endsWith("F$index"))
    }
}
