import CoreGraphics

/// Column widths of the Vergleich table (IosVergleich:33, `104px 1fr 1fr`). Pure maths, no UI.
public enum ComparisonLayout {
    public struct Widths: Equatable, Sendable {
        public let label: CGFloat
        public let value: CGFloat

        public init(label: CGFloat, value: CGFloat) {
            self.label = label
            self.value = value
        }
    }

    /// The two value columns share what the card leaves after padding, the label column and both gaps.
    public static func columns(cardWidth: CGFloat, padding: CGFloat, gap: CGFloat, labelColumn: CGFloat) -> Widths {
        let value = (cardWidth - 2 * padding - labelColumn - 2 * gap) / 2
        return Widths(label: labelColumn, value: max(0, value))
    }
}
