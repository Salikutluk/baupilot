import SwiftUI

/// Accent link inside a row (14/600 "Anleitung", IosAbnElemente:34): a 44 pt hit area whose -12 margin keeps it at the
/// line height. A nil `action` renders it and does nothing.
public struct InlineLink: View {
    private let title: String
    private let size: CGFloat
    private let action: (() -> Void)?
    private let identifier: String?

    public init(_ title: String, size: CGFloat, action: (() -> Void)?, identifier: String? = nil) {
        self.title = title
        self.size = size
        self.action = action
        self.identifier = identifier
    }

    public var body: some View {
        Button(action: action ?? {}) {
            Text(title)
                .interText(size, .semibold)
                .foregroundStyle(Tokens.accent.color)
                .frame(minHeight: 44)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .optionalIdentifier(identifier)
        .padding(.vertical, -12)
    }
}
