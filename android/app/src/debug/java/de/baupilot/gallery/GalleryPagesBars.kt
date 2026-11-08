package de.baupilot.gallery

import androidx.compose.runtime.Composable
import de.baupilot.designsystem.components.Action
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotNavigationBar
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.Tab
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.BaupilotIcon

@Composable
internal fun BarsPage() {
    Caption("TopAppBar · top-level, Glocke mit Badge")
    BaupilotTopAppBar(
        title = "Übersicht",
        subtitle = "Einfamilienhaus · Musterweg 12",
        padding = BarPadding.TopLevelAction,
        titleStyle = TitleStyle.LargeTall,
        actions = listOf(Action.Bell(label = "Mitteilungen, 2 neu", badge = "2")),
    )
    Caption("TopAppBar · Zurück, Aktion")
    BaupilotTopAppBar(
        title = "Estrich",
        padding = BarPadding.Nav,
        navigation = Nav.Back(),
        actions = listOf(Action.Icon(BaupilotIcon.MoreVertical, "Weitere Optionen")),
    )
    Caption("TopAppBar · Schließen, Detail mit Untertitel und Zähler")
    BaupilotTopAppBar(
        title = "F8 · Schlafen",
        subtitle = "OG · Dreh-Kipp-Fenster, 1-flügelig",
        padding = BarPadding.NavCounter,
        navigation = Nav.Close(),
        titleStyle = TitleStyle.Detail,
        trailingText = "3 / 6",
    )
    Caption("TopAppBar · top-level, Textaktion (40)")
    BaupilotTopAppBar(
        title = "Abnahmen",
        padding = BarPadding.TopLevelAction,
        actions = listOf(Action.Text("Vorlagen", height = 40)),
    )
    Caption("NavigationBar · Übersicht aktiv")
    BaupilotNavigationBar(selected = Tab.Overview, onSelect = {})
    Caption("NavigationBar · Abnahmen aktiv")
    BaupilotNavigationBar(selected = Tab.Acceptance, onSelect = {})
}

@Composable
internal fun BarsMorePage() {
    Caption("TopAppBar · top-level, Textaktion (48)")
    BaupilotTopAppBar(
        title = "Abnahmen",
        padding = BarPadding.TopLevelAction,
        actions = listOf(Action.Text("Vorlagen", height = 48)),
    )
    Caption("TopAppBar · kompakter Titel mit Ellipse, Teilen")
    BaupilotTopAppBar(
        title = "Angebot Estrichbau Wagner",
        subtitle = "PDF · 2 Seiten · 10.12.2026",
        padding = BarPadding.Nav,
        navigation = Nav.Back(),
        titleStyle = TitleStyle.Compact,
        actions = listOf(Action.Icon(BaupilotIcon.Share, "Teilen")),
    )
    Caption("TopAppBar · modal, Abbrechen und Sichern")
    BaupilotTopAppBar(
        title = "Mangel erfassen",
        padding = BarPadding.Modal,
        navigation = Nav.Close(label = "Abbrechen"),
        titleStyle = TitleStyle.Detail,
        actions = listOf(Action.Text("Sichern", height = 40)),
    )
}
