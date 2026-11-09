import SwiftUI

/// Labelled multi-line input (IosAbnMangel:33-36): 13/600 label, 6 pt gap, a border-box field of r12 with a
/// 1 pt border, padding 12 14, 16/400 text at line-height 1.4. Three rows are 93.2 tall in the CSS arithmetic, 93.0 as rendered.
public struct TextAreaField: View {
    private enum Metrics {
        static let borderWidth: CGFloat = 1
        static let verticalPadding: CGFloat = 12
        /// 14 pt padding inside the 1 pt border.
        static let horizontalInset: CGFloat = 15
        static let fontSize: CGFloat = 16
        static let lineHeight: CGFloat = fontSize * 1.4
    }

    /// Height of the box including its border for `rows` rows of text. Chrome rounds the text block of a
    /// textarea to whole points (3 × 22.4 = 67.2 renders 67), so the reference box is 93.0, not 93.2.
    static func boxHeight(rows: Int) -> CGFloat {
        2 * Metrics.borderWidth + 2 * Metrics.verticalPadding + (CGFloat(rows) * Metrics.lineHeight).rounded()
    }

    private static var leading: CGFloat { max(0, Metrics.lineHeight - InterMetrics.naturalHeight(Metrics.fontSize)) }
    /// The CSS line box is 20 (Inter 16), the leading above the glyphs is (22.4 − 20) / 2.
    private var halfLeading: CGFloat { (Metrics.lineHeight - InterMetrics.lineBox(Metrics.fontSize)) / 2 }

    private let label: String
    @Binding private var text: String
    private let rows: Int
    private let border: ColorToken
    private let accessibilityLabel: String

    public init(
        label: String, text: Binding<String>, rows: Int = 3, border: ColorToken,
        accessibilityLabel: String
    ) {
        self.label = label
        self._text = text
        self.rows = rows
        self.border = border
        self.accessibilityLabel = accessibilityLabel
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .interText(13, .semibold)
                .foregroundStyle(Tokens.muted.color)
            TextField("", text: $text, axis: .vertical)
                .font(.inter(Metrics.fontSize, .regular))
                .lineSpacing(Self.leading)
                .lineLimit(rows, reservesSpace: true)
                .foregroundStyle(Tokens.text.color)
                .padding(.horizontal, Metrics.horizontalInset - Metrics.borderWidth)
                // CSS puts half of the line's leading above the first line; SwiftUI adds it below.
                .padding(.top, Metrics.verticalPadding + halfLeading)
                .padding(.bottom, Metrics.verticalPadding - halfLeading)
                .padding(Metrics.borderWidth)
                .frame(height: Self.boxHeight(rows: rows), alignment: .top)
                .background(RoundedRectangle(cornerRadius: Tokens.Radius.field).fill(Tokens.surface.color))
                .overlay(
                    RoundedRectangle(cornerRadius: Tokens.Radius.field)
                        .strokeBorder(border.color, lineWidth: Metrics.borderWidth)
                )
                .accessibilityLabel(accessibilityLabel)
        }
    }
}
