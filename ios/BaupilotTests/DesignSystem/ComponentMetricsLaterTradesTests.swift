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
}
