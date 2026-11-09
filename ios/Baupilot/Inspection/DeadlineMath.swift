/// Deadline text of the Mangel form (D9): a base day plus an offset, formatted per platform style. Pure, locale-independent.
public enum DeadlineMath {
    public enum Style: Equatable, Sendable {
        /// `"Fr, 15.01.2027"`
        case ios
        /// `"15.01.2027"`
        case android
    }

    static let weekdays = ["Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"]

    public static func label(base: CalendarDay, offsetDays: Int, style: Style) -> String {
        let day = base.adding(days: offsetDays)
        let date = "\(twoDigits(day.day)).\(twoDigits(day.month)).\(day.year)"
        switch style {
        case .ios: return "\(weekdays[day.weekdayIndex]), \(date)"
        case .android: return date
        }
    }

    private static func twoDigits(_ value: Int) -> String {
        value < 10 ? "0\(value)" : "\(value)"
    }
}
