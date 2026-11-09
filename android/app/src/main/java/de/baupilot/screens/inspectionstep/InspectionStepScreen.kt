package de.baupilot.screens.inspectionstep

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
import androidx.compose.ui.unit.sp
import de.baupilot.app.AppRoute
import de.baupilot.app.StepModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotLinearProgress
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.BaupilotVerticalStepper
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.PhotoTile
import de.baupilot.designsystem.components.PhotoTileKind
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.WindowLevelSketch
import de.baupilot.screens.ScreenCopy

/** Actions the Prüfschritt screen raises; the shell turns them into navigation. */
sealed interface StepAction {
    data class Open(val route: AppRoute) : StepAction
}

/**
 * Prüfschritt F8 (Nr. 12, AndAbnPruefung.dc.html): close bar with the counter, the 42 % progress bar and the
 * rail stepper. The current step carries the text, the photo grid (guide, photo with tick, add) and the two
 * buttons; "Mangel" opens the defect form, "In Ordnung" has no action. The grid is a row of weighted boxes
 * with canvases, because the stepper rows are measured with an intrinsic height and cannot hold a
 * subcomposing layout (R5).
 */
@Composable
fun InspectionStepScreen(
    model: StepModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (StepAction) -> Unit = {},
) {
    val route = model.route
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                subtitle = model.subtitle,
                padding = BarPadding.NavCounter,
                navigation = Nav.Close(ScreenCopy.AbnPruefung.CLOSE, onClose),
                titleStyle = TitleStyle.Detail,
                trailingText = model.counter,
            )
        },
        contentPadding = PaddingValues(0.dp),
        spacing = 0.dp,
        modifier = modifier.testTag("screen.inspection.${route.tradeId}.${route.elementId}.step.${route.step}"),
    ) {
        BaupilotLinearProgress(progress = model.progress, modifier = Modifier.padding(horizontal = 16.dp))
        BaupilotVerticalStepper(
            steps = steps(model, onAction),
            variant = StepperVariant.Rail,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        )
    }
}

/**
 * Width split of the two buttons (AndAbnPruefung.dc.html:62-63), measured on the reference: both are `flex: 1`, but
 * the `<a>` is content-box (its 2 border adds to its width) and the `<button>` keeps the browser's padding 6 + 6.
 * With 318 - 8 = 310 shared, each gets 148 plus its own extra: 150 for "Mangel", 160 for "In Ordnung".
 */
private const val DEFECT_BUTTON_WEIGHT = 150f
private const val OK_BUTTON_WEIGHT = 160f

private fun steps(model: StepModel, onAction: (StepAction) -> Unit): List<Step> {
    val current = model.current
    val done = model.done.mapIndexed { index, row ->
        Step(number = index + 1, title = row.label, state = StepState.Done, meta = row.meta)
    }
    val upcoming = model.next.mapIndexed { index, row ->
        // `n` is a JSON string; the position after the current step is the fallback for a non-numeric entry.
        Step(number = row.n.toIntOrNull() ?: (current.n + 1 + index), title = row.label, state = StepState.Open)
    }
    val now = Step(
        number = current.n,
        title = current.title,
        state = StepState.Current,
        body = { CurrentBody(model, onAction) },
    )
    return done + now + upcoming
}

@Composable
private fun CurrentBody(model: StepModel, onAction: (StepAction) -> Unit) {
    val current = model.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BaupilotText(text = current.text, size = 14.sp, lineHeight = 20.sp, color = Tokens.muted)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PhotoTile(
                kind = PhotoTileKind.Guide(model.guideLabel) { WindowLevelSketch() },
                height = 80.dp,
                modifier = Modifier.weight(1f),
            )
            PhotoTile(
                kind = PhotoTileKind.Scene(current.photoCaption, checked = true),
                height = 80.dp,
                modifier = Modifier.weight(1f),
            )
            PhotoTile(
                kind = PhotoTileKind.Add(current.addPhotoLabel),
                height = 80.dp,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            OutlinedButton(
                label = model.defectLabel,
                modifier = Modifier.weight(DEFECT_BUTTON_WEIGHT).testTag("step.defect"),
                contentColor = Tokens.err,
                icon = BaupilotIcon.AlertTriangleW22,
                borderOutside = true,
                onClick = { onAction(StepAction.Open(model.defectTarget)) },
            )
            FilledButton(
                label = model.okLabel,
                size = ButtonSize.Regular,
                container = FilledContainer.Primary,
                modifier = Modifier.weight(OK_BUTTON_WEIGHT),
                icon = BaupilotIcon.CheckW30,
            )
        }
    }
}
