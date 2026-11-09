package de.baupilot.inspection

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Start and end point of a gradient line in the box's own coordinates (origin top left). */
data class GradientLine(val start: Point, val end: Point)

/**
 * Endpoints of a CSS `linear-gradient(<angle>, …)` in a width × height box. CSS angles run clockwise from
 * "to top"; the line passes through the box centre and has length `|W·sinθ| + |H·cosθ|`.
 */
object CssGradient {
    fun endpoints(angleDegrees: Float, width: Float, height: Float): GradientLine {
        val theta = Math.toRadians(angleDegrees.toDouble())
        val dx = sin(theta)
        val dy = -cos(theta)
        val half = (abs(width * dx) + abs(height * dy)) / 2.0
        val cx = width / 2.0
        val cy = height / 2.0
        return GradientLine(
            start = Point((cx - dx * half).toFloat(), (cy - dy * half).toFloat()),
            end = Point((cx + dx * half).toFloat(), (cy + dy * half).toFloat()),
        )
    }
}
