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
                .font(.inter(11, .bold))
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

    public init(
        number: Int,
        title: String,
        state: State,
        meta: Meta? = nil,
        minHeight: CGFloat = 44,
        track: ColorToken = Tokens.track.core,
        ok: ColorToken = Tokens.ok.core,
        okInk: ColorToken = Tokens.okInk.core
    ) {
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
            Text(title)
                .font(.inter(15, .regular))
                .foregroundStyle(Tokens.muted.color)
                .frame(maxWidth: .infinity, alignment: .leading)
            if let meta { metaView(meta) }
        }
        .padding(.horizontal, 14)
        .frame(minHeight: minHeight)
        .background(Tokens.surface.color, in: RoundedRectangle(cornerRadius: 12))
        .accessibilityElement(children: .combine)
    }

    private func openRow(tag: String?) -> some View {
        HStack(spacing: 12) {
            OpenRing(number: number, track: track)
            Text(title)
                .font(.inter(15, .regular))
                .foregroundStyle(Tokens.text.color)
                .frame(maxWidth: .infinity, alignment: .leading)
            if let tag {
                Text(tag)
                    .font(.inter(12, .semibold))
                    .foregroundStyle(Tokens.eFg.color)
            }
        }
        .padding(.horizontal, 14)
        .frame(minHeight: minHeight)
        .accessibilityElement(children: .combine)
    }

    @ViewBuilder
    private func metaView(_ meta: Meta) -> some View {
        switch meta {
        case .text(let value):
            Text(value).font(.inter(13, .regular)).foregroundStyle(Tokens.muted.color)
        case .strong(let value):
            Text(value).font(.inter(13, .semibold)).foregroundStyle(Tokens.text.color)
        case .ok(let value):
            Text(value).font(.inter(14, .semibold)).foregroundStyle(Tokens.oFg.color)
        case .swatch(let color):
            Circle().fill(color.color).frame(width: 14, height: 14)
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
                .font(.inter(14, .bold))
                .foregroundStyle(Tokens.onAccent.color)
                .frame(width: 26, height: 26)
                .background(Tokens.accent.color, in: Circle())
        }
    }

    private let number: Int
    private let title: String
    private let spacing: CGFloat
    private let content: Content

    public init(number: Int, title: String, spacing: CGFloat = 14, @ViewBuilder content: () -> Content) {
        self.number = number
        self.title = title
        self.spacing = spacing
        self.content = content()
    }

    public var body: some View {
        Card(padding: EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16), border: .current) {
            VStack(alignment: .leading, spacing: spacing) {
                HStack(spacing: 12) {
                    NumberCircle(number: number)
                    Text(title)
                        .font(.inter(19, .bold))
                        .foregroundStyle(Tokens.text.color)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                content
            }
        }
    }
}

public extension CurrentStepCard {
    /// Body paragraph of a step: 15 muted, line-height 1.45. Inter's natural line is 1.21 em, so
    /// `lineSpacing` = (1.45 - 1.21) * 15 = 3.6 pt.
    static func body(_ text: String) -> some View {
        Text(text)
            .font(.inter(15, .regular))
            .foregroundStyle(Tokens.muted.color)
            .lineSpacing((1.45 - 1.21) * 15)
            .fixedSize(horizontal: false, vertical: true)
    }
}
