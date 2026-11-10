import SwiftUI

/// A check with a hint (IosElRauchmelder:44-52): a 22 ring with a 2 pt border, margin-top 1, gap 12, top-aligned
/// with the text column (15/600 title, 13 x 1.4 `muted` hint, gap 2). Done fills the ring with `ok` and a 12 tick in
/// `okInk`; open draws a `track` ring.
public struct CheckHintRow: View {
    private let title: String
    private let hint: String
    private let done: Bool
    private let ok: ColorToken
    private let okInk: ColorToken
    private let track: ColorToken

    public init(
        title: String, hint: String, done: Bool, ok: ColorToken, okInk: ColorToken, track: ColorToken
    ) {
        self.title = title
        self.hint = hint
        self.done = done
        self.ok = ok
        self.okInk = okInk
        self.track = track
    }

    private var ring: some View {
        ZStack {
            Circle().strokeBorder((done ? ok : track).color, lineWidth: 2)
            if done {
                Circle().fill(ok.color)
                Icon(.checkW35, size: 12, color: okInk.color)
            }
        }
        .frame(width: 22, height: 22)
        .padding(.top, 1)
        .layoutPriority(1)
    }

    public var body: some View {
        HStack(alignment: .top, spacing: 12) {
            ring
            VStack(alignment: .leading, spacing: 2) {
                GreedyLabel(text: title, size: 15, weight: .semibold, color: Tokens.text, lineHeight: .normal)
                GreedyLabel(text: hint, size: 13, weight: .regular, color: Tokens.muted, lineHeight: .factor(1.4))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .fixedSize(horizontal: false, vertical: true)
        .accessibilityElement(children: .combine)
    }
}
