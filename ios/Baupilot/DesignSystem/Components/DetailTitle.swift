import SwiftUI

/// Title with a muted subtitle under a back bar: 28 pt on Vergleich, 22 pt on the document viewer.
public struct DetailTitle: View {
    public enum Style {
        case size28
        case size22

        fileprivate var titleSize: CGFloat { self == .size28 ? 28 : 22 }
        fileprivate var tracking: CGFloat { self == .size28 ? -0.5 : -0.3 }
        fileprivate var subtitleSize: CGFloat { self == .size28 ? 14 : 13 }
        fileprivate var gap: CGFloat { self == .size28 ? 4 : 2 }
    }

    private let title: String
    private let subtitle: String
    private let style: Style

    public init(_ title: String, subtitle: String, style: Style) {
        self.title = title
        self.subtitle = subtitle
        self.style = style
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: style.gap) {
            Text(title)
                .interText(style.titleSize, .bold, tracking: style.tracking)
                .foregroundStyle(Tokens.text.color)
                .accessibilityAddTraits(.isHeader)
            Text(subtitle)
                .interText(style.subtitleSize, .regular)
                .foregroundStyle(Tokens.muted.color)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
