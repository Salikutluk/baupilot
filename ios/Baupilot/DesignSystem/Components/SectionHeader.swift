import SwiftUI

/// Section heading: `.title` (20/700, optional trailing link or 14 muted note) on Home, `.caps` (13/600 uppercase) on Profil.
public struct SectionHeader: View {
    public struct Link {
        public let title: String
        public let size: CGFloat
        public let action: (() -> Void)?
        /// Invisible accessibility identifier of the link button (placeholder links, D20).
        public let identifier: String?

        public init(title: String, size: CGFloat = 15, action: (() -> Void)?, identifier: String? = nil) {
            self.title = title
            self.size = size
            self.action = action
            self.identifier = identifier
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
    private let identifier: String?

    /// `identifier` goes on the heading text: on the whole header it would turn a link-less header into one text node
    /// with no font the accessibility audit can check (Dynamic Type finding).
    public init(_ title: String, style: Style, identifier: String? = nil) {
        self.title = title
        self.style = style
        self.identifier = identifier
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

        /// A short title ("Alle 6") is narrower than 44; the hit shape grows to 44 × 44 without moving the layout.
        static var hitShape: some Shape { MinimumHitShape() }

        var body: some View {
            Button(action: link.action ?? {}) {
                Text(link.title)
                    .interText(link.size, .semibold)
                    .foregroundStyle(Tokens.accent.color)
                    .frame(minHeight: Metrics.linkHitHeight)
                    .contentShape(Self.hitShape)
            }
            .buttonStyle(.plain)
            .optionalIdentifier(link.identifier)
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
                    .optionalIdentifier(identifier)
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
                    .optionalIdentifier(identifier)
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

extension View {
    /// Sets the accessibility identifier when one is given; without one the view is returned unchanged.
    @ViewBuilder
    func optionalIdentifier(_ identifier: String?) -> some View {
        if let identifier { accessibilityIdentifier(identifier) } else { self }
    }
}
