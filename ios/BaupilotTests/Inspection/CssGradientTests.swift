import XCTest
@testable import Baupilot

final class CssGradientTests: XCTestCase {
    private let tolerance: CGFloat = 0.0005

    func testIOSTile160Degrees() {
        let line = CssGradient.endpoints(angleDegrees: 160, width: 99.3333, height: 84)
        XCTAssertEqual(line.start.x, 30.3582, accuracy: tolerance)
        XCTAssertEqual(line.start.y, -11.0495, accuracy: tolerance)
        XCTAssertEqual(line.end.x, 68.9751, accuracy: tolerance)
        XCTAssertEqual(line.end.y, 95.0495, accuracy: tolerance)
    }

    func testAndroidTile160Degrees() {
        let line = CssGradient.endpoints(angleDegrees: 160, width: 100.6667, height: 80)
        XCTAssertEqual(line.start.x, 31.5897, accuracy: tolerance)
        XCTAssertEqual(line.start.y, -11.4977, accuracy: tolerance)
        XCTAssertEqual(line.end.x, 69.0770, accuracy: tolerance)
        XCTAssertEqual(line.end.y, 91.4977, accuracy: tolerance)
    }

    func testOneEightyDegreesRunsTopToBottom() {
        let line = CssGradient.endpoints(angleDegrees: 180, width: 100, height: 80)
        XCTAssertEqual(line.start.x, 50, accuracy: tolerance)
        XCTAssertEqual(line.start.y, 0, accuracy: tolerance)
        XCTAssertEqual(line.end.x, 50, accuracy: tolerance)
        XCTAssertEqual(line.end.y, 80, accuracy: tolerance)
    }
}
