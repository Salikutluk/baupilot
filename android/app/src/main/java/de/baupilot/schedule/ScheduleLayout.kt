package de.baupilot.schedule

import de.baupilot.data.Schedule
import de.baupilot.data.Trade
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max

enum class ScheduleState { Done, Running, Planned }

/** One chart row. `barX`/`barWidth`/`diamondCenterX` are unsnapped; the chart draws only `draw*` for boxes (D22). */
data class ScheduleRowLayout(
    val tradeId: String,
    val name: String,
    val state: ScheduleState,
    val barX: Float,
    val barWidth: Float,
    val diamondCenterX: Float?,
) {
    val drawX: Float get() = snap(barX.toDouble()).toFloat()
    val drawWidth: Float get() = (snap(barX.toDouble() + barWidth.toDouble()) - snap(barX.toDouble())).toFloat()
    val diamondDrawCenterX: Float? get() = diamondCenterX?.let { snap(it.toDouble()).toFloat() }
}

data class MonthLabel(val x: Float, val label: String)

data class ScheduleLayout(
    val rows: List<ScheduleRowLayout>,
    val todayX: Float,
    val todayLabelX: Float,
    val monthLabels: List<MonthLabel>,
) {
    /** Left edge of the 2-wide today line. */
    val todayDrawX: Float get() = snap(todayX.toDouble()).toFloat()

    companion object {
        const val NAME_COLUMN: Float = 118f
        const val COLUMN_GAP: Float = 8f
        const val ROW_PITCH: Float = 34f

        private const val CHART_ORIGIN = 126.0
        private const val MIN_BAR_WIDTH = 10.0

        /** Reads span, today and axis from [schedule] so callers cannot pass mismatched values. */
        fun make(trades: List<Trade>, schedule: Schedule, chartWidth: Float): ScheduleLayout {
            val span = schedule.spanMonths.toDouble()
            val today = schedule.todayMonth
            val track = chartWidth.toDouble() - CHART_ORIGIN

            fun pct(m: Double): Double = roundHalfAway(m / span * 100.0, 2)
            fun x(m: Double): Double = CHART_ORIGIN + pct(m) / 100.0 * track

            val rows = trades.map { trade ->
                val a = trade.window.startMonth
                val b = trade.window.endMonth
                val state = when {
                    b <= today -> ScheduleState.Done
                    a <= today -> ScheduleState.Running
                    else -> ScheduleState.Planned
                }
                val askAt = a - 2.0
                val diamond = state == ScheduleState.Planned && today - 0.5 <= askAt && askAt < today + 2.0
                ScheduleRowLayout(
                    tradeId = trade.id,
                    name = trade.name,
                    state = state,
                    barX = x(a).toFloat(),
                    barWidth = max(pct(b - a) / 100.0 * track, MIN_BAR_WIDTH).toFloat(),
                    diamondCenterX = if (diamond) x(max(askAt, 0.0)).toFloat() else null,
                )
            }
            return ScheduleLayout(
                rows = rows,
                todayX = (CHART_ORIGIN + track * roundHalfAway(today / span, 4)).toFloat(),
                todayLabelX = (x(today) - 14.0).toFloat(),
                monthLabels = schedule.axis.map { MonthLabel(x(it.month.toDouble()).toFloat(), it.label) },
            )
        }
    }
}

/** Half away from zero, like JS `toFixed`, on the double. */
private fun roundHalfAway(value: Double, decimals: Int): Double {
    val factor = Math.pow(10.0, decimals.toDouble())
    return snap(value * factor) / factor
}

/** D22: box edges land on whole dp. */
private fun snap(value: Double): Double =
    if (value < 0) -floor(abs(value) + 0.5) else floor(value + 0.5)
