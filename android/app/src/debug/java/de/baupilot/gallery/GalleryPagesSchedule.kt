package de.baupilot.gallery

import androidx.compose.runtime.Composable
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.AskRow
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.LegendItem
import de.baupilot.designsystem.components.LegendShape
import de.baupilot.designsystem.components.ScheduleChart
import de.baupilot.designsystem.components.ScheduleLegend
import de.baupilot.schedule.MonthLabel
import de.baupilot.schedule.ScheduleLayout
import de.baupilot.schedule.ScheduleRowLayout
import de.baupilot.schedule.ScheduleState

/** Only the parts the Zeitplan screen added: ask row, legend and a small chart with all three bar states. */
@Composable
internal fun SchedulePage() {
    Caption("AskRow · min 52 auf primaryContainer")
    BaupilotCard(container = Tokens.primaryContainer) {
        AskRow("Estrich", "Start KW 8 · Angebote bis 15.01.", onClick = null)
        AskRow("Fliesen & Maler", "Start März · jetzt je 3 Angebote einholen", onClick = null)
    }
    Caption("ScheduleLegend")
    ScheduleLegend(
        listOf(
            LegendItem("Erledigt", Tokens.done),
            LegendItem("Läuft", Tokens.primary),
            LegendItem("Geplant", Tokens.plan),
            LegendItem("Anfragen", Tokens.primary, LegendShape.Diamond),
        ),
    )
    Caption("ScheduleChart · erledigt, läuft, geplant mit Anfrage-Raute")
    ScheduleChart(sampleLayout, "Heute", "Beispiel-Zeitplan")
}

private val sampleLayout = ScheduleLayout(
    rows = listOf(
        ScheduleRowLayout("a", "Erdarbeiten", ScheduleState.Done, barX = 126f, barWidth = 40.4f, diamondCenterX = null),
        ScheduleRowLayout("b", "Fenster & Türen", ScheduleState.Running, barX = 200f, barWidth = 60f, diamondCenterX = null),
        ScheduleRowLayout("c", "Estrich", ScheduleState.Planned, barX = 262.752f, barWidth = 16.5088f, diamondCenterX = 239.1872f),
    ),
    todayX = 243.2296f,
    todayLabelX = 229.2296f,
    monthLabels = listOf(MonthLabel(126f, "Mär 26"), MonthLabel(223.6952f, "Nov"), MonthLabel(321.3672f, "Jul")),
)
