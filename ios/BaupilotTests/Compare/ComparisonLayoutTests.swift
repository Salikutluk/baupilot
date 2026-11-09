import XCTest
@testable import Baupilot

final class ComparisonLayoutTests: XCTestCase {
    func testColumnsIOS() {
        let widths = ComparisonLayout.columns(cardWidth: 350, padding: 14, gap: 10, labelColumn: 104)
        XCTAssertEqual(widths, ComparisonLayout.Widths(label: 104, value: 99))
    }

    func testColumnsAreNonNegative() {
        let widths = ComparisonLayout.columns(cardWidth: 100, padding: 14, gap: 10, labelColumn: 104)
        XCTAssertEqual(widths.value, 0)
        XCTAssertEqual(widths.label, 104)
    }
}
