import XCTest
@testable import Baupilot

final class RouteTitlesTests: XCTestCase {
    private let titles = RouteTitles(
        tabLabels: [.home: "Übersicht", .documents: "Dokumente"],
        tradeNames: ["screed": "Estrich"],
        documentTitles: ["d1": "Angebot Estrich"],
        compareTitle: "Angebote vergleichen",
        notificationsTitle: "Mitteilungen")

    func testFirstEntryUsesTabLabel() {
        let path: [AppRoute] = [.trade(id: "screed")]
        XCTAssertEqual(titles.backLabel(path: path, index: 0, tab: .home), "Übersicht")
        XCTAssertEqual(titles.backLabel(path: path, index: 1, tab: .documents), "Dokumente")
        XCTAssertEqual(titles.backLabel(path: path, index: 5, tab: .documents), "Dokumente")
    }

    func testCompareFromGewerkShowsTradeName() {
        let path: [AppRoute] = [.trade(id: "screed"), .compare(tradeId: "screed")]
        XCTAssertEqual(titles.backLabel(path: path, index: 1, tab: .home), "Estrich")
        XCTAssertEqual(titles.title(path[1]), "Angebote vergleichen")
    }

    func testViewerFromDocumentsTabShowsDokumente() {
        let path: [AppRoute] = [.document(id: "d1")]
        XCTAssertEqual(titles.backLabel(path: path, index: 0, tab: .documents), "Dokumente")
        XCTAssertEqual(titles.title(path[0]), "Angebot Estrich")
    }

    func testTitleFallsBackToId() {
        XCTAssertEqual(titles.title(.trade(id: "unknown")), "unknown")
        XCTAssertEqual(titles.title(.document(id: "dx")), "dx")
        XCTAssertEqual(titles.title(.notifications), "Mitteilungen")
        XCTAssertEqual(titles.backLabel(path: [], index: 0, tab: .profile), "")
    }
}
