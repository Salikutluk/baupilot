package de.baupilot.inspection

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlatnessViewModelTest {
    private val labels = FlatnessLabels(
        within = "Innerhalb der Toleranz",
        outside = "Über Grenzwert – als Mangel erfassen",
        none = "Kein Messpunkt über Grenzwert",
        point = "Messpunkt",
        pluralSuffix = "e",
        overLimit = "über Grenzwert → Mangel erfassen",
    )

    private fun make(limit: Int = 4, gap: Int = 3, points: List<Int> = listOf(2, 3, 6)) = FlatnessViewModel.of(
        limitDefault = limit, limitMin = 1, limitMax = 15,
        gap = gap, points = points, gapMin = 0, gapMax = 20, gapStep = 1,
        labels = labels,
    )

    private val fixture = make()

    @Test
    fun initialStateMatchesFixture() {
        assertEquals("3 mm", fixture.gapText)
        assertEquals("4 mm", fixture.limitText)
        assertTrue(fixture.isWithin)
        assertEquals("Innerhalb der Toleranz", fixture.resultText)
        assertEquals(listOf("M1 · 2 mm", "M2 · 3 mm", "M3 · 6 mm"), fixture.points.indices.map { fixture.pointText(it) })
        assertEquals(1, fixture.overCount)
        assertEquals("1 Messpunkt über Grenzwert → Mangel erfassen", fixture.summaryText)
        assertEquals(4, fixture.nextIndex)
    }

    @Test
    fun withinIsInclusive() {
        val atLimit = fixture.increment()
        assertEquals(4, atLimit.gap)
        assertTrue(atLimit.isWithin)
        val over = atLimit.increment()
        assertEquals(5, over.gap)
        assertFalse(over.isWithin)
        assertEquals("Über Grenzwert – als Mangel erfassen", over.resultText)
    }

    @Test
    fun pointOverIsStrict() {
        assertEquals(0, make(points = listOf(4)).overCount)
        assertEquals(1, make(points = listOf(5)).overCount)
    }

    @Test
    fun saveAppendsWithoutReset() {
        val saved = fixture.increment().increment().save()
        assertEquals(listOf(2, 3, 6, 5), saved.points)
        assertEquals("M4 · 5 mm", saved.pointText(3))
        assertEquals(2, saved.overCount)
        assertEquals("2 Messpunkte über Grenzwert → Mangel erfassen", saved.summaryText)
        assertEquals(5, saved.gap)
        assertEquals(5, saved.nextIndex)
    }

    @Test
    fun summaryWithNoneOver() {
        val none = make(points = listOf(1, 4))
        assertEquals(0, none.overCount)
        assertEquals("Kein Messpunkt über Grenzwert", none.summaryText)
    }

    @Test
    fun clampsGap() {
        val zero = make(gap = 0)
        assertEquals(zero, zero.decrement())
        val max = make(gap = 20)
        assertEquals(max, max.increment())
        var vm = fixture
        repeat(40) { vm = vm.increment() }
        assertEquals(20, vm.gap)
        repeat(40) { vm = vm.decrement() }
        assertEquals(0, vm.gap)
    }

    @Test
    fun limitIsClampedToRange() {
        assertEquals(1, make(limit = 0).limit)
        assertEquals(15, make(limit = 16).limit)
        assertEquals(4, make(limit = 4).limit)
    }

    @Test
    fun sketchLineHeight() {
        assertEquals(8, make(gap = 3).sketchLineHeight)
        assertEquals(26, make(gap = 12).sketchLineHeight)
        assertEquals(26, make(gap = 20).sketchLineHeight)
        assertEquals(2, make(gap = 0).sketchLineHeight)
    }

    @Test
    fun chipTextIsOneString() {
        val text = fixture.pointText(0)
        assertTrue(text.contains(" · "))
        assertEquals("M1 · 2 mm", text)
    }

    @Test
    fun saverRoundTrips() {
        val moved = fixture.increment().increment().save()
        val scope = SaverScope { true }
        val saved = with(FlatnessViewModel.saver(fixture)) { scope.save(moved) }
        assertEquals(listOf(5, 2, 3, 6, 5), saved)
        assertEquals(moved, FlatnessViewModel.saver(fixture).restore(saved!!))
        assertNull(FlatnessViewModel.saver(fixture).restore(emptyList()))
    }
}
