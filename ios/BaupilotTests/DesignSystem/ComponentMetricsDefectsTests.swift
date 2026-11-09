import SwiftUI
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsDefectsTests: XCTestCase {
    /// IosAbnMaengel:40-41: `height: 44px` is border-box, so the 1 pt outline sits inside the 44.
    func testPillButtonIs44TallIncludingTheBorder() {
        let outline = PillButton("Erinnern", style: .outline(border: Tokens.border.abnMaengel), action: nil)
        let filled = PillButton("Behoben?", style: .filled, action: nil)
        XCTAssertEqual(size(of: outline, width: 200).height, 44)
        XCTAssertEqual(size(of: filled, width: 200).height, 44)
    }

    /// IosAbnMaengel:47-54: `min-height: 48px`, the row is a link with an optional action.
    func testCountLinkRowIsAtLeast48Tall() {
        let row = CountLinkRow(
            icon: .checkW26, iconColor: Tokens.ok.core, title: "Behobene Mängel", count: "3", action: nil)
        XCTAssertEqual(size(of: row, width: 322).height, 48)
    }
}
