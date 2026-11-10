import SwiftUI

/// Schematic of the straightedge over the floor (IosEsEbenheit:32-36): a 64 tall box, r14, clipped, holding the lath
/// (inset 24, top 18, 8 tall, r2), the floor (inset 16, y 40-50, flat top, half-ellipse bottom) and the vertical gap
/// line at the horizontal centre (2 wide, from y 26, `lineHeight` tall). Decorative, hidden from accessibility.
public struct FlatnessSketch: View {
    /// A flat top with the lower half of an ellipse below it: CSS `border-radius: 0 0 50% 50% / 0 0 100% 100%`.
    struct FloorShape: Shape {
        func path(in rect: CGRect) -> Path {
            var path = Path()
            path.move(to: CGPoint(x: rect.minX, y: rect.minY))
            path.addLine(to: CGPoint(x: rect.maxX, y: rect.minY))
            let kappa: CGFloat = 0.5522847498
            let rx = rect.width / 2
            path.addCurve(
                to: CGPoint(x: rect.midX, y: rect.maxY),
                control1: CGPoint(x: rect.maxX, y: rect.minY + kappa * rect.height),
                control2: CGPoint(x: rect.midX + kappa * rx, y: rect.maxY))
            path.addCurve(
                to: CGPoint(x: rect.minX, y: rect.minY),
                control1: CGPoint(x: rect.midX - kappa * rx, y: rect.maxY),
                control2: CGPoint(x: rect.minX, y: rect.minY + kappa * rect.height))
            path.closeSubpath()
            return path
        }
    }

    static let height: CGFloat = 64

    private let lineHeight: CGFloat
    private let line: ColorToken
    private let lath: ColorToken
    private let floor: ColorToken
    private let background: ColorToken

    public init(lineHeight: CGFloat, line: ColorToken, lath: ColorToken, floor: ColorToken, background: ColorToken) {
        self.lineHeight = lineHeight
        self.line = line
        self.lath = lath
        self.floor = floor
        self.background = background
    }

    public var body: some View {
        ZStack(alignment: .topLeading) {
            RoundedRectangle(cornerRadius: 2)
                .fill(lath.color)
                .frame(height: 8)
                .padding(.horizontal, 24)
                .offset(y: 18)
            FloorShape()
                .fill(floor.color)
                .frame(height: 10)
                .padding(.horizontal, 16)
                .offset(y: 40)
            GeometryReader { proxy in
                Rectangle()
                    .fill(line.color)
                    .frame(width: 2, height: lineHeight)
                    .offset(x: proxy.size.width / 2, y: 26)
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: Self.height)
        .background(background.color)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .accessibilityHidden(true)
    }
}
