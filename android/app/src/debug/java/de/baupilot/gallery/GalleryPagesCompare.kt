package de.baupilot.gallery

import androidx.compose.runtime.Composable
import de.baupilot.compare.columns
import de.baupilot.designsystem.components.ActionRow
import de.baupilot.designsystem.components.ComparisonColumn
import de.baupilot.designsystem.components.ComparisonRow
import de.baupilot.designsystem.components.ComparisonTable
import de.baupilot.designsystem.components.InfoCallout

@Composable
internal fun ComparePage() {
    Caption("ComparisonTable · Kopf mit Günstigstes, zwei fette Zeilen, fünf getönte (AndVergleich:22)")
    ComparisonTable(
        columns = listOf(
            ComparisonColumn(name = "Estrichbau Wagner", badge = "Günstigstes"),
            ComparisonColumn(name = "Fließestrich Hahn"),
        ),
        rows = listOf(
            ComparisonRow("Preis brutto", "9.800 €", "10.450 €", strong = true, highlighted = false),
            ComparisonRow("Pro m²", "87,50 €", "93,30 €", strong = true, highlighted = false),
            ComparisonRow("Estrichart", "Zement CT", "Fließ CAF", strong = false, highlighted = true),
            ComparisonRow("Dicke", "65 mm", "50 mm", strong = false, highlighted = true),
            ComparisonRow("Aufheizen", "extra", "inklusive", strong = false, highlighted = true),
            ComparisonRow("Termin", "KW 8", "KW 9", strong = false, highlighted = true),
            ComparisonRow("Zahlung", "2 Raten", "1 Rate", strong = false, highlighted = true),
        ),
        widths = columns(cardWidth = 358f, padding = 16f, gap = 10f, labelColumn = 104f),
        contentDescription = "Vergleich der Estrich-Angebote",
    )
    Caption("InfoCallout · Info-Symbol (AndVergleich:52)")
    InfoCallout("Hahn enthält das Belegreifheizen (ca. 600 €). Ohne diesen Posten liegen beide nur rund 50 € auseinander.")
    Caption("ActionRow · ohne Aktion (AndVergleich:57)")
    ActionRow("Bodenbau Lorenz", "Angefragt am 05.12. · noch offen", "Erinnern")
}
