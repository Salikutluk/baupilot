import SwiftUI

/// Round countdown (IosSanDruck:48-50): a 72 pt disc whose `fill` runs clockwise from 12 o'clock to
/// `fraction × 360°` with a hard stop, `track` for the rest, a centred `hole` disc and a 14 pt label. An image with
/// `accessibilityLabel`.
public struct TimerRing: View {
    private let text: String
    private let fraction: Double
    private let accessibilityLabel: String
    private let fill: ColorToken
    private let track: ColorToken
    private let hole: ColorToken
    private let diameter: CGFloat
    private let holeDiameter: CGFloat
    private let textWeight: InterWeight

    public init(
        text: String, fraction: Double, accessibilityLabel: String, fill: ColorToken, track: ColorToken,
        hole: ColorToken, diameter: CGFloat = 72, holeDiameter: CGFloat, textWeight: InterWeight
    ) {
        self.text = text
        self.fraction = fraction
        self.accessibilityLabel = accessibilityLabel
        self.fill = fill
        self.track = track
        self.hole = hole
        self.diameter = diameter
        self.holeDiameter = holeDiameter
        self.textWeight = textWeight
    }

    /// The pie from 12 o'clock clockwise.
    private struct Wedge: Shape {
        let fraction: Double

        func path(in rect: CGRect) -> Path {
            let centre = CGPoint(x: rect.midX, y: rect.midY)
            var path = Path()
            path.move(to: centre)
            path.addArc(
                center: centre, radius: rect.width / 2, startAngle: .degrees(-90),
                endAngle: .degrees(-90 + min(max(fraction, 0), 1) * 360), clockwise: false)
            path.closeSubpath()
            return path
        }
    }

    public var body: some View {
        ZStack {
            Circle().fill(track.color)
            Wedge(fraction: fraction).fill(fill.color)
            Circle().fill(hole.color).frame(width: holeDiameter, height: holeDiameter)
            Text(text)
                .interText(14, textWeight)
                .foregroundStyle(Tokens.text.color)
        }
        .frame(width: diameter, height: diameter)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityLabel)
        .accessibilityAddTraits(.isImage)
    }
}
