import SwiftUI

/// Row with a title (15), a 12 `muted` subtitle (gap 1) and the prototype switch (IosElRauchmelder:67-76). Min-height
/// 52, padding 0 14, gap 12. The screen owns the state and flips it in `action`.
public struct ToggleRow: View {
    private let title: String
    private let subtitle: String
    private let isOn: Bool
    private let onColor: ColorToken
    private let accessibilityLabel: String
    private let action: (() -> Void)?

    public init(
        title: String, subtitle: String, isOn: Bool, onColor: ColorToken, accessibilityLabel: String,
        action: (() -> Void)?
    ) {
        self.title = title
        self.subtitle = subtitle
        self.isOn = isOn
        self.onColor = onColor
        self.accessibilityLabel = accessibilityLabel
        self.action = action
    }

    public var body: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .interText(15, .regular)
                    .foregroundStyle(Tokens.text.color)
                Text(subtitle)
                    .interText(12, .regular)
                    .foregroundStyle(Tokens.muted.color)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            IosToggle(
                isOn: isOn, onColor: onColor, accessibilityLabel: accessibilityLabel, action: action ?? {})
        }
        .padding(.horizontal, 14)
        .frame(minHeight: 52)
    }
}
