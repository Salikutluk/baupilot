#if DEBUG
import SwiftUI

/// Sign-off parts: DotRow and the signed / pending SignatureCard.
struct GallerySignOffPage: View {
    @State private var empty = SignaturePad.emptyStrokes
    @State private var drawn = GallerySignOffPage.sampleStrokes

    private static var sampleStrokes: SignatureStrokes {
        var strokes = SignaturePad.emptyStrokes
        strokes.begin(at: CGPoint(x: 20, y: 44))
        for point in [(40, 16), (62, 40), (84, 14), (120, 36)] {
            strokes.extend(to: CGPoint(x: point.0, y: point.1))
        }
        strokes.end()
        strokes.begin(at: CGPoint(x: 72, y: 54))
        strokes.end()
        return strokes
    }

    var body: some View {
        GalleryGroup("DotRow · min 52, with and without divider") {
            Card(style: .row, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    DotRow(
                        dot: Tokens.err.abnSignatur, title: "F2 · Flügel fällt zu", subtitle: "Mittel · 2 Fotos",
                        trailing: "bis 15.01.")
                    DotRow(
                        dot: Tokens.err.abnSignatur, title: "F5 · Dichtung lose", subtitle: "Gering · 1 Foto",
                        trailing: "bis 29.01.", showsDivider: true, divider: Tokens.border.abnSignatur)
                }
            }
        }
        GalleryGroup("SignatureCard · signed / pending (154 tall, stretched)") {
            HStack(alignment: .top, spacing: 10) {
                SignatureCard(
                    style: .signed(name: "Fensterbau Weber", status: "Unterschrieben 14:32", statusColor: Tokens.oFg),
                    caption: "Handwerker", divider: Tokens.border.abnSignatur,
                    padAccessibilityLabel: "Unterschrift Handwerker")
                SignatureCard(
                    style: .pending(
                        name: "Max Mustermann", status: "Ausstehend", placeholder: "Hier unterschreiben",
                        strokes: $empty),
                    caption: "Bauherr", divider: Tokens.border.abnSignatur, padAccessibilityLabel: "Bauherr")
            }
            .fixedSize(horizontal: false, vertical: true)
        }
        GalleryGroup("SignatureCard · pending with strokes (placeholder hidden)") {
            HStack(alignment: .top, spacing: 10) {
                SignatureCard(
                    style: .pending(
                        name: "Max Mustermann", status: "Ausstehend", placeholder: "Hier unterschreiben",
                        strokes: $drawn),
                    caption: "Bauherr", divider: Tokens.border.abnSignatur, padAccessibilityLabel: "Bauherr")
                Color.clear.frame(maxWidth: .infinity, maxHeight: 0)
            }
            .fixedSize(horizontal: false, vertical: true)
        }
    }
}
#endif
