import XCTest

/// M6 later trades (section 9 UI): the cross-screen links and gestures of the plumbing, screed, tiles and
/// painting screens, driven through the real shell.
@MainActor
final class LaterTradesUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUp() async throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    private func element(_ identifier: String) -> XCUIElement {
        app.descendants(matching: .any)[identifier]
    }

    private func logIn() {
        XCTAssertTrue(element("screen.login").waitForExistence(timeout: 10))
        let submit = app.buttons["login.submit"]
        XCTAssertTrue(submit.waitForExistence(timeout: 5))
        submit.tap()
        XCTAssertTrue(element("screen.home").waitForExistence(timeout: 10))
    }

    private func tap(_ identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        // A plain row container hands its identifier to the text children, so several elements can match.
        let target = element(identifier).firstMatch
        XCTAssertTrue(target.waitForExistence(timeout: 5), identifier, file: file, line: line)
        if !(target.exists && app.frame.contains(target.frame)) { app.swipeUp() }
        target.tap()
    }

    /// An overview row is tapped at its centre, on the text column: the greedy text must not swallow the tap.
    private func tapRow(_ identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        let row = element(identifier).firstMatch
        XCTAssertTrue(row.waitForExistence(timeout: 5), identifier, file: file, line: line)
        if !app.frame.contains(row.frame) { app.swipeUp() }
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
    }

    private func expectScreen(_ identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        XCTAssertTrue(element(identifier).waitForExistence(timeout: 5), identifier, file: file, line: line)
    }

    private func back(to identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        tap("nav.back")
        expectScreen(identifier, file: file, line: line)
    }

    private func expectLabel(
        _ identifier: String, _ expected: String, file: StaticString = #filePath, line: UInt = #line
    ) {
        let target = element(identifier).firstMatch
        XCTAssertTrue(target.waitForExistence(timeout: 5), identifier, file: file, line: line)
        XCTAssertTrue(target.label.contains(expected), "\(identifier): \(target.label)", file: file, line: line)
    }

    /// A plain-style trade row only hits on drawn content, so tap on its title line.
    private func openTrade(_ tradeId: String, screen: String, file: StaticString = #filePath, line: UInt = #line) {
        expectScreen("inspections.view.trades", file: file, line: line)
        let row = element("inspections.trade.\(tradeId)")
        XCTAssertTrue(row.waitForExistence(timeout: 5), tradeId, file: file, line: line)
        if !app.frame.contains(row.frame) { app.swipeUp() }
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.1, dy: 0.2)).tap()
        expectScreen(screen, file: file, line: line)
    }

    private func openTradeFromHome(_ tradeId: String) {
        logIn()
        tap("tab.inspections")
        expectScreen("screen.inspections")
        openTrade(tradeId, screen: "screen.inspection.\(tradeId)")
    }

    func testSlopeCalculatorUpdatesLive() throws {
        openTradeFromHome("plumbing")
        tapRow("overview.row.0.0")
        expectScreen("screen.inspection.plumbing.slope.A3")
        tap("slope.drop.decrement")
        tap("slope.drop.decrement")
        expectLabel("slope.drop.value", "1,5 cm")
        // D7: the card is one element; `.combine` joins the parts in an unspecified format (R12), so check each part.
        expectLabel("slope.result", "0,75 %")
        expectLabel("slope.result", "Zu flach · Soll ≥ 1,0 %")
        tap("slope.drop.increment")
        expectLabel("slope.result", "1,00 %")
        expectLabel("slope.result", "Ausreichend · Soll ≥ 1,0 %")
        XCTAssertFalse(element("slope.percent").exists)
        XCTAssertFalse(element("slope.verdict").exists)
    }

    func testFlatnessSaveAddsPoint() throws {
        openTradeFromHome("screed")
        tapRow("overview.row.0.2")
        expectScreen("screen.inspection.screed.flatness")
        tap("flatness.gap.increment")
        tap("flatness.gap.increment")
        expectLabel("flatness.result", "Über Grenzwert – als Mangel erfassen")
        tap("flatness.save")
        expectLabel("flatness.point.3", "M4 · 5 mm")
        let count = element("flatness.count").firstMatch
        XCTAssertTrue(count.waitForExistence(timeout: 5))
        XCTAssertEqual(count.label.lowercased(), "gemessen (4)")
        expectLabel("flatness.defect", "2 Messpunkte über Grenzwert → Mangel erfassen")
        tap("flatness.defect")
        expectScreen("screen.defect.new")
        tap("defect.cancel")
        expectScreen("screen.inspection.screed.flatness")
    }

    func testPlumbingLinks() throws {
        openTradeFromHome("plumbing")
        let targets: [(row: String, screen: String)] = [
            ("overview.row.0.0", "screen.inspection.plumbing.slope.A3"),
            ("overview.row.0.1", "screen.inspection.plumbing.pressure"),
            ("overview.row.2.0", "screen.inspection.plumbing.room.bathUpper"),
        ]
        for target in targets {
            tapRow(target.row)
            expectScreen(target.screen)
            back(to: "screen.inspection.plumbing")
        }
        // "Dämmung Warmwasser" has no target.
        tapRow("overview.row.0.2")
        expectScreen("screen.inspection.plumbing")
        XCTAssertFalse(element("screen.inspection.plumbing.slope.A3").exists)
        XCTAssertFalse(element("screen.inspection.plumbing.pressure").exists)
        XCTAssertFalse(element("screen.inspection.plumbing.room.bathUpper").exists)
    }

    func testScreedLinks() throws {
        openTradeFromHome("screed")
        let targets: [(row: String, screen: String)] = [
            ("overview.row.0.2", "screen.inspection.screed.flatness"),
            ("overview.row.1.0", "screen.inspection.screed.readiness"),
            ("overview.row.1.1", "screen.inspection.screed.readiness"),
        ]
        for target in targets {
            tapRow(target.row)
            expectScreen(target.screen)
            back(to: "screen.inspection.screed")
        }
        // "Risse & Hohlstellen" has no target.
        tapRow("overview.row.0.3")
        expectScreen("screen.inspection.screed")
        XCTAssertFalse(element("screen.inspection.screed.flatness").exists)
        XCTAssertFalse(element("screen.inspection.screed.readiness").exists)
    }

    func testTilesAndPaintingLinks() throws {
        openTradeFromHome("tiles")
        tapRow("overview.row.1.0")
        expectScreen("screen.inspection.tiles.room.bath")
        // I11 (kept): a tap on a tile does nothing. D8: the grid is one element, so the tile is hit by coordinate.
        let grid = element("tiles.grid").firstMatch
        XCTAssertTrue(grid.waitForExistence(timeout: 5))
        if !app.frame.contains(grid.frame) { app.swipeUp() }
        let valueBefore = grid.value as? String
        XCTAssertFalse(valueBefore?.isEmpty ?? true, "the tile grid must report a value")
        let tileWidth = (grid.frame.width - 2 * 8 - 7 * 4) / 8
        let column: CGFloat = 2
        let centre = CGVector(
            dx: (8 + column * (tileWidth + 4) + tileWidth / 2) / grid.frame.width,
            dy: (8 + 44 / 2) / grid.frame.height)
        grid.coordinate(withNormalizedOffset: centre).tap()
        expectScreen("screen.inspection.tiles.room.bath")
        XCTAssertFalse(element("screen.defect.new").exists)
        XCTAssertEqual(grid.value as? String, valueBefore)
        XCTAssertFalse(element("tiles.tile.tile-3").exists)
        back(to: "screen.inspection.tiles")
        back(to: "screen.inspections")

        openTrade("painting", screen: "screen.inspection.painting")
        tapRow("overview.row.0.0")
        expectScreen("screen.inspection.painting.room.living")
        back(to: "screen.inspection.painting")
    }

    func testDefectFromM6ScreensReturnsToOpener() throws {
        openTradeFromHome("plumbing")
        tapRow("overview.row.0.0")
        expectScreen("screen.inspection.plumbing.slope.A3")
        tap("slope.defect")
        expectScreen("screen.defect.new")
        tap("defect.save")
        expectScreen("screen.inspection.plumbing.slope.A3")

        back(to: "screen.inspection.plumbing")
        back(to: "screen.inspections")
        openTrade("tiles", screen: "screen.inspection.tiles")
        tapRow("overview.row.1.0")
        expectScreen("screen.inspection.tiles.room.bath")
        tap("tiles.defect")
        expectScreen("screen.defect.new")
        tap("defect.cancel")
        expectScreen("screen.inspection.tiles.room.bath")
    }

    /// D12: the section tile's label is its visible texts joined with ", ", without an invented prefix.
    func testSectionTileLabelMatchesVisibleText() throws {
        openTradeFromHome("plumbing")
        tapRow("overview.row.0.0")
        expectScreen("screen.inspection.plumbing.slope.A3")
        let tile = element("slope.section.A3").firstMatch
        XCTAssertTrue(tile.waitForExistence(timeout: 5))
        XCTAssertEqual(tile.label, "A3, jetzt")
    }
}
