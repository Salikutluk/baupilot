#if DEBUG
import SwiftUI

/// Dokumente parts: HintCard, SectionHeader caps inset, DetailTitle and the DocumentPreview page.
struct GalleryDocumentsPage: View {
    var body: some View {
        GalleryGroup("HintCard · aSoft + accent tile") {
            HintCard(
                icon: .clockW22, title: "Rechnung prüfen & zahlen", subtitle: "Fenster & Türen · fällig 22.12.",
                amount: "18.400 €", background: Tokens.aSoft, tileBackground: Tokens.accent,
                tileForeground: Tokens.onAccent, subtitleColor: Tokens.aFg, action: nil)
        }
        GalleryGroup("SectionHeader · caps inset 14 / 4") {
            VStack(alignment: .leading, spacing: 8) {
                SectionHeader("Dokumente", style: .caps)
                SectionHeader("Heute", style: .caps(inset: 4))
            }
        }
        GalleryGroup("DetailTitle · size28 / size22") {
            VStack(alignment: .leading, spacing: 12) {
                DetailTitle("Estrich vergleichen", subtitle: "3 Angebote · Stand 10.12.2026", style: .size28)
                DetailTitle("Angebot Estrichbau Wagner", subtitle: "PDF · 2 Seiten · eingegangen 10.12.2026", style: .size22)
            }
        }
        GalleryGroup("DocumentPreview · 250 x 320") {
            DocumentPreview(
                issuerName: "Estrichbau Wagner GmbH", issuerAddress: "Industriestraße 4 · 68519 Viernheim",
                number: "Angebot Nr. 2026-118",
                reference: "BV Einfamilienhaus Musterweg 12 · gültig bis 31.01.2027",
                lines: [
                    .init(pos: "1", text: "Randdämmstreifen 8 mm, liefern & setzen", sum: "420,00"),
                    .init(pos: "2", text: "PE-Folie als Trennlage", sum: "310,00"),
                    .init(pos: "3", text: "Zementestrich CT-C25-F4, 65 mm, 112 m²", sum: "7.150,00"),
                    .init(pos: "4", text: "Bewegungsfugen anlegen", sum: "355,00"),
                    .init(pos: "5", text: "Baustelleneinrichtung", sum: "1.565,00"),
                ],
                totalLabel: "Gesamt brutto", total: "9.800,00 €", accessibilityLabel: "Vorschau Seite 1 von 2")
        }
    }
}
#endif
