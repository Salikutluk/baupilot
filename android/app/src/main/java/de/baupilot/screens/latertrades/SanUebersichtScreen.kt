package de.baupilot.screens.latertrades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.AppRoute
import de.baupilot.app.OverviewHeader
import de.baupilot.app.OverviewLeading
import de.baupilot.app.OverviewTone
import de.baupilot.app.PhaseKind
import de.baupilot.app.TradeOverviewModel
import de.baupilot.app.TradeOverviewRow
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.PhaseCard
import de.baupilot.designsystem.components.PhaseCardState
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.Trailing

/** Actions of the four later-trade overviews; the shell turns them into navigation. */
sealed interface OverviewAction {
    data class Open(val route: AppRoute) : OverviewAction
}

/** Avatar colours and status text colour of an overview row (the status map `S`, AndSanUebersicht.dc.html:66). */
private class ToneStyle(val avatarBackground: ColorToken, val avatarForeground: ColorToken, val text: ColorToken)

private fun OverviewTone.style(): ToneStyle = when (this) {
    OverviewTone.Ok -> ToneStyle(Tokens.okBg, Tokens.okFg, Tokens.okFg)
    OverviewTone.Today, OverviewTone.Running -> ToneStyle(Tokens.primaryContainer, Tokens.onPrimaryContainer, Tokens.primary)
    OverviewTone.Planned, OverviewTone.Open, OverviewTone.Required -> ToneStyle(Tokens.planBg, Tokens.muted, Tokens.muted)
    OverviewTone.Defect -> ToneStyle(Tokens.errBg, Tokens.errFg, Tokens.err)
}

/**
 * Sanitaer overview (Abnahme Sanitär und Heizung, AndSanUebersicht.dc.html): the two phase cards (no action), then per
 * group a 14 / 500 `primary` header and 60 high avatar rows with the status as trailing text. A row with a target
 * opens it; a row without one has no click handler (M3 D5, M6 D9).
 */
@Composable
fun SanUebersichtScreen(
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
                            PhaseCardState.Active(kind.fraction, Tokens.track.sanUebersicht),
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
    }
}

/**
 * The status text of a row (13 / 500), one pixel lower than the row centres it: the reference (Chrome) puts the
 * 15 px line box of a 60 high row's centre on the next device pixel (69 px at 3x), Compose rounds the half pixel (68 px).
 */
@Composable
private fun StatusText(text: String, color: ColorToken) {
    BaupilotText(text = text, size = 13.sp, weight = 500, color = color, modifier = Modifier.offset { IntOffset(0, 1) })
}

@Composable
private fun OverviewRow(row: TradeOverviewRow, modifier: Modifier, onAction: (OverviewAction) -> Unit) {
    val style = row.tone.style()
    val target = row.target
    val icon = (row.leading as? OverviewLeading.Icon)?.icon
    BaupilotListItem(
        title = row.name,
        subtitle = row.sub,
        modifier = modifier,
        leading = icon?.let { Leading.Avatar.icon(it, style.avatarBackground, style.avatarForeground) },
        trailing = Trailing.Switch { StatusText(row.statusLabel, style.text) },
        minHeight = ListItemHeight.H60,
        onClick = target?.let { route -> { onAction(OverviewAction.Open(route)) } },
    )
}
