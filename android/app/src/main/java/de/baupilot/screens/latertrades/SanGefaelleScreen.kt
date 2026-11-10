package de.baupilot.screens.latertrades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import de.baupilot.app.SectionStatus
import de.baupilot.app.SlopeCheckModel
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.InfoCallout
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.OutlinedIconButton
import de.baupilot.designsystem.components.SlopeResult
import de.baupilot.designsystem.components.SocketTile
import de.baupilot.designsystem.components.SocketTileStyle
import de.baupilot.designsystem.components.StepperTags
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.ValueStepper
import de.baupilot.inspection.SlopeViewModel
import de.baupilot.screens.ScreenCopy

/** Actions of the drain slope screen; the shell turns them into navigation. */
sealed interface SlopeAction {
    data class Open(val route: AppRoute) : SlopeAction
}

/**
 * Width split of "Mangel" and "Speichern" (AndSanGefaelle.dc.html:71-74), measured on the reference. Both are
 * `flex: 1`, but the `<a>` is content-box (its 2 border adds to its width) and the `<button>` keeps the browser's
 * padding 6 + 6. With 326 - 48 - 2 × 8 = 262 shared, each gets 131 ± 5: 126 for "Mangel", 136 for "Speichern".
 */
private const val DEFECT_BUTTON_WEIGHT = 126f
private const val SAVE_BUTTON_WEIGHT = 136f

/**
 * Sanitaer Gefaelle check (AndSanGefaelle.dc.html:17-82): the four section tiles, the calculator card and the note.
 * The length and the drop live here (D3, D12) and are never written: the steppers change the percentage, the verdict
 * and the schema at once. Only the back arrow and "Mangel" act; the camera, "Speichern" and the section tiles have no
 * action. [onClose] is the back arrow.
 */
@Composable
fun SanGefaelleScreen(
    model: SlopeCheckModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (SlopeAction) -> Unit = {},
) {
    var slope by rememberSaveable(stateSaver = SlopeViewModel.saver(model.initial)) { mutableStateOf(model.initial) }
    val route = model.route
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                subtitle = model.subtitle,
                padding = BarPadding.NavCounter,
                navigation = Nav.Back(onClick = onClose),
                titleStyle = TitleStyle.Detail,
                trailingText = model.counter,
            )
        },
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 0.dp),
        spacing = 14.dp,
        modifier = modifier.testTag("screen.inspection.${route.tradeId}.${route.check}.${route.item}"),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            model.sections.forEach { section ->
                SocketTile(
                    id = section.id,
                    label = section.label,
                    style = section.status.style(),
                    contentDescription = "${section.id} ${section.label}".trim(),
                    modifier = Modifier.weight(1f).testTag("slope.section.${section.id}"),
                )
            }
        }
        BaupilotCard {
            CalculatorBody(model, slope, { slope = it }, onAction)
        }
        InfoCallout(
            text = model.warning,
            container = null,
            content = Tokens.muted,
            contentPadding = PaddingValues(4.dp),
            textSize = 13.sp,
            lineHeight = 18.sp,
        )
    }
}

/** AndSanGefaelle:33-76: the instruction, the two steppers (gap 10), the live result and the three buttons (gap 14). */
@Composable
private fun CalculatorBody(
    model: SlopeCheckModel,
    slope: SlopeViewModel,
    onChange: (SlopeViewModel) -> Unit,
    onAction: (SlopeAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BaupilotText(text = model.hint, size = 14.sp, lineHeight = 20.sp, color = Tokens.muted)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LabelledStepper(
                label = model.lengthLabel,
                value = slope.lengthText,
                onDecrement = { onChange(slope.decrementLength()) },
                onIncrement = { onChange(slope.incrementLength()) },
                tags = StepperTags("slope.length.decrement", "slope.length.value", "slope.length.increment"),
                modifier = Modifier.weight(1f),
            )
            LabelledStepper(
                label = model.dropLabel,
                value = slope.dropText,
                onDecrement = { onChange(slope.decrementDrop()) },
                onIncrement = { onChange(slope.incrementDrop()) },
                tags = StepperTags("slope.drop.decrement", "slope.drop.value", "slope.drop.increment"),
                modifier = Modifier.weight(1f),
            )
        }
        SlopeResult(
            percent = slope.percentText,
            verdict = slope.verdictText,
            sufficient = slope.isSufficient,
            schemaY2 = slope.schemaY2,
            levelLabel = ScreenCopy.SanGefaelle.SCHEMA_LEVEL,
            deltaLabel = ScreenCopy.SanGefaelle.SCHEMA_DELTA,
            percentTag = "slope.percent",
            verdictTag = "slope.verdict",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            OutlinedIconButton(label = model.photoLabel, modifier = Modifier.testTag("slope.photo"))
            OutlinedButton(
                label = model.defectLabel,
                modifier = Modifier.weight(DEFECT_BUTTON_WEIGHT).testTag("slope.defect"),
                contentColor = Tokens.err,
                borderColor = Tokens.outline,
                borderOutside = true,
                onClick = { onAction(SlopeAction.Open(model.defectTarget)) },
            )
            FilledButton(
                label = model.saveLabel,
                size = ButtonSize.Regular,
                container = FilledContainer.Primary,
                modifier = Modifier.weight(SAVE_BUTTON_WEIGHT).testTag("slope.save"),
            )
        }
    }
}

/** A 12 / 500 `muted` label over a stepper that fills the column (gap 6). */
@Composable
private fun LabelledStepper(
    label: String,
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    tags: StepperTags,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BaupilotText(text = label, size = 12.sp, weight = 500, color = Tokens.muted)
        ValueStepper(
            label = label,
            value = value,
            onDecrement = onDecrement,
            onIncrement = onIncrement,
            tags = tags,
            fillsWidth = true,
        )
    }
}

/** The section status map `S` (AndSanGefaelle:100-107): the current section has a 2 `primary` border inside. */
private fun SectionStatus.style(): SocketTileStyle = when (this) {
    SectionStatus.Ok -> SocketTileStyle(Tokens.okBg, Tokens.okFg)
    SectionStatus.Defect -> SocketTileStyle(Tokens.errBg, Tokens.errFg)
    SectionStatus.Now -> SocketTileStyle(Tokens.primaryContainer, Tokens.onPrimaryContainer, Tokens.primary)
    SectionStatus.Open -> SocketTileStyle(Tokens.openBg, Tokens.muted)
}
