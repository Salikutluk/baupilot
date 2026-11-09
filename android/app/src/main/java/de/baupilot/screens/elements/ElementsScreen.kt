package de.baupilot.screens.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.AppRoute
import de.baupilot.app.ElementRow
import de.baupilot.app.ElementStatus
import de.baupilot.app.ElementsModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.Action
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.ExtendedFab
import de.baupilot.designsystem.components.FabIcon
import de.baupilot.designsystem.components.FilterChip
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.SectionLink
import de.baupilot.designsystem.components.SegmentedProgress
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.Trailing

/** Actions the Elemente screen raises; the shell turns them into navigation. */
sealed interface ElementsAction {
    data class Open(val route: AppRoute) : ElementsAction
}

/** Avatar colours and status text colour of an element row (AndAbnElemente.dc.html:107). */
private class ElementStyle(val avatarBackground: ColorToken, val avatarForeground: ColorToken, val text: ColorToken)

private fun ElementStatus.style(): ElementStyle = when (this) {
    ElementStatus.Ok -> ElementStyle(Tokens.okBg, Tokens.okFg, Tokens.okFg)
    ElementStatus.Defect -> ElementStyle(Tokens.errBg, Tokens.errFg, Tokens.err)
    ElementStatus.Now -> ElementStyle(Tokens.primaryContainer, Tokens.onPrimaryContainer, Tokens.primary)
    ElementStatus.Open -> ElementStyle(Tokens.planBg, Tokens.muted, Tokens.muted)
}

/** Segment colour of a progress entry (AndAbnElemente.dc.html:110): ok, defect, otherwise the track. */
private fun ElementStatus.segment(): ColorToken = when (this) {
    ElementStatus.Ok -> Tokens.ok
    ElementStatus.Defect -> Tokens.err
    ElementStatus.Now, ElementStatus.Open -> Tokens.track.abnElemente
}

/**
 * Fenster – Elemente (Nr. 11, AndAbnElemente.dc.html): detail bar with the help placeholder, the summary card
 * (counter, guide, segmented progress), the filter chips (no action) and the floors with their element rows.
 * The extended FAB opens the element that is being inspected.
 */
@Composable
fun ElementsScreen(
    model: ElementsModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (ElementsAction) -> Unit = {},
) {
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                subtitle = model.subtitle,
                padding = BarPadding.Nav,
                navigation = Nav.Back(onClick = onBack),
                titleStyle = TitleStyle.Detail,
                actions = listOf(Action.Icon(BaupilotIcon.HelpCircle, model.helpLabel)),
            )
        },
        contentPadding = PaddingValues(0.dp),
        spacing = 0.dp,
        modifier = modifier.testTag("screen.inspection.${model.tradeId}"),
        fab = {
            ExtendedFab(
                label = model.nextLabel,
                icon = FabIcon.Play,
                modifier = Modifier.testTag("inspection.next"),
                onClick = { onAction(ElementsAction.Open(model.nextTarget)) },
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp)),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BaupilotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BaupilotText(
                            text = model.counter,
                            size = 16.sp,
                            weight = 500,
                            color = Tokens.text,
                            modifier = Modifier.alignByBaseline(),
                        )
                        BaupilotText(
                            text = model.guide,
                            size = 14.sp,
                            color = Tokens.muted,
                            modifier = Modifier.alignByBaseline(),
                        )
                    }
                    SegmentedProgress(
                        segments = model.progress.map { it.segment() },
                        height = 6.dp,
                        contentDescription = model.counter,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                model.chips.forEachIndexed { index, label ->
                    FilterChip(label = label, selected = index == 0, showCheck = index == 0)
                }
            }
        }
        model.floors.forEach { floor ->
            SectionHeader(
                title = floor.title,
                style = SectionHeaderStyle.Compact(SectionLink(floor.moreLabel)),
                modifier = Modifier.padding(PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp)),
            )
            floor.items.forEach { row -> ElementListRow(row, onAction) }
        }
    }
}

@Composable
private fun ElementListRow(row: ElementRow, onAction: (ElementsAction) -> Unit) {
    val style = row.status.style()
    val target = row.target
    BaupilotListItem(
        title = row.room,
        subtitle = row.type,
        modifier = Modifier.testTag("inspection.element.${row.id}"),
        leading = Leading.Avatar.id(row.id, style.avatarBackground, style.avatarForeground),
        trailing = Trailing.Text(row.label, 13.sp, style.text),
        minHeight = ListItemHeight.H60,
        onClick = target?.let { route -> { onAction(ElementsAction.Open(route)) } },
    )
}
