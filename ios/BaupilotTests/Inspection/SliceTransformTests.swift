import XCTest
@testable import Baupilot

final class SliceTransformTests: XCTestCase {
    private let tolerance: CGFloat = 0.0005
    private let viewBox = CGSize(width: 100, height: 80)

    func testIOSTileWidthScalesByHeight() {
        let fit = SliceTransform.fit(viewBox: viewBox, into: CGSize(width: 99.333, height: 84))
        XCTAssertEqual(fit.scale, 1.05, accuracy: tolerance)
        XCTAssertEqual(fit.offsetX, -2.8333, accuracy: tolerance)
        XCTAssertEqual(fit.offsetY, 0, accuracy: tolerance)
    }

    func testAndroidTileWidthScalesByWidth() {
        let fit = SliceTransform.fit(viewBox: viewBox, into: CGSize(width: 100.667, height: 80))
        XCTAssertEqual(fit.scale, 1.006667, accuracy: tolerance)
        XCTAssertEqual(fit.offsetX, 0, accuracy: tolerance)
        XCTAssertEqual(fit.offsetY, -0.2667, accuracy: tolerance)
    }
}
