package de.baupilot.screens.latertrades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.PressureCheckModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotLinearProgress
import de.baupilot.designsystem.components.BaupilotOutlinedTextField
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.BaupilotVerticalStepper
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FieldLook
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant
import de.baupilot.designsystem.components.TimerRing
import de.baupilot.designsystem.components.TitleStyle

/**
 * Sanitaer Druckprobe Trinkwasser (AndSanDruck.dc.html:17-84): close bar with the counter, the fixed 50 % progress
 * bar and three compact steppers. The current step is a card with the timer ring, the read-only start reading, the
 * editable end reading (D12: local text, never saved) and the photo button, which has no action. [onClose] is the
 * close control.
 */
@Composable
fun SanDruckScreen(
    model: PressureCheckModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val route = model.route
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                subtitle = model.subtitle,
                padding = BarPadding.NavCounter,
                navigation = Nav.Close(onClick = onClose),
                titleStyle = TitleStyle.Detail,
                trailingText = model.counter,
            )
        },
        contentPadding = PaddingValues(0.dp),
        spacing = 0.dp,
        modifier = modifier.testTag("screen.inspection.${route.tradeId}.${route.check}"),
    ) {
        BaupilotLinearProgress(progress = model.progress, modifier = Modifier.padding(horizontal = 16.dp))
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BaupilotVerticalStepper(doneSteps(model), StepperVariant.Compact, currentSpacing = 14.dp)
            BaupilotVerticalStepper(
                steps = listOf(
                    Step(
                        number = model.current.n,
                        title = model.current.title,
                        state = StepState.Current,
                        body = { CurrentBody(model) },
                    ),
                ),
                variant = StepperVariant.Compact,
                currentSpacing = 14.dp,
            )
            BaupilotVerticalStepper(nextSteps(model), StepperVariant.Compact, currentSpacing = 14.dp)
        }
    }
}

/** AndSanDruck:41-43: the done rows carry their meta in 13 / 500 `text`. */
private fun doneSteps(model: PressureCheckModel): List<Step> = model.done.mapIndexed { index, row ->
    Step(
        number = index + 1,
        title = row.label,
        state = StepState.Done,
        meta = row.meta,
        metaColor = Tokens.text,
        metaWeight = 500,
    )
}

private fun nextSteps(model: PressureCheckModel): List<Step> = model.next.mapIndexed { index, row ->
    // `n` is a JSON string; the position after the current step is the fallback for a non-numeric entry.
    Step(
        number = row.n.toIntOrNull() ?: (model.current.n + 1 + index),
        title = row.label,
        state = StepState.Open,
        tag = row.tag,
    )
}

/** AndSanDruck:35-81: the ring row, the two readings and the photo button, gap 14. */
@Composable
private fun CurrentBody(model: PressureCheckModel) {
    val timer = model.timer
    var end by rememberSaveable { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TimerRing(
                fraction = timer.fraction,
                label = timer.remaining,
                accessibilityLabel = timer.accessibilityLabel,
                modifier = Modifier.testTag("pressure.timer"),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                BaupilotText(text = timer.remainingLabel, size = 15.sp, weight = 500, color = Tokens.text)
                BaupilotText(text = timer.note, size = 13.sp, lineHeight = 18.sp, color = Tokens.muted)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                BaupilotOutlinedTextField(
                    label = timer.startLabel,
                    value = timer.startValue,
                    onValueChange = {},
                    placeholder = "",
                    notch = Tokens.surfaceContainer,
                    readOnly = true,
                    modifier = Modifier.testTag("pressure.start"),
                )
            }
            Box(Modifier.weight(1f)) {
                BaupilotOutlinedTextField(
                    label = timer.endLabel,
                    value = end,
                    onValueChange = { end = it },
                    placeholder = timer.endPlaceholder,
                    look = FieldLook.Focused,
                    notch = Tokens.surfaceContainer,
                    fieldModifier = Modifier.testTag("pressure.end"),
                )
            }
        }
        OutlinedButton(
            label = model.photoLabel,
            icon = BaupilotIcon.Camera,
            iconSize = 20.dp,
            modifier = Modifier.testTag("pressure.photo"),
        )
    }
}
