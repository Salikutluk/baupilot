package de.baupilot.screens.defects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.DefectCard
import de.baupilot.app.DefectUrgency
import de.baupilot.app.DefectsBoardModel
import de.baupilot.app.InspectionsView
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.Action
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTabRow
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.StatusChip
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.designsystem.components.StatusChipSize
import de.baupilot.designsystem.components.TabLabelAlignment
import de.baupilot.designsystem.components.TabScreenScaffold
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.Trailing

/** Actions the Mängel view raises; the shell turns them into navigation. */
sealed interface DefectsAction {
    data class SelectView(val view: InspectionsView) : DefectsAction
}

/** Chip colours of a card's due label (AndAbnMaengel.dc.html:78-80: late `errBg`, soon `warnBg`, later `surfaceContainerHigh`). */
private fun DefectUrgency.chipColors(): StatusChipColors = when (this) {
    DefectUrgency.Late -> StatusChipColors.Error
    DefectUrgency.Soon -> StatusChipColors.Warn(Tokens.warnFg.abnMaengel)
    DefectUrgency.Later -> StatusChipColors.Neutral
}

/**
 * Abnahmen, Offene Mängel view (Nr. 15, AndAbnMaengel.dc.html): the Gewerke header with a 48 high "Vorlagen"
 * placeholder link, the two-tab view switch (indicator 96, Mängel selected), one outlined card per defect
 * (the late one on `errCard` with its two placeholder buttons) and the "Behobene Mängel" row. The tab row
 * sits in the top-bar slot because the 12 dp content padding starts below it.
 */
@Composable
fun DefectsScreen(
    model: DefectsBoardModel,
    modifier: Modifier = Modifier,
    onAction: (DefectsAction) -> Unit = {},
) {
    val views = listOf(InspectionsView.Trades, InspectionsView.Defects)
    TabScreenScaffold(
        topBar = {
            Column {
                BaupilotTopAppBar(
                    title = model.title,
                    padding = BarPadding.TopLevelAction,
                    titleStyle = TitleStyle.Large,
                    actions = listOf(Action.Text(model.templatesLabel, 48)),
                )
                BaupilotTabRow(
                    tabs = model.views,
                    selectedIndex = views.indexOf(InspectionsView.Defects),
                    contentDescription = model.viewsLabel,
                    tabTags = views.map { "inspections.segment.${it.name.lowercase()}" },
                    indicatorWidth = 96.dp,
                    unselectedAlignment = TabLabelAlignment.Center,
                    onSelect = { onAction(DefectsAction.SelectView(views[it])) },
                )
            }
        },
        spacing = 0.dp,
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().testTag("inspections.view.defects"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            model.cards.forEach { card -> DefectCardView(card, model) }
            BaupilotListItem(
                title = model.resolvedLabel,
                leading = Leading.Icon(BaupilotIcon.CheckW26, Tokens.okFg),
                trailing = Trailing.Text(model.resolvedCount, 14.sp, Tokens.muted, weight = 400),
                minHeight = ListItemHeight.H56,
                horizontalPadding = 4.dp,
            )
        }
    }
}

@Composable
private fun DefectCardView(card: DefectCard, model: DefectsBoardModel) {
    val late = card.urgency == DefectUrgency.Late
    BaupilotCard(
        modifier = Modifier.fillMaxWidth().testTag("defect.${card.id}"),
        container = if (late) Tokens.errCard else null,
        radius = 12.dp,
        border = if (late) Tokens.err else Tokens.outlineVariant,
        padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        borderOutside = true,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BaupilotText(
                    text = card.trade,
                    size = 12.sp,
                    weight = 500,
                    color = Tokens.muted,
                    letterSpacing = 0.4.sp,
                )
                StatusChip(card.dueLabel, card.urgency.chipColors(), StatusChipSize.Auto)
            }
            BaupilotText(text = card.title, size = 16.sp, color = Tokens.text)
            BaupilotText(text = card.meta, size = 14.sp, color = Tokens.muted)
            if (card.showsActions) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    OutlinedButton(model.remindLabel, size = ButtonSize.Compact)
                    FilledButton(model.resolveLabel, ButtonSize.Compact, FilledContainer.Primary)
                }
            }
        }
    }
}
