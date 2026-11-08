import SwiftUI
import UIKit
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsBarTests: XCTestCase {
    func testTabBarIs84Tall() {
        let bar = TabBar(selected: .overview, onSelect: { _ in })
        XCTAssertEqual(size(of: bar, width: 390).height, 84)
    }

    /// 1 (top border) + 12 + 50 (button) + 34.
    func testBottomActionBarIs97TallWithOneButton() {
        let bar = BottomActionBar { PrimaryButton("Weiter", action: nil) }
        XCTAssertEqual(size(of: bar, width: 390).height, 97)
    }

    /// 1 + 12 + 50 + 6 (gap) + caption + 30; the caption is 12 pt text, so the bar grows by its
    /// line height, and the bottom padding is 4 smaller than the standard variant.
    func testBottomActionBarCaptionVariantUsesBottomPadding30() {
        let bar = BottomActionBar(bottomPadding: .withCaption, caption: "Hinweis") {
            PrimaryButton("Weiter", action: nil)
        }
        let height = size(of: bar, width: 390).height
        XCTAssertGreaterThan(height, 1 + 12 + 50 + 6 + 30)
        XCTAssertLessThan(height, 1 + 12 + 50 + 6 + 30 + 20)
    }
}
