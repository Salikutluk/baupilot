#if DEBUG
import SwiftUI

/// Abnahme Elemente parts: SegmentedProgress, ProgressLegend, DetailTitle size30, the caps header with a link, InlineLink.
struct GalleryInspectionElementsPage: View {
    private let segments: [ColorToken] =
        [0, 1, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2].map { kind in
            switch kind {
            case 0: return Tokens.ok.core
            case 1: return Tokens.err.abnElemente
            default: return Tokens.track.abnElemente
            }
        }

    var body: some View {
        GalleryGroup("DetailTitle · size30 (30/700, tracking -0.6, subtitle 14, gap 4)") {
            DetailTitle("Fenster & Türen", subtitle: "Fensterbau Weber GmbH · Fr, 18.12.2026", style: .size30)
        }
        GalleryGroup("SegmentedProgress · 12 segments, height 8, gap 3, radius 3") {
            SegmentedProgress(segments: segments, height: 8, accessibilityLabel: "7 von 12 Elementen geprüft")
        }
        GalleryGroup("ProgressLegend · dot 8, gap 6, row gap 14, 13/400 muted") {
            ProgressLegend(items: [
                .init(label: "6 in Ordnung", color: Tokens.ok.core),
                .init(label: "1 Mangel", color: Tokens.err.abnElemente),
                .init(label: "5 offen", color: Tokens.track.abnElemente),
            ])
        }
        GalleryGroup("InlineLink · 14/600 accent, min-height 44, margin -12") {
            HStack(alignment: .firstTextBaseline) {
                Text("7 von 12 Elementen geprüft")
                    .interText(16, .semibold)
                    .foregroundStyle(Tokens.text.color)
                Spacer(minLength: 0)
                InlineLink("Anleitung", size: 14, action: nil)
            }
        }
        GalleryGroup("SectionHeader · caps inset 4 with link 13/600, min-height 44, margin -14") {
            SectionHeader(
                "Erdgeschoss", style: .caps(inset: 4, link: SectionHeader.Link(title: "Alle 6", size: 13, action: nil)))
        }
    }
}
#endif
