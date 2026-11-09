import SwiftUI

/// Link row with a leading icon, a title, a muted count and a chevron (IosAbnMaengel:47-54): min-height 48, gap 10,
/// icon 18, title 15/400, count 15/400, chevron 14. Place it in `Card(style: .row)` with padding 0 14. A nil `action`
/// renders it and does nothing.
public struct CountLinkRow: View {
    private let icon: BaupilotIcon
    private let iconColor: ColorToken
    private let title: String
    private let count: String
    private let action: (() -> Void)?

    public init(icon: BaupilotIcon, iconColor: ColorToken, title: String, count: String, action: (() -> Void)?) {
        self.icon = icon
        self.iconColor = iconColor
        self.title = title
        self.count = count
        self.action = action
    }

    public var body: some View {
        Button(action: action ?? {}) {
            HStack(spacing: 10) {
                Icon(icon, size: 18, color: iconColor.color)
                Text(title)
                    .interText(15, .regular)
                    .foregroundStyle(Tokens.text.color)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text(count)
                    .interText(15, .regular)
                    .foregroundStyle(Tokens.muted.color)
                Icon(.chevronRightW24, size: 14, color: Tokens.muted.color)
            }
            .frame(minHeight: 48)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
    }
}
