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
    private let subtitle: String?
    private let style: Style
    private let subtitleLines: TextLines
    private let subtitleLineHeight: CGFloat?
    private let gap: CGFloat?

    /// `subtitle: nil` shows the title alone (IosElVerteiler). `subtitleLines: .greedy` wraps the subtitle like Chrome;
    /// `subtitleLineHeight` is the CSS factor (Rauchmelder: 1.4). `gap` replaces the style's gap (4, or 2 at 22).
    public init(
        _ title: String, subtitle: String?, style: Style, subtitleLines: TextLines = .single,
        subtitleLineHeight: CGFloat? = nil, gap: CGFloat? = nil
    ) {
        self.title = title
        self.subtitle = subtitle
        self.style = style
        self.subtitleLines = subtitleLines
        self.subtitleLineHeight = subtitleLineHeight
        self.gap = gap
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: gap ?? style.gap) {
            Text(title)
                .interText(style.titleSize, .bold, tracking: style.tracking)
                .foregroundStyle(Tokens.text.color)
                .accessibilityAddTraits(.isHeader)
            if let subtitle {
                subtitleView(subtitle)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private func subtitleView(_ text: String) -> some View {
        let lineHeight: LineHeight = subtitleLineHeight.map { .factor($0) } ?? .normal
        if subtitleLines == .greedy {
            GreedyLabel(
                text: text, size: style.subtitleSize, weight: .regular, color: Tokens.muted, lineHeight: lineHeight)
                .fixedSize(horizontal: false, vertical: true)
        } else {
            Text(text)
                .interText(style.subtitleSize, .regular, lineHeight: lineHeight, lines: subtitleLines)
                .foregroundStyle(Tokens.muted.color)
        }
    }
}
