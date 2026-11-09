package de.baupilot.inspection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignatureStrokesTest {
    private val empty = SignatureStrokes(Extent(100f, 50f))

    @Test
    fun startsEmpty() {
        assertTrue(empty.isEmpty)
        assertEquals(emptyList<List<Point>>(), empty.strokes)
    }

    @Test
    fun beginExtendEndCollectsOneStroke() {
        val s = empty.begin(Point(10f, 10f)).extend(Point(20f, 15f)).extend(Point(30f, 20f)).end()

        assertFalse(s.isEmpty)
        assertEquals(listOf(listOf(Point(10f, 10f), Point(20f, 15f), Point(30f, 20f))), s.strokes)
    }

    @Test
    fun secondStrokeIsSeparate() {
        val s = empty.begin(Point(1f, 1f)).extend(Point(2f, 2f)).end().begin(Point(5f, 5f)).extend(Point(6f, 6f)).end()

        assertEquals(2, s.strokes.size)
        assertEquals(listOf(Point(5f, 5f), Point(6f, 6f)), s.strokes[1])
    }

    @Test
    fun pointsAreClampedToBounds() {
        val s = empty.begin(Point(-5f, 60f)).extend(Point(120f, -1f)).end()

        assertEquals(listOf(Point(0f, 50f), Point(100f, 0f)), s.strokes.single())
    }

    @Test
    fun extendWithoutBeginIsIgnored() {
        assertEquals(empty, empty.extend(Point(10f, 10f)))
    }

    @Test
    fun extendAfterEndIsIgnored() {
        val ended = empty.begin(Point(10f, 10f)).end()

        assertEquals(ended, ended.extend(Point(20f, 20f)))
    }

    @Test
    fun tapKeepsAOnePointStroke() {
        val s = empty.begin(Point(40f, 25f)).end()

        assertFalse(s.isEmpty)
        assertEquals(listOf(listOf(Point(40f, 25f))), s.strokes)
    }

    @Test
    fun endWithoutBeginIsIgnored() {
        assertEquals(empty, empty.end())
    }
}
