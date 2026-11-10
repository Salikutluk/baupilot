package de.baupilot.screens.latertrades

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
import de.baupilot.app.TileCheckModel
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
import de.baupilot.designsystem.components.LegendRow
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant
import de.baupilot.designsystem.components.TileGrid
import de.baupilot.designsystem.components.TileItem
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.screens.earlytrades.CheckStepAction

/**
 * Width split of the two buttons (AndFlBad.dc.html:58-59), measured on the reference: both are `flex: 1`, but the `<a>`
 * has no border and the `<button>` adds 1 + 1 border and 6 + 6 browser padding. With 326 - 8 = 318 shared, each gets
 * 159 plus its own extra: 152 for "Als Mangel", 166 for "Keine Hohlstellen".
 */
private const val DEFECT_BUTTON_WEIGHT = 152f
private const val NONE_BUTTON_WEIGHT = 166f

/**
 * Fliesen Raumprüfung Bad (AndFlBad.dc.html:17-70): close bar with the counter, the fixed 42 % progress bar and three
 * compact steppers. The current step is a card with the instruction, the tile grid, the result line and the two
 * buttons. "Als Mangel" opens the defect form (D5); the tiles and "Keine Hohlstellen" have no action (D12).
 * [onClose] is the close control.
 */
@Composable
fun FlBadScreen(
    model: TileCheckModel,
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
                    Step(number = index + 1, title = label, state = StepState.Done)
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

@Composable
private fun CurrentBody(model: TileCheckModel, onAction: (CheckStepAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BaupilotText(text = model.current.text, size = 14.sp, lineHeight = 20.sp, color = Tokens.muted)
        LegendRow(direction = model.wallLabel, mark = model.markHint)
        TileGrid(
            tiles = model.tiles.map { tile ->
                TileItem(
                    id = tile.id,
                    hollow = tile.hollow,
                    label = tile.label,
                    contentDescription = tile.accessibilityLabel,
                    modifier = Modifier.testTag("tiles.tile.${tile.id}"),
                )
            },
            columns = model.columns,
            accessibilityLabel = model.gridAccessibilityLabel,
        )
        BaupilotText(text = model.result, size = 13.sp, weight = 500, color = Tokens.err)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            FilledButton(
                label = model.defectLabel,
                size = ButtonSize.Regular,
                container = FilledContainer.Error,
                modifier = Modifier.weight(DEFECT_BUTTON_WEIGHT).testTag("tiles.defect"),
                onClick = { onAction(CheckStepAction.Open(model.defectTarget)) },
            )
            OutlinedButton(
                label = model.noneLabel,
                modifier = Modifier.weight(NONE_BUTTON_WEIGHT).testTag("tiles.none"),
            )
        }
    }
}
