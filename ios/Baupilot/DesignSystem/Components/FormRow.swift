import SwiftUI

/// Row of a grouped form card (IosAbnMangel:70-82): a title and a value or a switch, at least 48 pt tall,
/// with an optional 1 pt top divider that sits above the 48 pt content box.
public struct FormRow: View {
    public enum Trailing {
        case value(String)
        case toggle(isOn: Bool, onColor: ColorToken, accessibilityLabel: String, action: () -> Void)
    }

    private let title: String
    private let trailing: Trailing
    private let showsDivider: Bool
    private let divider: ColorToken
    private let toggleIdentifier: String?

    public init(
        title: String, trailing: Trailing, showsDivider: Bool, divider: ColorToken,
        toggleIdentifier: String? = nil
    ) {
        self.title = title
        self.trailing = trailing
        self.showsDivider = showsDivider
        self.divider = divider
        self.toggleIdentifier = toggleIdentifier
    }

    public var body: some View {
        VStack(spacing: 0) {
            if showsDivider {
                Rectangle()
                    .fill(divider.color)
                    .frame(height: 1)
            }
            HStack(spacing: 10) {
                Text(title)
                    .interText(16, .regular, lines: .block)
                    .foregroundStyle(Tokens.text.color)
                    .frame(maxWidth: .infinity, alignment: .leading)
                trailingView
            }
            .padding(.horizontal, 14)
            .frame(minHeight: 48)
        }
    }

    @ViewBuilder
    private var trailingView: some View {
        switch trailing {
        case .value(let text):
            Text(text)
                .interText(15, .regular, lines: .block)
                .foregroundStyle(Tokens.muted.color)
                .fixedSize(horizontal: true, vertical: false)
                .layoutPriority(1)
        case .toggle(let isOn, let onColor, let label, let action):
            IosToggle(isOn: isOn, onColor: onColor, accessibilityLabel: label, action: action)
                .accessibilityIdentifier(toggleIdentifier ?? "")
        }
    }
}
