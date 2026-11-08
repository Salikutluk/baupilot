package de.baupilot.schedule

import de.baupilot.data.AxisTick
import de.baupilot.data.Schedule
import de.baupilot.data.Trade
import de.baupilot.data.TradeWindow
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ScheduleLayoutTest {
    private val fixture = Json.parseToJsonElement(
        File(System.getProperty("baupilot.repoRoot"), "fixtures/baupilot.json").readText(),
    )
    private val trades: List<Trade> = Json.decodeFromJsonElement(
        kotlinx.serialization.builtins.ListSerializer(Trade.serializer()),
        fixture.jsonObject["trades"]!!,
    )
    private val schedule: Schedule = Json.decodeFromJsonElement(
        Schedule.serializer(),
        fixture.jsonObject["schedule"]!!,
    )
    private val layout = ScheduleLayout.make(trades, schedule, 358f)
    private val delta = 0.0005f

    private fun row(id: String) = layout.rows.first { it.tradeId == id }

    private fun synthetic(startMonth: Double, endMonth: Double, today: Double): ScheduleRowLayout {
        val trade = Trade(
            id = "t",
            name = "T",
            phase = 1,
            window = TradeWindow(startMonth, endMonth),
            scheduleState = "planned",
            askNow = false,
        )
        val sched = Schedule("x", 19, today, listOf(AxisTick(0, "a")), emptyList())
        return ScheduleLayout.make(listOf(trade), sched, 358f).rows.single()
    }

    @Test
    fun statusFollowsTodayRule() {
        val expected = listOf(
            "earthworks" to ScheduleState.Done,
            "shell" to ScheduleState.Done,
            "carpentry" to ScheduleState.Done,
            "roofing" to ScheduleState.Done,
            "windows" to ScheduleState.Running,
            "electrical" to ScheduleState.Planned,
            "plumbing" to ScheduleState.Planned,
            "screed" to ScheduleState.Planned,
            "tiles" to ScheduleState.Planned,
            "painting" to ScheduleState.Planned,
            "landscaping" to ScheduleState.Planned,
        )
        assertEquals(trades.map { it.id }, layout.rows.map { it.tradeId })
        assertEquals(expected, layout.rows.map { it.tradeId to it.state })
        assertEquals(trades.map { it.name }, layout.rows.map { it.name })
    }

    @Test
    fun statusMatchesFixtureScheduleState() {
        trades.forEach { trade ->
            val state = row(trade.id).state
            assertEquals(trade.scheduleState, state.name.lowercase())
        }
    }

    @Test
    fun diamondsOnlyForScreedTilesPainting() {
        val withDiamond = layout.rows.filter { it.diamondCenterX != null }.map { it.tradeId }.toSet()
        assertEquals(trades.filter { it.askNow }.map { it.id }.toSet(), withDiamond)
        assertEquals(setOf("screed", "tiles", "painting"), withDiamond)
    }

    @Test
    fun diamondVisibilityBoundaries() {
        // today = 10.0, planned windows start after it; askAt = start - 2
        assertNotNull(synthetic(11.5, 13.0, 10.0).diamondCenterX) // askAt == today - 0.5
        assertNull(synthetic(14.0, 15.0, 10.0).diamondCenterX) // askAt == today + 2
        assertNull(synthetic(11.49, 13.0, 10.0).diamondCenterX) // askAt == today - 0.51
        assertNotNull(synthetic(13.99, 15.0, 10.0).diamondCenterX) // just inside the upper bound
        // a running trade never shows one
        val running = synthetic(9.0, 11.0, 10.0)
        assertEquals(ScheduleState.Running, running.state)
        assertNull(running.diamondCenterX)
    }

    @Test
    fun barMinWidthIs10() {
        assertEquals(10f, row("roofing").barWidth, delta)
        assertEquals(10f, row("windows").barWidth, delta)
        layout.rows.forEach { assert(it.barWidth >= 10f) }
    }

    @Test
    fun barGeometryMatchesPrototype() {
        val expected = mapOf(
            "earthworks" to (126f to 18.3048f),
            "shell" to (144.3048f to 79.3672f),
            "carpentry" to (223.6952f to 10.9968f),
            "roofing" to (233.4624f to 10f),
            "windows" to (240.7704f to 10f),
            "electrical" to (249.3312f to 23.2f),
            "plumbing" to (255.4328f to 19.5344f),
            "screed" to (267.636f to 17.0984f),
            "tiles" to (279.8624f to 17.0984f),
            "painting" to (289.6296f to 19.5344f),
            "landscaping" to (315.2656f to 30.5312f),
        )
        expected.forEach { (id, xw) ->
            assertEquals("$id x", xw.first, row(id).barX, delta)
            assertEquals("$id w", xw.second, row(id).barWidth, delta)
        }
        assertEquals(243.2296f - 0f, row("screed").diamondCenterX!!, delta)
        assertEquals(255.4328f, row("tiles").diamondCenterX!!, delta)
        assertEquals(265.2f, row("painting").diamondCenterX!!, delta)
    }

    @Test
    fun todayLineAndLabelX() {
        assertEquals(243.2296f, layout.todayX, delta)
        assertEquals(243f, layout.todayDrawX, delta)
        assertEquals(229.2296f, layout.todayLabelX, delta)
    }

    @Test
    fun monthLabelPositionsAndTexts() {
        assertEquals(listOf("Mär 26", "Jul", "Nov", "Mär 27", "Jul"), layout.monthLabels.map { it.label })
        val xs = listOf(126f, 174.836f, 223.6952f, 272.5312f, 321.3672f)
        xs.zip(layout.monthLabels).forEach { (x, m) -> assertEquals(x, m.x, delta) }
    }

    @Test
    fun snappedDrawValuesMatchReference() {
        assertEquals(126f, row("earthworks").drawX, delta)
        assertEquals(18f, row("earthworks").drawWidth, delta)
        assertEquals(144f, row("shell").drawX, delta)
        assertEquals(80f, row("shell").drawWidth, delta)
        assertEquals(224f, row("carpentry").drawX, delta)
        assertEquals(11f, row("carpentry").drawWidth, delta)
        assertEquals(243f, row("screed").diamondDrawCenterX!!, delta)
        assertEquals(255f, row("tiles").diamondDrawCenterX!!, delta)
        assertEquals(265f, row("painting").diamondDrawCenterX!!, delta)
        assertNull(row("electrical").diamondDrawCenterX)
    }

    @Test
    fun rawWindowsMatchMilestoneTable() {
        val expected = listOf(
            0.0 to 1.5, 1.5 to 8.0, 8.0 to 8.9, 8.8 to 9.5, 9.4 to 10.0, 10.1 to 12.0,
            10.6 to 12.2, 11.6 to 13.0, 12.6 to 14.0, 13.4 to 15.0, 15.5 to 18.0,
        )
        assertEquals(expected, trades.map { it.window.startMonth to it.window.endMonth })
        assertEquals(19, schedule.spanMonths)
        assertEquals(9.6, schedule.todayMonth, 0.0)
    }
}
