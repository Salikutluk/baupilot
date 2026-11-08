#if DEBUG
import SwiftUI

/// TabBar and BottomActionBar variants.
struct GalleryBarsPage: View {
    var body: some View {
        GalleryGroup("TabBar · overview selected") {
            TabBar(selected: .overview, onSelect: { _ in })
        }
        GalleryGroup("TabBar · inspections selected") {
            TabBar(selected: .inspections, onSelect: { _ in })
        }
        GalleryGroup("BottomActionBar · single primary") {
            BottomActionBar {
                PrimaryButton("Angebote vergleichen", action: nil)
            }
        }
        GalleryGroup("BottomActionBar · primary + icon button") {
            BottomActionBar {
                HStack(spacing: 10) {
                    PrimaryButton("Angebote vergleichen", action: nil)
                    SecondaryIconButton(label: "Dokument hinzufügen", action: nil)
                }
            }
        }
        GalleryGroup("BottomActionBar · grid 1 : 1.4") {
            BottomActionBar {
                RatioRow(spacing: 10) {
                    SecondaryButton("Rückfrage", action: nil)
                    PrimaryButton("Wagner beauftragen", action: nil)
                }
            }
        }
        GalleryGroup("BottomActionBar · caption") {
            BottomActionBar(
                bottomPadding: .withCaption,
                caption: "PDF mit Fotos geht an Sie und den Handwerker"
            ) {
                PrimaryButton("Protokoll abschließen", action: nil)
            }
        }
    }
}

/// Two children side by side with widths in the ratio 1 : 1.4 (the prototype's `1fr 1.4fr` grid).
private struct RatioRow: Layout {
    let spacing: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let width = proposal.width ?? 0
        let widths = columnWidths(total: width)
        let height = zip(subviews, widths).map { $0.sizeThatFits(.init(width: $1, height: nil)).height }.max() ?? 0
        return CGSize(width: width, height: height)
    }

    func placeSubviews(
        in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()
    ) {
        var x = bounds.minX
        for (subview, width) in zip(subviews, columnWidths(total: bounds.width)) {
            subview.place(
                at: CGPoint(x: x, y: bounds.minY), anchor: .topLeading,
                proposal: .init(width: width, height: bounds.height))
            x += width + spacing
        }
    }

    private func columnWidths(total: CGFloat) -> [CGFloat] {
        let free = max(0, total - spacing)
        return [free / 2.4, free * 1.4 / 2.4]
    }
}
#endif
