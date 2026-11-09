import SwiftUI

/// One "next step" line: a 22 pt ring (ticked when done) and the title.
public struct ChecklistRow: View {
    /// 22 pt border-box ring, border 2. Done: filled `ok` with a `okInk` tick; open: hollow `muted` ring.
    struct Ring: View {
        let done: Bool

        var body: some View {
            ZStack {
                if done {
                    Circle().fill(Tokens.ok.core.color)
                    Icon(.checkW35, size: 12, color: Tokens.okInk.core.color)
                } else {
                    Circle().strokeBorder(Tokens.muted.color, lineWidth: 2)
                }
            }
            .frame(width: 22, height: 22)
        }
    }

    private let title: String
    private let done: Bool

    public init(title: String, done: Bool) {
        self.title = title
        self.done = done
    }

    public var body: some View {
        HStack(spacing: 12) {
            Ring(done: done)
            Text(title)
                .interText(15, .regular)
                .foregroundStyle((done ? Tokens.muted : Tokens.text).color)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .accessibilityElement(children: .combine)
    }
}
