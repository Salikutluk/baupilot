import XCTest
@testable import Baupilot

final class InspectionGeometryTests: XCTestCase {
    func testSignatureSampleSegmentsEqualThePrototypePath() {
        func p(_ x: CGFloat, _ y: CGFloat) -> CGPoint { CGPoint(x: x, y: y) }
        let expected: [[CGPoint]] = [
            [p(14, 14), p(20, 10), p(22, 18)],
            [p(24, 26), p(16, 36), p(22, 32)],
            [p(28, 28), p(34, 10), p(38, 12)],
            [p(42, 14), p(36, 30), p(42, 28)],
            [p(48, 26), p(50, 16), p(54, 18)],
            [p(58, 20), p(56, 28), p(62, 26)],
            [p(68, 24), p(72, 18), p(76, 20)],
            [p(80, 22), p(82, 26), p(90, 22)],
            [p(98, 18), p(102, 16), p(110, 14)],
        ]
        XCTAssertEqual(SignatureSample.start, p(6, 34))
        XCTAssertEqual(SignatureSample.size, CGSize(width: 120, height: 48))
        XCTAssertEqual(SignatureSample.lineWidth, 2)
        XCTAssertEqual(SignatureSample.segments.map { [$0.control1, $0.control2, $0.end] }, expected)
    }

    func testWindowLevelSketchEqualsTheSVG() {
        typealias G = WindowLevelSketch.Geometry
        XCTAssertEqual(G.size, CGSize(width: 56, height: 44))
        XCTAssertEqual(G.frame, .init(rect: CGRect(x: 8, y: 12, width: 32, height: 30), cornerRadius: 1.5))
        XCTAssertEqual(G.frameLineWidth, 2)
        XCTAssertEqual(G.mullion, .init(from: CGPoint(x: 24, y: 12), to: CGPoint(x: 24, y: 42)))
        XCTAssertEqual(G.mullionLineWidth, 1.5)
        XCTAssertEqual(G.topLevel, .init(rect: CGRect(x: 6, y: 3, width: 36, height: 6), cornerRadius: 2))
        XCTAssertEqual(G.topBubble, .init(center: CGPoint(x: 24, y: 6), radius: 2))
        XCTAssertEqual(G.sideLevel, .init(rect: CGRect(x: 45, y: 12, width: 6, height: 30), cornerRadius: 2))
        XCTAssertEqual(G.sideBubble, .init(center: CGPoint(x: 48, y: 27), radius: 2))
    }

    func testPhotoSceneEqualsThePrototype() {
        typealias G = PhotoSceneGeometry
        XCTAssertEqual(G.viewBox, CGSize(width: 100, height: 80))
        XCTAssertEqual(G.gradientAngleDegrees, 160)
        XCTAssertEqual(G.gradientStops, [0, 0.6, 1])
        XCTAssertEqual(G.frame, CGRect(x: 22, y: 10, width: 56, height: 62))
        XCTAssertEqual(G.frameLineWidth, 5)
        XCTAssertEqual(G.mullion, WindowLevelSketch.Geometry.Line(from: CGPoint(x: 50, y: 10), to: CGPoint(x: 50, y: 72)))
        XCTAssertEqual(G.mullionLineWidth, 3)
        XCTAssertEqual(G.level, WindowLevelSketch.Geometry.RoundedRect(rect: CGRect(x: 18, y: 4, width: 64, height: 7), cornerRadius: 2))
    }
}
