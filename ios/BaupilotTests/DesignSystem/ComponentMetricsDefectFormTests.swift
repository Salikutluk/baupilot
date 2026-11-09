import SwiftUI
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsDefectFormTests: XCTestCase {
    /// IosAbnMangel:35: 3 rows at line-height 1.4, padding 12, 1 pt border (border-box) = 93.2 by arithmetic;
    /// the reference renders 93.0 (Chrome rounds the text block), within 1/3 of the planned figure.
    func testTextAreaFieldThreeRowsIs93Point2Tall() {
        let field = TextAreaField(
            label: "Beschreibung", text: .constant("Flügel fällt ab"), border: Tokens.border.abnMangel,
            accessibilityLabel: "Beschreibung")
        let box = TextAreaField.boxHeight(rows: 3)
        XCTAssertEqual(box, 93.2, accuracy: 1.0 / 3)
        // label line box + gap 6 + box
        XCTAssertEqual(size(of: field, width: 350).height, InterMetrics.lineBox(13) + 6 + box, accuracy: 1.0 / 3)
    }

    func testDisplayFieldBoxIs48Tall() {
        let field = DisplayField(label: "Behebung bis", value: "Fr, 15.01.2027", border: Tokens.border.abnMangel)
        XCTAssertEqual(size(of: field, width: 350).height, InterMetrics.lineBox(13) + 8 + 48, accuracy: 1.0 / 3)
    }

    func testFormRowIsAtLeast48TallAndTheDividerAddsOne() {
        let plain = FormRow(
            title: "Zuständig", trailing: .value("Fensterbau Weber GmbH"), showsDivider: false,
            divider: Tokens.border.abnMangel)
        let divided = FormRow(
            title: "Handwerker per E-Mail informieren",
            trailing: .toggle(isOn: true, onColor: Tokens.ok.abnMangel, accessibilityLabel: "x") {},
            showsDivider: true, divider: Tokens.border.abnMangel)
        XCTAssertEqual(size(of: plain, width: 350).height, 48)
        XCTAssertEqual(size(of: divided, width: 350).height, 49)
    }

    func testOptionChipsAre34Tall() {
        let chips = OptionChips(
            items: ["+ 14 Tage", "+ 4 Wochen", "Eigenes Datum"], selection: 1, border: Tokens.border.abnMangel
        ) { _ in }
        XCTAssertEqual(size(of: chips, width: 350).height, 34)
    }

    /// R16: the chip draws 34 pt but its hit shape grows to 44 pt tall.
    func testOptionChipsHitShapeIs44TallAndContainsTheChip() {
        let box = CGRect(x: 0, y: 0, width: 96, height: 34)
        let hit = OptionChips.hitShape.path(in: box).boundingRect
        XCTAssertEqual(hit.height, 44)
        XCTAssertTrue(hit.contains(box))
    }

    /// Selecting the chip must not shift the row: the selected weight is wider, never taller.
    func testOptionChipsSelectionDoesNotChangeHeight() {
        for selection in [nil, 0, 2] as [Int?] {
            let chips = OptionChips(items: ["A", "B", "C"], selection: selection, border: Tokens.border.abnMangel) { _ in }
            XCTAssertEqual(size(of: chips, width: 350).height, 34)
        }
    }

    func testAlertBannerStacksTitleAndSubtitleWithPadding() {
        let banner = AlertBanner(
            icon: .alertTriangleW22, title: "F2 · Wohnen · Schritt 1",
            subtitle: "Drehstellung: Flügel bleibt nicht stehen",
            background: Tokens.eSoft.core, foreground: Tokens.eFg)
        XCTAssertEqual(
            size(of: banner, width: 350).height, 24 + InterMetrics.lineBox(15) + 1 + InterMetrics.lineBox(13),
            accuracy: 1.0 / 3)
    }
}
