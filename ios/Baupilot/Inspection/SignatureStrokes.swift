import CoreGraphics

/// The strokes of the owner's signature surface (AbnSignatur). Pure state, no UI: the view feeds gesture points in.
public struct SignatureStrokes: Equatable, Sendable {
    public let bounds: CGRect
    public private(set) var strokes: [[CGPoint]] = []
    private var isDrawing = false

    public init(bounds: CGRect) {
        self.bounds = bounds
    }

    public var isEmpty: Bool { strokes.isEmpty }

    /// Starts a stroke with one point; if the finger lifts without moving it stays a dot.
    public mutating func begin(at point: CGPoint) {
        strokes.append([clamped(point)])
        isDrawing = true
    }

    public mutating func extend(to point: CGPoint) {
        guard isDrawing, !strokes.isEmpty else { return }
        strokes[strokes.count - 1].append(clamped(point))
    }

    public mutating func end() {
        isDrawing = false
    }

    private func clamped(_ point: CGPoint) -> CGPoint {
        CGPoint(
            x: min(max(point.x, bounds.minX), bounds.maxX),
            y: min(max(point.y, bounds.minY), bounds.maxY)
        )
    }
}
