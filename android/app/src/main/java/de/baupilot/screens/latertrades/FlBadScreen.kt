package de.baupilot.screens.latertrades

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.baupilot.app.TileCheckModel
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.Nav
import de.baupilot.screens.earlytrades.CheckStepAction

/**
 * Fliesen Bad check (tile grid).
 * Stub: renders the root tag and the close control and the identified defect action (section 5.5).
 * The body arrives in a later step with the same signature.
 */
@Composable
fun FlBadScreen(
    model: TileCheckModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (CheckStepAction) -> Unit = {},
) {
    Column(
        modifier.fillMaxSize().background(Tokens.bg.color()).testTag(
            "screen.inspection.${model.route.tradeId}.room.${model.route.roomId}",
        ),
    ) {
        BaupilotTopAppBar(title = model.title, padding = BarPadding.Nav, navigation = Nav.Close(onClick = onClose))
        Box(
            Modifier.height(48.dp).testTag("tiles.defect").clickable { onAction(CheckStepAction.Open(model.defectTarget)) },
        )
    }
}
