package de.baupilot.screens.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.AppRoute
import de.baupilot.app.ElementsModel
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.Nav

/** Actions the Elemente screen raises; the shell turns them into navigation. */
sealed interface ElementsAction {
    data class Open(val route: AppRoute) : ElementsAction
}

/**
 * Fenster – Elemente (Nr. 11): stub with the back bar and the identified action elements (element rows and the
 * next button); the pixel-exact content arrives with the screen task.
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
                padding = BarPadding.Nav,
                navigation = Nav.Back(onClick = onBack),
            )
        },
        contentPadding = PaddingValues(0.dp),
        spacing = 0.dp,
        modifier = modifier.testTag("screen.inspection.${model.tradeId}"),
    ) {
        model.floors.flatMap { it.items }.forEach { row ->
            val target = row.target
            BaupilotText(
                text = row.label,
                size = 16.sp,
                color = Tokens.text,
                modifier = Modifier
                    .testTag("inspection.element.${row.id}")
                    .then(if (target != null) Modifier.clickable { onAction(ElementsAction.Open(target)) } else Modifier),
            )
        }
        BaupilotText(
            text = model.nextLabel,
            size = 16.sp,
            color = Tokens.text,
            modifier = Modifier
                .testTag("inspection.next")
                .clickable { onAction(ElementsAction.Open(model.nextTarget)) },
        )
    }
}
