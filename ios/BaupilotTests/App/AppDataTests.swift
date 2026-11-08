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

    // MARK: - M3 screen models

    private func expectDecoding(
        _ path: String, editing edit: (inout [String: Any]) -> Void,
        file: StaticString = #filePath, line: UInt = #line
    ) async throws {
        let repo = try repository(editing: edit)
        do {
            _ = try await AppData.load(from: repo)
            XCTFail("Expected RepositoryError.decoding(\(path))", file: file, line: line)
        } catch {
            XCTAssertEqual(error as? RepositoryError, .decoding(path), file: file, line: line)
        }
    }

    /// Parses "7.150,00" to cents.
    private func cents(_ german: String) throws -> Int {
        let parts = german.replacingOccurrences(of: ".", with: "").split(separator: ",")
        XCTAssertEqual(parts.count, 2, german)
        return try XCTUnwrap(Int(parts[0])) * 100 + XCTUnwrap(Int(parts[1]))
    }

    func testGewerkModelFromFixture() async throws {
        let loadedData = try await loaded()
        let screed = try XCTUnwrap(loadedData.trades["screed"])
        XCTAssertEqual(screed.tradeId, "screed")
        XCTAssertEqual(screed.title, "Estrich")
        XCTAssertEqual(screed.statusLabel, "Angebote einholen")
        XCTAssertEqual(screed.phaseLabel, "Phase 8 · Estrich")
        XCTAssertEqual(screed.facts.map(\.label), ["Zeitfenster", "Angebotsfrist", "Wartet auf"])
        XCTAssertEqual(screed.facts.map(\.value), ["KW 8–9", "15.01.2027", "Installateur"])
        XCTAssertEqual(screed.offersSummary, "2 von 3 erhalten")
        XCTAssertEqual(screed.offersHeading, "Angebote")
        XCTAssertEqual(screed.bestLabel, "Günstigstes")
        XCTAssertEqual(screed.stepsHeading, "Nächste Schritte")
        XCTAssertEqual(screed.steps.map(\.label), [
            "Anfrage an 3 Betriebe gesendet", "Angebote vergleichen", "Auftrag vergeben, Termin fixieren",
        ])
        XCTAssertEqual(screed.steps.map(\.done), [true, false, false])
        XCTAssertEqual(screed.compareLabel, "Angebote vergleichen")
        XCTAssertEqual(screed.moreLabel, "Weitere Aktionen")
        XCTAssertEqual(screed.offers.map(\.id), ["offer-wagner", "offer-hahn", "offer-lorenz"])
        XCTAssertEqual(screed.offers.map(\.price), ["9.800 €", "10.450 €", "Ausstehend"])
        XCTAssertEqual(screed.offers.map(\.best), [true, false, false])
        XCTAssertEqual(screed.offers.map(\.pending), [false, false, true])
        XCTAssertEqual(screed.offers[0].meta, "PDF · eingegangen 10.12.")
        XCTAssertEqual(screed.offers[2].meta, "Angefragt am 05.12.")
    }

    func testOfferTargetsFollowViewerData() async throws {
        let loadedData = try await loaded()
        let offers = try XCTUnwrap(loadedData.trades["screed"]).offers
        XCTAssertEqual(offers.map(\.target), [.document(id: "doc-offer-wagner"), nil, nil])
        let hahn = try await MockRepository.bundled().documents().items.first { $0.id == "doc-offer-hahn" }
        XCTAssertNotNil(hahn)
        XCTAssertNil(hahn?.viewer)
    }

    func testGewerkCompareTarget() async throws {
        let loadedData = try await loaded()
        let screed = try XCTUnwrap(loadedData.trades["screed"])
        XCTAssertEqual(screed.compareTarget, .compare(tradeId: "screed"))
    }

    func testOnlyTradesWithDetailHaveGewerkModel() async throws {
        let data = try await loaded()
        XCTAssertEqual(Set(data.trades.keys), ["screed"])
        XCTAssertEqual(Set(data.comparisons.keys), ["screed"])
        XCTAssertEqual(Set(data.viewers.keys), ["doc-offer-wagner"])
    }

    func testMissingPlatformValueThrows() async throws {
        try await expectDecoding("trades.detail.offersSummary") { object in
            var trades = object["trades"] as? [[String: Any]] ?? []
            let index = trades.firstIndex { $0["id"] as? String == "screed" } ?? 0
            var detail = trades[index]["detail"] as? [String: Any] ?? [:]
            detail["offersSummary"] = ["ios": NSNull(), "android": "nur Android"]
            trades[index]["detail"] = detail
            object["trades"] = trades
        }
    }

    func testStepCountMismatchThrows() async throws {
        try await expectDecoding("screens.Gewerk.ios.steps") { object in
            var screens = object["screens"] as? [String: Any] ?? [:]
            var gewerk = screens["Gewerk"] as? [String: Any] ?? [:]
            var ios = gewerk["ios"] as? [String: Any] ?? [:]
            ios["steps"] = [["label": "Nur ein Schritt"]]
            gewerk["ios"] = ios
            screens["Gewerk"] = gewerk
            object["screens"] = screens
        }
    }

    func testCompareRowsHighlightRule() async throws {
        let loadedData = try await loaded()
        let compare = try XCTUnwrap(loadedData.comparisons["screed"])
        XCTAssertEqual(compare.rows.count, 7)
        XCTAssertEqual(compare.rows.map(\.strong), [true, true, false, false, false, false, false])
        XCTAssertEqual(compare.rows.map(\.highlighted), [false, false, true, true, true, true, true])
        XCTAssertEqual(compare.rows[0].label, "Preis brutto")
        XCTAssertEqual(compare.rows[0].a, "9.800 €")
        XCTAssertEqual(compare.rows[0].b, "10.450 €")
        XCTAssertEqual(compare.rows[6].label, "Zahlung")
    }

    func testCompareColumnsAndTargets() async throws {
        let loadedData = try await loaded()
        let compare = try XCTUnwrap(loadedData.comparisons["screed"])
        XCTAssertEqual(compare.columns.map(\.name), ["Estrichbau Wagner", "Fließestrich Hahn"])
        XCTAssertEqual(compare.columns.map(\.best), [true, false])
        XCTAssertEqual(compare.columns.map(\.target), [.document(id: "doc-offer-wagner"), nil])
    }

    func testCompareTextsAndPendingRow() async throws {
        let loadedData = try await loaded()
        let compare = try XCTUnwrap(loadedData.comparisons["screed"])
        XCTAssertEqual(compare.tradeId, "screed")
        XCTAssertEqual(compare.title, "Angebote vergleichen")
        XCTAssertEqual(compare.subtitle, "2 von 3 erhalten · gleiche Fläche 112 m²")
        XCTAssertEqual(compare.deadlineNote, "Frist 15.01.")
        XCTAssertEqual(compare.tableLabel, "Vergleich der Estrich-Angebote")
        XCTAssertEqual(
            compare.note,
            "Hahn enthält das Belegreifheizen (ca. 600 €). Rechnet man es heraus, liegen beide nur rund 50 € auseinander.")
        XCTAssertEqual(compare.pending, PendingOffer(firm: "Bodenbau Lorenz", text: "Angefragt am 05.12. · noch kein Angebot"))
        XCTAssertEqual([compare.remindLabel, compare.askLabel, compare.awardLabel], ["Erinnern", "Rückfrage", "Wagner beauftragen"])
    }

    func testCompareOfferIdsMismatchThrows() async throws {
        try await expectDecoding("comparisons.offerIds") { object in
            var comparisons = object["comparisons"] as? [[String: Any]] ?? []
            comparisons[0]["offerIds"] = ["offer-wagner", "offer-unknown"]
            object["comparisons"] = comparisons
        }
    }

    func testDocumentsFiltersMapToKinds() async throws {
        let documents = try await loaded().documents
        XCTAssertEqual(documents.filters.map(\.text), ["Alle 26", "Angebote 7", "Verträge 3", "Rechnungen 4", "Pläne 12"])
        XCTAssertEqual(documents.filters.map(\.kind), [nil, .offer, .contract, .invoice, .plan])
        XCTAssertEqual(documents.initialFilter, 0)
        XCTAssertEqual(documents.kicker, "26 Dokumente · 11 Gewerke")
        XCTAssertEqual(documents.title, "Dokumente")
        XCTAssertEqual(documents.addLabel, "Dokument hinzufügen")
        XCTAssertEqual(documents.searchLabel, "Dokumente durchsuchen")
        XCTAssertEqual(documents.searchPlaceholder, "Firma, Gewerk oder Betrag suchen")
        XCTAssertEqual(documents.filtersLabel, "Dokumenttyp")
        XCTAssertEqual(documents.recentLabel, "Zuletzt hinzugefügt")
        XCTAssertEqual(documents.byTradeLabel, "Nach Gewerk")
    }

    func testDocumentsFilterRows() async throws {
        let documents = try await loaded().documents
        XCTAssertEqual(documents.rows.count, 5)
        XCTAssertEqual(documents.rows(filter: 0).count, 5)
        XCTAssertEqual(documents.rows(filter: 1).map(\.id), ["doc-offer-hahn", "doc-offer-wagner"])
        XCTAssertEqual(documents.rows(filter: 2).map(\.id), ["doc-contract-electrical"])
        XCTAssertEqual(documents.rows(filter: 3).map(\.id), ["doc-invoice-windows-2"])
        XCTAssertEqual(documents.rows(filter: 4).map(\.id), ["doc-plan-floor-og"])
        XCTAssertEqual(documents.rows(filter: 9).count, 5)
        XCTAssertEqual(documents.rows(filter: -1).count, 5)
    }

    func testDocumentRowsFromItems() async throws {
        let rows = try await loaded().documents.rows
        XCTAssertEqual(rows.map(\.kind), [.offer, .offer, .invoice, .contract, .plan])
        XCTAssertEqual(rows.map(\.amount), ["10.450 €", "9.800 €", "18.400 €", nil, nil])
        XCTAssertEqual(rows[0].title, "Angebot Fließestrich Hahn")
        XCTAssertEqual(rows[0].meta, "Estrich · 14.12.")
        XCTAssertEqual(rows.map(\.target), [nil, .document(id: "doc-offer-wagner"), nil, nil, nil])
    }

    func testFilterMappingMismatchThrows() async throws {
        try await expectDecoding("screens.Dokumente.ios.filters") { object in
            var screens = object["screens"] as? [String: Any] ?? [:]
            var dokumente = screens["Dokumente"] as? [String: Any] ?? [:]
            var ios = dokumente["ios"] as? [String: Any] ?? [:]
            var filters = ios["filters"] as? [[String: Any]] ?? []
            filters[2]["text"] = "Verträge 4"
            ios["filters"] = filters
            dokumente["ios"] = ios
            screens["Dokumente"] = dokumente
            object["screens"] = screens
        }
    }

    func testDocumentsBannerFromFixture() async throws {
        let banner = try await loaded().documents.banner
        XCTAssertEqual(banner.title, "Rechnung prüfen & zahlen")
        XCTAssertEqual(banner.sub, "Fenster & Türen · fällig 22.12.")
        XCTAssertEqual(banner.amount, "18.400 €")
    }

    func testViewerLinesAndTotal() async throws {
        let loadedData = try await loaded()
        let viewer = try XCTUnwrap(loadedData.viewers["doc-offer-wagner"])
        XCTAssertEqual(viewer.id, "doc-offer-wagner")
        XCTAssertEqual(viewer.title, "Angebot Estrichbau Wagner")
        XCTAssertEqual(viewer.subtitle, "PDF · 2 Seiten · eingegangen 10.12.2026")
        XCTAssertEqual(viewer.pageLabel, "Vorschau Seite 1 von 2")
        XCTAssertEqual(viewer.pageFooter, "Seite 1 von 2")
        XCTAssertEqual(viewer.issuerName, "Estrichbau Wagner GmbH")
        XCTAssertEqual(viewer.issuerAddress, "Industriestraße 4 · 68519 Viernheim")
        XCTAssertEqual(viewer.number, "Angebot Nr. 2026-118")
        XCTAssertEqual(viewer.reference, "BV Einfamilienhaus Musterweg 12 · gültig bis 31.01.2027")
        XCTAssertEqual(viewer.lines.map(\.pos), ["1", "2", "3", "4", "5"])
        XCTAssertEqual(viewer.lines.first?.text, "Randdämmstreifen 8 mm, liefern & setzen")
        XCTAssertEqual(viewer.lines.map(\.sum), ["420,00", "310,00", "7.150,00", "355,00", "1.565,00"])
        XCTAssertEqual(viewer.totalLabel, "Gesamt brutto")
        XCTAssertEqual(viewer.total, "9.800,00 €")
        XCTAssertEqual(viewer.facts.map(\.value), ["Estrich", "Angebot", "9.800 €"])
        XCTAssertEqual(viewer.compareLabel, "Im Vergleich ansehen")
        XCTAssertEqual(viewer.shareLabel, "Teilen")
        XCTAssertEqual(viewer.compareTarget, .compare(tradeId: "screed"))
    }

    func testViewerLinesSumToTotal() async throws {
        let loadedData = try await loaded()
        let viewer = try XCTUnwrap(loadedData.viewers["doc-offer-wagner"])
        let sum = try viewer.lines.reduce(0) { try $0 + cents($1.sum) }
        XCTAssertEqual(sum, 980_000)
        XCTAssertEqual(try cents(viewer.total.replacingOccurrences(of: " €", with: "")), sum)
    }

    func testViewerPageFooterNilThrows() async throws {
        try await expectDecoding("viewer.pageFooter") { object in
            var documents = object["documents"] as? [String: Any] ?? [:]
            var items = documents["items"] as? [[String: Any]] ?? []
            let index = items.firstIndex { $0["id"] as? String == "doc-offer-wagner" } ?? 0
            var viewer = items[index]["viewer"] as? [String: Any] ?? [:]
            viewer["pageFooter"] = ["ios": NSNull(), "android": "nur Android"]
            items[index]["viewer"] = viewer
            documents["items"] = items
            object["documents"] = documents
        }
    }

    func testNotificationGroupsAndUnread() async throws {
        let notifications = try await loaded().notifications
        XCTAssertEqual(notifications.title, "Mitteilungen")
        XCTAssertEqual(notifications.markAllLabel, "Alle gelesen")
        XCTAssertEqual(notifications.groups.map(\.title), ["Heute", "Diese Woche"])
        XCTAssertEqual(notifications.groups[0].items.map(\.unread), [true, true])
        XCTAssertEqual(notifications.groups[1].items.map(\.unread), [false, false, false])
        XCTAssertEqual(notifications.groups.flatMap(\.items).map(\.kind), [.frist, .geld, .angebot, .angebot, .termin])
        XCTAssertEqual(notifications.groups[0].items[0].title, "Frist überschritten")
        XCTAssertEqual(notifications.groups[0].items[0].when, "08:10")
        XCTAssertEqual(notifications.unreadCount, 2)
    }

    func testNotificationTargets() async throws {
        let items = try await loaded().notifications.groups.flatMap(\.items)
        XCTAssertEqual(items.map(\.target), [
            .tab(.inspections), .tab(.documents), .push(.compare(tradeId: "screed")),
            .push(.trade(id: "screed")), nil,
        ])
    }

    func testUnknownNotificationTargetThrows() async throws {
        try await expectDecoding("notifications.items.target") { object in
            var groups = object["notifications"] as? [[String: Any]] ?? []
            var items = groups[0]["items"] as? [[String: Any]] ?? []
            items[0]["target"] = "elsewhere/screed"
            groups[0]["items"] = items
            object["notifications"] = groups
        }
    }

    func testBellCountMatchesUnreadNotifications() async throws {
        let project = try await MockRepository.bundled().project()
        let data = try await loaded()
        XCTAssertEqual(project.unreadNotifications, 2)
        XCTAssertEqual(data.notifications.unreadCount, project.unreadNotifications)
        XCTAssertTrue(data.home.hasUnreadNotifications)
        XCTAssertTrue(data.home.notificationsAria.contains("2"))
    }

    func testMissingScreenTextThrowsFixtureMissing() async throws {
        let repo = try repository { object in
            var screens = object["screens"] as? [String: Any] ?? [:]
            screens["Vergleich"] = nil
            object["screens"] = screens
        }
        do {
            _ = try await AppData.load(from: repo)
            XCTFail("Expected RepositoryError.fixtureMissing")
        } catch {
            XCTAssertEqual(error as? RepositoryError, .fixtureMissing("screens.Vergleich.ios"))
        }
    }
}
