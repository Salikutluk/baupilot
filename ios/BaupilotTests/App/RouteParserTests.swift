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

    func testDefectsMapsToInspectionsTab() throws {
        XCTAssertEqual(try RouteParser.target("defects", field: field), .tab(.inspections))
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
        XCTAssertThrowsError(try RouteParser.route("defects", field: field))
        XCTAssertEqual(try RouteParser.route("trade/screed", field: field), .trade(id: "screed"))
    }
}
