#if DEBUG
import SwiftUI

/// Abnahmen parts: the Gewerke list (no leading tile, badge + small chevron) and the ListRow background.
struct GalleryInspectionsPage: View {
    var body: some View {
        // The block-text rows report no usable minimum height: next to other groups the page would squeeze them.
        VStack(alignment: .leading, spacing: 12) {
            GalleryGroup("ListRow · badge + chevron 14 · minHeight 54 · background hi on the third row") {
                Card(padding: EdgeInsets()) {
                    VStack(alignment: .leading, spacing: 0) {
                        ListRow(
                            title: "Erdarbeiten", subtitle: "9 Prüfschritte · abgenommen 14.04.",
                            trailing: .badgeAndChevron(StatusBadge("Abgenommen", tone: .ok), .small),
                            titleSize: .large, density: .minHeight(54), showsDivider: false, subtitleLines: .block, action: nil)
                        ListRow(
                            title: "Rohbau", subtitle: "9 Prüfschritte · 1 Mangel offen",
                            trailing: .badgeAndChevron(
                                StatusBadge("Mit Vorbehalt", tone: .warn(fg: Tokens.wFg.abnGewerke)), .small),
                            titleSize: .large, density: .minHeight(54), showsDivider: true, action: nil)
                        ListRow(
                            title: "Fenster & Türen", subtitle: "6 Prüfschritte je Element · 7 von 12",
                            trailing: .badgeAndChevron(StatusBadge("Heute", tone: .accent), .small),
                            titleSize: .large, density: .minHeight(54), showsDivider: true,
                            background: Tokens.hi, action: nil)
                        ListRow(
                            title: "Elektro", subtitle: "10 Prüfschritte · KW 3",
                            trailing: .badgeAndChevron(
                                StatusBadge("Geplant", tone: .neutral(bg: Tokens.chip.abnGewerke)), .small),
                            titleSize: .large, density: .minHeight(54), showsDivider: true, action: nil)
                    }
                }
            }
            GalleryGroup("SegmentedControl · regular 44 · chip AbnMaengel · second segment selected") {
                SegmentedControl(
                    segments: ["Gewerke", "Offene Mängel (3)"], selection: 1, height: .regular,
                    background: .chip(Tokens.chip.abnMaengel), accessibilityLabel: "Ansicht", onSelect: { _ in })
            }
            GalleryGroup("PillButton · 44 tall incl. border · outline (border AbnMaengel) + filled · gap 8") {
                HStack(spacing: 8) {
                    PillButton("Erinnern", style: .outline(border: Tokens.border.abnMaengel), action: nil)
                    PillButton("Behoben?", style: .filled, action: nil)
                }
            }
            GalleryGroup("CountLinkRow · min 48 · icon 18 · count muted · chevron 14 · in Card(.row)") {
                Card(
                    style: .row, padding: EdgeInsets(top: 0, leading: 14, bottom: 0, trailing: 14)
                ) {
                    CountLinkRow(
                        icon: .checkW26, iconColor: Tokens.ok.core, title: "Behobene Mängel", count: "3", action: nil)
                }
            }
        }
        .fixedSize(horizontal: false, vertical: true)
    }
}
#endif
