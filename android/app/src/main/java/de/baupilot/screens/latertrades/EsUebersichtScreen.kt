package de.baupilot.screens.latertrades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.AppRoute
import de.baupilot.app.OverviewHeader
import de.baupilot.app.TradeOverviewModel
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.Nav

/**
 * Estrich overview (screed). Stub: renders the root tag, the back control and the identified phase, fact and row elements (section 5.5);
 * a row without a target has no action. The body arrives in a later step with the same signature.
 */
@Composable
fun EsUebersichtScreen(
    model: TradeOverviewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (OverviewAction) -> Unit = {},
) {
    Column(
        modifier.fillMaxSize().background(Tokens.bg.color()).testTag(
            "screen.inspection.${model.tradeId}",
        ),
    ) {
        BaupilotTopAppBar(title = model.title, padding = BarPadding.Nav, navigation = Nav.Back(onClick = onBack))
        when (val header = model.header) {
            is OverviewHeader.Phases -> header.phases.forEachIndexed { index, phase ->
                BaupilotText(
                    text = phase.title,
                    size = 14.sp,
                    color = Tokens.text,
                    modifier = Modifier.testTag("overview.phase.$index"),
                )
            }

            is OverviewHeader.Facts -> header.facts.forEachIndexed { index, fact ->
                BaupilotText(
                    text = fact.label,
                    size = 14.sp,
                    color = Tokens.text,
                    modifier = Modifier.testTag("overview.fact.$index"),
                )
            }
        }
        model.groups.forEachIndexed { group, section ->
            section.rows.forEachIndexed { row, item ->
                val target = item.target
                val tap = if (target != null) Modifier.clickable { onAction(OverviewAction.Open(target)) } else Modifier
                BaupilotText(
                    text = item.name,
                    size = 14.sp,
                    color = Tokens.text,
                    modifier = Modifier.height(48.dp).testTag("overview.row.$group.$row").then(tap),
                )
            }
        }
    }
}
