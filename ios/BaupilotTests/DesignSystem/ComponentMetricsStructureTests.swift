import SwiftUI
import UIKit
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsStructureTests: XCTestCase {
    func testBackBarIs44Tall() {
        let link = BackBar(label: "Zurück", action: nil)
        XCTAssertEqual(size(of: link, width: 350).height, 44)

        let counter = BackBar(label: "Zurück", action: nil, trailing: .counter("2 / 5"))
        XCTAssertEqual(size(of: counter, width: 350).height, 44)

        let modal = BackBar.modal(cancel: "Abbrechen", title: "Mangel erfassen", confirm: "Sichern")
        XCTAssertEqual(size(of: modal, width: 350).height, 44)
    }

    /// The trailing buttons are measured through `LargeTitleHeader.TrailingView`, the exact view
    /// the header places in its row, at their intrinsic size (`size(of:width:)` pins the width,
    /// so it cannot report a button's own width), so the measured frame is the real button frame.
    func testHeaderButtonsAre44x44() {
        let bell = LargeTitleHeader.TrailingView(
            trailing: .bell(unread: true, label: "Mitteilungen, 2 neu", action: nil))
        let add = LargeTitleHeader.TrailingView(
            trailing: .add(label: "Dokument hinzufügen", action: nil))
        for view in [AnyView(bell), AnyView(add)] {
            let hosting = UIHostingController(rootView: view.fixedSize())
            let measured = hosting.sizeThatFits(
                in: CGSize(width: CGFloat.greatestFiniteMagnitude, height: .greatestFiniteMagnitude))
            XCTAssertEqual(measured.width, 44)
            XCTAssertEqual(measured.height, 44)
        }
    }

    func testSectionHeaderNoteDoesNotChangeTheHeaderHeight() {
        let plain = SectionHeader("Angebote", style: .title())
        let noted = SectionHeader("Angebote", style: .title(note: "2 von 3 erhalten"))
        XCTAssertEqual(size(of: noted, width: 350).height, size(of: plain, width: 350).height)
    }

    /// The view's own size at its intrinsic width (`size(of:width:)` pins the width, so it cannot report one).
    private func intrinsicSize(of view: some View) -> CGSize {
        UIHostingController(rootView: view.fixedSize()).sizeThatFits(
            in: CGSize(width: CGFloat.greatestFiniteMagnitude, height: .greatestFiniteMagnitude))
    }

    /// 250 x 320 whatever the lines, with and without lines.
    func testDocumentPreviewIs250x320() {
        func preview(_ lines: [DocumentPreview.Line]) -> DocumentPreview {
            DocumentPreview(
                issuerName: "Estrichbau Wagner GmbH", issuerAddress: "Industriestraße 4 · 68519 Viernheim",
                number: "Angebot Nr. 2026-118", reference: "BV Einfamilienhaus Musterweg 12 · gültig bis 31.01.2027",
                lines: lines, totalLabel: "Gesamt brutto", total: "9.800,00 €",
                accessibilityLabel: "Vorschau Seite 1 von 2")
        }
        let few = [DocumentPreview.Line(pos: "1", text: "Randdämmstreifen 8 mm, liefern & setzen", sum: "420,00")]
        let many = (1...5).map { DocumentPreview.Line(pos: "\($0)", text: "Position \($0)", sum: "100,00") }
        for lines in [few, many] {
            XCTAssertEqual(intrinsicSize(of: preview(lines)), CGSize(width: 250, height: 320))
        }
    }

    /// Tile 36 square; the card is 12 + the taller of tile and text column (19 + 2 + 16) + 12.
    func testHintCardTileIs36() {
        XCTAssertEqual(
            intrinsicSize(of: HintCard.Tile(icon: .clockW22, background: Tokens.accent, foreground: Tokens.onAccent)),
            CGSize(width: 36, height: 36))
        let card = HintCard(
            icon: .clockW22, title: "Rechnung prüfen & zahlen", subtitle: "Fenster & Türen · fällig 22.12.",
            amount: "18.400 €", background: Tokens.aSoft, tileBackground: Tokens.accent,
            tileForeground: Tokens.onAccent, subtitleColor: Tokens.aFg, action: nil)
        let column = InterMetrics.lineBox(15) + 2 + InterMetrics.lineBox(13)
        XCTAssertEqual(size(of: card, width: 350).height, 24 + max(36, column))
    }

    func testDetailTitleStylesStackTitleAndSubtitle() {
        let large = size(of: DetailTitle("Estrich vergleichen", subtitle: "3 Angebote", style: .size28), width: 350).height
        XCTAssertEqual(large, InterMetrics.lineBox(28) + 4 + InterMetrics.lineBox(14))
        let small = size(of: DetailTitle("Angebot Estrichbau Wagner", subtitle: "PDF · 2 Seiten", style: .size22), width: 350).height
        XCTAssertEqual(small, InterMetrics.lineBox(22) + 2 + InterMetrics.lineBox(13))
    }

    func testSectionHeaderCapsInsetDefaultsTo14() {
        let narrow = size(of: SectionHeader("Profil", style: .caps(inset: 4)), width: 350)
        let normal = size(of: SectionHeader("Profil", style: .caps), width: 350)
        XCTAssertEqual(narrow.height, normal.height)
        XCTAssertEqual(normal.height, InterMetrics.lineBox(13))
    }
}
