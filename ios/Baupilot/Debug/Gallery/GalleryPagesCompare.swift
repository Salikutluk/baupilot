#if DEBUG
import SwiftUI

/// Vergleich parts: ComparisonTable (header with badge and action, strong and highlighted rows),
/// InfoCallout in its accent form and ActionRow.
struct GalleryComparePage: View {
    private let widths = ComparisonLayout.columns(cardWidth: 350, padding: 14, gap: 10, labelColumn: 104)

    var body: some View {
        GalleryGroup("ComparisonTable · badge + action header, strong / highlighted / plain rows") {
            ComparisonTable(
                columns: [
                    .init(name: "Estrichbau Wagner", badge: "Günstigstes", action: {}),
                    .init(name: "Fließestrich Hahn", badge: nil, action: nil),
                ],
                rows: [
                    .init(label: "Preis brutto", a: "9.800 €", b: "10.450 €", strong: true, highlighted: false),
                    .init(label: "Estrichart", a: "Zement CT", b: "Fließ CAF", strong: false, highlighted: true),
                    .init(label: "Dicke", a: "65 mm", b: "50 mm", strong: false, highlighted: true),
                    .init(label: "Termin", a: "KW 8", b: "KW 8", strong: false, highlighted: false),
                ],
                widths: widths, accessibilityLabel: "Vergleich der Estrich-Angebote")
        }
        GalleryGroup("InfoCallout · accent r14 / 14") {
            InfoCallout(
                text: "Hahn enthält das Belegreifheizen (ca. 600 €). Rechnet man es heraus, liegen beide nur rund 50 € auseinander.",
                icon: .infoW22, tone: .accent, radius: .r14, textSize: 14, textLines: .block)
        }
        GalleryGroup("ActionRow · without action") {
            ActionRow(
                title: "Bodenbau Lorenz", subtitle: "Angefragt am 05.12. · noch kein Angebot",
                actionTitle: "Erinnern", action: nil)
        }
    }
}
#endif
