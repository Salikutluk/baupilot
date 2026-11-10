import SwiftUI

/// Row of a protocol list: a 20 pt status dot, a 14/400 label and a 12 meta text (IosErd:39-46). Place rows inside
/// a `Card(style: .row, padding: EdgeInsets())`; the divider sits on top of every row that is not the first.
public struct ProtocolRow: View {
    public enum Mark {
        case ok
        case defect
    }

    private let label: String
    private let meta: String
    private let mark: Mark
    private let showsDivider: Bool
    private let action: (() -> Void)?

    public init(label: String, meta: String, mark: Mark, showsDivider: Bool, action: (() -> Void)?) {
        self.label = label
        self.meta = meta
        self.mark = mark
        self.showsDivider = showsDivider
        self.action = action
    }

    private var dot: some View {
        ZStack {
            Circle().fill((mark == .ok ? Tokens.ok.core : Tokens.err.core).color)
            Icon(mark == .ok ? .checkW35 : .exclamationW35, size: 11, color: Tokens.okInk.core.color)
        }
        .frame(width: 20, height: 20)
    }

    private var content: some View {
        VStack(spacing: 0) {
            if showsDivider {
                Rectangle().fill(Tokens.border.core.color).frame(height: 1)
            }
            HStack(spacing: 12) {
                dot
                GreedyLabel(text: label, size: 14, weight: .regular, color: Tokens.text, lineHeight: .normal)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text(meta)
                    .interText(12, mark == .ok ? .regular : .bold)
                    .foregroundStyle((mark == .ok ? Tokens.muted : Tokens.eFg).color)
                    .fixedSize()
            }
            .padding(.horizontal, 14)
            .frame(minHeight: 46)
        }
        // A finite height proposal from a parent stack would clip the representable label to it.
        .fixedSize(horizontal: false, vertical: true)
        .contentShape(Rectangle())
    }

    public var body: some View {
        if let action {
            Button(action: action) { content }.buttonStyle(.plain)
        } else {
            content.accessibilityElement(children: .combine)
        }
    }
}
