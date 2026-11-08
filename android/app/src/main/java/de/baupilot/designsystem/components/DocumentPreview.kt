package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.cssShadow
import kotlin.math.max
import kotlin.math.round

private const val LINE_FACTOR = 1.35f
private const val LINE_HEIGHT_DP = 7 * LINE_FACTOR
private const val ROW_PADDING_DP = 4f
private const val HEADER_DP = 10 * LINE_FACTOR + 1f + LINE_HEIGHT_DP
private const val NUMBER_DP = 4f + 9 * LINE_FACTOR + 1f + LINE_HEIGHT_DP
private const val TOTAL_DP = 2f + 6f + 9 * LINE_FACTOR
private const val PAGE_HEIGHT_DP = 320f
private const val PAGE_BOTTOM_PADDING_DP = 14f

/** One position of the preview page: number, description and sum. */
class PreviewLine(val pos: String, val text: String, val sum: String)

/**
 * Paper preview of an offer (AndDokViewer:35-59): 250 x 320, padding 18 18 14, always white (`doc*` tokens,
 * the same in dark mode), shadow 0 4 14. Page text is 7 sp with the CSS line height 1.35 (D11). The page is one
 * semantics node, an image described by [contentDescription].
 */
@Composable
fun DocumentPreview(
    issuerName: String,
    issuerAddress: String,
    number: String,
    reference: String,
    lines: List<PreviewLine>,
    totalLabel: String,
    total: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    radius: Dp = 2.dp,
) {
    val shape = RoundedCornerShape(radius)
    // Chrome keeps the fractional CSS heights (a 7 sp line is 9.45 dp) and paints every box edge on a whole dp.
    // Each block gets round(end) - round(start) of the running total, so rules and rows land where Chrome's do.
    val grid = WholeDpGrid()
    Column(
        modifier = modifier
            .size(250.dp, 320.dp)
            .cssShadow(4.dp, 14.dp, Tokens.docShadow, shape)
            .background(Tokens.docPaper.color(), shape)
            .padding(horizontal = 18.dp)
            .clearAndSetSemantics {
                this.contentDescription = contentDescription
                role = Role.Image
            },
    ) {
        Spacer(Modifier.height(grid.next(18f)))
        Row(
            Modifier.fillMaxWidth().height(grid.next(HEADER_DP)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                PageText(issuerName, 10, 700, Tokens.docBrand)
                PageText(issuerAddress, 7, 400, Tokens.docMeta)
            }
            Box(Modifier.size(22.dp).background(Tokens.docBrand.color(), RoundedCornerShape(4.dp)))
        }
        Spacer(Modifier.height(grid.next(9f)))
        Column(Modifier.fillMaxWidth().height(grid.next(NUMBER_DP)).padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            PageText(number, 9, 700, Tokens.docInk)
            PageText(reference, 7, 400, Tokens.docMeta)
        }
        Spacer(Modifier.height(grid.next(9f)))
        Rule(grid.next(1f), Tokens.docRule)
        lines.forEach { line ->
            Row(
                Modifier.fillMaxWidth().height(grid.next(LINE_HEIGHT_DP + 2 * ROW_PADDING_DP)).padding(vertical = ROW_PADDING_DP.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PageText(line.pos, 7, 400, Tokens.docPos, Modifier.width(12.dp))
                PageText(line.text, 7, 400, Tokens.docInk, Modifier.weight(1f))
                PageText(line.sum, 7, 600, Tokens.docInk)
            }
            Rule(grid.next(1f), Tokens.docRuleLight)
        }
        // margin-top: auto pins the total to the bottom edge of the content box
        Spacer(Modifier.height(grid.next(max(0f, PAGE_HEIGHT_DP - PAGE_BOTTOM_PADDING_DP - TOTAL_DP - grid.edge))))
        Rule(grid.next(2f), Tokens.docInk)
        Row(
            Modifier.fillMaxWidth().height(grid.next(6f + 9 * LINE_FACTOR)).padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PageText(totalLabel, 9, 700, Tokens.docInk)
            PageText(total, 9, 700, Tokens.docInk)
        }
    }
}

/** Running total of fractional dp heights; [next] hands out whole-dp heights whose edges are the rounded totals. */
internal class WholeDpGrid {
    var edge = 0f
        private set
    private var placed = 0

    fun next(height: Float): Dp {
        edge += height
        val end = round(edge).toInt()
        val whole = max(0, end - placed)
        placed += whole
        return whole.dp
    }
}

@Composable
private fun Rule(thickness: Dp, color: ColorToken) {
    Box(Modifier.fillMaxWidth().height(thickness).background(color.color()))
}

@Composable
private fun PageText(text: String, size: Int, weight: Int, color: ColorToken, modifier: Modifier = Modifier) {
    BaupilotText(
        text = text,
        size = size.sp,
        weight = weight,
        color = color,
        lineHeight = (size * LINE_FACTOR).sp,
        modifier = modifier,
    )
}
