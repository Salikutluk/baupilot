import SwiftUI

/// Bar pinned under a screen's content: a 1 pt top border, then the action content padded
/// 12 / 20 / 34 (30 with a caption). Total 97 with one 50 pt button.
public struct BottomActionBar<Content: View>: View {
    public enum BottomPadding {
        case standard
        case withCaption

        var value: CGFloat {
            switch self {
            case .standard: return Tokens.Layout.actionBarPadding.bottom
            case .withCaption: return 30
            }
        }
    }

    private let bottomPadding: BottomPadding
    private let caption: String?
    private let border: ColorToken
    private let content: Content

    public init(
        bottomPadding: BottomPadding = .standard,
        caption: String? = nil,
        border: ColorToken = Tokens.border.core,
        @ViewBuilder content: () -> Content
    ) {
        self.border = border
        self.bottomPadding = bottomPadding
        self.caption = caption
        self.content = content()
    }

    public var body: some View {
        let inset = Tokens.Layout.actionBarPadding
        return VStack(spacing: 0) {
            Rectangle()
                .fill(border.color)
                .frame(height: 1)
            VStack(spacing: 6) {
                content
                if let caption {
                    Text(caption)
                        .font(.inter(12, .regular))
                        .foregroundStyle(Tokens.muted.color)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.top, inset.top)
            .padding(.leading, inset.leading)
            .padding(.trailing, inset.trailing)
            .padding(.bottom, bottomPadding.value)
        }
        .background(Tokens.bar.color)
    }
}
