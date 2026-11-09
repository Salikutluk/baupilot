import SwiftUI

/// Pill action inside a card (IosAbnMaengel:40-41): 44 tall including the 1 pt border (`1px` border-box), padding 0 14,
/// radius 22 (a circular capsule at 44; the default continuous corner draws a squircle), 13/600. Outline: transparent with `text` ink and a border in the screen's token; filled: `accent` with
/// `onAccent`. A nil `action` renders it and does nothing.
public struct PillButton: View {
    public enum Style {
        case outline(border: ColorToken)
        case filled
    }

    private static let height: CGFloat = 44
    private static let borderWidth: CGFloat = 1

    private let title: String
    private let style: Style
    private let action: (() -> Void)?

    public init(_ title: String, style: Style, action: (() -> Void)?) {
        self.title = title
        self.style = style
        self.action = action
    }

    public var body: some View {
        let shape = Capsule(style: .circular)
        Button(action: action ?? {}) {
            Text(title)
                .interText(13, .semibold)
                .foregroundStyle(foreground.color)
                .padding(.horizontal, 14 + border)
                .frame(height: Self.height)
                .background {
                    if case .filled = style {
                        shape.fill(Tokens.accent.color)
                    }
                }
                .overlay {
                    if case .outline(let token) = style {
                        shape.strokeBorder(token.color, lineWidth: Self.borderWidth)
                    }
                }
        }
        .buttonStyle(.plain)
    }

    private var foreground: ColorToken {
        switch style {
        case .outline: return Tokens.text
        case .filled: return Tokens.onAccent
        }
    }

    /// Border width per side: the outline has one, the filled button `border: 0`.
    private var border: CGFloat {
        switch style {
        case .outline: return Self.borderWidth
        case .filled: return 0
        }
    }
}
