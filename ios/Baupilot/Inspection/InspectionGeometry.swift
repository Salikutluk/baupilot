import CoreGraphics

/// The "So prüfen" sketch: a window with a spirit level on top and on the side (IosAbnPruefung:50). SVG defaults:
/// butt caps, miter joins. Colours are chosen by the view (stroke `muted`, level `accent`, bubble `chip`).
public enum WindowLevelSketch {
    public enum Geometry {
        public struct RoundedRect: Equatable, Sendable {
            public let rect: CGRect
            public let cornerRadius: CGFloat

            public init(rect: CGRect, cornerRadius: CGFloat) {
                self.rect = rect
                self.cornerRadius = cornerRadius
            }
        }

        public struct Line: Equatable, Sendable {
            public let from: CGPoint
            public let to: CGPoint

            public init(from: CGPoint, to: CGPoint) {
                self.from = from
                self.to = to
            }
        }

        public struct Circle: Equatable, Sendable {
            public let center: CGPoint
            public let radius: CGFloat

            public init(center: CGPoint, radius: CGFloat) {
                self.center = center
                self.radius = radius
            }
        }

        // source: IosAbnPruefung.dc.html:50 (viewBox 0 0 56 44; AndAbnPruefung.dc.html:57 holds the same shapes)
        public static let size = CGSize(width: 56, height: 44)
        public static let frame = RoundedRect(rect: CGRect(x: 8, y: 12, width: 32, height: 30), cornerRadius: 1.5)
        public static let frameLineWidth: CGFloat = 2
        public static let mullion = Line(from: CGPoint(x: 24, y: 12), to: CGPoint(x: 24, y: 42))
        public static let mullionLineWidth: CGFloat = 1.5
        public static let topLevel = RoundedRect(rect: CGRect(x: 6, y: 3, width: 36, height: 6), cornerRadius: 2)
        public static let topBubble = Circle(center: CGPoint(x: 24, y: 6), radius: 2)
        public static let sideLevel = RoundedRect(rect: CGRect(x: 45, y: 12, width: 6, height: 30), cornerRadius: 2)
        public static let sideBubble = Circle(center: CGPoint(x: 48, y: 27), radius: 2)
    }
}

/// The photo tile's scene (IosAbnPruefung:51): a CSS gradient background plus an SVG drawn in viewBox units.
public enum PhotoSceneGeometry {
    // source: IosAbnPruefung.dc.html:51 / AndAbnPruefung.dc.html:58
    public static let viewBox = CGSize(width: 100, height: 80)
    public static let gradientAngleDegrees: Double = 160
    public static let gradientStops: [CGFloat] = [0, 0.6, 1]
    public static let frame = CGRect(x: 22, y: 10, width: 56, height: 62)
    public static let frameLineWidth: CGFloat = 5
    public static let mullion = WindowLevelSketch.Geometry.Line(from: CGPoint(x: 50, y: 10), to: CGPoint(x: 50, y: 72))
    public static let mullionLineWidth: CGFloat = 3
    public static let level = WindowLevelSketch.Geometry.RoundedRect(
        rect: CGRect(x: 18, y: 4, width: 64, height: 7),
        cornerRadius: 2
    )
}

/// The craftsman's signature (IosAbnSignatur:57): `M6 34` plus nine absolute cubic segments in a 120 × 48 box,
/// stroke 2, round cap and join.
public enum SignatureSample {
    public struct Segment: Equatable, Sendable {
        public let control1: CGPoint
        public let control2: CGPoint
        public let end: CGPoint

        init(_ x1: CGFloat, _ y1: CGFloat, _ x2: CGFloat, _ y2: CGFloat, _ x: CGFloat, _ y: CGFloat) {
            control1 = CGPoint(x: x1, y: y1)
            control2 = CGPoint(x: x2, y: y2)
            end = CGPoint(x: x, y: y)
        }
    }

    // source: IosAbnSignatur.dc.html:57 / AndAbnSignatur.dc.html:60, path "M6 34c8-20 14-24 16-16s-6 18 …" made absolute
    public static let size = CGSize(width: 120, height: 48)
    public static let lineWidth: CGFloat = 2
    public static let start = CGPoint(x: 6, y: 34)
    public static let segments: [Segment] = [
        Segment(14, 14, 20, 10, 22, 18),
        Segment(24, 26, 16, 36, 22, 32),
        Segment(28, 28, 34, 10, 38, 12),
        Segment(42, 14, 36, 30, 42, 28),
        Segment(48, 26, 50, 16, 54, 18),
        Segment(58, 20, 56, 28, 62, 26),
        Segment(68, 24, 72, 18, 76, 20),
        Segment(80, 22, 82, 26, 90, 22),
        Segment(98, 18, 102, 16, 110, 14),
    ]
}
