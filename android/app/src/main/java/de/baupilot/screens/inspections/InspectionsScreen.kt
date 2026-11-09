package de.baupilot.screens.inspections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.AppRoute
import de.baupilot.app.InspectionStatus
import de.baupilot.app.InspectionTradeRow
import de.baupilot.app.InspectionsModel
import de.baupilot.app.InspectionsView
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.Action
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTabRow
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.TabLabelAlignment
import de.baupilot.designsystem.components.TabScreenScaffold
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.Trailing

/** Actions the Gewerke view raises; the shell turns them into navigation. */
sealed interface InspectionsAction {
    data class SelectView(val view: InspectionsView) : InspectionsAction
    data class Open(val route: AppRoute) : InspectionsAction
}

/** Avatar icon, avatar colours and status text colour of an overview row (AndAbnGewerke.dc.html:60-63). */
private class StatusStyle(
    val icon: BaupilotIcon,
    val avatarBackground: ColorToken,
    val avatarForeground: ColorToken,
    val text: ColorToken,
)

private fun InspectionStatus.style(): StatusStyle = when (this) {
    InspectionStatus.Accepted -> StatusStyle(BaupilotIcon.CheckW22, Tokens.okBg, Tokens.okFg, Tokens.okFg)
    InspectionStatus.Reserved -> StatusStyle(
        BaupilotIcon.AlertCircleW22,
        Tokens.warnBg,
        Tokens.warnFg.abnGewerke,
        Tokens.warnFg.abnGewerke,
    )
    InspectionStatus.Today -> StatusStyle(BaupilotIcon.ClipboardW22, Tokens.nowBg, Tokens.nowFg, Tokens.primary)
    InspectionStatus.Planned -> StatusStyle(BaupilotIcon.CalendarW22, Tokens.planBg, Tokens.muted, Tokens.muted)
}

/**
 * Abnahmen, Gewerke view (Nr. 10, AndAbnGewerke.dc.html): large title with the "Vorlagen" placeholder link,
 * the two-tab view switch (indicator 64, unselected label centred) and one 60 high row per trade, the
 * `today` row on `hi`. The tab row sits in the top-bar slot because the 8 dp content padding starts below it.
 */
@Composable
fun InspectionsScreen(
    model: InspectionsModel,
    modifier: Modifier = Modifier,
    onAction: (InspectionsAction) -> Unit = {},
) {
    val views = listOf(InspectionsView.Trades, InspectionsView.Defects)
    TabScreenScaffold(
        topBar = {
            Column {
                BaupilotTopAppBar(
                    title = model.title,
                    padding = BarPadding.TopLevelAction,
                    titleStyle = TitleStyle.Large,
                    actions = listOf(Action.Text(model.templatesLabel, 40)),
                )
                BaupilotTabRow(
                    tabs = model.views,
                    selectedIndex = views.indexOf(InspectionsView.Trades),
                    contentDescription = model.viewsLabel,
                    tabTags = views.map { "inspections.segment.${it.name.lowercase()}" },
                    indicatorWidth = 64.dp,
                    unselectedAlignment = TabLabelAlignment.Center,
                    onSelect = { onAction(InspectionsAction.SelectView(views[it])) },
                )
            }
        },
        spacing = 0.dp,
        modifier = modifier,
        contentPadding = PaddingValues(top = 8.dp),
    ) {
        Column(Modifier.testTag("inspections.view.trades")) {
            model.rows.forEach { row -> TradeRow(row, onAction) }
        }
    }
}

@Composable
private fun TradeRow(row: InspectionTradeRow, onAction: (InspectionsAction) -> Unit) {
    val style = row.status.style()
    val target = row.target
    BaupilotListItem(
        title = row.name,
        subtitle = row.subtitle,
        modifier = Modifier.testTag("inspections.trade.${row.tradeId}"),
        leading = Leading.Avatar.icon(style.icon, style.avatarBackground, style.avatarForeground),
        trailing = Trailing.Text(row.statusLabel, 12.sp, style.text),
        minHeight = ListItemHeight.H60,
        background = if (row.status == InspectionStatus.Today) Tokens.hi else null,
        onClick = target?.let { route -> { onAction(InspectionsAction.Open(route)) } },
    )
}
