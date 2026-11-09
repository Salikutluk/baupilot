package de.baupilot.screens.defectform

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.DefectFormModel
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.Nav

/** Actions the Mangel form raises; the shell turns them into navigation. */
sealed interface DefectFormAction {
    data object Cancel : DefectFormAction
    data class Save(val tradeId: String) : DefectFormAction
}

/**
 * Mangel erfassen (Nr. 13): stub with the modal bar (close) and the identified "Sichern" action; the
 * pixel-exact content and the form controls arrive with the screen task.
 */
@Composable
fun DefectFormScreen(
    model: DefectFormModel,
    modifier: Modifier = Modifier,
    onAction: (DefectFormAction) -> Unit = {},
) {
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                padding = BarPadding.Modal,
                navigation = Nav.Close(label = "Abbrechen", onClick = { onAction(DefectFormAction.Cancel) }),
            )
        },
        contentPadding = PaddingValues(0.dp),
        spacing = 0.dp,
        modifier = modifier.testTag("screen.defect.new"),
    ) {
        // The bar's text action carries no test tag (TopAppBar.kt only gains nav.close), so the stub exposes "Sichern" here.
        BaupilotText(
            text = model.saveLabel,
            size = 14.sp,
            weight = 500,
            color = Tokens.primary,
            modifier = Modifier
                .testTag("defect.save")
                .clickable { onAction(DefectFormAction.Save(model.tradeId)) },
        )
    }
}
