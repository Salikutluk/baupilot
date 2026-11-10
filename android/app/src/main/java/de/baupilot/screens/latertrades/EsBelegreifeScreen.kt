package de.baupilot.screens.latertrades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.ReadinessCheckModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.CheckMetaRow
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.HeatingBarChart
import de.baupilot.designsystem.components.InfoCallout
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.StatusChip
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.designsystem.components.StatusChipSize

/**
 * Estrich Belegreife / Aufheizprotokoll (AndEsBelegreife.dc.html:17-70): the heating chart card, the CM measurement
 * card and the lock note, gap 12. "Tag 6 eintragen" has no action (section 6). [onBack] is the back arrow.
 */
@Composable
fun EsBelegreifeScreen(
    model: ReadinessCheckModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(title = model.screenTitle, padding = BarPadding.Nav, navigation = Nav.Back(onClick = onBack))
        },
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 0.dp),
        spacing = 12.dp,
        modifier = modifier.testTag("screen.inspection.${model.route.tradeId}.${model.route.check}"),
    ) {
        HeatingCard(model)
        CmCard(model)
        InfoCallout(
            text = model.cm.note,
            icon = BaupilotIcon.Lock,
            container = Tokens.surfaceContainer,
            content = Tokens.muted,
            verticalAlignment = Alignment.CenterVertically,
        )
    }
}

/** AndEsBelegreife:25-49: the header on one baseline, the text, the chart with its axis and the today row (gap 12). */
@Composable
private fun HeatingCard(model: ReadinessCheckModel) {
    val chart = model.chart
    BaupilotCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BaupilotText(
                    text = model.cardTitle,
                    size = 18.sp,
                    weight = 500,
                    color = Tokens.text,
                    modifier = Modifier.alignByBaseline(),
                )
                BaupilotText(
                    text = model.counter,
                    size = 13.sp,
                    weight = 500,
                    color = Tokens.primary,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            BaupilotText(text = model.text, size = 14.sp, lineHeight = 20.sp, color = Tokens.muted)
            HeatingBarChart(
                bars = chart.bars,
                accessibilityLabel = chart.accessibilityLabel,
                recorded = Tokens.ok,
                today = Tokens.primary,
                planned = Tokens.track.esBelegreife,
                modifier = Modifier.testTag("readiness.chart"),
                topPadding = 0.dp,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BaupilotText(text = chart.start, size = 12.sp, color = Tokens.muted)
                BaupilotText(text = chart.max, size = 12.sp, color = Tokens.muted)
                BaupilotText(text = chart.end, size = 12.sp, color = Tokens.muted)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BaupilotText(text = chart.today, size = 13.sp, color = Tokens.muted)
                    BaupilotText(text = chart.plan, size = 18.sp, weight = 500, color = Tokens.text)
                }
                FilledButton(
                    label = chart.action,
                    size = ButtonSize.Compact,
                    container = FilledContainer.Primary,
                    modifier = Modifier.testTag("readiness.action"),
                    horizontalPadding = 20.dp,
                )
            }
        }
    }
}

/** AndEsBelegreife:51-63: the bordered card (border outside the padding) with the header, the text and the rows (gap 8). */
@Composable
private fun CmCard(model: ReadinessCheckModel) {
    val cm = model.cm
    BaupilotCard(container = null, border = Tokens.outlineVariant, borderOutside = true) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BaupilotText(text = cm.title, size = 18.sp, weight = 500, color = Tokens.text)
                StatusChip(text = cm.tag, colors = StatusChipColors.Error, size = StatusChipSize.H24)
            }
            BaupilotText(text = cm.text, size = 14.sp, lineHeight = 20.sp, color = Tokens.muted)
            cm.rows.forEachIndexed { i, row ->
                CheckMetaRow(row.label, row.meta, row.done, Modifier.testTag("readiness.cm.$i"))
            }
        }
    }
}
