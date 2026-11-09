package de.baupilot.screens.inspections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.AppRoute
import de.baupilot.app.InspectionsModel
import de.baupilot.app.InspectionsView
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.TabScreenScaffold

/** Actions the Gewerke view raises; the shell turns them into navigation. */
sealed interface InspectionsAction {
    data class SelectView(val view: InspectionsView) : InspectionsAction
    data class Open(val route: AppRoute) : InspectionsAction
}

/**
 * Abnahmen, Gewerke view (Nr. 10): stub with the identified action elements (segments and trade rows); the
 * pixel-exact content arrives with the screen task.
 */
@Composable
fun InspectionsScreen(
    model: InspectionsModel,
    modifier: Modifier = Modifier,
    onAction: (InspectionsAction) -> Unit = {},
) {
    TabScreenScaffold(
        topBar = { BaupilotTopAppBar(title = model.title, padding = BarPadding.TitleOnly) },
        spacing = 0.dp,
        modifier = modifier,
    ) {
        Column(Modifier.testTag("inspections.view.trades")) {
            model.views.zip(listOf(InspectionsView.Trades, InspectionsView.Defects)).forEach { (label, view) ->
                BaupilotText(
                    text = label,
                    size = 14.sp,
                    color = Tokens.text,
                    modifier = Modifier
                        .testTag("inspections.segment.${view.name.lowercase()}")
                        .clickable { onAction(InspectionsAction.SelectView(view)) },
                )
            }
            model.rows.forEach { row ->
                val target = row.target
                BaupilotText(
                    text = row.name,
                    size = 16.sp,
                    color = Tokens.text,
                    modifier = Modifier
                        .testTag("inspections.trade.${row.tradeId}")
                        .then(if (target != null) Modifier.clickable { onAction(InspectionsAction.Open(target)) } else Modifier),
                )
            }
        }
    }
}
