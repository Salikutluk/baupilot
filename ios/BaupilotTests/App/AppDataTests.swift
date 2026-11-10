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
            .defects, .tab(.documents), .push(.compare(tradeId: "screed")),
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

    // MARK: - M4 inspection models

    private func editWindowsCheck(
        _ object: inout [String: Any], _ key: String, trade: String = "windows",
        _ edit: (inout [String: Any]) -> Void
    ) {
        editTrade(&object, trade) { tradeObject in
            var checks = tradeObject["checks"] as? [String: Any] ?? [:]
            var check = checks[key] as? [String: Any] ?? [:]
            edit(&check)
            checks[key] = check
            tradeObject["checks"] = checks
        }
    }

    private func editTrade(
        _ object: inout [String: Any], _ trade: String, _ edit: (inout [String: Any]) -> Void
    ) {
        var inspections = object["inspections"] as? [String: Any] ?? [:]
        var byTrade = inspections["byTrade"] as? [String: Any] ?? [:]
        var tradeObject = byTrade[trade] as? [String: Any] ?? [:]
        edit(&tradeObject)
        byTrade[trade] = tradeObject
        inspections["byTrade"] = byTrade
        object["inspections"] = inspections
    }

    private func editOverview(_ object: inout [String: Any], _ edit: (inout [String: Any]) -> Void) {
        var inspections = object["inspections"] as? [String: Any] ?? [:]
        var overview = inspections["overview"] as? [String: Any] ?? [:]
        edit(&overview)
        inspections["overview"] = overview
        object["inspections"] = inspections
    }

    private let checksPath = "inspections.byTrade.windows.checks"
    private let stepRoute = AppRoute.inspectionStep(tradeId: "windows", elementId: "F8", step: 3)

    func testLoadsInspectionsOverview() async throws {
        let model = try await loaded().inspections
        XCTAssertEqual(model.title, "Abnahmen")
        XCTAssertEqual(model.templatesLabel, "Vorlagen")
        XCTAssertEqual(model.viewsLabel, "Ansicht")
        XCTAssertEqual(model.views, ["Gewerke", "Offene Mängel (3)"])
        XCTAssertEqual(model.rows.map(\.tradeId), [
            "earthworks", "shell", "carpentry", "roofing", "windows", "electrical", "plumbing", "screed", "tiles",
            "painting",
        ])
        XCTAssertEqual(model.rows.map(\.status), [
            .accepted, .reserved, .accepted, .reserved, .today, .planned, .planned, .planned, .planned, .planned,
        ])
        XCTAssertEqual(model.rows[0].name, "Erdarbeiten")
        XCTAssertEqual(model.rows[0].subtitle, "9 Prüfschritte · abgenommen 14.04.")
        XCTAssertEqual(model.rows[1].subtitle, "9 Prüfschritte · 1 Mangel offen")
        XCTAssertEqual(model.rows[1].statusLabel, "Mit Vorbehalt")
        XCTAssertEqual(model.rows[4].subtitle, "6 Prüfschritte je Element · 7 von 12")
        XCTAssertEqual(model.rows.map(\.statusLabel).filter { $0 == "Mit Vorbehalt" }.count, 2)
        XCTAssertEqual(model.rows[4].target, .inspection(tradeId: "windows"))
        XCTAssertEqual(model.rows[0].target, .inspection(tradeId: "earthworks"))
        XCTAssertNil(model.rows[3].target)
    }

    func testLoadsDefectsBoard() async throws {
        let board = try await loaded().defectsBoard
        XCTAssertEqual(board.title, "Abnahmen")
        XCTAssertEqual(board.views, ["Gewerke", "Offene Mängel (3)"])
        XCTAssertEqual(board.viewsLabel, "Ansicht")
        XCTAssertEqual(board.cards.map(\.id), ["defect-shell-crack", "defect-roof-verge", "defect-window-f2"])
        XCTAssertEqual(board.cards.map(\.trade), ["Rohbau", "Dachdecker", "Fenster & Türen"])
        XCTAssertEqual(board.cards.map(\.urgency), [.late, .soon, .later])
        XCTAssertEqual(board.cards.map(\.showsActions), [true, false, false])
        XCTAssertEqual(board.cards.map(\.dueLabel), ["3 Tage überfällig", "noch 21 Tage", "noch 28 Tage"])
        XCTAssertEqual(board.cards[2].title, "F2 · Flügel fällt zu")
        XCTAssertEqual(board.cards[2].meta, "Frist 15.01.2027 · 2 Fotos")
        XCTAssertEqual(board.remindLabel, "Erinnern")
        XCTAssertEqual(board.resolveLabel, "Behoben?")
        XCTAssertEqual(board.resolvedLabel, "Behobene Mängel")
        XCTAssertEqual(board.resolvedCount, "3")
    }

    func testLoadsElements() async throws {
        let loadedData = try await loaded()
        XCTAssertEqual(Set(loadedData.inspectionElements.keys), ["windows"])
        let elements = try XCTUnwrap(loadedData.inspectionElements["windows"])
        XCTAssertEqual(elements.tradeId, "windows")
        XCTAssertEqual(elements.title, "Fenster & Türen")
        XCTAssertEqual(elements.subtitle, "Fensterbau Weber GmbH · Fr, 18.12.2026")
        XCTAssertEqual(elements.counter, "7 von 12 Elementen geprüft")
        XCTAssertEqual(elements.guideLinkLabel, "Anleitung")
        XCTAssertEqual(elements.progress.count, 12)
        XCTAssertEqual(elements.progress.filter { $0 == .ok }.count, 6)
        XCTAssertEqual(elements.progress.filter { $0 == .defect }.count, 1)
        XCTAssertEqual(elements.progress.filter { $0 == .open }.count, 5)
        XCTAssertEqual(elements.legend.map(\.status), [.ok, .defect, .open])
        XCTAssertEqual(elements.legend.map(\.label), ["6 in Ordnung", "1 Mangel", "5 offen"])
        XCTAssertEqual(elements.floors.map(\.title), ["Erdgeschoss", "Obergeschoss"])
        XCTAssertEqual(elements.floors.map(\.moreLabel), ["Alle 6", "Alle 6"])
        let items = elements.floors.flatMap(\.items)
        XCTAssertEqual(items.map(\.id), ["F1", "F2", "F3", "F7", "F8", "F9"])
        XCTAssertEqual(items.map(\.status), [.ok, .defect, .ok, .ok, .now, .open])
        XCTAssertEqual(items[1].room, "Wohnen")
        XCTAssertEqual(items[1].type, "Dreh-Kipp, 1-flügelig")
        XCTAssertEqual(items[1].label, "1 Mangel")
        XCTAssertEqual(items.map(\.target), [nil, .newDefect, nil, nil, stepRoute, nil])
        XCTAssertEqual(elements.nextLabel, "Weiter prüfen: F8 · Schlafen")
        XCTAssertEqual(elements.nextTarget, stepRoute)
    }

    func testLoadsStepModel() async throws {
        let loadedData = try await loaded()
        XCTAssertEqual(Set(loadedData.inspectionSteps.keys), [stepRoute])
        let step = try XCTUnwrap(loadedData.inspectionSteps[stepRoute])
        XCTAssertEqual(step.route, stepRoute)
        XCTAssertEqual(step.counter, "Schritt 3 von 6")
        XCTAssertEqual(step.title, "F8 · Schlafen")
        XCTAssertEqual(step.subtitle, "Obergeschoss · Dreh-Kipp-Fenster, 1-flügelig")
        XCTAssertEqual(step.done.map(\.label), ["Drehstellung: Flügel bleibt stehen", "Kippstellung: Flügel hält Position"])
        XCTAssertEqual(step.done.map(\.meta), ["1 Foto", "1 Foto"])
        XCTAssertEqual(step.current.n, 3)
        XCTAssertEqual(step.current.title, "Sitzt das Fenster im Wasser?")
        XCTAssertEqual(
            step.current.text,
            "Wasserwaage oben und seitlich an den Rahmen halten – die Blase muss jeweils mittig stehen.")
        XCTAssertEqual(step.current.photoCaption, "Foto oben")
        XCTAssertEqual(step.current.addPhotoLabel, "Foto seitlich")
        XCTAssertEqual(step.guideLabel, "So prüfen")
        XCTAssertEqual(step.defectLabel, "Mangel")
        XCTAssertEqual(step.okLabel, "In Ordnung")
        XCTAssertEqual(step.next.map(\.n), [4, 5, 6])
        XCTAssertEqual(step.next.map(\.label), [
            "Schließen & Verriegeln, Griff leichtgängig", "Glas, Rahmen & Beschläge unbeschädigt",
            "Anschlussfugen & Fensterbank dicht",
        ])
        XCTAssertEqual(step.defectTarget, .newDefect)
    }

    func testLoadsDefectForm() async throws {
        let form = try await loaded().defectForm
        XCTAssertEqual(form.tradeId, "windows")
        XCTAssertEqual(form.title, "Mangel erfassen")
        XCTAssertEqual(form.saveLabel, "Sichern")
        XCTAssertEqual(form.cancelLabel, "Abbrechen")
        XCTAssertEqual(form.context, "F2 · Wohnen · Schritt 1")
        XCTAssertEqual(form.finding, "Drehstellung: Flügel bleibt nicht stehen")
        XCTAssertEqual(form.descriptionLabel, "Beschreibung")
        XCTAssertEqual(form.description, "Flügel fällt ab ca. 45° Öffnung selbstständig wieder zu.")
        XCTAssertEqual(form.photosLabel, "Fotos (2)")
        XCTAssertEqual(form.photoLabels, ["Foto 1", "Foto 2"])
        XCTAssertEqual(form.addPhotoLabel, "Foto aufnehmen")
        XCTAssertEqual(form.severityLabel, "Schwere")
        XCTAssertEqual(form.severities, ["Gering", "Mittel", "Erheblich"])
        XCTAssertEqual(form.initialSeverity, 1)
        XCTAssertEqual(form.deadlineLabel, "Behebung bis")
        XCTAssertEqual(form.deadlineOptions.map(\.label), ["+ 14 Tage", "+ 4 Wochen", "Eigenes Datum"])
        XCTAssertEqual(form.deadlineOptions.map(\.offsetDays), [14, 28, nil])
        XCTAssertEqual(form.deadlineBase, CalendarDay(year: 2026, month: 12, day: 18))
        XCTAssertEqual(form.initialDeadline, 1)
        XCTAssertEqual(form.deadlineText(option: 0), "Fr, 01.01.2027")
        XCTAssertEqual(form.deadlineText(option: 1), "Fr, 15.01.2027")
        XCTAssertNil(form.deadlineText(option: 2))
        XCTAssertNil(form.deadlineText(option: 3))
        XCTAssertEqual(form.responsibleLabel, "Zuständig")
        XCTAssertEqual(form.responsible, "Fensterbau Weber GmbH")
        XCTAssertEqual(form.notifyLabel, "Handwerker per E-Mail informieren")
        XCTAssertTrue(form.notifyInitial)
    }

    func testLoadsSignOff() async throws {
        let loadedData = try await loaded()
        XCTAssertEqual(Set(loadedData.signOffs.keys), ["windows"])
        let sign = try XCTUnwrap(loadedData.signOffs["windows"])
        XCTAssertEqual(sign.inspectionId, "windows")
        XCTAssertEqual(sign.title, "Abnahme abschließen")
        XCTAssertEqual(sign.subtitle, "Fr, 18.12.2026 · Musterweg 12")
        XCTAssertEqual(sign.stats.map(\.value), ["12", "11", "1"])
        XCTAssertEqual(sign.stats.map(\.label), ["geprüft", "in Ordnung", "Mangel"])
        XCTAssertEqual(sign.stats.map(\.tone), [.surface, .ok, .error])
        XCTAssertEqual(sign.defects.map(\.title), ["F2 · Flügel fällt zu"])
        XCTAssertEqual(sign.defects.map(\.meta), ["Mittel · 2 Fotos"])
        XCTAssertEqual(sign.defects.map(\.due), ["bis 15.01."])
        XCTAssertEqual(sign.reservation, "Die Abnahme erfolgt unter Vorbehalt der aufgeführten Mängel und Fristen.")
        XCTAssertEqual(sign.craftsmanLabel, "Handwerker")
        XCTAssertEqual(sign.craftsmanName, "Fensterbau Weber")
        XCTAssertEqual(sign.craftsmanStatus, "Unterschrieben 14:32")
        XCTAssertEqual(sign.ownerLabel, "Bauherr")
        XCTAssertEqual(sign.ownerName, "Max Mustermann")
        XCTAssertEqual(sign.ownerPending, "Ausstehend")
        XCTAssertEqual(sign.signHereLabel, "Hier unterschreiben")
        XCTAssertEqual(sign.signatureLabel, "Unterschrift Handwerker")
        XCTAssertEqual(sign.finishLabel, "Protokoll abschließen")
        XCTAssertEqual(sign.note, "PDF mit Fotos geht an Sie und den Handwerker")
        XCTAssertEqual(sign.finishTarget, .defects)
    }

    func testLoadsAllM4ScreenTexts() async throws {
        let repo = try MockRepository.bundled()
        let tabs = try await repo.screenText("AbnGewerke", as: TabsScreenText.self).tabs
        XCTAssertEqual(tabs.map(\.route), ["home", "schedule", "inspections", "documents", "profile"])
        XCTAssertEqual(tabs.map(\.label), ["Übersicht", "Zeitplan", "Abnahmen", "Dokumente", "Profil"])
        let maengel = try await repo.screenText("AbnMaengel", as: TabsScreenText.self)
        XCTAssertEqual(maengel.tabs, tabs)
        for name in ["AbnElemente", "AbnPruefung", "AbnMangel", "AbnSignatur"] {
            _ = try await repo.screenText(name, as: EmptyScreenText.self)
        }
        let missing = try repository { object in
            var screens = object["screens"] as? [String: Any] ?? [:]
            screens["AbnSignatur"] = nil
            object["screens"] = screens
        }
        do {
            _ = try await AppData.load(from: missing)
            XCTFail("Expected RepositoryError.fixtureMissing")
        } catch {
            XCTAssertEqual(error as? RepositoryError, .fixtureMissing("screens.AbnSignatur.ios"))
        }
    }

    func testLoadsAllM4Routes() async throws {
        let repo = try MockRepository.bundled()
        let overview = try await repo.inspections()
        guard case let .object(root) = overview, case let .object(ov)? = root["overview"],
            case let .array(rows)? = ov["rows"]
        else { return XCTFail("overview rows missing") }
        var parsed = 0
        for case let .object(row) in rows {
            guard case let .string(raw)? = row["route"] else { continue }
            let route = try RouteParser.route(raw, field: "inspections.overview.rows.route")
            XCTAssertNotNil(route, raw)
            parsed += 1
        }
        XCTAssertEqual(parsed, 9)
        let data = try await loaded()
        XCTAssertEqual(data.inspections.rows.compactMap(\.target).count, 9)
        let items = try XCTUnwrap(data.inspectionElements["windows"]).floors.flatMap(\.items)
        XCTAssertEqual(items.compactMap(\.target), [.newDefect, stepRoute])
    }

    func testInspectionTitlesAndFallbacks() async throws {
        let titles = try await loaded().routeTitles
        XCTAssertEqual(titles.title(.inspection(tradeId: "windows")), "Fenster & Türen")
        XCTAssertEqual(titles.title(.inspection(tradeId: "roofing")), "Dachdecker")
        XCTAssertEqual(titles.title(stepRoute), "F8 · Schlafen")
        XCTAssertEqual(titles.title(.inspectionStep(tradeId: "windows", elementId: "F9", step: 1)), "F9")
        XCTAssertEqual(titles.title(.newDefect), "Mangel erfassen")
        XCTAssertEqual(titles.title(.signOff(inspectionId: "windows")), "Abnahme abschließen")
        XCTAssertEqual(titles.title(.signOff(inspectionId: "shell")), "shell")
        XCTAssertEqual(titles.title(.inspection(tradeId: "unknown")), "unknown")
    }

    func testEveryRouteTitleIsNonEmpty() async throws {
        let data = try await loaded()
        let routes: [AppRoute] = [
            .trade(id: "screed"), .compare(tradeId: "screed"), .document(id: "doc-offer-wagner"), .notifications,
            .inspection(tradeId: "windows"), stepRoute, .newDefect, .signOff(inspectionId: "windows"),
        ]
        for route in routes {
            XCTAssertFalse(data.routeTitles.title(route).isEmpty, "\(route)")
        }
        for tab in AppTab.allCases {
            XCTAssertFalse(data.routeTitles.backLabel(path: [], index: 0, tab: tab).isEmpty, "\(tab)")
        }
        XCTAssertEqual(
            data.routeTitles.backLabel(path: [.inspection(tradeId: "windows"), stepRoute], index: 1, tab: .inspections),
            "Fenster & Türen")
        XCTAssertEqual(
            data.routeTitles.backLabel(path: [.inspection(tradeId: "windows")], index: 0, tab: .inspections), "Abnahmen")
    }

    // MARK: M4 consistency

    func testWindowFiguresAreConsistent() async throws {
        let data = try await loaded()
        let elements = try XCTUnwrap(data.inspectionElements["windows"])
        let sign = try XCTUnwrap(data.signOffs["windows"])
        let form = data.defectForm
        let progressDefects = elements.progress.filter { $0 == .defect }.count
        let stats = try sign.stats.map { try XCTUnwrap(Int($0.value)) }
        XCTAssertEqual(stats[0], elements.progress.count)
        XCTAssertEqual(stats[2], progressDefects)
        XCTAssertEqual(stats[1], stats[0] - stats[2])
        let defectItems = elements.floors.flatMap(\.items).filter { $0.status == .defect }
        XCTAssertEqual(defectItems.map(\.id), ["F2"])
        XCTAssertTrue(form.context.hasPrefix("F2"))
        let repoDefects = try await MockRepository.bundled().defects().filter { $0.tradeId == "windows" }
        let windowsDefect = try XCTUnwrap(repoDefects.first)
        XCTAssertEqual(repoDefects.count, 1)
        let signDefect = try XCTUnwrap(sign.defects.first)
        XCTAssertEqual(signDefect.title, windowsDefect.title)
        let photos = try XCTUnwrap(windowsDefect.photos)
        XCTAssertEqual(signDefect.meta, "\(try XCTUnwrap(windowsDefect.severity)) · \(photos) Fotos")
        XCTAssertEqual(windowsDefect.deadline, "2027-01-15")
        XCTAssertEqual(signDefect.due, "bis 15.01.")
        XCTAssertEqual(form.deadlineText(option: form.initialDeadline), "Fr, 15.01.2027")
        XCTAssertEqual(windowsDefect.deadlineLabel, "Frist 15.01.2027")
        let board = data.defectsBoard
        XCTAssertEqual(board.cards.count, 3)
        XCTAssertEqual(board.views[1], "Offene Mängel (\(board.cards.count))")
        XCTAssertEqual(data.inspections.views, board.views)
    }

    // MARK: M4 error paths

    func testMissingIosCounterFailsLoad() async throws {
        try await expectDecoding("\(checksPath).elements.counter") { object in
            editWindowsCheck(&object, "elements") { $0["counter"] = ["ios": NSNull(), "android": "x"] }
        }
    }

    func testMissingIosSummaryLabelFailsLoad() async throws {
        try await expectDecoding("\(checksPath).elements.summary.label") { object in
            editWindowsCheck(&object, "elements") { check in
                var summary = check["summary"] as? [[String: Any]] ?? []
                summary[1]["label"] = ["ios": NSNull(), "android": "x"]
                check["summary"] = summary
            }
        }
    }

    func testMissingIosOverviewStatusLabelFailsLoad() async throws {
        try await expectDecoding("inspections.overview.statusLabels") { object in
            editOverview(&object) { overview in
                var labels = overview["statusLabels"] as? [String: Any] ?? [:]
                labels["reserved"] = ["ios": NSNull(), "android": "x"]
                overview["statusLabels"] = labels
            }
        }
    }

    func testUnknownOverviewStatusFailsLoad() async throws {
        try await expectDecoding("inspections.overview.rows.status") { object in
            editOverview(&object) { overview in
                var rows = overview["rows"] as? [[String: Any]] ?? []
                rows[0]["status"] = "bogus"
                overview["rows"] = rows
            }
        }
    }

    func testUnknownElementStatusFailsLoad() async throws {
        try await expectDecoding("\(checksPath).elements.progress") { object in
            editWindowsCheck(&object, "elements") { check in
                var progress = check["progress"] as? [String] ?? []
                progress[0] = "bogus"
                check["progress"] = progress
            }
        }
        try await expectDecoding("\(checksPath).elements.floors.items.status") { object in
            editWindowsCheck(&object, "elements") { check in
                var floors = check["floors"] as? [[String: Any]] ?? []
                var items = floors[0]["items"] as? [[String: Any]] ?? []
                items[0]["status"] = "bogus"
                floors[0]["items"] = items
                check["floors"] = floors
            }
        }
    }

    func testTwoNowElementsFailLoad() async throws {
        try await expectDecoding("\(checksPath).elements.floors") { object in
            editWindowsCheck(&object, "elements") { check in
                var floors = check["floors"] as? [[String: Any]] ?? []
                var items = floors[0]["items"] as? [[String: Any]] ?? []
                items[0]["status"] = "now"
                floors[0]["items"] = items
                check["floors"] = floors
            }
        }
    }

    func testStepNumberMismatchFailsLoad() async throws {
        try await expectDecoding("\(checksPath).stepF8") { object in
            editWindowsCheck(&object, "stepF8") { check in
                var current = check["current"] as? [String: Any] ?? [:]
                current["n"] = "4"
                check["current"] = current
            }
        }
        try await expectDecoding("\(checksPath).stepF8") { object in
            editWindowsCheck(&object, "stepF8") { check in
                var current = check["current"] as? [String: Any] ?? [:]
                current["n"] = "03"
                check["current"] = current
            }
        }
    }

    func testStepPhotosCountFailsLoad() async throws {
        try await expectDecoding("\(checksPath).stepF8.current.photos") { object in
            editWindowsCheck(&object, "stepF8") { check in
                var current = check["current"] as? [String: Any] ?? [:]
                current["photos"] = ["Foto oben"]
                check["current"] = current
            }
        }
    }

    func testSeverityNotInOptionsFailsLoad() async throws {
        try await expectDecoding("\(checksPath).defectF2.severity") { object in
            editWindowsCheck(&object, "defectF2") { check in
                var severity = check["severity"] as? [String: Any] ?? [:]
                severity["selected"] = "Kritisch"
                check["severity"] = severity
            }
        }
    }

    func testDeadlineMismatchFailsLoad() async throws {
        try await expectDecoding("\(checksPath).defectF2.deadlineLabel") { object in
            editWindowsCheck(&object, "defectF2") { check in
                check["deadlineLabel"] = ["ios": "Sa, 16.01.2027", "android": "16.01.2027"]
            }
        }
    }

    func testMalformedOverviewRouteFailsLoad() async throws {
        try await expectDecoding("inspections.overview.rows.route") { object in
            editOverview(&object) { overview in
                var rows = overview["rows"] as? [[String: Any]] ?? []
                rows[0]["route"] = "inspection/a/b/c/d"
                overview["rows"] = rows
            }
        }
    }

    func testMalformedElementRouteFailsLoad() async throws {
        try await expectDecoding("\(checksPath).elements.floors.items.route") { object in
            editWindowsCheck(&object, "elements") { check in
                var floors = check["floors"] as? [[String: Any]] ?? []
                var items = floors[0]["items"] as? [[String: Any]] ?? []
                items[0]["route"] = "defect/old"
                floors[0]["items"] = items
                check["floors"] = floors
            }
        }
    }

    func testStatsCountMismatchFailsLoad() async throws {
        try await expectDecoding("\(checksPath).signature.stats") { object in
            editWindowsCheck(&object, "signature") { check in
                var stats = check["stats"] as? [[String: Any]] ?? []
                stats.removeLast()
                check["stats"] = stats
            }
        }
    }

    func testMissingIosOwnerFailsLoad() async throws {
        try await expectDecoding("\(checksPath).signature.owner") { object in
            editWindowsCheck(&object, "signature") { $0["owner"] = ["ios": NSNull(), "android": "x"] }
        }
    }

    func testWrongTypeUnderElementsFailsWithSlicePath() async throws {
        try await expectDecoding("\(checksPath).elements") { object in
            editWindowsCheck(&object, "elements") { $0["title"] = 5 }
        }
        try await expectDecoding("\(checksPath).stepF8") { object in
            editWindowsCheck(&object, "stepF8") { $0["done"] = "none" }
        }
        try await expectDecoding("inspections.overview") { object in
            editOverview(&object) { $0["rows"] = 7 }
        }
    }

    func testMissingSliceFailsAsFixtureMissing() async throws {
        let repo = try repository { object in
            editWindowsCheck(&object, "defectF2") { _ in }
            var inspections = object["inspections"] as? [String: Any] ?? [:]
            var byTrade = inspections["byTrade"] as? [String: Any] ?? [:]
            var windows = byTrade["windows"] as? [String: Any] ?? [:]
            var checks = windows["checks"] as? [String: Any] ?? [:]
            checks["defectF2"] = nil
            windows["checks"] = checks
            byTrade["windows"] = windows
            inspections["byTrade"] = byTrade
            object["inspections"] = inspections
        }
        do {
            _ = try await AppData.load(from: repo)
            XCTFail("Expected RepositoryError.fixtureMissing")
        } catch {
            XCTAssertEqual(error as? RepositoryError, .fixtureMissing("\(checksPath).defectF2"))
        }
    }

    // MARK: - M5 early trades

    private let shellPath = "inspections.byTrade.shell"
    private let electricalPath = "inspections.byTrade.electrical"
    private let measureRoute = AppRoute.inspectionCheck(tradeId: "shell", check: "measure")
    private let kitchenRoute = AppRoute.inspectionRoom(tradeId: "electrical", roomId: "kitchen")
    private let panelRoute = AppRoute.inspectionCheck(tradeId: "electrical", check: "panel")
    private let smokeRoute = AppRoute.inspectionCheck(tradeId: "electrical", check: "smoke")

    /// Replaces `list[index][key]` of a list member of a dictionary.
    private func setListItem(
        _ object: inout [String: Any], _ list: String, _ index: Int, _ key: String, _ value: Any
    ) {
        var items = object[list] as? [[String: Any]] ?? []
        items[index][key] = value
        object[list] = items
    }

    func testLoadsTradeProtocols() async throws {
        let protocols = try await loaded().tradeProtocols
        XCTAssertEqual(Set(protocols.keys), ["earthworks", "shell", "carpentry"])
        let earthworks = try XCTUnwrap(protocols["earthworks"])
        XCTAssertEqual(earthworks.title, "Erdarbeiten")
        XCTAssertEqual(earthworks.subtitle, "Tiefbau Schäfer · Baugrube & Gründung")
        XCTAssertEqual(earthworks.banner.tone, .ok)
        XCTAssertEqual(earthworks.banner.title, "Abgenommen am 14.04.2026")
        XCTAssertEqual(earthworks.banner.documentLabel, "PDF")
        XCTAssertNil(earthworks.banner.target)
        XCTAssertEqual(earthworks.groups.map(\.title), ["Absteckung", "Baugrube", "Leitungen & Verfüllung"])
        XCTAssertEqual(earthworks.groups.map(\.rows.count), [2, 4, 3])
        XCTAssertEqual(earthworks.groups[2].rows[2].label, "Mutterboden gesichert, Aushub abgefahren")
        XCTAssertTrue(earthworks.groups.flatMap(\.rows).allSatisfy { $0.mark == .ok && $0.target == nil })

        let shell = try XCTUnwrap(protocols["shell"])
        XCTAssertEqual(shell.banner.tone, .warn)
        XCTAssertEqual(shell.banner.title, "Abgenommen mit Vorbehalt")
        XCTAssertNil(shell.banner.documentLabel)
        XCTAssertEqual(shell.banner.target, .defects)
        let rows = shell.groups.flatMap(\.rows)
        XCTAssertEqual(rows.count, 9)
        let crack = try XCTUnwrap(rows.first { $0.label == "Kellerwand Nord ohne Risse" })
        XCTAssertEqual(crack.mark, .defect)
        XCTAssertEqual(crack.target, .defects)
        let measure = try XCTUnwrap(rows.first { $0.label == "Maße & Lot von Wänden und Öffnungen" })
        XCTAssertEqual(measure.meta, "12 Öffnungen")
        XCTAssertEqual(measure.target, .push(measureRoute))
        let others = rows.filter { $0.label != crack.label && $0.label != measure.label }
        XCTAssertTrue(others.allSatisfy { $0.mark == .ok && $0.target == nil })

        let carpentry = try XCTUnwrap(protocols["carpentry"])
        XCTAssertEqual(carpentry.subtitle, "Holzbau Becker · Dachstuhl Satteldach 38°")
        XCTAssertEqual(carpentry.banner.tone, .ok)
        XCTAssertEqual(carpentry.groups.flatMap(\.rows).count, 8)
    }

    func testLoadsElectricalOverview() async throws {
        let overviews = try await loaded().electricalOverviews
        XCTAssertEqual(Set(overviews.keys), ["electrical"])
        let model = try XCTUnwrap(overviews["electrical"])
        XCTAssertEqual(model.title, "Elektro")
        XCTAssertEqual(model.subtitle, "Elektro Schmitt GmbH · Mo, 26.04.2027")
        XCTAssertEqual(model.phases.map(\.title), ["1 · Rohinstallation", "2 · Fertiginstallation"])
        XCTAssertEqual(model.phases[0].kind, .done(status: "Abgenommen", date: "am 29.01.2027"))
        XCTAssertEqual(model.phases[1].kind, .active(progressLabel: "4 von 11 geprüft", fraction: 0.36))
        XCTAssertEqual(model.groups.map(\.title), ["Haustechnik", "Räume", "Dokumente vom Elektriker"])
        XCTAssertEqual(model.groups.map(\.moreLabel), [nil, "Alle 9", nil])
        let rows = model.groups.flatMap(\.rows)
        XCTAssertEqual(rows.map(\.name), [
            "Zählerschrank & FI-Test", "Rauchwarnmelder", "Küche", "Wohnen / Essen", "Bad",
            "Prüfprotokoll VDE 0100-600", "Verteilerplan & Stromkreisliste",
        ])
        let expectedTargets: [AppRoute?] = [panelRoute, smokeRoute, kitchenRoute, nil, nil, nil, nil]
        XCTAssertEqual(rows.map(\.target), expectedTargets)
        XCTAssertEqual(rows.map(\.tone), [.open, .running, .running, .ok, .open, .missing, .ok])
        XCTAssertEqual(rows.map(\.statusLabel), [
            "Offen", "Läuft", "Läuft", "In Ordnung", "Offen", "Fehlt", "Vorhanden",
        ])
        XCTAssertEqual(rows.map(\.icon), [.meterCabinet, .smokeAlarm, .home, .home, .home, .document, .document])
    }

    func testLoadsMeasureCheck() async throws {
        let data = try await loaded()
        XCTAssertEqual(Set(data.measureChecks.keys), [measureRoute])
        let check = try XCTUnwrap(data.measureChecks[measureRoute])
        XCTAssertEqual(check.route, measureRoute)
        XCTAssertEqual(check.counter, "Öffnung 8 von 12")
        XCTAssertEqual(check.title, "Fensteröffnung F8")
        XCTAssertEqual(check.subtitle(tolerance: 12), "OG Schlafen · Toleranz laut Vertrag ± 12 mm")
        XCTAssertEqual(check.subtitle(tolerance: check.initial.tolerance), "OG Schlafen · Toleranz laut Vertrag ± 12 mm")
        XCTAssertEqual(check.instruction,
            "Zollstock oben, mittig und unten anlegen. Den kleinsten Wert eintragen – das Fenster muss überall passen.")
        XCTAssertEqual(check.measuredHeading, "Bereits gemessen")
        XCTAssertEqual(check.plumbTitle, "Wände im Lot")
        XCTAssertEqual(check.plumbSubtitle, "2-m-Wasserwaage an allen Außenecken")
        XCTAssertEqual(check.plumbValue, "8 / 8")
        XCTAssertEqual(check.saveLabel, "Speichern")
        XCTAssertEqual(check.photoLabel, "Foto aufnehmen")
        XCTAssertEqual(check.defectLabel, "Mangel")
        XCTAssertEqual(check.withinLabel, "Innerhalb der Toleranz")
        XCTAssertEqual(check.outsideLabel, "Außerhalb der Toleranz – Mangel erfassen")
        XCTAssertEqual(check.dimensions.map(\.key), [.width, .height])
        XCTAssertEqual(check.dimensions.map(\.label), ["Breite", "Höhe"])
        XCTAssertEqual(check.measured, ["F1", "F2", "F3", "F4", "F5", "F6", "F7"])
        XCTAssertEqual(check.defectTarget, .newDefect)
        let initial = check.initial
        XCTAssertEqual(initial.valueText(.width), "1005")
        XCTAssertEqual(initial.valueText(.height), "1395")
        XCTAssertEqual(initial.diffText(.width), "−5 mm")
        XCTAssertEqual(initial.diffText(.height), "+10 mm")
        XCTAssertEqual(initial.step, 5)
        XCTAssertEqual(initial.tolerance, 12)
        XCTAssertTrue(initial.isWithinTolerance)
    }

    func testLoadsRoomCheck() async throws {
        let data = try await loaded()
        XCTAssertEqual(Set(data.roomChecks.keys), [kitchenRoute])
        let room = try XCTUnwrap(data.roomChecks[kitchenRoute])
        XCTAssertEqual(room.counter, "Schritt 2 von 5")
        XCTAssertEqual(room.title, "Küche")
        XCTAssertEqual(room.subtitle, "EG · laut Elektroplan: 8 Steckdosen, 3 Auslässe, 3 Geräteanschlüsse")
        XCTAssertEqual(room.done.map(\.label), ["Anzahl & Position laut Plan"])
        XCTAssertEqual(room.done.map(\.meta), ["8 / 8"])
        XCTAssertEqual(room.current.n, 2)
        XCTAssertEqual(room.current.title, "Jede Steckdose testen")
        XCTAssertEqual(room.sockets.map(\.id), ["S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"])
        XCTAssertEqual(room.sockets.map(\.status), [.ok, .ok, .ok, .defect, .ok, .now, .open, .open])
        XCTAssertEqual(room.sockets.map(\.label), [
            "korrekt", "korrekt", "korrekt", "Fehler", "korrekt", "jetzt", "offen", "offen",
        ])
        XCTAssertEqual(room.sockets[3].accessibilityLabel, "Steckdose S4: Fehler")
        XCTAssertEqual(room.progressLabel, "5 von 8 getestet")
        XCTAssertEqual(room.defectNote, "S4: Mangel erfasst")
        XCTAssertEqual(room.okNote, "S6 in Ordnung")
        XCTAssertEqual(room.defectLabel, "Mangel")
        XCTAssertEqual(room.next.map(\.n), [3, 4, 5])
        XCTAssertEqual(room.next[0].label, "Jeder Schalter schaltet das richtige Licht")
        XCTAssertEqual(room.defectTarget, .newDefect)
    }

    func testLoadsPanelCheck() async throws {
        let data = try await loaded()
        XCTAssertEqual(Set(data.panelChecks.keys), [panelRoute])
        let panel = try XCTUnwrap(data.panelChecks[panelRoute])
        XCTAssertEqual(panel.counter, "Schritt 3 von 5")
        XCTAssertEqual(panel.title, "Zählerschrank")
        XCTAssertEqual(panel.callout,
            "Nur von außen prüfen. Keine Abdeckungen abschrauben – das macht der Elektriker.")
        XCTAssertEqual(panel.done.map(\.label), ["Abdeckungen vollständig", "Jede Sicherung beschriftet"])
        XCTAssertEqual(panel.done.map(\.meta), ["1 Foto", "2 Fotos"])
        XCTAssertEqual(panel.current.n, 3)
        XCTAssertEqual(panel.current.title, "FI-Schutzschalter testen")
        XCTAssertEqual(panel.current.steps, [
            "Prüftaste „T“ am FI-Schalter drücken.",
            "Der Hebel muss sofort nach unten springen.",
            "Hebel wieder nach oben – Strom ist zurück.",
        ])
        XCTAssertEqual(panel.fis.map(\.name), ["FI 1 · Erdgeschoss", "FI 2 · Obergeschoss"])
        XCTAssertEqual(panel.fis.map(\.tone), [.ok, .open])
        XCTAssertEqual(panel.fis.map(\.statusLabel), ["Ausgelöst", "Jetzt testen"])
        XCTAssertEqual(panel.outcomes, ["Löst nicht aus", "FI 2 ausgelöst"])
        XCTAssertEqual(panel.next.map(\.n), [4, 5])
        XCTAssertEqual(panel.next.map(\.tag), [nil, "Pflicht"])
        XCTAssertEqual(panel.defectTarget, .newDefect)
    }

    func testLoadsSmokeCheck() async throws {
        let data = try await loaded()
        XCTAssertEqual(Set(data.smokeChecks.keys), [smokeRoute])
        let smoke = try XCTUnwrap(data.smokeChecks[smokeRoute])
        XCTAssertEqual(smoke.counter, "3 von 5")
        XCTAssertEqual(smoke.title, "Rauchwarnmelder")
        XCTAssertEqual(smoke.subtitle, "Pflicht in Schlaf- und Kinderzimmern und in Fluren, die als Fluchtweg dienen.")
        XCTAssertEqual(smoke.room, "Flur OG")
        XCTAssertEqual(smoke.nowLabel, "Jetzt prüfen")
        XCTAssertEqual(smoke.rooms.map(\.name), ["Schlafen", "Kind 1", "Kind 2", "Flur OG", "Flur EG"])
        XCTAssertEqual(smoke.rooms.map(\.status), [.ok, .ok, .ok, .now, .open])
        XCTAssertEqual(smoke.rooms.map(\.statusLabel), ["Getestet", "Getestet", "Getestet", "Jetzt", "Offen"])
        XCTAssertEqual(smoke.checks.map(\.done), [true, false])
        XCTAssertEqual(smoke.checks.map(\.label), ["Melder an der Decke montiert", "Testknopf drücken"])
        XCTAssertEqual(smoke.outcomes, ["Kein Piepton", "Piept – OK"])
        XCTAssertEqual(smoke.reminder, "Jährlich an Test erinnern")
        XCTAssertEqual(smoke.battery, "Batterien laut Hersteller bis 2037")
        XCTAssertTrue(smoke.reminderInitiallyOn)
        XCTAssertEqual(smoke.defectTarget, .newDefect)
    }

    func testCheckTitlesFeedRouteTitles() async throws {
        let titles = try await loaded().routeTitles
        XCTAssertEqual(titles.title(measureRoute), "Fensteröffnung F8")
        XCTAssertEqual(titles.title(kitchenRoute), "Küche")
        XCTAssertEqual(titles.title(panelRoute), "Zählerschrank")
        XCTAssertEqual(titles.title(smokeRoute), "Rauchwarnmelder")
        XCTAssertEqual(titles.title(.inspection(tradeId: "shell")), "Rohbau")
        XCTAssertEqual(titles.title(.inspection(tradeId: "electrical")), "Elektro")
        XCTAssertEqual(titles.backLabel(path: [.inspection(tradeId: "shell"), measureRoute], index: 1, tab: .inspections), "Rohbau")
        XCTAssertEqual(titles.backLabel(path: [.inspection(tradeId: "electrical"), kitchenRoute], index: 1, tab: .inspections), "Elektro")
    }

    func testLoadsAllM5ScreenTexts() async throws {
        let repo = try MockRepository.bundled()
        let names = ["Erd", "Rohbau", "RohbauMasse", "Zimmerer", "ElUebersicht", "ElRaum", "ElVerteiler", "ElRauchmelder"]
        for name in names {
            _ = try await repo.screenText(name, as: EmptyScreenText.self)
        }
        for name in names {
            let missing = try repository { object in
                var screens = object["screens"] as? [String: Any] ?? [:]
                screens[name] = nil
                object["screens"] = screens
            }
            do {
                _ = try await AppData.load(from: missing)
                XCTFail("Expected RepositoryError.fixtureMissing for \(name)")
            } catch {
                XCTAssertEqual(error as? RepositoryError, .fixtureMissing("screens.\(name).ios"))
            }
        }
    }

    func testLoadsAllM5Routes() async throws {
        let repo = try MockRepository.bundled()
        let root = try await repo.inspections()
        guard case let .object(inspections) = root, case let .object(byTrade)? = inspections["byTrade"] else {
            return XCTFail("byTrade missing")
        }
        var shellRoutes: [String] = []
        var electricalRoutes: [String] = []
        for (tradeId, node) in byTrade where ["earthworks", "shell", "carpentry", "electrical"].contains(tradeId) {
            guard case let .object(trade) = node, case let .array(groups)? = trade["groups"] else {
                return XCTFail("groups missing \(tradeId)")
            }
            for case let .object(group) in groups {
                guard case let .array(rows)? = group["rows"] else { return XCTFail("rows missing") }
                for case let .object(row) in rows {
                    guard case let .string(raw)? = row["route"] else { continue }
                    if tradeId == "electrical" { electricalRoutes.append(raw) } else { shellRoutes.append(raw) }
                }
            }
        }
        XCTAssertEqual(shellRoutes.sorted(), ["defects", "inspection/shell/measure"])
        XCTAssertEqual(electricalRoutes.sorted(), [
            "inspection/electrical/panel", "inspection/electrical/room/kitchen", "inspection/electrical/smoke",
        ])
        for raw in shellRoutes {
            _ = try RouteParser.inspectionLink(raw, field: "\(shellPath).groups.rows.route")
        }
        for raw in electricalRoutes {
            XCTAssertNotNil(try RouteParser.route(raw, field: "\(electricalPath).groups.rows.route"), raw)
        }
        let data = try await AppData.load(from: repo)
        let loadedRoutes = Set(data.measureChecks.keys).union(data.roomChecks.keys).union(data.panelChecks.keys)
            .union(data.smokeChecks.keys)
        for raw in shellRoutes + electricalRoutes {
            guard case let .push(route)? = try RouteParser.target(raw, field: raw) else { continue }
            XCTAssertTrue(loadedRoutes.contains(route), raw)
        }
    }

    func testLoadsElementsStaysWindowsOnly() async throws {
        let elements = try await loaded().inspectionElements
        XCTAssertEqual(Set(elements.keys), ["windows"])
    }

    // MARK: M5 error paths

    func testUnknownSocketStatusFailsLoad() async throws {
        try await expectDecoding("\(electricalPath).checks.kitchen.sockets") { object in
            editWindowsCheck(&object, "kitchen", trade: "electrical") {
                setListItem(&$0, "sockets", 0, "status", "broken")
            }
        }
    }

    func testUnknownElectricalStatusLabelFailsLoad() async throws {
        try await expectDecoding("\(electricalPath).groups.rows.statusLabel") { object in
            editTrade(&object, "electrical") { trade in
                var groups = trade["groups"] as? [[String: Any]] ?? []
                setListItem(&groups[1], "rows", 0, "statusLabel", "Unbekannt")
                trade["groups"] = groups
            }
        }
    }

    func testPhaseWithBothLabelsFailsLoad() async throws {
        try await expectDecoding("\(electricalPath).phases") { object in
            editTrade(&object, "electrical") { setListItem(&$0, "phases", 1, "statusLabel", "Abgenommen") }
        }
        try await expectDecoding("\(electricalPath).phases") { object in
            editTrade(&object, "electrical") { trade in
                var phases = trade["phases"] as? [[String: Any]] ?? []
                phases[1]["progressLabel"] = nil
                trade["phases"] = phases
            }
        }
    }

    func testUnknownBannerStatusFailsLoad() async throws {
        try await expectDecoding("inspections.byTrade.carpentry.banner") { object in
            editOverview(&object) { overview in
                var rows = overview["rows"] as? [[String: Any]] ?? []
                let index = rows.firstIndex { $0["tradeId"] as? String == "carpentry" } ?? 0
                rows[index]["status"] = "today"
                overview["rows"] = rows
            }
        }
    }

    func testCheckRouteWithoutCheckFailsLoad() async throws {
        try await expectDecoding("\(shellPath).groups.rows.route") { object in
            editTrade(&object, "shell") { trade in
                var groups = trade["groups"] as? [[String: Any]] ?? []
                setListItem(&groups[0], "rows", 0, "route", "inspection/shell/missing")
                trade["groups"] = groups
            }
        }
        try await expectDecoding("\(electricalPath).groups.rows.route") { object in
            editTrade(&object, "electrical") { trade in
                var groups = trade["groups"] as? [[String: Any]] ?? []
                setListItem(&groups[1], "rows", 0, "route", "inspection/electrical/room/attic")
                trade["groups"] = groups
            }
        }
    }

    func testShellRowTabRouteFailsLoad() async throws {
        try await expectDecoding("\(shellPath).groups.rows.route") { object in
            editTrade(&object, "shell") { trade in
                var groups = trade["groups"] as? [[String: Any]] ?? []
                setListItem(&groups[0], "rows", 0, "route", "home")
                trade["groups"] = groups
            }
        }
    }

    func testSmokeCurrentRoomMismatchFailsLoad() async throws {
        try await expectDecoding("\(electricalPath).checks.smoke.current.room") { object in
            editWindowsCheck(&object, "smoke", trade: "electrical") { check in
                var current = check["current"] as? [String: Any] ?? [:]
                current["room"] = "Flur EG"
                check["current"] = current
            }
        }
    }

    func testMissingIosMoreFailsLoad() async throws {
        try await expectDecoding("\(electricalPath).groups.more") { object in
            editTrade(&object, "electrical") { trade in
                var groups = trade["groups"] as? [[String: Any]] ?? []
                groups[1]["more"] = ["ios": NSNull(), "android": NSNull()]
                trade["groups"] = groups
            }
        }
    }

    func testMissingIosDateLabelFailsLoad() async throws {
        try await expectDecoding("\(electricalPath).phases.dateLabel") { object in
            editTrade(&object, "electrical") {
                setListItem(&$0, "phases", 0, "dateLabel", ["ios": NSNull(), "android": NSNull()])
            }
        }
    }

    func testPanelOutcomesCountFailsLoad() async throws {
        try await expectDecoding("\(electricalPath).checks.panel.current.outcomes") { object in
            editWindowsCheck(&object, "panel", trade: "electrical") { check in
                var current = check["current"] as? [String: Any] ?? [:]
                current["outcomes"] = ["Löst nicht aus"]
                check["current"] = current
            }
        }
    }

    func testUnknownCheckScreenFailsLoad() async throws {
        try await expectDecoding("\(shellPath).checks.measure.screen") { object in
            editWindowsCheck(&object, "measure", trade: "shell") { $0["screen"] = "Unbekannt" }
        }
    }

    func testStepNumberNotPositiveFailsLoad() async throws {
        try await expectDecoding("\(electricalPath).checks.kitchen.next") { object in
            editWindowsCheck(&object, "kitchen", trade: "electrical") {
                setListItem(&$0, "next", 0, "n", "03")
            }
        }
    }

    func testWrongTypeInShellOpeningFailsWithSlicePath() async throws {
        try await expectDecoding("measurements.shellOpening") { object in
            var measurements = object["measurements"] as? [String: Any] ?? [:]
            var opening = measurements["shellOpening"] as? [String: Any] ?? [:]
            opening["target"] = "1010 x 1385"
            measurements["shellOpening"] = opening
            object["measurements"] = measurements
        }
    }

    func testMissingShellOpeningFailsAsFixtureMissing() async throws {
        let repo = try repository { object in
            var measurements = object["measurements"] as? [String: Any] ?? [:]
            measurements["shellOpening"] = nil
            object["measurements"] = measurements
        }
        do {
            _ = try await AppData.load(from: repo)
            XCTFail("Expected RepositoryError.fixtureMissing")
        } catch {
            XCTAssertEqual(error as? RepositoryError, .fixtureMissing("measurements.shellOpening"))
        }
    }

    // MARK: - M6 later trades

    private let plumbingPath = "inspections.byTrade.plumbing"
    private let screedPath = "inspections.byTrade.screed"
    private let tilesPath = "inspections.byTrade.tiles"
    private let paintingPath = "inspections.byTrade.painting"
    private let slopeRoute = AppRoute.inspectionCheckItem(tradeId: "plumbing", check: "slope", item: "A3")
    private let pressureRoute = AppRoute.inspectionCheck(tradeId: "plumbing", check: "pressure")
    private let bathUpperRoute = AppRoute.inspectionRoom(tradeId: "plumbing", roomId: "bathUpper")
    private let flatnessRoute = AppRoute.inspectionCheck(tradeId: "screed", check: "flatness")
    private let readinessRoute = AppRoute.inspectionCheck(tradeId: "screed", check: "readiness")
    private let bathRoute = AppRoute.inspectionRoom(tradeId: "tiles", roomId: "bath")
    private let livingRoute = AppRoute.inspectionRoom(tradeId: "painting", roomId: "living")
    private let laterScreens = [
        "SanUebersicht", "SanGefaelle", "SanDruck", "SanBad", "EsUebersicht", "EsEbenheit", "EsBelegreife",
        "FlUebersicht", "FlBad", "MaUebersicht", "MaRaum",
    ]

    private func editMeasurement(
        _ object: inout [String: Any], _ key: String, _ edit: (inout [String: Any]) -> Void
    ) {
        var measurements = object["measurements"] as? [String: Any] ?? [:]
        var measurement = measurements[key] as? [String: Any] ?? [:]
        edit(&measurement)
        measurements[key] = measurement
        object["measurements"] = measurements
    }

    /// Edits `groups[group].rows[row]` of a trade.
    private func setRow(
        _ object: inout [String: Any], trade: String, group: Int, row: Int, _ key: String, _ value: Any
    ) {
        editTrade(&object, trade) { tradeObject in
            var groups = tradeObject["groups"] as? [[String: Any]] ?? []
            setListItem(&groups[group], "rows", row, key, value)
            tradeObject["groups"] = groups
        }
    }

    func testLoadsTradeOverviews() async throws {
        let overviews = try await loaded().tradeOverviews
        XCTAssertEqual(Set(overviews.keys), ["plumbing", "screed", "tiles", "painting"])

        let plumbing = try XCTUnwrap(overviews["plumbing"])
        XCTAssertEqual(plumbing.title, "Sanitär & Heizung")
        XCTAssertEqual(plumbing.subtitle, "Haustechnik Becker GmbH · Do, 04.02.2027")
        XCTAssertNil(plumbing.notice)
        XCTAssertEqual(plumbing.header, .phases([
            PhaseModel(title: "1 · Rohinstallation", kind: .active(progressLabel: "3 von 6 geprüft", fraction: 0.50)),
            PhaseModel(title: "2 · Fertigmontage", kind: .planned(status: "Geplant KW 14")),
        ]))
        XCTAssertEqual(plumbing.groups.map(\.title), ["Leitungen – vor dem Verschließen", "Heizung", "Bäder & Küche"])
        let plumbingRows = plumbing.groups.flatMap(\.rows)
        XCTAssertEqual(plumbingRows.map(\.name), [
            "Gefälle Abwasser", "Druckprobe Trinkwasser", "Dämmung Warmwasser", "Fußbodenheizung",
            "Wärmepumpe & Verteiler", "Bad OG", "Gäste-WC & Küche",
        ])
        XCTAssertEqual(plumbingRows[0].sub, "4 Abschnitte · 2 geprüft, 1 Mangel")
        XCTAssertEqual(plumbingRows[1].sub, "Mit Installateur · 10:00 Uhr")
        XCTAssertEqual(plumbingRows.map(\.leading), [
            .icon(.pipe), .icon(.drop), .icon(.pipe), .icon(.heat), .icon(.heat), .icon(.home), .icon(.home),
        ])
        XCTAssertEqual(plumbingRows.map(\.tone), [.defect, .today, .ok, .ok, .planned, .planned, .planned])
        XCTAssertEqual(plumbingRows.map(\.statusLabel), [
            "1 Mangel", "Heute", "In Ordnung", "In Ordnung", "Geplant", "Geplant", "Geplant",
        ])
        let plumbingTargets: [AppRoute?] = [slopeRoute, pressureRoute, nil, nil, nil, bathUpperRoute, nil]
        XCTAssertEqual(plumbingRows.map(\.target), plumbingTargets)

        let screed = try XCTUnwrap(overviews["screed"])
        XCTAssertEqual(screed.subtitle, "Estrichbau Wagner · Mo, 22.02.2027")
        XCTAssertEqual(screed.notice, "Ohne CM-Messung und Aufheizprotokoll keinen Boden verlegen lassen.")
        XCTAssertEqual(screed.header, .phases([
            PhaseModel(title: "1 · Nach dem Einbau", kind: .active(progressLabel: "3 von 4 geprüft", fraction: 0.75)),
            PhaseModel(title: "2 · Belegreife", kind: .planned(status: "Aufheizen ab 01.03.")),
        ]))
        let screedRows = screed.groups.flatMap(\.rows)
        XCTAssertEqual(screedRows.map(\.tone), [.ok, .ok, .defect, .open, .open, .required])
        XCTAssertEqual(screedRows[5].sub, "Durch den Bodenleger · vor Verlegung")
        XCTAssertTrue(screedRows.allSatisfy { $0.leading == .none })
        let screedTargets: [AppRoute?] = [nil, nil, flatnessRoute, nil, readinessRoute, readinessRoute]
        XCTAssertEqual(screedRows.map(\.target), screedTargets)

        let tiles = try XCTUnwrap(overviews["tiles"])
        XCTAssertEqual(tiles.header, .phases([
            PhaseModel(title: "1 · Abdichtung", kind: .done(status: "Abgenommen", date: "am 15.03.2027")),
            PhaseModel(title: "2 · Fliesen fertig", kind: .active(progressLabel: "2 von 4 Räumen", fraction: 0.50)),
        ]))
        let tileRows = tiles.groups.flatMap(\.rows)
        XCTAssertEqual(tileRows.map(\.tone), [.ok, .ok, .running, .ok, .ok, .open, .open])
        XCTAssertEqual(tileRows[0].sub, "Dichtband in Ecken & am Ablauf · 4 Fotos")
        XCTAssertEqual(tileRows.compactMap(\.target), [bathRoute])

        let painting = try XCTUnwrap(overviews["painting"])
        XCTAssertEqual(painting.header, .facts([
            FactModel(label: "Qualität", value: "Q3"),
            FactModel(label: "Farbtöne", value: "3 bemustert"),
            FactModel(label: "Räume", value: "3 von 9"),
        ]))
        let paintRows = painting.groups.flatMap(\.rows)
        XCTAssertEqual(paintRows.map(\.leading), [
            .swatch(.sand), .swatch(.white), .swatch(.white), .swatch(.sage), .swatch(.white), .swatch(.white),
        ])
        XCTAssertEqual(paintRows.map(\.tone), [.running, .ok, .defect, .ok, .open, .open])
        XCTAssertEqual(paintRows[0].sub, "Sand · Decke Weiß · Schritt 2 von 5")
        XCTAssertEqual(paintRows.compactMap(\.target), [livingRoute])
    }

    func testLoadsSlopeCheck() async throws {
        let data = try await loaded()
        XCTAssertEqual(Set(data.slopeChecks.keys), [slopeRoute])
        let slope = try XCTUnwrap(data.slopeChecks[slopeRoute])
        XCTAssertEqual(slope.route, slopeRoute)
        XCTAssertEqual(slope.counter, "Abschnitt 3 von 4")
        XCTAssertEqual(slope.title, "Gefälle Abwasser")
        XCTAssertEqual(slope.subtitle, "A3 · Bad OG → Fallleitung · DN 50")
        XCTAssertTrue(slope.hint.hasPrefix("Digitale Wasserwaage"))
        XCTAssertTrue(slope.warning.hasPrefix("Jetzt prüfen:"))
        XCTAssertEqual(slope.sections.map(\.id), ["A1", "A2", "A3", "A4"])
        XCTAssertEqual(slope.sections.map(\.label), ["1,4 %", "0,4 %", "jetzt", "offen"])
        XCTAssertEqual(slope.sections.map(\.status), [.ok, .defect, .now, .open])
        XCTAssertEqual(slope.defectLabel, "Mangel")
        XCTAssertEqual(slope.lengthLabel, "Länge")
        XCTAssertEqual(slope.dropLabel, "Höhenunterschied")
        XCTAssertEqual(slope.saveLabel, "Speichern")
        XCTAssertEqual(slope.photoLabel, "Foto aufnehmen")
        XCTAssertEqual(slope.schemaLevelLabel, "waagerecht")
        XCTAssertEqual(slope.schemaDeltaLabel, "Δh")
        XCTAssertEqual(slope.defectTarget, .newDefect)
        let initial = slope.initial
        XCTAssertEqual(initial.lengthText, "2,0 m")
        XCTAssertEqual(initial.dropText, "2,5 cm")
        XCTAssertEqual(initial.percentText, "1,25 %")
        XCTAssertEqual(initial.verdictText, "Ausreichend · Soll ≥ 1,0 %")
        XCTAssertEqual(initial.sollTenths, 10)
        XCTAssertEqual(initial.lengthRange, SlopeViewModel.Range(min: 5, max: 100, step: 5))
        XCTAssertEqual(initial.dropRange, SlopeViewModel.Range(min: 0, max: 300, step: 5))
    }

    func testLoadsPressureCheck() async throws {
        let data = try await loaded()
        XCTAssertEqual(Set(data.pressureChecks.keys), [pressureRoute])
        let pressure = try XCTUnwrap(data.pressureChecks[pressureRoute])
        XCTAssertEqual(pressure.route, pressureRoute)
        XCTAssertEqual(pressure.counter, "Schritt 3 von 5")
        XCTAssertEqual(pressure.title, "Druckprobe Trinkwasser")
        XCTAssertEqual(pressure.subtitle, "Gemeinsam mit dem Installateur · vor dem Verschließen")
        XCTAssertEqual(pressure.done.map(\.label), ["Leitungen sichtbar & beschriftet", "Manometer zu Beginn"])
        XCTAssertEqual(pressure.done.map(\.meta), ["3 Fotos", "10,0 bar"])
        XCTAssertEqual(pressure.current.n, 3)
        XCTAssertEqual(pressure.current.title, "Manometer am Ende ablesen")
        XCTAssertEqual(pressure.timer.fraction, 0.58, accuracy: 0.0001)
        XCTAssertEqual(pressure.timer.remaining, "12:40")
        XCTAssertEqual(pressure.timer.accessibilityLabel, "Prüfzeit zu 58 Prozent abgelaufen")
        XCTAssertEqual(pressure.timer.remainingLabel, "Noch 12:40 Min. Prüfzeit")
        XCTAssertEqual(pressure.timer.note, "Prüfdruck und Dauer legt der Installateur fest.")
        XCTAssertEqual(pressure.timer.startLabel, "Start (10:02)")
        XCTAssertEqual(pressure.timer.startValue, "10,0 bar")
        XCTAssertEqual(pressure.timer.endLabel, "Ende")
        XCTAssertEqual(pressure.timer.endPlaceholder, "– bar")
        XCTAssertEqual(pressure.next.map(\.n), [4, 5])
        XCTAssertEqual(pressure.next.map(\.tag), [nil, "Pflicht"])
        XCTAssertEqual(pressure.photoLabel, "Manometer fotografieren")
    }

    func testLoadsObjectCheck() async throws {
        let data = try await loaded()
        XCTAssertEqual(Set(data.objectChecks.keys), [bathUpperRoute])
        let objects = try XCTUnwrap(data.objectChecks[bathUpperRoute])
        XCTAssertEqual(objects.route, bathUpperRoute)
        XCTAssertEqual(objects.trailing, "Fertigmontage")
        XCTAssertEqual(objects.counter, "5 Objekte · 1 von 5 geprüft")
        XCTAssertEqual(objects.title, "Bad OG")
        XCTAssertEqual(objects.objects.map(\.name), ["WC", "Waschtisch", "Dusche", "Heizkörper", "Lüfter"])
        XCTAssertEqual(objects.objects.map(\.state), [.done, .now, .open, .open, .open])
        XCTAssertEqual(objects.heading, "Waschtisch")
        XCTAssertEqual(objects.checks.map(\.ok), [true, true, false, false, false])
        XCTAssertEqual(objects.checks[2].hint, "Mit Papiertuch unten abwischen – muss trocken bleiben.")
        XCTAssertEqual(objects.checks[0].label, "Warm links, kalt rechts")
        XCTAssertTrue(objects.nextHint.hasPrefix("Als Nächstes Dusche:"))
        XCTAssertEqual(objects.tabsLabel, "Objekte")
    }

    func testLoadsFlatnessCheck() async throws {
        let data = try await loaded()
        XCTAssertEqual(Set(data.flatnessChecks.keys), [flatnessRoute])
        let flatness = try XCTUnwrap(data.flatnessChecks[flatnessRoute])
        XCTAssertEqual(flatness.route, flatnessRoute)
        XCTAssertEqual(flatness.counter, "Raum 5 von 7")
        XCTAssertEqual(flatness.title, "Ebenheit · Wohnen")
        XCTAssertEqual(
            flatness.subtitle(limitText: flatness.initial.limitText),
            "Grenzwert laut Vertrag: 4 mm Spalt unter 2-m-Latte")
        XCTAssertEqual(
            flatness.instruction,
            "Richtlatte in mehreren Richtungen auflegen. Größten Spalt darunter mit dem Messkeil ablesen und eintragen.")
        XCTAssertEqual(flatness.stepperLabelPrefix, "Spalt Messpunkt M")
        XCTAssertEqual(flatness.stepperAccessibilityNoun, "Spalt")
        XCTAssertEqual(flatness.saveLabel, "Messpunkt speichern")
        XCTAssertEqual(flatness.photoLabel, "Foto aufnehmen")
        XCTAssertEqual(String(format: flatness.measuredFormat, 3), "Gemessen (3)")
        XCTAssertEqual(flatness.defectTarget, .newDefect)
        let initial = flatness.initial
        XCTAssertEqual(initial.limit, 4)
        XCTAssertEqual(initial.gapText, "3 mm")
        XCTAssertEqual(initial.pointText(2), "M3 · 6 mm")
        XCTAssertEqual(initial.summaryText, "1 Messpunkt über Grenzwert → Mangel erfassen")
    }

    func testLoadsReadinessCheck() async throws {
        let data = try await loaded()
        XCTAssertEqual(Set(data.readinessChecks.keys), [readinessRoute])
        let readiness = try XCTUnwrap(data.readinessChecks[readinessRoute])
        XCTAssertEqual(readiness.route, readinessRoute)
        XCTAssertEqual(readiness.screenTitle, "Aufheizen & Belegreife")
        XCTAssertEqual(readiness.cardTitle, "Aufheizprotokoll")
        XCTAssertEqual(readiness.counter, "Tag 6 von 21")
        XCTAssertTrue(readiness.text.hasPrefix("Plan vom Heizungsbauer."))
        let chart = readiness.chart
        XCTAssertEqual(chart.bars.count, 21)
        XCTAssertEqual(chart.bars.prefix(8).map(\.height), [40, 40, 40, 48, 56, 56, 64, 72])
        XCTAssertEqual(chart.bars.last?.height, 40)
        XCTAssertEqual(chart.bars.filter { $0.kind == .recorded }.count, 5)
        XCTAssertEqual(chart.bars.filter { $0.kind == .today }.count, 1)
        XCTAssertEqual(chart.bars.filter { $0.kind == .planned }.count, 15)
        XCTAssertEqual(chart.bars[4].kind, .recorded)
        XCTAssertEqual(chart.bars[5].kind, .today)
        XCTAssertEqual(chart.bars[6].kind, .planned)
        XCTAssertEqual(chart.accessibilityLabel, "Vorlauftemperatur je Tag, Tag 1 bis 5 erfasst, heute Tag 6")
        XCTAssertEqual([chart.start, chart.max, chart.end], ["Tag 1", "Max. 45 °C", "Tag 21"])
        XCTAssertEqual([chart.today, chart.plan, chart.action], ["Heute laut Plan", "35 °C Vorlauf", "Tag 6 eintragen"])
        XCTAssertEqual(readiness.cm.title, "CM-Messung")
        XCTAssertEqual(readiness.cm.tag, "Pflicht")
        XCTAssertEqual(readiness.cm.note, "Bodenbelag erst nach Freigabe")
        XCTAssertEqual(readiness.cm.rows.map(\.label), ["Messstellen markiert", "Messung durchgeführt", "Ergebnis unter Grenzwert"])
        XCTAssertEqual(readiness.cm.rows.map(\.meta), ["wegen Fußbodenheizung", "geplant 19.03.", "– CM-%"])
        XCTAssertEqual(readiness.cm.rows.map(\.done), [true, false, false])
    }

    func testLoadsTileCheck() async throws {
        let data = try await loaded()
        XCTAssertEqual(Set(data.tileChecks.keys), [bathRoute])
        let tiles = try XCTUnwrap(data.tileChecks[bathRoute])
        XCTAssertEqual(tiles.route, bathRoute)
        XCTAssertEqual(tiles.counter, "Schritt 3 von 6")
        XCTAssertEqual(tiles.subtitle, "Boden 9 m² · Wand 28 m² · Feinsteinzeug 60×60")
        XCTAssertEqual(tiles.title, "Bad OG")
        XCTAssertEqual(tiles.done, ["Fliese & Verlegemuster wie bemustert", "Fugenbild gleichmäßig, Fugen voll"])
        XCTAssertEqual(tiles.current.n, 3)
        XCTAssertEqual(tiles.current.title, "Hohlstellen abklopfen")
        XCTAssertEqual(tiles.next.map(\.n), [4, 5, 6])
        XCTAssertEqual(tiles.columns, 8)
        XCTAssertEqual(tiles.tiles.count, 24)
        XCTAssertEqual(tiles.tiles.filter(\.hollow).map(\.id), ["tile-3", "tile-4"])
        XCTAssertEqual(tiles.tiles[2].label, "hohl")
        XCTAssertNil(tiles.tiles[0].label)
        XCTAssertEqual(tiles.tiles[2].accessibilityLabel, "Fliese 3: hohl")
        XCTAssertEqual(tiles.wallLabel, "↑ Wand mit Dusche")
        XCTAssertEqual(tiles.markHint, "klingt hohl")
        XCTAssertEqual(tiles.gridAccessibilityLabel, "Fliesenraster Boden, oben die Wand mit Dusche, unten die Tür")
        XCTAssertEqual(tiles.result, "2 Fliesen klingen hohl (vor der Dusche)")
        XCTAssertEqual(tiles.defectLabel, "Als Mangel")
        XCTAssertEqual(tiles.noneLabel, "Keine Hohlstellen")
        XCTAssertEqual(tiles.defectTarget, .newDefect)
    }

    func testLoadsPaintCheck() async throws {
        let data = try await loaded()
        XCTAssertEqual(Set(data.paintChecks.keys), [livingRoute])
        let paint = try XCTUnwrap(data.paintChecks[livingRoute])
        XCTAssertEqual(paint.route, livingRoute)
        XCTAssertEqual(paint.counter, "Schritt 2 von 5")
        XCTAssertEqual(paint.subtitle, "Wände Sand · Decke Weiß · Qualitätsstufe Q3")
        XCTAssertEqual(paint.title, "Wohnen / Essen")
        XCTAssertEqual(paint.done, ["Farbton wie bemustert"])
        XCTAssertEqual(paint.doneSwatch, .sand)
        XCTAssertEqual(paint.current.n, 2)
        XCTAssertEqual(paint.current.title, "Streiflicht-Test")
        XCTAssertEqual(paint.walls.map(\.name), ["Nord ✓", "Ost ✓", "Süd", "West", "Decke"])
        XCTAssertEqual(paint.walls.map(\.state), [.done, .done, .now, .open, .open])
        XCTAssertEqual(paint.wallsLabel, "Flächen")
        XCTAssertEqual(paint.next.map(\.n), [3, 4, 5])
        XCTAssertEqual(paint.defectLabel, "Mangel")
        XCTAssertEqual(paint.okLabel, "In Ordnung")
        XCTAssertEqual(paint.photoLabel, "Foto aufnehmen")
        XCTAssertEqual(paint.defectTarget, .newDefect)
    }

    func testLaterTradeTitlesFeedRouteTitles() async throws {
        let titles = try await loaded().routeTitles
        XCTAssertEqual(titles.title(slopeRoute), "Gefälle Abwasser")
        XCTAssertEqual(titles.title(pressureRoute), "Druckprobe Trinkwasser")
        XCTAssertEqual(titles.title(bathUpperRoute), "Bad OG")
        XCTAssertEqual(titles.title(flatnessRoute), "Ebenheit · Wohnen")
        XCTAssertEqual(titles.title(readinessRoute), "Aufheizen & Belegreife")
        XCTAssertEqual(titles.title(bathRoute), "Bad OG")
        XCTAssertEqual(titles.title(livingRoute), "Wohnen / Essen")
        let plumbing = AppRoute.inspection(tradeId: "plumbing")
        XCTAssertEqual(titles.title(plumbing), "Sanitär & Heizung")
        XCTAssertEqual(titles.backLabel(path: [plumbing, pressureRoute], index: 1, tab: .inspections), "Sanitär")
        XCTAssertEqual(titles.backLabel(path: [plumbing, slopeRoute], index: 1, tab: .inspections), "Sanitär")
        XCTAssertEqual(
            titles.backLabel(path: [.inspection(tradeId: "screed"), flatnessRoute], index: 1, tab: .inspections),
            "Estrich")
        XCTAssertEqual(
            titles.backLabel(path: [.inspection(tradeId: "tiles"), bathRoute], index: 1, tab: .inspections), "Fliesen")
        XCTAssertEqual(
            titles.backLabel(path: [.inspection(tradeId: "painting"), livingRoute], index: 1, tab: .inspections),
            "Maler")
        XCTAssertEqual(titles.backLabel(path: [plumbing], index: 0, tab: .inspections), "Abnahmen")
    }

    func testLoadsAllM6ScreenTexts() async throws {
        let repo = try MockRepository.bundled()
        for name in laterScreens {
            _ = try await repo.screenText(name, as: EmptyScreenText.self)
        }
        for name in laterScreens {
            let missing = try repository { object in
                var screens = object["screens"] as? [String: Any] ?? [:]
                screens[name] = nil
                object["screens"] = screens
            }
            do {
                _ = try await AppData.load(from: missing)
                XCTFail("Expected RepositoryError.fixtureMissing for \(name)")
            } catch {
                XCTAssertEqual(error as? RepositoryError, .fixtureMissing("screens.\(name).ios"))
            }
        }
    }

    func testLoadsAllM6Routes() async throws {
        let repo = try MockRepository.bundled()
        let root = try await repo.inspections()
        guard case let .object(inspections) = root, case let .object(byTrade)? = inspections["byTrade"] else {
            return XCTFail("byTrade missing")
        }
        var raws: [String] = []
        for tradeId in ["plumbing", "screed", "tiles", "painting"] {
            guard case let .object(trade)? = byTrade[tradeId], case let .array(groups)? = trade["groups"] else {
                return XCTFail("groups missing \(tradeId)")
            }
            for case let .object(group) in groups {
                guard case let .array(rows)? = group["rows"] else { return XCTFail("rows missing") }
                for case let .object(row) in rows {
                    if case let .string(raw)? = row["route"] { raws.append(raw) }
                }
            }
        }
        XCTAssertEqual(raws.sorted(), [
            "inspection/painting/room/living", "inspection/plumbing/pressure", "inspection/plumbing/room/bathUpper",
            "inspection/plumbing/slope/A3", "inspection/screed/flatness", "inspection/screed/readiness",
            "inspection/screed/readiness", "inspection/tiles/room/bath",
        ])
        let data = try await AppData.load(from: repo)
        let loadedRoutes = Set(data.slopeChecks.keys).union(data.pressureChecks.keys).union(data.flatnessChecks.keys)
            .union(data.readinessChecks.keys).union(data.objectChecks.keys).union(data.tileChecks.keys)
            .union(data.paintChecks.keys)
        XCTAssertEqual(loadedRoutes.count, 7)
        for raw in raws {
            let route = try XCTUnwrap(try RouteParser.route(raw, field: raw), raw)
            XCTAssertTrue(loadedRoutes.contains(route), raw)
        }
    }

    // MARK: M6 error paths

    func testUnknownOverviewStatusLabelFailsLoad() async throws {
        try await expectDecoding("\(plumbingPath).groups.rows.statusLabel") { object in
            setRow(&object, trade: "plumbing", group: 0, row: 2, "statusLabel", "Unbekannt")
        }
        try await expectDecoding("\(plumbingPath).groups.rows.statusLabel") { object in
            setRow(&object, trade: "plumbing", group: 0, row: 0, "statusLabel", "0 Mangel")
        }
    }

    func testOverviewWithPhasesAndFactsFailsLoad() async throws {
        try await expectDecoding(plumbingPath) { object in
            editTrade(&object, "plumbing") { $0["facts"] = [["label": "Qualität", "value": "Q3"]] }
        }
        try await expectDecoding(plumbingPath) { object in
            editTrade(&object, "plumbing") { $0["phases"] = nil }
        }
    }

    func testPhaseWithBothLabelsInTilesFailsLoad() async throws {
        try await expectDecoding("\(tilesPath).phases") { object in
            editTrade(&object, "tiles") { setListItem(&$0, "phases", 1, "statusLabel", "Geplant") }
        }
    }

    func testPlumbingIconShapeMismatchFailsLoad() async throws {
        try await expectDecoding("\(plumbingPath).groups") { object in
            editTrade(&object, "plumbing") { trade in
                var groups = trade["groups"] as? [[String: Any]] ?? []
                var rows = groups[1]["rows"] as? [[String: Any]] ?? []
                rows.removeLast()
                groups[1]["rows"] = rows
                trade["groups"] = groups
            }
        }
    }

    func testUnknownPaintWordFailsLoad() async throws {
        try await expectDecoding("\(paintingPath).groups.rows.sub") { object in
            setRow(&object, trade: "painting", group: 0, row: 0, "sub", ["ios": "Rot · Schritt 2 von 5", "android": "Rot"])
        }
    }

    func testUnparseableSectionLabelFailsLoad() async throws {
        try await expectDecoding("measurements.drainSlope.sections") { object in
            editMeasurement(&object, "drainSlope") { setListItem(&$0, "sections", 0, "label", "viel") }
        }
    }

    func testSlopeCurrentSectionMismatchFailsLoad() async throws {
        try await expectDecoding("\(plumbingPath).checks.slope") { object in
            editWindowsCheck(&object, "slope", trade: "plumbing") { $0["subtitle"] = "A2 · Bad OG → Fallleitung · DN 50" }
        }
        try await expectDecoding("\(plumbingPath).checks.slope") { object in
            editWindowsCheck(&object, "slope", trade: "plumbing") {
                $0["counter"] = ["ios": "Abschnitt 2 von 4", "android": "2 / 4"]
            }
        }
    }

    func testSlopeMeasurementKeyMismatchFailsLoad() async throws {
        try await expectDecoding("\(plumbingPath).checks.slope.measurementKey") { object in
            editWindowsCheck(&object, "slope", trade: "plumbing") { $0["measurementKey"] = "screedFlatness" }
        }
        try await expectDecoding("\(screedPath).checks.readiness.measurementKey") { object in
            editWindowsCheck(&object, "readiness", trade: "screed") { $0["measurementKey"] = "drainSlope" }
        }
        try await expectDecoding("\(screedPath).checks.flatness.measurementKey") { object in
            editWindowsCheck(&object, "flatness", trade: "screed") { $0["measurementKey"] = "drainSlope" }
        }
    }

    func testUnknownCheckItemFailsLoad() async throws {
        try await expectDecoding("\(plumbingPath).groups.rows.route") { object in
            setRow(&object, trade: "plumbing", group: 0, row: 0, "route", "inspection/plumbing/slope/A9")
        }
        try await expectDecoding("\(screedPath).groups.rows.route") { object in
            setRow(&object, trade: "screed", group: 0, row: 2, "route", "inspection/screed/missing")
        }
        try await expectDecoding("\(tilesPath).groups.rows.route") { object in
            setRow(&object, trade: "tiles", group: 1, row: 0, "route", "inspection/tiles/room/attic")
        }
    }

    func testTwoSelectedObjectsFailsLoad() async throws {
        try await expectDecoding("\(plumbingPath).checks.bathUpper.objects") { object in
            editWindowsCheck(&object, "bathUpper", trade: "plumbing") { setListItem(&$0, "objects", 0, "selected", true) }
        }
        try await expectDecoding("\(plumbingPath).checks.bathUpper.objects") { object in
            editWindowsCheck(&object, "bathUpper", trade: "plumbing") { setListItem(&$0, "objects", 1, "done", true) }
        }
    }

    func testSelectedWallWithTickFailsLoad() async throws {
        try await expectDecoding("\(paintingPath).checks.living.walls") { object in
            editWindowsCheck(&object, "living", trade: "painting") { setListItem(&$0, "walls", 2, "name", "Süd ✓") }
        }
        try await expectDecoding("\(paintingPath).checks.living.walls") { object in
            editWindowsCheck(&object, "living", trade: "painting") { setListItem(&$0, "walls", 0, "selected", true) }
        }
    }

    func testUnknownTileStatusFailsLoad() async throws {
        try await expectDecoding("\(tilesPath).checks.bath.tiles") { object in
            editWindowsCheck(&object, "bath", trade: "tiles") { setListItem(&$0, "tiles", 0, "status", "broken") }
        }
    }

    func testWrongTileCountFailsLoad() async throws {
        try await expectDecoding("\(tilesPath).checks.bath.tiles") { object in
            editWindowsCheck(&object, "bath", trade: "tiles") { check in
                var tiles = check["tiles"] as? [[String: Any]] ?? []
                tiles.removeLast()
                check["tiles"] = tiles
            }
        }
    }

    func testStepContinuityFailsLoad() async throws {
        try await expectDecoding("\(plumbingPath).checks.pressure.next") { object in
            editWindowsCheck(&object, "pressure", trade: "plumbing") { setListItem(&$0, "next", 0, "n", "5") }
        }
        try await expectDecoding("\(tilesPath).checks.bath.next") { object in
            editWindowsCheck(&object, "bath", trade: "tiles") { setListItem(&$0, "next", 2, "n", "7") }
        }
        try await expectDecoding("\(paintingPath).checks.living.next") { object in
            editWindowsCheck(&object, "living", trade: "painting") { check in
                var current = check["current"] as? [String: Any] ?? [:]
                current["n"] = "3"
                check["current"] = current
            }
        }
        try await expectDecoding("\(paintingPath).checks.living.current") { object in
            editWindowsCheck(&object, "living", trade: "painting") { check in
                var current = check["current"] as? [String: Any] ?? [:]
                current["n"] = "02"
                check["current"] = current
            }
        }
    }

    func testHeatingTodayOutOfRangeFailsLoad() async throws {
        try await expectDecoding("measurements.screedHeating") { object in
            editMeasurement(&object, "screedHeating") { $0["todayDay"] = 22 }
        }
        try await expectDecoding("measurements.screedHeating") { object in
            editMeasurement(&object, "screedHeating") { $0["recordedDays"] = 6 }
        }
    }

    func testUnknownM6ScreenFailsLoad() async throws {
        try await expectDecoding("\(plumbingPath).checks.pressure.screen") { object in
            editWindowsCheck(&object, "pressure", trade: "plumbing") { $0["screen"] = "Unbekannt" }
        }
        try await expectDecoding("\(plumbingPath).checks.pressure.screen") { object in
            editWindowsCheck(&object, "pressure", trade: "plumbing") { $0["screen"] = "ElRaum" }
        }
    }

    func testMissingIosTilesDateLabelFailsLoad() async throws {
        try await expectDecoding("\(tilesPath).phases.dateLabel") { object in
            editTrade(&object, "tiles") {
                setListItem(&$0, "phases", 0, "dateLabel", ["ios": NSNull(), "android": NSNull()])
            }
        }
    }

    func testMissingIosBathSubtitleFailsLoad() async throws {
        try await expectDecoding("\(plumbingPath).checks.bathUpper.subtitle") { object in
            editWindowsCheck(&object, "bathUpper", trade: "plumbing") {
                $0["subtitle"] = ["ios": NSNull(), "android": NSNull()]
            }
        }
    }

    func testWrongTypeInDrainSlopeFailsWithSlicePath() async throws {
        try await expectDecoding("measurements.drainSlope") { object in
            editMeasurement(&object, "drainSlope") { $0["lengthDm"] = "20" }
        }
    }

    func testMissingDrainSlopeFailsAsFixtureMissing() async throws {
        let repo = try repository { object in
            var measurements = object["measurements"] as? [String: Any] ?? [:]
            measurements["drainSlope"] = nil
            object["measurements"] = measurements
        }
        do {
            _ = try await AppData.load(from: repo)
            XCTFail("Expected RepositoryError.fixtureMissing")
        } catch {
            XCTAssertEqual(error as? RepositoryError, .fixtureMissing("measurements.drainSlope"))
        }
    }

    func testResolvedDefectsComeFromRepository() async throws {
        let resolved = try await MockRepository.bundled().resolvedDefects()
        XCTAssertEqual(resolved.count, "3")
    }
}
