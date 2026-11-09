#if DEBUG
import SwiftUI

/// Abnahmen parts: the Gewerke list (no leading tile, badge + small chevron) and the ListRow background.
struct GalleryInspectionsPage: View {
    var body: some View {
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
    }
}
#endif
