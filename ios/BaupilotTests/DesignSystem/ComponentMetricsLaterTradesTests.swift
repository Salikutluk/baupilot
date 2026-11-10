import SwiftUI
import UIKit
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsLaterTradesTests: XCTestCase {
    private static let longSubtitle = "4 Abschnitte · 2 geprüft, 1 Mangel"

    /// D13: a `.greedy` subtitle wraps onto a second 13 pt line box and grows the padded row.
    func testListRowGreedySubtitleWrapsOntoSecondLineBox() {
        let row = ListRow(
            title: "Gefälle", subtitle: Self.longSubtitle, leading: .iconTile(.pipe, size: .s34r9), trailing: .badge(StatusBadge("1 Mangel", tone: .error())),
            showsDivider: false, subtitleLines: .greedy, action: nil)
        let expected = 24 + InterMetrics.lineBox(15) + 3 + 2 * InterMetrics.lineBox(13)
        XCTAssertEqual(size(of: row, width: 350).height, expected, accuracy: 0.01)
    }

    /// A short greedy subtitle stays one line box tall, like `.single`.
    func testListRowGreedySubtitleKeepsOneLineWhenItFits() {
        let greedy = ListRow(
            title: "Dämmung", subtitle: "Sichtprüfung · 2 Fotos", showsDivider: false, subtitleLines: .greedy,
            action: nil)
        let single = ListRow(title: "Dämmung", subtitle: "Sichtprüfung · 2 Fotos", showsDivider: false, action: nil)
        XCTAssertEqual(size(of: greedy, width: 350).height, size(of: single, width: 350).height, accuracy: 0.01)
    }

    // MARK: Gefälle (SanGefaelle)

    private func intrinsic(_ view: some View) -> CGSize {
        let hosting = UIHostingController(rootView: view.fixedSize())
        return hosting.sizeThatFits(
            in: CGSize(width: CGFloat.greatestFiniteMagnitude, height: .greatestFiniteMagnitude))
    }

    private func slopeTile(height: CGFloat? = nil) -> SocketTile {
        let style = SocketTile.Style(background: Tokens.oSoft, foreground: Tokens.oFg)
        let id = "A1", label = "1,4 %", accessibility = "Abschnitt A1: 1,4 %", identifier = "slope.section.A1"
        guard let height else {
            return SocketTile(id: id, label: label, style: style, accessibilityLabel: accessibility, identifier: identifier)
        }
        return SocketTile(
            id: id, label: label, style: style, accessibilityLabel: accessibility, identifier: identifier,
            height: height)
    }

    /// IosSanGefaelle:34: `height: 52px`; the default stays 56 for the earlier screens.
    func testSocketTileHeightIsConfigurable() {
        XCTAssertEqual(size(of: slopeTile(), width: 81.5).height, 56)
        XCTAssertEqual(size(of: slopeTile(height: 52), width: 81.5).height, 52)
    }

    /// 4 columns of 52 with gap 8; the filler of a short last row has the same height.
    func testSocketGridHonoursTileHeightInFullAndPartialRows() {
        func grid(_ count: Int) -> SocketGrid {
            SocketGrid(tiles: (1...count).map { _ in slopeTile(height: 52) }, height: 52)
        }
        XCTAssertEqual(size(of: grid(4), width: 326).height, 52)
        XCTAssertEqual(size(of: grid(5), width: 326).height, 52 + 8 + 52)
    }

    /// IosSanGefaelle:46-51: two columns of 152 (gap 10) hold a 48 tall stepper that fills its column; the buttons
    /// stay 40 and the value does not push the width.
    func testValueStepperFillsWidthWithoutMinimum() {
        let stepper = ValueStepper(
            label: "Länge", value: "2,0 m", fillsWidth: true, onDecrement: nil, onIncrement: nil)
        let measured = size(of: stepper, width: 152)
        XCTAssertEqual(measured.width, 152)
        XCTAssertEqual(measured.height, 48)
        let narrow = size(of: stepper, width: 120)
        XCTAssertEqual(narrow.width, 120)
    }

    func testValueStepperValueSizeDefaultsTo16() {
        let normal = intrinsic(
            ValueStepper(label: "Länge", value: "2,0 m", valueMinWidth: 0, onDecrement: nil, onIncrement: nil))
        let big = intrinsic(
            ValueStepper(
                label: "Länge", value: "2,0 m", valueMinWidth: 0, valueSize: 17, onDecrement: nil, onIncrement: nil))
        XCTAssertEqual(normal.height, 48)
        XCTAssertGreaterThan(big.width, normal.width)
    }

    private func result(background: ColorToken = Tokens.oSoft, foreground: ColorToken = Tokens.oFg) -> SlopeResult {
        SlopeResult(
            percent: "1,25 %", verdict: "Ausreichend · Soll ≥ 1,0 %", schemaY2: 15.1, levelLabel: "waagerecht",
            deltaLabel: "Δh", background: background, foreground: foreground)
    }

    /// IosSanGefaelle:68-75: padding 14 around the tallest child, the text column (26 line box + gap 2 + 13 line
    /// box = 49) rather than the 44 tall schema; the reference card measures 77.
    func testSlopeResultIs77TallAndFillsTheWidth() {
        let measured = size(of: result(), width: 282)
        XCTAssertEqual(measured.height, 28 + InterMetrics.lineBox(26) + 2 + InterMetrics.lineBox(13))
        XCTAssertEqual(measured.height, 77)
        XCTAssertEqual(measured.width, 282)
    }

    /// The verdict is one line (`white-space: nowrap`): a narrow column does not make the card taller.
    func testSlopeResultKeepsTheVerdictOnOneLine() {
        XCTAssertEqual(size(of: result(), width: 200).height, 77)
    }

    /// The schema is a 92 x 44 canvas.
    func testSlopeSchemaIs92By44() {
        XCTAssertEqual(intrinsic(SlopeResult.Schema(y2: 15.1, levelLabel: "waagerecht", deltaLabel: "Δh", color: Tokens.oFg)),
                       CGSize(width: 92, height: 44))
    }

    // MARK: Druckprobe (SanDruck)

    private func ring(fraction: Double = 0.58) -> TimerRing {
        TimerRing(
            text: "12:40", fraction: fraction, accessibilityLabel: "Prüfzeit zu 58 Prozent abgelaufen",
            fill: Tokens.accent, track: Tokens.track.sanDruck, hole: Tokens.surface, holeDiameter: 56,
            textWeight: .bold)
    }

    /// IosSanDruck:48: a 72 x 72 disc, `flex-shrink: 0`.
    func testTimerRingIs72By72() {
        XCTAssertEqual(intrinsic(ring()), CGSize(width: 72, height: 72))
    }

    /// `conic-gradient(accent 0 58%, track 58% 100%)`: the fill starts at 12 o'clock and runs clockwise, so the
    /// right half of the band (3 o'clock) is fill and the left half (9 o'clock) is track at 58 %.
    func testTimerRingFillRunsClockwiseFromTwelve() {
        let pixels = render(ring(), size: CGSize(width: 72, height: 72), scale: 1)
        let twelve = pixels.pixel(37, 3)
        let three = pixels.pixel(68, 36)
        let nine = pixels.pixel(3, 36)
        XCTAssertEqual(twelve, three)
        XCTAssertNotEqual(twelve, nine)
    }

    /// At half a turn the fill ends at 6 o'clock: just right of it is fill, just left of it is track.
    func testTimerRingHardStopAtFraction() {
        let pixels = render(ring(fraction: 0.5), size: CGSize(width: 72, height: 72), scale: 1)
        XCTAssertEqual(pixels.pixel(40, 68), pixels.pixel(37, 3))
        XCTAssertNotEqual(pixels.pixel(32, 68), pixels.pixel(37, 3))
    }

    /// IosSanDruck:56-60: the 12 pt label over a 6 pt gap over the 48 pt box.
    func testReadingFieldBoxIs48UnderItsLabel() {
        let value = ReadingField(label: "Start (10:02)", content: .value("10,0 bar"), identifier: nil)
        let measured = size(of: value, width: 152)
        XCTAssertEqual(measured.width, 152)
        XCTAssertEqual(measured.height, InterMetrics.lineBox(12) + 6 + 48, accuracy: 0.01)
    }

    func testReadingFieldInputHasTheSameHeightAsValue() {
        let value = ReadingField(label: "Ende", content: .value("10,0 bar"), identifier: nil)
        let input = ReadingField(label: "Ende", content: .input(text: .constant(""), placeholder: "– bar"), identifier: "pressure.end")
        XCTAssertEqual(size(of: input, width: 152).height, size(of: value, width: 152).height, accuracy: 0.01)
    }
}

