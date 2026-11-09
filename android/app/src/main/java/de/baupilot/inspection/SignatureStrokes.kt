package de.baupilot.inspection

/** A point in the drawing surface's coordinates. */
data class Point(val x: Float, val y: Float)

/**
 * Strokes of the owner's signature surface (D10). Immutable: every operation returns a new value.
 * Points are clamped to `0..width` × `0..height`. `begin` opens a stroke, `extend` adds to the open
 * stroke (ignored when none is open), `end` closes it; a `begin` + `end` without movement keeps a
 * one-point stroke, drawn as a dot.
 */
data class SignatureStrokes(
    val bounds: Extent,
    val strokes: List<List<Point>> = emptyList(),
    val isDrawing: Boolean = false,
) {
    val isEmpty: Boolean get() = strokes.isEmpty()

    fun begin(at: Point): SignatureStrokes =
        copy(strokes = strokes + listOf(listOf(clamp(at))), isDrawing = true)

    fun extend(to: Point): SignatureStrokes {
        if (!isDrawing) return this
        val open = strokes.last() + clamp(to)
        return copy(strokes = strokes.dropLast(1) + listOf(open))
    }

    fun end(): SignatureStrokes = if (isDrawing) copy(isDrawing = false) else this

    private fun clamp(p: Point) = Point(p.x.coerceIn(0f, bounds.width), p.y.coerceIn(0f, bounds.height))
}
