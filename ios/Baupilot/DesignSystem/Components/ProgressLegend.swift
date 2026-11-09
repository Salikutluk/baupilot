import SwiftUI

/// Dot legend under a progress bar (IosAbnElemente:32-36): dot 8 (radius 4), gap 6, items 14 apart, 13/400 `muted`.
public struct ProgressLegend: View {
    public struct Item {
        public let label: String
        public let color: ColorToken

        public init(label: String, color: ColorToken) {
            self.label = label
            self.color = color
        }
    }

    private let items: [Item]

    public init(items: [Item]) {
        self.items = items
    }

    public var body: some View {
        HStack(spacing: 14) {
            ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                HStack(spacing: 6) {
                    Circle().fill(item.color.color).frame(width: 8, height: 8)
                    Text(item.label)
                        .interText(13, .regular)
                        .foregroundStyle(Tokens.muted.color)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
