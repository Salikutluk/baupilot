package de.baupilot.inspection

import androidx.compose.runtime.saveable.Saver
import kotlin.math.abs

enum class MeasureDimension { WIDTH, HEIGHT }

/**
 * State of the measurement screen (D3). Immutable: every operation returns a copy. Width and height
 * are measured against their targets; a dimension is within tolerance when `abs(actual − target)`
 * is at most [tolerance] (inclusive). The screen maps [isWithinTolerance] to its fixture labels.
 */
data class MeasureViewModel(
    val targetW: Int,
    val targetH: Int,
    val actualW: Int,
    val actualH: Int,
    val step: Int,
    val tolerance: Int,
    val unit: String,
) {
    /** Adds one [step]; there is no upper bound. */
    fun increment(d: MeasureDimension): MeasureViewModel = moved(d, step)

    /**
     * Subtracts one [step]. Deviation from the prototype (R8): a decrement that would go below 0 is
     * a no-op, so a measured value is never negative.
     */
    fun decrement(d: MeasureDimension): MeasureViewModel = moved(d, -step)

    /** Sign, absolute difference, space, unit: `+` above target, `−` below, `±` on target. */
    fun diffText(d: MeasureDimension): String {
        val diff = diff(d)
        val sign = if (diff > 0) "+" else if (diff < 0) "−" else "±"
        return "$sign${abs(diff)} $unit"
    }

    /** Plain integer, never grouped: "1005", not "1.005" (R9), whatever the device locale. */
    fun valueText(d: MeasureDimension): String = actual(d).toString()

    fun isWithin(d: MeasureDimension): Boolean = abs(diff(d)) <= tolerance

    val isWithinTolerance: Boolean
        get() = isWithin(MeasureDimension.WIDTH) && isWithin(MeasureDimension.HEIGHT)

    private fun actual(d: MeasureDimension) = if (d == MeasureDimension.WIDTH) actualW else actualH

    private fun target(d: MeasureDimension) = if (d == MeasureDimension.WIDTH) targetW else targetH

    private fun diff(d: MeasureDimension) = actual(d) - target(d)

    private fun moved(d: MeasureDimension, delta: Int): MeasureViewModel {
        val next = actual(d) + delta
        if (next < 0) return this
        return if (d == MeasureDimension.WIDTH) copy(actualW = next) else copy(actualH = next)
    }

    companion object {
        /** [toleranceDefault] is clamped to [toleranceMin]..[toleranceMax]. */
        fun of(
            target: Pair<Int, Int>,
            actual: Pair<Int, Int>,
            step: Int,
            toleranceDefault: Int,
            toleranceMin: Int,
            toleranceMax: Int,
            unit: String,
        ) = MeasureViewModel(
            targetW = target.first,
            targetH = target.second,
            actualW = actual.first,
            actualH = actual.second,
            step = step,
            tolerance = toleranceDefault.coerceIn(toleranceMin, toleranceMax),
            unit = unit,
        )

        /**
         * Saves only the entered values `[actualW, actualH]`; targets, step, tolerance and unit come
         * from the fixture, so they are restored from [base]. A list of any other size restores nothing.
         */
        fun saver(base: MeasureViewModel): Saver<MeasureViewModel, List<Int>> = Saver(
            save = { listOf(it.actualW, it.actualH) },
            restore = { if (it.size == 2) base.copy(actualW = it[0], actualH = it[1]) else null },
        )
    }
}
