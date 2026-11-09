package de.baupilot.screens.docviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.AppRoute
import de.baupilot.app.DocViewerModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.wholeDpCentered
import de.baupilot.designsystem.components.Action
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.BottomActionBar
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.DocumentPreview
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.PreviewLine
import de.baupilot.designsystem.components.TagChip
import de.baupilot.designsystem.components.TagChipStyle
import de.baupilot.designsystem.components.TitleStyle

/** Actions the viewer raises; the shell turns them into navigation. */
sealed interface DocViewerAction {
    data class OpenCompare(val tradeId: String) : DocViewerAction
}

/**
 * Dokument ansehen (AndDokViewer.dc.html): compact back bar with the file line as subtitle and a share action,
 * the paper preview on the well, the trade and type tags with the amount, the change row and the pinned compare
 * button. The compare button follows [DocViewerModel.compareTarget] and is inert without one; share and change
 * have no action.
 */
@Composable
fun DocViewerScreen(
    model: DocViewerModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (DocViewerAction) -> Unit = {},
) {
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                padding = BarPadding.Nav,
                navigation = Nav.Back(onClick = onBack),
                subtitle = model.subtitle,
                titleStyle = TitleStyle.Compact,
                actions = listOf(Action.Icon(BaupilotIcon.ShareNetwork, model.shareLabel, null)),
            )
        },
        contentPadding = PaddingValues(0.dp),
        spacing = 0.dp,
        modifier = modifier.testTag("screen.document.${model.id}"),
        bottomBar = {
            BottomActionBar {
                FilledButton(
                    label = model.compareLabel,
                    size = ButtonSize.Regular,
                    container = FilledContainer.Primary,
                    modifier = Modifier.weight(1f).testTag("document.compare"),
                    onClick = (model.compareTarget as? AppRoute.Compare)?.let { target ->
                        { onAction(DocViewerAction.OpenCompare(target.tradeId)) }
                    },
                )
            }
        },
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .background(Tokens.well.color())
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            DocumentPreview(
                issuerName = model.issuerName,
                issuerAddress = model.issuerAddress,
                number = model.number,
                reference = model.reference,
                lines = model.lines.map { PreviewLine(it.pos, it.text, it.sum) },
                totalLabel = model.totalLabel,
                total = model.total,
                contentDescription = model.pageLabel,
            )
        }
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TagChip(model.facts[0].value, TagChipStyle.Filled)
                TagChip(model.facts[1].value, TagChipStyle.Outlined)
                Spacer(Modifier.weight(1f))
                BaupilotText(
                    text = model.facts[2].value, size = 16.sp, weight = 500, color = Tokens.text,
                    modifier = wholeDpCentered(),
                )
            }
            BaupilotListItem(
                title = model.changeLabel,
                leading = Leading.Icon(BaupilotIcon.Edit),
                minHeight = ListItemHeight.H56,
                horizontalPadding = 0.dp,
            )
        }
    }
}
