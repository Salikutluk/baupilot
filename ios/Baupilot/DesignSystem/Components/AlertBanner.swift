import SwiftUI

/// Rounded banner with a leading icon, a 15/600 title and a 13/400 subtitle (IosAbnMangel:25-31).
/// The tones are parameters: a screen passes its own soft/foreground pair.
public struct AlertBanner: View {
    private let icon: BaupilotIcon
    private let title: String
    private let subtitle: String
    private let background: ColorToken
    private let foreground: ColorToken

    public init(
        icon: BaupilotIcon, title: String, subtitle: String,
        background: ColorToken, foreground: ColorToken
    ) {
        self.icon = icon
        self.title = title
        self.subtitle = subtitle
        self.background = background
        self.foreground = foreground
    }

    public var body: some View {
        HStack(spacing: 10) {
            Icon(icon, size: 20, color: foreground.color)
                .layoutPriority(1)
            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .interText(15, .semibold, lines: .block)
                Text(subtitle)
                    .interText(13, .regular, lines: .block)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .foregroundStyle(foreground.color)
        .padding(.vertical, 12)
        .padding(.horizontal, 14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(RoundedRectangle(cornerRadius: 12).fill(background.color))
        .accessibilityElement(children: .combine)
    }
}
