import SwiftUI

/// A check with a trailing note (IosEsBelegreife:55-62): at least 40 tall, gap 12, centred. The 22 ring has a 2 pt
/// border: done fills it with `ok` and a 12 tick in `okInk`, open draws only the `openRing`. The title (15/400 `text`)
/// wraps greedily and takes the free width; the meta (13 `muted`) keeps its full width.
public struct CheckMetaRow: View {
    private let title: String
    private let meta: String
    private let done: Bool
    private let ok: ColorToken
    private let okInk: ColorToken
    private let openRing: ColorToken

    public init(title: String, meta: String, done: Bool, ok: ColorToken, okInk: ColorToken, openRing: ColorToken) {
        self.title = title
        self.meta = meta
        self.done = done
        self.ok = ok
        self.okInk = okInk
        self.openRing = openRing
    }

    private var ring: some View {
        ZStack {
            if done { Circle().fill(ok.color) }
            Circle().strokeBorder((done ? ok : openRing).color, lineWidth: 2)
            if done { Icon(.checkW35, size: 12, color: okInk.color) }
        }
        .frame(width: 22, height: 22)
        .layoutPriority(1)
    }

    public var body: some View {
        HStack(alignment: .center, spacing: 12) {
            ring
            GreedyLabel(text: title, size: 15, weight: .regular, color: Tokens.text, lineHeight: .normal)
                .frame(maxWidth: .infinity, alignment: .leading)
            Text(meta)
                .interText(13, .regular)
                .foregroundStyle(Tokens.muted.color)
                .fixedSize()
        }
        .frame(minHeight: 40)
        .accessibilityElement(children: .combine)
    }
}
