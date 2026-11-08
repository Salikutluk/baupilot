import XCTest

@MainActor
final class AppNavigationUITests: XCTestCase {
    private static let tabs = ["home", "schedule", "inspections", "documents", "profile"]

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

    func testLoginLeadsToOverview() {
        logIn()
    }

    func testTabBarSwitchesAllFiveTabs() {
        logIn()
        for tab in Self.tabs {
            let button = app.buttons["tab.\(tab)"]
            XCTAssertTrue(button.waitForExistence(timeout: 5), "tab.\(tab)")
            button.tap()
            XCTAssertTrue(element("screen.\(tab)").waitForExistence(timeout: 5), "screen.\(tab)")
        }
    }

    func testPushHidesTabBarAndBackReturnsToRoot() {
        logIn()
        let bell = app.buttons["Mitteilungen, 2 neu"]
        XCTAssertTrue(bell.waitForExistence(timeout: 5))
        bell.tap()
        XCTAssertTrue(element("screen.notifications").waitForExistence(timeout: 5))
        for tab in Self.tabs {
            XCTAssertFalse(element("tab.\(tab)").exists, "tab.\(tab) must be hidden on a pushed route")
        }
        let back = app.buttons["nav.back"]
        XCTAssertTrue(back.waitForExistence(timeout: 5))
        back.tap()
        XCTAssertTrue(element("screen.home").waitForExistence(timeout: 5))
        for tab in Self.tabs {
            XCTAssertTrue(element("tab.\(tab)").waitForExistence(timeout: 5), "tab.\(tab)")
        }
    }

    func testDocumentTileOpensDocumentsTab() {
        logIn()
        let tile = app.buttons.matching(NSPredicate(format: "label BEGINSWITH 'Angebote'")).firstMatch
        XCTAssertTrue(tile.waitForExistence(timeout: 5))
        if !tile.isHittable { app.swipeUp() }
        tile.tap()
        XCTAssertTrue(element("screen.documents").waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons["tab.documents"].isSelected)
    }

    func testProfileSwitchSurvivesTabSwitch() {
        logIn()
        app.buttons["tab.profile"].tap()
        XCTAssertTrue(element("screen.profile").waitForExistence(timeout: 5))
        let toggle = app.switches["Wochenrückblick"]
        XCTAssertTrue(toggle.waitForExistence(timeout: 5))
        if !toggle.isHittable { app.swipeUp() }
        XCTAssertEqual(toggle.value as? String, "0")
        toggle.tap()
        XCTAssertEqual(toggle.value as? String, "1")
        app.buttons["tab.home"].tap()
        XCTAssertTrue(element("screen.home").waitForExistence(timeout: 5))
        app.buttons["tab.profile"].tap()
        XCTAssertTrue(element("screen.profile").waitForExistence(timeout: 5))
        let again = app.switches["Wochenrückblick"]
        XCTAssertTrue(again.waitForExistence(timeout: 5))
        XCTAssertEqual(again.value as? String, "1")
    }

    func testOnlySelectedTabIsInAccessibilityTree() {
        logIn()
        XCTAssertEqual(screenIdentifiers(), ["screen.home"])
        app.buttons["tab.profile"].tap()
        XCTAssertTrue(element("screen.profile").waitForExistence(timeout: 5))
        XCTAssertEqual(screenIdentifiers(), ["screen.profile"])
    }

    func testPlaceholderLinkHasNoAction() {
        XCTAssertTrue(element("screen.login").waitForExistence(timeout: 10))
        let link = app.descendants(matching: .any).matching(NSPredicate(format: "label == 'Vergessen?'")).firstMatch
        XCTAssertTrue(link.waitForExistence(timeout: 5))
        link.tap()
        XCTAssertTrue(element("screen.login").exists)
        XCTAssertFalse(element("screen.home").exists)
    }
}
