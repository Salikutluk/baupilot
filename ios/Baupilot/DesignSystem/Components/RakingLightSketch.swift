import SwiftUI

/// Schematic of the raking-light test (IosMaRaum:44-48): an 88 tall box, r12, clipped, filled with a 100° gradient from
/// `lit` (0 %) to `wall` (70 %), holding the torch (left 14, top 34, 34 x 18, r4), its glow (left 48, top 39, 8 x 8, r4)
/// and the cast shadow (x at 62 % of the width, top 22, 2 x 44, opacity 0.6). Decorative, hidden from accessibility.
public struct RakingLightSketch: View {
    static let height: CGFloat = 88

    private let lit: ColorToken
    private let wall: ColorToken
    private let lamp: ColorToken
    private let glow: ColorToken
    private let shadow: ColorToken

    public init(lit: ColorToken, wall: ColorToken, lamp: ColorToken, glow: ColorToken, shadow: ColorToken) {
        self.lit = lit
        self.wall = wall
        self.lamp = lamp
        self.glow = glow
        self.shadow = shadow
    }

    public var body: some View {
        GeometryReader { proxy in
            let size = proxy.size
            let line = CssGradient.endpoints(angleDegrees: 100, width: size.width, height: size.height)
            ZStack(alignment: .topLeading) {
                LinearGradient(
                    stops: [
                        Gradient.Stop(color: lit.color, location: 0),
                        Gradient.Stop(color: wall.color, location: 0.7),
                    ],
                    startPoint: UnitPoint(x: line.start.x / size.width, y: line.start.y / size.height),
                    endPoint: UnitPoint(x: line.end.x / size.width, y: line.end.y / size.height))
                RoundedRectangle(cornerRadius: 4)
                    .fill(lamp.color)
                    .frame(width: 34, height: 18)
                    .offset(x: 14, y: 34)
                RoundedRectangle(cornerRadius: 4)
                    .fill(glow.color)
                    .frame(width: 8, height: 8)
                    .offset(x: 48, y: 39)
                Rectangle()
                    .fill(shadow.color)
                    .frame(width: 2, height: 44)
                    .opacity(0.6)
                    .offset(x: size.width * 0.62, y: 22)
            }
            .frame(width: size.width, height: size.height, alignment: .topLeading)
        }
        .frame(maxWidth: .infinity)
        .frame(height: Self.height)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .accessibilityHidden(true)
    }
}
