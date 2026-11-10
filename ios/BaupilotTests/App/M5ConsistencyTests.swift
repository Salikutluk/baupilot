import XCTest
@testable import Baupilot

/// Figures that appear in more than one place of the fixture must agree (M5 I3, I4).
final class M5ConsistencyTests: XCTestCase {
    private func loaded() async throws -> AppData {
        try await AppData.load(from: MockRepository.bundled())
    }

    private let kitchenRoute = AppRoute.inspectionRoom(tradeId: "electrical", roomId: "kitchen")
    private let panelRoute = AppRoute.inspectionCheck(tradeId: "electrical", check: "panel")
    private let smokeRoute = AppRoute.inspectionCheck(tradeId: "electrical", check: "smoke")

    /// The first integer of a text ("3 von 5 getestet" gives 3).
    private func leadingNumber(_ text: String) throws -> Int {
        let digits = text.prefix { $0.isNumber }
        return try XCTUnwrap(Int(digits), text)
    }

    /// The integer after the last "von" ("Schritt 2 von 5" gives 5).
    private func total(_ text: String) throws -> Int {
        try XCTUnwrap(text.components(separatedBy: " von ").last.flatMap { Int($0.prefix { $0.isNumber }) }, text)
    }

    func testElectricalFiguresAreConsistent() async throws {
        let data = try await loaded()
        let overview = try XCTUnwrap(data.electricalOverviews["electrical"])
        let rows = overview.groups.flatMap(\.rows)
        let room = try XCTUnwrap(data.roomChecks[kitchenRoute])
        let panel = try XCTUnwrap(data.panelChecks[panelRoute])
        let smoke = try XCTUnwrap(data.smokeChecks[smokeRoute])

        // The date of the Termin in the subtitle falls on the weekday that is printed with it.
        let termin = try XCTUnwrap(overview.subtitle.components(separatedBy: " · ").last)
        let parts = termin.components(separatedBy: ", ")
        XCTAssertEqual(parts.first, "Mo")
        let date = try XCTUnwrap(parts.last).split(separator: ".").compactMap { Int($0) }
        XCTAssertEqual(date.count, 3)
        let weekdays = ["Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"]
        XCTAssertEqual(
            weekdays[CalendarDay(year: date[2], month: date[1], day: date[0]).weekdayIndex], parts.first)
        XCTAssertEqual(CalendarDay(year: 2027, month: 4, day: 26).weekdayIndex, 0)

        // Rohinstallation: status and date read as one sentence.
        guard case let .done(status, doneDate) = overview.phases[0].kind else { return XCTFail("phase 0 is not done") }
        XCTAssertEqual("\(status) \(try XCTUnwrap(doneDate))", "Abgenommen am 29.01.2027")

        // Rauchwarnmelder row: tested rooms / all rooms.
        let smokeRow = try XCTUnwrap(rows.first { $0.name == "Rauchwarnmelder" })
        XCTAssertEqual(try leadingNumber(smokeRow.sub), smoke.rooms.filter { $0.status == .ok }.count)
        XCTAssertEqual(try total(smokeRow.sub), smoke.rooms.count)
        XCTAssertEqual(smokeRow.target, smokeRoute)

        // Küche row: "Schritt 2 von 5", and the steps of the room add up to 5.
        let kitchenRow = try XCTUnwrap(rows.first { $0.name == "Küche" })
        XCTAssertTrue(kitchenRow.sub.hasSuffix("Schritt 2 von 5"))
        XCTAssertEqual(room.done.count + 1 + room.next.count, 5)
        XCTAssertEqual(room.counter, "Schritt \(room.current.n) von 5")
        XCTAssertEqual(room.next.map(\.n), [3, 4, 5])

        // Zählerschrank: "5 Prüfschritte" = done + current + next.
        let panelRow = try XCTUnwrap(rows.first { $0.name == "Zählerschrank & FI-Test" })
        XCTAssertEqual(try leadingNumber(panelRow.sub), panel.done.count + 1 + panel.next.count)
        XCTAssertEqual(panel.counter, "Schritt \(panel.current.n) von 5")

        // Sockets: tested (ok + defect) matches "5 von 8 getestet"; the done meta "8 / 8" is the socket count.
        let tested = room.sockets.filter { $0.status == .ok || $0.status == .defect }.count
        XCTAssertEqual(try leadingNumber(room.progressLabel), tested)
        XCTAssertEqual(try total(room.progressLabel), room.sockets.count)
        XCTAssertEqual(room.done[0].meta, "\(room.sockets.count) / \(room.sockets.count)")

        // The notes name the sockets they speak of.
        let defects = room.sockets.filter { $0.status == .defect }
        XCTAssertEqual(defects.count, 1)
        XCTAssertTrue(room.defectNote.hasPrefix(defects[0].id))
        let current = room.sockets.filter { $0.status == .now }
        XCTAssertEqual(current.count, 1)
        XCTAssertTrue(room.okNote.hasPrefix(current[0].id))

        // The defect outcome of the panel names the FI that is still to be tested.
        let openFis = panel.fis.filter { $0.tone == .open }
        XCTAssertEqual(openFis.count, 1)
        let fiNumber = try XCTUnwrap(openFis[0].name.split(separator: " ").dropFirst().first)
        XCTAssertEqual(panel.outcomes[1], "FI \(fiNumber) ausgelöst")
    }

    func testProtocolFiguresMatchOverview() async throws {
        let data = try await loaded()
        let overviewRows = data.inspections.rows
        let counts = ["earthworks": 9, "shell": 9, "carpentry": 8]
        for (tradeId, expected) in counts {
            let model = try XCTUnwrap(data.tradeProtocols[tradeId], tradeId)
            XCTAssertEqual(model.groups.flatMap(\.rows).count, expected, tradeId)
            let row = try XCTUnwrap(overviewRows.first { $0.tradeId == tradeId })
            XCTAssertEqual(try leadingNumber(row.subtitle), expected, tradeId)
            XCTAssertTrue(row.subtitle.hasPrefix("\(expected) Prüfschritte"), tradeId)
        }

        // The shell banner counts the defect rows.
        let shell = try XCTUnwrap(data.tradeProtocols["shell"])
        let defectRows = shell.groups.flatMap(\.rows).filter { $0.mark == .defect }
        XCTAssertEqual(try leadingNumber(shell.banner.text), defectRows.count)
        XCTAssertTrue(shell.banner.text.hasPrefix("\(defectRows.count) Mangel offen"))

        // "12 Öffnungen" and "Öffnung 8 von 12" agree, 7 of them are measured, and the title names opening 8.
        let measure = try XCTUnwrap(data.measureChecks[.inspectionCheck(tradeId: "shell", check: "measure")])
        let openings = try leadingNumber(try XCTUnwrap(
            shell.groups.flatMap(\.rows).first { $0.meta.hasSuffix("Öffnungen") }?.meta))
        XCTAssertEqual(openings, 12)
        XCTAssertEqual(measure.counter, "Öffnung 8 von \(openings)")
        let position = try leadingNumber(measure.counter.replacingOccurrences(of: "Öffnung ", with: ""))
        XCTAssertEqual(position, 8)
        XCTAssertEqual(measure.measured.count, position - 1)
        XCTAssertEqual(measure.title, "Fensteröffnung F\(position)")
        XCTAssertEqual(measure.measured.last, "F\(position - 1)")
    }
}
