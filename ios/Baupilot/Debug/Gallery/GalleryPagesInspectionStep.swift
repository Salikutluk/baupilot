#if DEBUG
import SwiftUI

/// Prüfschritt parts: WindowLevelSketch, PhotoPlaceholder and the PhotoTile kinds `.scene`, `.icon`, `.add(nil)`.
struct GalleryInspectionStepPage: View {
    var body: some View {
        GalleryGroup("WindowLevelSketch · 56 x 44 on chip, stroke muted, level accent") {
            WindowLevelSketchView(stroke: Tokens.muted, level: Tokens.accent, bubble: Tokens.chip.core)
                .padding(12)
                .background(Tokens.chip.core.color, in: RoundedRectangle(cornerRadius: 10))
        }
        GalleryGroup("PhotoPlaceholder · 99.333 x 84 / 100.667 x 80 (slice)") {
            HStack(spacing: 8) {
                PhotoPlaceholder()
                    .frame(width: 99.333, height: 84)
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                PhotoPlaceholder()
                    .frame(width: 100.667, height: 80)
                    .clipShape(RoundedRectangle(cornerRadius: 10))
            }
        }
        GalleryGroup("PhotoTile · 84 guide / scene (checked, caption) / add") {
            HStack(spacing: 8) {
                PhotoTile(
                    kind: .guide(
                        title: "So prüfen",
                        illustration: AnyView(
                            WindowLevelSketchView(stroke: Tokens.muted, level: Tokens.accent, bubble: Tokens.chip.core))),
                    height: .h84)
                PhotoTile(
                    kind: .scene(AnyView(PhotoPlaceholder()), caption: "Foto oben", checked: true), height: .h84)
                PhotoTile(kind: .add(label: "Foto seitlich"), height: .h84)
            }
        }
        GalleryGroup("PhotoTile · 76 scene (plain) / icon / icon with fill / add (icon only)") {
            HStack(spacing: 8) {
                PhotoTile(kind: .scene(AnyView(PhotoPlaceholder()), caption: nil, checked: false), height: .h76)
                PhotoTile(kind: .icon(.image), height: .h76, accessibilityLabel: "Foto")
                PhotoTile(
                    kind: .icon(.image, fill: Tokens.chip.abnMangel), height: .h76, accessibilityLabel: "Foto")
                PhotoTile(kind: .add(label: nil), height: .h76, accessibilityLabel: "Foto aufnehmen")
            }
        }
    }
}
#endif
