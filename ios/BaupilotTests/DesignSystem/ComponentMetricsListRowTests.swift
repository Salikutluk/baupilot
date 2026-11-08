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
}
