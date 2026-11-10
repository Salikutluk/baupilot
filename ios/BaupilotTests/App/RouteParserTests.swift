import XCTest
@testable import Baupilot

final class RouteParserTests: XCTestCase {
    private let field = "schedule.asks.target"

    func testParsesTabsTradeCompareDocumentNotifications() throws {
        for tab in AppTab.allCases {
            XCTAssertEqual(try RouteParser.target(tab.rawValue, field: field), .tab(tab))
        }
        XCTAssertEqual(try RouteParser.target("notifications", field: field), .push(.notifications))
        XCTAssertEqual(try RouteParser.target("trade/screed", field: field), .push(.trade(id: "screed")))
        XCTAssertEqual(
            try RouteParser.target("trade/screed/compare", field: field), .push(.compare(tradeId: "screed")))
        XCTAssertEqual(try RouteParser.target("document/d-1_a", field: field), .push(.document(id: "d-1_a")))
    }

    func testDefectsIsDefectsTarget() throws {
        XCTAssertEqual(try RouteParser.target("defects", field: field), .defects)
    }

    func testParsesInspectionRoutes() throws {
        XCTAssertEqual(
            try RouteParser.target("inspection/windows", field: field), .push(.inspection(tradeId: "windows")))
        XCTAssertEqual(
            try RouteParser.target("inspection/windows/F8/step/3", field: field),
            .push(.inspectionStep(tradeId: "windows", elementId: "F8", step: 3)))
        XCTAssertEqual(
            try RouteParser.target("inspection/windows/F8/step/12", field: field),
            .push(.inspectionStep(tradeId: "windows", elementId: "F8", step: 12)))
        XCTAssertEqual(
            try RouteParser.target("inspection/windows/sign", field: field),
            .push(.signOff(inspectionId: "windows")))
        XCTAssertEqual(try RouteParser.target("defect/new", field: field), .push(.newDefect))
    }

    func testRejectsMalformedInspectionRoutes() {
        let malformed = [
            "inspection/", "inspection/a/b/step", "inspection/a/b/sign", "inspection/a/b/c/d", "inspection/a/room",
            "inspection/a/F8/step", "inspection/a/F8/step/0",
            "inspection/a/F8/step/03", "inspection/a/F8/step/x", "inspection/a/F8/steps/3",
            "defect/old", "defect/new/x", "inspection/a/sign/x",
        ]
        for raw in malformed {
            XCTAssertThrowsError(try RouteParser.target(raw, field: field), raw) { error in
                XCTAssertEqual(error as? RepositoryError, .decoding(field), raw)
            }
        }
    }

    func testParsesCheckAndRoomRoutes() throws {
        XCTAssertEqual(
            try RouteParser.target("inspection/shell/measure", field: field),
            .push(.inspectionCheck(tradeId: "shell", check: "measure")))
        XCTAssertEqual(
            try RouteParser.target("inspection/electrical/panel", field: field),
            .push(.inspectionCheck(tradeId: "electrical", check: "panel")))
        XCTAssertEqual(
            try RouteParser.target("inspection/electrical/smoke", field: field),
            .push(.inspectionCheck(tradeId: "electrical", check: "smoke")))
        XCTAssertEqual(
            try RouteParser.target("inspection/electrical/room/kitchen", field: field),
            .push(.inspectionRoom(tradeId: "electrical", roomId: "kitchen")))
        XCTAssertEqual(
            try RouteParser.target("inspection/windows/sign", field: field),
            .push(.signOff(inspectionId: "windows")))
    }

    func testRejectsMalformedCheckRoutes() {
        let malformed = [
            "inspection/a/step", "inspection/a/room", "inspection/a/room/", "inspection/a/room/x/y",
            "inspection/a/b/step", "inspection/a/b/sign", "inspection/a/b/c/d", "inspection/a/-x", "inspection/a/room/-x", "inspection//measure",
        ]
        for raw in malformed {
            XCTAssertThrowsError(try RouteParser.target(raw, field: field), raw) { error in
                XCTAssertEqual(error as? RepositoryError, .decoding(field), raw)
            }
        }
    }

    func testParsesCheckItemRoute() throws {
        XCTAssertEqual(
            try RouteParser.target("inspection/plumbing/slope/A3", field: field),
            .push(.inspectionCheckItem(tradeId: "plumbing", check: "slope", item: "A3")))
        XCTAssertEqual(
            try RouteParser.target("inspection/a/b/c", field: field),
            .push(.inspectionCheckItem(tradeId: "a", check: "b", item: "c")))
        XCTAssertEqual(
            try RouteParser.route("inspection/plumbing/slope/A3", field: field),
            .inspectionCheckItem(tradeId: "plumbing", check: "slope", item: "A3"))
        XCTAssertEqual(
            try RouteParser.inspectionLink("inspection/plumbing/slope/A3", field: field),
            .push(.inspectionCheckItem(tradeId: "plumbing", check: "slope", item: "A3")))
        // `room` keeps priority over the item rule.
        XCTAssertEqual(
            try RouteParser.target("inspection/plumbing/room/bath", field: field),
            .push(.inspectionRoom(tradeId: "plumbing", roomId: "bath")))
    }

    func testRejectsMalformedCheckItemRoutes() {
        let malformed = [
            "inspection/a/b/step", "inspection/a/b/sign", "inspection/a/b/room", "inspection/a/b/-x",
            "inspection/a/b/c/d", "inspection/a//c", "inspection/a/F8/step", "inspection/a/sign/c",
            "inspection/a/step/c",
        ]
        for raw in malformed {
            XCTAssertThrowsError(try RouteParser.target(raw, field: field), raw) { error in
                XCTAssertEqual(error as? RepositoryError, .decoding(field), raw)
            }
        }
    }

    func testInspectionLinkAcceptsDefectsAndPush() throws {
        XCTAssertEqual(try RouteParser.inspectionLink("defects", field: field), .defects)
        XCTAssertEqual(
            try RouteParser.inspectionLink("inspection/shell/measure", field: field),
            .push(.inspectionCheck(tradeId: "shell", check: "measure")))
        XCTAssertEqual(
            try RouteParser.inspectionLink("inspection/windows", field: field),
            .push(.inspection(tradeId: "windows")))
    }

    func testInspectionLinkRejectsTab() {
        for raw in ["home", "inspections", "foo"] {
            XCTAssertThrowsError(try RouteParser.inspectionLink(raw, field: field), raw) { error in
                XCTAssertEqual(error as? RepositoryError, .decoding(field), raw)
            }
        }
    }

    func testNilIsNil() throws {
        XCTAssertNil(try RouteParser.target(nil, field: field))
        XCTAssertNil(try RouteParser.route(nil, field: field))
    }

    func testRejectsMalformedTargets() {
        for raw in ["trade/", "trade/a/b", "trade/a/compare/x", "", "foo", "trade/a b", "document/", "-x"] {
            XCTAssertThrowsError(try RouteParser.target(raw, field: field), raw) { error in
                XCTAssertEqual(error as? RepositoryError, .decoding(field), raw)
            }
        }
    }

    func testRouteRejectsTabTargets() throws {
        XCTAssertThrowsError(try RouteParser.route("home", field: field)) { error in
            XCTAssertEqual(error as? RepositoryError, .decoding(field))
        }
        XCTAssertEqual(try RouteParser.route("trade/screed", field: field), .trade(id: "screed"))
        XCTAssertEqual(try RouteParser.route("defect/new", field: field), .newDefect)
    }

    func testRouteRejectsDefectsTarget() {
        XCTAssertThrowsError(try RouteParser.route("defects", field: field)) { error in
            XCTAssertEqual(error as? RepositoryError, .decoding(field))
        }
    }
}
