import CoreGraphics

/// SVG `preserveAspectRatio="xMidYMid slice"` (IosAbnPruefung:51): scale to cover, centre the overflow. Pure maths, no UI.
public enum SliceTransform {
    public struct Fit: Equatable, Sendable {
        public let scale: CGFloat
        public let offsetX: CGFloat
        public let offsetY: CGFloat

        public init(scale: CGFloat, offsetX: CGFloat, offsetY: CGFloat) {
            self.scale = scale
            self.offsetX = offsetX
            self.offsetY = offsetY
        }
    }

    public static func fit(viewBox: CGSize, into size: CGSize) -> Fit {
        let scale = max(size.width / viewBox.width, size.height / viewBox.height)
        return Fit(
            scale: scale,
            offsetX: (size.width - viewBox.width * scale) / 2,
            offsetY: (size.height - viewBox.height * scale) / 2
        )
    }
}
