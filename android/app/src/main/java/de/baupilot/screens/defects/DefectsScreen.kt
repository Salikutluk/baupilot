package de.baupilot.screens.defects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.DefectsBoardModel
import de.baupilot.app.InspectionsView
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.TabScreenScaffold

/** Actions the Mängel view raises; the shell turns them into navigation. */
sealed interface DefectsAction {
    data class SelectView(val view: InspectionsView) : DefectsAction
}

/**
 * Abnahmen, Offene Mängel view (Nr. 15): stub with the identified action elements (segments and defect cards);
 * the pixel-exact content arrives with the screen task.
 */
@Composable
fun DefectsScreen(
    model: DefectsBoardModel,
    modifier: Modifier = Modifier,
    onAction: (DefectsAction) -> Unit = {},
) {
    TabScreenScaffold(
        topBar = { BaupilotTopAppBar(title = model.title, padding = BarPadding.TitleOnly) },
        spacing = 0.dp,
        modifier = modifier,
    ) {
        Column(Modifier.testTag("inspections.view.defects")) {
            model.views.zip(listOf(InspectionsView.Trades, InspectionsView.Defects)).forEach { (label, view) ->
                BaupilotText(
                    text = label,
                    size = 14.sp,
                    color = Tokens.text,
                    modifier = Modifier
                        .testTag("inspections.segment.${view.name.lowercase()}")
                        .clickable { onAction(DefectsAction.SelectView(view)) },
                )
            }
            model.cards.forEach { card ->
                BaupilotText(
                    text = card.title,
                    size = 16.sp,
                    color = Tokens.text,
                    modifier = Modifier.testTag("defect.${card.id}"),
                )
            }
        }
    }
}
