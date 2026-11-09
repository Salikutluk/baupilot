import XCTest

@MainActor
final class TradeFlowUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUp() async throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    private func element(_ identifier: String) -> XCUIElement {
        app.descendants(matching: .any)[identifier]
    }

    private func screenIdentifiers() -> [String] {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "identifier BEGINSWITH 'screen.'"))
            .allElementsBoundByIndex
            .map(\.identifier)
    }

    private func logIn() {
        XCTAssertTrue(element("screen.login").waitForExistence(timeout: 10))
        let submit = app.buttons["login.submit"]
        XCTAssertTrue(submit.waitForExistence(timeout: 5))
        submit.tap()
        XCTAssertTrue(element("screen.home").waitForExistence(timeout: 10))
    }

    /// Taps the element once it exists; scrolls up first when it sits below the fold.
    private func tap(_ identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        let target = element(identifier)
        XCTAssertTrue(target.waitForExistence(timeout: 5), identifier, file: file, line: line)
        if !isOnScreen(target) { app.swipeUp() }
        target.tap()
    }

    /// `isHittable` throws for an element beyond the screen edge, so compare its frame with the screen instead.
    private func isOnScreen(_ target: XCUIElement) -> Bool {
        target.exists && app.frame.contains(target.frame)
    }

    private func expectScreen(_ identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        XCTAssertTrue(element(identifier).waitForExistence(timeout: 5), identifier, file: file, line: line)
    }

    private func back(to identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        tap("nav.back")
        expectScreen(identifier, file: file, line: line)
    }

    private func openEstrich() {
        let row = app.buttons.matching(NSPredicate(format: "label CONTAINS 'Estrich'")).firstMatch
        XCTAssertTrue(row.waitForExistence(timeout: 5))
        if !row.isHittable { app.swipeUp() }
        row.tap()
        expectScreen("screen.trade.screed")
    }

    private func openBell() {
        let bell = app.buttons["Mitteilungen, 2 neu"]
        XCTAssertTrue(bell.waitForExistence(timeout: 5))
        bell.tap()
        expectScreen("screen.notifications")
    }

    func testGewerkCompareDocumentAndBack() {
        logIn()
        openEstrich()
        tap("trade.compare")
        expectScreen("screen.compare.screed")
        XCTAssertTrue(element("compare.column.offer-hahn").waitForExistence(timeout: 5))
        tap("compare.column.offer-hahn")
        XCTAssertEqual(screenIdentifiers(), ["screen.compare.screed"])
        tap("compare.column.offer-wagner")
        expectScreen("screen.document.doc-offer-wagner")
        back(to: "screen.compare.screed")
        back(to: "screen.trade.screed")
        back(to: "screen.home")
        XCTAssertTrue(app.buttons["tab.home"].waitForExistence(timeout: 5))
    }

    func testOfferRowOpensDocument() {
        logIn()
        openEstrich()
        tap("trade.offer.offer-wagner")
        expectScreen("screen.document.doc-offer-wagner")
        back(to: "screen.trade.screed")
        tap("trade.offer.offer-lorenz")
        expectScreen("screen.trade.screed")
        XCTAssertEqual(screenIdentifiers(), ["screen.trade.screed"])
    }

    func testDocViewerCompareReturnsToExistingCompare() {
        logIn()
        openEstrich()
        tap("trade.compare")
        expectScreen("screen.compare.screed")
        tap("compare.column.offer-wagner")
        expectScreen("screen.document.doc-offer-wagner")
        tap("document.compare")
        expectScreen("screen.compare.screed")
        back(to: "screen.trade.screed")
    }

    func testBellOpensNotificationsAndCompareTarget() {
        logIn()
        openBell()
        tap("notification.notif-offer-hahn")
        expectScreen("screen.compare.screed")
        back(to: "screen.notifications")
    }

    func testNotificationOpensTrade() {
        logIn()
        openBell()
        tap("notification.notif-offer-lorenz-missing")
        expectScreen("screen.trade.screed")
    }

    func testNotificationSwitchesToDocumentsTab() {
        logIn()
        openBell()
        tap("notification.notif-invoice-due")
        expectScreen("screen.documents")
        XCTAssertTrue(app.buttons["tab.documents"].waitForExistence(timeout: 5))
    }

    func testNotificationDefectsOpensInspectionsTab() {
        logIn()
        openBell()
        tap("notification.notif-defect-overdue")
        expectScreen("screen.inspections")
        expectScreen("inspections.view.defects")
    }

    func testNotificationWithoutTargetDoesNothing() {
        logIn()
        openBell()
        tap("notification.notif-electrical-confirmed")
        expectScreen("screen.notifications")
        XCTAssertEqual(screenIdentifiers(), ["screen.notifications"])
    }

    func testDocumentsRowOpensViewerAndBack() {
        logIn()
        tap("tab.documents")
        expectScreen("screen.documents")
        tap("documents.row.doc-offer-wagner")
        expectScreen("screen.document.doc-offer-wagner")
        XCTAssertTrue(app.buttons["nav.back"].label.contains("Dokumente"))
        back(to: "screen.documents")
    }

    func testDocumentsChipsScrollAndFilter() {
        logIn()
        tap("tab.documents")
        expectScreen("screen.documents")
        let chip = element("documents.filter.plan")
        var swipes = 0
        while !isOnScreen(chip) && swipes < 4 {
            element("documents.filter.all").swipeLeft()
            swipes += 1
        }
        tap("documents.filter.plan")
        expectScreen("documents.row.doc-plan-floor-og")
        XCTAssertFalse(element("documents.row.doc-offer-wagner").exists)
        let all = element("documents.filter.all")
        swipes = 0
        while !isOnScreen(all) && swipes < 4 {
            chip.swipeRight()
            swipes += 1
        }
        tap("documents.filter.all")
        expectScreen("documents.row.doc-plan-floor-og")
        expectScreen("documents.row.doc-offer-wagner")
    }

    func testDocumentsFilterSurvivesTabSwitch() {
        logIn()
        tap("tab.documents")
        expectScreen("screen.documents")
        tap("documents.filter.offer")
        expectScreen("documents.row.doc-offer-wagner")
        XCTAssertFalse(element("documents.row.doc-plan-floor-og").exists)
        tap("tab.home")
        expectScreen("screen.home")
        tap("tab.documents")
        expectScreen("screen.documents")
        expectScreen("documents.row.doc-offer-wagner")
        XCTAssertFalse(element("documents.row.doc-plan-floor-og").exists)
    }
}
