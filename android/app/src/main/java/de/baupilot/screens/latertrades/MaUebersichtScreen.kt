package de.baupilot.screens.latertrades

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.OverviewHeader
import de.baupilot.app.OverviewLeading
import de.baupilot.app.OverviewTone
import de.baupilot.app.PaintColor
import de.baupilot.app.TradeOverviewModel
import de.baupilot.app.TradeOverviewRow
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FactGrid
import de.baupilot.designsystem.components.FactItem
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.Trailing

private fun PaintColor.token(): ColorToken = when (this) {
    PaintColor.White -> Tokens.paintWhite
    PaintColor.Sand -> Tokens.paintSand
    PaintColor.Sage -> Tokens.paintSage
}

/** AndMaUebersicht:61: ok `okFg`, running `primary`, open `muted`, defect `err`; the other tones follow their neighbours. */
private fun OverviewTone.statusColor(): ColorToken = when (this) {
    OverviewTone.Ok -> Tokens.okFg
    OverviewTone.Running, OverviewTone.Today -> Tokens.primary
    OverviewTone.Open, OverviewTone.Planned -> Tokens.muted
    OverviewTone.Defect, OverviewTone.Required -> Tokens.err
}

/**
 * Maler overview (AndMaUebersicht.dc.html): the fact card (margin 8 16 0), per group a 14 / 500 `primary` header and
 * 64 high rows with a 40 paint swatch and the status as 13 / 500 text. A row with a target opens it; a row without
 * one has no click handler (M6 section 6).
 */
@Composable
fun MaUebersichtScreen(
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
        if (header is OverviewHeader.Facts) {
            FactGrid(
                items = header.facts.mapIndexed { index, fact ->
                    FactItem(fact.label, fact.value, Modifier.testTag("overview.fact.$index"))
                },
                modifier = Modifier.padding(PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp)),
            )
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
    }
}

@Composable
private fun OverviewRow(row: TradeOverviewRow, modifier: Modifier, onAction: (OverviewAction) -> Unit) {
    val target = row.target
    val leading = (row.leading as? OverviewLeading.Swatch)?.let { Leading.Swatch(it.color.token()) }
    BaupilotListItem(
        title = row.name,
        subtitle = row.sub,
        modifier = modifier,
        leading = leading,
        trailing = Trailing.Text(row.statusLabel, 13.sp, row.tone.statusColor(), 500),
        minHeight = ListItemHeight.H64,
        onClick = target?.let { route -> { onAction(OverviewAction.Open(route)) } },
    )
}
