package de.baupilot.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.components.DocumentPreview
import de.baupilot.designsystem.components.HintCard
import de.baupilot.designsystem.components.PreviewLine
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.SectionLink

@Composable
internal fun DocumentsPage() {
    Caption("HintCard · Titel, Untertitel und Betrag (AndDokumente:31)")
    HintCard(BaupilotIcon.Clock, "Rechnung prüfen & zahlen", "Fenster & Türen · fällig 22.12.", "18.400 €")
    Caption("SectionHeader · Compact mit Link, 48 Trefferfläche (AndDokumente:40)")
    SectionHeader("Zuletzt hinzugefügt", SectionHeaderStyle.Compact(SectionLink("Nach Gewerk")))
    Caption("DocumentPreview · 250 × 320, immer weiß (AndDokViewer:35)")
    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
        DocumentPreview(
            issuerName = "Estrichbau Wagner GmbH",
            issuerAddress = "Industriestraße 4 · 68519 Viernheim",
            number = "Angebot Nr. 2026-118",
            reference = "BV Einfamilienhaus Musterweg 12 · gültig bis 31.01.2027",
            lines = listOf(
                PreviewLine("1", "Randdämmstreifen 8 mm, liefern & setzen", "420,00"),
                PreviewLine("2", "PE-Folie als Trennlage", "310,00"),
                PreviewLine("3", "Zementestrich CT-C25-F4, 65 mm, 112 m²", "7.150,00"),
                PreviewLine("4", "Bewegungsfugen anlegen", "355,00"),
                PreviewLine("5", "Baustelleneinrichtung", "1.565,00"),
            ),
            totalLabel = "Gesamt brutto",
            total = "9.800,00 €",
            contentDescription = "Vorschau Seite 1 von 2",
        )
    }
}
