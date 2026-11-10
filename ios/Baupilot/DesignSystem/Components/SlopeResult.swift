import SwiftUI

/// Live result of the slope check (IosSanGefaelle:68-75): a 92 x 44 schema of the pipe next to the percentage (26/700)
/// and a one-line verdict (13/600), gap 14, padding 14, r12, in `foreground` on `background`.
public struct SlopeResult: View {
    /// The pipe drawn against the dashed level line (SVG `viewBox 0 0 92 44`), all strokes in one colour.
    struct Schema: View {
        static let size = CGSize(width: 92, height: 44)

        let y2: CGFloat
        let levelLabel: String
        let deltaLabel: String
        let color: ColorToken

        var body: some View {
            Canvas { context, _ in
                let ink = color.color
                var level = Path()
                level.move(to: CGPoint(x: 2, y: 12))
                level.addLine(to: CGPoint(x: 90, y: 12))
                context.stroke(
                    level, with: .color(ink.opacity(0.6)), style: StrokeStyle(lineWidth: 1, dash: [3, 3]))
                var pipe = Path()
                pipe.move(to: CGPoint(x: 4, y: 12))
                pipe.addLine(to: CGPoint(x: 78, y: y2))
                context.stroke(pipe, with: .color(ink), style: StrokeStyle(lineWidth: 7, lineCap: .round))
                var marker = Path()
                marker.move(to: CGPoint(x: 86, y: 12))
                marker.addLine(to: CGPoint(x: 86, y: y2))
                context.stroke(marker, with: .color(ink), style: StrokeStyle(lineWidth: 1.5))
                label(levelLabel, size: 8, weight: .semibold, opacity: 0.8, baseline: CGPoint(x: 2, y: 8), in: &context)
                label(deltaLabel, size: 9, weight: .bold, opacity: 1, baseline: CGPoint(x: 76, y: 42), in: &context)
            }
            .frame(width: Self.size.width, height: Self.size.height)
            .accessibilityHidden(true)
        }

        /// SVG `text` sits on its baseline at `baseline`; the resolved text reports where its own baseline is.
        private func label(
            _ text: String, size: CGFloat, weight: InterWeight, opacity: Double, baseline: CGPoint,
            in context: inout GraphicsContext
        ) {
            let resolved = context.resolve(
                Text(text).font(.inter(size, weight)).foregroundStyle(color.color.opacity(opacity)))
            let natural = resolved.measure(in: CGSize(width: CGFloat.infinity, height: CGFloat.infinity))
            context.draw(
                resolved, at: CGPoint(x: baseline.x, y: baseline.y - resolved.firstBaseline(in: natural)),
                anchor: .topLeading)
        }
    }

    private let percent: String
    private let verdict: String
    private let schemaY2: CGFloat
    private let levelLabel: String
    private let deltaLabel: String
    private let background: ColorToken
    private let foreground: ColorToken
    private let identifiers: (percent: String, verdict: String)?

    public init(
        percent: String, verdict: String, schemaY2: CGFloat, levelLabel: String, deltaLabel: String,
        background: ColorToken, foreground: ColorToken, identifiers: (percent: String, verdict: String)? = nil
    ) {
        self.percent = percent
        self.verdict = verdict
        self.schemaY2 = schemaY2
        self.levelLabel = levelLabel
        self.deltaLabel = deltaLabel
        self.background = background
        self.foreground = foreground
        self.identifiers = identifiers
    }

    public var body: some View {
        HStack(spacing: 14) {
            Schema(y2: schemaY2, levelLabel: levelLabel, deltaLabel: deltaLabel, color: foreground)
            VStack(alignment: .leading, spacing: 2) {
                Text(percent)
                    .interText(26, .bold, tracking: -0.4)
                    .accessibilityIdentifier(identifiers?.percent ?? "")
                Text(verdict)
                    .interText(13, .semibold)
                    .fixedSize(horizontal: true, vertical: false)
                    .accessibilityIdentifier(identifiers?.verdict ?? "")
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .foregroundStyle(foreground.color)
        .padding(14)
        .frame(maxWidth: .infinity)
        .background(RoundedRectangle(cornerRadius: 12).fill(background.color))
    }
}
