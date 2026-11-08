import SwiftUI

/// 44 pt search input with a leading magnifier.
public struct SearchField: View {
    @Binding private var text: String
    private let placeholder: String
    private let accessibilityLabel: String
    private let background: ColorToken

    public init(
        text: Binding<String>,
        placeholder: String,
        accessibilityLabel: String,
        background: ColorToken = Tokens.chip.core
    ) {
        self._text = text
        self.placeholder = placeholder
        self.accessibilityLabel = accessibilityLabel
        self.background = background
    }

    public var body: some View {
        HStack(spacing: 8) {
            Icon(.searchW22, size: 18, color: Tokens.muted.color)
            TextField(
                "", text: $text,
                prompt: Text(placeholder)
                    .font(.inter(16, .regular))
                    .foregroundStyle(Tokens.placeholder.color)
            )
            .font(.inter(16, .regular))
            .foregroundStyle(Tokens.text.color)
            .accessibilityLabel(accessibilityLabel)
        }
        .padding(.horizontal, 12)
        .frame(height: 44)
        .background(background.color, in: RoundedRectangle(cornerRadius: 12))
    }
}
