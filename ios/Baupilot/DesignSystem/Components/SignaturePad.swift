import SwiftUI

/// Drawing area of a signature tile: draws the craftsman's `SignatureSample` (centred in its 120 × 48 box) or the
/// live `SignatureStrokes`, stroke 2, round cap and join. A non-nil binding makes it a drawing surface.
public struct SignaturePad: View {
    /// Outer limit of a fresh strokes value; the pad clamps every point to its own size before storing it.
    public static let emptyStrokes = SignatureStrokes(bounds: CGRect(x: 0, y: 0, width: 4000, height: 4000))

    private let strokes: Binding<SignatureStrokes>?
    private let sample: Bool
    private let color: ColorToken
    private let accessibilityLabel: String

    /// Where the running drag started. A drag the system cancels never reaches `onEnded`; the next drag starts
    /// elsewhere, so a changed start location opens a new stroke instead of extending the old one.
    @State private var dragStart: CGPoint?

    public init(
        strokes: Binding<SignatureStrokes>?, sample: Bool, color: ColorToken, accessibilityLabel: String
    ) {
        self.strokes = strokes
        self.sample = sample
        self.color = color
        self.accessibilityLabel = accessibilityLabel
    }

    static func clamp(_ point: CGPoint, to size: CGSize) -> CGPoint {
        CGPoint(x: min(max(point.x, 0), size.width), y: min(max(point.y, 0), size.height))
    }

    private static let style = StrokeStyle(lineWidth: SignatureSample.lineWidth, lineCap: .round, lineJoin: .round)

    public var body: some View {
        GeometryReader { proxy in
            Canvas { context, canvasSize in
                var path = Path()
                if sample {
                    let origin = CGPoint(
                        x: (canvasSize.width - SignatureSample.size.width) / 2,
                        y: (canvasSize.height - SignatureSample.size.height) / 2)
                    func place(_ point: CGPoint) -> CGPoint { CGPoint(x: origin.x + point.x, y: origin.y + point.y) }
                    path.move(to: place(SignatureSample.start))
                    for segment in SignatureSample.segments {
                        path.addCurve(
                            to: place(segment.end), control1: place(segment.control1),
                            control2: place(segment.control2))
                    }
                }
                for stroke in strokes?.wrappedValue.strokes ?? [] {
                    guard let first = stroke.first else { continue }
                    path.move(to: first)
                    if stroke.count == 1 { path.addLine(to: first) }
                    for point in stroke.dropFirst() { path.addLine(to: point) }
                }
                context.stroke(path, with: .color(color.color), style: Self.style)
            }
            .contentShape(Rectangle())
            .gesture(drawing(size: proxy.size), including: strokes == nil ? .none : .all)
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityLabel)
        .accessibilityValue(strokes.map { "strokes: \($0.wrappedValue.strokes.count)" } ?? "")
    }

    private func drawing(size: CGSize) -> some Gesture {
        DragGesture(minimumDistance: 0)
            .onChanged { value in
                guard let strokes else { return }
                let point = Self.clamp(value.location, to: size)
                if dragStart == value.startLocation {
                    strokes.wrappedValue.extend(to: point)
                } else {
                    if dragStart != nil { strokes.wrappedValue.end() }
                    dragStart = value.startLocation
                    strokes.wrappedValue.begin(at: point)
                }
            }
            .onEnded { _ in
                dragStart = nil
                strokes?.wrappedValue.end()
            }
    }
}
