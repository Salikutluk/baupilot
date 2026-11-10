package de.baupilot.screens.earlytrades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import de.baupilot.app.AppRoute
import de.baupilot.app.MeasureCheckModel
import de.baupilot.app.MeasureDimensionModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.AlertBanner
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.OutlinedIconButton
import de.baupilot.designsystem.components.StatusChip
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.designsystem.components.StatusChipSize
import de.baupilot.designsystem.components.StepperTags
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.Trailing
import de.baupilot.designsystem.components.ValueStepper
import de.baupilot.inspection.MeasureDimension
import de.baupilot.inspection.MeasureViewModel
import de.baupilot.screens.ScreenCopy

/** Actions of the Maße & Lot screen; the shell turns them into navigation. */
sealed interface MeasureAction {
    data class Open(val route: AppRoute) : MeasureAction
}

/**
 * Width split of "Mangel" and "Speichern" (AndRohbauMasse.dc.html:48-49), measured on the reference. Both are
 * `flex: 1`, but the `<a>` is content-box (its 2 border adds to its width) and the `<button>` keeps the browser's
 * padding 6 + 6. With 326 - 48 - 2 × 8 = 262 shared, each gets 131 ± 5: 126 for "Mangel", 136 for "Speichern".
 */
private const val DEFECT_BUTTON_WEIGHT = 126f
private const val SAVE_BUTTON_WEIGHT = 136f

/**
 * Rohbau Maße & Lot (Nr. 18, AndRohbauMasse.dc.html:17-71). The measure value is kept here (D3, D12) and never
 * written (I6): the stepper changes the diff text and the result live. Only "Mangel" and the back arrow act; the
 * camera, "Speichern", the chips and the plumb row have no action.
 */
@Composable
fun RohbauMasseScreen(
    model: MeasureCheckModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (MeasureAction) -> Unit = {},
) {
    var measure by rememberSaveable(stateSaver = MeasureViewModel.saver(model.initial)) {
        mutableStateOf(model.initial)
    }
    val route = model.route
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                subtitle = model.subtitle(measure.tolerance),
                padding = BarPadding.NavCounter,
                navigation = Nav.Back(onClick = onBack),
                titleStyle = TitleStyle.Detail,
                trailingText = model.counter,
            )
        },
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 0.dp),
        spacing = 14.dp,
        modifier = modifier.testTag("screen.inspection.${route.tradeId}.${route.check}"),
    ) {
        BaupilotCard {
            MeasureCardBody(model, measure, { measure = it }, onAction)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BaupilotText(text = model.measuredHeading, size = 14.sp, weight = 500, color = Tokens.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                model.measured.forEach { StatusChip(it, StatusChipColors.Ok(), StatusChipSize.H32Body) }
            }
        }
        // The row's `border-top` (AndRohbauMasse:60): the divider belongs to the row, no scaffold gap between them.
        Column {
            Box(Modifier.fillMaxWidth().height(1.dp).background(Tokens.divider.color()))
            BaupilotListItem(
                title = model.plumbTitle,
                subtitle = model.plumbSubtitle,
                leading = Leading.Icon(BaupilotIcon.CheckCircleW24, Tokens.ok),
                trailing = Trailing.Text(model.plumbValue, 14.sp, Tokens.okFg, 500),
                minHeight = ListItemHeight.H56,
                horizontalPadding = 0.dp,
                subtitleSize = 13.sp,
            )
        }
    }
}

/** AndRohbauMasse:25-52: the instruction, one row per dimension, the live result and the three buttons (gap 12). */
@Composable
private fun MeasureCardBody(
    model: MeasureCheckModel,
    measure: MeasureViewModel,
    onChange: (MeasureViewModel) -> Unit,
    onAction: (MeasureAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BaupilotText(text = model.instruction, size = 14.sp, lineHeight = 20.sp, color = Tokens.muted)
        model.dimensions.forEach { DimensionRow(it, measure, onChange) }
        val within = measure.isWithinTolerance
        AlertBanner(
            icon = null,
            title = if (within) model.withinLabel else model.outsideLabel,
            subtitle = null,
            modifier = Modifier.testTag("measure.result"),
            background = if (within) Tokens.okBg else Tokens.errBg,
            foreground = if (within) Tokens.okFg else Tokens.errFg,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            OutlinedIconButton(label = model.photoLabel, modifier = Modifier.testTag("measure.photo"))
            OutlinedButton(
                label = model.defectLabel,
                modifier = Modifier.weight(DEFECT_BUTTON_WEIGHT).testTag("measure.defect"),
                contentColor = Tokens.err,
                borderColor = Tokens.outline,
                borderOutside = true,
                onClick = { onAction(MeasureAction.Open(model.defectTarget)) },
            )
            FilledButton(
                label = model.saveLabel,
                size = ButtonSize.Regular,
                container = FilledContainer.Primary,
                modifier = Modifier.weight(SAVE_BUTTON_WEIGHT).testTag("measure.save"),
            )
        }
    }
}

/** Label over "Soll … mm · diff" on the left, the stepper on the right (gap 10). */
@Composable
private fun DimensionRow(
    dimension: MeasureDimensionModel,
    measure: MeasureViewModel,
    onChange: (MeasureViewModel) -> Unit,
) {
    val key = dimension.key
    val name = if (key == MeasureDimension.WIDTH) "width" else "height"
    val target = if (key == MeasureDimension.WIDTH) measure.targetW else measure.targetH
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BaupilotText(text = dimension.label, size = 16.sp, color = Tokens.text)
            Row {
                BaupilotText(
                    text = "${ScreenCopy.RohbauMasse.TARGET_PREFIX} $target ${measure.unit} · ",
                    size = 13.sp,
                    color = Tokens.muted,
                )
                BaupilotText(
                    text = measure.diffText(key),
                    size = 13.sp,
                    weight = 500,
                    color = if (measure.isWithin(key)) Tokens.okFg else Tokens.err,
                    modifier = Modifier.testTag("measure.$name.diff"),
                )
            }
        }
        ValueStepper(
            label = dimension.label,
            value = measure.valueText(key),
            onDecrement = { onChange(measure.decrement(key)) },
            onIncrement = { onChange(measure.increment(key)) },
            tags = StepperTags("measure.$name.decrement", "measure.$name.value", "measure.$name.increment"),
        )
    }
}
