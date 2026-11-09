import SwiftUI

/// Borderless row with a title, a muted subtitle and an accent action word (IosVergleich:57).
/// With an `action` the whole row is the button; without one it renders the same and does nothing.
public struct ActionRow: View {
    private let title: String
    private let subtitle: String
    private let actionTitle: String
    private let action: (() -> Void)?

    public init(title: String, subtitle: String, actionTitle: String, action: (() -> Void)?) {
        self.title = title
        self.subtitle = subtitle
        self.actionTitle = actionTitle
        self.action = action
    }

    public var body: some View {
        if let action {
            Button(action: action) { row }.buttonStyle(.plain)
        } else {
            row
        }
    }

    private var row: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .interText(15, .semibold)
                    .foregroundStyle(Tokens.text.color)
                Text(subtitle)
                    .interText(13, .regular)
                    .foregroundStyle(Tokens.muted.color)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            Text(actionTitle)
                .interText(15, .semibold)
                .foregroundStyle(Tokens.accent.color)
                .fixedSize()
        }
        .frame(minHeight: 44)
        .contentShape(Rectangle())
    }
}
