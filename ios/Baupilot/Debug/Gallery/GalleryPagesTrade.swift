#if DEBUG
import SwiftUI

/// Gewerk parts: ListRow `.amount`, StatusBadge `.micro`, ChecklistRow and the SectionHeader note.
struct GalleryTradePage: View {
    var body: some View {
        GalleryGroup("SectionHeader · title + note") {
            SectionHeader("Angebote", style: .title(note: "2 von 3 erhalten"))
        }
        GalleryGroup("ListRow · amount (best + badge / plain / pending muted)") {
            Card(padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ListRow(
                        title: "Estrich Wagner", subtitle: "Zement · 8 Tage",
                        leading: .iconTile(.document, size: .s38),
                        trailing: .amount(
                            "5.870 €", size: 15, weight: .bold, color: Tokens.text,
                            badge: StatusBadge("Günstigstes", tone: .ok, size: .micro, weight: .bold)),
                        showsDivider: false, action: nil)
                    ListRow(
                        title: "Estrich Hahn", subtitle: "Anhydrit · 6 Tage",
                        leading: .iconTile(.document, size: .s38),
                        trailing: .amount("6.420 €", size: 15, weight: .bold, color: Tokens.text),
                        showsDivider: true, action: nil)
                    ListRow(
                        title: "Estrich Lorenz", subtitle: "Angebot ausstehend",
                        leading: .iconTile(.document, size: .s38),
                        trailing: .amount("offen", size: 15, weight: .bold, color: Tokens.muted),
                        showsDivider: true, action: nil)
                }
            }
        }
        GalleryGroup("StatusBadge · micro (ok / accent / neutral)") {
            HStack(spacing: 8) {
                StatusBadge("Günstigstes", tone: .ok, size: .micro, weight: .bold)
                StatusBadge("Neu", tone: .accent, size: .micro, weight: .bold)
                StatusBadge("Offen", tone: .neutral(), size: .micro, weight: .bold)
            }
        }
        GalleryGroup("ChecklistRow · done / open") {
            VStack(alignment: .leading, spacing: 10) {
                ChecklistRow(title: "Angebote einholen", done: true)
                ChecklistRow(title: "Angebote vergleichen", done: false)
                ChecklistRow(title: "Auftrag vergeben", done: false)
            }
        }
    }
}
#endif
