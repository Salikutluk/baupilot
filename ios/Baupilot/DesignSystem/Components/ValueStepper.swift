import SwiftUI

/// Minus / value / plus stepper on a `chip` capsule (IosRohbauMasse:39-43): 48 tall, r12, padding 0 4, gap 4.
/// The 40 pt buttons keep their drawn size; their tappable shape grows to 44.
public struct ValueStepper: View {
    static let minusGlyph = "−"
    static let plusGlyph = "+"

    /// One 40 pt button: r10 `surface`, 20/600 `text` glyph. The hit shape is the box inset by -2 (44 pt).
    struct StepButton: View {
        static let hitInset: CGFloat = -2

        /// The tappable shape: the drawn box grown 2 pt on every side.
        static var hitShape: some Shape { Rectangle().inset(by: hitInset) }

        let glyph: String
        let label: String
        var identifier: String?
        var action: (() -> Void)?

        var body: some View {
            let face = Text(glyph)
                .interText(20, .semibold)
                .foregroundStyle(Tokens.text.color)
                .frame(width: 40, height: 40)
                .background(RoundedRectangle(cornerRadius: 10).fill(Tokens.surface.color))
                .contentShape(Self.hitShape)
            Group {
                if let action {
                    Button(action: action) { face }.buttonStyle(.plain)
                } else {
                    face
                }
            }
            .accessibilityLabel(label)
            .accessibilityIdentifier(identifier ?? "")
        }
    }

    private let label: String
    private let value: String
    private let valueMinWidth: CGFloat
    private let onDecrement: (() -> Void)?
    private let onIncrement: (() -> Void)?
    private let identifiers: (dec: String, value: String, inc: String)?

    public init(
        label: String, value: String, valueMinWidth: CGFloat = 54, onDecrement: (() -> Void)?,
        onIncrement: (() -> Void)?, identifiers: (dec: String, value: String, inc: String)? = nil
    ) {
        self.label = label
        self.value = value
        self.valueMinWidth = valueMinWidth
        self.onDecrement = onDecrement
        self.onIncrement = onIncrement
        self.identifiers = identifiers
    }

    public var body: some View {
        HStack(spacing: 4) {
            StepButton(
                glyph: Self.minusGlyph, label: "\(label) verringern", identifier: identifiers?.dec,
                action: onDecrement)
            Text(value)
                .interText(16, .bold)
                .foregroundStyle(Tokens.text.color)
                .frame(minWidth: valueMinWidth)
                .accessibilityIdentifier(identifiers?.value ?? "")
            StepButton(
                glyph: Self.plusGlyph, label: "\(label) erhöhen", identifier: identifiers?.inc, action: onIncrement)
        }
        .padding(.horizontal, 4)
        .frame(height: 48)
        .background(RoundedRectangle(cornerRadius: 12).fill(Tokens.chip.core.color))
    }
}
