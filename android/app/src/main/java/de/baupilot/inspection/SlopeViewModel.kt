package de.baupilot.inspection

import androidx.compose.runtime.saveable.Saver
import kotlin.math.roundToInt

/** Verdict labels from the fixture (`drainSlope.labels`). */
data class SlopeLabels(val sufficient: String, val tooFlat: String, val sollPrefix: String)

/**
 * State of the drain slope calculator (D3). Immutable: every operation returns a copy. Length and
 * drop are integers (decimetres, millimetres); all texts are built with integer arithmetic only, so
 * ties round half up like JS `toFixed` and no locale can introduce grouping or a decimal point (R4).
 */
data class SlopeViewModel(
    val lenDm: Int,
    val dropMm: Int,
    val sollTenths: Int,
    val lengthMin: Int,
    val lengthMax: Int,
    val lengthStep: Int,
    val dropMin: Int,
    val dropMax: Int,
    val dropStep: Int,
    val labels: SlopeLabels,
) {
    fun incrementLength(): SlopeViewModel = copy(lenDm = (lenDm + lengthStep).coerceAtMost(lengthMax))

    fun decrementLength(): SlopeViewModel = copy(lenDm = (lenDm - lengthStep).coerceAtLeast(lengthMin))

    fun incrementDrop(): SlopeViewModel = copy(dropMm = (dropMm + dropStep).coerceAtMost(dropMax))

    fun decrementDrop(): SlopeViewModel = copy(dropMm = (dropMm - dropStep).coerceAtLeast(dropMin))

    /** Exact integer form of `dropMm / lenDm >= soll`; the boundary is inclusive. */
    val isSufficient: Boolean
        get() = 10 * dropMm >= sollTenths * lenDm

    /** Percent in hundredths, half-up: `(200 * dropMm + lenDm) / (2 * lenDm)`. */
    val percentHundredths: Int
        get() = (200 * dropMm + lenDm) / (2 * lenDm)

    val percentText: String
        get() {
            val h = percentHundredths
            val rest = h % 100
            return "${h / 100},${if (rest < 10) "0" else ""}$rest %"
        }

    val lengthText: String
        get() = "${lenDm / 10},${lenDm % 10} m"

    val dropText: String
        get() = "${dropMm / 10},${dropMm % 10} cm"

    val verdictText: String
        get() = "${if (isSufficient) labels.sufficient else labels.tooFlat} · " +
            "${labels.sollPrefix} ${sollTenths / 10},${sollTenths % 10} %"

    /** The prototype's `12 + min(26, max(2, pct * 10))`, rounded to one decimal; for drawing only. */
    val schemaY2: Double
        get() = (120 + percentHundredths.coerceIn(20, 260)).toDouble() / 10

    companion object {
        /** [sollDefault] is clamped to [sollMin]..[sollMax] in tenths of a percent. */
        fun of(
            lenDm: Int,
            dropMm: Int,
            sollDefault: Double,
            sollMin: Double,
            sollMax: Double,
            lengthMin: Int,
            lengthMax: Int,
            lengthStep: Int,
            dropMin: Int,
            dropMax: Int,
            dropStep: Int,
            labels: SlopeLabels,
        ) = SlopeViewModel(
            lenDm = lenDm,
            dropMm = dropMm,
            sollTenths = (sollDefault * 10).roundToInt()
                .coerceIn((sollMin * 10).roundToInt(), (sollMax * 10).roundToInt()),
            lengthMin = lengthMin,
            lengthMax = lengthMax,
            lengthStep = lengthStep,
            dropMin = dropMin,
            dropMax = dropMax,
            dropStep = dropStep,
            labels = labels,
        )

        /** Saves `[lenDm, dropMm]`; the rest comes from [base]. A list of any other size restores nothing. */
        fun saver(base: SlopeViewModel): Saver<SlopeViewModel, List<Int>> = Saver(
            save = { listOf(it.lenDm, it.dropMm) },
            restore = { if (it.size == 2) base.copy(lenDm = it[0], dropMm = it[1]) else null },
        )
    }
}
