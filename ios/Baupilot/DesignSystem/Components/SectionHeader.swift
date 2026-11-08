import SwiftUI

/// Section heading: `.title` (20/700, optional trailing link) on Home, `.caps` (13/600 uppercase) on Profil.
public struct SectionHeader: View {
    public struct Link {
        public let title: String
        public let action: (() -> Void)?

        public init(title: String, action: (() -> Void)?) {
            self.title = title
            self.action = action
        }
    }

    public enum Style {
        case title(link: Link? = nil)
        case caps
    }

    private let title: String
    private let style: Style

    public init(_ title: String, style: Style) {
        self.title = title
        self.style = style
    }

    private enum Metrics {
        static let linkHitHeight: CGFloat = 44
        /// `margin: -12px 0` on the link (IosHome:50) keeps it at the title's line height.
        static let linkMargin: CGFloat = -12
        static let capsInset: CGFloat = 14
    }

    /// The link with its 44 pt hit area, before the header trims the margins. A nil `action` renders it and does nothing.
    struct LinkButton: View {
        let link: Link

        var body: some View {
            Button(action: link.action ?? {}) {
                Text(link.title)
                    .interText(15, .semibold)
                    .foregroundStyle(Tokens.accent.color)
                    .frame(minHeight: Metrics.linkHitHeight)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
        }
    }

    public var body: some View {
        switch style {
        case .title(let link):
            HStack(alignment: .firstTextBaseline) {
                Text(title)
                    .interText(20, .bold)
                    .foregroundStyle(Tokens.text.color)
                    .accessibilityAddTraits(.isHeader)
                Spacer(minLength: 0)
                if let link {
                    LinkButton(link: link)
                        .padding(.vertical, Metrics.linkMargin)
                }
            }
        case .caps:
            Text(title)
                .interText(13, .semibold, tracking: 0.4)
                .textCase(.uppercase)
                .foregroundStyle(Tokens.muted.color)
                .padding(.leading, Metrics.capsInset)
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityAddTraits(.isHeader)
        }
    }
}
