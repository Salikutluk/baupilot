package de.baupilot.screens.latertrades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.PaintCheckModel
import de.baupilot.app.PaintColor
import de.baupilot.app.WallState
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
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
import de.baupilot.designsystem.components.ObjectChip
import de.baupilot.designsystem.components.ObjectChipState
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.OutlinedIconButton
import de.baupilot.designsystem.components.RakingLightSketch
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.screens.earlytrades.CheckStepAction

/**
 * Width split of "Mangel" and "In Ordnung" (AndMaRaum:58-60), measured on the reference like RohbauMasse: with the 48
 * photo button and two gaps of 8 the buttons share 326 - 48 - 2 x 8 = 262 (131 each); the `<a>` has no border and the
 * `<button>` adds 1 + 1 border and 6 + 6 browser padding, so 126 for "Mangel" and 136 for "In Ordnung".
 */
private const val DEFECT_BUTTON_WEIGHT = 126f
private const val OK_BUTTON_WEIGHT = 136f

/**
 * Maler Raumprüfung Wohnen (AndMaRaum.dc.html:17-72): close bar with the counter, the fixed 30 % progress bar and three
 * compact steppers. The done row ends in the colour swatch, the current card holds the raking-light sketch, the
 * instruction, the wall chips and the three buttons. "Mangel" opens the defect form (D5); the wall chips, the camera
 * and "In Ordnung" have no action (D12). [onClose] is the close control.
 */
@Composable
fun MaRaumScreen(
    model: PaintCheckModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (CheckStepAction) -> Unit = {},
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
        modifier = modifier.testTag("screen.inspection.${route.tradeId}.room.${route.roomId}"),
    ) {
        BaupilotLinearProgress(progress = model.progress, modifier = Modifier.padding(horizontal = 16.dp))
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BaupilotVerticalStepper(
                steps = model.done.mapIndexed { index, label ->
                    Step(
                        number = index + 1,
                        title = label,
                        state = StepState.Done,
                        trailing = { PaintSwatch(model.doneSwatch.token()) },
                    )
                },
                variant = StepperVariant.Compact,
            )
            BaupilotVerticalStepper(
                steps = listOf(
                    Step(
                        number = model.current.n,
                        title = model.current.title,
                        state = StepState.Current,
                        body = { CurrentBody(model, onAction) },
                    ),
                ),
                variant = StepperVariant.Compact,
            )
            BaupilotVerticalStepper(
                steps = model.next.mapIndexed { index, row ->
                    // `n` is a JSON string; the position after the current step is the fallback for a non-numeric entry.
                    Step(
                        number = row.n.toIntOrNull() ?: (model.current.n + 1 + index),
                        title = row.label,
                        state = StepState.Open,
                    )
                },
                variant = StepperVariant.Compact,
                openMinHeight = 40.dp,
            )
        }
    }
}

/** 20 round swatch with a 1 `outlineVariant` border, 22 outside (AndMaRaum:30). */
@Composable
private fun PaintSwatch(color: ColorToken) {
    Box(Modifier.size(22.dp).background(color.color(), CircleShape).border(1.dp, Tokens.outlineVariant.color(), CircleShape))
}

private fun PaintColor.token(): ColorToken = when (this) {
    PaintColor.White -> Tokens.paintWhite
    PaintColor.Sand -> Tokens.paintSand
    PaintColor.Sage -> Tokens.paintSage
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CurrentBody(model: PaintCheckModel, onAction: (CheckStepAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RakingLightSketch()
        BaupilotText(text = model.current.text, size = 14.sp, lineHeight = 20.sp, color = Tokens.muted)
        FlowRow(
            modifier = Modifier.semantics { contentDescription = model.wallsLabel },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            model.walls.forEachIndexed { index, wall ->
                ObjectChip(
                    label = wall.name,
                    state = when (wall.state) {
                        WallState.Done -> ObjectChipState.Done
                        WallState.Now -> ObjectChipState.Now
                        WallState.Open -> ObjectChipState.Open
                    },
                    modifier = Modifier.testTag("paint.wall.$index"),
                    showDoneIcon = false,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            OutlinedIconButton(label = model.photoLabel, modifier = Modifier.testTag("paint.photo"))
            OutlinedButton(
                label = model.defectLabel,
                modifier = Modifier.weight(DEFECT_BUTTON_WEIGHT).testTag("step.defect"),
                contentColor = Tokens.err,
                borderOutside = true,
                onClick = { onAction(CheckStepAction.Open(model.defectTarget)) },
            )
            FilledButton(
                label = model.okLabel,
                size = ButtonSize.Regular,
                container = FilledContainer.Primary,
                modifier = Modifier.weight(OK_BUTTON_WEIGHT).testTag("step.ok"),
            )
        }
    }
}
