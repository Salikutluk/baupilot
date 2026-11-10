import SwiftUI

/// Single-select row of pill filters that bleeds 20 pt past the screen padding on both sides.
public struct FilterChips: View {
    private let items: [String]
    private let selection: Int
    private let accessibilityLabel: String
    private let inactive: ColorToken
    private let identifiers: [String]?
    private let onSelect: (Int) -> Void

    public init(
        items: [String],
        selection: Int,
        accessibilityLabel: String,
        inactive: ColorToken = Tokens.chip.core,
        identifiers: [String]? = nil,
        onSelect: @escaping (Int) -> Void
    ) {
        assert(identifiers == nil || identifiers?.count == items.count, "FilterChips: one identifier per item")
        self.inactive = inactive
        self.identifiers = identifiers
        self.items = items
        self.selection = selection
        self.accessibilityLabel = accessibilityLabel
        self.onSelect = onSelect
    }

    public var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(items.indices, id: \.self) { index in
                    chip(index)
                }
            }
            .padding(.horizontal, 20)
        }
        .padding(.horizontal, -20)
        .accessibilityElement(children: .contain)
        .accessibilityLabel(accessibilityLabel)
    }

    private func chip(_ index: Int) -> some View {
        let active = index == selection
        return Button {
            onSelect(index)
        } label: {
            Text(items[index])
                .interText(14, .semibold)
                .foregroundStyle((active ? Tokens.bg : Tokens.text).color)
                .lineLimit(1)
                .fixedSize()
                .padding(.horizontal, 12)
                .frame(height: 36)
                .background(
                    (active ? Tokens.text : inactive).color, in: RoundedRectangle(cornerRadius: 18)
                )
                // D14: 36 < 44, so the hit area grows by (44 − 36) / 2 without changing layout.
                .contentShape(Rectangle().inset(by: -4))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(active ? [.isButton, .isSelected] : .isButton)
        .accessibilityIdentifier(identifiers?[index] ?? "")
    }
}

/// Step chip of a multi-step flow: finished, current or still open.
public struct SelectionChip: View {
    public enum State {
        case done
        case active
        case open
    }

    private let text: String
    private let state: State
    private let openBorder: ColorToken
    private let showsTick: Bool
    private let borderBox: Bool
    private let accessibilityLabel: String?
    private let isButton: Bool

    /// `borderBox` makes the open chip's 1 pt border part of its 36 pt box, as a CSS button is: the padding is 13, so
    /// the outer width is the text plus 26 (IosSanBad:35). `accessibilityLabel` replaces the combined label and
    /// `isButton` adds the button trait for a chip that is shown as a tab without an action.
    public init(
        _ text: String,
        state: State,
        openBorder: ColorToken = Tokens.border.core,
        showsTick: Bool = false,
        borderBox: Bool = false,
        accessibilityLabel: String? = nil,
        isButton: Bool = false
    ) {
        self.text = text
        self.state = state
        self.openBorder = openBorder
        self.showsTick = showsTick
        self.borderBox = borderBox
        self.accessibilityLabel = accessibilityLabel
        self.isButton = isButton
    }

    private var foreground: ColorToken {
        switch state {
        case .done: return Tokens.oFg
        case .active: return Tokens.onAccent
        case .open: return Tokens.text
        }
    }

    private var fill: Color {
        switch state {
        case .done: return Tokens.oSoft.color
        case .active: return Tokens.accent.color
        case .open: return .clear
        }
    }

    public var body: some View {
        HStack(spacing: 6) {
            if state == .done && showsTick {
                Icon(.checkW30, size: 14, color: foreground.color)
            }
            Text(text)
                .interText(14, .semibold)
                .foregroundStyle(foreground.color)
                .lineLimit(1)
        }
        .fixedSize()
        .padding(.horizontal, borderBox && state == .open ? 13 : 12)
        .frame(height: 36)
        .background(fill, in: RoundedRectangle(cornerRadius: 18))
        .overlay {
            if state == .open {
                Capsule().strokeBorder(openBorder.color, lineWidth: 1)
            }
        }
        .contentShape(Rectangle().inset(by: -4))
        .accessibilityElement(children: .combine)
        .accessibilityLabel(accessibilityLabel ?? text)
        .accessibilityAddTraits(traits)
    }

    private var traits: AccessibilityTraits {
        var result: AccessibilityTraits = isButton ? .isButton : []
        if state == .active { result.formUnion(.isSelected) }
        return result
    }
}
