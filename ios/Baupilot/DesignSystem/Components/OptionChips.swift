import SwiftUI

/// Row of 34 pt pill chips with one selection (IosAbnMangel:61-65). The hit area grows to 44 pt without
/// changing the layout. A chip whose `enabled` entry is false does nothing.
public struct OptionChips: View {
    private enum Metrics {
        static let height: CGFloat = 34
        static let hitHeight: CGFloat = 44
        static let hitInset = -(hitHeight - height) / 2
    }

    /// The tappable shape of a chip: its box grown to 44 pt tall (R16).
    static var hitShape: some Shape { Rectangle().inset(by: Metrics.hitInset) }

    private let items: [String]
    private let selection: Int?
    private let border: ColorToken
    private let identifiers: [String]?
    private let enabled: [Bool]?
    private let onSelect: (Int) -> Void

    public init(
        items: [String], selection: Int?, border: ColorToken, identifiers: [String]? = nil,
        enabled: [Bool]? = nil, onSelect: @escaping (Int) -> Void
    ) {
        assert(
            identifiers == nil || identifiers?.count == items.count, "OptionChips: one identifier per item")
        assert(enabled == nil || enabled?.count == items.count, "OptionChips: one enabled flag per item")
        self.items = items
        self.selection = selection
        self.border = border
        self.identifiers = identifiers
        self.enabled = enabled
        self.onSelect = onSelect
    }

    public var body: some View {
        HStack(spacing: 8) {
            ForEach(items.indices, id: \.self) { index in
                chip(index)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func chip(_ index: Int) -> some View {
        let selected = index == selection
        let isEnabled = enabled?[index] ?? true
        let shape = Capsule()
        return Button {
            if isEnabled { onSelect(index) }
        } label: {
            Text(items[index])
                .interText(14, selected ? .semibold : .regular)
                .foregroundStyle((selected ? Tokens.aFg : Tokens.text).color)
                .fixedSize()
                // Border-box with padding 12: an unselected chip's 1 pt border adds to the inset, a selected chip has none.
                .padding(.horizontal, selected ? 12 : 13)
                .frame(height: Metrics.height)
                .background {
                    if selected {
                        shape.fill(Tokens.aSoft.color)
                    }
                }
                .overlay {
                    if !selected {
                        Ring(radius: Metrics.height / 2, width: 1).fill(border.color, style: FillStyle(eoFill: true))
                    }
                }
                .contentShape(Self.hitShape)
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(selected ? [.isButton, .isSelected] : .isButton)
        .accessibilityIdentifier(identifiers?[index] ?? "")
    }
}

/// The 1 pt border of a pill as a filled ring. A stroked stadium renders a stray hairline beside both ends in the
/// snapshot renderer; the even-odd fill of outer minus inner does not.
private struct Ring: Shape {
    let radius: CGFloat
    let width: CGFloat

    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.addRoundedRect(in: rect, cornerSize: CGSize(width: radius, height: radius))
        path.addRoundedRect(
            in: rect.insetBy(dx: width, dy: width),
            cornerSize: CGSize(width: radius - width, height: radius - width))
        return path
    }
}
