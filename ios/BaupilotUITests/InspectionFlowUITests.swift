import XCTest

/// Abnahme-Kernflow (M4): the section 9 UI suite, AC2 included.
@MainActor
final class InspectionFlowUITests: XCTestCase {
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
        let target = element(identifier)
        XCTAssertTrue(target.waitForExistence(timeout: 5), identifier, file: file, line: line)
        if !(target.exists && app.frame.contains(target.frame)) { app.swipeUp() }
        target.tap()
    }

    private func expectScreen(_ identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        XCTAssertTrue(element(identifier).waitForExistence(timeout: 5), identifier, file: file, line: line)
    }

    private func back(to identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        tap("nav.back")
        expectScreen(identifier, file: file, line: line)
    }

    private func openWindows() {
        tap("tab.inspections")
        expectScreen("screen.inspections")
        tap("inspections.trade.windows")
        expectScreen("screen.inspection.windows")
    }

    private func openDefectFormFromStep() {
        openWindows()
        tap("inspection.element.F8")
        expectScreen("screen.inspection.windows.F8.step.3")
        tap("step.defect")
        expectScreen("screen.defect.new")
    }

    /// AC2: Elemente → Prüfschritt → Mangel → Unterschrift → Offene Mängel.
    func testWindowAcceptanceFlowEndsOnOpenDefects() {
        logIn()
        openDefectFormFromStep()
        tap("defect.severity.2")
        tap("defect.deadline.0")
        tap("defect.save")
        expectScreen("screen.inspection.windows.sign")
        let owner = element("sign.owner")
        XCTAssertTrue(owner.waitForExistence(timeout: 5))
        let placeholder = app.staticTexts["Hier unterschreiben"]
        XCTAssertTrue(placeholder.exists)
        XCTAssertEqual(owner.value as? String, "strokes: 0")
        let start = owner.coordinate(withNormalizedOffset: CGVector(dx: 0.2, dy: 0.5))
        let end = owner.coordinate(withNormalizedOffset: CGVector(dx: 0.8, dy: 0.6))
        start.press(forDuration: 0.1, thenDragTo: end)
        XCTAssertEqual(owner.value as? String, "strokes: 1")
        XCTAssertFalse(placeholder.exists)
        tap("sign.finish")
        expectScreen("screen.inspections")
        expectScreen("inspections.view.defects")
        let card = element("defect.defect-window-f2")
        XCTAssertTrue(card.waitForExistence(timeout: 5))
        XCTAssertTrue(card.label.contains("Frist 15.01.2027 · 2 Fotos"))
        XCTAssertTrue(app.buttons["tab.inspections"].exists)
    }

    func testSegmentsSwitchViews() {
        logIn()
        tap("tab.inspections")
        expectScreen("inspections.view.trades")
        tap("inspections.segment.defects")
        expectScreen("inspections.view.defects")
        XCTAssertFalse(element("inspections.view.trades").exists)
        tap("inspections.segment.trades")
        expectScreen("inspections.view.trades")
        tap("inspections.segment.defects")
        expectScreen("inspections.view.defects")
        tap("inspections.segment.trades")
        tap("inspections.trade.windows")
        expectScreen("screen.inspection.windows")
        back(to: "screen.inspections")
        expectScreen("inspections.view.trades")
    }

    func testTradeRowsFollowFixtureRoutes() {
        logIn()
        tap("tab.inspections")
        expectScreen("screen.inspections")
        let earthworks = element("inspections.trade.earthworks")
        XCTAssertTrue(earthworks.waitForExistence(timeout: 5))
        // A plain-style row only hits on drawn content and this row's subtitle wraps narrow, so tap on the title.
        earthworks.coordinate(withNormalizedOffset: CGVector(dx: 0.1, dy: 0.2)).tap()
        expectScreen("screen.inspection.earthworks")
        XCTAssertTrue(element("protocol.banner").waitForExistence(timeout: 5))
        back(to: "screen.inspections")
        let plumbing = element("inspections.trade.plumbing")
        XCTAssertTrue(plumbing.waitForExistence(timeout: 5))
        if !app.frame.contains(plumbing.frame) { app.swipeUp() }
        plumbing.coordinate(withNormalizedOffset: CGVector(dx: 0.1, dy: 0.2)).tap()
        expectScreen("screen.inspection.plumbing")
        XCTAssertFalse(element("protocol.banner").exists)
        back(to: "screen.inspections")
        tap("inspections.trade.roofing")
        expectScreen("screen.inspections")
        XCTAssertFalse(element("screen.inspection.roofing").exists)
    }

    func testCancelDefectReturnsToStep() {
        logIn()
        openDefectFormFromStep()
        tap("defect.cancel")
        expectScreen("screen.inspection.windows.F8.step.3")
    }

    func testCancelDefectFromF2ReturnsToElements() {
        logIn()
        openWindows()
        tap("inspection.element.F2")
        expectScreen("screen.defect.new")
        tap("defect.cancel")
        expectScreen("screen.inspection.windows")
    }

    func testDefectFormControls() {
        logIn()
        openDefectFormFromStep()
        tap("defect.severity.2")
        XCTAssertTrue(element("defect.severity.2").isSelected)
        tap("defect.deadline.0")
        XCTAssertEqual(element("defect.deadline.value").label, "Fr, 01.01.2027")
        let notify = element("defect.notify")
        let before = notify.value as? String
        tap("defect.notify")
        XCTAssertNotEqual(notify.value as? String, before)
        let value = element("defect.deadline.value").label
        tap("defect.deadline.2")
        XCTAssertEqual(element("defect.deadline.value").label, value)
    }

    func testSignOffBackReturnsToElements() {
        logIn()
        openDefectFormFromStep()
        tap("defect.save")
        expectScreen("screen.inspection.windows.sign")
        back(to: "screen.inspection.windows")
    }

    func testElementsNextOpensStep() {
        logIn()
        openWindows()
        tap("inspection.next")
        expectScreen("screen.inspection.windows.F8.step.3")
    }

    func testElementF2OpensDefectForm() {
        logIn()
        openWindows()
        tap("inspection.element.F2")
        expectScreen("screen.defect.new")
    }
}
