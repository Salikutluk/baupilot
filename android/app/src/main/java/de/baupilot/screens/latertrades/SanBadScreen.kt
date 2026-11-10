package de.baupilot.screens.latertrades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import de.baupilot.app.ObjectCheckModel
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.Nav

/**
 * Sanitaer Bad check (bathroom objects).
 * Stub: renders the root tag and the close control (section 5.5).
 * The body arrives in a later step with the same signature.
 */
@Composable
fun SanBadScreen(
    model: ObjectCheckModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().background(Tokens.bg.color()).testTag(
            "screen.inspection.${model.route.tradeId}.room.${model.route.roomId}",
        ),
    ) {
        BaupilotTopAppBar(title = model.title, padding = BarPadding.Nav, navigation = Nav.Close(onClick = onClose))
    }
}
