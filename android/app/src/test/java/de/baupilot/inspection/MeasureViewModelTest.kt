package de.baupilot.inspection

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeasureViewModelTest {
    private val fixture = MeasureViewModel.of(
        target = 1010 to 1385, actual = 1005 to 1395, step = 5,
        toleranceDefault = 12, toleranceMin = 1, toleranceMax = 30, unit = "mm",
    )
    private val width = MeasureDimension.WIDTH
    private val height = MeasureDimension.HEIGHT

    @Test
    fun initialStateMatchesFixture() {
        assertEquals("−5 mm", fixture.diffText(width))
        assertEquals("+10 mm", fixture.diffText(height))
        assertTrue(fixture.isWithin(width))
        assertTrue(fixture.isWithin(height))
        assertTrue(fixture.isWithinTolerance)
    }

    @Test
    fun incrementAndDecrementMoveByStep() {
        val up = fixture.increment(width)
        assertEquals("1010", up.valueText(width))
        assertEquals("1395", up.valueText(height))
        val down = fixture.decrement(height)
        assertEquals("1390", down.valueText(height))
        assertEquals("1005", down.valueText(width))
        assertEquals(fixture, fixture.increment(width).decrement(width))
    }

    @Test
    fun diffTextUsesTypographicSigns() {
        assertEquals(0x2212, fixture.diffText(width).codePointAt(0))
        assertEquals(0x002B, fixture.diffText(height).codePointAt(0))
        val zero = fixture.decrement(height).decrement(height)
        assertEquals("±0 mm", zero.diffText(height))
        assertEquals(0x00B1, zero.diffText(height).codePointAt(0))
    }

    @Test
    fun boundaryIsInclusive() {
        fun vm(tol: Int) = MeasureViewModel.of(
            target = 1000 to 1000, actual = 1010 to 1000, step = 5,
            toleranceDefault = tol, toleranceMin = 1, toleranceMax = 30, unit = "mm",
        )
        assertTrue(vm(10).isWithin(width))
        assertFalse(vm(9).isWithin(width))
    }

    @Test
    fun outsideWhenEitherDimensionExceeds() {
        val out = fixture.decrement(width).decrement(width)
        assertEquals("−15 mm", out.diffText(width))
        assertFalse(out.isWithinTolerance)
        assertTrue(out.increment(width).isWithinTolerance)
    }

    @Test
    fun toleranceIsClampedToRange() {
        fun tol(t: Int) = MeasureViewModel.of(
            target = 1 to 1, actual = 1 to 1, step = 5,
            toleranceDefault = t, toleranceMin = 1, toleranceMax = 30, unit = "mm",
        ).tolerance
        assertEquals(1, tol(0))
        assertEquals(30, tol(31))
        assertEquals(12, tol(12))
    }

    @Test
    fun decrementStopsAtZero() {
        val small = MeasureViewModel.of(
            target = 10 to 10, actual = 7 to 3, step = 5,
            toleranceDefault = 12, toleranceMin = 1, toleranceMax = 30, unit = "mm",
        )
        assertEquals("2", small.decrement(width).valueText(width))
        assertEquals("2", small.decrement(width).decrement(width).valueText(width))
        assertEquals("3", small.decrement(height).valueText(height))
    }

    @Test
    fun valueTextHasNoGrouping() {
        assertEquals("1005", fixture.valueText(width))
        var vm = fixture
        repeat(1799) { vm = vm.increment(width) }
        assertEquals("10000", vm.valueText(width))
    }

    @Test
    fun saverRoundTrips() {
        val moved = fixture.increment(width).decrement(height).decrement(height)
        val scope = SaverScope { true }
        val saved = with(MeasureViewModel.saver(fixture)) { scope.save(moved) }
        assertEquals(listOf(1010, 1385), saved)
        assertEquals(moved, MeasureViewModel.saver(fixture).restore(saved!!))
        assertNull(MeasureViewModel.saver(fixture).restore(listOf(1)))
    }
}
