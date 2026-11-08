import SwiftUI

/// Home document tile: icon, label and count in a 52 pt surface tile.
public struct DocumentTile: View {
    private enum Metrics {
        static let height: CGFloat = 52
        static let radius: CGFloat = 14
        static let horizontal: CGFloat = 14
        static let gap: CGFloat = 10
        static let iconSize: CGFloat = 20
    }

    private let icon: BaupilotIcon
    private let label: String
    private let count: String
    private let action: (() -> Void)?

    public init(icon: BaupilotIcon, label: String, count: String, action: (() -> Void)?) {
        self.icon = icon
        self.label = label
        self.count = count
        self.action = action
    }

    private var tile: some View {
        HStack(spacing: Metrics.gap) {
            Icon(icon, size: Metrics.iconSize, color: Tokens.accent.color)
            HStack(alignment: .firstTextBaseline) {
                Text(label)
                    .interText(15, .semibold)
                    .foregroundStyle(Tokens.text.color)
                Spacer(minLength: 0)
                Text(count)
                    .interText(15, .semibold)
                    .foregroundStyle(Tokens.muted.color)
            }
        }
        .padding(.horizontal, Metrics.horizontal)
        .frame(maxWidth: .infinity, minHeight: Metrics.height, maxHeight: Metrics.height, alignment: .leading)
        .background(Tokens.surface.color, in: RoundedRectangle(cornerRadius: Metrics.radius))
        .contentShape(RoundedRectangle(cornerRadius: Metrics.radius))
    }

    public var body: some View {
        if let action {
            Button(action: action) { tile }.buttonStyle(.plain)
        } else {
            tile
        }
    }
}
