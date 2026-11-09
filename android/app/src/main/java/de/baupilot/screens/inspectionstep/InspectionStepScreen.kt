package de.baupilot.screens.inspectionstep

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.AppRoute
import de.baupilot.app.StepModel
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.Nav

/** Actions the Prüfschritt screen raises; the shell turns them into navigation. */
sealed interface StepAction {
    data class Open(val route: AppRoute) : StepAction
}

/**
 * Prüfschritt F8 (Nr. 12): stub with the close bar and the identified "Mangel" action; the pixel-exact content
 * arrives with the screen task.
 */
@Composable
fun InspectionStepScreen(
    model: StepModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (StepAction) -> Unit = {},
) {
    val route = model.route
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                padding = BarPadding.Nav,
                navigation = Nav.Close(onClick = onClose),
            )
        },
        contentPadding = PaddingValues(0.dp),
        spacing = 0.dp,
        modifier = modifier.testTag("screen.inspection.${route.tradeId}.${route.elementId}.step.${route.step}"),
    ) {
        BaupilotText(
            text = model.defectLabel,
            size = 16.sp,
            color = Tokens.text,
            modifier = Modifier
                .testTag("step.defect")
                .clickable { onAction(StepAction.Open(model.defectTarget)) },
        )
    }
}
