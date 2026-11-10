import SwiftUI

/// Row with a ticked circle, a 15/600 title over a 13 `muted` subtitle and a 13/600 trailing text
/// (IosRohbauMasse:65-73). Min height 52, padding 0 14, gap 12. A screen places it in `Card(.row, 0)`.
public struct CheckRow: View {
    private let title: String
    private let subtitle: String
    private let trailing: String
    private let fill: ColorToken
    private let ink: ColorToken
    private let trailingColor: ColorToken

    public init(
        title: String, subtitle: String, trailing: String, fill: ColorToken, ink: ColorToken,
        trailingColor: ColorToken
    ) {
        self.title = title
        self.subtitle = subtitle
        self.trailing = trailing
        self.fill = fill
        self.ink = ink
        self.trailingColor = trailingColor
    }

    public var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle().fill(fill.color)
                Icon(.checkW35, size: 11, color: ink.color)
            }
            .frame(width: 20, height: 20)
            .layoutPriority(1)
            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .interText(15, .semibold, lines: .block)
                    .foregroundStyle(Tokens.text.color)
                Text(subtitle)
                    .interText(13, .regular, lines: .block)
                    .foregroundStyle(Tokens.muted.color)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            Text(trailing)
                .interText(13, .semibold)
                .foregroundStyle(trailingColor.color)
                .fixedSize()
                .layoutPriority(1)
        }
        .padding(.horizontal, 14)
        .frame(minHeight: 52)
        .accessibilityElement(children: .combine)
    }
}
