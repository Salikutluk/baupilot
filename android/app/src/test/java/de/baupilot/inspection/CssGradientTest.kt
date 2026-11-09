package de.baupilot.inspection

import org.junit.Assert.assertEquals
import org.junit.Test

class CssGradientTest {
    private val delta = 0.0005f

    @Test
    fun androidTileAt160Degrees() {
        val line = CssGradient.endpoints(angleDegrees = 160f, width = 100.6667f, height = 80f)

        assertEquals(31.5897f, line.start.x, delta)
        assertEquals(-11.4977f, line.start.y, delta)
        assertEquals(69.0770f, line.end.x, delta)
        assertEquals(91.4977f, line.end.y, delta)
    }

    @Test
    fun iosTileAt160Degrees() {
        val line = CssGradient.endpoints(angleDegrees = 160f, width = 99.3333f, height = 84f)

        assertEquals(30.3582f, line.start.x, delta)
        assertEquals(-11.0495f, line.start.y, delta)
        assertEquals(68.9751f, line.end.x, delta)
        assertEquals(95.0495f, line.end.y, delta)
    }

    @Test
    fun oneEightyDegreesRunsTopToBottom() {
        val line = CssGradient.endpoints(angleDegrees = 180f, width = 60f, height = 40f)

        assertEquals(30f, line.start.x, delta)
        assertEquals(0f, line.start.y, delta)
        assertEquals(30f, line.end.x, delta)
        assertEquals(40f, line.end.y, delta)
    }

    @Test
    fun ninetyDegreesRunsLeftToRight() {
        val line = CssGradient.endpoints(angleDegrees = 90f, width = 60f, height = 40f)

        assertEquals(0f, line.start.x, delta)
        assertEquals(20f, line.start.y, delta)
        assertEquals(60f, line.end.x, delta)
        assertEquals(20f, line.end.y, delta)
    }
}
