import CoreGraphics

/// Start and end point of a CSS `linear-gradient(<angle>, …)` in a `width` × `height` box. Pure maths, no UI.
public enum CssGradient {
    public struct Line: Equatable, Sendable {
        public let start: CGPoint
        public let end: CGPoint

        public init(start: CGPoint, end: CGPoint) {
            self.start = start
            self.end = end
        }
    }

    /// CSS convention: 0° points up, angles grow clockwise; the gradient line passes the centre and is
    /// `|W·sinθ| + |H·cosθ|` long, so the corners get the first and last stop.
    public static func endpoints(angleDegrees: Double, width: CGFloat, height: CGFloat) -> Line {
        let radians = angleDegrees * .pi / 180
        let sine = CGFloat(sin(radians))
        let cosine = CGFloat(cos(radians))
        let length = abs(width * sine) + abs(height * cosine)
        let centre = CGPoint(x: width / 2, y: height / 2)
        let dx = sine * length / 2
        let dy = -cosine * length / 2
        return Line(
            start: CGPoint(x: centre.x - dx, y: centre.y - dy),
            end: CGPoint(x: centre.x + dx, y: centre.y + dy)
        )
    }
}
