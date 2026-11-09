import XCTest
@testable import Baupilot

final class SignatureStrokesTests: XCTestCase {
    private let bounds = CGRect(x: 0, y: 0, width: 100, height: 50)

    func testStartsEmpty() {
        let strokes = SignatureStrokes(bounds: bounds)
        XCTAssertTrue(strokes.isEmpty)
        XCTAssertEqual(strokes.strokes, [])
    }

    func testDragCollectsPoints() {
        var strokes = SignatureStrokes(bounds: bounds)
        strokes.begin(at: CGPoint(x: 10, y: 10))
        strokes.extend(to: CGPoint(x: 20, y: 15))
        strokes.extend(to: CGPoint(x: 30, y: 20))
        strokes.end()
        XCTAssertFalse(strokes.isEmpty)
        XCTAssertEqual(strokes.strokes, [[CGPoint(x: 10, y: 10), CGPoint(x: 20, y: 15), CGPoint(x: 30, y: 20)]])
    }

    func testPointsAreClampedToBounds() {
        var strokes = SignatureStrokes(bounds: bounds)
        strokes.begin(at: CGPoint(x: -5, y: 70))
        strokes.extend(to: CGPoint(x: 140, y: -3))
        XCTAssertEqual(strokes.strokes, [[CGPoint(x: 0, y: 50), CGPoint(x: 100, y: 0)]])
    }

    func testExtendWithoutBeginIsIgnored() {
        var strokes = SignatureStrokes(bounds: bounds)
        strokes.extend(to: CGPoint(x: 10, y: 10))
        XCTAssertTrue(strokes.isEmpty)
    }

    func testExtendAfterEndIsIgnored() {
        var strokes = SignatureStrokes(bounds: bounds)
        strokes.begin(at: CGPoint(x: 1, y: 1))
        strokes.end()
        strokes.extend(to: CGPoint(x: 9, y: 9))
        XCTAssertEqual(strokes.strokes, [[CGPoint(x: 1, y: 1)]])
    }

    func testTapKeepsAOnePointStroke() {
        var strokes = SignatureStrokes(bounds: bounds)
        strokes.begin(at: CGPoint(x: 40, y: 20))
        strokes.end()
        XCTAssertFalse(strokes.isEmpty)
        XCTAssertEqual(strokes.strokes, [[CGPoint(x: 40, y: 20)]])
    }

    func testSecondStrokeIsSeparate() {
        var strokes = SignatureStrokes(bounds: bounds)
        strokes.begin(at: CGPoint(x: 1, y: 1))
        strokes.extend(to: CGPoint(x: 2, y: 2))
        strokes.end()
        strokes.begin(at: CGPoint(x: 5, y: 5))
        strokes.end()
        XCTAssertEqual(strokes.strokes.count, 2)
        XCTAssertEqual(strokes.strokes[1], [CGPoint(x: 5, y: 5)])
    }
}
