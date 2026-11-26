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

    // MARK: Large text units

    private static let lineHeights: [LineHeight] = [
        .normal, .factor(1.15), .factor(1.25), .factor(1.35), .factor(1.4), .factor(1.45),
    ]
    private static let sizes: [CGFloat] = [7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 22, 24, 26, 28, 30, 32, 34]

    /// The size iOS renders: the body curve's value, rounded to a whole point (`scaledFont` and `Font.custom` both do).
    private func scaled(_ size: CGFloat, _ category: UIContentSizeCategory) -> CGFloat {
        UIFontMetrics(forTextStyle: .body).scaledValue(
            for: size, compatibleWith: UITraitCollection(preferredContentSizeCategory: category)
        ).rounded()
    }

    func testLineBoxForCategoryIsIdenticalAtLarge() {
        for lineHeight in Self.lineHeights {
            for size in Self.sizes {
                for category in [UIContentSizeCategory.large, .medium, .extraSmall] {
                    let a = lineHeight.box(for: size, category: category)
                    let b = lineHeight.box(for: size)
                    XCTAssertEqual(a.box, b.box, "\(lineHeight) \(size) \(category.rawValue)")
                    XCTAssertEqual(a.baseline, b.baseline, "\(lineHeight) \(size) \(category.rawValue)")
                }
            }
        }
    }

    func testLineBoxScalesAboveLarge() {
        for lineHeight in Self.lineHeights {
            for size in [CGFloat(11), 15, 24, 34] {
                let large = lineHeight.box(for: size, category: .large)
                let xl = lineHeight.box(for: size, category: .extraLarge)
                let expected = lineHeight.box(for: scaled(size, .extraLarge))
                XCTAssertGreaterThan(xl.box, large.box, "\(lineHeight) \(size)")
                XCTAssertEqual(xl.box, expected.box, "\(lineHeight) \(size)")
                XCTAssertEqual(xl.baseline, expected.baseline, "\(lineHeight) \(size)")
            }
        }
    }

    /// The `UIFontMetrics` curve must match what `Font.custom(_:size:)` renders at that Dynamic Type size.
    func testInterTextSingleAtXLMatchesScaledBox() {
        BaupilotFonts.registerIfNeeded()
        for pt: CGFloat in [13, 15, 20] {
            let s = size(
                of: Text("Hamburg").interText(pt, .regular).environment(\.dynamicTypeSize, .xLarge), width: 300)
            // A wrap-capable line is `minHeight: box`, so it measures max(box, text). The plain text at XL is what
            // `Font.custom` renders; it must be the natural height of the rounded scaled size, which is what the
            // box formula is built on. A measured text height snaps to the 1/3 pt pixel grid (scale 3), the
            // tolerance; the unrounded size (16.33 / 21.67 against 16 / 22) is off by more than that.
            let plain = size(
                of: Text("Hamburg").font(.inter(pt, .regular)).environment(\.dynamicTypeSize, .xLarge), width: 300)
            let scaled = InterMetrics.scaledSize(pt, category: .extraLarge)
            XCTAssertEqual(plain.height, InterMetrics.naturalHeight(scaled), accuracy: 1.0 / 3 + 0.001, "\(pt)")
            let box = LineHeight.normal.box(for: pt, category: .extraLarge).box
            XCTAssertEqual(s.height, max(box, plain.height), accuracy: 0.01, "\(pt)")
        }
    }

    func testSingleTruncatingIsSingleAtLarge() {
        BaupilotFonts.registerIfNeeded()
        let probeA = BaselineProbe()
        let probeB = BaselineProbe()
        for width: CGFloat in [40, 300] {
            let single = size(
                of: BaselineLayout(probe: probeA) {
                    Text("Hamburg Hamburg Hamburg").interText(15, .regular, lines: .single)
                }.environment(\.dynamicTypeSize, .large), width: width)
            let truncating = size(
                of: BaselineLayout(probe: probeB) {
                    Text("Hamburg Hamburg Hamburg").interText(15, .regular, lines: .singleTruncating)
                }.environment(\.dynamicTypeSize, .large), width: width)
            XCTAssertEqual(single, truncating, "width \(width)")
            XCTAssertEqual(single.height, 19, "width \(width)")
            XCTAssertEqual(probeA.baseline, probeB.baseline, "width \(width)")
        }
    }

    /// Above `.large` a `scaledFrame` is a minimum height, and a stack that proposes less height than the wrapped text
    /// needs must not compress it into an ellipsis; at `.large` the frame is exactly `height`.
    func testScaledFrameDoesNotCompressWrappedTextAtXL() {
        BaupilotFonts.registerIfNeeded()
        let text = "Hamburg Hamburg Hamburg Hamburg Hamburg Hamburg Hamburg Hamburg Hamburg Hamburg"
        let probe = ProposalProbe()
        for (category, expectsGrowth) in [(DynamicTypeSize.xLarge, true), (.large, false)] {
            _ = size(
                of: ProposalLayout(probe: probe, proposedHeight: 40) {
                    Text(text).interText(15, .regular, lines: .multi).scaledFrame(height: 40, alignment: .top)
                }.environment(\.dynamicTypeSize, category), width: 120)
            if expectsGrowth {
                XCTAssertGreaterThan(probe.height, 40, "\(category)")
            } else {
                XCTAssertEqual(probe.height, 40, "\(category)")
            }
        }
    }

    func testGreedyLabelFontScalesAtXL() {
        BaupilotFonts.registerIfNeeded()
        let large = GreedyLabel.font(weight: .medium, size: 15, category: .large)
        let xl = GreedyLabel.font(weight: .medium, size: 15, category: .extraLarge)
        XCTAssertEqual(large.pointSize, 15)
        XCTAssertGreaterThan(xl.pointSize, large.pointSize)
        XCTAssertEqual(xl.pointSize, scaled(15, .extraLarge))
        XCTAssertEqual(xl.fontName, large.fontName)
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

/// Records the height the child reports when it is proposed `proposedHeight`.
final class ProposalProbe: @unchecked Sendable {
    nonisolated(unsafe) var height: CGFloat = -1
}

private struct ProposalLayout: Layout {
    let probe: ProposalProbe
    let proposedHeight: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let size = subviews[0].sizeThatFits(ProposedViewSize(width: proposal.width, height: proposedHeight))
        probe.height = size.height
        return size
    }

    func placeSubviews(
        in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()
    ) {
        subviews[0].place(at: bounds.origin, proposal: proposal)
    }
}
