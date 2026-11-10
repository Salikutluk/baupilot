import SwiftUI

/// Banner on top of a protocol screen: icon, 15/700 title over a 13/400 text, and an optional trailing document
/// button or chevron (IosErd:29-37, IosRohbau). The tones are parameters; a screen passes its own soft/foreground
/// pair. With an `action` the whole banner is one button.
public struct ProtocolBanner: View {
    public enum Trailing {
        /// 36 pt `surface` button with a 14/600 label; its hit area is 44.
        case document(label: String, action: (() -> Void)?)
        /// `chevronRightW24` at 14 in the foreground colour.
        case chevron
        case none
    }

    /// The document button: 36 tall, padding 0 12, r10. The hit shape grows 4 pt on every side (44 tall).
    struct DocumentButton: View {
        static let hitInset: CGFloat = -4

        /// The tappable shape: the drawn box inset by -4 on all sides.
        static var hitShape: some Shape { Rectangle().inset(by: hitInset) }

        let label: String
        var action: (() -> Void)?

        var body: some View {
            let face = Text(label)
                .interText(14, .semibold)
                .foregroundStyle(Tokens.text.color)
                .fixedSize()
                .padding(.horizontal, 12)
                .frame(height: 36)
                .background(RoundedRectangle(cornerRadius: 10).fill(Tokens.surface.color))
                .contentShape(Self.hitShape)
            if let action {
                Button(action: action) { face }.buttonStyle(.plain)
            } else {
                face
            }
        }
    }

    private let icon: BaupilotIcon
    private let title: String
    private let text: String
    private let background: ColorToken
    private let foreground: ColorToken
    private let trailing: Trailing
    private let action: (() -> Void)?

    public init(
        icon: BaupilotIcon, title: String, text: String, background: ColorToken, foreground: ColorToken,
        trailing: Trailing = .none, action: (() -> Void)?
    ) {
        self.icon = icon
        self.title = title
        self.text = text
        self.background = background
        self.foreground = foreground
        self.trailing = trailing
        self.action = action
    }

    private var banner: some View {
        HStack(spacing: 12) {
            Icon(icon, size: 22, color: foreground.color)
                .layoutPriority(1)
            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .interText(15, .bold, lines: .block)
                Text(text)
                    .interText(13, .regular, lines: .block)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            switch trailing {
            case .document(let label, let documentAction):
                DocumentButton(label: label, action: documentAction)
                    .layoutPriority(1)
            case .chevron:
                Icon(.chevronRightW24, size: 14, color: foreground.color)
                    .layoutPriority(1)
            case .none:
                EmptyView()
            }
        }
        .foregroundStyle(foreground.color)
        .padding(.vertical, 12)
        .padding(.horizontal, 14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(RoundedRectangle(cornerRadius: 14).fill(background.color))
    }

    public var body: some View {
        if let action {
            Button(action: action) { banner }.buttonStyle(.plain)
        } else {
            banner.accessibilityElement(children: .combine)
        }
    }
}
