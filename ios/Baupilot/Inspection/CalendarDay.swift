import Foundation

/// A calendar day without time or zone (proleptic Gregorian). All deadline arithmetic runs on this, never on an instant.
public struct CalendarDay: Equatable, Sendable {
    public let year: Int
    public let month: Int
    public let day: Int

    public init(year: Int, month: Int, day: Int) {
        self.year = year
        self.month = month
        self.day = day
    }

    /// The single conversion from an instant to a day: the day on the wall clock of `timeZone` (the app uses
    /// Europe/Berlin, the zone `MockRepository.now()` builds its instant in; UTC would give the previous day).
    public static func from(_ date: Date, timeZone: TimeZone) -> CalendarDay {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = timeZone
        let parts = calendar.dateComponents([.year, .month, .day], from: date)
        return CalendarDay(year: parts.year ?? 1970, month: parts.month ?? 1, day: parts.day ?? 1)
    }

    /// Days since 1970-01-01 (civil-from-days algorithm, no `Calendar`).
    var epochDay: Int {
        let y = month <= 2 ? year - 1 : year
        let era = (y >= 0 ? y : y - 399) / 400
        let yoe = y - era * 400
        let doy = (153 * (month + (month > 2 ? -3 : 9)) + 2) / 5 + day - 1
        let doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }

    init(epochDay: Int) {
        let z = epochDay + 719468
        let era = (z >= 0 ? z : z - 146096) / 146097
        let doe = z - era * 146097
        let yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        let doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        let mp = (5 * doy + 2) / 153
        let day = doy - (153 * mp + 2) / 5 + 1
        let month = mp < 10 ? mp + 3 : mp - 9
        self.init(year: yoe + era * 400 + (month <= 2 ? 1 : 0), month: month, day: day)
    }

    public func adding(days: Int) -> CalendarDay {
        CalendarDay(epochDay: epochDay + days)
    }

    /// 0 = Monday … 6 = Sunday (1970-01-01 was a Thursday).
    public var weekdayIndex: Int {
        ((epochDay + 3) % 7 + 7) % 7
    }
}
