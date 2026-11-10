import XCTest
@testable import Baupilot

/// Figures that appear in more than one place of the M6 fixture must agree (M6 §9).
final class M6ConsistencyTests: XCTestCase {
    private func loaded() async throws -> AppData {
        try await AppData.load(from: MockRepository.bundled())
    }

    private let slopeRoute = AppRoute.inspectionCheckItem(tradeId: "plumbing", check: "slope", item: "A3")
    private let pressureRoute = AppRoute.inspectionCheck(tradeId: "plumbing", check: "pressure")
    private let bathUpperRoute = AppRoute.inspectionRoom(tradeId: "plumbing", roomId: "bathUpper")
    private let flatnessRoute = AppRoute.inspectionCheck(tradeId: "screed", check: "flatness")
    private let readinessRoute = AppRoute.inspectionCheck(tradeId: "screed", check: "readiness")
    private let bathRoute = AppRoute.inspectionRoom(tradeId: "tiles", roomId: "bath")
    private let livingRoute = AppRoute.inspectionRoom(tradeId: "painting", roomId: "living")

    private let weekdays = ["Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"]

    /// The integers of a text, in order ("Schritt 3 von 6" gives [3, 6]).
    private func numbers(_ text: String) -> [Int] {
        text.split { !$0.isNumber }.compactMap { Int($0) }
    }

    private func rows(_ model: TradeOverviewModel) -> [TradeOverviewModel.Row] {
        model.groups.flatMap(\.rows)
    }

    /// Splits "Betrieb · Mo, 22.02.2027" into the weekday and the day.
    private func termin(_ subtitle: String) throws -> (weekday: String, day: CalendarDay) {
        let last = try XCTUnwrap(subtitle.components(separatedBy: " · ").last)
        let parts = last.components(separatedBy: ", ")
        let date = try XCTUnwrap(parts.last).split(separator: ".").compactMap { Int($0) }
        XCTAssertEqual(date.count, 3, subtitle)
        return (try XCTUnwrap(parts.first), CalendarDay(year: date[2], month: date[1], day: date[0]))
    }

    func testScreedDatesAreConsistent() async throws {
        let data = try await loaded()
        let overview = try XCTUnwrap(data.tradeOverviews["screed"])
        let readiness = try XCTUnwrap(data.readinessChecks[readinessRoute])
        let heating = try await MockRepository.bundled().measurementSlice(ScreedHeatingDto.self, key: "screedHeating")

        let einbau = try termin(overview.subtitle)
        XCTAssertEqual(einbau.weekday, "Mo")
        XCTAssertEqual(einbau.day.weekdayIndex, 0)
        XCTAssertEqual(weekdays[einbau.day.weekdayIndex], einbau.weekday)
        XCTAssertEqual(einbau.day, CalendarDay(year: 2027, month: 2, day: 22))

        guard case let .phases(phases) = overview.header,
            case let .planned(status) = phases[1].kind
        else { return XCTFail("phase 1 is not planned") }
        XCTAssertTrue(status.hasSuffix("ab 01.03."), status)
        let aufheiz = try XCTUnwrap(rows(overview).first { $0.name == "Aufheizprotokoll" })
        XCTAssertTrue(aufheiz.sub.hasSuffix("ab 01.03."), aufheiz.sub)

        let count = heating.planCelsius.count
        XCTAssertEqual(readiness.counter, "Tag \(heating.todayDay) von \(count)")
        XCTAssertEqual(readiness.chart.end, "Tag \(count)")
        XCTAssertEqual(readiness.chart.max, "Max. \(try XCTUnwrap(heating.planCelsius.max())) °C")
        XCTAssertEqual(readiness.chart.plan, "\(heating.planCelsius[heating.todayDay - 1]) °C Vorlauf")
        XCTAssertEqual(readiness.chart.action, "Tag \(heating.todayDay) eintragen")
        XCTAssertEqual(
            readiness.chart.accessibilityLabel,
            "Vorlauftemperatur je Tag, Tag 1 bis \(heating.recordedDays) erfasst, heute Tag \(heating.todayDay)")
        XCTAssertEqual(readiness.cm.rows[1].meta, "geplant 19.03.")
    }

    func testOverviewDatesHaveCorrectWeekdays() async throws {
        let data = try await loaded()
        let expected: [(String, String, CalendarDay)] = [
            ("plumbing", "Do", CalendarDay(year: 2027, month: 2, day: 4)),
            ("tiles", "Mo", CalendarDay(year: 2027, month: 3, day: 22)),
            ("painting", "Mo", CalendarDay(year: 2027, month: 4, day: 12)),
        ]
        for (tradeId, weekday, day) in expected {
            let overview = try XCTUnwrap(data.tradeOverviews[tradeId])
            let parsed = try termin(overview.subtitle)
            XCTAssertEqual(parsed.weekday, weekday, tradeId)
            XCTAssertEqual(parsed.day, day, tradeId)
            XCTAssertEqual(weekdays[parsed.day.weekdayIndex], weekday, tradeId)
        }
    }

    func testPhaseFractionsMatchProgressLabels() async throws {
        let data = try await loaded()
        let expected: [(String, Int, Int)] = [("plumbing", 3, 6), ("screed", 3, 4), ("tiles", 2, 4)]
        for (tradeId, done, total) in expected {
            let overview = try XCTUnwrap(data.tradeOverviews[tradeId])
            guard case let .phases(phases) = overview.header else { return XCTFail("\(tradeId) has no phases") }
            let active = phases.compactMap { phase -> (String, Double)? in
                if case let .active(label, fraction) = phase.kind { return (label, fraction) }
                return nil
            }
            XCTAssertEqual(active.count, 1, tradeId)
            XCTAssertEqual(numbers(active[0].0), [done, total], tradeId)
            XCTAssertEqual(active[0].1, Double(done) / Double(total), accuracy: 0.0001, tradeId)
        }
    }

    func testSlopeFiguresMatchOverview() async throws {
        let data = try await loaded()
        let overview = try XCTUnwrap(data.tradeOverviews["plumbing"])
        let slope = try XCTUnwrap(data.slopeChecks[slopeRoute])
        let row = try XCTUnwrap(rows(overview).first { $0.name == "Gefälle Abwasser" })

        let measured = slope.sections.filter { $0.status == .ok || $0.status == .defect }.count
        let defects = slope.sections.filter { $0.status == .defect }.count
        XCTAssertEqual(row.sub, "\(slope.sections.count) Abschnitte · \(measured) geprüft, \(defects) Mangel")
        XCTAssertEqual(row.statusLabel, "\(defects) Mangel")
        XCTAssertEqual(row.target, slopeRoute)
        XCTAssertTrue(slope.initial.isSufficient)
        XCTAssertEqual(slope.initial.dropMm, 25)
        XCTAssertEqual(slope.initial.lenDm, 20)
    }

    func testFlatnessFiguresMatchOverview() async throws {
        let data = try await loaded()
        let overview = try XCTUnwrap(data.tradeOverviews["screed"])
        let flatness = try XCTUnwrap(data.flatnessChecks[flatnessRoute])
        let row = try XCTUnwrap(rows(overview).first { $0.name == "Ebenheit" })

        let rowNumbers = numbers(row.sub.components(separatedBy: " · ").last ?? "")
        let counterNumbers = numbers(flatness.counter)
        XCTAssertEqual(rowNumbers.count, 2)
        XCTAssertEqual(counterNumbers.count, 2)
        XCTAssertEqual(counterNumbers[0], rowNumbers[0] + 1)
        XCTAssertEqual(counterNumbers[1], rowNumbers[1])
        XCTAssertEqual(row.statusLabel, "\(flatness.initial.overCount) Mangel")
        XCTAssertEqual(row.target, flatnessRoute)
    }

    func testPressureFiguresAreConsistent() async throws {
        let data = try await loaded()
        let pressure = try XCTUnwrap(data.pressureChecks[pressureRoute])
        XCTAssertEqual(pressure.timer.startValue, pressure.done[1].meta)
        XCTAssertEqual(pressure.timer.startValue, "10,0 bar")
        XCTAssertTrue(pressure.timer.accessibilityLabel.contains("\(Int((pressure.timer.fraction * 100).rounded()))"))
        XCTAssertEqual(numbers(pressure.counter), [3, 5])
        XCTAssertEqual(pressure.done.count + 1 + pressure.next.count, 5)
        XCTAssertEqual(pressure.current.n, pressure.done.count + 1)
        XCTAssertTrue(pressure.timer.remainingLabel.contains(pressure.timer.remaining))
    }

    func testObjectFiguresAreConsistent() async throws {
        let data = try await loaded()
        let overview = try XCTUnwrap(data.tradeOverviews["plumbing"])
        let objects = try XCTUnwrap(data.objectChecks[bathUpperRoute])
        let row = try XCTUnwrap(rows(overview).first { $0.name == "Bad OG" })

        XCTAssertEqual(numbers(objects.counter), [5, 1, 5])
        XCTAssertEqual(objects.objects.filter { $0.state == .done }.count, 1)
        XCTAssertEqual(objects.objects.count, 5)
        XCTAssertTrue(row.sub.hasSuffix("\(objects.objects.count) Objekte"), row.sub)
        let selected = try XCTUnwrap(objects.objects.firstIndex { $0.state == .now })
        XCTAssertEqual(objects.heading, objects.objects[selected].name)
        XCTAssertTrue(objects.nextHint.contains(objects.objects[selected + 1].name))
        XCTAssertEqual(objects.objects[selected + 1].name, "Dusche")
        XCTAssertEqual(row.target, bathUpperRoute)
    }

    func testTileFiguresAreConsistent() async throws {
        let data = try await loaded()
        let overview = try XCTUnwrap(data.tradeOverviews["tiles"])
        let tiles = try XCTUnwrap(data.tileChecks[bathRoute])
        let row = try XCTUnwrap(rows(overview).first { $0.name == "Bad OG" })

        let hollow = tiles.tiles.enumerated().filter { $0.element.hollow }.map(\.offset)
        XCTAssertEqual(numbers(tiles.result).first, hollow.count)
        XCTAssertEqual(hollow, [2, 3])
        XCTAssertTrue(hollow.allSatisfy { $0 < tiles.columns })
        XCTAssertEqual(tiles.tiles.count, tiles.columns * 3)
        let steps = tiles.done.count + 1 + tiles.next.count
        XCTAssertEqual(numbers(tiles.counter), [3, steps])
        XCTAssertTrue(row.sub.hasSuffix("Schritt 3 von \(steps)"), row.sub)
    }

    func testPaintFiguresAreConsistent() async throws {
        let data = try await loaded()
        let overview = try XCTUnwrap(data.tradeOverviews["painting"])
        let paint = try XCTUnwrap(data.paintChecks[livingRoute])
        let row = try XCTUnwrap(rows(overview).first { $0.name == "Wohnen / Essen" })

        let steps = paint.done.count + 1 + paint.next.count
        XCTAssertEqual(steps, 5)
        XCTAssertEqual(numbers(paint.counter), [2, steps])
        XCTAssertTrue(row.sub.hasSuffix("Schritt 2 von \(steps)"), row.sub)
        XCTAssertEqual(paint.doneSwatch, .sand)
        XCTAssertTrue(paint.subtitle.contains("Sand"))
        XCTAssertTrue(row.sub.contains("Sand"))
        XCTAssertEqual(row.leading, .swatch(.sand))
        XCTAssertEqual(row.target, livingRoute)
    }
}
