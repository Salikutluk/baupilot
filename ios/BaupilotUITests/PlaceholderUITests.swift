import XCTest

/// M7 placeholders (D13, D20, I4): an element that the prototype shows without a real target is visible and does
/// nothing. Each test launches the source screen through `-BaupilotScreen <Name>`, taps the placeholder and asserts
/// that the screen identifiers are exactly the current screen and that no tab changed. Placeholders that already have
/// a test (the Login "Vergessen?" in AppNavigationUITests, the Gewerk Lorenz offer, the Mitteilungen item, the
/// AbnGewerke Dachdecker row, the FlBad tile and the null-route rows of M4-M6) are listed in the manifest, not
/// repeated here. The helpers are copied per class like in the other UI-test classes (project convention).
@MainActor
final class PlaceholderUITests: XCTestCase {
    private static let tabs = ["home", "schedule", "inspections", "documents", "profile"]

    private var app: XCUIApplication!

    override func setUp() async throws {
        continueAfterFailure = false
        app = XCUIApplication()
    }

    // MARK: Helpers

    private func launch(_ screen: String, file: StaticString = #filePath, line: UInt = #line) {
        if app.state != .notRunning { app.terminate() }
        app.launchArguments = ["-BaupilotScreen", screen]
        app.launch()
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 10), screen, file: file, line: line)
    }

    private func element(_ identifier: String) -> XCUIElement {
        app.descendants(matching: .any)[identifier]
    }

    private func screenIdentifiers() -> [String] {
        Set(
            app.descendants(matching: .any)
                .matching(NSPredicate(format: "identifier BEGINSWITH 'screen.'"))
                .allElementsBoundByIndex
                .map(\.identifier)
        ).sorted()
    }

    /// The selected tab, or nil while the tab bar is hidden (a pushed route).
    private func selectedTab() -> String? {
        Self.tabs.first { app.buttons["tab.\($0)"].exists && app.buttons["tab.\($0)"].isSelected }
    }

    /// `isHittable` throws for an element beyond the screen edge, so compare its frame with the screen instead.
    private func isOnScreen(_ target: XCUIElement) -> Bool {
        target.exists && app.frame.contains(target.frame)
    }

    /// Scrolls the element into view: down the page first, back up when it sits above.
    private func reveal(_ target: XCUIElement) {
        var attempts = 0
        while !isOnScreen(target), attempts < 4 {
            app.swipeUp()
            attempts += 1
        }
        attempts = 0
        while !isOnScreen(target), attempts < 8 {
            app.swipeDown()
            attempts += 1
        }
    }

    private func settle(_ seconds: TimeInterval = 0.5) {
        let wait = XCTestExpectation(description: "settle")
        wait.isInverted = true
        _ = XCTWaiter().wait(for: [wait], timeout: seconds)
    }

    private func identifiers(prefix: String) -> [String] {
        var seen: [String] = []
        let matches = app.descendants(matching: .any)
            .matching(NSPredicate(format: "identifier BEGINSWITH %@", prefix))
            .allElementsBoundByIndex
        for match in matches where !seen.contains(match.identifier) { seen.append(match.identifier) }
        return seen
    }

    /// Taps the placeholder at its centre (a plain row only hits on drawn content) and asserts that nothing happened:
    /// the screen identifiers are exactly `screens` and the selected tab is the one from before the tap.
    private func assertNoAction(
        _ identifier: String, screens: [String], file: StaticString = #filePath, line: UInt = #line
    ) {
        let target = element(identifier).firstMatch
        XCTAssertTrue(target.waitForExistence(timeout: 10), identifier, file: file, line: line)
        XCTAssertEqual(screenIdentifiers(), screens.sorted(), "before \(identifier)", file: file, line: line)
        let tab = selectedTab()
        reveal(target)
        target.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
        settle()
        XCTAssertEqual(screenIdentifiers(), screens.sorted(), identifier, file: file, line: line)
        XCTAssertEqual(selectedTab(), tab, "tab changed by \(identifier)", file: file, line: line)
        XCTAssertTrue(element(identifier).exists, "\(identifier) must stay visible", file: file, line: line)
    }

    private func assertNoAction(
        _ identifiers: [String], screens: [String], file: StaticString = #filePath, line: UInt = #line
    ) {
        for identifier in identifiers { assertNoAction(identifier, screens: screens, file: file, line: line) }
    }

    /// Every element whose identifier starts with the prefix; at least `minimum` must exist.
    private func assertNoAction(
        prefix: String, minimum: Int, screens: [String], file: StaticString = #filePath, line: UInt = #line
    ) {
        let deadline = Date().addingTimeInterval(10)
        while identifiers(prefix: prefix).isEmpty, Date() < deadline { settle(0.25) }
        let found = identifiers(prefix: prefix)
        XCTAssertGreaterThanOrEqual(found.count, minimum, prefix, file: file, line: line)
        assertNoAction(found, screens: screens, file: file, line: line)
    }

    // MARK: Login (1)

    func testPlaceholdersLoginNoAction() {
        launch("Login")
        // "Vergessen?" has its own test (AppNavigationUITests.testPlaceholderLinkHasNoAction).
        assertNoAction(["ph.Login.create", "ph.Login.apple"], screens: ["screen.login"])
    }

    // MARK: Tab roots (3, 4, 6, 10, 15)

    func testPlaceholdersZeitplanNoAction() {
        launch("Zeitplan")
        // fixtures/baupilot.json:206 schedule.asks[1].target = null
        assertNoAction("schedule.ask.1", screens: ["screen.schedule"])
    }

    func testPlaceholdersProfilNoAction() {
        launch("Profil")
        // The profile card and the link rows of the groups "Bauvorhaben" (0) and "App" (2); the switches are real.
        assertNoAction(
            [
                "ph.Profil.card",
                "ph.Profil.row.0.0", "ph.Profil.row.0.1", "ph.Profil.row.0.2",
                "ph.Profil.row.2.0", "ph.Profil.row.2.1", "ph.Profil.row.2.2",
            ], screens: ["screen.profile"])
    }

    func testPlaceholdersDokumenteNoAction() {
        launch("Dokumente")
        // Hint card, "Nach Gewerk", the add button and the rows without a viewer (offer Hahn, invoice, contract, plan).
        assertNoAction(
            [
                "ph.Dokumente.hint", "ph.Dokumente.bytrade", "ph.Dokumente.add",
                "documents.row.doc-offer-hahn", "documents.row.doc-invoice-windows-2",
                "documents.row.doc-contract-electrical", "documents.row.doc-plan-floor-og",
            ], screens: ["screen.documents"])
    }

    func testPlaceholdersAbnGewerkeNoAction() {
        launch("AbnGewerke")
        // The Dachdecker row is covered by InspectionFlowUITests.testTradeRowsFollowFixtureRoutes.
        assertNoAction("ph.AbnGewerke.vorlagen", screens: ["screen.inspections"])
    }

    func testPlaceholdersAbnMaengelNoAction() {
        launch("AbnMaengel")
        assertNoAction(["ph.AbnMaengel.vorlagen", "ph.AbnMaengel.resolved"], screens: ["screen.inspections"])
        // The "Erinnern" / "Erledigt" pills of the open defect cards.
        assertNoAction(prefix: "ph.AbnMaengel.remind.", minimum: 1, screens: ["screen.inspections"])
        assertNoAction(prefix: "ph.AbnMaengel.resolve.", minimum: 1, screens: ["screen.inspections"])
    }

    // MARK: Pushed screens

    func testPlaceholdersGewerkNoAction() {
        launch("Gewerk")
        // The Lorenz offer is covered by TradeFlowUITests.testOfferRowOpensDocument; the Hahn offer has no
        // action (m3 D5).
        assertNoAction(
            ["trade.offer.offer-hahn", "ph.Gewerk.more", "ph.Gewerk.add"], screens: ["screen.trade.screed"])
    }

    func testPlaceholdersDokViewerNoAction() {
        launch("DokViewer")
        let screen = "screen.document.doc-offer-wagner"
        assertNoAction("ph.DokViewer.share", screens: [screen])
        assertNoAction(prefix: "ph.DokViewer.fact.", minimum: 3, screens: [screen])
    }

    func testPlaceholdersVergleichNoAction() {
        launch("Vergleich")
        // The Hahn column has no document; Wagner's opens the viewer (TradeFlowUITests).
        assertNoAction(
            ["compare.column.offer-hahn", "ph.Vergleich.remind", "ph.Vergleich.ask", "ph.Vergleich.award"],
            screens: ["screen.compare.screed"])
    }

    func testPlaceholdersMitteilungenNoAction() {
        launch("Mitteilungen")
        // The "Termin bestätigt" item is covered by TradeFlowUITests.testNotificationWithoutTargetDoesNothing.
        assertNoAction("ph.Mitteilungen.markall", screens: ["screen.notifications"])
    }

    func testPlaceholdersAbnElementeNoAction() {
        launch("AbnElemente")
        // fixtures/baupilot.json: elements F1, F3, F7, F9 have route = null.
        assertNoAction(
            [
                "ph.AbnElemente.guide", "ph.AbnElemente.more.0", "ph.AbnElemente.more.1",
                "inspection.element.F1", "inspection.element.F3", "inspection.element.F7",
                "inspection.element.F9",
            ], screens: ["screen.inspection.windows"])
    }

    func testPlaceholdersAbnPruefungNoAction() {
        launch("AbnPruefung")
        assertNoAction("ph.AbnPruefung.ok", screens: ["screen.inspection.windows.F8.step.3"])
    }

    func testPlaceholdersErdNoAction() {
        launch("Erd")
        assertNoAction("ph.Erd.pdf", screens: ["screen.inspection.earthworks"])
    }

    func testPlaceholdersZimmererNoAction() {
        launch("Zimmerer")
        assertNoAction("ph.Zimmerer.pdf", screens: ["screen.inspection.carpentry"])
    }

    // MARK: Null-route rows (M4-M6)

    /// Rohbau 0.0 / 0.1 / 1.1 / 2.0 are covered by EarlyTradesUITests.testShellLinks; 1.2, 1.3 and 2.1 are new.
    func testPlaceholdersRohbauRowsNoAction() {
        launch("Rohbau")
        assertNoAction(
            ["protocol.row.1.2", "protocol.row.1.3", "protocol.row.2.1"], screens: ["screen.inspection.shell"])
    }

    /// ElÜbersicht 1.1 / 2.0 / 2.1 are covered by EarlyTradesUITests.testElectricalLinks; 1.2 and "Alle 9" are new.
    func testPlaceholdersElUebersichtNoAction() {
        launch("ElUebersicht")
        assertNoAction(
            ["electrical.row.1.2", "ph.ElUebersicht.more.1"], screens: ["screen.inspection.electrical"])
    }

    /// SanÜbersicht 0.2 is covered by LaterTradesUITests.testPlumbingLinks; 1.0, 1.1 and 2.1 are new.
    func testPlaceholdersSanUebersichtNoAction() {
        launch("SanUebersicht")
        assertNoAction(
            ["overview.row.1.0", "overview.row.1.1", "overview.row.2.1"],
            screens: ["screen.inspection.plumbing"])
    }

    /// EsÜbersicht 0.3 is covered by LaterTradesUITests.testScreedLinks; 0.0 and 0.1 are new.
    func testPlaceholdersEsUebersichtNoAction() {
        launch("EsUebersicht")
        assertNoAction(["overview.row.0.0", "overview.row.0.1"], screens: ["screen.inspection.screed"])
    }

    func testPlaceholdersFlUebersichtNoAction() {
        launch("FlUebersicht")
        assertNoAction(
            [
                "overview.row.0.0", "overview.row.0.1", "overview.row.1.1", "overview.row.1.2",
                "overview.row.1.3", "overview.row.2.0",
            ], screens: ["screen.inspection.tiles"])
    }

    func testPlaceholdersMaUebersichtNoAction() {
        launch("MaUebersicht")
        assertNoAction(
            [
                "overview.row.0.1", "overview.row.0.2", "overview.row.1.0", "overview.row.1.1",
                "overview.row.1.2",
            ], screens: ["screen.inspection.painting"])
    }

    // MARK: Buttons without action ("Ohne Aktion" in M4-M6)

    func testPlaceholdersRohbauMasseNoAction() {
        launch("RohbauMasse")
        assertNoAction(["measure.photo", "measure.save"], screens: ["screen.inspection.shell.measure"])
    }

    func testPlaceholdersElVerteilerNoAction() {
        launch("ElVerteiler")
        assertNoAction("step.ok", screens: ["screen.inspection.electrical.panel"])
    }

    func testPlaceholdersElRauchmelderNoAction() {
        launch("ElRauchmelder")
        assertNoAction("step.ok", screens: ["screen.inspection.electrical.smoke"])
    }

    func testPlaceholdersElRaumNoAction() {
        launch("ElRaum")
        assertNoAction("step.ok", screens: ["screen.inspection.electrical.room.kitchen"])
    }

    func testPlaceholdersSanGefaelleNoAction() {
        launch("SanGefaelle")
        let screen = "screen.inspection.plumbing.slope.A3"
        assertNoAction(["slope.photo", "slope.save"], screens: [screen])
        assertNoAction(prefix: "slope.section.", minimum: 4, screens: [screen])
    }

    func testPlaceholdersSanDruckNoAction() {
        launch("SanDruck")
        assertNoAction("pressure.photo", screens: ["screen.inspection.plumbing.pressure"])
    }

    func testPlaceholdersSanBadNoAction() {
        launch("SanBad")
        let screen = "screen.inspection.plumbing.room.bathUpper"
        assertNoAction(prefix: "object.tab.", minimum: 4, screens: [screen])
        assertNoAction(prefix: "object.check.", minimum: 2, screens: [screen])
    }

    func testPlaceholdersEsEbenheitNoAction() {
        launch("EsEbenheit")
        assertNoAction(["flatness.photo", "flatness.save"], screens: ["screen.inspection.screed.flatness"])
    }

    func testPlaceholdersEsBelegreifeNoAction() {
        launch("EsBelegreife")
        assertNoAction("readiness.action", screens: ["screen.inspection.screed.readiness"])
    }

    func testPlaceholdersFlBadNoAction() {
        launch("FlBad")
        // A tile is covered by LaterTradesUITests.testTilesAndPaintingLinks; "Keine Hohlstellen" is new.
        assertNoAction("tiles.none", screens: ["screen.inspection.tiles.room.bath"])
    }

    func testPlaceholdersMaRaumNoAction() {
        launch("MaRaum")
        let screen = "screen.inspection.painting.room.living"
        assertNoAction(["paint.photo", "step.ok"], screens: [screen])
        assertNoAction(prefix: "paint.wall.", minimum: 4, screens: [screen])
    }
}
