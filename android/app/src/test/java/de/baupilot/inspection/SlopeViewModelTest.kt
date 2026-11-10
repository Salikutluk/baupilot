package de.baupilot.inspection

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SlopeViewModelTest {
    private val labels = SlopeLabels(sufficient = "Ausreichend", tooFlat = "Zu flach", sollPrefix = "Soll ≥")

    private fun make(lenDm: Int = 20, dropMm: Int = 25, sollDefault: Double = 1.0) = SlopeViewModel.of(
        lenDm = lenDm, dropMm = dropMm,
        sollDefault = sollDefault, sollMin = 0.5, sollMax = 3.0,
        lengthMin = 5, lengthMax = 100, lengthStep = 5,
        dropMin = 0, dropMax = 300, dropStep = 5,
        labels = labels,
    )

    private val fixture = make()

    @Test
    fun initialStateMatchesFixture() {
        assertEquals("2,0 m", fixture.lengthText)
        assertEquals("2,5 cm", fixture.dropText)
        assertEquals("1,25 %", fixture.percentText)
        assertEquals("Ausreichend · Soll ≥ 1,0 %", fixture.verdictText)
        assertTrue(fixture.isSufficient)
        assertEquals(24.5, fixture.schemaY2, 0.0)
    }

    @Test
    fun steppersMoveByHalfUnits() {
        assertEquals("2,5 m", fixture.incrementLength().lengthText)
        assertEquals("2,0 cm", fixture.decrementDrop().dropText)
    }

    @Test
    fun boundaryIsInclusive() {
        val edge = make(lenDm = 20, dropMm = 20)
        assertEquals("1,00 %", edge.percentText)
        assertTrue(edge.isSufficient)
        val flat = make(lenDm = 20, dropMm = 15)
        assertEquals("0,75 %", flat.percentText)
        assertFalse(flat.isSufficient)
        assertEquals("Zu flach · Soll ≥ 1,0 %", flat.verdictText)
    }

    @Test
    fun clampsAtRangeEnds() {
        val low = make(lenDm = 5, dropMm = 0)
        assertEquals(low, low.decrementLength())
        assertEquals(low, low.decrementDrop())
        val high = make(lenDm = 100, dropMm = 300)
        assertEquals(high, high.incrementLength())
        assertEquals(high, high.incrementDrop())
        assertEquals(10, make(lenDm = 5).incrementLength().lenDm)
        var vm = fixture
        repeat(60) { vm = vm.incrementLength().incrementDrop() }
        assertEquals(100, vm.lenDm)
        assertEquals(300, vm.dropMm)
        repeat(70) { vm = vm.decrementLength().decrementDrop() }
        assertEquals(5, vm.lenDm)
        assertEquals(0, vm.dropMm)
    }

    @Test
    fun schemaY2IsClamped() {
        assertEquals(14.0, make(lenDm = 100, dropMm = 0).schemaY2, 0.0)
        assertEquals(14.0, make(lenDm = 100, dropMm = 20).schemaY2, 0.0)
        assertEquals(38.0, make(lenDm = 5, dropMm = 300).schemaY2, 0.0)
        assertEquals(20.3, make(lenDm = 30, dropMm = 25).schemaY2, 0.0)
    }

    @Test
    fun percentRoundsHalfUpLikeToFixed() {
        assertEquals("1,13 %", make(lenDm = 40, dropMm = 45).percentText)
        assertEquals("0,38 %", make(lenDm = 40, dropMm = 15).percentText)
    }

    @Test
    fun textsUseCommaWithoutGrouping() {
        val big = make(lenDm = 100, dropMm = 300)
        assertEquals("10,0 m", big.lengthText)
        assertEquals("30,0 cm", big.dropText)
        assertEquals("3,00 %", big.percentText)
        val small = make(lenDm = 5, dropMm = 0)
        assertEquals("0,5 m", small.lengthText)
        assertEquals("0,0 cm", small.dropText)
        assertEquals("0,00 %", small.percentText)
        assertEquals("0,05 %", make(lenDm = 100, dropMm = 5).percentText)
        assertFalse(big.percentText.contains('.'))
    }

    @Test
    fun sollIsClampedToRange() {
        assertEquals(5, make(sollDefault = 0.2).sollTenths)
        assertTrue(make(sollDefault = 0.2).verdictText.endsWith("Soll ≥ 0,5 %"))
        assertEquals(30, make(sollDefault = 4.0).sollTenths)
        assertTrue(make(sollDefault = 4.0).verdictText.endsWith("Soll ≥ 3,0 %"))
    }

    @Test
    fun verdictUsesTypographicGlyphs() {
        val verdict = fixture.verdictText
        assertTrue(verdict.contains('≥'))
        assertTrue(verdict.contains('·'))
        assertTrue(verdict.contains(" · "))
    }

    @Test
    fun saverRoundTrips() {
        val moved = fixture.incrementLength().decrementDrop()
        val scope = SaverScope { true }
        val saved = with(SlopeViewModel.saver(fixture)) { scope.save(moved) }
        assertEquals(listOf(25, 20), saved)
        assertEquals(moved, SlopeViewModel.saver(fixture).restore(saved!!))
        assertNull(SlopeViewModel.saver(fixture).restore(listOf(1)))
    }
}
