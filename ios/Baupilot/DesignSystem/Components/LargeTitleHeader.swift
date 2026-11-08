import SwiftUI

/// Large screen title with an optional kicker above it and an optional trailing control.
public struct LargeTitleHeader: View {
    public enum Trailing {
        case none
        case bell(unread: Bool, label: String, action: (() -> Void)?)
        case add(label: String, action: (() -> Void)?)
        case link(text: String, action: (() -> Void)?)
    }

    private let title: String
    private let kicker: String?
    private let trailing: Trailing

    public init(title: String, kicker: String? = nil, trailing: Trailing = .none) {
        self.title = title
        self.kicker = kicker
        self.trailing = trailing
    }

    public var body: some View {
        HStack(alignment: .bottom) {
            VStack(alignment: .leading, spacing: 2) {
                if let kicker {
                    Text(kicker)
                        .interText(13, .semibold)
                        .foregroundStyle(Tokens.muted.color)
                }
                Text(title)
                    .interText(32, .bold, tracking: -0.6)
                    .foregroundStyle(Tokens.text.color)
                    .accessibilityAddTraits(.isHeader)
            }
            Spacer(minLength: 0)
            TrailingView(trailing: trailing)
        }
    }

    /// The trailing control exactly as the header places it in its row.
    struct TrailingView: View {
        let trailing: Trailing

        var body: some View {
            switch trailing {
            case .none:
                EmptyView()
            case let .bell(unread, label, action):
                Button(action: { action?() }) {
                    ZStack {
                        Circle().fill(Tokens.surface.color)
                        Icon(.bell, size: 20, color: Tokens.text.color)
                        if unread {
                            Circle()
                                .fill(Tokens.accent.color)
                                .frame(width: 8, height: 8)
                                .padding(.top, 10)
                                .padding(.trailing, 11)
                                .frame(width: 44, height: 44, alignment: .topTrailing)
                        }
                    }
                    .frame(width: 44, height: 44)
                }
                .buttonStyle(.plain)
                .accessibilityLabel(label)
            case let .add(label, action):
                Button(action: { action?() }) {
                    ZStack {
                        Circle().fill(Tokens.accent.color)
                        Icon(.plusW24, size: 20, color: Tokens.onAccent.color)
                    }
                    .frame(width: 44, height: 44)
                }
                .buttonStyle(.plain)
                .accessibilityLabel(label)
            case let .link(text, action):
                Button(action: { action?() }) {
                    Text(text)
                        .interText(15, .semibold)
                        .foregroundStyle(Tokens.accent.color)
                        .frame(minHeight: 44)
                }
                .buttonStyle(.plain)
            }
        }
    }
}
