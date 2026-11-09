import SwiftUI

/// Title with a muted subtitle under a back bar: 30 pt on Abnahme Elemente, 28 pt on Vergleich, 22 pt on the document viewer.
public struct DetailTitle: View {
    public enum Style {
        case size30
        case size28
        case size22

        fileprivate var titleSize: CGFloat {
            switch self {
            case .size30: return 30
            case .size28: return 28
            case .size22: return 22
            }
        }

        fileprivate var tracking: CGFloat {
            switch self {
            case .size30: return -0.6
            case .size28: return -0.5
            case .size22: return -0.3
            }
        }

        fileprivate var subtitleSize: CGFloat { self == .size22 ? 13 : 14 }
        fileprivate var gap: CGFloat { self == .size22 ? 2 : 4 }
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
