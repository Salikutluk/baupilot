import SwiftUI

/// Labelled 48 pt text input. The border colour is a parameter so a screen can pass its own token.
public struct FormField: View {
    public enum Kind { case email, password }

    /// Trailing 13/600 accent link beside the label. A nil `action` renders it and does nothing.
    public struct Link {
        public let title: String
        public let action: (() -> Void)?

        public init(title: String, action: (() -> Void)?) {
            self.title = title
            self.action = action
        }
    }

    private enum Metrics {
        static let fieldHeight: CGFloat = 48
        static let borderWidth: CGFloat = 1
        /// 14 pt padding inside the 1 pt border of the border-box input.
        static let horizontalInset: CGFloat = 15
        static let linkHitHeight: CGFloat = 44
    }

    private let label: String
    @Binding private var text: String
    private let placeholder: String
    private let kind: Kind
    private let border: ColorToken
    private let trailingLink: Link?

    public init(
        label: String, text: Binding<String>, placeholder: String, kind: Kind,
        border: ColorToken = Tokens.border.core, trailingLink: Link? = nil
    ) {
        self.label = label
        self._text = text
        self.placeholder = placeholder
        self.kind = kind
        self.border = border
        self.trailingLink = trailingLink
    }

    private var prompt: Text {
        Text(placeholder)
            .font(.inter(16, .regular))
            .foregroundStyle(Tokens.placeholder.color)
    }

    @ViewBuilder
    private var input: some View {
        switch kind {
        case .email:
            TextField("", text: $text, prompt: prompt)
                .keyboardType(.emailAddress)
                .textContentType(.emailAddress)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
        case .password:
            SecureField("", text: $text, prompt: prompt)
                .textContentType(.password)
        }
    }

    private var header: some View {
        HStack(spacing: 0) {
            Text(label)
                .interText(13, .semibold)
                .foregroundStyle(Tokens.muted.color)
            Spacer(minLength: 0)
            if let trailingLink {
                // 44 pt hit area; the -14 margins keep it at the label's 16 pt line height.
                Button(action: trailingLink.action ?? {}) {
                    Text(trailingLink.title)
                        .interText(13, .semibold)
                        .foregroundStyle(Tokens.accent.color)
                        .frame(minHeight: Metrics.linkHitHeight)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .padding(.vertical, -14)
            }
        }
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            header
            input
                .font(.inter(16, .regular))
                .foregroundStyle(Tokens.text.color)
                .padding(.horizontal, Metrics.horizontalInset)
                .frame(height: Metrics.fieldHeight)

                .background(
                    RoundedRectangle(cornerRadius: Tokens.Radius.field).fill(Tokens.surface.color)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: Tokens.Radius.field)
                        .strokeBorder(border.color, lineWidth: Metrics.borderWidth)
                )
                .accessibilityLabel(label)
        }
    }
}
