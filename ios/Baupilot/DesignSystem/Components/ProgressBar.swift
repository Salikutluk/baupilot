import SwiftUI

/// Horizontal progress bar. `.phase` is the 8 pt home bar (IosHome:36), `.card` the 4 pt
/// card bar (IosElUebersicht:39). `value` is clamped to 0…1.
public struct ProgressBar: View {
    public enum Style {
        case phase
        case card

        var height: CGFloat {
            switch self {
            case .phase: return 8
            case .card: return 4
            }
        }

        var radius: CGFloat {
            switch self {
            case .phase: return 4
            case .card: return 2
            }
        }
    }

    private let value: Double
    private let style: Style
    private let track: ColorToken

    public init(value: Double, style: Style, track: ColorToken = Tokens.track.core) {
        self.value = value
        self.style = style
        self.track = track
    }

    public var body: some View {
        let fraction = CGFloat(min(max(value, 0), 1))
        let shape = RoundedRectangle(cornerRadius: style.radius)
        GeometryReader { proxy in
            ZStack(alignment: .leading) {
                shape.fill(track.color)
                shape.fill(Tokens.accent.color)
                    .frame(width: proxy.size.width * fraction)
            }
        }
        .frame(height: style.height)
        .clipShape(shape)
        .accessibilityHidden(true)
    }
}
