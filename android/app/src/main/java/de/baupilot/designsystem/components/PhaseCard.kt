package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens

/** State of a [PhaseCard]: a finished phase, a phase still to come, or the running one with its progress bar. */
sealed interface PhaseCardState {
    /** Outlined card, no fill, the value in `okFg`. */
    data object Done : PhaseCardState

    /** Like [Done] (outlined, no fill) but the value in `muted`: the phase has not started. */
    data object Planned : PhaseCardState

    /** `primaryContainer` card with a 4 dp bar filled to [fraction] (0..1) on [track]. */
    class Active(val fraction: Float, val track: ColorToken) : PhaseCardState
}

/**
 * Phase card of the element overview (AndElUebersicht:30-41): radius 12, padding 12, a 12 / 500 [label] over a
 * 15 / 500 [value]. [PhaseCardState.Done] and [PhaseCardState.Planned] have a 1 dp `outlineVariant` border outside the padding
 * (CSS content box) and gap 4, the value in `okFg` or `muted`;
 * [PhaseCardState.Active] has gap 6 and the bar. Two cards share one height when their row is
 * `Row(Modifier.height(IntrinsicSize.Min))` and each gets `Modifier.weight(1f).fillMaxHeight()`.
 */
@Composable
fun PhaseCard(
    label: String,
    value: String,
    state: PhaseCardState = PhaseCardState.Done,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    when (state) {
        PhaseCardState.Done, PhaseCardState.Planned -> Column(
            modifier = modifier
                .clip(shape)
                .border(1.dp, Tokens.outlineVariant.color(), shape)
                .padding(1.dp)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BaupilotText(text = label, size = 12.sp, weight = 500, color = Tokens.muted)
            BaupilotText(
                text = value,
                size = 15.sp,
                weight = 500,
                color = if (state == PhaseCardState.Planned) Tokens.muted else Tokens.okFg,
            )
        }
        is PhaseCardState.Active -> Column(
            modifier = modifier
                .clip(shape)
                .background(Tokens.primaryContainer.color())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BaupilotText(text = label, size = 12.sp, weight = 500, color = Tokens.onPrimaryContainer)
            BaupilotText(text = value, size = 15.sp, weight = 500, color = Tokens.onPrimaryContainer)
            BaupilotLinearProgress(progress = state.fraction, track = state.track)
        }
    }
}
