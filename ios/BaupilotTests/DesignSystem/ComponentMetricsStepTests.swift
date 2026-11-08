import SwiftUI
import UIKit
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsStepTests: XCTestCase {
    /// Natural size with no constraint, so a fixed-size glyph reports its own size.
    private func naturalSize(of view: some View) -> CGSize {
        UIHostingController(rootView: view.fixedSize())
            .sizeThatFits(in: CGSize(width: CGFloat.greatestFiniteMagnitude,
                                     height: CGFloat.greatestFiniteMagnitude))
    }

    func testStepRingIs22AndCurrentNumberIs26() {
        BaupilotFonts.registerIfNeeded()
        XCTAssertEqual(naturalSize(of: StepRow.DoneRing()), CGSize(width: 22, height: 22))
        XCTAssertEqual(naturalSize(of: StepRow.OpenRing(number: 4)), CGSize(width: 22, height: 22))
        XCTAssertEqual(naturalSize(of: CurrentStepCard<EmptyView>.NumberCircle(number: 3)), CGSize(width: 26, height: 26))
    }

    func testRowsKeepTheirMinHeight() {
        BaupilotFonts.registerIfNeeded()
        for minHeight: CGFloat in [44, 40, 48] {
            XCTAssertEqual(
                size(of: StepRow(number: 1, title: "Rahmen", state: .done, minHeight: minHeight), width: 350).height,
                minHeight)
            XCTAssertEqual(
                size(of: StepRow(number: 4, title: "Dichtung", state: .open(tag: "Pflicht"), minHeight: minHeight),
                     width: 350).height,
                minHeight)
        }
    }

    /// A card border lies outside the padding (IosAbnPruefung:43 border 2, IosAbnMaengel:73 border 1.5 -> 1).
    func testCardBorderAddsToThePaddedBox() {
        let content = Color.clear.frame(height: 100)
        XCTAssertEqual(size(of: Card { content }, width: 350).height, 128)
        XCTAssertEqual(size(of: Card(border: .current) { content }, width: 350).height, 132)
        XCTAssertEqual(size(of: Card(border: .overdue()) { content }, width: 350).height, 130)
    }
}
