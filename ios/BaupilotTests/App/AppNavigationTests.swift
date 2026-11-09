import XCTest
@testable import Baupilot

final class AppNavigationTests: XCTestCase {
    private func loggedIn() -> AppNavigation {
        var nav = AppNavigation()
        nav.logIn()
        return nav
    }

    func testStartsLoggedOutOnHome() {
        let nav = AppNavigation()
        XCTAssertFalse(nav.isLoggedIn)
        XCTAssertEqual(nav.selectedTab, .home)
        XCTAssertTrue(nav.paths.isEmpty)
        for tab in AppTab.allCases { XCTAssertEqual(nav.path(for: tab), []) }
        XCTAssertTrue(nav.showsTabBar)
    }

    func testLogInSelectsHomeAndClearsStacks() {
        var nav = loggedIn()
        nav.select(.schedule)
        nav.push(.trade(id: "screed"))
        nav.logIn()
        XCTAssertTrue(nav.isLoggedIn)
        XCTAssertEqual(nav.selectedTab, .home)
        XCTAssertTrue(nav.paths.isEmpty)
        for tab in AppTab.allCases { XCTAssertEqual(nav.path(for: tab), []) }
    }

    func testSelectSwitchesWithoutTouchingStacks() {
        var nav = loggedIn()
        nav.push(.notifications)
        nav.select(.schedule)
        nav.push(.trade(id: "screed"))
        let before = nav.paths
        nav.select(.profile)
        XCTAssertEqual(nav.selectedTab, .profile)
        XCTAssertEqual(nav.paths, before)
        nav.select(.home)
        XCTAssertEqual(nav.paths, before)
        XCTAssertEqual(nav.path(for: .home), [.notifications])
        XCTAssertEqual(nav.path(for: .schedule), [.trade(id: "screed")])
    }

    func testPushOnlyAffectsSelectedTab() {
        var nav = loggedIn()
        nav.select(.schedule)
        nav.push(.trade(id: "screed"))
        nav.push(.notifications)
        XCTAssertEqual(nav.path(for: .schedule), [.trade(id: "screed"), .notifications])
        for tab in AppTab.allCases where tab != .schedule {
            XCTAssertEqual(nav.path(for: tab), [], "\(tab)")
        }
        nav.pop()
        XCTAssertEqual(nav.path(for: .schedule), [.trade(id: "screed")])
    }

    func testReselectPopsToRoot() {
        var nav = loggedIn()
        nav.push(.notifications)
        nav.select(.schedule)
        nav.push(.trade(id: "screed"))
        nav.select(.schedule)
        XCTAssertEqual(nav.selectedTab, .schedule)
        XCTAssertEqual(nav.path(for: .schedule), [])
        XCTAssertEqual(nav.path(for: .home), [.notifications])
    }

    func testPopOnEmptyIsNoOp() {
        var nav = loggedIn()
        let before = nav
        nav.pop()
        XCTAssertEqual(nav, before)
    }

    func testShowsTabBarOnlyAtRoot() {
        var nav = loggedIn()
        XCTAssertTrue(nav.showsTabBar)
        nav.push(.notifications)
        XCTAssertFalse(nav.showsTabBar)
        nav.select(.schedule)
        XCTAssertTrue(nav.showsTabBar)
        nav.select(.home)
        XCTAssertFalse(nav.showsTabBar)
        nav.pop()
        XCTAssertTrue(nav.showsTabBar)
    }

    func testPushWhileLoggedOutIsNoOp() {
        var nav = AppNavigation()
        let before = nav
        nav.push(.notifications)
        XCTAssertEqual(nav, before)
        nav.pop()
        XCTAssertEqual(nav, before)
    }

    func testSetPathReplacesOnlyThatTab() {
        var nav = loggedIn()
        nav.push(.notifications)
        nav.setPath([.trade(id: "tiles")], for: .schedule)
        XCTAssertEqual(nav.path(for: .schedule), [.trade(id: "tiles")])
        XCTAssertEqual(nav.path(for: .home), [.notifications])
        XCTAssertEqual(nav.selectedTab, .home)
        nav.setPath([], for: .schedule)
        XCTAssertEqual(nav.path(for: .schedule), [])
        XCTAssertEqual(nav.path(for: .home), [.notifications])

        var out = AppNavigation()
        let before = out
        out.setPath([.notifications], for: .home)
        XCTAssertEqual(out, before)
    }

    func testOpenAppendsNewRoute() {
        var nav = loggedIn()
        nav.open(.trade(id: "screed"))
        nav.open(.compare(tradeId: "screed"))
        XCTAssertEqual(nav.path(for: .home), [.trade(id: "screed"), .compare(tradeId: "screed")])
    }

    func testOpenExistingRoutePopsBackToIt() {
        var nav = loggedIn()
        nav.open(.trade(id: "screed"))
        nav.open(.compare(tradeId: "screed"))
        nav.open(.document(id: "d1"))
        nav.open(.trade(id: "screed"))
        XCTAssertEqual(nav.path(for: .home), [.trade(id: "screed")])
    }

    func testOpenNeverDuplicates() {
        var nav = loggedIn()
        nav.open(.trade(id: "screed"))
        nav.open(.compare(tradeId: "screed"))
        nav.open(.document(id: "d1"))
        nav.open(.compare(tradeId: "screed"))
        XCTAssertEqual(nav.path(for: .home), [.trade(id: "screed"), .compare(tradeId: "screed")])
    }

    func testOpenWhileLoggedOutIsNoOp() {
        var nav = AppNavigation()
        let before = nav
        nav.open(.notifications)
        XCTAssertEqual(nav, before)
    }

    func testGoTabSelectsWithoutTouchingStacks() {
        var nav = loggedIn()
        nav.open(.notifications)
        nav.go(.tab(.documents))
        XCTAssertEqual(nav.selectedTab, .documents)
        XCTAssertEqual(nav.path(for: .home), [.notifications])
        XCTAssertEqual(nav.path(for: .documents), [])
    }

    func testGoPushOpensOnSelectedTab() {
        var nav = loggedIn()
        nav.select(.documents)
        nav.go(.push(.document(id: "d1")))
        XCTAssertEqual(nav.path(for: .documents), [.document(id: "d1")])
        XCTAssertEqual(nav.path(for: .home), [])
    }

    // MARK: Inspections (M4)

    func testShowDefectsSelectsInspectionsRootWithDefectsView() {
        var nav = loggedIn()
        nav.showDefects()
        XCTAssertEqual(nav.selectedTab, .inspections)
        XCTAssertEqual(nav.inspectionsView, .defects)
        XCTAssertEqual(nav.path(for: .inspections), [])
        XCTAssertTrue(nav.showsTabBar)
    }

    func testShowDefectsClearsOnlyInspectionsStack() {
        var nav = loggedIn()
        nav.open(.notifications)
        nav.select(.inspections)
        nav.open(.inspection(tradeId: "windows"))
        nav.open(.newDefect)
        nav.select(.schedule)
        nav.push(.trade(id: "screed"))
        nav.showDefects()
        XCTAssertEqual(nav.selectedTab, .inspections)
        XCTAssertEqual(nav.path(for: .inspections), [])
        XCTAssertEqual(nav.path(for: .home), [.notifications])
        XCTAssertEqual(nav.path(for: .schedule), [.trade(id: "screed")])
    }

    func testGoDefectsCallsShowDefects() {
        var viaGo = loggedIn()
        viaGo.open(.notifications)
        var direct = viaGo
        viaGo.go(.defects)
        direct.showDefects()
        XCTAssertEqual(viaGo, direct)
        XCTAssertEqual(viaGo.inspectionsView, .defects)
    }

    func testSelectInspectionsViewKeepsPathsAndTab() {
        var nav = loggedIn()
        nav.select(.inspections)
        nav.open(.inspection(tradeId: "windows"))
        let before = nav
        nav.selectInspectionsView(.defects)
        XCTAssertEqual(nav.inspectionsView, .defects)
        XCTAssertEqual(nav.paths, before.paths)
        XCTAssertEqual(nav.selectedTab, before.selectedTab)
        nav.selectInspectionsView(.trades)
        XCTAssertEqual(nav, before)

        nav.selectInspectionsView(.defects)
        nav.select(.inspections)
        XCTAssertEqual(nav.inspectionsView, .defects, "re-tap keeps the view")
        nav.select(.home)
        nav.select(.inspections)
        XCTAssertEqual(nav.inspectionsView, .defects, "tab round trip keeps the view")
    }

    func testLogInResetsInspectionsView() {
        var nav = loggedIn()
        nav.showDefects()
        nav.logIn()
        XCTAssertEqual(nav.inspectionsView, .trades)
        XCTAssertEqual(nav.selectedTab, .home)
    }

    func testOpenAboveBuildsAnchorThenRoute() {
        var nav = loggedIn()
        nav.select(.inspections)
        nav.open(.inspection(tradeId: "windows"))
        nav.open(.inspectionStep(tradeId: "windows", elementId: "F8", step: 3))
        nav.open(.newDefect)
        nav.open(.signOff(inspectionId: "windows"), above: .inspection(tradeId: "windows"))
        XCTAssertEqual(
            nav.path(for: .inspections),
            [.inspection(tradeId: "windows"), .signOff(inspectionId: "windows")])

        var fresh = loggedIn()
        fresh.select(.inspections)
        fresh.open(.signOff(inspectionId: "windows"), above: .inspection(tradeId: "windows"))
        XCTAssertEqual(
            fresh.path(for: .inspections),
            [.inspection(tradeId: "windows"), .signOff(inspectionId: "windows")])
    }

    func testOpenAboveNeverDuplicates() {
        var nav = loggedIn()
        nav.select(.inspections)
        let anchor = AppRoute.inspection(tradeId: "windows")
        let route = AppRoute.signOff(inspectionId: "windows")
        nav.open(route, above: anchor)
        nav.open(route, above: anchor)
        XCTAssertEqual(nav.path(for: .inspections), [anchor, route])
    }

    func testNewOpsAreNoOpsWhenLoggedOut() {
        var nav = AppNavigation()
        let before = nav
        nav.showDefects()
        XCTAssertEqual(nav, before)
        nav.selectInspectionsView(.defects)
        XCTAssertEqual(nav, before)
        nav.open(.newDefect, above: .inspection(tradeId: "windows"))
        XCTAssertEqual(nav, before)
        nav.go(.defects)
        XCTAssertEqual(nav, before)
    }
}
