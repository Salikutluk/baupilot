import SwiftUI

/// Row used in the iOS lists. Place rows inside a `Card(padding: EdgeInsets())`; the divider
/// sits on top of every row that is not the first. An optional `background` is painted under the whole
/// row, divider included. `subtitleLines: .block` lets a long subtitle wrap inside the flex column (CSS
/// `min-width: 0`) instead of truncating; `.greedy` wraps it through `GreedyLabel` (Chrome's breaks). `titleLines`
/// does the same for the title, and `textGap` replaces the density's gap between title and subtitle.
public struct ListRow: View {
    public enum TitleSize {
        case regular
        case large

        var fontSize: CGFloat { self == .large ? 16 : 15 }
    }

    public enum Density: Equatable {
        case padded(v: CGFloat)
        case minHeight(CGFloat)

        var textGap: CGFloat {
            switch self {
            case .padded: return 3
            case .minHeight: return 2
            }
        }
    }

    public enum TileSize {
        case s38
        case s34r9
        case s34r10
        case s36r10

        var side: CGFloat {
            switch self {
            case .s38: return 38
            case .s34r9, .s34r10: return 34
            case .s36r10: return 36
            }
        }

        var radius: CGFloat { self == .s34r9 ? 9 : 10 }
    }

    public enum TextTileSize {
        case s48x40
        case s36

        var width: CGFloat { self == .s48x40 ? 48 : 36 }
        var height: CGFloat { self == .s48x40 ? 40 : 36 }
        var radius: CGFloat { self == .s48x40 ? 12 : 10 }
        var fontSize: CGFloat { self == .s48x40 ? 12 : 13 }
    }

    public enum Leading {
        case none
        case iconTile(
            BaupilotIcon, size: TileSize = .s38, bg: ColorToken = Tokens.chip.core,
            fg: ColorToken = Tokens.muted)
        case textTile(String, TextTileSize, chip: ColorToken = Tokens.chip.core)
        /// A paint swatch: `side` fill plus a 1 pt border outside it (CSS content-box), so 16 outer for 14.
        case swatch(ColorToken, side: CGFloat, radius: CGFloat, border: ColorToken)
    }

    public enum ChevronSize {
        case small
        case regular

        var icon: BaupilotIcon { self == .small ? .chevronRightW24 : .chevronRightW22 }
        var side: CGFloat { self == .small ? 14 : 16 }
    }

    public enum Trailing {
        case none
        case badge(StatusBadge)
        case chevron(ChevronSize)
        case badgeAndChevron(StatusBadge, ChevronSize)
        case value(String, InterWeight)
        case valueAndChevron(String, InterWeight, ChevronSize)
        /// Amount over an optional badge, trailing aligned, gap 4.
        case amount(String, size: CGFloat, weight: InterWeight, color: ColorToken, badge: StatusBadge? = nil)
    }

    struct LeadingView: View {
        let leading: Leading

        var body: some View {
            switch leading {
            case .none:
                EmptyView()
            case .iconTile(let icon, let size, let bg, let fg):
                Icon(icon, size: 18, color: fg.color)
                    .frame(width: size.side, height: size.side)
                    .background(bg.color, in: RoundedRectangle(cornerRadius: size.radius))
            case .textTile(let text, let size, let chip):
                Text(text)
                    .interText(size.fontSize, .bold)
                    .foregroundStyle(Tokens.muted.color)
                    .lineLimit(1)
                    .fixedSize()
                    .frame(width: size.width, height: size.height)
                    .background(
                        chip.color,
                        in: RoundedRectangle(cornerRadius: size.radius))
            case .swatch(let fill, let side, let radius, let border):
                RoundedRectangle(cornerRadius: radius)
                    .fill(fill.color)
                    .overlay { RoundedRectangle(cornerRadius: radius).strokeBorder(border.color, lineWidth: 1) }
                    .frame(width: side + 2, height: side + 2)
            }
        }
    }

    private let title: String
    private let subtitle: String?
    private let leading: Leading
    private let trailing: Trailing
    private let titleSize: TitleSize
    private let density: Density
    private let showsDivider: Bool
    let dividerColor: ColorToken
    let background: ColorToken?
    let subtitleLines: TextLines
    let textGap: CGFloat?
    let titleLines: TextLines
    private let action: (() -> Void)?

    public init(
        title: String,
        subtitle: String? = nil,
        leading: Leading = .none,
        trailing: Trailing = .none,
        titleSize: TitleSize = .regular,
        density: Density = .padded(v: 12),
        showsDivider: Bool,
        dividerColor: ColorToken = Tokens.border.core,
        background: ColorToken? = nil,
        subtitleLines: TextLines = .single,
        textGap: CGFloat? = nil,
        titleLines: TextLines = .single,
        action: (() -> Void)?
    ) {
        self.title = title
        self.subtitle = subtitle
        self.leading = leading
        self.trailing = trailing
        self.titleSize = titleSize
        self.density = density
        self.showsDivider = showsDivider
        self.dividerColor = dividerColor
        self.background = background
        self.subtitleLines = subtitleLines
        self.textGap = textGap
        self.titleLines = titleLines
        self.action = action
    }

    public var body: some View {
        VStack(spacing: 0) {
            if showsDivider {
                Rectangle().fill(dividerColor.color).frame(height: 1)
            }
            if let action {
                Button(action: action) { row }.buttonStyle(.plain)
            } else {
                row
            }
        }
        // CSS paints the anchor's background under its own top border, so the divider is tinted too.
        .background { if let background { background.color } }
    }

    @ViewBuilder
    private var row: some View {
        let content = HStack(spacing: 12) {
            LeadingView(leading: leading)
            VStack(alignment: .leading, spacing: textGap ?? density.textGap) {
                titleView
                if let subtitle {
                    subtitleView(subtitle)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            trailingView
        }
        .padding(.horizontal, 14)
        switch density {
        case .padded(let v):
            content.padding(.vertical, v)
        case .minHeight(let h):
            content.frame(minHeight: h)
        }
    }

    /// A `.greedy` subtitle breaks like Chrome through `GreedyLabel`; the other modes go through `interText`.
    @ViewBuilder
    private func subtitleView(_ text: String) -> some View {
        if subtitleLines == .greedy {
            GreedyLabel(text: text, size: 13, weight: .regular, color: Tokens.muted, lineHeight: .normal)
        } else {
            Text(text)
                .interText(13, .regular, lines: subtitleLines)
                .foregroundStyle(Tokens.muted.color)
        }
    }

    @ViewBuilder
    private var titleView: some View {
        if titleLines == .greedy {
            GreedyLabel(
                text: title, size: titleSize.fontSize, weight: .semibold, color: Tokens.text, lineHeight: .normal)
        } else {
            Text(title)
                .interText(titleSize.fontSize, .semibold, lines: titleLines)
                .foregroundStyle(Tokens.text.color)
        }
    }

    @ViewBuilder
    private var trailingView: some View {
        switch trailing {
        case .none:
            EmptyView()
        case .badge(let badge):
            badge
        case .chevron(let size):
            chevron(size)
        case .badgeAndChevron(let badge, let size):
            badge
            chevron(size)
        case .value(let text, let weight):
            value(text, weight)
        case .valueAndChevron(let text, let weight, let size):
            value(text, weight)
            chevron(size)
        case .amount(let text, let size, let weight, let color, let badge):
            VStack(alignment: .trailing, spacing: 4) {
                Text(text)
                    .interText(size, weight)
                    .foregroundStyle(color.color)
                    .lineLimit(1)
                    .fixedSize()
                if let badge {
                    badge
                }
            }
        }
    }

    private func chevron(_ size: ChevronSize) -> some View {
        Icon(size.icon, size: size.side, color: Tokens.muted.color)
    }

    private func value(_ text: String, _ weight: InterWeight) -> some View {
        Text(text)
            .interText(13, weight)
            .foregroundStyle(Tokens.muted.color)
            .lineLimit(1)
            .fixedSize()
    }
}
