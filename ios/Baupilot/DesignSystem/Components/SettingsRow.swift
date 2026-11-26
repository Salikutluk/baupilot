import SwiftUI

/// One row of a Profil settings group (IosProfil:36-50): label with a link value and chevron, a toggle,
/// or nothing. Place rows inside a `Card(padding: EdgeInsets())`; the divider sits on top of every row
/// that is not the first. The row has no action of its own; only the toggle flips its binding.
public struct SettingsRow: View {
    public enum Trailing {
        case link(value: String?)
        case toggle(isOn: Binding<Bool>)
        case none
    }

    public enum Tone {
        case regular
        case danger

        var labelColor: ColorToken { self == .danger ? Tokens.danger : Tokens.text }
    }

    private enum Metrics {
        static let minHeight: CGFloat = 46
        static let horizontal: CGFloat = 14
        static let gap: CGFloat = 12
        static let chevronSize: CGFloat = 16
        /// Chrome seats the value and the switch one device pixel (1/3 pt at 3x) away from SwiftUI's position.
    }

    private let label: String
    private let trailing: Trailing
    private let tone: Tone
    private let showsDivider: Bool
    private let identifier: String?

    /// `identifier` goes on the label text: an identifier on the whole row would make the audit see one text node
    /// with no font it can check (Dynamic Type finding).
    public init(
        label: String, trailing: Trailing, tone: Tone = .regular, showsDivider: Bool, identifier: String? = nil
    ) {
        self.label = label
        self.trailing = trailing
        self.tone = tone
        self.showsDivider = showsDivider
        self.identifier = identifier
    }

    public var body: some View {
        VStack(spacing: 0) {
            if showsDivider {
                Rectangle().fill(Tokens.border.core.color).frame(height: 1)
            }
            HStack(spacing: Metrics.gap) {
                Text(label)
                    .interText(16, .regular)
                    .foregroundStyle(tone.labelColor.color)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .optionalIdentifier(identifier)
                trailingView
            }
            .padding(.horizontal, Metrics.horizontal)
            .frame(minHeight: Metrics.minHeight)
        }
    }

    @ViewBuilder
    private var trailingView: some View {
        switch trailing {
        case .none:
            EmptyView()
        case .link(let value):
            if let value, !value.isEmpty {
                Text(value)
                    .interText(15, .regular)
                    .foregroundStyle(Tokens.muted.color)
                    .lineLimit(1)
                    .fixedSize()
            }
            Icon(.chevronRightW22, size: Metrics.chevronSize, color: Tokens.muted.color)
        case .toggle(let isOn):
            IosToggle(
                isOn: isOn.wrappedValue, onColor: Tokens.on, accessibilityLabel: label,
                action: { isOn.wrappedValue.toggle() }
            )
        }
    }
}
