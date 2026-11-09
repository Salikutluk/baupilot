import XCTest
@testable import Baupilot

final class DeadlineMathTests: XCTestCase {
    private let base = CalendarDay(year: 2026, month: 12, day: 18)

    func testPlus14DaysRollsOverTheYear() {
        XCTAssertEqual(DeadlineMath.label(base: base, offsetDays: 14, style: .ios), "Fr, 01.01.2027")
    }

    func testPlus28Days() {
        XCTAssertEqual(DeadlineMath.label(base: base, offsetDays: 28, style: .ios), "Fr, 15.01.2027")
    }

    func testAndroidStyleHasNoWeekday() {
        XCTAssertEqual(DeadlineMath.label(base: base, offsetDays: 42, style: .android), "29.01.2027")
    }

    func testWeekdayOfANonFridayBase() {
        let monday = CalendarDay(year: 2026, month: 12, day: 21)
        XCTAssertEqual(DeadlineMath.label(base: monday, offsetDays: 0, style: .ios), "Mo, 21.12.2026")
        XCTAssertEqual(DeadlineMath.label(base: monday, offsetDays: 6, style: .ios), "So, 27.12.2026")
    }

    func testLeapDay() {
        let base = CalendarDay(year: 2028, month: 2, day: 28)
        XCTAssertEqual(DeadlineMath.label(base: base, offsetDays: 1, style: .android), "29.02.2028")
        XCTAssertEqual(DeadlineMath.label(base: base, offsetDays: 2, style: .android), "01.03.2028")
    }

    func testBerlinMidnightMapsToSameDay() throws {
        // The instant MockRepository.now() builds: 2026-12-18 00:00 Europe/Berlin = 2026-12-17 23:00 UTC.
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = try XCTUnwrap(TimeZone(identifier: "Europe/Berlin"))
        let instant = try XCTUnwrap(calendar.date(from: DateComponents(year: 2026, month: 12, day: 18)))
        let berlin = try XCTUnwrap(TimeZone(identifier: "Europe/Berlin"))
        XCTAssertEqual(CalendarDay.from(instant, timeZone: berlin), CalendarDay(year: 2026, month: 12, day: 18))
        let utc = try XCTUnwrap(TimeZone(identifier: "UTC"))
        XCTAssertEqual(CalendarDay.from(instant, timeZone: utc), CalendarDay(year: 2026, month: 12, day: 17))
    }
}
