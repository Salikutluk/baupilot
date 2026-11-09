package de.baupilot.inspection

import kotlin.math.roundToLong

/** One segment's horizontal extent in dp. */
data class SegmentEdge(val start: Float, val end: Float)

/**
 * Segment edges of a segmented control (D12, R11). Each segment has the exact width
 * `(width - gap * (count - 1)) / count`; start and end come from the exact cumulative position
 * `i * (w + gap)` and are snapped to the nearest `1 / scale` (device pixel), never per-segment width.
 */
object SegmentLayout {
    fun edges(count: Int, width: Float, gap: Float, scale: Float): List<SegmentEdge> {
        if (count <= 0) return emptyList()
        val w = (width.toDouble() - gap.toDouble() * (count - 1)) / count
        fun snap(value: Double): Float = ((value * scale).roundToLong() / scale.toDouble()).toFloat()
        return List(count) { i ->
            val start = i * (w + gap)
            SegmentEdge(start = snap(start), end = snap(start + w))
        }
    }
}
