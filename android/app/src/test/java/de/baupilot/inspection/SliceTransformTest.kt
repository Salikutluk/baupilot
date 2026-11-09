package de.baupilot.inspection

import org.junit.Assert.assertEquals
import org.junit.Test

class SliceTransformTest {
    private val delta = 0.0005f
    private val viewBox = Extent(100f, 80f)

    @Test
    fun androidTileWidthSlicesToHeight() {
        val t = SliceTransform.fit(viewBox, Extent(100.667f, 80f))

        assertEquals(1.006667f, t.scale, delta)
        assertEquals(0f, t.offsetX, delta)
        assertEquals(-0.2667f, t.offsetY, delta)
    }

    @Test
    fun iosTileWidthCase() {
        val t = SliceTransform.fit(viewBox, Extent(99.333f, 84f))

        assertEquals(1.05f, t.scale, delta)
        assertEquals(-2.8333f, t.offsetX, delta)
        assertEquals(0f, t.offsetY, delta)
    }

    @Test
    fun identityWhenTheBoxMatches() {
        val t = SliceTransform.fit(viewBox, viewBox)

        assertEquals(SliceTransform(1f, 0f, 0f), t)
    }
}
