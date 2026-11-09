package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.wholeDpCentered
import de.baupilot.inspection.SignatureStrokes

/** What a [SignatureCard] shows. */
sealed interface SignatureCardStyle {
    /**
     * Signed tile: [label] above the [name] and the [status] line, the sample signature (described as
     * [signatureDescription]) at the end.
     */
    class Signed(
        val label: String,
        val name: String,
        val status: String,
        val signatureDescription: String,
    ) : SignatureCardStyle

    /** Open tile for the owner: [caption] on top, the [placeholder] until [strokes] has a stroke, a 1 line below. */
    class Pending(
        val caption: String,
        val placeholder: String,
        val strokes: SignatureStrokes,
        val onStrokesChange: (SignatureStrokes) -> Unit,
    ) : SignatureCardStyle
}

private val CardShape = RoundedCornerShape(12.dp)

/**
 * Signature tile (AndAbnSignatur.dc.html:54-66). Signed: radius 12, 1 `outlineVariant` border, no fill, padding 12 16,
 * row gap 12. Pending: 104 border-box, radius 12, 2 dashed `primary` border, `surfaceContainer` fill, padding 12 16,
 * a drawing surface under the texts.
 */
@Composable
fun SignatureCard(style: SignatureCardStyle, modifier: Modifier = Modifier) {
    when (style) {
        is SignatureCardStyle.Signed -> SignedCard(style, modifier)
        is SignatureCardStyle.Pending -> PendingCard(style, modifier)
    }
}

@Composable
private fun SignedCard(style: SignatureCardStyle.Signed, modifier: Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Tokens.outlineVariant.color(), CardShape)
            .padding(1.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f).then(wholeDpCentered()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BaupilotText(text = style.label, size = 12.sp, weight = 500, color = Tokens.muted)
            BaupilotText(text = style.name, size = 16.sp, color = Tokens.text)
            BaupilotText(text = style.status, size = 13.sp, color = Tokens.okFg)
        }
        SignatureSampleCanvas(
            Modifier
                .size(120.dp, 48.dp)
                .semantics { contentDescription = style.signatureDescription },
        )
    }
}

/**
 * Dash pattern of the owner tile's border in dp (R10), measured on `AbnSignatur_light.png` along the top edge:
 * period 10 (30 px), dash 6 (18 px), gap 4 (12 px). Chrome starts the pattern 3 dp earlier than a path that
 * starts at the rounded rectangle's corner, hence the [DASH_PHASE].
 */
private const val DASH_ON = 6f
private const val DASH_OFF = 4f
private const val DASH_PHASE = 3f

@Composable
private fun PendingCard(style: SignatureCardStyle.Pending, modifier: Modifier) {
    val dash = Tokens.primary.color()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .background(Tokens.surfaceContainer.color(), CardShape)
            .drawBehind {
                val width = 2.dp.toPx()
                drawRoundRect(
                    color = dash,
                    topLeft = Offset(width / 2, width / 2),
                    size = Size(size.width - width, size.height - width),
                    cornerRadius = CornerRadius(12.dp.toPx() - width / 2),
                    style = Stroke(
                        width = width,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_ON.dp.toPx(), DASH_OFF.dp.toPx()), DASH_PHASE.dp.toPx()),
                    ),
                )
            },
    ) {
        SignaturePad(
            strokes = style.strokes,
            onStrokesChange = style.onStrokesChange,
            modifier = Modifier.matchParentSize(),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            BaupilotText(text = style.caption, size = 12.sp, weight = 500, color = Tokens.muted)
            if (style.strokes.isEmpty) {
                BaupilotText(
                    text = style.placeholder,
                    size = 15.sp,
                    weight = 500,
                    color = Tokens.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Tokens.outline.color()))
        }
    }
}
