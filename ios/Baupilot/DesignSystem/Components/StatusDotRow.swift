import SwiftUI

/// Row with a status dot, a 15/400 name and a 13/600 status (IosElRauchmelder:59-64). Min-height 46, padding 0 14,
/// gap 12, dot 8. The divider (1 pt) sits on top of every row but the first. A screen places rows in `Card(.row, 0)`.
public struct StatusDotRow: View {
    private let dot: ColorToken
    private let title: String
    private let status: String
    private let statusColor: ColorToken
    private let showsDivider: Bool
    private let divider: ColorToken

    public init(
        dot: ColorToken, title: String, status: String, statusColor: ColorToken, showsDivider: Bool,
        divider: ColorToken
    ) {
        self.dot = dot
        self.title = title
        self.status = status
        self.statusColor = statusColor
        self.showsDivider = showsDivider
        self.divider = divider
    }

    public var body: some View {
        VStack(spacing: 0) {
            if showsDivider {
                Rectangle().fill(divider.color).frame(height: 1)
            }
            HStack(spacing: 12) {
                Circle().fill(dot.color).frame(width: 8, height: 8)
                Text(title)
                    .interText(15, .regular)
                    .foregroundStyle(Tokens.text.color)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text(status)
                    .interText(13, .semibold)
                    .foregroundStyle(statusColor.color)
                    .fixedSize()
            }
            .padding(.horizontal, 14)
            .frame(minHeight: 46)
        }
        .accessibilityElement(children: .combine)
    }
}
