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
}
