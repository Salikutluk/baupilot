import XCTest
@testable import Baupilot

final class AppDataTests: XCTestCase {
    private func fixtureObject() throws -> [String: Any] {
        let url = try XCTUnwrap(Bundle.main.url(forResource: "baupilot", withExtension: "json"))
        let object = try JSONSerialization.jsonObject(with: Data(contentsOf: url))
        return try XCTUnwrap(object as? [String: Any])
    }

    private func repository(editing edit: (inout [String: Any]) -> Void) throws -> MockRepository {
        var object = try fixtureObject()
        edit(&object)
        return try MockRepository(data: JSONSerialization.data(withJSONObject: object))
    }

    private func loaded() async throws -> AppData {
        try await AppData.load(from: MockRepository.bundled())
    }

    func testLoadsFromBundledFixture() async throws {
        let data = try await loaded()
        XCTAssertEqual(data.login.title, "Baupilot")
        XCTAssertEqual(data.login.headline, ["Ihr Bauvorhaben.", "Ein Plan."])
        XCTAssertEqual(data.home.projectLabel, "Einfamilienhaus · Musterweg 12")
        XCTAssertEqual(data.home.notificationsAria, "Mitteilungen, 2 neu")
        XCTAssertEqual(data.schedule.moveInLabel, "Einzug geplant September 2027")
        XCTAssertEqual(data.tradeNames["screed"], "Estrich")
        XCTAssertEqual(data.tradeNames["windows"], "Fenster & Türen")
        XCTAssertEqual(data.home.tabs.map(\.tab), AppTab.allCases)
    }

    func testHomePhaseTitleProgressAndLabels() async throws {
        let home = try await loaded().home
        XCTAssertEqual(home.phaseTitle, "Fenster & Türen")
        XCTAssertEqual(home.progress, 0.45, accuracy: 0.0001)
        XCTAssertEqual(home.phaseLabel, "Phase 5 von 11")
        XCTAssertEqual(home.constructionLabel, "Baubeginn März 2026")
        XCTAssertEqual(home.moveInLabel, "Einzug geplant Sep 2027")
    }

    func testHomeHasThreeTradesWithKWChips() async throws {
        let trades = try await loaded().home.trades
        XCTAssertEqual(trades.map(\.kw), ["KW 3", "KW 5", "KW 8"])
        XCTAssertEqual(trades.map(\.tradeId), ["electrical", "plumbing", "screed"])
        XCTAssertEqual(trades.map(\.name), ["Elektriker", "Installateur", "Estrich"])
        XCTAssertEqual(trades.map(\.statusLabel), ["Beauftragt", "Vergleichen", "Anfragen"])
        XCTAssertEqual(trades.map(\.when), ["Rohinstallation", "2 Angebote da", "Angebote bis 15.01."])
    }

    func testHomeDocTiles() async throws {
        let docs = try await loaded().home.docs
        XCTAssertEqual(docs.map(\.count), ["7", "3", "4", "12"])
        XCTAssertEqual(docs.map(\.label), ["Angebote", "Verträge", "Rechnungen", "Pläne"])
        XCTAssertEqual(docs.map(\.icon), [.document, .contract, .receipt, .map])
    }

    func testScheduleAskTargets() async throws {
        let schedule = try await loaded().schedule
        XCTAssertEqual(schedule.asks.map(\.target), [.trade(id: "screed"), nil])
        XCTAssertEqual(schedule.asks.map(\.name), ["Estrich", "Fliesen & Maler"])
        let tradeCount = try await MockRepository.bundled().trades().count
        XCTAssertEqual(schedule.layout.rows.count, tradeCount)
        XCTAssertEqual(schedule.ariaLabel, "Zeitplan aller Gewerke von März 2026 bis September 2027, heute Mitte Dezember 2026")
    }

    func testProfileGroupsAndToggleStates() async throws {
        let profile = try await loaded().profile
        XCTAssertEqual(profile.initials, "MM")
        XCTAssertEqual(profile.name, "Max Mustermann")
        XCTAssertEqual(profile.contact, "max@beispiel.de · Bauherr")
        XCTAssertEqual(profile.groups.map(\.title), ["Bauvorhaben", "Benachrichtigungen", "App"])
        let toggles = profile.groups[1].rows
        XCTAssertEqual(toggles.map(\.isToggle), [true, true, true])
        XCTAssertEqual(toggles.map(\.checked), [true, true, false])
        XCTAssertEqual(profile.groups[0].rows.map(\.value), ["Musterweg 12", "März 2026", "4 Kontakte"])
        XCTAssertEqual(profile.groups[0].rows.map(\.isLink), [true, true, true])
    }

    func testUnknownPhaseTradeThrowsDecoding() async throws {
        let repo = try repository { object in
            var project = object["project"] as? [String: Any] ?? [:]
            var phase = project["phase"] as? [String: Any] ?? [:]
            phase["tradeId"] = "nope"
            project["phase"] = phase
            object["project"] = project
        }
        do {
            _ = try await AppData.load(from: repo)
            XCTFail("Expected RepositoryError.decoding")
        } catch {
            XCTAssertEqual(error as? RepositoryError, .decoding("project.phase.tradeId"))
        }
    }

    func testUnknownAskTargetThrowsDecoding() async throws {
        let repo = try repository { object in
            var schedule = object["schedule"] as? [String: Any] ?? [:]
            var asks = schedule["asks"] as? [[String: Any]] ?? []
            asks[0]["target"] = "elsewhere/screed"
            schedule["asks"] = asks
            object["schedule"] = schedule
        }
        do {
            _ = try await AppData.load(from: repo)
            XCTFail("Expected RepositoryError.decoding")
        } catch {
            XCTAssertEqual(error as? RepositoryError, .decoding("schedule.asks.target"))
        }
    }
}
