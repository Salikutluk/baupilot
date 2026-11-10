import SwiftUI

/// One socket of the room check: a 56 tall r12 button-like tile with the id (14/700) over a state label (11/600),
/// gap 2 (IosElRaum:46-49). A `now` tile draws a 2 pt border inside its box. The tile has no action: the screen
/// only shows the state, so it is a button for accessibility without one.
public struct SocketTile: View {
    public struct Style {
        public let background: ColorToken
        public let foreground: ColorToken
        public let border: ColorToken?

        public init(background: ColorToken, foreground: ColorToken, border: ColorToken? = nil) {
            self.background = background
            self.foreground = foreground
            self.border = border
        }
    }

    private let id: String
    private let label: String
    private let style: Style
    private let accessibilityLabel: String
    private let identifier: String

    public init(id: String, label: String, style: Style, accessibilityLabel: String, identifier: String) {
        self.id = id
        self.label = label
        self.style = style
        self.accessibilityLabel = accessibilityLabel
        self.identifier = identifier
    }

    public var body: some View {
        let shape = RoundedRectangle(cornerRadius: 12)
        VStack(spacing: 2) {
            Text(id).interText(14, .bold)
            Text(label).interText(11, .semibold)
        }
        .foregroundStyle(style.foreground.color)
        .frame(maxWidth: .infinity)
        .frame(height: 56)
        .background(style.background.color, in: shape)
        .overlay {
            if let border = style.border {
                shape.strokeBorder(border.color, lineWidth: 2)
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(accessibilityLabel))
        .accessibilityAddTraits(.isButton)
        .accessibilityIdentifier(identifier)
    }
}

/// Tiles in equal columns (`repeat(4, minmax(0, 1fr))`), gap 8 both ways. Built from stacks, not a lazy grid, so
/// the height is known to a parent that sizes to its content. A last row with fewer tiles keeps the column width.
public struct SocketGrid: View {
    private let tiles: [SocketTile]
    private let columns: Int
    private let gap: CGFloat

    public init(tiles: [SocketTile], columns: Int = 4, gap: CGFloat = 8) {
        self.tiles = tiles
        self.columns = columns
        self.gap = gap
    }

    private var rows: [[Int]] {
        stride(from: 0, to: tiles.count, by: columns).map { start in
            Array(start..<min(start + columns, tiles.count))
        }
    }

    public var body: some View {
        VStack(spacing: gap) {
            ForEach(rows, id: \.self) { row in
                HStack(spacing: gap) {
                    ForEach(0..<columns, id: \.self) { column in
                        if column < row.count {
                            tiles[row[column]]
                        } else {
                            Color.clear.frame(maxWidth: .infinity).frame(height: 56)
                        }
                    }
                }
            }
        }
    }
}
