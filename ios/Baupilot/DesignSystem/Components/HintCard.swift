import SwiftUI

/// Tappable hint on a list screen: icon tile, title with subtitle and a trailing amount, on a tinted card.
public struct HintCard: View {
    private enum Metrics {
        static let radius: CGFloat = 16
        static let vertical: CGFloat = 12
        static let horizontal: CGFloat = 14
        static let gap: CGFloat = 12
        static let textGap: CGFloat = 2
    }

    /// The 36 pt rounded tile that holds the 18 pt icon.
    struct Tile: View {
        let icon: BaupilotIcon
        let background: ColorToken
        let foreground: ColorToken

        var body: some View {
            Icon(icon, size: 18, color: foreground.color)
                .frame(width: 36, height: 36)
                .background(RoundedRectangle(cornerRadius: 10).fill(background.color))
        }
    }

    private let icon: BaupilotIcon
    private let title: String
    private let subtitle: String
    private let amount: String
    private let background: ColorToken
    private let tileBackground: ColorToken
    private let tileForeground: ColorToken
    private let subtitleColor: ColorToken
    private let action: (() -> Void)?

    public init(
        icon: BaupilotIcon, title: String, subtitle: String, amount: String, background: ColorToken,
        tileBackground: ColorToken, tileForeground: ColorToken, subtitleColor: ColorToken,
        action: (() -> Void)?
    ) {
        self.icon = icon
        self.title = title
        self.subtitle = subtitle
        self.amount = amount
        self.background = background
        self.tileBackground = tileBackground
        self.tileForeground = tileForeground
        self.subtitleColor = subtitleColor
        self.action = action
    }

    private var card: some View {
        HStack(spacing: Metrics.gap) {
            Tile(icon: icon, background: tileBackground, foreground: tileForeground)
            VStack(alignment: .leading, spacing: Metrics.textGap) {
                Text(title)
                    .interText(15, .semibold)
                    .foregroundStyle(Tokens.text.color)
                Text(subtitle)
                    .interText(13, .regular)
                    .foregroundStyle(subtitleColor.color)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            Text(amount)
                .interText(15, .bold)
                .foregroundStyle(Tokens.text.color)
                .fixedSize()
        }
        .padding(.horizontal, Metrics.horizontal)
        .padding(.vertical, Metrics.vertical)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(background.color, in: RoundedRectangle(cornerRadius: Metrics.radius))
        .contentShape(RoundedRectangle(cornerRadius: Metrics.radius))
    }

    public var body: some View {
        if let action {
            Button(action: action) { card }
                .buttonStyle(.plain)
                .accessibilityElement(children: .combine)
        } else {
            card.accessibilityElement(children: .combine)
        }
    }
}
