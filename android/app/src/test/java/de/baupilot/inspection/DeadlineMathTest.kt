package de.baupilot.inspection

import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class DeadlineMathTest {
    private val base = LocalDate.of(2026, 12, 18)

    @Test
    fun androidStyleHasNoWeekday() {
        assertEquals("01.01.2027", DeadlineMath.label(base, 14, DeadlineStyle.Android))
        assertEquals("15.01.2027", DeadlineMath.label(base, 28, DeadlineStyle.Android))
        assertEquals("29.01.2027", DeadlineMath.label(base, 42, DeadlineStyle.Android))
    }

    @Test
    fun labelIgnoresTheDeviceLocale() {
        val saved = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG-u-nu-arab"))
            assertEquals("15.01.2027", DeadlineMath.label(base, 28, DeadlineStyle.Android))
            assertEquals("Fr, 15.01.2027", DeadlineMath.label(base, 28, DeadlineStyle.Ios))
        } finally {
            Locale.setDefault(saved)
        }
    }

    @Test
    fun defaultStyleIsAndroid() {
        assertEquals("29.01.2027", DeadlineMath.label(base, 42))
    }

    @Test
    fun iosStylePrefixesTheGermanWeekday() {
        assertEquals("Fr, 01.01.2027", DeadlineMath.label(base, 14, DeadlineStyle.Ios))
        assertEquals("Fr, 15.01.2027", DeadlineMath.label(base, 28, DeadlineStyle.Ios))
    }

    @Test
    fun weekdayOfANonFridayBase() {
        val monday = LocalDate.of(2026, 12, 21)

        assertEquals("Mo, 21.12.2026", DeadlineMath.label(monday, 0, DeadlineStyle.Ios))
        assertEquals("Mo, 04.01.2027", DeadlineMath.label(monday, 14, DeadlineStyle.Ios))
    }

    @Test
    fun weekdayTableRunsMondayToSunday() {
        val labels = (0..6).map { DeadlineMath.label(LocalDate.of(2026, 12, 21), it, DeadlineStyle.Ios).take(2) }

        assertEquals(listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"), labels)
    }

    @Test
    fun yearRolloverAndLeapDay() {
        assertEquals("01.01.2027", DeadlineMath.label(LocalDate.of(2026, 12, 31), 1))
        assertEquals("29.02.2028", DeadlineMath.label(LocalDate.of(2028, 2, 1), 28))
    }
}
