package de.baupilot.screens.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.AppRoute
import de.baupilot.app.ScheduleModel
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.AskRow
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.LegendItem
import de.baupilot.designsystem.components.LegendShape
import de.baupilot.designsystem.components.ScheduleChart
import de.baupilot.designsystem.components.ScheduleLegend
import de.baupilot.designsystem.components.TabScreenScaffold
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.screens.ScreenCopy

sealed interface ScheduleAction {
    data class OpenTrade(val id: String) : ScheduleAction
}

private val LEGEND = listOf(
    LegendItem(ScreenCopy.Zeitplan.LEGEND_DONE, Tokens.done),
    LegendItem(ScreenCopy.Zeitplan.LEGEND_RUNNING, Tokens.primary),
    LegendItem(ScreenCopy.Zeitplan.LEGEND_PLANNED, Tokens.plan),
    LegendItem(ScreenCopy.Zeitplan.LEGEND_ASK, Tokens.primary, LegendShape.Diamond),
)

/**
 * Zeitplan (AndZeitplan.dc.html): title bar with the move-in subtitle, the ask card, the legend and the chart.
 * An ask row with a trade target opens that trade ("Estrich"); one without ("Fliesen & Maler") does nothing.
 */
@Composable
fun ScheduleScreen(model: ScheduleModel, onAction: (ScheduleAction) -> Unit) {
    TabScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = ScreenCopy.Zeitplan.TITLE,
                subtitle = model.moveInLabel,
                padding = BarPadding.TitleOnly,
                titleStyle = TitleStyle.LargeTall,
            )
        },
        spacing = 12.dp,
        modifier = Modifier.testTag("screen.schedule"),
    ) {
        BaupilotCard(
            container = Tokens.primaryContainer,
            modifier = Modifier.fillMaxWidth(),
            padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        ) {
            BaupilotText(
                text = ScreenCopy.Zeitplan.ASK_HEADING,
                size = 14.sp,
                weight = 500,
                color = Tokens.onPrimaryContainer,
                modifier = Modifier.padding(vertical = 4.dp),
            )
            model.asks.forEach { ask ->
                val trade = ask.target as? AppRoute.Trade
                AskRow(
                    title = ask.name,
                    subtitle = ask.sub,
                    onClick = trade?.let { { onAction(ScheduleAction.OpenTrade(it.id)) } },
                )
            }
        }
        ScheduleLegend(LEGEND)
        ScheduleChart(model.layout, ScreenCopy.Zeitplan.TODAY, model.ariaLabel)
    }
}
