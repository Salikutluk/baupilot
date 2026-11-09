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

    func testInspectionTitlesAndFallbacks() {
        let step = AppRoute.inspectionStep(tradeId: "windows", elementId: "F8", step: 3)
        var full = titles
        full.inspectionTitles = ["windows": "Fenster & Türen"]
        full.stepTitles = [step: "F8 · Schlafen"]
        full.newDefectTitle = "Mangel erfassen"
        full.signOffTitles = ["windows": "Abnahme abschließen"]
        XCTAssertEqual(full.title(.inspection(tradeId: "windows")), "Fenster & Türen")
        XCTAssertEqual(full.title(step), "F8 · Schlafen")
        XCTAssertEqual(full.title(.newDefect), "Mangel erfassen")
        XCTAssertEqual(full.title(.signOff(inspectionId: "windows")), "Abnahme abschließen")

        XCTAssertEqual(titles.title(.inspection(tradeId: "roofing")), "roofing")
        XCTAssertEqual(titles.title(.inspectionStep(tradeId: "windows", elementId: "F9", step: 1)), "F9")
        XCTAssertEqual(titles.title(.signOff(inspectionId: "windows")), "windows")
    }

    func testBackLabelsOnInspectionPaths() {
        let step = AppRoute.inspectionStep(tradeId: "windows", elementId: "F8", step: 3)
        var full = RouteTitles(
            tabLabels: [.inspections: "Abnahmen"], tradeNames: [:], documentTitles: [:],
            compareTitle: "Angebote vergleichen", notificationsTitle: "Mitteilungen")
        full.inspectionTitles = ["windows": "Fenster & Türen"]
        let inspection = AppRoute.inspection(tradeId: "windows")
        XCTAssertEqual(full.backLabel(path: [inspection], index: 0, tab: .inspections), "Abnahmen")
        XCTAssertEqual(full.backLabel(path: [inspection, step], index: 1, tab: .inspections), "Fenster & Türen")
        XCTAssertEqual(
            full.backLabel(path: [inspection, .signOff(inspectionId: "windows")], index: 1, tab: .inspections),
            "Fenster & Türen")
    }
}
