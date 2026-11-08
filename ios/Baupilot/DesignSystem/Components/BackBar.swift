import SwiftUI

/// 44 pt navigation bar: a back link with an optional trailing element, or the modal
/// cancel / title / confirm bar.
public struct BackBar: View {
    public enum Trailing {
        case none
        case counter(String)
        case note(String)
        case icon(BaupilotIcon, size: CGFloat, label: String, action: (() -> Void)?)
        case text(String, action: (() -> Void)?)
    }

    private enum Mode {
        case back(label: String, action: (() -> Void)?, trailing: Trailing)
        case modal(
            cancel: String, title: String, confirm: String,
            onCancel: (() -> Void)?, onConfirm: (() -> Void)?)
    }

    private let mode: Mode

    public init(label: String, action: (() -> Void)?, trailing: Trailing = .none) {
        mode = .back(label: label, action: action, trailing: trailing)
    }

    private init(mode: Mode) {
        self.mode = mode
    }

    public static func modal(
        cancel: String,
        title: String,
        confirm: String,
        onCancel: (() -> Void)? = nil,
        onConfirm: (() -> Void)? = nil
    ) -> BackBar {
        BackBar(
            mode: .modal(
                cancel: cancel, title: title, confirm: confirm,
                onCancel: onCancel, onConfirm: onConfirm))
    }

    public var body: some View {
        switch mode {
        case let .back(label, action, trailing):
            HStack(spacing: 0) {
                Button(action: { action?() }) {
                    HStack(spacing: 2) {
                        Icon(.chevronLeftW24, size: 24, color: Tokens.accent.color)
                        Text(label)
                            .interText(17, .regular)
                            .foregroundStyle(Tokens.accent.color)
                    }
                    .frame(minHeight: 44)
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("nav.back")
                .padding(.leading, -6)
                Spacer(minLength: 0)
                trailingView(trailing)
            }
            .frame(height: 44)
        case let .modal(cancel, title, confirm, onCancel, onConfirm):
            HStack(spacing: 0) {
                Button(action: { onCancel?() }) {
                    Text(cancel)
                        .interText(17, .regular)
                        .foregroundStyle(Tokens.accent.color)
                        .frame(minHeight: 44)
                }
                .buttonStyle(.plain)
                .frame(maxWidth: .infinity, alignment: .leading)
                Text(title)
                    .interText(17, .semibold)
                    .foregroundStyle(Tokens.text.color)
                    .fixedSize()
                    .layoutPriority(1)
                    .accessibilityAddTraits(.isHeader)
                Button(action: { onConfirm?() }) {
                    Text(confirm)
                        .interText(17, .semibold)
                        .foregroundStyle(Tokens.accent.color)
                        .frame(minHeight: 44)
                }
                .buttonStyle(.plain)
                .frame(maxWidth: .infinity, alignment: .trailing)
            }
            .frame(height: 44)
        }
    }

    @ViewBuilder
    private func trailingView(_ trailing: Trailing) -> some View {
        switch trailing {
        case .none:
            EmptyView()
        case let .counter(text):
            Text(text)
                .interText(15, .semibold)
                .foregroundStyle(Tokens.muted.color)
        case let .note(text):
            Text(text)
                .interText(15, .regular)
                .foregroundStyle(Tokens.muted.color)
        case let .icon(icon, size, label, action):
            Button(action: { action?() }) {
                Icon(icon, size: size, color: Tokens.accent.color)
                    .frame(width: 44, height: 44)
            }
            .buttonStyle(.plain)
            .accessibilityLabel(label)
        case let .text(text, action):
            Button(action: { action?() }) {
                Text(text)
                    .interText(17, .regular)
                    .foregroundStyle(Tokens.accent.color)
                    .frame(minHeight: 44)
            }
            .buttonStyle(.plain)
        }
    }
}
