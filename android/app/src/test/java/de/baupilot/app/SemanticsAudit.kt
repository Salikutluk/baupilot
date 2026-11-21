package de.baupilot.app

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density

/** One audit result: the broken [rule], the element (`testTag`, `label`) and its touch bounds in dp (`l,t,r,b`). */
data class Finding(val rule: String, val testTag: String?, val label: String?, val boundsDp: String)

/**
 * Semantics-based accessibility audit (plan 5.8 fallback; ATF does not run under Robolectric, see
 * `build/m7/notes/p12t2-spike.md`). Runs over a merged semantics tree.
 */
object SemanticsAudit {
    const val NO_LABEL = "no-label"
    const val IMAGE_WITHOUT_DESCRIPTION = "image-without-description"
    const val TOUCH_TARGET = "touch-target"
    const val DUPLICATE_BOUNDS = "duplicate-bounds"

    private const val MIN_TOUCH_DP = 48f

    fun check(root: SemanticsNode): List<Finding> {
        val density = root.layoutInfo.density
        val nodes = flatten(root)
        val findings = mutableListOf<Finding>()
        val clickable = nodes.filter { it.config.contains(SemanticsActions.OnClick) }

        for (node in clickable) {
            if (label(node).isEmpty()) findings += finding(NO_LABEL, node, density)
            val bounds = node.touchBoundsInRoot
            if (bounds.width < MIN_TOUCH_DP * density.density - 0.5f || bounds.height < MIN_TOUCH_DP * density.density - 0.5f) {
                findings += finding(TOUCH_TARGET, node, density)
            }
        }
        for (node in nodes) {
            if (node.config.getOrNull(SemanticsProperties.Role) == Role.Image &&
                node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty().none { it.isNotBlank() }
            ) {
                findings += finding(IMAGE_WITHOUT_DESCRIPTION, node, density)
            }
        }
        clickable.groupBy { it.touchBoundsInRoot }.values.filter { it.size > 1 }.forEach { same ->
            same.forEach { findings += finding(DUPLICATE_BOUNDS, it, density) }
        }
        return findings
    }

    private fun flatten(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap { flatten(it) }

    private fun label(node: SemanticsNode): String {
        val text = node.config.getOrNull(SemanticsProperties.Text).orEmpty().joinToString(" ") { it.text }
        val description = node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty().joinToString(" ")
        return listOf(description, text).filter { it.isNotBlank() }.joinToString(" ").trim()
    }

    private fun finding(rule: String, node: SemanticsNode, density: Density): Finding {
        val b = node.touchBoundsInRoot
        fun dp(px: Float) = (px / density.density).let { if (it % 1f == 0f) it.toInt().toString() else "%.1f".format(java.util.Locale.ROOT, it) }
        return Finding(
            rule = rule,
            testTag = node.config.getOrNull(SemanticsProperties.TestTag),
            label = label(node).ifEmpty { null },
            boundsDp = "${dp(b.left)},${dp(b.top)},${dp(b.right)},${dp(b.bottom)}",
        )
    }
}
