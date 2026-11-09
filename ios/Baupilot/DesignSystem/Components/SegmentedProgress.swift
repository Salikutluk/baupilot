import SwiftUI

/// Progress track of equal segments (IosAbnElemente:27-31): gap 3, radius 3, one colour per segment. The edges come
/// from `SegmentLayout` (cumulative, snapped to device pixels), never from per-segment rounding. It is one
/// accessibility element carrying `accessibilityLabel`.
public struct SegmentedProgress: View {
    private let segments: [ColorToken]
    private let height: CGFloat
    private let gap: CGFloat
    private let radius: CGFloat
    private let label: String

    @Environment(\.displayScale) private var displayScale

    public init(
        segments: [ColorToken], height: CGFloat, gap: CGFloat = 3, radius: CGFloat = 3, accessibilityLabel: String
    ) {
        self.segments = segments
        self.height = height
        self.gap = gap
        self.radius = radius
        self.label = accessibilityLabel
    }

    public var body: some View {
        GeometryReader { proxy in
            let edges = SegmentLayout.edges(
                count: segments.count, width: proxy.size.width, gap: gap, scale: displayScale)
            ZStack(alignment: .topLeading) {
                ForEach(Array(edges.enumerated()), id: \.offset) { index, edge in
                    RoundedRectangle(cornerRadius: radius)
                        .fill(segments[index].color)
                        .frame(width: edge.end - edge.start, height: height)
                        .offset(x: edge.start)
                }
            }
        }
        .frame(height: height)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(label)
    }
}
