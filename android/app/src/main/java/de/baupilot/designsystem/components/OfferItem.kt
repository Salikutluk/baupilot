package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens

/**
 * Offer row (AndGewerk:55): padding 12 16, radius 12, 1 border (`okBorder` when [best], else `divider`),
 * [bestBackground] fill when [best]. Firm 16 over meta 14 `muted`; price 16 / 500 (`muted` when [pending])
 * over the 12 / 500 `okFg` [bestLabel] for the best offer. Without [onClick] the row is not a button.
 */
@Composable
fun OfferItem(
    firm: String,
    meta: String,
    price: String,
    best: Boolean,
    bestLabel: String,
    pending: Boolean,
    bestBackground: ColorToken,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Tokens.Radius.listCard)
    var frame = modifier.fillMaxWidth()
    if (best) frame = frame.background(bestBackground.color(), shape)
    frame = frame.border(1.dp, (if (best) Tokens.okBorder else Tokens.divider).color(), shape).clip(shape)
    if (onClick != null) frame = frame.clickable(role = Role.Button, onClick = onClick)
    Row(
        modifier = frame.padding(horizontal = 16.dp, vertical = 12.dp).padding(1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BaupilotText(text = firm, size = 16.sp, color = Tokens.text)
            BaupilotText(text = meta, size = 14.sp, color = Tokens.muted)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BaupilotText(text = price, size = 16.sp, weight = 500, color = if (pending) Tokens.muted else Tokens.text)
            if (best) BaupilotText(text = bestLabel, size = 12.sp, weight = 500, color = Tokens.okFg)
        }
    }
}
