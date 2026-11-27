package de.baupilot.capture

/**
 * Due text and urgency of a defect from the days left until its deadline. The overdue and "noch" plurals follow the
 * fixture; "heute fällig" and the singular `Tag` are not in the prototype (offene Punkte).
 */
object DueLabels {
    private const val SOON_DAYS = 21L

    fun dueLabel(days: Long): String = when {
        days < 0 -> "${-days} ${tag(-days)} überfällig"
        days == 0L -> "heute fällig"
        else -> "noch $days ${tag(days)}"
    }

    /** `late` when overdue, `soon` up to 21 days, otherwise `later` (calibrated on the fixture: 21 soon, 28 later). */
    fun urgency(days: Long): String = when {
        days < 0 -> "late"
        days <= SOON_DAYS -> "soon"
        else -> "later"
    }

    private fun tag(count: Long): String = if (count == 1L) "Tag" else "Tage"
}
