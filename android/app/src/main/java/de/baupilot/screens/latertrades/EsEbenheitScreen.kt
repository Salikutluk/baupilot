package de.baupilot.screens.latertrades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import de.baupilot.app.AppRoute
import de.baupilot.app.FlatnessCheckModel
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.AlertBanner
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.OutlinedIconButton
import de.baupilot.designsystem.components.StatusChip
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.designsystem.components.StatusChipSize
import de.baupilot.designsystem.components.StepperTags
import de.baupilot.designsystem.components.TextLink
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.ValueStepper
import de.baupilot.inspection.FlatnessViewModel
import java.util.Locale

/** Actions of the flatness screen; the shell turns them into navigation. */
sealed interface FlatnessAction {
    data class Open(val route: AppRoute) : FlatnessAction
}

/**
 * Estrich Ebenheit check (AndEsEbenheit.dc.html:17-57): the instruction, the gap stepper, the live result and the
 * save row in a card, then the saved points as chips and the defect link. The gap and the points live here (D4) and
 * are never written. The camera has no action; the defect link opens the defect form (D5). [onClose] is the back arrow.
 */
@Composable
fun EsEbenheitScreen(
    model: FlatnessCheckModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (FlatnessAction) -> Unit = {},
) {
    var flatness by rememberSaveable(stateSaver = FlatnessViewModel.saver(model.initial)) { mutableStateOf(model.initial) }
    val route = model.route
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                subtitle = model.subtitle(flatness.limitText),
                padding = BarPadding.NavCounter,
                navigation = Nav.Back(onClick = onClose),
                titleStyle = TitleStyle.Detail,
                trailingText = model.counter,
            )
        },
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 0.dp),
        spacing = 14.dp,
        modifier = modifier.testTag("screen.inspection.${route.tradeId}.${route.check}"),
    ) {
        BaupilotCard {
            MeasureBody(model, flatness) { flatness = it }
        }
        MeasuredSection(model, flatness, onAction)
    }
}

/** AndEsEbenheit:30-47: the instruction, the labelled stepper (gap 12), the result banner and the save row (gaps 14, 8). */
@Composable
private fun MeasureBody(model: FlatnessCheckModel, flatness: FlatnessViewModel, onChange: (FlatnessViewModel) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BaupilotText(text = model.instruction, size = 14.sp, lineHeight = 20.sp, color = Tokens.muted)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BaupilotText(
                text = "${model.stepperLabelPrefix}${flatness.nextIndex}",
                size = 16.sp,
                color = Tokens.text,
                modifier = Modifier.weight(1f),
            )
            ValueStepper(
                label = model.stepperAccessibilityNoun,
                value = flatness.gapText,
                onDecrement = { onChange(flatness.decrement()) },
                onIncrement = { onChange(flatness.increment()) },
                valueMinWidth = 60.dp,
                tags = StepperTags("flatness.gap.decrement", "flatness.gap.value", "flatness.gap.increment"),
                valueSize = 17.sp,
            )
        }
        AlertBanner(
            icon = null,
            title = flatness.resultText,
            subtitle = null,
            modifier = Modifier.testTag("flatness.result"),
            background = if (flatness.isWithin) Tokens.okBg else Tokens.errBg,
            foreground = if (flatness.isWithin) Tokens.okFg else Tokens.errFg,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedIconButton(label = model.photoLabel, modifier = Modifier.testTag("flatness.photo"))
            FilledButton(
                label = model.saveLabel,
                size = ButtonSize.Regular,
                container = FilledContainer.Primary,
                modifier = Modifier.weight(1f).testTag("flatness.save"),
                onClick = { onChange(flatness.save()) },
            )
        }
    }
}

/** AndEsEbenheit:50-59: the "Gemessen (n)" heading, the point chips and the summary link (gap 8). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MeasuredSection(model: FlatnessCheckModel, flatness: FlatnessViewModel, onAction: (FlatnessAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BaupilotText(
            text = String.format(Locale.ROOT, model.measuredFormat, flatness.points.size),
            size = 14.sp,
            weight = 500,
            color = Tokens.primary,
            modifier = Modifier.testTag("flatness.count"),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            flatness.points.forEachIndexed { i, point ->
                StatusChip(
                    text = flatness.pointText(i),
                    colors = if (point > flatness.limit) StatusChipColors.Error else StatusChipColors.Ok(),
                    size = StatusChipSize.H32Body,
                    modifier = Modifier.testTag("flatness.point.$i"),
                )
            }
        }
        TextLink(
            text = flatness.summaryText,
            color = Tokens.err,
            modifier = Modifier.testTag("flatness.defect"),
            onClick = { onAction(FlatnessAction.Open(model.defectTarget)) },
        )
    }
}
