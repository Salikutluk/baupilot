package de.baupilot.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs
import kotlin.math.ceil

/**
 * The line boxes of `BaupilotText` at large font scales (D16, I8). Compose 1.11.4 resolves an sp `lineHeight`
 * proportionally only above fontScale 1.05; up to there it is `lineHeight.toPx()`. Scale <= 1.05 must stay as today.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LargeFontLineBoxTest {
    @get:Rule
    val rule = createComposeRule()

    /** Sizes in use without an explicit line height. */
    private val sizes = listOf(10f, 11f, 12f, 13f, 14f, 15f, 16f, 17f, 18f, 20f, 22f, 24f, 26f, 28f, 32f)

    /**
     * `(size, lineHeight)` pairs in use with an explicit CSS line height (grep of `lineHeight =` in `src/main`; the
     * 7 / 9 / 10 sp pairs of `DocumentPreview` are laid out at a fixed font scale 1 and are not scaled).
     */
    private val explicit = listOf(
        13f to 18f, 14f to 18f, 14f to 20f, 16f to 24f, 22f to 28f, 28f to 36f, 32f to 40f,
    )

    private class Measured(val density: Density, val results: Map<String, TextLayoutResult>)

    private fun style(size: Float, lineHeight: Float) = TextStyle(
        fontSize = size.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = Roboto,
        lineHeight = lineHeight.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Top,
            trim = LineHeightStyle.Trim.None,
        ),
    )

    private fun measure(): Measured {
        val results = mutableMapOf<String, TextLayoutResult>()
        lateinit var density: Density
        rule.setContent {
            density = LocalDensity.current
            Column {
                for (size in sizes) {
                    BasicText(
                        text = "Hxg",
                        style = style(size, RobotoMetrics.lineBox(size)),
                        onTextLayout = { results["box:$size"] = it },
                    )
                }
                for ((size, lineHeight) in explicit) {
                    BasicText(
                        text = "Hxg",
                        style = style(size, lineHeight),
                        onTextLayout = { results["css:$size/$lineHeight"] = it },
                    )
                }
            }
        }
        rule.waitForIdle()
        return Measured(density, results)
    }

    private fun assertWithinOnePx(label: String, expected: Int, actual: Int) {
        assertEquals("$label expected $expected, got $actual", true, abs(expected - actual) <= 1)
    }

    @Test
    @Config(fontScale = 1.3f)
    fun lineBoxMatchesComposeLineHeightAt130() {
        val m = measure()
        assertEquals("font scale this test really runs at", 1.3f, m.density.fontScale, 1e-4f)
        for (size in sizes) {
            val layout = m.results.getValue("box:$size")
            val composePx = (layout.getLineBottom(0) - layout.getLineTop(0)).toInt()
            assertWithinOnePx("lineBox($size)", composePx, with(m.density) { robotoLineBoxPx(size) })
        }
    }

    @Test
    @Config(fontScale = 1.3f)
    fun baselineMatchesComposeAt130() {
        val m = measure()
        assertEquals("font scale this test really runs at", 1.3f, m.density.fontScale, 1e-4f)
        for (size in sizes) {
            val composePx = m.results.getValue("box:$size").firstBaseline.toInt()
            assertWithinOnePx("baseline($size)", composePx, with(m.density) { robotoBaselinePx(size) })
        }
        // Compose clamps a line below the font's own height and puts the leading beneath the first baseline, so its
        // first baseline is no reference for the CSS one. The premise is its proportional resolution of the explicit
        // line height; the baseline, resolved the same way, must sit inside that line and below the Roboto baseline.
        for ((size, lineHeight) in explicit) {
            val layout = m.results.getValue("css:$size/$lineHeight")
            val linePx = (layout.getLineBottom(0) - layout.getLineTop(0)).toInt()
            val baselinePx = with(m.density) { cssBaselinePx(size, lineHeight) }
            assertWithinOnePx("lineHeight($size/$lineHeight)", linePx, with(m.density) { ceil(lineHeight / size * size.sp.toPx()).toInt() })
            assertEquals("cssBaseline($size/$lineHeight) inside the line", true, baselinePx in 1 until linePx)
            assertEquals("cssBaseline($size/$lineHeight) not above the Roboto baseline", true, baselinePx >= with(m.density) { robotoBaselinePx(size) })
        }
    }

    @Test
    @Config(fontScale = 1f)
    fun helpersUnchangedAtScale1() = assertHelpersAreTodaysExpressions(1f)

    @Test
    @Config(fontScale = 1.04f)
    fun helpersUnchangedAt104() = assertHelpersAreTodaysExpressions(1.04f)

    private fun assertHelpersAreTodaysExpressions(expectedFontScale: Float) {
        lateinit var density: Density
        rule.setContent { density = LocalDensity.current }
        rule.waitForIdle()
        assertEquals("font scale this test really runs at", expectedFontScale, density.fontScale, 1e-4f)
        with(density) {
            for (size in sizes) {
                assertEquals("box($size)", RobotoMetrics.lineBox(size).sp.roundToPx(), robotoLineBoxPx(size))
                assertEquals("baseline($size)", RobotoMetrics.baselineOffset(size).sp.roundToPx(), robotoBaselinePx(size))
            }
            for ((size, lineHeight) in explicit) {
                val halfLeading = kotlin.math.floor((lineHeight - RobotoMetrics.lineBox(size)) / 2f)
                val today = (RobotoMetrics.baselineOffset(size) + halfLeading).sp.roundToPx()
                assertEquals("css($size/$lineHeight)", today, cssBaselinePx(size, lineHeight))
            }
        }
    }
}
