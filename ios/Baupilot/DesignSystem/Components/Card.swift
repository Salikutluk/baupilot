import SwiftUI

/// Surface container with the prototype's card radii. List containers pass `padding: EdgeInsets()`.
public struct Card<Content: View>: View {
    public enum Style {
        case standard, hero, row, grouped

        var radius: CGFloat {
            switch self {
            case .standard: return Tokens.Radius.card
            case .hero: return Tokens.Radius.cardHero
            case .row: return Tokens.Radius.rowCard
            case .grouped: return Tokens.Radius.field
            }
        }
    }

    public struct Border {
        public let width: CGFloat
        public let color: ColorToken

        public init(width: CGFloat, color: ColorToken) {
            self.width = width
            self.color = color
        }

        /// Current-step card (IosAbnPruefung:43).
        public static var current: Border { Border(width: 2, color: Tokens.accent) }

        /// Overdue card (IosAbnMaengel:73, `1.5px` renders 1 pt). The colour is injectable per screen.
        public static func overdue(_ color: ColorToken = Tokens.err.core) -> Border {
            Border(width: 1, color: color)
        }
    }

    private let style: Style
    private let padding: EdgeInsets
    private let border: Border?
    private let content: Content

    public init(
        style: Style = .standard,
        padding: EdgeInsets = EdgeInsets(top: 14, leading: 14, bottom: 14, trailing: 14),
        border: Border? = nil,
        @ViewBuilder content: () -> Content
    ) {
        self.style = style
        self.padding = padding
        self.border = border
        self.content = content()
    }

    public var body: some View {
        let shape = RoundedRectangle(cornerRadius: style.radius)
        content
            .padding(padding)
            .padding(border?.width ?? 0)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Tokens.surface.color, in: shape)
            .clipShape(shape)
            .overlay {
                if let border {
                    shape.strokeBorder(border.color.color, lineWidth: border.width)
                }
            }
    }
}
