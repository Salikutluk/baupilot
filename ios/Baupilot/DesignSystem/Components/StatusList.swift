import SwiftUI

/// Rows of name, small note and a status badge on a tinted r12 container (IosElVerteiler:50-56). Row min-height 48,
/// padding 0 12, gap 10, 15/600 over 12 `muted` (gap 1); a 1 pt divider sits on top of every row after the first.
public struct StatusList: View {
    public struct Row {
        public let title: String
        public let subtitle: String
        public let badge: StatusBadge

        public init(title: String, subtitle: String, badge: StatusBadge) {
            self.title = title
            self.subtitle = subtitle
            self.badge = badge
        }
    }

    private let rows: [Row]
    private let background: ColorToken
    private let divider: ColorToken

    public init(rows: [Row], background: ColorToken, divider: ColorToken) {
        self.rows = rows
        self.background = background
        self.divider = divider
    }

    public var body: some View {
        VStack(spacing: 0) {
            ForEach(Array(rows.enumerated()), id: \.offset) { index, row in
                if index > 0 {
                    Rectangle().fill(divider.color).frame(height: 1)
                }
                HStack(spacing: 10) {
                    VStack(alignment: .leading, spacing: 1) {
                        Text(row.title)
                            .interText(15, .semibold)
                            .foregroundStyle(Tokens.text.color)
                        Text(row.subtitle)
                            .interText(12, .regular)
                            .foregroundStyle(Tokens.muted.color)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    row.badge
                }
                .padding(.horizontal, 12)
                .frame(minHeight: 48)
                .accessibilityElement(children: .combine)
            }
        }
        .frame(maxWidth: .infinity)
        .background(background.color)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
