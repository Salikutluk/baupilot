package de.baupilot.screens.latertrades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.baupilot.app.OverviewHeader
import de.baupilot.app.OverviewTone
import de.baupilot.app.PhaseKind
import de.baupilot.app.TradeOverviewModel
import de.baupilot.app.TradeOverviewRow
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.InfoCallout
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.PhaseCard
import de.baupilot.designsystem.components.PhaseCardState
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.designsystem.components.StatusChipSize
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.Trailing

/**
 * Notice padding 12 / 16 with 4 dp more at the end: Chrome measures "Ohne CM-Messung und Aufheizprotokoll keinen" a
 * hair wider than the 294 dp text box and breaks before "keinen"; Compose measures it 292 dp and keeps it on line one.
 */
private val NoticePadding = PaddingValues(start = 16.dp, top = 12.dp, end = 20.dp, bottom = 12.dp)

private fun OverviewTone.chipColors(): StatusChipColors = when (this) {
    OverviewTone.Ok -> StatusChipColors.Ok()
    OverviewTone.Running, OverviewTone.Today -> StatusChipColors.Running
    OverviewTone.Open, OverviewTone.Planned -> StatusChipColors.Open
    OverviewTone.Defect, OverviewTone.Required -> StatusChipColors.Error
}

/**
 * Estrich overview (AndEsUebersicht.dc.html): a phase card pair (active with the 0.75 bar, planned), per group a
 * 14 / 500 `primary` header and 64 high rows with a 28 high status chip, then the warn notice (margin 8 16 0).
 * A row with a target opens it; a row without one has no click handler (M6 section 6).
 */
@Composable
fun EsUebersichtScreen(
    model: TradeOverviewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (OverviewAction) -> Unit = {},
) {
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                subtitle = model.subtitle,
                padding = BarPadding.Nav,
                navigation = Nav.Back(onClick = onBack),
                titleStyle = TitleStyle.Detail,
            )
        },
        contentPadding = PaddingValues(0.dp),
        spacing = 0.dp,
        modifier = modifier.testTag("screen.inspection.${model.tradeId}"),
    ) {
        val header = model.header
        if (header is OverviewHeader.Phases) {
            Row(
                modifier = Modifier
                    .padding(PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp))
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                header.phases.forEachIndexed { index, phase ->
                    val cardModifier = Modifier.weight(1f).fillMaxHeight().testTag("overview.phase.$index")
                    when (val kind = phase.kind) {
                        is PhaseKind.Done -> PhaseCard(phase.title, kind.status, PhaseCardState.Done, cardModifier)
                        is PhaseKind.Planned -> PhaseCard(phase.title, kind.status, PhaseCardState.Planned, cardModifier)
                        is PhaseKind.Active -> PhaseCard(
                            phase.title,
                            kind.progressLabel,
                            PhaseCardState.Active(kind.fraction, Tokens.track.esUebersicht),
                            cardModifier,
                        )
                    }
                }
            }
        }
        model.groups.forEachIndexed { groupIndex, group ->
            SectionHeader(
                title = group.title,
                style = SectionHeaderStyle.Compact(),
                modifier = Modifier.padding(PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 2.dp)),
            )
            group.rows.forEachIndexed { rowIndex, row ->
                OverviewRow(row, Modifier.testTag("overview.row.$groupIndex.$rowIndex"), onAction)
            }
        }
        model.notice?.let { notice ->
            InfoCallout(
                text = notice,
                modifier = Modifier
                    .padding(PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp))
                    .testTag("overview.notice"),
                icon = BaupilotIcon.AlertTriangleW22,
                container = Tokens.warnBg,
                content = Tokens.warnFg,
                contentPadding = NoticePadding,
            )
        }
    }
}

@Composable
private fun OverviewRow(row: TradeOverviewRow, modifier: Modifier, onAction: (OverviewAction) -> Unit) {
    val target = row.target
    BaupilotListItem(
        title = row.name,
        subtitle = row.sub,
        modifier = modifier,
        trailing = Trailing.StatusChip(row.statusLabel, row.tone.chipColors(), StatusChipSize.H28),
        minHeight = ListItemHeight.H64,
        onClick = target?.let { route -> { onAction(OverviewAction.Open(route)) } },
    )
}
