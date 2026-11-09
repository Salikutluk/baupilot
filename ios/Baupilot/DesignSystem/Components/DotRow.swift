import SwiftUI

/// Row with a status dot, a 15/600 title over a 13 muted subtitle and a 13/600 muted trailing text
/// (IosAbnSignatur:40-49). Place rows inside a `Card(style: .row, padding: EdgeInsets())`; the divider sits on top
/// of every row that is not the first.
public struct DotRow: View {
    private let dot: ColorToken
    private let title: String
    private let subtitle: String
    private let trailing: String
    private let showsDivider: Bool
    private let divider: ColorToken

    public init(
        dot: ColorToken, title: String, subtitle: String, trailing: String,
        showsDivider: Bool = false, divider: ColorToken = Tokens.border.core
    ) {
        self.dot = dot
        self.title = title
        self.subtitle = subtitle
        self.trailing = trailing
        self.showsDivider = showsDivider
        self.divider = divider
    }

    public var body: some View {
        VStack(spacing: 0) {
            if showsDivider {
                Rectangle().fill(divider.color).frame(height: 1)
            }
            HStack(spacing: 10) {
                Circle()
                    .fill(dot.color)
                    .frame(width: 8, height: 8)
                VStack(alignment: .leading, spacing: 1) {
                    Text(title)
                        .interText(15, .semibold)
                        .foregroundStyle(Tokens.text.color)
                    Text(subtitle)
                        .interText(13, .regular)
                        .foregroundStyle(Tokens.muted.color)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                Text(trailing)
                    .interText(13, .semibold)
                    .foregroundStyle(Tokens.muted.color)
            }
            .padding(.horizontal, 14)
            .frame(minHeight: 52)
        }
        .accessibilityElement(children: .combine)
    }
}
