package de.baupilot.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.components.DocumentTile
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.SectionLink

/** Only the parts the Home screen added: section headers and the document tile. */
@Composable
internal fun OverviewPage() {
    Caption("SectionHeader · Overview mit Link (h40)")
    SectionHeader("Als Nächstes benötigt", SectionHeaderStyle.Overview(SectionLink("Alle")))
    Caption("SectionHeader · Overview ohne Link (h32)")
    SectionHeader("Dokumente", SectionHeaderStyle.Overview())
    Caption("SectionHeader · Settings")
    SectionHeader("Benachrichtigungen", SectionHeaderStyle.Settings)
    Caption("DocumentTile · 2 x 2, Abstand 8")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { DocumentTile(BaupilotIcon.Document, "Angebote", "7", onClick = null) }
        Box(Modifier.weight(1f)) { DocumentTile(BaupilotIcon.Contract, "Verträge", "3", onClick = null) }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { DocumentTile(BaupilotIcon.Receipt, "Rechnungen", "4", onClick = null) }
        Box(Modifier.weight(1f)) { DocumentTile(BaupilotIcon.Map, "Pläne", "12", onClick = null) }
    }
}
