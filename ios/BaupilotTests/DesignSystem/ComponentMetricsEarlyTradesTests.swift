import SwiftUI
import UIKit
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsEarlyTradesTests: XCTestCase {
    private func intrinsic(_ view: some View) -> CGSize {
        let hosting = UIHostingController(rootView: view.fixedSize())
        return hosting.sizeThatFits(
            in: CGSize(width: CGFloat.greatestFiniteMagnitude, height: .greatestFiniteMagnitude))
    }

    /// IosErd:40: `min-height: 46px`, a one-line label stays 46 tall.
    func testProtocolRowMinHeightIs46() {
        let row = ProtocolRow(label: "Aushub", meta: "12.10.", mark: .ok, showsDivider: false, action: nil)
        XCTAssertEqual(size(of: row, width: 350).height, 46)
        let defect = ProtocolRow(label: "Aushub", meta: "Mangel", mark: .defect, showsDivider: false, action: nil)
        XCTAssertEqual(size(of: defect, width: 350).height, 46)
    }

    func testProtocolRowDividerAddsOnePoint() {
        let plain = ProtocolRow(label: "Aushub", meta: "12.10.", mark: .ok, showsDivider: false, action: nil)
        let divided = ProtocolRow(label: "Aushub", meta: "12.10.", mark: .ok, showsDivider: true, action: nil)
        XCTAssertEqual(size(of: divided, width: 350).height, size(of: plain, width: 350).height + 1)
    }

    /// A long label wraps greedily and grows the row past the minimum.
    func testProtocolRowGrowsWhenTheLabelWraps() {
        let row = ProtocolRow(
            label: "Bewehrung, Schalung und Betonierabschnitt gemäß Statik geprüft und dokumentiert",
            meta: "12.10.", mark: .ok, showsDivider: false, action: nil)
        XCTAssertEqual(size(of: row, width: 350).height, 51)
    }

    /// The document button draws 36 tall; its hit shape grows to 44 without moving the layout.
    func testBannerDocumentButtonIs36TallWithA44HitArea() {
        let button = ProtocolBanner.DocumentButton(label: "PDF")
        XCTAssertEqual(intrinsic(button).height, 36)
        let box = CGRect(x: 0, y: 0, width: 60, height: 36)
        let hit = ProtocolBanner.DocumentButton.hitShape.path(in: box).boundingRect
        XCTAssertGreaterThanOrEqual(hit.height, 44)
        XCTAssertGreaterThanOrEqual(hit.width, 44)
        XCTAssertTrue(hit.contains(box))
    }

    func testBannerHeightFollowsTheDocumentButton() {
        let banner = ProtocolBanner(
            icon: .checkCircleW24, title: "Titel", text: "Text", background: Tokens.oSoft, foreground: Tokens.oFg,
            trailing: .document(label: "PDF", action: nil), action: nil)
        // padding 12 + max(text 18 + 1 + 16, button 36) + 12
        XCTAssertEqual(size(of: banner, width: 350).height, 12 + 36 + 12)
    }

    /// A parent stack with a finite height must not squeeze a wrapped label back to the 46 pt minimum.
    func testProtocolRowKeepsItsWrappedHeightInAFiniteHeightStack() {
        final class Box { var height: CGFloat = 0 }
        let box = Box()
        let long = "Bewehrung, Schalung und Betonierabschnitt gemäß Statik geprüft und dokumentiert"
        let card = Card(style: .row, padding: EdgeInsets()) {
            VStack(spacing: 0) {
                ProtocolRow(label: "Aushub", meta: "12.04.", mark: .ok, showsDivider: false, action: nil)
                ProtocolRow(label: long, meta: "14.04.", mark: .ok, showsDivider: true, action: nil)
            }
        }
        .background(GeometryReader { proxy in Color.clear.onAppear { box.height = proxy.size.height } })
        let page = VStack(alignment: .leading, spacing: 12) { card }
            .frame(width: 350, height: 140, alignment: .topLeading)
        let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 350, height: 140))
        let host = UIHostingController(rootView: page)
        window.rootViewController = host
        window.makeKeyAndVisible()
        host.view.layoutIfNeeded()
        RunLoop.current.run(until: Date().addingTimeInterval(0.2))
        XCTAssertEqual(box.height, 46 + 1 + 51)
    }

    /// IosRohbauMasse:39-43: 4 + 40 + 4 + 54 + 4 + 40 + 4 = 150 wide, 48 high; the buttons stay 40, their hit shape is 44.
    func testValueStepperIs48By150WithButtons40AndHitArea44() {
        let stepper = ValueStepper(label: "Breite", value: "1005", onDecrement: nil, onIncrement: nil)
        let natural = intrinsic(stepper)
        XCTAssertEqual(natural.height, 48)
        XCTAssertEqual(natural.width, 150)
        XCTAssertEqual(intrinsic(ValueStepper.StepButton(glyph: "−", label: "Breite verringern", action: nil)),
                       CGSize(width: 40, height: 40))
        let box = CGRect(x: 0, y: 0, width: 40, height: 40)
        let hit = ValueStepper.StepButton.hitShape.path(in: box).boundingRect
        XCTAssertGreaterThanOrEqual(hit.height, 44)
        XCTAssertGreaterThanOrEqual(hit.width, 44)
        XCTAssertTrue(hit.contains(box))
    }

    func testValueStepperGlyphsAreMinusSignAndPlus() {
        XCTAssertEqual(ValueStepper.minusGlyph, "−")
        XCTAssertEqual(ValueStepper.plusGlyph, "+")
    }

    func testCodeTileIs40Tall() {
        let tile = CodeTile("F1", background: Tokens.oSoft, foreground: Tokens.oFg)
        XCTAssertEqual(size(of: tile, width: 83).height, 40)
    }

    /// IosRohbauMasse:65: `min-height: 52px`; a long title grows the row past it.
    func testCheckRowMinHeightIs52() {
        let row = CheckRow(
            title: "Wände im Lot", subtitle: "2-m-Wasserwaage", trailing: "8 / 8",
            fill: Tokens.oFg, ink: Tokens.surface, trailingColor: Tokens.oFg)
        XCTAssertEqual(size(of: row, width: 350).height, 52)
    }

    /// The text-only banner (no icon, no subtitle) is padding 12 + one 15 pt line + 12.
    func testTextOnlyAlertBannerHidesIconAndSubtitle() {
        let withBoth = AlertBanner(
            icon: .alertTriangleW22, title: "Innerhalb der Toleranz", subtitle: "Untertitel",
            background: Tokens.oSoft, foreground: Tokens.oFg)
        let textOnly = AlertBanner(
            icon: nil, title: "Innerhalb der Toleranz", subtitle: nil, background: Tokens.oSoft, foreground: Tokens.oFg)
        XCTAssertLessThan(size(of: textOnly, width: 350).height, size(of: withBoth, width: 350).height)
        XCTAssertEqual(size(of: textOnly, width: 350).height, 12 + InterMetrics.lineBox(15) + 12)
    }

    /// IosElUebersicht:30-43: the two phase tiles share one grid row, so the shorter active tile stretches to the
    /// done tile: both 81 tall, columns (350 - 8) / 2 = 171.
    func testPhaseTilePairHasEqualHeightsWith171Columns() {
        final class Boxes {
            var done = CGSize.zero
            var active = CGSize.zero
        }
        let boxes = Boxes()
        let pair = HStack(spacing: 8) {
            PhaseTile(
                label: "1 · Rohinstallation", value: "Abgenommen", date: "am 29.01.2027", state: .done,
                fillsHeight: true)
                .background(GeometryReader { proxy in Color.clear.onAppear { boxes.done = proxy.size } })
            PhaseTile(
                label: "2 · Fertiginstallation", value: "4 von 11 geprüft", state: .active(progress: 0.36),
                track: Tokens.track.elUebersicht, fillsHeight: true)
                .background(GeometryReader { proxy in Color.clear.onAppear { boxes.active = proxy.size } })
        }
        .fixedSize(horizontal: false, vertical: true)
        .frame(width: 350, height: 200, alignment: .topLeading)
        let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 350, height: 200))
        let host = UIHostingController(rootView: pair)
        window.rootViewController = host
        window.makeKeyAndVisible()
        host.view.layoutIfNeeded()
        RunLoop.current.run(until: Date().addingTimeInterval(0.2))
        XCTAssertEqual(boxes.done, CGSize(width: 171, height: 81))
        XCTAssertEqual(boxes.active, CGSize(width: 171, height: 81))
    }

    /// Without `fillsHeight` the active tile keeps its own (shorter) height: the M4 rendering is unchanged (I8).
    func testPhaseTileKeepsItsOwnHeightByDefault() {
        let done = PhaseTile(label: "1 · Rohinstallation", value: "Abgenommen", date: "am 29.01.2027", state: .done)
        let active = PhaseTile(label: "2 · Fertiginstallation", value: "4 von 11 geprüft", state: .active(progress: 0.36))
        XCTAssertEqual(size(of: done, width: 171).height, 81)
        XCTAssertLessThan(size(of: active, width: 171).height, 81)
    }

    /// IosElUebersicht:48: text gap 1 instead of the density's 2 (min-height) / 3 (padded).
    func testListRowTextGapOverridesTheDensityGap() {
        func row(gap: CGFloat?) -> ListRow {
            ListRow(
                title: "Zählerschrank", subtitle: "Verteiler und FI-Schutzschalter", density: .padded(v: 12),
                showsDivider: false, textGap: gap, action: nil)
        }
        let standard = size(of: row(gap: nil), width: 350).height
        XCTAssertEqual(standard, 12 + InterMetrics.lineBox(15) + 3 + InterMetrics.lineBox(13) + 12)
        XCTAssertEqual(size(of: row(gap: 1), width: 350).height, standard - 2)
    }

    /// A greedy title wraps onto a second line box and grows the 52 pt minimum row.
    func testListRowGreedyTitleWrapsAndGrowsTheRow() {
        func row(title: String) -> ListRow {
            ListRow(
                title: title, subtitle: "Haustechnik",
                leading: .iconTile(.home, size: .s34r9), trailing: .badge(StatusBadge("Offen", tone: .open)),
                density: .minHeight(52), showsDivider: false, textGap: 1, titleLines: .greedy, action: nil)
        }
        XCTAssertEqual(size(of: row(title: "Küche"), width: 350).height, 52)
        let wrapped = size(
            of: row(title: "Schutzleiter und Fehlerstrom im Gebäude prüfen"), width: 350).height
        // no vertical padding: two title line boxes + gap 1 + one subtitle line box carry the row past 52
        XCTAssertEqual(wrapped, InterMetrics.lineBox(15) * 2 + 1 + InterMetrics.lineBox(13))
    }

    // MARK: Electrical steps (M5 phase 13)

    private func socket(_ id: String, border: ColorToken? = nil) -> SocketTile {
        SocketTile(
            id: id, label: "korrekt",
            style: SocketTile.Style(background: Tokens.oSoft, foreground: Tokens.oFg, border: border),
            accessibilityLabel: "Steckdose \(id): korrekt", identifier: "room.socket.\(id)")
    }

    /// IosElRaum:46: `height: 56px`; the 2 pt `now` border is drawn inside, so it does not grow the tile.
    func testSocketTileIs56TallWithAndWithoutBorder() {
        XCTAssertEqual(size(of: socket("S1"), width: 72.5).height, 56)
        XCTAssertEqual(size(of: socket("S6", border: Tokens.accent), width: 72.5).height, 56)
    }

    /// 4 columns, gap 8: a full second row and a partly filled one both add 8 + 56.
    func testSocketGridRowsAreStacked56HighWithGap8() {
        func grid(_ count: Int) -> SocketGrid {
            SocketGrid(tiles: (1...count).map { socket("S\($0)") })
        }
        XCTAssertEqual(size(of: grid(4), width: 314).height, 56)
        XCTAssertEqual(size(of: grid(5), width: 314).height, 56 + 8 + 56)
        XCTAssertEqual(size(of: grid(8), width: 314).height, 56 + 8 + 56)
    }

    /// IosElVerteiler:45: 15 x 1.45 line boxes, gap 4.
    func testNumberedListStacksItemsWithGap4() {
        let list = NumberedList(items: ["Eins", "Zwei", "Drei"], color: Tokens.muted)
        XCTAssertEqual(size(of: list, width: 282).height, 3 * 15 * 1.45 + 2 * 4, accuracy: 0.34)
    }

    /// A long item wraps inside the hanging indent: the marker column stays 20 wide.
    func testNumberedListItemWrapsInsideTheIndent() {
        let one = NumberedList(items: ["Hebel"], color: Tokens.muted)
        let long = NumberedList(
            items: ["Der Hebel muss sofort nach unten springen, sonst ist der Schalter defekt und zu ersetzen."],
            color: Tokens.muted)
        XCTAssertEqual(size(of: one, width: 282).height, 15 * 1.45, accuracy: 0.34)
        XCTAssertGreaterThan(size(of: long, width: 282).height, 2 * 15 * 1.45 - 0.34)
    }

    /// IosElVerteiler:50: `min-height: 48px`; the divider (1 pt) sits on top of every row after the first.
    func testStatusListRowMinHeightIs48WithDividerBetweenRows() {
        func row(_ title: String) -> StatusList.Row {
            StatusList.Row(title: title, subtitle: "Bad", badge: StatusBadge("Ausgelöst", tone: .ok))
        }
        func list(_ rows: [StatusList.Row]) -> StatusList {
            StatusList(rows: rows, background: Tokens.chip.elVerteiler, divider: Tokens.border.elVerteiler)
        }
        XCTAssertEqual(size(of: list([row("FI 1")]), width: 282).height, 48)
        XCTAssertEqual(size(of: list([row("FI 1"), row("FI 2")]), width: 282).height, 48 + 1 + 48)
    }

    /// IosElRauchmelder:44: ring 22 (+1 margin) against a 15/600 title, gap 2, and a 13 x 1.4 hint.
    func testCheckHintRowHeightFollowsTheTextColumn() {
        let row = CheckHintRow(
            title: "Prüftaste", hint: "Taste drücken", done: false,
            ok: Tokens.ok.elRauchmelder, okInk: Tokens.okInk.elRauchmelder, track: Tokens.track.elRauchmelder)
        XCTAssertEqual(size(of: row, width: 282).height, InterMetrics.lineBox(15) + 2 + 13 * 1.4, accuracy: 0.34)
        let wrapped = CheckHintRow(
            title: "Prüftaste", hint: "Taste drücken und mindestens fünf Sekunden halten, bis der Ton kommt",
            done: true, ok: Tokens.ok.elRauchmelder, okInk: Tokens.okInk.elRauchmelder,
            track: Tokens.track.elRauchmelder)
        XCTAssertEqual(
            size(of: wrapped, width: 282).height, InterMetrics.lineBox(15) + 2 + 2 * 13 * 1.4, accuracy: 0.34)
    }

    /// IosElRauchmelder:61: `min-height: 46px`; the divider adds one point.
    func testStatusDotRowMinHeightIs46() {
        func row(divider: Bool) -> StatusDotRow {
            StatusDotRow(
                dot: Tokens.ok.elRauchmelder, title: "Flur OG", status: "Geprüft", statusColor: Tokens.oFg,
                showsDivider: divider, divider: Tokens.border.core)
        }
        XCTAssertEqual(size(of: row(divider: false), width: 350).height, 46)
        XCTAssertEqual(size(of: row(divider: true), width: 350).height, 47)
    }

    /// IosElRauchmelder:70: `min-height: 52px`.
    func testToggleRowMinHeightIs52() {
        let row = ToggleRow(
            title: "Jährlich an Test erinnern", subtitle: "Batterien laut Hersteller bis 2037", isOn: true,
            onColor: Tokens.ok.elRauchmelder, accessibilityLabel: "Jährlich an Test erinnern", action: nil)
        XCTAssertEqual(size(of: row, width: 350).height, 52)
    }

    /// `titleLines: .greedy` wraps the open step's title onto a second line box (ElRaum / ElVerteiler next rows).
    func testStepRowGreedyTitleWrapsOntoASecondLineBox() {
        let title = "Herd, Spülmaschine & Dunstabzug angeschlossen"
        let single = StepRow(number: 4, title: title, state: .open(), minHeight: 0)
        let greedy = StepRow(number: 4, title: title, state: .open(), minHeight: 0, titleLines: .greedy)
        XCTAssertEqual(size(of: single, width: 350).height, 22)
        XCTAssertEqual(size(of: greedy, width: 350).height, 2 * InterMetrics.lineBox(15))
    }

    /// IosElRauchmelder:36: the `.note` header has no number circle (26) and sits on the 19/700 line box.
    func testCurrentStepCardNoteHeaderHasNoNumberCircle() {
        let numbered = CurrentStepCard(number: 1, title: "Flur OG", spacing: 12) { Color.clear.frame(height: 10) }
        let note = CurrentStepCard(
            header: .note(title: "Flur OG", note: "Jetzt prüfen", noteColor: Tokens.aFg), spacing: 12
        ) { Color.clear.frame(height: 10) }
        XCTAssertEqual(
            size(of: numbered, width: 350).height - size(of: note, width: 350).height,
            26 - InterMetrics.lineBox(19), accuracy: 0.34)
    }

    /// IosElVerteiler has no subtitle; a subtitle adds `gap` and its line box. `gap` replaces the style's 4.
    func testDetailTitleSubtitleIsOptionalAndGapIsConfigurable() {
        let bare = DetailTitle("Zählerschrank", subtitle: nil, style: .size28)
        let with = DetailTitle("Zählerschrank", subtitle: "EG", style: .size28)
        let wide = DetailTitle("Zählerschrank", subtitle: "EG", style: .size28, gap: 6)
        let bareHeight = size(of: bare, width: 350).height
        XCTAssertEqual(size(of: with, width: 350).height, bareHeight + 4 + InterMetrics.lineBox(14), accuracy: 0.34)
        XCTAssertEqual(size(of: wide, width: 350).height, bareHeight + 6 + InterMetrics.lineBox(14), accuracy: 0.34)
    }

    /// Rauchmelder: a 2-line subtitle with line height x 1.4 is two 19.6 pt boxes.
    func testDetailTitleSubtitleLineHeightAppliesToWrappedLines() {
        let long = "Pflicht in Schlaf- und Kinderzimmern und in Fluren, die als Fluchtweg dienen."
        let title = DetailTitle(
            "Rauchwarnmelder", subtitle: long, style: .size28, subtitleLines: .greedy, subtitleLineHeight: 1.4,
            gap: 6)
        let bare = DetailTitle("Rauchwarnmelder", subtitle: nil, style: .size28)
        XCTAssertEqual(
            size(of: title, width: 350).height - size(of: bare, width: 350).height, 6 + 2 * 14 * 1.4, accuracy: 0.34)
    }
}
