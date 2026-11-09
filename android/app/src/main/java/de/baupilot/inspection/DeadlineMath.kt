package de.baupilot.inspection

import java.time.LocalDate
import java.util.Locale

/** Label style of a deadline: iOS prints a German weekday first, Android only the date. */
enum class DeadlineStyle { Ios, Android }

/**
 * Deadline labels on calendar days (D9, R21): proleptic Gregorian day arithmetic on the [LocalDate] itself,
 * no time zone, no instant. The weekday table is fixed (Mo … So), so the output is locale-independent.
 */
object DeadlineMath {
    private val weekdays = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")

    fun label(base: LocalDate, offsetDays: Int, style: DeadlineStyle = DeadlineStyle.Android): String {
        val day = base.plusDays(offsetDays.toLong())
        val date = String.format(Locale.ROOT, "%02d.%02d.%04d", day.dayOfMonth, day.monthValue, day.year)
        return when (style) {
            DeadlineStyle.Ios -> "${weekdays[day.dayOfWeek.value - 1]}, $date"
            DeadlineStyle.Android -> date
        }
    }
}
