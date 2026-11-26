import XCTest

/// M7 link coverage (D5): every test starts at the source screen through `-BaupilotScreen <Name>`, taps the link
/// element itself and asserts the identifier of the target screen. The full click paths stay in the M2-M6 flow tests.
/// The helpers are copied per class like in the other UI-test classes (project convention, no base class).
@MainActor
final class LinkCoverageUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUp() async throws {
        continueAfterFailure = false
        app = XCUIApplication()
    }

    private func launch(_ screen: String, file: StaticString = #filePath, line: UInt = #line) {
        if app.state != .notRunning { app.terminate() }
        app.launchArguments = ["-BaupilotScreen", screen]
        app.launch()
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 10), screen, file: file, line: line)
    }

    private func element(_ identifier: String) -> XCUIElement {
        app.descendants(matching: .any)[identifier]
    }

    /// Taps the element once it exists; scrolls up first when it sits below the fold.
    private func tap(_ identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        // A plain row container hands its identifier to the text children, so several elements can match.
        let target = element(identifier).firstMatch
        XCTAssertTrue(target.waitForExistence(timeout: 10), identifier, file: file, line: line)
        if !isOnScreen(target) { app.swipeUp() }
        target.tap()
    }

    /// `isHittable` throws for an element beyond the screen edge, so compare its frame with the screen instead.
    private func isOnScreen(_ target: XCUIElement) -> Bool {
        target.exists && app.frame.contains(target.frame)
    }

    /// A row is tapped at its centre, on the text column: a plain-style row only hits on drawn content.
    private func tapRow(_ identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        let row = element(identifier).firstMatch
        XCTAssertTrue(row.waitForExistence(timeout: 10), identifier, file: file, line: line)
        if !isOnScreen(row) { app.swipeUp() }
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
    }

    private func expectScreen(_ identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        XCTAssertTrue(element(identifier).waitForExistence(timeout: 5), identifier, file: file, line: line)
    }

    private func back(to identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        tap("nav.back", file: file, line: line)
        expectScreen(identifier, file: file, line: line)
    }

    private func tapTab(_ tab: String, file: StaticString = #filePath, line: UInt = #line) {
        let button = app.buttons["tab.\(tab)"]
        XCTAssertTrue(button.waitForExistence(timeout: 10), "tab.\(tab)", file: file, line: line)
        button.tap()
    }

    /// The tab target's root identifier. The Abnahmen tab restores its last view (2.2 trap), so every tap that is
    /// meant for the Gewerke screen (10) also asserts `inspections.view.trades`.
    private func expectTab(_ tab: String, file: StaticString = #filePath, line: UInt = #line) {
        expectScreen("screen.\(tab)", file: file, line: line)
        if tab == "inspections" { expectScreen("inspections.view.trades", file: file, line: line) }
    }

    /// Taps each target tab from the source tab and returns to the source after every one.
    private func expectTabs(
        from source: String, to targets: [String], file: StaticString = #filePath, line: UInt = #line
    ) {
        for target in targets {
            tapTab(target, file: file, line: line)
            expectTab(target, file: file, line: line)
            tapTab(source, file: file, line: line)
            expectScreen("screen.\(source)", file: file, line: line)
        }
    }

    /// Abnahmen root, Gewerke view: opens the trade row on its title line (the subtitle wraps narrow).
    private func openTrade(_ tradeId: String, screen: String, file: StaticString = #filePath, line: UInt = #line) {
        expectScreen("inspections.view.trades", file: file, line: line)
        let row = element("inspections.trade.\(tradeId)")
        XCTAssertTrue(row.waitForExistence(timeout: 5), tradeId, file: file, line: line)
        if !app.frame.contains(row.frame) { app.swipeUp() }
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.1, dy: 0.2)).tap()
        expectScreen(screen, file: file, line: line)
    }

    private func expectDefectForm(file: StaticString = #filePath, line: UInt = #line) {
        expectScreen("screen.defect.new", file: file, line: line)
    }

    // MARK: Tab bars (3, 4, 6, 10, 15)

    func testScheduleTabsOpenEveryTab() {
        launch("Zeitplan")
        expectScreen("screen.schedule")
        expectTabs(from: "schedule", to: ["home", "profile", "documents"])
    }

    func testScheduleAskOpensScreedTrade() {
        launch("Zeitplan")
        // fixtures/baupilot.json:206 schedule.asks[0].target = trade/screed
        tap("schedule.ask.0")
        expectScreen("screen.trade.screed")
    }

    func testProfileTabsOpenEveryTab() {
        launch("Profil")
        expectScreen("screen.profile")
        expectTabs(from: "profile", to: ["schedule", "documents", "inspections"])
    }

    func testDocumentsTabsOpenScheduleAndInspections() {
        launch("Dokumente")
        expectScreen("screen.documents")
        expectTabs(from: "documents", to: ["schedule", "inspections"])
    }

    func testInspectionsTabsOpenHomeScheduleProfile() {
        launch("AbnGewerke")
        expectScreen("inspections.view.trades")
        for target in ["home", "schedule", "profile"] {
            tapTab(target)
            expectTab(target)
            tapTab("inspections")
            expectScreen("screen.inspections")
            expectScreen("inspections.view.trades")
        }
    }

    func testDefectsViewTabsOpenEveryTab() {
        launch("AbnMaengel")
        expectScreen("screen.inspections")
        expectScreen("inspections.view.defects")
        for target in ["home", "schedule", "profile", "documents"] {
            tapTab(target)
            expectTab(target)
            tapTab("inspections")
            expectScreen("inspections.view.defects")
        }
    }

    // MARK: Back links to the Gewerke screen (10) and the elements screen (11)

    func testCarpentryRowOpensZimmererAndBack() {
        launch("AbnGewerke")
        openTrade("carpentry", screen: "screen.inspection.carpentry")
        back(to: "screen.inspections")
        expectScreen("inspections.view.trades")
    }

    func testStepCloseReturnsToElements() {
        launch("AbnPruefung")
        expectScreen("screen.inspection.windows.F8.step.3")
        back(to: "screen.inspection.windows")
    }

    func testElectricalBackReturnsToInspections() {
        launch("ElUebersicht")
        expectScreen("screen.inspection.electrical")
        back(to: "screen.inspections")
        expectScreen("inspections.view.trades")
    }

    func testScreedBackReturnsToInspections() {
        launch("EsUebersicht")
        expectScreen("screen.inspection.screed")
        back(to: "screen.inspections")
        expectScreen("inspections.view.trades")
    }

    func testPaintingBackReturnsToInspections() {
        launch("MaUebersicht")
        expectScreen("screen.inspection.painting")
        back(to: "screen.inspections")
        expectScreen("inspections.view.trades")
    }

    // MARK: Defect openers (13)

    func testPanelDefectOpensDefectForm() {
        launch("ElVerteiler")
        expectScreen("screen.inspection.electrical.panel")
        tap("step.defect")
        expectDefectForm()
    }

    func testSmokeDefectOpensDefectForm() {
        launch("ElRauchmelder")
        expectScreen("screen.inspection.electrical.smoke")
        tap("step.defect")
        expectDefectForm()
    }

    func testPaintingRoomDefectOpensDefectForm() {
        launch("MaRaum")
        expectScreen("screen.inspection.painting.room.living")
        tap("step.defect")
        expectDefectForm()
    }

    // MARK: Home (2)

    func testHomeTradeRowsOpenTheirTargets() {
        // fixtures/baupilot.json screens.Home.ios.trades (electrical, plumbing, screed), route trade/<tradeId>.
        for tradeId in ["electrical", "plumbing", "screed"] {
            launch("Home")
            expectScreen("screen.home")
            tapRow("home.trade.\(tradeId)")
            expectScreen("screen.trade.\(tradeId)")
        }
    }

    func testHomeDocumentTilesOpenDocuments() {
        // fixtures/baupilot.json screens.Home.ios.docs: four tiles, each opens the Dokumente tab.
        for index in 0..<4 {
            launch("Home")
            expectScreen("screen.home")
            tapRow("home.doc.\(index)")
            expectScreen("screen.documents")
            XCTAssertTrue(app.buttons["tab.documents"].isSelected, "home.doc.\(index)")
        }
    }

    func testHomeScheduleLinkOpensSchedule() {
        launch("Home")
        expectScreen("screen.home")
        let link = element("home.schedule")
        XCTAssertTrue(link.waitForExistence(timeout: 10))
        link.tap()
        expectScreen("screen.schedule")
        XCTAssertTrue(app.buttons["tab.schedule"].isSelected)
    }

    // MARK: Fixture-driven rows (17, 20, 24, 28, 31, 33)

    /// One row per fixture route. A UI-test target cannot read the app bundle, so the table is literal and
    /// `scripts/link-coverage` keeps it honest. `screens` are asserted together (the defects route lands on the
    /// Abnahmen root with the Mängel view selected).
    private struct RouteRow {
        let source: String
        let row: String
        let screens: [String]
        let fixtureLine: Int
    }

    private let routeRows: [RouteRow] = [
        // fixtures/baupilot.json:659 inspections.byTrade.shell.groups[0].rows[2].route = defects
        RouteRow(source: "Rohbau", row: "protocol.row.0.2", screens: ["screen.inspections", "inspections.view.defects"], fixtureLine: 659),
        // fixtures/baupilot.json:672 inspections.byTrade.shell.groups[1].rows[0].route = inspection/shell/measure
        RouteRow(source: "Rohbau", row: "protocol.row.1.0", screens: ["screen.inspection.shell.measure"], fixtureLine: 672),
        // fixtures/baupilot.json:1089 inspections.byTrade.electrical.groups[0].rows[0].route = inspection/electrical/panel
        RouteRow(source: "ElUebersicht", row: "electrical.row.0.0", screens: ["screen.inspection.electrical.panel"], fixtureLine: 1089),
        // fixtures/baupilot.json:1095 inspections.byTrade.electrical.groups[0].rows[1].route = inspection/electrical/smoke
        RouteRow(source: "ElUebersicht", row: "electrical.row.0.1", screens: ["screen.inspection.electrical.smoke"], fixtureLine: 1095),
        // fixtures/baupilot.json:1113 inspections.byTrade.electrical.groups[1].rows[0].route = inspection/electrical/room/kitchen
        RouteRow(source: "ElUebersicht", row: "electrical.row.1.0", screens: ["screen.inspection.electrical.room.kitchen"], fixtureLine: 1113),
        // fixtures/baupilot.json:1388 inspections.byTrade.plumbing.groups[0].rows[0].route = inspection/plumbing/slope/A3
        RouteRow(source: "SanUebersicht", row: "overview.row.0.0", screens: ["screen.inspection.plumbing.slope.A3"], fixtureLine: 1388),
        // fixtures/baupilot.json:1394 inspections.byTrade.plumbing.groups[0].rows[1].route = inspection/plumbing/pressure
        RouteRow(source: "SanUebersicht", row: "overview.row.0.1", screens: ["screen.inspection.plumbing.pressure"], fixtureLine: 1394),
        // fixtures/baupilot.json:1428 inspections.byTrade.plumbing.groups[2].rows[0].route = inspection/plumbing/room/bathUpper
        RouteRow(source: "SanUebersicht", row: "overview.row.2.0", screens: ["screen.inspection.plumbing.room.bathUpper"], fixtureLine: 1428),
        // fixtures/baupilot.json:1617 inspections.byTrade.screed.groups[0].rows[2].route = inspection/screed/flatness
        RouteRow(source: "EsUebersicht", row: "overview.row.0.2", screens: ["screen.inspection.screed.flatness"], fixtureLine: 1617),
        // fixtures/baupilot.json:1634 inspections.byTrade.screed.groups[1].rows[0].route = inspection/screed/readiness
        RouteRow(source: "EsUebersicht", row: "overview.row.1.0", screens: ["screen.inspection.screed.readiness"], fixtureLine: 1634),
        // fixtures/baupilot.json:1643 inspections.byTrade.screed.groups[1].rows[1].route = inspection/screed/readiness
        RouteRow(source: "EsUebersicht", row: "overview.row.1.1", screens: ["screen.inspection.screed.readiness"], fixtureLine: 1643),
        // fixtures/baupilot.json:1753 inspections.byTrade.tiles.groups[1].rows[0].route = inspection/tiles/room/bath
        RouteRow(source: "FlUebersicht", row: "overview.row.1.0", screens: ["screen.inspection.tiles.room.bath"], fixtureLine: 1753),
        // fixtures/baupilot.json:1989 inspections.byTrade.painting.groups[0].rows[0].route = inspection/painting/room/living
        RouteRow(source: "MaUebersicht", row: "overview.row.0.0", screens: ["screen.inspection.painting.room.living"], fixtureLine: 1989),
    ]

    func testFixtureRowsOpenTheirRoutes() {
        for route in routeRows {
            launch(route.source)
            tapRow(route.row)
            for screen in route.screens {
                XCTAssertTrue(
                    element(screen).waitForExistence(timeout: 5),
                    "\(route.source) \(route.row) (fixtures/baupilot.json:\(route.fixtureLine)) -> \(screen)")
            }
        }
    }
}
