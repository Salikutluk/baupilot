package de.baupilot.inspection

import androidx.compose.runtime.saveable.Saver

/** Result and summary labels from the fixture (`screedFlatness.labels`). */
data class FlatnessLabels(
    val within: String,
    val outside: String,
    val none: String,
    val point: String,
    val pluralSuffix: String,
    val overLimit: String,
)

/**
 * State of the screed flatness screen (D4). Immutable: every operation returns a copy. The gap is
 * within the limit when `gap <= limit` (inclusive); a saved point is over the limit when
 * `p > limit` (strict).
 */
data class FlatnessViewModel(
    val limit: Int,
    val gap: Int,
    val points: List<Int>,
    val gapMin: Int,
    val gapMax: Int,
    val gapStep: Int,
    val labels: FlatnessLabels,
) {
    fun increment(): FlatnessViewModel = copy(gap = (gap + gapStep).coerceAtMost(gapMax))

    fun decrement(): FlatnessViewModel = copy(gap = (gap - gapStep).coerceAtLeast(gapMin))

    /** Appends the current gap as a point and keeps the gap (the prototype's `points.concat([gap])`). */
    fun save(): FlatnessViewModel = copy(points = points + gap)

    val isWithin: Boolean
        get() = gap <= limit

    val overCount: Int
        get() = points.count { it > limit }

    val nextIndex: Int
        get() = points.size + 1

    val gapText: String
        get() = "$gap mm"

    val limitText: String
        get() = "$limit mm"

    /** One string with U+00B7 between plain spaces: "M1 · 2 mm". */
    fun pointText(i: Int): String = "M${i + 1} · ${points[i]} mm"

    val resultText: String
        get() = if (isWithin) labels.within else labels.outside

    val summaryText: String
        get() {
            val n = overCount
            if (n == 0) return labels.none
            return "$n ${labels.point}${if (n > 1) labels.pluralSuffix else ""} ${labels.overLimit}"
        }

    val sketchLineHeight: Int
        get() = minOf(26, 2 + 2 * gap)

    companion object {
        /** [limitDefault] is clamped to [limitMin]..[limitMax]. */
        fun of(
            limitDefault: Int,
            limitMin: Int,
            limitMax: Int,
            gap: Int,
            points: List<Int>,
            gapMin: Int,
            gapMax: Int,
            gapStep: Int,
            labels: FlatnessLabels,
        ) = FlatnessViewModel(
            limit = limitDefault.coerceIn(limitMin, limitMax),
            gap = gap,
            points = points,
            gapMin = gapMin,
            gapMax = gapMax,
            gapStep = gapStep,
            labels = labels,
        )

        /** Saves `[gap] + points`; the rest comes from [base]. An empty list restores nothing. */
        fun saver(base: FlatnessViewModel): Saver<FlatnessViewModel, List<Int>> = Saver(
            save = { listOf(it.gap) + it.points },
            restore = { if (it.isNotEmpty()) base.copy(gap = it[0], points = it.drop(1)) else null },
        )
    }
}
