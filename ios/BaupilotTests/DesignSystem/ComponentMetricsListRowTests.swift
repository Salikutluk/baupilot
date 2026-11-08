import SwiftUI
import UIKit
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsListRowTests: XCTestCase {
    /// The tile is measured through `ListRow.LeadingView`, the exact view the row places in its
    /// HStack, at its intrinsic size.
    private func intrinsic(_ leading: ListRow.Leading) -> CGSize {
        let hosting = UIHostingController(rootView: ListRow.LeadingView(leading: leading).fixedSize())
        return hosting.sizeThatFits(
            in: CGSize(width: CGFloat.greatestFiniteMagnitude, height: .greatestFiniteMagnitude))
    }

    func testListRowIconTileSizes() {
        XCTAssertEqual(intrinsic(.iconTile(.bell, size: .s38)), CGSize(width: 38, height: 38))
        XCTAssertEqual(intrinsic(.iconTile(.bell, size: .s34r9)), CGSize(width: 34, height: 34))
        XCTAssertEqual(intrinsic(.iconTile(.bell, size: .s34r10)), CGSize(width: 34, height: 34))
        XCTAssertEqual(intrinsic(.iconTile(.bell, size: .s36r10)), CGSize(width: 36, height: 36))
    }

    func testListRowTextTileSizes() {
        XCTAssertEqual(intrinsic(.textTile("KW", .s48x40)), CGSize(width: 48, height: 40))
        XCTAssertEqual(intrinsic(.textTile("E1", .s36)), CGSize(width: 36, height: 36))
    }

    func testListRowPaddedHeightFollowsDensity() {
        let regular = ListRow(
            title: "Titel", leading: .iconTile(.bell, size: .s38), showsDivider: false, action: nil)
        XCTAssertEqual(size(of: regular, width: 350).height, 38 + 24)

        let dense = ListRow(
            title: "Titel", leading: .iconTile(.bell, size: .s38), density: .padded(v: 11),
            showsDivider: false, action: nil)
        XCTAssertEqual(size(of: dense, width: 350).height, 38 + 22)

        let minimum = ListRow(title: "Titel", density: .minHeight(56), showsDivider: false, action: nil)
        XCTAssertEqual(size(of: minimum, width: 350).height, 56)
    }

    func testListRowDividerAddsOnePoint() {
        let plain = ListRow(title: "Titel", showsDivider: false, action: nil)
        let divided = ListRow(title: "Titel", showsDivider: true, action: nil)
        XCTAssertEqual(
            size(of: divided, width: 350).height, size(of: plain, width: 350).height + 1)
    }

    func testListRowDefaultDividerColourIsCoreBorder() {
        XCTAssertEqual(
            ListRow(title: "Titel", showsDivider: true, action: nil).dividerColor, Tokens.border.core)
    }

    func testListRowAmountTrailingStacksBadgeUnderAmountWithGap4() {
        let amount = ListRow(
            title: "Wagner", trailing: .amount("4.820 €", size: 15, weight: .bold, color: Tokens.text),
            showsDivider: false, action: nil)
        let withBadge = ListRow(
            title: "Wagner",
            trailing: .amount(
                "4.820 €", size: 15, weight: .bold, color: Tokens.text,
                badge: StatusBadge("Günstigstes", tone: .ok, size: .micro, weight: .bold)),
            showsDivider: false, action: nil)
        let badgeHeight = size(
            of: StatusBadge("Günstigstes", tone: .ok, size: .micro, weight: .bold), width: 200
        ).height
        let amountColumn = InterMetrics.lineBox(15)
        // Column: amount line box + gap 4 + badge; the row pads 12 above and below.
        XCTAssertEqual(
            size(of: withBadge, width: 350).height, amountColumn + 4 + badgeHeight + 24,
            accuracy: 0.5)
        XCTAssertGreaterThanOrEqual(
            size(of: withBadge, width: 350).height, size(of: amount, width: 350).height)
    }

    private let compareWidths = ComparisonLayout.columns(
        cardWidth: 350, padding: 14, gap: 10, labelColumn: 104)

    /// IosVergleich:44: `min-height: 40px` content box plus the 1 px top border (Vergleich_light.png rows repeat every 41 pt).
    func testComparisonRowMinHeight40() {
        BaupilotFonts.registerIfNeeded()
        let row = ComparisonTable.RowView(
            row: .init(label: "Dicke", a: "65 mm", b: "50 mm", strong: false, highlighted: true),
            widths: compareWidths, minHeight: 40, highlight: Tokens.diff)
        XCTAssertEqual(size(of: row, width: 350).height, 41)
    }

    /// The hit area is the cell grown by `hitExpansion` above and below, without moving the layout.
    func testComparisonColumnHeaderHitArea44() {
        BaupilotFonts.registerIfNeeded()
        let nameOnly = ComparisonTable.HeaderCell(
            column: .init(name: "Fließestrich Hahn", badge: nil, action: {}), width: compareWidths.value)
        XCTAssertGreaterThanOrEqual(
            size(of: nameOnly, width: compareWidths.value).height + 2 * ComparisonTable.hitExpansion, 44)
    }
}

