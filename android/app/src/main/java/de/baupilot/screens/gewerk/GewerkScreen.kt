package de.baupilot.screens.gewerk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.baupilot.app.AppRoute
import de.baupilot.app.GewerkModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.Action
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotTabRow
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.BottomActionBar
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FactRow
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.OfferItem
import de.baupilot.designsystem.components.TagChip
import de.baupilot.designsystem.components.TagChipStyle
import de.baupilot.designsystem.components.TonalIconButton
import de.baupilot.screens.ScreenCopy

/**
 * Gewerk-Detail (AndGewerk.dc.html): back bar with overflow action, status and phase tags, the fact card, the
 * tab row and the offers, with the attach and compare actions pinned to the bottom. Offers and the compare
 * button follow their route through [onNavigate]; the attach and overflow controls and the tabs have no action.
 */
@Composable
fun GewerkScreen(
    model: GewerkModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigate: (AppRoute) -> Unit = {},
) {
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                padding = BarPadding.Nav,
                navigation = Nav.Back(onClick = onBack),
                actions = listOf(Action.Icon(BaupilotIcon.MoreVertical, model.moreLabel, null)),
            )
        },
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp),
        spacing = 16.dp,
        modifier = modifier.testTag("screen.trade.${model.tradeId}"),
        bottomBar = {
            BottomActionBar {
                TonalIconButton(BaupilotIcon.Paperclip, model.addLabel)
                FilledButton(
                    label = model.compareLabel,
                    size = ButtonSize.Regular,
                    container = FilledContainer.Primary,
                    modifier = Modifier.weight(1f).testTag("trade.compare"),
                    onClick = model.compareTarget?.let { target -> { onNavigate(target) } },
                )
            }
        },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TagChip(model.statusLabel, TagChipStyle.Filled)
            TagChip(model.phaseLabel, TagChipStyle.Outlined)
        }
        BaupilotCard(padding = PaddingValues(0.dp)) {
            model.facts.forEach { fact -> FactRow(fact.icon, fact.label, fact.value) }
        }
        BaupilotTabRow(
            tabs = model.tabs.map { it.label },
            selectedIndex = model.tabs.indexOfFirst { it.selected },
            contentDescription = ScreenCopy.Gewerk.TABS,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            model.offers.forEach { offer ->
                OfferItem(
                    firm = offer.firm,
                    meta = offer.meta,
                    price = offer.price,
                    best = offer.best,
                    bestLabel = model.bestLabel,
                    pending = offer.pending,
                    bestBackground = Tokens.okBg.gewerk,
                    onClick = offer.target?.let { target -> { onNavigate(target) } },
                    modifier = Modifier.testTag("trade.offer.${offer.id}"),
                )
            }
        }
    }
}
