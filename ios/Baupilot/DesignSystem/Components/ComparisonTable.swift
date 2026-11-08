import SwiftUI

/// Offer comparison card: a header with the two column names and one row per fact (IosVergleich:32).
/// The widths come in as `ComparisonLayout.Widths`; `highlighted` comes in per row from the model.
public struct ComparisonTable: View {
    public struct Column {
        public let name: String
        public let badge: String?
        public let action: (() -> Void)?
        public let identifier: String?

        public init(name: String, badge: String?, action: (() -> Void)?, identifier: String? = nil) {
            self.name = name
            self.badge = badge
            self.action = action
            self.identifier = identifier
        }
    }

    public struct Row {
        public let label: String
        public let a: String
        public let b: String
        public let strong: Bool
        public let highlighted: Bool

        public init(label: String, a: String, b: String, strong: Bool, highlighted: Bool) {
            self.label = label
            self.a = a
            self.b = b
            self.strong = strong
            self.highlighted = highlighted
        }
    }

    /// Grows a column header's hit area above and below its content so it reaches 44 pt.
    static let hitExpansion: CGFloat = 14

    struct HeaderCell: View {
        let column: Column
        let width: CGFloat

        var body: some View {
            if let action = column.action {
                Button(action: action) { content }
                    .buttonStyle(.plain)
                    .accessibilityIdentifier(column.identifier ?? "")
            } else if let identifier = column.identifier {
                content
                    .accessibilityElement(children: .combine)
                    .accessibilityIdentifier(identifier)
            } else {
                content
            }
        }

        private var content: some View {
            VStack(alignment: .leading, spacing: 4) {
                if let badge = column.badge {
                    StatusBadge(badge, tone: .ok, size: .micro, weight: .bold)
                }
                Text(column.name)
                    .interText(14, .bold, lineHeight: .factor(1.25), lines: .block)
                    .foregroundStyle(Tokens.text.color)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .frame(width: width, alignment: .leading)
            .padding(.vertical, ComparisonTable.hitExpansion)
            .contentShape(Rectangle())
            .padding(.vertical, -ComparisonTable.hitExpansion)
        }
    }

    struct RowView: View {
        let row: Row
        let widths: ComparisonLayout.Widths
        let minHeight: CGFloat
        let highlight: ColorToken

        var body: some View {
            VStack(spacing: 0) {
                Rectangle().fill(Tokens.border.core.color).frame(height: 1)
                HStack(spacing: 10) {
                    Text(row.label)
                        .interText(13, .regular)
                        .foregroundStyle(Tokens.muted.color)
                        .frame(width: widths.label, alignment: .leading)
                    value(row.a)
                    value(row.b)
                }
                .padding(.horizontal, 14)
                .frame(minHeight: minHeight)
                .background(row.highlighted ? highlight.color : Color.clear, in: Rectangle())
            }
            .accessibilityElement(children: .combine)
        }

        private func value(_ text: String) -> some View {
            Text(text)
                .interText(14, row.strong ? .bold : .medium)
                .foregroundStyle(Tokens.text.color)
                // A 17 pt line box centred in 40 sits at 11.5; Chrome paints it at 12. 1 pt of padding makes the
                // box 18 tall, which centres at 11 and moves the glyphs down by that half point.
                .padding(.top, 1)
                .frame(width: widths.value, alignment: .leading)
        }
    }

    private let columns: [Column]
    private let rows: [Row]
    private let widths: ComparisonLayout.Widths
    private let rowMinHeight: CGFloat
    private let highlight: ColorToken
    private let accessibilityLabel: String

    public init(
        columns: [Column], rows: [Row], widths: ComparisonLayout.Widths, rowMinHeight: CGFloat = 40,
        highlight: ColorToken = Tokens.diff, accessibilityLabel: String
    ) {
        self.columns = columns
        self.rows = rows
        self.widths = widths
        self.rowMinHeight = rowMinHeight
        self.highlight = highlight
        self.accessibilityLabel = accessibilityLabel
    }

    public var body: some View {
        let shape = RoundedRectangle(cornerRadius: 16)
        VStack(spacing: 0) {
            HStack(alignment: .bottom, spacing: 10) {
                Color.clear.frame(width: widths.label, height: 0)
                ForEach(Array(columns.enumerated()), id: \.offset) { _, column in
                    HeaderCell(column: column, width: widths.value)
                }
            }
            .padding(EdgeInsets(top: 12, leading: 14, bottom: 10, trailing: 14))
            ForEach(Array(rows.enumerated()), id: \.offset) { _, row in
                RowView(row: row, widths: widths, minHeight: rowMinHeight, highlight: highlight)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Tokens.surface.color, in: shape)
        .clipShape(shape)
        .accessibilityElement(children: .contain)
        .accessibilityLabel(accessibilityLabel)
    }
}
