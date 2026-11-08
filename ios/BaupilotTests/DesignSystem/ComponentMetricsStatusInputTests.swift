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
        // Line box (D1) + vertical padding: 12 pt → 15 + 2×4, 12 pt → 15 + 2×5, 11 pt → 14 + 2×3.
        XCTAssertEqual(compact, InterMetrics.lineBox(12) + 8, accuracy: tolerance)
        XCTAssertEqual(roomy, InterMetrics.lineBox(12) + 10, accuracy: tolerance)
        XCTAssertEqual(mini, InterMetrics.lineBox(11) + 6, accuracy: tolerance)
        XCTAssertEqual([compact, roomy, mini], [23, 25, 20])
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

    /// Label line box 16 + gap 6 + field 48; the trailing link (44 hit area, margin -14) adds nothing.
    func testFormFieldIs48Tall() {
        func height(_ link: FormField.Link?) -> CGFloat {
            size(
                of: FormField(
                    label: "Passwort", text: .constant(""), placeholder: "••••••••", kind: .password,
                    trailingLink: link),
                width: 342
            ).height
        }
        let label = InterMetrics.lineBox(13)
        XCTAssertEqual(height(nil), label + 6 + 48)
        XCTAssertEqual(height(FormField.Link(title: "Vergessen?", action: nil)), label + 6 + 48)
        XCTAssertEqual(height(nil), 70)
    }

    func testIconTilesAreSquare() {
        func tile(_ size: IconTile.Size) -> CGSize {
            UIHostingController(
                rootView: IconTile(
                    icon: .house, size: size, background: Tokens.accent, foreground: Tokens.onAccent
                ).fixedSize()
            ).sizeThatFits(in: CGSize(width: CGFloat.greatestFiniteMagnitude, height: CGFloat.greatestFiniteMagnitude))
        }
        XCTAssertEqual(tile(.brand40), CGSize(width: 40, height: 40))
        XCTAssertEqual(tile(.feature32), CGSize(width: 32, height: 32))
        XCTAssertEqual(size(of: FeatureRow(icon: .calendar, text: "Zeitplan"), width: 342).height, 32)
    }
}
