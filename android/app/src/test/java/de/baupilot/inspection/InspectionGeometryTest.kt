package de.baupilot.inspection

import org.junit.Assert.assertEquals
import org.junit.Test

class InspectionGeometryTest {
    private fun cubic(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) =
        CubicSegment(Point(x1, y1), Point(x2, y2), Point(x, y))

    @Test
    fun signatureSampleIsNineAbsoluteCubicsFromSixThirtyFour() {
        assertEquals(Point(6f, 34f), SignatureSample.start)
        assertEquals(Extent(120f, 48f), SignatureSample.box)
        assertEquals(2f, SignatureSample.strokeWidth, 0f)
        assertEquals(
            listOf(
                cubic(14f, 14f, 20f, 10f, 22f, 18f),
                cubic(24f, 26f, 16f, 36f, 22f, 32f),
                cubic(28f, 28f, 34f, 10f, 38f, 12f),
                cubic(42f, 14f, 36f, 30f, 42f, 28f),
                cubic(48f, 26f, 50f, 16f, 54f, 18f),
                cubic(58f, 20f, 56f, 28f, 62f, 26f),
                cubic(68f, 24f, 72f, 18f, 76f, 20f),
                cubic(80f, 22f, 82f, 26f, 90f, 22f),
                cubic(98f, 18f, 102f, 16f, 110f, 14f),
            ),
            SignatureSample.segments,
        )
    }

    @Test
    fun signatureSampleSegmentsAreTheRelativePathSummedUp() {
        // M6 34 c8-20 14-24 16-16 s-6 18 0 14 ... : each cubic ends at the previous end plus its relative delta.
        val ends = SignatureSample.segments.map { it.end }

        assertEquals(
            listOf(
                Point(22f, 18f), Point(22f, 32f), Point(38f, 12f), Point(42f, 28f), Point(54f, 18f),
                Point(62f, 26f), Point(76f, 20f), Point(90f, 22f), Point(110f, 14f),
            ),
            ends,
        )
    }

    @Test
    fun sketchBoxAndFrame() {
        val g = WindowLevelSketch.Geometry

        assertEquals(Extent(56f, 44f), g.box)
        assertEquals(SketchRect(8f, 12f, 32f, 30f, 1.5f), g.frame)
        assertEquals(2f, g.frameStrokeWidth, 0f)
        assertEquals(SketchLine(Point(24f, 12f), Point(24f, 42f)), g.mullion)
        assertEquals(1.5f, g.mullionStrokeWidth, 0f)
    }

    @Test
    fun sketchLevelsAndBubbles() {
        val g = WindowLevelSketch.Geometry

        assertEquals(SketchRect(6f, 3f, 36f, 6f, 2f), g.topLevel)
        assertEquals(SketchCircle(Point(24f, 6f), 2f), g.topBubble)
        assertEquals(SketchRect(45f, 12f, 6f, 30f, 2f), g.sideLevel)
        assertEquals(SketchCircle(Point(48f, 27f), 2f), g.sideBubble)
    }

    @Test
    fun photoSceneMatchesTheSvg() {
        val s = PhotoScene

        assertEquals(Extent(100f, 80f), s.viewBox)
        assertEquals(160f, s.gradientAngleDegrees, 0f)
        assertEquals(listOf(0f, 0.6f, 1f), s.gradientStops)
        assertEquals(SketchRect(22f, 10f, 56f, 62f, 0f), s.frame)
        assertEquals(5f, s.frameStrokeWidth, 0f)
        assertEquals(SketchLine(Point(50f, 10f), Point(50f, 72f)), s.mullion)
        assertEquals(3f, s.mullionStrokeWidth, 0f)
        assertEquals(SketchRect(18f, 4f, 64f, 7f, 2f), s.level)
    }
}
