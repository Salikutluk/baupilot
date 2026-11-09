import SwiftUI

/// Labelled read-only value box (IosAbnMangel:57-60): 13/600 label, a 48 pt border-box field of r12 with a
/// 1 pt border and a 16/600 value. It takes no input.
public struct DisplayField: View {
    private enum Metrics {
        static let height: CGFloat = 48
        /// 14 pt padding inside the 1 pt border.
        static let horizontalInset: CGFloat = 15
    }

    private let label: String
    private let value: String
    private let border: ColorToken
    private let labelGap: CGFloat
    private let valueIdentifier: String?

    public init(
        label: String, value: String, border: ColorToken, labelGap: CGFloat = 8,
        valueIdentifier: String? = nil
    ) {
        self.label = label
        self.value = value
        self.border = border
        self.labelGap = labelGap
        self.valueIdentifier = valueIdentifier
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: labelGap) {
            Text(label)
                .interText(13, .semibold)
                .foregroundStyle(Tokens.muted.color)
            Text(value)
                .interText(16, .semibold)
                .foregroundStyle(Tokens.text.color)
                .padding(.horizontal, Metrics.horizontalInset)
                .frame(maxWidth: .infinity, minHeight: Metrics.height, maxHeight: Metrics.height, alignment: .leading)
                .background(RoundedRectangle(cornerRadius: Tokens.Radius.field).fill(Tokens.surface.color))
                .overlay(
                    RoundedRectangle(cornerRadius: Tokens.Radius.field)
                        .strokeBorder(border.color, lineWidth: 1)
                )
                .accessibilityIdentifier(valueIdentifier ?? "")
        }
    }
}
