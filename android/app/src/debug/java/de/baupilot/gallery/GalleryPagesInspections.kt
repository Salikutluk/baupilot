package de.baupilot.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTabRow
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.StatusChip
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.designsystem.components.StatusChipSize
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
    Caption("BaupilotCard · borderOutside, container null / errCard, r12, 14 16 (AndAbnMaengel:31)")
    DefectCardSample(late = true)
    DefectCardSample(late = false)
    Caption("BaupilotListItem · Behobene Mängel H56, Padding 4, Zähler 14 / 400 (AndAbnMaengel:44)")
    BaupilotListItem(
        title = "Behobene Mängel",
        leading = Leading.Icon(BaupilotIcon.CheckW26, Tokens.okFg),
        trailing = Trailing.Text("3", 14.sp, Tokens.muted, weight = 400),
        minHeight = ListItemHeight.H56,
        horizontalPadding = 4.dp,
    )
}

@Composable
private fun DefectCardSample(late: Boolean) {
    BaupilotCard(
        modifier = Modifier.fillMaxWidth(),
        container = if (late) Tokens.errCard else null,
        radius = 12.dp,
        border = if (late) Tokens.err else Tokens.outlineVariant,
        padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        borderOutside = true,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BaupilotText("ROHBAU", 12.sp, 500, Tokens.muted, letterSpacing = 0.4.sp)
                StatusChip(
                    if (late) "3 Tage überfällig" else "noch 28 Tage",
                    if (late) StatusChipColors.Error else StatusChipColors.Neutral,
                    StatusChipSize.Auto,
                )
            }
            BaupilotText("Riss in Kellerwand Nord", 16.sp, color = Tokens.text)
            BaupilotText("Frist 15.12.2026", 14.sp, color = Tokens.muted)
            if (late) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    OutlinedButton("Erinnern", size = ButtonSize.Compact)
                    FilledButton("Behoben prüfen", ButtonSize.Compact, FilledContainer.Primary)
                }
            }
        }
    }
}
