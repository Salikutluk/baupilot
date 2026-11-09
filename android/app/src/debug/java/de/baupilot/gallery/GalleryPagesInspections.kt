package de.baupilot.gallery

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTabRow
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.TabLabelAlignment
import de.baupilot.designsystem.components.Trailing

@Composable
internal fun InspectionsPage() {
    Caption("BaupilotTabRow · Indikator 64, Label mittig (AndAbnGewerke:24)")
    BaupilotTabRow(
        tabs = listOf("Gewerke", "Offene Mängel (3)"),
        selectedIndex = 0,
        contentDescription = "Ansicht",
        indicatorWidth = 64.dp,
        unselectedAlignment = TabLabelAlignment.Center,
        onSelect = {},
    )
    Caption("BaupilotListItem · Gewerke-Zeilen H60, Status 12 / 500 (AndAbnGewerke:28)")
    Column {
        BaupilotListItem(
            title = "Erdarbeiten",
            subtitle = "9 Prüfschritte · 14.04.",
            leading = Leading.Avatar.icon(BaupilotIcon.CheckW22, Tokens.okBg, Tokens.okFg),
            trailing = Trailing.Text("Abgenommen", 12.sp, Tokens.okFg),
            minHeight = ListItemHeight.H60,
        )
        BaupilotListItem(
            title = "Rohbau",
            subtitle = "9 Prüfschritte · 1 Mangel offen",
            leading = Leading.Avatar.icon(BaupilotIcon.AlertCircleW22, Tokens.warnBg, Tokens.warnFg.abnGewerke),
            trailing = Trailing.Text("Vorbehalt", 12.sp, Tokens.warnFg.abnGewerke),
            minHeight = ListItemHeight.H60,
        )
        BaupilotListItem(
            title = "Fenster & Türen",
            subtitle = "6 Schritte je Element · 7 / 12",
            leading = Leading.Avatar.icon(BaupilotIcon.ClipboardW22, Tokens.nowBg, Tokens.nowFg),
            trailing = Trailing.Text("Heute", 12.sp, Tokens.primary),
            minHeight = ListItemHeight.H60,
            background = Tokens.hi,
        )
        BaupilotListItem(
            title = "Elektro",
            subtitle = "10 Prüfschritte · KW 3",
            leading = Leading.Avatar.icon(BaupilotIcon.CalendarW22, Tokens.planBg, Tokens.muted),
            trailing = Trailing.Text("Geplant", 12.sp, Tokens.muted),
            minHeight = ListItemHeight.H60,
        )
    }
}
