import SwiftUI

/// Prototype switch: 51×31 track, 27×27 knob. Drawn by hand; no system toggle style.
public struct IosToggle: View {
    private let isOn: Bool
    private let onColor: ColorToken
    private let accessibilityLabel: String
    private let action: () -> Void

    public init(
        isOn: Bool,
        onColor: ColorToken = Tokens.on,
        accessibilityLabel: String,
        action: @escaping () -> Void
    ) {
        self.isOn = isOn
        self.onColor = onColor
        self.accessibilityLabel = accessibilityLabel
        self.action = action
    }

    public var body: some View {
        Button(action: action) {
            ZStack(alignment: isOn ? .trailing : .leading) {
                RoundedRectangle(cornerRadius: 16)
                    .fill((isOn ? onColor : Tokens.off).color)
                Circle()
                    .fill(Tokens.knob.color)
                    .frame(width: 27, height: 27)
                    .shadow(color: Tokens.knobShadow.color, radius: 2, x: 0, y: 2)
                    .padding(2)
            }
            .frame(width: 51, height: 31)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text(accessibilityLabel))
        .accessibilityValue(Text(isOn ? "1" : "0"))
        .accessibilityAddTraits(.isToggle)
    }
}
