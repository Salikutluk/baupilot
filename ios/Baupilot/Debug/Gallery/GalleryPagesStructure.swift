#if DEBUG
import SwiftUI

/// LargeTitleHeader and BackBar variants.
struct GalleryStructurePage: View {
    var body: some View {
        GalleryGroup("LargeTitleHeader · kicker + bell (unread)") {
            LargeTitleHeader(
                title: "Übersicht", kicker: "Einfamilienhaus · Musterweg 12",
                trailing: .bell(unread: true, label: "Mitteilungen, 2 neu", action: nil))
        }
        GalleryGroup("LargeTitleHeader · kicker + add") {
            LargeTitleHeader(
                title: "Dokumente", kicker: "26 Dokumente · 11 Gewerke",
                trailing: .add(label: "Dokument hinzufügen", action: nil))
        }
        GalleryGroup("LargeTitleHeader · link") {
            LargeTitleHeader(title: "Abnahmen", trailing: .link(text: "Vorlagen", action: nil))
        }
        GalleryGroup("LargeTitleHeader · plain") {
            LargeTitleHeader(title: "Profil")
        }
        GalleryGroup("BackBar · plain") {
            BackBar(label: "Übersicht", action: nil)
        }
        GalleryGroup("BackBar · counter") {
            BackBar(label: "Übersicht", action: nil, trailing: .counter("Schritt 3 von 6"))
        }
        GalleryGroup("BackBar · note") {
            BackBar(label: "Übersicht", action: nil, trailing: .note("Frist 15.01."))
        }
        GalleryGroup("BackBar · icon") {
            BackBar(
                label: "Übersicht", action: nil,
                trailing: .icon(.moreHorizontal, size: 24, label: "Weitere Aktionen", action: nil))
        }
        GalleryGroup("BackBar · text") {
            BackBar(label: "Übersicht", action: nil, trailing: .text("Alle gelesen", action: nil))
        }
        GalleryGroup("BackBar · modal") {
            BackBar.modal(cancel: "Abbrechen", title: "Mangel erfassen", confirm: "Sichern")
        }
    }
}
#endif
