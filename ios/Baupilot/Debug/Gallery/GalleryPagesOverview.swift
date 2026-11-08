#if DEBUG
import SwiftUI

/// SectionHeader (title with link, title, caps) and DocumentTile (the Home parts).
struct GalleryOverviewPage: View {
    var body: some View {
        GalleryGroup("SectionHeader · title with link") {
            SectionHeader(
                "Als Nächstes benötigt",
                style: .title(link: SectionHeader.Link(title: "Zeitplan", action: nil)))
        }
        GalleryGroup("SectionHeader · title") {
            SectionHeader("Dokumente", style: .title())
        }
        GalleryGroup("SectionHeader · caps") {
            SectionHeader("Bauvorhaben", style: .caps)
        }
        GalleryGroup("DocumentTile · 2 x 2 grid") {
            Grid(horizontalSpacing: 10, verticalSpacing: 10) {
                GridRow {
                    DocumentTile(icon: .document, label: "Angebote", count: "7", action: nil)
                    DocumentTile(icon: .contract, label: "Verträge", count: "3", action: nil)
                }
                GridRow {
                    DocumentTile(icon: .receipt, label: "Rechnungen", count: "4", action: nil)
                    DocumentTile(icon: .map, label: "Pläne", count: "12", action: nil)
                }
            }
        }
    }
}
#endif
