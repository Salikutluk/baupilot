package de.baupilot.compare

/** Widths of the three comparison columns (label, value, value) in dp; the two value columns are equal. */
data class ComparisonWidths(val label: Float, val value: Float)

/**
 * Column widths of the comparison table (D12): the label column is fixed and the two value columns share
 * `cardWidth - 2 * padding - labelColumn - 2 * gap` equally. A card too narrow for the label column
 * yields value columns of 0.
 */
fun columns(cardWidth: Float, padding: Float, gap: Float, labelColumn: Float): ComparisonWidths {
    val value = (cardWidth - 2f * padding - labelColumn - 2f * gap) / 2f
    return ComparisonWidths(label = labelColumn, value = maxOf(value, 0f))
}
