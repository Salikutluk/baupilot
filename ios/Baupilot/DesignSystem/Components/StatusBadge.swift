import SwiftUI

/// Pill label for a status. Colours come from the tone; the screen picks the tone.
public struct StatusBadge: View {
    public enum Tone {
        case accent
        case ok
        case info
        case error(eSoft: ColorToken = Tokens.eSoft.core)
        case warn(fg: ColorToken = Tokens.wFg.core)
        case neutral(bg: ColorToken = Tokens.chip.core)
        case open

        var background: ColorToken {
            switch self {
            case .accent: return Tokens.aSoft
            case .ok: return Tokens.oSoft
            case .info: return Tokens.iSoft
            case .error(let eSoft): return eSoft
            case .warn: return Tokens.wSoft
            case .neutral(let bg): return bg
            case .open: return Tokens.open
            }
        }

        var foreground: ColorToken {
            switch self {
            case .accent: return Tokens.aFg
            case .ok: return Tokens.oFg
            case .info: return Tokens.iFg
            case .error: return Tokens.eFg
            case .warn(let fg): return fg
            case .neutral: return Tokens.muted
            case .open: return Tokens.text
            }
        }
    }

    public enum Size {
        case compact
        case roomy
        case mini

        var fontSize: CGFloat { self == .mini ? 11 : 12 }
        var horizontal: CGFloat {
            switch self {
            case .compact: return 8
            case .roomy: return 9
            case .mini: return 7
            }
        }
        var vertical: CGFloat {
            switch self {
            case .compact: return 4
            case .roomy: return 5
            case .mini: return 3
            }
        }
        var radius: CGFloat {
            switch self {
            case .compact: return 7
            case .roomy: return 8
            case .mini: return 6
            }
        }
    }

    private let text: String
    private let tone: Tone
    private let size: Size
    private let weight: InterWeight

    public init(_ text: String, tone: Tone, size: Size = .compact, weight: InterWeight = .semibold) {
        self.text = text
        self.tone = tone
        self.size = size
        self.weight = weight
    }

    public var body: some View {
        Text(text)
            .interText(size.fontSize, weight)
            .foregroundStyle(tone.foreground.color)
            .lineLimit(1)
            .fixedSize()
            .padding(.horizontal, size.horizontal)
            .padding(.vertical, size.vertical)
            .background(tone.background.color, in: RoundedRectangle(cornerRadius: size.radius))
    }
}
