import SwiftUI

/// Ordered list of instructions (IosElVerteiler:45): `padding-left: 20`, 15 x 1.45, gap 4. The "N." marker is
/// right-aligned so it ends at the indent; the item text wraps greedily inside the hanging indent.
public struct NumberedList: View {
    private let items: [String]
    private let size: CGFloat
    private let lineHeight: CGFloat
    private let color: ColorToken
    private let gap: CGFloat
    private let indent: CGFloat

    public init(
        items: [String], size: CGFloat = 15, lineHeight: CGFloat = 1.45, color: ColorToken, gap: CGFloat = 4,
        indent: CGFloat = 20
    ) {
        self.items = items
        self.size = size
        self.lineHeight = lineHeight
        self.color = color
        self.gap = gap
        self.indent = indent
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: gap) {
            ForEach(Array(items.enumerated()), id: \.offset) { index, item in
                HStack(alignment: .top, spacing: 0) {
                    Text("\(index + 1).\u{00A0}")
                        .interText(size, .regular, lineHeight: .factor(lineHeight))
                        .foregroundStyle(color.color)
                        .fixedSize()
                        .frame(width: indent, alignment: .trailing)
                    GreedyLabel(
                        text: item, size: size, weight: .regular, color: color, lineHeight: .factor(lineHeight))
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
        }
        // A finite height proposal from a parent stack would clip the representable labels to it.
        .fixedSize(horizontal: false, vertical: true)
    }
}
