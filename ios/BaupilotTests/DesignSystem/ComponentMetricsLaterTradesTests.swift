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

    // MARK: Bad OG (SanBad)

    private func chip(_ text: String, _ state: SelectionChip.State, borderBox: Bool = true) -> SelectionChip {
        SelectionChip(text, state: state, openBorder: Tokens.outline, showsTick: true, borderBox: borderBox)
    }

    /// IosSanBad:35-44: the open chip is a border-box button (36 tall, 1 pt border inside, padding 12), so its outer
    /// width is the text plus 26; a done or active chip has no border and is the text plus 24. Without `borderBox`
    /// the open chip keeps the earlier gallery width (text + 24).
    func testSelectionChipBorderBoxAddsTheBorderToThePadding() {
        let plain = intrinsic(chip("Dusche", .open, borderBox: false))
        let boxed = intrinsic(chip("Dusche", .open))
        XCTAssertEqual(boxed.width, plain.width + 2, accuracy: 0.01)
        XCTAssertEqual(boxed.height, 36)
        let active = intrinsic(chip("Dusche", .active))
        XCTAssertEqual(boxed.width, active.width + 2, accuracy: 0.01)
    }

    private func flow(_ names: [String]) -> some View {
        FlowLayout(spacing: 8, lineSpacing: 8) {
            ForEach(names, id: \.self) { name in
                SelectionChip(name, state: .open, openBorder: Tokens.outline, showsTick: true, borderBox: true)
            }
        }
    }

    /// The reference wraps SanBad's five chips 3 + 2 at 314 pt: [WC, Waschtisch, Dusche] / [Heizkörper, Lüfter].
    func testFlowLayoutWrapsSanBadChipsThreeThenTwo() {
        let names = ["WC", "Waschtisch", "Dusche", "Heizkörper", "Lüfter"]
        XCTAssertEqual(size(of: flow(names), width: 350).height, 36 + 8 + 36)
        let widths = names.map { intrinsic(chip($0, .open)).width }
        XCTAssertLessThanOrEqual(widths[0...2].reduce(0, +) + 16, 350)
        XCTAssertGreaterThan(widths[0...3].reduce(0, +) + 24, 350)
        XCTAssertEqual(size(of: flow(names), width: 800).height, 36)
    }

    /// Every chip on its own row when the container is as narrow as the widest chip.
    func testFlowLayoutPutsEachItemOnItsOwnLineWhenNarrow() {
        let names = ["WC", "Waschtisch", "Dusche"]
        let widest = names.map { intrinsic(chip($0, .open)).width }.max() ?? 0
        XCTAssertEqual(size(of: flow(names), width: widest).height, 3 * 36 + 2 * 8)
    }

    private func row(
        ok: Bool, showsDivider: Bool, title: String = "Warm links, kalt rechts",
        hint: String = "Hebel ganz nach links: Wasser wird warm."
    ) -> ObjectCheckRow {
        ObjectCheckRow(
            title: title, hint: hint, ok: ok, showsDivider: showsDivider, okFill: Tokens.ok.sanBad,
            okInk: Tokens.okInk.sanBad, outline: Tokens.outline, divider: Tokens.border.core,
            okAccessibilityLabel: "Warm links, kalt rechts: in Ordnung",
            defectAccessibilityLabel: "Warm links, kalt rechts: Mangel", identifiers: nil)
    }

    /// IosSanBad:48-60: `min-height: 56px` of content, the 44 pt buttons centred; row 2 on adds the 1 pt top border.
    func testObjectCheckRowIs56TallAndAddsTheDivider() {
        XCTAssertEqual(size(of: row(ok: true, showsDivider: false, hint: "Kurz."), width: 314).height, 56)
        XCTAssertEqual(size(of: row(ok: false, showsDivider: true, hint: "Kurz."), width: 314).height, 57)
    }

    /// A hint that wraps in the 208 pt text column: title line box + gap 2 + two hint boxes of 13 x 1.35 (the host
    /// rounds up to the 1/3 pt pixel grid), which just exceeds the 56 minimum like the reference's row 1.
    func testObjectCheckRowGrowsWithAWrappedHint() {
        let measured = size(of: row(ok: true, showsDivider: false), width: 314).height
        XCTAssertEqual(measured, InterMetrics.lineBox(15) + 2 + 2 * 13 * 1.35, accuracy: 0.34)
        XCTAssertGreaterThan(measured, 56)
    }

    // MARK: Ebenheit (EsEbenheit)

    private func sketch(lineHeight: CGFloat = 8) -> FlatnessSketch {
        FlatnessSketch(
            lineHeight: lineHeight, line: Tokens.oFg, lath: Tokens.accent, floor: Tokens.track.esEbenheit,
            background: Tokens.surface)
    }

    /// IosEsEbenheit:32: a 64 tall, full-width box.
    func testFlatnessSketchIs64TallAndFillsTheWidth() {
        let measured = size(of: sketch(), width: 350)
        XCTAssertEqual(measured.width, 350)
        XCTAssertEqual(measured.height, 64)
    }

    private func solid(_ token: ColorToken) -> [UInt8] {
        render(Rectangle().fill(token.color), size: CGSize(width: 2, height: 2), scale: 1).pixel(0, 0)
    }

    /// The lath (inset 24, y 18-26) is `lath`, the floor (inset 16, y 40-50) has a flat top and a rounded bottom
    /// (the corner stays `background`, the middle is `floor`), the line (x 175, y 26 on) is `line`.
    func testFlatnessSketchDrawsLathFloorAndLine() {
        let pixels = render(sketch(lineHeight: 10), size: CGSize(width: 350, height: 64), scale: 1)
        XCTAssertEqual(pixels.pixel(100, 22), solid(Tokens.accent))
        XCTAssertEqual(pixels.pixel(100, 38), solid(Tokens.surface))
        XCTAssertEqual(pixels.pixel(100, 42), solid(Tokens.track.esEbenheit))
        XCTAssertEqual(pixels.pixel(175, 45), solid(Tokens.track.esEbenheit))
        XCTAssertEqual(pixels.pixel(17, 49), solid(Tokens.surface))
        XCTAssertEqual(pixels.pixel(175, 30), solid(Tokens.oFg))
        XCTAssertEqual(pixels.pixel(177, 30), solid(Tokens.surface))
        XCTAssertEqual(pixels.pixel(175, 40), solid(Tokens.track.esEbenheit))
    }

    /// IosEsEbenheit:58: 36 tall, padding 0 12, hugging the 14/600 text.
    func testMeasureChipIs36TallAndHugsItsText() {
        let chip = MeasureChip("M3 · 6 mm", background: Tokens.eSoft.core, foreground: Tokens.eFg)
        let text = intrinsic(Text("M3 · 6 mm").interText(14, .semibold))
        let measured = intrinsic(chip)
        XCTAssertEqual(measured.height, 36)
        XCTAssertEqual(measured.width, text.width + 24, accuracy: 0.01)
    }

    /// IosEsEbenheit:62: a link of at least 44 that wraps greedily instead of staying on one line.
    func testTextLinkIsAtLeast44AndWrapsGreedily() {
        let short = TextLink("Kurz", color: Tokens.err.core, action: nil)
        XCTAssertEqual(size(of: short, width: 350).height, 44)
        let long = TextLink("1 Messpunkt über Grenzwert → Mangel erfassen", color: Tokens.err.core, action: nil)
        XCTAssertEqual(size(of: long, width: 250).height, 44)
        XCTAssertEqual(size(of: long, width: 110).height, 5 * InterMetrics.lineBox(15), accuracy: 0.01)
    }

    // MARK: Belegreife (EsBelegreife)

    private func bars(_ heights: [CGFloat]) -> [(height: CGFloat, color: ColorToken)] {
        heights.map { (height: $0, color: Tokens.ok.core) }
    }

    /// IosEsBelegreife:33: an 80 tall bar box under 4 of padding is 84 tall and fills the width.
    func testHeatingBarChartIs84TallAndFillsTheWidth() {
        let measured = size(of: HeatingBarChart(bars: bars([72, 40]), accessibilityLabel: "Chart"), width: 314)
        XCTAssertEqual(measured.width, 314)
        XCTAssertEqual(measured.height, 84)
    }

    /// 21 bars of (314 - 3 x 20) / 21 = 12.095, bottom-aligned, gap 3, top corners rounded: bar 0 is 72 tall, so its
    /// top-left pixel (row 12) is clear while the pixel below it is filled; the gap and the space above stay clear.
    func testHeatingBarChartSharesTheWidthAndRoundsTheTopCorners() {
        let heights = [CGFloat](repeating: 72, count: 1) + [CGFloat](repeating: 40, count: 20)
        let pixels = render(
            HeatingBarChart(bars: bars(heights), accessibilityLabel: "Chart"), size: CGSize(width: 314, height: 84),
            scale: 1)
        let clear: [UInt8] = [0, 0, 0, 0]
        XCTAssertLessThan(pixels.pixel(0, 12)[3], 32)
        XCTAssertEqual(pixels.pixel(6, 13), solid(Tokens.ok.core))
        XCTAssertEqual(pixels.pixel(6, 83), solid(Tokens.ok.core))
        XCTAssertEqual(pixels.pixel(6, 5), clear)
        XCTAssertEqual(pixels.pixel(13, 60), clear)
        XCTAssertEqual(pixels.pixel(14, 60), clear)
        XCTAssertEqual(pixels.pixel(20, 50), solid(Tokens.ok.core))
        XCTAssertEqual(pixels.pixel(20, 40), clear)
        XCTAssertEqual(pixels.pixel(307, 50), solid(Tokens.ok.core))
        XCTAssertEqual(pixels.pixel(313, 50), solid(Tokens.ok.core))
    }

    private func checkRow(done: Bool, title: String = "Messung durchgeführt") -> CheckMetaRow {
        CheckMetaRow(
            title: title, meta: "geplant 19.03.", done: done, ok: Tokens.ok.core, okInk: Tokens.okInk.core,
            openRing: Tokens.track.esBelegreife)
    }

    /// IosEsBelegreife:55: `min-height: 40px`; a title that wraps grows the row.
    func testCheckMetaRowIsAtLeast40AndGrowsWithAWrappedTitle() {
        XCTAssertEqual(size(of: checkRow(done: false), width: 314).height, 40)
        let long = checkRow(done: false, title: "Ergebnis unter Grenzwert und noch viel mehr Text, der umbricht")
        XCTAssertGreaterThan(size(of: long, width: 200).height, 40)
    }

    /// The 22 ring (2 pt border) is `ok` all through when done and only an `openRing` outline when open.
    func testCheckMetaRowRingIsFilledWhenDoneAndHollowWhenOpen() {
        let done = render(checkRow(done: true), size: CGSize(width: 314, height: 40), scale: 1)
        XCTAssertEqual(done.pixel(1, 20), solid(Tokens.ok.core))
        XCTAssertEqual(done.pixel(4, 20), solid(Tokens.ok.core))
        let open = render(checkRow(done: false), size: CGSize(width: 314, height: 40), scale: 1)
        XCTAssertEqual(open.pixel(1, 20), solid(Tokens.track.esBelegreife))
        XCTAssertEqual(open.pixel(11, 20), [0, 0, 0, 0])
    }

    /// IosEsBelegreife:51: 44 tall, padding 0 16, hugging the 15/600 label; the default button stays full width.
    func testCompactPrimaryButtonIs44TallAndHugsItsLabel() {
        let button = PrimaryButton("Tag 6 eintragen", size: .compact, action: nil)
        let text = intrinsic(Text("Tag 6 eintragen").interText(15, .semibold))
        let measured = intrinsic(button)
        XCTAssertEqual(measured.height, 44)
        XCTAssertEqual(measured.width, text.width + 32, accuracy: 0.01)
        XCTAssertEqual(size(of: PrimaryButton("Tag 6 eintragen", size: .medium, action: nil), width: 200).height, 50)
    }

    private func hasInk(_ pixels: Pixels, x: Range<Int>, y: Range<Int>, background: [UInt8]) -> Bool {
        y.contains { row in x.contains { column in pixels.pixel(column, row) != background } }
    }

    /// IosEsBelegreife:67: the lock note with leading text, `line-height: normal` and a centred row is 24 + 18 tall
    /// and starts its text right after the icon (x 42); the default locked tone centres the text in the width.
    func testInfoCalloutLockNoteCanLeadWithNormalLineHeight() {
        let leading = InfoCallout(
            text: "Kurz", icon: .lock, tone: .locked, textSize: 14, textAlignment: .leading, lineHeight: .normal,
            alignment: .center)
        let centred = InfoCallout(text: "Kurz", icon: .lock, tone: .locked, textSize: 14)
        XCTAssertEqual(size(of: leading, width: 350).height, 24 + max(18, InterMetrics.lineBox(14)), accuracy: 0.34)
        let chip = solid(Tokens.chip.core)
        let frame = CGSize(width: 350, height: 44)
        XCTAssertTrue(hasInk(render(leading, size: frame, scale: 1), x: 42..<60, y: 12..<32, background: chip))
        XCTAssertFalse(hasInk(render(centred, size: frame, scale: 1), x: 42..<60, y: 12..<32, background: chip))
    }
}
