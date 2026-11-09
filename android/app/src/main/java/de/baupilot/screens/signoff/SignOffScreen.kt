package de.baupilot.screens.signoff

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.NavigationTarget
import de.baupilot.app.SignOffModel
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.Nav

/** Actions the Abnahme screen raises; the shell turns them into navigation. */
sealed interface SignOffAction {
    data class Finish(val target: NavigationTarget) : SignOffAction
}

/**
 * Abnahme & Unterschrift (Nr. 14): stub with the back bar and the identified tiles and "Protokoll abschließen"
 * action; the pixel-exact content and the drawing surface arrive with the screen task.
 */
@Composable
fun SignOffScreen(
    model: SignOffModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (SignOffAction) -> Unit = {},
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
        modifier = modifier.testTag("screen.inspection.${model.inspectionId}.sign"),
    ) {
        BaupilotText(
            text = model.craftsmanLabel,
            size = 16.sp,
            color = Tokens.text,
            modifier = Modifier.testTag("sign.craftsman"),
        )
        BaupilotText(
            text = model.ownerLabel,
            size = 16.sp,
            color = Tokens.text,
            modifier = Modifier.testTag("sign.owner"),
        )
        BaupilotText(
            text = model.finishLabel,
            size = 16.sp,
            color = Tokens.text,
            modifier = Modifier
                .testTag("sign.finish")
                .clickable { onAction(SignOffAction.Finish(model.finishTarget)) },
        )
    }
}
