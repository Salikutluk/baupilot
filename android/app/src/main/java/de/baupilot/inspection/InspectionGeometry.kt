package de.baupilot.inspection

/** Rounded rectangle in viewBox units (`rx` 0 = square corners). */
data class SketchRect(val x: Float, val y: Float, val width: Float, val height: Float, val rx: Float)

data class SketchLine(val from: Point, val to: Point)

data class SketchCircle(val center: Point, val radius: Float)

/** One absolute cubic Bezier segment: two control points and the end point. */
data class CubicSegment(val control1: Point, val control2: Point, val end: Point)

/**
 * The "So prüfen" sketch (56 × 44, SVG defaults: butt caps, miter joins).
 * Source: AndAbnPruefung.dc.html:57, IosAbnPruefung.dc.html:51 (same coordinates).
 * Colours are applied by the view: frame and mullion strokes `muted`, levels `primary`, bubbles `surfaceContainerHigh`.
 */
object WindowLevelSketch {
    object Geometry {
        val box = Extent(56f, 44f)
        val frame = SketchRect(8f, 12f, 32f, 30f, 1.5f)
        const val frameStrokeWidth = 2f
        val mullion = SketchLine(Point(24f, 12f), Point(24f, 42f))
        const val mullionStrokeWidth = 1.5f
        val topLevel = SketchRect(6f, 3f, 36f, 6f, 2f)
        val topBubble = SketchCircle(Point(24f, 6f), 2f)
        val sideLevel = SketchRect(45f, 12f, 6f, 30f, 2f)
        val sideBubble = SketchCircle(Point(48f, 27f), 2f)
    }
}

/**
 * The photo tile scene, drawn in viewBox units over the CSS gradient (stops 0, 0.6, 1).
 * Source: AndAbnPruefung.dc.html:58. Colours: frame and mullion `photoFrame`, level `photoLevel`.
 */
object PhotoScene {
    val viewBox = Extent(100f, 80f)
    const val gradientAngleDegrees = 160f
    val gradientStops = listOf(0f, 0.6f, 1f)
    val frame = SketchRect(22f, 10f, 56f, 62f, 0f)
    const val frameStrokeWidth = 5f
    val mullion = SketchLine(Point(50f, 10f), Point(50f, 72f))
    const val mullionStrokeWidth = 3f
    val level = SketchRect(18f, 4f, 64f, 7f, 2f)
}

/**
 * The craftsman's signature sample in a 120 × 48 box, stroke 2, round cap and join, colour `text`.
 * Source: AndAbnSignatur.dc.html:60 (`M6 34c8-20 14-24 16-16s-6 18 …`) as 9 absolute cubic segments.
 */
object SignatureSample {
    val box = Extent(120f, 48f)
    const val strokeWidth = 2f
    val start = Point(6f, 34f)
    val segments = listOf(
        cubic(14f, 14f, 20f, 10f, 22f, 18f),
        cubic(24f, 26f, 16f, 36f, 22f, 32f),
        cubic(28f, 28f, 34f, 10f, 38f, 12f),
        cubic(42f, 14f, 36f, 30f, 42f, 28f),
        cubic(48f, 26f, 50f, 16f, 54f, 18f),
        cubic(58f, 20f, 56f, 28f, 62f, 26f),
        cubic(68f, 24f, 72f, 18f, 76f, 20f),
        cubic(80f, 22f, 82f, 26f, 90f, 22f),
        cubic(98f, 18f, 102f, 16f, 110f, 14f),
    )

    private fun cubic(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) =
        CubicSegment(Point(x1, y1), Point(x2, y2), Point(x, y))
}
