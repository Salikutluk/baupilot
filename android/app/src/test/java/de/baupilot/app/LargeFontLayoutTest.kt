package de.baupilot.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.ext.junit.rules.ActivityScenarioRule
import de.baupilot.designsystem.components.PAGE_TEXT_TAG
import de.baupilot.snapshot.ScreenCatalog
import de.baupilot.snapshot.TestAppData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** One text element that is allowed to overflow because its prototype line is a one-line ellipsis. */
data class IntendedEllipsis(val screen: String, val tag: String, val prototypeLine: String)

/**
 * A text of a fixed-geometry mock-up that overflows its box in height by at most [maxOverDp] dp (measured from
 * `TextLayoutResult`: text height minus available height). Not an ellipsis: the box is a whole-dp rounding of a
 * fractional CSS height, so the gap is the same at font scale 1.
 */
data class FixedGeometryGap(val screen: String, val tag: String, val maxOverDp: Double, val reason: String)

/** One finding of [LargeFontLayoutTest]: [kind] is `overflow` or `overlap`; [element] names the node(s). */
data class LayoutFinding(val kind: String, val element: String, val detail: String)

/**
 * Allowlist of intended one-line ellipsis (plan 5.10, D16): matched by screen and test tag, with the prototype line
 * (`white-space: nowrap` / `text-overflow: ellipsis`) that justifies it. One entry per prototype line.
 */
object LargeFontAllowlist {
    val ellipsis: List<IntendedEllipsis> = listOf(
        // The row names of the schedule chart (118 dp column): `white-space: nowrap; text-overflow: ellipsis`.
        IntendedEllipsis("Zeitplan", "schedule.row.name", "AndZeitplan.dc.html:57"),
        // The document row titles: `white-space: nowrap; overflow: hidden; text-overflow: ellipsis`.
        IntendedEllipsis("Dokumente", "documents.row.title", "AndDokumente.dc.html:53"),
    )

    /**
     * DokViewer: the paper preview is laid out at font scale 1 (`DocumentPreview`) and its box heights are the
     * whole-dp rounding of Chrome's fractional ones (7 sp x 1.35 = 9.45 dp). Measured (Robolectric, density 1):
     * 11 texts of 10.00 dp in a 9.00 dp box and 2 texts of 13.00 dp in a 12.00 dp box, so 1.00 dp over each.
     */
    val fixedGeometryGaps: List<FixedGeometryGap> = listOf(
        FixedGeometryGap("DokViewer", PAGE_TEXT_TAG, 1.0, "AndDokViewer.dc.html:35-59, fractional line heights on a whole-dp grid"),
    )

    fun allowsGap(screen: String, tag: String?, overDp: Double): Boolean =
        tag != null && fixedGeometryGaps.any { it.screen == screen && it.tag == tag && overDp <= it.maxOverDp + 0.001 }

    fun allows(screen: String, tag: String?): Boolean = tag != null && ellipsis.any { it.screen == screen && it.tag == tag }
}

/** Pure checks over a semantics tree, shared by the screen test and the control tests. */
object LargeFontLayout {
    /** Two texts overlap when their frames intersect by more than this, in dp^2. */
    const val OVERLAP_TOLERANCE_DP2 = 1.0

    private class TextNode(val node: SemanticsNode, val text: String, val tag: String?)

    private fun SemanticsNode.descendants(): Sequence<SemanticsNode> =
        sequenceOf(this) + children.asSequence().flatMap { it.descendants() }

    private fun SemanticsNode.isAncestorOf(other: SemanticsNode): Boolean {
        var parent = other.parent
        while (parent != null) {
            if (parent.id == id) return true
            parent = parent.parent
        }
        return false
    }

    private fun SemanticsNode.layoutResult(): TextLayoutResult? {
        val action = config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action ?: return null
        val results = mutableListOf<TextLayoutResult>()
        return if (action(results)) results.firstOrNull() else null
    }

    private fun SemanticsNode.label(): String =
        config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text } ?: ""

    private fun SemanticsNode.tag(): String? = config.getOrNull(SemanticsProperties.TestTag)

    private fun TextNode.name(): String = (tag?.let { "tag=$it " } ?: "") + "\"$text\""

    fun check(screen: String, root: SemanticsNode, density: Float): List<LayoutFinding> {
        val texts = root.descendants()
            .filter { it.config.contains(SemanticsActions.GetTextLayoutResult) }
            .map { TextNode(it, it.label(), it.tag()) }
            .toList()
        val findings = mutableListOf<LayoutFinding>()
        for (t in texts) {
            val result = t.node.layoutResult() ?: continue
            if (!(result.didOverflowHeight || result.didOverflowWidth)) continue
            if (LargeFontAllowlist.allows(screen, t.tag)) continue
            val axes = listOfNotNull("height".takeIf { result.didOverflowHeight }, "width".takeIf { result.didOverflowWidth })
            val maxHeight = result.layoutInput.constraints.maxHeight
            val overDp = (result.multiParagraph.height - maxHeight) / density.toDouble()
            if (result.didOverflowHeight && !result.didOverflowWidth && LargeFontAllowlist.allowsGap(screen, t.tag, overDp)) continue
            val measured = "text %.2f dp, available %.2f dp, over %.2f dp".format(
                result.multiParagraph.height / density,
                maxHeight / density,
                (result.multiParagraph.height - maxHeight) / density,
            )
            findings += LayoutFinding("overflow", t.name(), "overflows ${axes.joinToString("+")}; lines=${result.lineCount}; $measured")
        }
        for (i in texts.indices) for (j in i + 1 until texts.size) {
            val a = texts[i].node
            val b = texts[j].node
            if (a.isAncestorOf(b) || b.isAncestorOf(a)) continue
            val ra = a.boundsInRoot
            val rb = b.boundsInRoot
            val w = minOf(ra.right, rb.right) - maxOf(ra.left, rb.left)
            val h = minOf(ra.bottom, rb.bottom) - maxOf(ra.top, rb.top)
            if (w <= 0f || h <= 0f) continue
            val area = (w / density).toDouble() * (h / density)
            if (area > OVERLAP_TOLERANCE_DP2) {
                findings += LayoutFinding("overlap", "${texts[i].name()} x ${texts[j].name()}", "%.1f dp^2".format(area))
            }
        }
        return findings
    }
}

/**
 * Large-text layout checks over all 34 screens at font scale 1.3 (plan 5.10, D15): (1) no text layout overflows its
 * constraints, (2) no two unrelated texts overlap. Path: Robolectric `GraphicsMode.NATIVE` (see build/m7/notes/p15t2.md).
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(fontScale = 1.3f, qualifiers = "w411dp-h3000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LargeFontLayoutTest(private val screen: String) {
    @get:Rule
    val rule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> =
        createAndroidComposeRule<ComponentActivity>()

    @Test
    fun noClippedOrOverlappingTextAtFontScale130() {
        val data = TestAppData.load()
        var fontScale = 0f
        var density = 0f
        rule.setContent {
            val d = androidx.compose.ui.platform.LocalDensity.current
            fontScale = d.fontScale
            density = d.density
            BaupilotAppContent(ScreenCatalog.state(screen), onStateChange = {}, data = data)
        }
        rule.waitForIdle()
        assertEquals("$screen must be laid out at font scale 1.3", 1.3f, fontScale, 0.001f)
        val root = rule.onRoot(useUnmergedTree = true).fetchSemanticsNode()
        val findings = LargeFontLayout.check(screen, root, density)
        assertTrue(
            "$screen at font scale 1.3: ${findings.size} findings\n" +
                findings.joinToString("\n") { "  ${it.kind}: ${it.element} (${it.detail})" },
            findings.isEmpty(),
        )
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenCatalog.all.map { (name, _) -> arrayOf<Any>(name) }
    }
}
