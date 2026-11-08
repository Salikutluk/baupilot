#if DEBUG
import SwiftUI

/// Photo tiles, stat tiles and fact strips (split off the steps page to keep each page within 844 pt).
struct GalleryTilesPage: View {
    var body: some View {
        GalleryGroup("PhotoTile · 84 guide / photo (checked, caption) / add") {
            HStack(spacing: 8) {
                PhotoTile(kind: .guide(title: "So prüfen", illustration: guideIllustration), height: .h84)
                PhotoTile(kind: .photo(photo, caption: "Foto oben", checked: true), height: .h84)
                PhotoTile(kind: .add(label: "Foto seitlich"), height: .h84)
            }
        }
        GalleryGroup("PhotoTile · 76 photo") {
            HStack(spacing: 8) {
                PhotoTile(kind: .photo(photo, caption: nil, checked: false), height: .h76)
                PhotoTile(kind: .add(label: "Foto"), height: .h76)
                PhotoTile(kind: .guide(title: "So prüfen", illustration: guideIllustration), height: .h76)
            }
        }
        GalleryGroup("CountTile · surface / ok / error · PhaseTile · done / planned / active") {
            VStack(spacing: 8) {
                HStack(spacing: 8) {
                    CountTile(value: "12", label: "geprüft", tone: .surface)
                    CountTile(value: "11", label: "in Ordnung", tone: .ok)
                    CountTile(value: "1", label: "Mangel", tone: .error)
                }
                HStack(spacing: 8) {
                    PhaseTile(label: "1 · Rohinstallation", value: "Abgenommen", date: "am 29.01.2027", state: .done)
                    PhaseTile(label: "2 · Fertiginstallation", value: "4 von 11 geprüft", state: .active(progress: 0.36))
                }
                HStack(spacing: 8) {
                    PhaseTile(label: "1 · Rohinstallation", value: "3 von 6 geprüft", state: .active(progress: 0.5))
                    PhaseTile(label: "2 · Fertigmontage", value: "Geplant KW 14", state: .planned)
                }
            }
        }
        GalleryGroup("FactStrip · divided / grid") {
            VStack(spacing: 8) {
                FactStrip(
                    items: [("Zeitfenster", "KW 8–9"), ("Angebotsfrist", "15.01.2027"), ("Wartet auf", "Installateur")],
                    style: .divided)
                FactStrip(
                    items: [("Qualität", "Q3"), ("Farbtöne", "3 bemustert"), ("Räume", "3 von 9")], style: .grid)
            }
        }
    }

    private var photo: Image { Image(systemName: "photo") }

    private var guideIllustration: AnyView {
        AnyView(Icon(.clipboardW22, size: 28, color: Tokens.muted.color))
    }
}
#endif
