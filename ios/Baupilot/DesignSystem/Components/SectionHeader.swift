import SwiftUI

/// Section heading: `.title` (20/700, optional trailing link or 14 muted note) on Home, `.caps` (13/600 uppercase) on Profil.
public struct SectionHeader: View {
    public struct Link {
        public let title: String
        public let size: CGFloat
        public let action: (() -> Void)?

        public init(title: String, size: CGFloat = 15, action: (() -> Void)?) {
            self.title = title
            self.size = size
            self.action = action
        }
    }

    public enum Style {
        case title(link: Link? = nil, note: String? = nil)
        /// With a `link` the inset applies on both sides (`margin: 0 4px`, IosAbnElemente:42) and the link sits on the
        /// row's centre line.
        case caps(inset: CGFloat, link: Link? = nil)

        /// 13/600 uppercase with the Profil inset of 14.
        public static var caps: Style { .caps(inset: 14) }
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
        /// `margin: -14px 0` on the floor link (IosAbnElemente:42).
        static let capsLinkMargin: CGFloat = -14
    }

    /// The link with its 44 pt hit area, before the header trims the margins. A nil `action` renders it and does nothing.
    struct LinkButton: View {
        let link: Link

        var body: some View {
            Button(action: link.action ?? {}) {
                Text(link.title)
                    .interText(link.size, .semibold)
                    .foregroundStyle(Tokens.accent.color)
                    .frame(minHeight: Metrics.linkHitHeight)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
        }
    }

    public var body: some View {
        switch style {
        case .title(let link, let note):
            HStack(alignment: .firstTextBaseline) {
                Text(title)
                    .interText(20, .bold)
                    .foregroundStyle(Tokens.text.color)
                    .accessibilityAddTraits(.isHeader)
                Spacer(minLength: 0)
                if let note {
                    Text(note)
                        .interText(14, .regular)
                        .foregroundStyle(Tokens.muted.color)
                }
                if let link {
                    LinkButton(link: link)
                        .padding(.vertical, Metrics.linkMargin)
                }
            }
        case .caps(let inset, let link):
            HStack(alignment: .center, spacing: 0) {
                Text(title)
                    .interText(13, .semibold, tracking: 0.4)
                    .textCase(.uppercase)
                    .foregroundStyle(Tokens.muted.color)
                    .accessibilityAddTraits(.isHeader)
                    .padding(.leading, inset)
                if let link {
                    Spacer(minLength: 0)
                    LinkButton(link: link)
                        .padding(.vertical, Metrics.capsLinkMargin)
                        .padding(.trailing, inset)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}
