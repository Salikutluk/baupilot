import SwiftUI

/// Signature tile of the sign-off grid (IosAbnSignatur:54-67): caption, a 64 high pad, name and status.
/// `.signed` shows the craftsman's sample over a 1 pt bottom border; `.pending` is the dashed owner tile whose pad is
/// a drawing surface, with a placeholder until a stroke exists. Grid tiles stretch to the row height.
public struct SignatureCard: View {
    public enum Style {
        case signed(name: String, status: String, statusColor: ColorToken)
        case pending(name: String, status: String, placeholder: String, strokes: Binding<SignatureStrokes>)
    }

    private static let padHeight: CGFloat = 64
    private static let radius: CGFloat = 14
    /// CSS `1.5px` borders render 1 pt wide (measured in the reference PNGs); the dash is the M1 convention.
    private static let borderWidth: CGFloat = 1
    private static let dash: [CGFloat] = [3, 2]

    private let style: Style
    private let caption: String
    private let divider: ColorToken
    private let padAccessibilityLabel: String
    private let identifier: String?

    public init(
        style: Style, caption: String, divider: ColorToken, padAccessibilityLabel: String,
        identifier: String? = nil
    ) {
        self.style = style
        self.caption = caption
        self.divider = divider
        self.padAccessibilityLabel = padAccessibilityLabel
        self.identifier = identifier
    }

    public var body: some View {
        switch style {
        case let .signed(name, status, statusColor):
            tile(name: name, status: status, statusColor: statusColor, border: false) {
                VStack(spacing: 0) {
                    SignaturePad(
                        strokes: nil, sample: true, color: Tokens.text, accessibilityLabel: padAccessibilityLabel)
                        .frame(height: Self.padHeight)
                    Rectangle().fill(divider.color).frame(height: 1)
                }
            }
            .accessibilityElement(children: .combine)
            .identified(identifier)
        case let .pending(name, status, placeholder, strokes):
            tile(name: name, status: status, statusColor: Tokens.muted, border: true) {
                ZStack {
                    if strokes.wrappedValue.isEmpty {
                        Text(placeholder)
                            .interText(14, .semibold)
                            .foregroundStyle(Tokens.accent.color)
                            .identified(identifier.map { "\($0).placeholder" })
                    }
                    SignaturePad(
                        strokes: strokes, sample: false, color: Tokens.text,
                        accessibilityLabel: padAccessibilityLabel)
                        .identified(identifier)
                }
                .frame(height: Self.padHeight)
            }
        }
    }

    private func tile<Pad: View>(
        name: String, status: String, statusColor: ColorToken, border: Bool, @ViewBuilder pad: () -> Pad
    ) -> some View {
        let shape = RoundedRectangle(cornerRadius: Self.radius)
        return VStack(alignment: .leading, spacing: 6) {
            Text(caption)
                .interText(12, .semibold)
                .foregroundStyle(Tokens.muted.color)
            pad()
            Text(name)
                .interText(13, .semibold)
                .foregroundStyle(Tokens.text.color)
            Text(status)
                .interText(12, .regular)
                .foregroundStyle(statusColor.color)
        }
        .padding(12 + (border ? Self.borderWidth : 0))
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(Tokens.surface.color, in: shape)
        .overlay {
            if border {
                shape.strokeBorder(
                    Tokens.accent.color, style: StrokeStyle(lineWidth: Self.borderWidth, dash: Self.dash))
            }
        }
    }
}

private extension View {
    @ViewBuilder
    func identified(_ identifier: String?) -> some View {
        if let identifier { accessibilityIdentifier(identifier) } else { self }
    }
}
