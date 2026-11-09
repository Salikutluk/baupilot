import SwiftUI

/// The photo tile's stand-in picture (IosAbnPruefung:51): a 160° sky gradient with a window and a spirit level on it.
/// The gradient line comes from `CssGradient`, the scene's scale and offset from `SliceTransform` (SVG
/// `xMidYMid slice`); the shapes come from `PhotoSceneGeometry`. It fills its container; the tile clips it.
public struct PhotoPlaceholder: View {
    public init() {}

    public var body: some View {
        GeometryReader { proxy in
            let size = proxy.size
            let line = CssGradient.endpoints(
                angleDegrees: PhotoSceneGeometry.gradientAngleDegrees, width: size.width, height: size.height)
            let fit = SliceTransform.fit(viewBox: PhotoSceneGeometry.viewBox, into: size)
            ZStack {
                LinearGradient(
                    stops: zip(PhotoSceneGeometry.gradientStops, stopColors).map {
                        Gradient.Stop(color: $1.color, location: $0)
                    },
                    startPoint: UnitPoint(x: line.start.x / size.width, y: line.start.y / size.height),
                    endPoint: UnitPoint(x: line.end.x / size.width, y: line.end.y / size.height))
                Canvas { context, _ in
                    context.translateBy(x: fit.offsetX, y: fit.offsetY)
                    context.scaleBy(x: fit.scale, y: fit.scale)
                    let frame = GraphicsContext.Shading.color(Tokens.photoFrame.color)
                    context.stroke(
                        Path(PhotoSceneGeometry.frame), with: frame, lineWidth: PhotoSceneGeometry.frameLineWidth)
                    var mullion = Path()
                    mullion.move(to: PhotoSceneGeometry.mullion.from)
                    mullion.addLine(to: PhotoSceneGeometry.mullion.to)
                    context.stroke(mullion, with: frame, lineWidth: PhotoSceneGeometry.mullionLineWidth)
                    context.fill(
                        Path(
                            roundedRect: PhotoSceneGeometry.level.rect,
                            cornerRadius: PhotoSceneGeometry.level.cornerRadius),
                        with: .color(Tokens.photoLevel.color))
                }
            }
            .frame(width: size.width, height: size.height)
        }
        .accessibilityHidden(true)
    }

    private var stopColors: [ColorToken] { [Tokens.photoSkyTop, Tokens.photoSkyMid, Tokens.photoSkyBottom] }
}
