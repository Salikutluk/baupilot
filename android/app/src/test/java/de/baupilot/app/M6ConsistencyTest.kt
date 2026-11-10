package de.baupilot.app

import de.baupilot.data.MockRepository
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Figures that appear twice in the fixture (milestone tasks M6): the screens must not contradict each other. */
class M6ConsistencyTest {
    private val fixture: String =
        File(System.getProperty("baupilot.repoRoot"), "fixtures/baupilot.json").readText()

    private val repository = MockRepository(fixture)

    private val slope = AppRoute.InspectionCheckItem("plumbing", "slope", "A3")
    private val pressure = AppRoute.InspectionCheck("plumbing", "pressure")
    private val bath = AppRoute.InspectionRoom("plumbing", "bathUpper")
    private val flatness = AppRoute.InspectionCheck("screed", "flatness")
    private val readiness = AppRoute.InspectionCheck("screed", "readiness")
    private val tile = AppRoute.InspectionRoom("tiles", "bath")
    private val paint = AppRoute.InspectionRoom("painting", "living")

    private fun leadingNumber(text: String): Int = Regex("^(\\d+)").find(text)!!.groupValues[1].toInt()

    private suspend fun rawTrade(tradeId: String): JsonObject =
        repository.inspections().jsonObject.getValue("byTrade").jsonObject.getValue(tradeId).jsonObject

    private fun JsonElement.ios(key: String): String =
        jsonObject.getValue(key).jsonObject.getValue("ios").jsonPrimitive.content

    private fun date(text: String): LocalDate {
        val (day, month, year) = Regex("(\\d{2})\\.(\\d{2})\\.(\\d{4})").find(text)!!.destructured
        return LocalDate.of(year.toInt(), month.toInt(), day.toInt())
    }

    @Test
    fun screedDatesAreConsistent() = runTest {
        val data = AppData.load(repository)
        val screed = data.tradeOverviews.getValue("screed")

        // Einbau "22.02.2027" is a Monday, like the iOS "Mo".
        val einbau = date(screed.subtitle.substringAfterLast(" · "))
        assertEquals(DayOfWeek.MONDAY, einbau.dayOfWeek)
        assertEquals(LocalDate.of(2027, 2, 22), einbau)
        val raw = rawTrade("screed")
        assertTrue(raw.ios("subtitle").startsWith("Estrichbau Wagner · Mo, "))

        // Phase and row both say "ab 01.03.".
        val planned = (screed.header as OverviewHeader.Phases).phases[1].kind as PhaseKind.Planned
        assertTrue(planned.status.endsWith("ab 01.03."))
        val heatingRow = screed.groups.flatMap { it.rows }.first { it.target == readiness }
        assertTrue(heatingRow.sub.endsWith("ab 01.03."))

        // Belegreife: "Tag 6 von 21" = todayDay / planCelsius.count.
        val heating = repository.measurements().jsonObject.getValue("screedHeating").jsonObject
        val plan = heating.getValue("planCelsius").jsonArray.map { it.jsonPrimitive.int }
        val todayDay = heating.getValue("todayDay").jsonPrimitive.int
        val recorded = heating.getValue("recordedDays").jsonPrimitive.int
        val check = data.readinessChecks.getValue(readiness)
        assertEquals("Tag $todayDay von ${plan.size}", check.counter)
        assertEquals("Tag ${plan.size}", check.chart.end)
        assertEquals("Max. ${plan.max()} °C", check.chart.max)
        assertEquals("${plan[todayDay - 1]} °C Vorlauf", check.chart.plan)
        assertEquals("Tag $todayDay eintragen", check.chart.action)
        assertTrue(check.chart.accessibilityLabel.contains("Tag 1 bis $recorded erfasst"))
        assertTrue(check.chart.accessibilityLabel.contains("heute Tag $todayDay"))

        // CM meta "geplant 19.03.".
        val cmDate = Regex("geplant (\\d{2})\\.(\\d{2})\\.").find(check.cm.rows[1].meta)!!.groupValues
        assertEquals("19.03.", "${cmDate[1]}.${cmDate[2]}.")
    }

    @Test
    fun overviewDatesHaveCorrectWeekdays() = runTest {
        val expected = mapOf(
            "plumbing" to ("Do, " to DayOfWeek.THURSDAY),
            "screed" to ("Mo, " to DayOfWeek.MONDAY),
            "tiles" to ("Mo, " to DayOfWeek.MONDAY),
            "painting" to ("Mo, " to DayOfWeek.MONDAY),
        )
        val data = AppData.load(repository)
        for ((tradeId, pair) in expected) {
            val (prefix, weekday) = pair
            val ios = rawTrade(tradeId).ios("subtitle")
            assertTrue(tradeId, ios.substringAfter(" · ").startsWith(prefix))
            assertEquals(tradeId, weekday, date(ios).dayOfWeek)
            assertEquals(tradeId, date(ios), date(data.tradeOverviews.getValue(tradeId).subtitle))
        }
    }

    @Test
    fun phaseFractionsMatchProgressLabels() = runTest {
        val data = AppData.load(repository)
        val expected = mapOf("plumbing" to (3 to 6), "screed" to (3 to 4), "tiles" to (2 to 4))
        for ((tradeId, parts) in expected) {
            val phases = (data.tradeOverviews.getValue(tradeId).header as OverviewHeader.Phases).phases
            val active = phases.map { it.kind }.filterIsInstance<PhaseKind.Active>().single()
            val numbers = Regex("(\\d+) von (\\d+)").find(active.progressLabel)!!.groupValues
            assertEquals(tradeId, parts, numbers[1].toInt() to numbers[2].toInt())
            assertEquals(tradeId, parts.first.toFloat() / parts.second, active.fraction, 0.0001f)
        }
    }

    @Test
    fun slopeFiguresMatchOverview() = runTest {
        val data = AppData.load(repository)
        val check = data.slopeChecks.getValue(slope)
        val row = data.tradeOverviews.getValue("plumbing").groups[0].rows[0]
        val measured = check.sections.count { it.status == SectionStatus.Ok || it.status == SectionStatus.Defect }
        val defects = check.sections.count { it.status == SectionStatus.Defect }
        // Android row sub: "4 Abschnitte · 2 geprüft" (the iOS side adds ", 1 Mangel").
        assertEquals("${check.sections.size} Abschnitte · $measured geprüft", row.sub)
        val iosSub = rawTrade("plumbing").getValue("groups").jsonArray[0].jsonObject.getValue("rows").jsonArray[0].ios("sub")
        assertEquals("${check.sections.size} Abschnitte · $measured geprüft, $defects Mangel", iosSub)
        assertEquals("$defects ${check.defectLabel}", row.statusLabel)
        assertTrue(check.initial.isSufficient)
        assertEquals(25 * 10 >= 10 * 20, check.initial.isSufficient)
    }

    @Test
    fun flatnessFiguresMatchOverview() = runTest {
        val data = AppData.load(repository)
        val check = data.flatnessChecks.getValue(flatness)
        val row = data.tradeOverviews.getValue("screed").groups.flatMap { it.rows }.first { it.target == flatness }
        val (index, count) = Regex("(\\d+) / (\\d+)").find(check.counter)!!.destructured
        assertTrue(row.sub.endsWith("${index.toInt() - 1} von $count Räumen"))
        assertEquals("${check.initial.overCount} Mangel", row.statusLabel)
    }

    @Test
    fun pressureFigures() = runTest {
        val check = AppData.load(repository).pressureChecks.getValue(pressure)
        assertEquals(check.done[1].meta, check.timer.startValue)
        assertTrue(check.timer.accessibilityLabel.contains((check.timer.fraction * 100).toInt().toString()))
        assertEquals("3 / ${check.done.size + 1 + check.next.size}", check.counter)
        assertEquals(check.done.size + 1, check.current.n)
        assertTrue(check.timer.remainingLabel.contains(check.timer.remaining))
    }

    @Test
    fun objectFigures() = runTest {
        val data = AppData.load(repository)
        val check = data.objectChecks.getValue(bath)
        val done = check.objects.count { it.state == ObjectState.Done }
        assertEquals("Fertigmontage · $done von ${check.objects.size} Objekten", check.counter)
        val row = data.tradeOverviews.getValue("plumbing").groups[2].rows[0]
        assertEquals("Fertigmontage · ${check.objects.size} Objekte", row.sub)
        val selected = check.objects.indexOfFirst { it.state == ObjectState.Now }
        assertEquals(check.objects[selected].name, check.heading)
        assertTrue(check.nextHint.contains(check.objects[selected + 1].name))
    }

    @Test
    fun tileFigures() = runTest {
        val data = AppData.load(repository)
        val check = data.tileChecks.getValue(tile)
        val hollow = check.tiles.count { it.hollow }
        assertTrue(check.result.startsWith("$hollow Fliesen"))
        assertEquals(listOf(2, 3), check.tiles.withIndex().filter { it.value.hollow }.map { it.index })
        val steps = check.done.size + 1 + check.next.size
        assertEquals("${check.current.n} / $steps", check.counter)
        val row = data.tradeOverviews.getValue("tiles").groups[1].rows[0]
        assertTrue(row.sub.endsWith("Schritt ${check.current.n} von $steps"))
        assertEquals(check.done.size + 1, check.current.n)
    }

    @Test
    fun paintFigures() = runTest {
        val data = AppData.load(repository)
        val check = data.paintChecks.getValue(paint)
        val steps = check.done.size + 1 + check.next.size
        assertEquals("${check.current.n} / $steps", check.counter)
        val row = data.tradeOverviews.getValue("painting").groups[0].rows[0]
        assertTrue(row.sub.endsWith("Schritt ${check.current.n} von $steps"))
        assertEquals(PaintColor.Sand, check.doneSwatch)
        assertTrue(check.subtitle.contains("Sand"))
        assertTrue(row.sub.startsWith("Sand"))
        assertEquals(PaintColor.Sand, (row.leading as OverviewLeading.Swatch).color)
    }
}
