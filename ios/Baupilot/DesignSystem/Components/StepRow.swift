import SwiftUI

/// Finished and open rows of a protocol step list. The current step is `CurrentStepCard`.
/// The stack gaps (8 / 6 / 2) belong to the screen.
public struct StepRow: View {
    public enum State {
        case done
        case open(tag: String? = nil)
    }

    /// Trailing detail of a finished row.
    public enum Meta {
        /// 13 muted.
        case text(String)
        /// 13/600 text.
        case strong(String)
        /// 14/600 oFg.
        case ok(String)
        /// Colour swatch dot.
        case swatch(ColorToken)
        /// Rounded paint swatch: `side` fill plus a 1 pt border outside it (CSS content-box), so 20 outer for 18.
        case swatchSquare(ColorToken, side: CGFloat, radius: CGFloat, border: ColorToken)
    }

    /// Filled ok ring with a tick.
    struct DoneRing: View {
        var ok: ColorToken = Tokens.ok.core
        var okInk: ColorToken = Tokens.okInk.core

        var body: some View {
            ZStack {
                Circle().fill(ok.color)
                Icon(.checkW35, size: 12, color: okInk.color)
            }
            .frame(width: 22, height: 22)
        }
    }

    /// Hollow ring with the step number.
    struct OpenRing: View {
        let number: Int
        var track: ColorToken = Tokens.track.core

        var body: some View {
            Text("\(number)")
                .interText(11, .bold)
                .foregroundStyle(Tokens.muted.color)
                .frame(width: 22, height: 22)
                .overlay { Circle().strokeBorder(track.color, lineWidth: 2) }
        }
    }

    private let number: Int
    private let title: String
    private let state: State
    private let meta: Meta?
    private let minHeight: CGFloat
    private let track: ColorToken
    private let ok: ColorToken
    private let okInk: ColorToken
    private let titleLines: TextLines

    public init(
        number: Int,
        title: String,
        state: State,
        meta: Meta? = nil,
        minHeight: CGFloat = 44,
        track: ColorToken = Tokens.track.core,
        ok: ColorToken = Tokens.ok.core,
        okInk: ColorToken = Tokens.okInk.core,
        titleLines: TextLines = .single
    ) {
        self.titleLines = titleLines
        self.ok = ok
        self.okInk = okInk
        self.number = number
        self.title = title
        self.state = state
        self.meta = meta
        self.minHeight = minHeight
        self.track = track
    }

    public var body: some View {
        switch state {
        case .done: doneRow
        case .open(let tag): openRow(tag: tag)
        }
    }

    private var doneRow: some View {
        HStack(spacing: 12) {
            DoneRing(ok: ok, okInk: okInk)
            titleView(Tokens.muted)
                .frame(maxWidth: .infinity, alignment: .leading)
            if let meta { metaView(meta) }
        }
        .padding(.horizontal, 14)
        .frame(minHeight: minHeight)
        .fixedSize(horizontal: false, vertical: titleLines == .greedy)
        .background(Tokens.surface.color, in: RoundedRectangle(cornerRadius: 12))
        .accessibilityElement(children: .combine)
    }

    private func openRow(tag: String?) -> some View {
        HStack(spacing: 12) {
            OpenRing(number: number, track: track)
            titleView(Tokens.text)
                .frame(maxWidth: .infinity, alignment: .leading)
            if let tag {
                Text(tag)
                    .interText(12, .semibold)
                    .foregroundStyle(Tokens.eFg.color)
            }
        }
        .padding(.horizontal, 14)
        .frame(minHeight: minHeight)
        .fixedSize(horizontal: false, vertical: titleLines == .greedy)
        .accessibilityElement(children: .combine)
    }

    /// The row title. `.greedy` breaks like Chrome through `GreedyLabel`; the other cases go through `interText`.
    @ViewBuilder
    private func titleView(_ color: ColorToken) -> some View {
        if titleLines == .greedy {
            GreedyLabel(text: title, size: 15, weight: .regular, color: color, lineHeight: .normal)
        } else {
            Text(title)
                .interText(15, .regular, lines: titleLines)
                .foregroundStyle(color.color)
        }
    }

    @ViewBuilder
    private func metaView(_ meta: Meta) -> some View {
        switch meta {
        case .text(let value):
            Text(value).interText(13, .regular).foregroundStyle(Tokens.muted.color)
        case .strong(let value):
            Text(value).interText(13, .semibold).foregroundStyle(Tokens.text.color)
        case .ok(let value):
            Text(value).interText(14, .semibold).foregroundStyle(Tokens.oFg.color)
        case .swatch(let color):
            Circle().fill(color.color).frame(width: 14, height: 14)
        case .swatchSquare(let fill, let side, let radius, let border):
            RoundedRectangle(cornerRadius: radius)
                .fill(fill.color)
                .overlay { RoundedRectangle(cornerRadius: radius).strokeBorder(border.color, lineWidth: 1) }
                .frame(width: side + 2, height: side + 2)
        }
    }
}

/// The step being worked on: a card with a 2 pt accent border, a numbered header and free content.
public struct CurrentStepCard<Content: View>: View {
    /// Accent circle holding the step number.
    struct NumberCircle: View {
        let number: Int

        var body: some View {
            Text("\(number)")
                .interText(14, .bold)
                .foregroundStyle(Tokens.onAccent.color)
                .frame(width: 26, height: 26)
                .background(Tokens.accent.color, in: Circle())
        }
    }

    /// Top row of the card: the accent number circle with the title, or the title with a note at its right.
    public enum Header {
        /// 26 accent circle holding the number, then the 19/700 title.
        case numbered(Int, title: String)
        /// No circle: the 19/700 title and a 13/600 note in `noteColor`, on the first baseline.
        case note(title: String, note: String, noteColor: ColorToken)
    }

    private let header: Header
    private let spacing: CGFloat
    private let content: Content

    public init(number: Int, title: String, spacing: CGFloat = 14, @ViewBuilder content: () -> Content) {
        self.init(header: .numbered(number, title: title), spacing: spacing, content: content)
    }

    public init(header: Header, spacing: CGFloat = 14, @ViewBuilder content: () -> Content) {
        self.header = header
        self.spacing = spacing
        self.content = content()
    }

    public var body: some View {
        Card(padding: EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16), border: .current) {
            VStack(alignment: .leading, spacing: spacing) {
                headerView
                content
            }
        }
    }
}

extension CurrentStepCard {
    @ViewBuilder
    fileprivate var headerView: some View {
        switch header {
        case .numbered(let number, let title):
            HStack(spacing: 12) {
                NumberCircle(number: number)
                Text(title)
                    .interText(19, .bold)
                    .foregroundStyle(Tokens.text.color)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
        case .note(let title, let note, let noteColor):
            HStack(alignment: .firstTextBaseline) {
                Text(title)
                    .interText(19, .bold)
                    .foregroundStyle(Tokens.text.color)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text(note)
                    .interText(13, .semibold)
                    .foregroundStyle(noteColor.color)
            }
        }
    }
}

public extension CurrentStepCard {
    /// Body paragraph of a step: 15 muted, line-height 1.45 (`interText` turns it into `lineSpacing`).
    static func body(_ text: String) -> some View {
        Text(text)
            .interText(15, .regular, lineHeight: .factor(1.45), lines: .multi)
            .foregroundStyle(Tokens.muted.color)
            .fixedSize(horizontal: false, vertical: true)
    }
}
