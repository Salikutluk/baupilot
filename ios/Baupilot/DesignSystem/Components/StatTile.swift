import SwiftUI

/// Count tile of the signature summary (IosAbnSignatur:32): large value over a label.
public struct CountTile: View {
    public enum Tone {
        case surface, ok, error
    }

    private let value: String
    private let label: String
    private let tone: Tone
    private let errorBackground: ColorToken

    public init(
        value: String, label: String, tone: Tone,
        errorBackground: ColorToken = Tokens.eSoft.core
    ) {
        self.value = value
        self.label = label
        self.tone = tone
        self.errorBackground = errorBackground
    }

    private var background: ColorToken {
        switch tone {
        case .surface: return Tokens.surface
        case .ok: return Tokens.oSoft
        case .error: return errorBackground
        }
    }

    private var foreground: ColorToken {
        switch tone {
        case .surface: return Tokens.text
        case .ok: return Tokens.oFg
        case .error: return Tokens.eFg
        }
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(value)
                .interText(24, .bold)
            Text(label)
                .interText(13, .regular)
        }
        .foregroundStyle(foreground.color)
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(background.color, in: RoundedRectangle(cornerRadius: 14))
        .accessibilityElement(children: .combine)
    }
}

/// Phase tile of the element overview (IosElUebersicht:31). The active phase carries an accent
/// border and a card progress bar instead of a date.
public struct PhaseTile: View {
    public enum State {
        case done, planned
        case active(progress: Double)
    }

    private let label: String
    private let value: String
    private let date: String?
    private let state: State
    private let track: ColorToken

    public init(
        label: String, value: String, date: String? = nil, state: State,
        track: ColorToken = Tokens.track.core
    ) {
        self.label = label
        self.value = value
        self.date = date
        self.state = state
        self.track = track
    }

    private var isActive: Bool {
        if case .active = state { return true }
        return false
    }

    private var valueColor: ColorToken {
        switch state {
        case .done: return Tokens.oFg
        case .planned: return Tokens.muted
        case .active: return Tokens.text
        }
    }

    public var body: some View {
        let shape = RoundedRectangle(cornerRadius: 14)
        VStack(alignment: .leading, spacing: isActive ? 6 : 4) {
            Text(label)
                .interText(12, .semibold)
                .foregroundStyle(Tokens.muted.color)
            Text(value)
                .interText(15, .semibold)
                .foregroundStyle(valueColor.color)
            if case .active(let progress) = state {
                ProgressBar(value: progress, style: .card, track: track)
            } else if let date {
                Text(date)
                    .interText(12, .regular)
                    .foregroundStyle(Tokens.muted.color)
            }
        }
        .padding(isActive ? 14 : 12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Tokens.surface.color, in: shape)
        .overlay {
            if isActive {
                shape.strokeBorder(Tokens.accent.color, lineWidth: 2)
            }
        }
        .accessibilityElement(children: .combine)
    }
}

/// Row of label/value facts: `.divided` is the hairline-separated strip (IosGewerk:37),
/// `.grid` the padded grid with bold values (IosMaUebersicht:30).
public struct FactStrip: View {
    public enum Style {
        case divided, grid
    }

    public struct Item {
        public let label: String
        public let value: String

        public init(label: String, value: String) {
            self.label = label
            self.value = value
        }
    }

    private let items: [Item]
    private let style: Style
    private let separator: ColorToken

    public init(
        items: [(label: String, value: String)], style: Style,
        separator: ColorToken = Tokens.border.core
    ) {
        self.items = items.map { Item(label: $0.label, value: $0.value) }
        self.style = style
        self.separator = separator
    }

    public var body: some View {
        switch style {
        case .divided: divided
        case .grid: grid
        }
    }

    private func fact(_ item: Item, gap: CGFloat, valueSize: CGFloat, valueWeight: InterWeight) -> some View {
        VStack(alignment: .leading, spacing: gap) {
            Text(item.label)
                .interText(12, .semibold)
                .foregroundStyle(Tokens.muted.color)
            Text(item.value)
                .interText(valueSize, valueWeight)
                .foregroundStyle(Tokens.text.color)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .accessibilityElement(children: .combine)
    }

    private var divided: some View {
        HStack(alignment: .top, spacing: 0) {
            ForEach(Array(items.enumerated()), id: \.offset) { index, item in
                fact(item, gap: 4, valueSize: 15, valueWeight: .semibold)
                    // CSS `border-left` sits inside the column, so columns after the first start 1 pt later.
                    .padding(.leading, index > 0 ? 13 : 12)
                    .padding(.trailing, 12)
                    .overlay(alignment: .leading) {
                        if index > 0 {
                            Rectangle().fill(separator.color).frame(width: 1)
                        }
                    }
            }
        }
        .padding(.vertical, 14)
        .padding(.horizontal, 4)
        .frame(maxWidth: .infinity)
        .background(Tokens.surface.color, in: RoundedRectangle(cornerRadius: 16))
    }

    private var grid: some View {
        HStack(alignment: .top, spacing: 8) {
            ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                fact(item, gap: 3, valueSize: 16, valueWeight: .bold)
            }
        }
        .padding(14)
        .frame(maxWidth: .infinity)
        .background(Tokens.surface.color, in: RoundedRectangle(cornerRadius: 14))
    }
}
