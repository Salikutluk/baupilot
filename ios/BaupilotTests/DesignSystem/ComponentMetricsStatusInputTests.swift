import SwiftUI
import UIKit
import XCTest

@testable import Baupilot

@MainActor
final class ComponentMetricsStatusInputTests: XCTestCase {
    private let tolerance: CGFloat = 0.5

    override func setUp() {
        super.setUp()
        BaupilotFonts.registerIfNeeded()
    }

    func testStatusBadgeHeights() {
        let compact = size(of: StatusBadge("Offen", tone: .open), width: 200).height
        let roomy = size(of: StatusBadge("Offen", tone: .open, size: .roomy), width: 200).height
        let mini = size(of: StatusBadge("Günstigstes", tone: .ok, size: .mini, weight: .bold), width: 200).height
        XCTAssertEqual(compact, 22.52, accuracy: tolerance)
        XCTAssertEqual(roomy, 24.52, accuracy: tolerance)
        XCTAssertEqual(mini, 19.31, accuracy: tolerance)
    }

    func testSegmentedControlHeights() {
        func height(_ h: SegmentedControl.SegmentHeight) -> CGFloat {
            size(
                of: SegmentedControl(
                    segments: ["Alle", "Offen"], selection: 0, height: h,
                    accessibilityLabel: "Filter", onSelect: { _ in }),
                width: 300
            ).height
        }
        XCTAssertEqual(height(.compact), 38)
        XCTAssertEqual(height(.regular), 48)
        XCTAssertEqual(height(.medium), 40)
    }

    func testToggleIs51x31() {
        let s = UIHostingController(
            rootView: IosToggle(isOn: true, accessibilityLabel: "Fristen & Termine", action: {}).fixedSize()
        ).sizeThatFits(in: CGSize(width: CGFloat.greatestFiniteMagnitude, height: CGFloat.greatestFiniteMagnitude))
        XCTAssertEqual(s, CGSize(width: 51, height: 31))
    }

    func testSearchFieldIs44Tall() {
        let s = size(
            of: SearchField(text: .constant(""), placeholder: "Suchen", accessibilityLabel: "Suche"),
            width: 335)
        XCTAssertEqual(s.height, 44)
    }

    func testFilterChipIs36Tall() {
        let s = size(
            of: FilterChips(items: ["Alle", "Pläne"], selection: 0, accessibilityLabel: "Filter", onSelect: { _ in }),
            width: 375)
        XCTAssertEqual(s.height, 36)
    }
}
