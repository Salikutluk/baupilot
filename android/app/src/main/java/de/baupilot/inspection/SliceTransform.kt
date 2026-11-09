package de.baupilot.inspection

/** A width and height in dp (or in viewBox units). */
data class Extent(val width: Float, val height: Float)

/** Uniform scale plus offsets that map viewBox units into the target box. */
data class SliceTransform(val scale: Float, val offsetX: Float, val offsetY: Float) {
    companion object {
        /** SVG `preserveAspectRatio="xMidYMid slice"`: `s = max(w / vw, h / vh)`, the scaled viewBox centred. */
        fun fit(viewBox: Extent, into: Extent): SliceTransform {
            val s = maxOf(into.width / viewBox.width, into.height / viewBox.height)
            return SliceTransform(
                scale = s,
                offsetX = (into.width - viewBox.width * s) / 2f,
                offsetY = (into.height - viewBox.height * s) / 2f,
            )
        }
    }
}
