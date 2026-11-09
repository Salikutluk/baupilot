package de.baupilot.inspection

import org.junit.Assert.assertEquals
import org.junit.Test

class SegmentLayoutTest {
    private val delta = 0.0005f

    @Test
    fun twelveSegmentsOnTheAndroidWidth() {
        val edges = SegmentLayout.edges(count = 12, width = 326f, gap = 3f, scale = 3f)

        assertEquals(12, edges.size)
        assertEquals(0f, edges.first().start, delta)
        assertEquals(326f, edges.last().end, delta)
    }

    @Test
    fun exactSegmentWidthIsTwentyFourPointFourOneSixSeven() {
        val width = (326f - 11f * 3f) / 12f

        assertEquals(24.4167f, width, delta)
    }

    @Test
    fun everyEdgeSitsOnTheOneThirdGrid() {
        val edges = SegmentLayout.edges(count = 12, width = 326f, gap = 3f, scale = 3f)

        for (edge in edges) {
            assertEquals(Math.round(edge.start * 3f) / 3f, edge.start, 1e-4f)
            assertEquals(Math.round(edge.end * 3f) / 3f, edge.end, 1e-4f)
        }
    }

    @Test
    fun startsAreSnappedFromTheExactCumulativePosition() {
        val edges = SegmentLayout.edges(count = 12, width = 326f, gap = 3f, scale = 3f)
        val w = (326.0 - 33.0) / 12.0

        edges.forEachIndexed { i, edge ->
            val exact = i * (w + 3.0)
            assertEquals(Math.round(exact * 3.0) / 3.0, edge.start.toDouble(), 1e-4)
        }
    }

    @Test
    fun gapsAndWidthsDifferByAtMostOneDevicePixel() {
        val edges = SegmentLayout.edges(count = 12, width = 326f, gap = 3f, scale = 3f)
        val pixel = 1f / 3f

        for (edge in edges) {
            assertEquals(24.4167f, edge.end - edge.start, pixel + delta)
        }
        for (i in 1 until edges.size) {
            assertEquals(3f, edges[i].start - edges[i - 1].end, pixel + delta)
        }
    }

    @Test
    fun singleSegmentSpansTheWholeWidth() {
        val edges = SegmentLayout.edges(count = 1, width = 100f, gap = 3f, scale = 3f)

        assertEquals(listOf(SegmentEdge(0f, 100f)), edges)
    }

    @Test
    fun noSegmentsGiveNoEdges() {
        assertEquals(emptyList<SegmentEdge>(), SegmentLayout.edges(count = 0, width = 100f, gap = 3f, scale = 3f))
    }
}
