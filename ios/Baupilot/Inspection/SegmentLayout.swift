import CoreGraphics

/// Segment edges of a progress track (AbnElemente progress bar), snapped to device pixels. Pure maths, no UI.
public enum SegmentLayout {
    public struct Edge: Equatable, Sendable {
        public let start: CGFloat
        public let end: CGFloat

        public init(start: CGFloat, end: CGFloat) {
            self.start = start
            self.end = end
        }
    }

    /// Each edge is the exact cumulative position rounded to the nearest `1 / scale`; the width of a segment is never
    /// rounded on its own, so fractional widths do not drift.
    public static func edges(count: Int, width: CGFloat, gap: CGFloat, scale: CGFloat) -> [Edge] {
        guard count > 0 else { return [] }
        let segment = (width - CGFloat(count - 1) * gap) / CGFloat(count)
        func snap(_ value: CGFloat) -> CGFloat { (value * scale).rounded() / scale }
        return (0..<count).map { index in
            let start = CGFloat(index) * (segment + gap)
            return Edge(start: snap(start), end: snap(start + segment))
        }
    }
}
