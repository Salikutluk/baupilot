#if DEBUG
import SwiftUI

/// Card styles and borders, ListRow variants.
struct GalleryContainersPage: View {
    var body: some View {
        GalleryGroup("Card · standard / hero") {
            HStack(spacing: 10) {
                Card(style: .standard) { sampleText("Rohbau") }
                Card(style: .hero) { sampleText("Dachdecker") }
            }
        }
        GalleryGroup("Card · row / grouped") {
            HStack(spacing: 10) {
                Card(style: .row) { sampleText("Estrich") }
                Card(style: .grouped) { sampleText("Installateur") }
            }
        }
        GalleryGroup("Card · border current / overdue") {
            HStack(spacing: 10) {
                Card(border: .current) { sampleText("Aktuell") }
                Card(border: .overdue()) { sampleText("Frist überschritten") }
            }
        }
        GalleryGroup("ListRow · icon tile, price, mini badge, coloured tile, divider") {
            Card(style: .standard, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ListRow(
                        title: "Estrichbau Wagner", subtitle: "PDF · eingegangen 10.12.",
                        leading: .iconTile(.documentW22, size: .s38),
                        trailing: .badge(StatusBadge("Günstigstes", tone: .ok, size: .mini, weight: .bold)),
                        showsDivider: false, action: nil)
                    ListRow(
                        title: "Fließestrich Hahn", subtitle: "PDF · eingegangen 14.12.",
                        leading: .iconTile(.documentW22, size: .s38),
                        trailing: .value("10.450 €", .bold),
                        showsDivider: true, action: nil)
                    ListRow(
                        title: "Angebot Estrichbau Wagner", subtitle: "PDF · 10.12.2026",
                        leading: .iconTile(.document, size: .s38, bg: Tokens.aSoft, fg: Tokens.aFg),
                        trailing: .chevron(.small),
                        showsDivider: true, action: nil)
                }
            }
        }
        GalleryGroup("ListRow · text tile, badge, chevron · min height, large title · 34 r9 tile") {
            Card(style: .standard, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ListRow(
                        title: "Elektriker", subtitle: "Rohinstallation",
                        leading: .textTile("KW 3", .s48x40),
                        trailing: .badgeAndChevron(
                            StatusBadge("Beauftragt", tone: .ok), .regular),
                        showsDivider: false, action: nil)
                    ListRow(
                        title: "Elektro", subtitle: "10 Prüfschritte · KW 3",
                        trailing: .badge(StatusBadge("Geplant", tone: .neutral())),
                        titleSize: .large, density: .minHeight(56),
                        showsDivider: true, action: nil)
                    ListRow(
                        title: "Bad OG", subtitle: "Boden & Wand · Schritt 3 von 6",
                        leading: .iconTile(.drop, size: .s34r9),
                        trailing: .chevron(.small),
                        showsDivider: true, action: nil)
                }
            }
        }
    }

    private func sampleText(_ text: String) -> some View {
        Text(text)
            .font(.inter(15, .semibold))
            .foregroundStyle(Tokens.text.color)
    }
}
#endif
