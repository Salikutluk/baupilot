package de.baupilot.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.HomeModel
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.Action
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotLinearProgress
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DocumentTile
import de.baupilot.designsystem.components.ExtendedFab
import de.baupilot.designsystem.components.FabIcon
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.SectionLink
import de.baupilot.designsystem.components.TabScreenScaffold
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.Trailing
import de.baupilot.screens.ScreenCopy

sealed interface HomeAction {
    data object OpenNotifications : HomeAction
    data class OpenTrade(val id: String) : HomeAction
    data object OpenDocumentsTab : HomeAction
}

/**
 * Übersicht (AndHome.dc.html): bell in the bar, phase card, the three trade rows, the 2 x 2 document grid and the
 * "Dokument" FAB. "Alle" and the FAB are placeholders without action. Trade rows open the trade, the bell opens
 * the notifications, the tiles switch to the documents tab.
 */
@Composable
fun HomeScreen(model: HomeModel, onAction: (HomeAction) -> Unit) {
    TabScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = ScreenCopy.Home.TITLE,
                subtitle = model.projectLabel,
                padding = BarPadding.TopLevelAction,
                titleStyle = TitleStyle.LargeTall,
                actions = listOf(
                    Action.Bell(
                        label = model.notificationsAria,
                        badge = model.badge,
                        onClick = { onAction(HomeAction.OpenNotifications) },
                    ),
                ),
            )
        },
        spacing = 16.dp,
        modifier = Modifier.testTag("screen.home"),
        fab = { ExtendedFab(label = ScreenCopy.Home.DOCUMENT_FAB, icon = FabIcon.Plus) },
    ) {
        PhaseCard(model)
        Column {
            SectionHeader(ScreenCopy.Home.NEXT_NEEDED, SectionHeaderStyle.Overview(SectionLink(ScreenCopy.Home.ALL)))
            model.trades.forEach { trade ->
                BaupilotListItem(
                    title = trade.name,
                    subtitle = trade.`when`,
                    leading = Leading.Avatar.initials(trade.initial, Tokens.secondaryContainer, Tokens.onSecondaryContainer),
                    trailing = Trailing.StatusChip(trade.statusLabel, trade.colors),
                    minHeight = ListItemHeight.H64,
                    horizontalPadding = 0.dp,
                    onClick = { onAction(HomeAction.OpenTrade(trade.tradeId)) },
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(ScreenCopy.Home.DOCUMENTS, SectionHeaderStyle.Overview())
            model.docs.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { doc ->
                        Box(Modifier.weight(1f)) {
                            DocumentTile(doc.icon, doc.label, doc.count, onClick = { onAction(HomeAction.OpenDocumentsTab) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhaseCard(model: HomeModel) {
    BaupilotCard(container = Tokens.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BaupilotText(
                    text = ScreenCopy.Home.PHASE_KICKER,
                    size = 14.sp,
                    weight = 500,
                    color = Tokens.onPrimaryContainer,
                )
                BaupilotText(
                    text = model.phaseLabel,
                    size = 14.sp,
                    weight = 500,
                    color = Tokens.onPrimaryContainer,
                )
            }
            BaupilotText(
                text = model.phaseTitle,
                size = 28.sp,
                lineHeight = 36.sp,
                color = Tokens.onPrimaryContainer,
            )
            BaupilotLinearProgress(model.progress, Tokens.track.home)
            BaupilotText(
                text = model.moveInLabel,
                size = 14.sp,
                color = Tokens.onPrimaryContainer,
            )
        }
    }
}
