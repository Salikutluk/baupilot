import SwiftUI
import XCTest

@testable import Baupilot

@MainActor
final class ComponentMetricsTileTests: XCTestCase {
    override func setUp() {
        super.setUp()
        BaupilotFonts.registerIfNeeded()
    }

    func testPhotoTileHeights() {
        func tile(_ height: PhotoTile.Height) -> PhotoTile {
            PhotoTile(
                kind: .photo(Image(systemName: "photo"), caption: "Foto oben", checked: true),
                height: height)
        }
        XCTAssertEqual(size(of: tile(.h84), width: 80).height, 84)
        XCTAssertEqual(size(of: tile(.h76), width: 80).height, 76)
    }

    /// 52 tall at any width; a nil action renders the same tile.
    func testDocumentTileIs52Tall() {
        func tile(_ action: (() -> Void)?) -> DocumentTile {
            DocumentTile(icon: .document, label: "Rechnungen", count: "4", action: action)
        }
        XCTAssertEqual(size(of: tile(nil), width: 165).height, 52)
        XCTAssertEqual(size(of: tile({}), width: 165).height, 52)
    }

    /// The link's hit area is 44 pt; the header trims 12 pt of margin on both sides (IosHome:50).
    func testSectionHeaderLinkHitAreaIs44() {
        let link = SectionHeader.Link(title: "Zeitplan", action: nil)
        XCTAssertEqual(size(of: SectionHeader.LinkButton(link: link), width: 120).height, 44)
        let withLink = size(of: SectionHeader("Als Nächstes benötigt", style: .title(link: link)), width: 350).height
        XCTAssertGreaterThan(withLink, 0)
        XCTAssertLessThan(withLink, 44, "the header trims the hit area to the title line")
    }

    /// Min-height 44: one-line and two-line rows, with and without an action.
    func testAskRowMinHeight44() {
        func row(_ action: (() -> Void)?) -> AskRow {
            AskRow(title: "Estrich", subtitle: "Start KW 8 · Angebote bis 15.01.", action: action)
        }
        XCTAssertEqual(size(of: row(nil), width: 322).height, 44)
        XCTAssertEqual(size(of: row({}), width: 322).height, 44)
    }

    /// 20 month header + 11 rows of 34 + 4 padding + the 11 pt "Heute" line.
    func testScheduleChartHeightIsHeaderPlus11RowsPlusLabel() async throws {
        let data = try await AppData.load(from: MockRepository.bundled()).schedule.layout
        let expected = 20 + 11 * 34 + 4 + InterMetrics.lineBox(11)
        let chart = ScheduleChart(layout: data, todayLabel: "Heute", accessibilityLabel: "Zeitplan")
        XCTAssertEqual(size(of: chart, width: 350).height, expected)
        XCTAssertEqual(size(of: chart, width: 350).width, 350)
    }

    /// 48 circle at any width of the proposal.
    func testAvatarIs48() {
        let avatar = Avatar(initials: "MM", size: .s48)
        XCTAssertEqual(size(of: avatar, width: 48), CGSize(width: 48, height: 48))
    }

    /// The icon tile is 34 square; unread and read rows are equally tall (the clear dot keeps its space)
    /// and the divider adds 1 pt on top.
    func testNotificationRowTileIs34() {
        func row(unread: Bool, divider: Bool = false, action: (() -> Void)? = nil) -> NotificationRow {
            NotificationRow(
                icon: .clockW22, iconBackground: Tokens.aSoft, iconForeground: Tokens.aFg,
                title: "Frist", when: "9:41", text: "Kurz.", unread: unread,
                showsDivider: divider, action: action)
        }
        let tile = NotificationRow.Tile(icon: .clockW22, background: Tokens.aSoft, foreground: Tokens.aFg)
        XCTAssertEqual(size(of: tile, width: 34), CGSize(width: 34, height: 34))
        let unread = size(of: row(unread: true), width: 350).height
        XCTAssertGreaterThanOrEqual(unread, 34 + 24)
        XCTAssertEqual(size(of: row(unread: false), width: 350).height, unread)
        XCTAssertEqual(size(of: row(unread: true, action: {}), width: 350).height, unread)
        XCTAssertEqual(size(of: row(unread: false, divider: true), width: 350).height, unread + 1)
    }

    /// Min-height 46 for every trailing kind; the divider adds its 1 pt on top.
    func testSettingsRowMinHeight46() {
        func row(_ trailing: SettingsRow.Trailing, divider: Bool = false) -> SettingsRow {
            SettingsRow(label: "Projekt", trailing: trailing, showsDivider: divider)
        }
        XCTAssertEqual(size(of: row(.link(value: "Musterweg 12")), width: 350).height, 46)
        XCTAssertEqual(size(of: row(.link(value: nil)), width: 350).height, 46)
        XCTAssertEqual(size(of: row(.none), width: 350).height, 46)
        XCTAssertEqual(size(of: row(.toggle(isOn: .constant(true))), width: 350).height, 46)
        XCTAssertEqual(size(of: row(.none, divider: true), width: 350).height, 47)
    }
}
