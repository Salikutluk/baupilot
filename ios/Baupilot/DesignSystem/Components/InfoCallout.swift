import SwiftUI

/// Rounded callout with a leading icon and wrapped text (IosElVerteiler:28, IosEsUebersicht:59,
/// IosSanBad:62, IosEsBelegreife:67, IosVergleich:52).
public struct InfoCallout: View {
    public enum Tone {
        case warn
        case neutralSurface
        case locked
        case accent

        func background(chip: ColorToken) -> ColorToken {
            switch self {
            case .warn: return Tokens.wSoft
            case .neutralSurface: return Tokens.surface
            case .locked: return chip
            case .accent: return Tokens.aSoft
            }
        }

        func foreground(warnFg: ColorToken) -> ColorToken {
            switch self {
            case .warn: return warnFg
            case .neutralSurface, .locked: return Tokens.muted
            case .accent: return Tokens.aFg
            }
        }
    }

    public enum Radius: CGFloat {
        case r12 = 12
        case r14 = 14
    }

    private let text: String
    private let icon: BaupilotIcon
    private let tone: Tone
    private let radius: Radius
    private let textSize: CGFloat
    private let iconSize: CGFloat
    private let chip: ColorToken
    private let warnFg: ColorToken

    /// `chip` (locked tone) and `warnFg` (warn tone) vary per screen.
    public init(
        text: String, icon: BaupilotIcon, tone: Tone, radius: Radius = .r12, textSize: CGFloat = 13,
        iconSize: CGFloat = 18, chip: ColorToken = Tokens.chip.core,
        warnFg: ColorToken = Tokens.wFg.core
    ) {
        self.chip = chip
        self.warnFg = warnFg
        self.text = text
        self.icon = icon
        self.tone = tone
        self.radius = radius
        self.textSize = textSize
        self.iconSize = iconSize
    }

    public var body: some View {
        HStack(alignment: tone == .locked ? .center : .top, spacing: 10) {
            Icon(icon, size: iconSize, color: tone.foreground(warnFg: warnFg).color)
                .padding(.top, tone == .accent ? 1 : 0)
                .layoutPriority(1)
            Text(text)
                .font(.inter(textSize, .regular))
                .foregroundStyle(tone.foreground(warnFg: warnFg).color)
                // CSS line-height 1.4 vs. the natural Inter line height 1.21:
                // lineSpacing = (1.4 - 1.21) * textSize.
                .lineSpacing((1.4 - 1.21) * textSize)
                .multilineTextAlignment(tone == .locked ? .center : .leading)
                .frame(maxWidth: .infinity, alignment: tone == .locked ? .center : .leading)
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 14)
        .background(
            RoundedRectangle(cornerRadius: radius.rawValue)
                .fill(tone.background(chip: chip).color))
    }
}
