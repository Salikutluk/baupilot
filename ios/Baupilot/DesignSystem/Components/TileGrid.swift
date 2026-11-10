import SwiftUI

/// The tile raster of a room check (IosFlBad:49): equal columns (`repeat(8, minmax(0, 1fr))`) of `tileHeight` tall
/// tiles on a `background` plate with `padding` and `gap`. A tile is a button for accessibility without an action; a
/// hollow tile carries a 2 pt `border` inside its box and a short label. Built from stacks, not a lazy grid, so the
/// height is known to a parent that sizes to its content.
public struct TileGrid: View {
    public struct Tile {
        public struct Style {
            public let fill: ColorToken
            public let border: ColorToken?
            public let labelColor: ColorToken

            public init(fill: ColorToken, border: ColorToken?, labelColor: ColorToken) {
                self.fill = fill
                self.border = border
                self.labelColor = labelColor
            }
        }

        public let id: String
        public let style: Style
        public let label: String?
        public let accessibilityLabel: String
        public let identifier: String

        public init(id: String, style: Style, label: String?, accessibilityLabel: String, identifier: String) {
            self.id = id
            self.style = style
            self.label = label
            self.accessibilityLabel = accessibilityLabel
            self.identifier = identifier
        }
    }

    private let tiles: [Tile]
    private let columns: Int
    private let gap: CGFloat
    private let padding: CGFloat
    private let radius: CGFloat
    private let tileRadius: CGFloat
    private let tileHeight: CGFloat
    private let background: ColorToken
    private let accessibilityLabel: String

    public init(
        tiles: [Tile], columns: Int, gap: CGFloat = 4, padding: CGFloat = 8, radius: CGFloat, tileRadius: CGFloat,
        tileHeight: CGFloat = 44, background: ColorToken, accessibilityLabel: String
    ) {
        self.tiles = tiles
        self.columns = columns
        self.gap = gap
        self.padding = padding
        self.radius = radius
        self.tileRadius = tileRadius
        self.tileHeight = tileHeight
        self.background = background
        self.accessibilityLabel = accessibilityLabel
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
                            tileView(tiles[row[column]])
                        } else {
                            Color.clear.frame(maxWidth: .infinity).frame(height: tileHeight)
                        }
                    }
                }
            }
        }
        .padding(padding)
        .frame(maxWidth: .infinity)
        .background(background.color, in: RoundedRectangle(cornerRadius: radius))
        .accessibilityElement(children: .contain)
        .accessibilityLabel(Text(accessibilityLabel))
    }

    private func tileView(_ tile: Tile) -> some View {
        let shape = RoundedRectangle(cornerRadius: tileRadius)
        return Color.clear
            .frame(maxWidth: .infinity)
            .frame(height: tileHeight)
            .background(tile.style.fill.color, in: shape)
            .overlay {
                if let border = tile.style.border {
                    shape.strokeBorder(border.color, lineWidth: 2)
                }
            }
            .overlay {
                if let label = tile.label {
                    Text(label)
                        .interText(10, .bold)
                        .foregroundStyle(tile.style.labelColor.color)
                        .fixedSize()
                }
            }
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(Text(tile.accessibilityLabel))
            .accessibilityAddTraits(.isButton)
            .accessibilityIdentifier(tile.identifier)
    }
}
