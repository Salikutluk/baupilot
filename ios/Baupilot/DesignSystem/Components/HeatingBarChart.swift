import SwiftUI

/// Bar chart of the heating protocol (IosEsBelegreife:33-37): bottom-aligned bars of equal width
/// `(w − gap × (n − 1)) / n` (flex: 1), rounded on the top corners only (3), in a `height` box with `topPadding` above
/// it. Each bar carries its own height and colour; the whole chart is one image with the label.
public struct HeatingBarChart: View {
    private let bars: [(height: CGFloat, color: ColorToken)]
    private let height: CGFloat
    private let topPadding: CGFloat
    private let gap: CGFloat
    private let accessibilityText: String

    public init(
        bars: [(height: CGFloat, color: ColorToken)], height: CGFloat = 80, topPadding: CGFloat = 4,
        gap: CGFloat = 3, accessibilityLabel: String
    ) {
        self.bars = bars
        self.height = height
        self.topPadding = topPadding
        self.gap = gap
        self.accessibilityText = accessibilityLabel
    }

    public var body: some View {
        HStack(alignment: .bottom, spacing: gap) {
            ForEach(bars.indices, id: \.self) { index in
                UnevenRoundedRectangle(topLeadingRadius: 3, bottomLeadingRadius: 0, bottomTrailingRadius: 0, topTrailingRadius: 3)
                    .fill(bars[index].color.color)
                    .frame(maxWidth: .infinity)
                    .frame(height: bars[index].height)
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: height, alignment: .bottom)
        .padding(.top, topPadding)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityText)
        .accessibilityAddTraits(.isImage)
    }
}
