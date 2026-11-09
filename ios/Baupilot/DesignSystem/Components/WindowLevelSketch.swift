import SwiftUI

/// The "So prüfen" sketch (IosAbnPruefung:50): a window with a spirit level on top and on the side, 56 × 44.
/// The shapes come from `WindowLevelSketch.Geometry`; SVG defaults apply (butt caps, miter joins). The view is
/// named `WindowLevelSketchView` because `WindowLevelSketch` is the geometry namespace.
public struct WindowLevelSketchView: View {
    private let stroke: ColorToken
    private let level: ColorToken
    private let bubble: ColorToken

    public init(stroke: ColorToken, level: ColorToken, bubble: ColorToken) {
        self.stroke = stroke
        self.level = level
        self.bubble = bubble
    }

    public var body: some View {
        Canvas { context, _ in
            typealias Geometry = WindowLevelSketch.Geometry
            let strokeColor = GraphicsContext.Shading.color(stroke.color)
            let levelColor = GraphicsContext.Shading.color(level.color)
            let bubbleColor = GraphicsContext.Shading.color(bubble.color)

            func rounded(_ shape: Geometry.RoundedRect) -> Path {
                Path(roundedRect: shape.rect, cornerRadius: shape.cornerRadius)
            }
            func circle(_ shape: Geometry.Circle) -> Path {
                Path(
                    ellipseIn: CGRect(
                        x: shape.center.x - shape.radius, y: shape.center.y - shape.radius,
                        width: shape.radius * 2, height: shape.radius * 2))
            }

            context.stroke(rounded(Geometry.frame), with: strokeColor, lineWidth: Geometry.frameLineWidth)
            var mullion = Path()
            mullion.move(to: Geometry.mullion.from)
            mullion.addLine(to: Geometry.mullion.to)
            context.stroke(mullion, with: strokeColor, lineWidth: Geometry.mullionLineWidth)
            context.fill(rounded(Geometry.topLevel), with: levelColor)
            context.fill(circle(Geometry.topBubble), with: bubbleColor)
            context.fill(rounded(Geometry.sideLevel), with: levelColor)
            context.fill(circle(Geometry.sideBubble), with: bubbleColor)
        }
        .frame(width: WindowLevelSketch.Geometry.size.width, height: WindowLevelSketch.Geometry.size.height)
        .accessibilityHidden(true)
    }
}
