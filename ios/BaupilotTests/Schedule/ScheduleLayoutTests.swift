import XCTest
@testable import Baupilot

final class ScheduleLayoutTests: XCTestCase {
    private let accuracy: CGFloat = 0.0005

    private func fixture() throws -> (trades: [Trade], schedule: Schedule) {
        let repo = try MockRepository.bundled()
        let trades = try runSync { try await repo.trades() }
        let schedule = try runSync { try await repo.schedule() }
        return (trades, schedule)
    }

    private func runSync<T: Sendable>(_ body: @escaping @Sendable () async throws -> T) throws -> T {
        let box = ResultBox<T>()
        let done = expectation(description: "load")
        Task {
            do { box.set(.success(try await body())) } catch { box.set(.failure(error)) }
            done.fulfill()
        }
        wait(for: [done], timeout: 10)
        return try XCTUnwrap(box.get()).get()
    }

    private func layout(width: CGFloat = 350) throws -> ScheduleLayout {
        let f = try fixture()
        return ScheduleLayout.make(trades: f.trades, schedule: f.schedule, chartWidth: width)
    }

    private func row(_ layout: ScheduleLayout, _ id: String) throws -> ScheduleRowLayout {
        try XCTUnwrap(layout.rows.first { $0.tradeId == id }, id)
    }

    private func synthetic(
        start: Double, end: Double, today: Double = 10
    ) -> ScheduleRowLayout? {
        let trade = Trade(
            id: "t", name: "T", phase: 1,
            window: Trade.Window(startMonth: start, endMonth: end),
            scheduleState: "planned", askNow: false,
            weekLabel: nil, statusLabel: nil, detail: nil)
        let schedule = Schedule(
            originLabel: "", spanMonths: 19, todayMonth: today, axis: [], asks: [])
        return ScheduleLayout.make(trades: [trade], schedule: schedule, chartWidth: 350).rows.first
    }

    func testStatusFollowsTodayRule() throws {
        let l = try layout()
        let expected: [ScheduleState] = [
            .done, .done, .done, .done, .running,
            .planned, .planned, .planned, .planned, .planned, .planned,
        ]
        XCTAssertEqual(l.rows.map(\.state), expected)
    }

    func testStatusMatchesFixtureScheduleState() throws {
        let f = try fixture()
        let l = ScheduleLayout.make(trades: f.trades, schedule: f.schedule, chartWidth: 350)
        XCTAssertEqual(l.rows.count, f.trades.count)
        for (trade, row) in zip(f.trades, l.rows) {
            XCTAssertEqual(row.tradeId, trade.id)
            XCTAssertEqual(row.name, trade.name)
            let state: String
            switch row.state {
            case .done: state = "done"
            case .running: state = "running"
            case .planned: state = "planned"
            }
            XCTAssertEqual(state, trade.scheduleState, trade.id)
        }
    }

    func testDiamondsOnlyForScreedTilesPainting() throws {
        let f = try fixture()
        let l = ScheduleLayout.make(trades: f.trades, schedule: f.schedule, chartWidth: 350)
        let computed = Set(l.rows.filter { $0.diamondCenterX != nil }.map(\.tradeId))
        let fixtureSet = Set(f.trades.filter(\.askNow).map(\.id))
        XCTAssertEqual(computed, fixtureSet)
        XCTAssertEqual(computed, ["screed", "tiles", "painting"])
    }

    func testDiamondVisibilityBoundaries() throws {
        // today = 10: askAt = start - 2
        XCTAssertNotNil(synthetic(start: 11.5, end: 13)?.diamondCenterX, "askAt == today - 0.5")
        XCTAssertNil(synthetic(start: 14, end: 15)?.diamondCenterX, "askAt == today + 2")
        XCTAssertNotNil(synthetic(start: 13.99, end: 15)?.diamondCenterX, "just before today + 2")
        XCTAssertNil(synthetic(start: 11.49, end: 13)?.diamondCenterX, "askAt == today - 0.51")
        let running = try XCTUnwrap(synthetic(start: 9.5, end: 11))
        XCTAssertEqual(running.state, .running)
        XCTAssertNil(running.diamondCenterX)
    }

    func testBarMinWidthIs10() throws {
        let l = try layout()
        XCTAssertEqual(try row(l, "roofing").barWidth, 10, accuracy: accuracy)
        XCTAssertEqual(try row(l, "windows").barWidth, 10, accuracy: accuracy)
        for r in l.rows { XCTAssertGreaterThanOrEqual(r.barWidth, 10) }
    }

    func testBarGeometryMatchesPrototype() throws {
        let l = try layout(width: 350)
        let expected: [(String, CGFloat, CGFloat)] = [
            ("earthworks", 126, 17.6736),
            ("shell", 143.6736, 76.6304),
            ("carpentry", 220.3264, 10.6176),
            ("roofing", 229.7568, 10),
            ("windows", 236.8128, 10),
            ("electrical", 245.0784, 22.4),
            ("plumbing", 250.9696, 18.8608),
            ("screed", 262.752, 16.5088),
            ("tiles", 274.5568, 16.5088),
            ("painting", 283.9872, 18.8608),
            ("landscaping", 308.7392, 29.4784),
        ]
        XCTAssertEqual(l.rows.count, expected.count)
        for (id, x, w) in expected {
            let r = try row(l, id)
            XCTAssertEqual(r.barX, x, accuracy: accuracy, "\(id) x")
            XCTAssertEqual(r.barWidth, w, accuracy: accuracy, "\(id) w")
        }
        let diamonds: [(String, CGFloat)] = [
            ("screed", 239.1872), ("tiles", 250.9696), ("painting", 260.4),
        ]
        for (id, x) in diamonds {
            let c = try XCTUnwrap(try row(l, id).diamondCenterX, id)
            XCTAssertEqual(c, x, accuracy: accuracy, "\(id) diamond")
        }
        for id in ["earthworks", "shell", "carpentry", "roofing", "windows",
                   "electrical", "plumbing", "landscaping"] {
            XCTAssertNil(try row(l, id).diamondCenterX, id)
        }
    }

    func testTodayLineAndLabelX() throws {
        let l = try layout(width: 350)
        XCTAssertEqual(l.todayX, 239.1872, accuracy: accuracy)
        XCTAssertEqual(l.todayLabelX, 225.1872, accuracy: accuracy)
    }

    func testMonthLabelPositionsAndTexts() throws {
        let l = try layout(width: 350)
        XCTAssertEqual(l.monthLabels.map(\.label), ["Mär 26", "Jul", "Nov", "Mär 27", "Jul"])
        let xs: [CGFloat] = [126, 173.152, 220.3264, 267.4784, 314.6304]
        XCTAssertEqual(l.monthLabels.count, xs.count)
        for (label, x) in zip(l.monthLabels, xs) {
            XCTAssertEqual(label.x, x, accuracy: accuracy, label.label)
        }
    }

    func testSnappedDrawValuesMatchReference() throws {
        let l = try layout(width: 350)
        let earth = try row(l, "earthworks")
        XCTAssertEqual(earth.drawX, 126)
        XCTAssertEqual(earth.drawWidth, 18)
        let shell = try row(l, "shell")
        XCTAssertEqual(shell.drawX, 144)
        XCTAssertEqual(shell.drawWidth, 76)
        let carpentry = try row(l, "carpentry")
        XCTAssertEqual(carpentry.drawX, 220)
        XCTAssertEqual(carpentry.drawWidth, 11)
        XCTAssertEqual(l.todayDrawX, 239)
        XCTAssertEqual(try row(l, "screed").diamondDrawCenterX, 239)
        XCTAssertEqual(try row(l, "tiles").diamondDrawCenterX, 251)
        XCTAssertEqual(try row(l, "painting").diamondDrawCenterX, 260)
        XCTAssertNil(try row(l, "shell").diamondDrawCenterX)
    }

    func testAndroidWidthMatchesPinnedValues() throws {
        let l = try layout(width: 358)
        XCTAssertEqual(try row(l, "earthworks").barWidth, 18.3048, accuracy: accuracy)
        XCTAssertEqual(try row(l, "landscaping").barX, 315.2656, accuracy: accuracy)
        XCTAssertEqual(l.todayX, 243.2296, accuracy: accuracy)
        XCTAssertEqual(l.todayDrawX, 243)
        XCTAssertEqual(try XCTUnwrap(try row(l, "painting").diamondCenterX), 265.2, accuracy: accuracy)
        XCTAssertEqual(try row(l, "painting").diamondDrawCenterX, 265)
    }

    func testRawWindowsMatchMilestoneTable() throws {
        let f = try fixture()
        let expected: [(String, Double, Double)] = [
            ("Erdarbeiten", 0, 1.5), ("Rohbau", 1.5, 8), ("Zimmerer", 8, 8.9),
            ("Dachdecker", 8.8, 9.5), ("Fenster & Türen", 9.4, 10), ("Elektro", 10.1, 12),
            ("Sanitär & Heizung", 10.6, 12.2), ("Estrich", 11.6, 13), ("Fliesen", 12.6, 14),
            ("Maler", 13.4, 15), ("Außenanlagen", 15.5, 18),
        ]
        XCTAssertEqual(f.trades.count, expected.count)
        for (trade, e) in zip(f.trades, expected) {
            XCTAssertEqual(trade.name, e.0)
            XCTAssertEqual(trade.window.startMonth, e.1, accuracy: 1e-9, trade.name)
            XCTAssertEqual(trade.window.endMonth, e.2, accuracy: 1e-9, trade.name)
        }
        XCTAssertEqual(f.schedule.spanMonths, 19)
        XCTAssertEqual(f.schedule.todayMonth, 9.6, accuracy: 1e-9)
    }
}

private final class ResultBox<T: Sendable>: @unchecked Sendable {
    private let lock = NSLock()
    private var result: Result<T, Error>?
    func set(_ value: Result<T, Error>) { lock.lock(); result = value; lock.unlock() }
    func get() -> Result<T, Error>? { lock.lock(); defer { lock.unlock() }; return result }
}
