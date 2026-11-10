import SwiftUI

/// A captioned 48 pt reading box (IosSanDruck:55-65): the label (12/600 `muted`) over a box (r12, 16/700) with gap 6.
/// `.value` is a `chip` fill with inset 14; `.input` is transparent with a 1 pt `accent` border (CSS 1.5, measured 1 pt)
/// and inset 15, its placeholder in `Tokens.placeholder`.
public struct ReadingField: View {
    public enum Content {
        case value(String)
        case input(text: Binding<String>, placeholder: String)
    }

    private enum Metrics {
        static let boxHeight: CGFloat = 48
        static let radius: CGFloat = 12
    }

    private let label: String
    private let content: Content
    private let identifier: String?

    public init(label: String, content: Content, identifier: String?) {
        self.label = label
        self.content = content
        self.identifier = identifier
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .interText(12, .semibold)
                .foregroundStyle(Tokens.muted.color)
            box
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private var box: some View {
        switch content {
        case .value(let value):
            Text(value)
                .interText(16, .bold)
                .foregroundStyle(Tokens.text.color)
                .padding(.horizontal, 14)
                .frame(maxWidth: .infinity, minHeight: Metrics.boxHeight, maxHeight: Metrics.boxHeight, alignment: .leading)
                .background(RoundedRectangle(cornerRadius: Metrics.radius).fill(Tokens.chip.core.color))
                .identified(identifier)
        case .input(let text, let placeholder):
            TextField(
                "", text: text,
                prompt: Text(placeholder).font(.inter(16, .bold)).foregroundStyle(Tokens.placeholder.color)
            )
            .font(.inter(16, .bold))
            .foregroundStyle(Tokens.text.color)
            .padding(.horizontal, 15)
            .frame(maxWidth: .infinity, minHeight: Metrics.boxHeight, maxHeight: Metrics.boxHeight, alignment: .leading)
            .overlay(
                RoundedRectangle(cornerRadius: Metrics.radius).strokeBorder(Tokens.accent.color, lineWidth: 1)
            )
            .accessibilityLabel(label)
            .identified(identifier)
        }
    }
}

private extension View {
    @ViewBuilder
    func identified(_ identifier: String?) -> some View {
        if let identifier { accessibilityIdentifier(identifier) } else { self }
    }
}
