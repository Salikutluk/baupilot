import XCTest

/// M5 early trades (section 9 UI): the cross-screen links and gestures of the protocol, measure and electrical
/// screens, driven through the real shell.
@MainActor
final class EarlyTradesUITests: XCTestCase {
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

    private func expectScreen(_ identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        XCTAssertTrue(element(identifier).waitForExistence(timeout: 5), identifier, file: file, line: line)
    }

    private func back(to identifier: String, file: StaticString = #filePath, line: UInt = #line) {
        tap("nav.back")
        expectScreen(identifier, file: file, line: line)
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

    private func openInspections() {
        logIn()
        tap("tab.inspections")
        expectScreen("screen.inspections")
    }

    private func openRohbau() {
        openInspections()
        openTrade("shell", screen: "screen.inspection.shell")
    }

    private func openElectrical() {
        openInspections()
        openTrade("electrical", screen: "screen.inspection.electrical")
    }

    private func openMeasure() {
        openRohbau()
        tap("protocol.row.1.0")
        expectScreen("screen.inspection.shell.measure")
    }

    private func openSmoke() {
        openElectrical()
        tap("electrical.row.0.1")
        expectScreen("screen.inspection.electrical.smoke")
    }

    func testShellMeasureStepperUpdatesResultLive() {
        openMeasure()
        let diff = element("measure.height.diff")
        let result = element("measure.result")
        XCTAssertTrue(diff.waitForExistence(timeout: 5))
        tap("measure.height.increment")
        XCTAssertEqual(diff.label, "+15 mm")
        XCTAssertTrue(result.label.contains("Außerhalb der Toleranz – Mangel erfassen"), result.label)
        tap("measure.height.decrement")
        XCTAssertEqual(diff.label, "+10 mm")
        XCTAssertTrue(result.label.contains("Innerhalb der Toleranz"), result.label)
    }

    func testShellLinks() {
        openRohbau()
        tap("protocol.banner")
        expectScreen("inspections.view.defects")
        XCTAssertFalse(element("screen.inspection.shell").exists)
        tap("inspections.segment.trades")
        openTrade("shell", screen: "screen.inspection.shell")
        tap("protocol.row.0.2")
        expectScreen("inspections.view.defects")
        XCTAssertFalse(element("screen.inspection.shell").exists)
        tap("inspections.segment.trades")
        openTrade("shell", screen: "screen.inspection.shell")
        // Rows without a target do nothing.
        for row in ["protocol.row.0.0", "protocol.row.0.1", "protocol.row.1.1", "protocol.row.2.0"] {
            tap(row)
            expectScreen("screen.inspection.shell")
            XCTAssertFalse(element("screen.defect.new").exists, row)
            XCTAssertFalse(element("inspections.view.defects").exists, row)
        }
    }

    func testElectricalLinks() {
        openElectrical()
        let targets: [(row: String, screen: String)] = [
            ("electrical.row.0.0", "screen.inspection.electrical.panel"),
            ("electrical.row.0.1", "screen.inspection.electrical.smoke"),
            ("electrical.row.1.0", "screen.inspection.electrical.room.kitchen"),
        ]
        for target in targets {
            tap(target.row)
            expectScreen(target.screen)
            back(to: "screen.inspection.electrical")
        }
        // "Wohnen / Essen" and the documents have no target.
        for row in ["electrical.row.1.1", "electrical.row.2.0", "electrical.row.2.1"] {
            tap(row)
            expectScreen("screen.inspection.electrical")
            XCTAssertFalse(element("screen.inspection.electrical.room.kitchen").exists, row)
        }
    }

    func testDefectFromM5ScreensReturnsToOpener() {
        openMeasure()
        tap("measure.defect")
        expectScreen("screen.defect.new")
        tap("defect.save")
        expectScreen("screen.inspection.shell.measure")

        back(to: "screen.inspection.shell")
        back(to: "screen.inspections")
        openTrade("electrical", screen: "screen.inspection.electrical")
        tap("electrical.row.1.0")
        expectScreen("screen.inspection.electrical.room.kitchen")
        tap("step.defect")
        expectScreen("screen.defect.new")
        tap("defect.cancel")
        expectScreen("screen.inspection.electrical.room.kitchen")
    }

    func testSmokeReminderToggles() {
        openSmoke()
        let reminder = app.switches["smoke.reminder"]
        XCTAssertTrue(reminder.waitForExistence(timeout: 5))
        XCTAssertEqual(reminder.value as? String, "1")
        reminder.tap()
        XCTAssertEqual(reminder.value as? String, "0")
        reminder.tap()
        XCTAssertEqual(reminder.value as? String, "1")
        // Nothing is saved (I6): the switch starts on again.
        reminder.tap()
        XCTAssertEqual(reminder.value as? String, "0")
        back(to: "screen.inspection.electrical")
        tap("electrical.row.0.1")
        expectScreen("screen.inspection.electrical.smoke")
        XCTAssertEqual(app.switches["smoke.reminder"].value as? String, "1")
    }
}
