import SwiftUI
import UIKit
import XCTest
@testable import Baupilot

@MainActor
final class TypographyTests: XCTestCase {
    func testInterWeightsResolveToRegisteredFonts() {
        BaupilotFonts.registerIfNeeded()
        let weights: [InterWeight] = [.regular, .medium, .semibold, .bold]
        for weight in weights {
            XCTAssertNotNil(
                UIFont(name: weight.postScriptName, size: 16), "Missing font for \(weight)")
        }
        XCTAssertEqual(Set(weights.map(\.postScriptName)), Set(BaupilotFonts.postScriptNames))
    }

    func testLineBoxMatchesChromeRounding() {
        let sizes: [CGFloat] = [10, 11, 12, 13, 14, 15, 16, 17, 20, 24, 32]
        let expected: [CGFloat] = [12, 14, 15, 16, 17, 19, 20, 20, 24, 29, 39]
        XCTAssertEqual(sizes.map(InterMetrics.lineBox), expected)
    }

    func testBaselineOffsetIsRoundedAscent() {
        XCTAssertEqual(InterMetrics.baselineOffset(15), 15)
        XCTAssertEqual(InterMetrics.baselineOffset(32), 31)
    }

    /// Where the descent rounds down, Blink moves one unit from ascent to descent: the box stays, the baseline rises.
    func testBaselineRisesWhereDescentRoundsDown() {
        let sizes: [CGFloat] = [10, 13, 14, 17, 18, 22]
        XCTAssertEqual(sizes.map(InterMetrics.baselineOffset), [9, 12, 13, 15, 16, 20])
        XCTAssertEqual(sizes.map(InterMetrics.lineBox), [12, 16, 17, 20, 21, 26])
    }

    func testInterTextSingleLineBoxAndBaseline() {
        BaupilotFonts.registerIfNeeded()
        let probe = BaselineProbe()
        let view = BaselineLayout(probe: probe) {
            Text("Hamburg").interText(15, .regular)
        }
        let s = size(of: view, width: 200)
        XCTAssertEqual(s.height, 19)
        XCTAssertEqual(probe.baseline, 15)
    }

    func testInterTextFactorIsUnrounded() {
        BaupilotFonts.registerIfNeeded()
        let s = size(
            of: Text("Hamburg").interText(15, .regular, lineHeight: .factor(1.5)), width: 200)
        // 22.5 pt is not a whole @3x pixel; SwiftUI rounds the hosted size up to the next 1/3 pt.
        XCTAssertEqual(s.height, 22.5, accuracy: 1.0 / 3)
    }

    /// A CSS block of n lines is exactly n line boxes tall; SwiftUI rounds a multi-line `Text` up to the pixel grid,
    /// and a 14/1.5 box (21 pt) is not a multiple of that grid for the text alone.
    func testInterTextBlockIsExactlyNLineBoxesTall() {
        BaupilotFonts.registerIfNeeded()
        let text = "Hamburg Hamburg Hamburg Hamburg Hamburg Hamburg"
        for width: CGFloat in [120, 200] {
            let s = size(
                of: Text(text).interText(14, .regular, lineHeight: .factor(1.5), lines: .block), width: width)
            XCTAssertGreaterThanOrEqual(s.height, 42, "the text wraps at \(width)")
            XCTAssertEqual(s.height.truncatingRemainder(dividingBy: 21), 0, accuracy: 0.001, "height \(s.height)")
        }
        let one = size(of: Text("Hamburg").interText(14, .regular, lineHeight: .factor(1.5), lines: .block), width: 200)
        XCTAssertEqual(one.height, 21, accuracy: 0.001)
    }
}

/// Records the first-text-baseline of its single child, measured from the child's top.
final class BaselineProbe: @unchecked Sendable {
    nonisolated(unsafe) var baseline: CGFloat = -1
}

private struct BaselineLayout: Layout {
    let probe: BaselineProbe

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        probe.baseline = subviews[0].dimensions(in: proposal)[.firstTextBaseline]
        return subviews[0].sizeThatFits(proposal)
    }

    func placeSubviews(
        in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()
    ) {
        probe.baseline = subviews[0].dimensions(in: .unspecified)[.firstTextBaseline]
        subviews[0].place(at: bounds.origin, proposal: proposal)
    }
}
