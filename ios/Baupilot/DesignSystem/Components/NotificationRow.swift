import SwiftUI

/// Mitteilungen row: icon tile, title with time, two-line-capable body and an unread dot (IosMitteilungen:33).
/// Place rows inside a `Card(padding: EdgeInsets())`; the divider sits on top of every row that is not the first.
/// With an `action` the whole row is the button; without one it is a plain view that VoiceOver still reaches.
public struct NotificationRow: View {
    struct Tile: View {
        let icon: BaupilotIcon
        let background: ColorToken
        let foreground: ColorToken

        var body: some View {
            Icon(icon, size: 18, color: foreground.color)
                .frame(width: 34, height: 34)
                .background(background.color, in: RoundedRectangle(cornerRadius: 10))
        }
    }

    private let icon: BaupilotIcon
    private let iconBackground: ColorToken
    private let iconForeground: ColorToken
    private let title: String
    private let when: String
    private let text: String
    private let unread: Bool
    private let showsDivider: Bool
    private let action: (() -> Void)?

    public init(
        icon: BaupilotIcon, iconBackground: ColorToken, iconForeground: ColorToken,
        title: String, when: String, text: String, unread: Bool, showsDivider: Bool,
        action: (() -> Void)?
    ) {
        self.icon = icon
        self.iconBackground = iconBackground
        self.iconForeground = iconForeground
        self.title = title
        self.when = when
        self.text = text
        self.unread = unread
        self.showsDivider = showsDivider
        self.action = action
    }

    public var body: some View {
        VStack(spacing: 0) {
            if showsDivider {
                Rectangle().fill(Tokens.border.core.color).frame(height: 1)
            }
            if let action {
                Button(action: action) { row }.buttonStyle(.plain)
            } else {
                row.accessibilityElement(children: .combine)
            }
        }
    }

    private var row: some View {
        HStack(alignment: .top, spacing: 12) {
            Tile(icon: icon, background: iconBackground, foreground: iconForeground)
            VStack(alignment: .leading, spacing: 3) {
                HStack(alignment: .top, spacing: 8) {
                    Text(title)
                        .interText(15, unread ? .bold : .semibold, lines: .block)
                        .foregroundStyle(Tokens.text.color)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    Text(when)
                        .interText(13, .regular)
                        .foregroundStyle(Tokens.muted.color)
                        .fixedSize()
                }
                GreedyLabel(text: text, size: 14, weight: .regular, color: Tokens.muted, lineHeight: .factor(1.35))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            Circle()
                .fill(unread ? Tokens.accent.color : Color.clear)
                .frame(width: 8, height: 8)
                .padding(.top, 6)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .contentShape(Rectangle())
    }
}
