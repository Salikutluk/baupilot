package de.baupilot.screens.earlytrades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.baupilot.app.FiRow
import de.baupilot.app.FiTone
import de.baupilot.app.PanelCheckModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotLinearProgress
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.BaupilotVerticalStepper
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.InfoCallout
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.NumberedList
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.designsystem.components.StatusList
import de.baupilot.designsystem.components.StatusRow
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant
import de.baupilot.designsystem.components.TitleStyle

/**
 * Width split of the two buttons (AndElVerteiler.dc.html:62-63), measured on the reference: both are `flex: 1`, but
 * the `<a>` is content-box (its 1 + 1 border adds to its width) and the `<button>` keeps the browser's padding 6 + 6.
 * With 358 - 2 x 16 (card padding) = 326 minus the 8 gap = 318 shared and 2 + 12 of own extras, each gets
 * (318 - 14) / 2 = 152 plus its own extra: 154 for "Löst nicht aus", 164 for the ok button.
 */
private const val DEFECT_BUTTON_WEIGHT = 154f
private const val OK_BUTTON_WEIGHT = 164f

/**
 * Elektro Zählerschrank & FI-Test (Nr. 22, AndElVerteiler.dc.html:17-82): close bar with the counter, the fixed 50 %
 * progress bar, the warning callout and the compact stepper. The current step is a card with the numbered
 * instructions, the FI status list and the two outcome buttons; "Löst nicht aus" opens the defect form, the ok
 * button and the FI rows have no action (I6).
 */
@Composable
fun ElVerteilerScreen(
    model: PanelCheckModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (CheckStepAction) -> Unit = {},
) {
    val route = model.route
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
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
            InfoCallout(
                text = model.callout,
                icon = BaupilotIcon.BoltW22,
                container = Tokens.warnBg,
                content = Tokens.warnFg,
            )
            BaupilotVerticalStepper(steps = doneSteps(model), variant = StepperVariant.Compact)
            BaupilotVerticalStepper(steps = listOf(currentStep(model, onAction)), variant = StepperVariant.Compact)
            BaupilotVerticalStepper(steps = nextSteps(model), variant = StepperVariant.Compact)
        }
    }
}

private fun doneSteps(model: PanelCheckModel): List<Step> = model.done.mapIndexed { index, row ->
    Step(number = index + 1, title = row.label, state = StepState.Done, meta = row.meta)
}

private fun nextSteps(model: PanelCheckModel): List<Step> = model.next.mapIndexed { index, row ->
    // `n` is a JSON string; the position after the current step is the fallback for a non-numeric entry.
    Step(
        number = row.n.toIntOrNull() ?: (model.current.n + 1 + index),
        title = row.label,
        state = StepState.Open,
        tag = row.tag,
    )
}

private fun currentStep(model: PanelCheckModel, onAction: (CheckStepAction) -> Unit): Step = Step(
    number = model.current.n,
    title = model.current.title,
    state = StepState.Current,
    body = { CurrentBody(model, onAction) },
)

@Composable
private fun CurrentBody(model: PanelCheckModel, onAction: (CheckStepAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NumberedList(items = listOf(model.current.text) + model.current.steps, color = Tokens.muted)
        StatusList(
            rows = model.fis.map { it.toRow() },
            background = Tokens.bg,
            divider = Tokens.outlineVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            OutlinedButton(
                label = model.outcomes[0],
                modifier = Modifier.weight(DEFECT_BUTTON_WEIGHT).testTag("step.defect"),
                contentColor = Tokens.err,
                borderOutside = true,
                onClick = { onAction(CheckStepAction.Open(model.defectTarget)) },
            )
            FilledButton(
                label = model.outcomes[1],
                size = ButtonSize.Regular,
                container = FilledContainer.Primary,
                modifier = Modifier.weight(OK_BUTTON_WEIGHT).testTag("step.ok"),
            )
        }
    }
}

/** AndElVerteiler:101-102 (D7): Ausgelöst is the green `okBg`/`okFg` chip, Jetzt testen the `openBg`/`text` one. */
private fun FiRow.toRow(): StatusRow = StatusRow(
    title = name,
    subtitle = sub,
    status = statusLabel,
    statusColors = when (tone) {
        FiTone.Ok -> StatusChipColors.Ok()
        FiTone.Open -> StatusChipColors.Pending
    },
)
