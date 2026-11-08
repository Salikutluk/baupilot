#if DEBUG
import SwiftUI

/// Every button and variant.
struct GalleryButtonsPage: View {
    var body: some View {
        GalleryGroup("PrimaryButton · large / medium / small") {
            VStack(spacing: 8) {
                PrimaryButton("Angebote vergleichen", size: .large, action: nil)
                PrimaryButton("Angebote vergleichen", size: .medium, action: nil)
                PrimaryButton("Angebote vergleichen", size: .small, action: nil)
            }
        }
        GalleryGroup("SecondaryButton · SecondaryIconButton") {
            HStack(spacing: 10) {
                SecondaryButton("Rückfrage", action: nil)
                SecondaryIconButton(label: "Dokument hinzufügen", action: nil)
            }
        }
        GalleryGroup("DefectButton · small / medium with icon") {
            HStack(alignment: .top, spacing: 10) {
                DefectButton("Mangel", size: .small, action: nil)
                DefectButton("Mangel", size: .medium, showsIcon: true, action: nil)
            }
        }
        GalleryGroup("OkButton · small / medium with icon") {
            HStack(spacing: 10) {
                OkButton("In Ordnung", size: .small, action: nil)
                OkButton("In Ordnung", size: .medium, showsIcon: true, action: nil)
            }
        }
        GalleryGroup("PhotoButton + pair layout (50, 1fr, 1fr)") {
            HStack(alignment: .top, spacing: 10) {
                PhotoButton(label: "Foto aufnehmen", action: nil)
                DefectButton("Mangel", size: .medium, showsIcon: true, action: nil)
                OkButton("In Ordnung", size: .medium, showsIcon: true, action: nil)
            }
        }
        GalleryGroup("PhotoButton.wide") {
            PhotoButton.wide("Foto aufnehmen", action: nil)
        }
    }
}
#endif
