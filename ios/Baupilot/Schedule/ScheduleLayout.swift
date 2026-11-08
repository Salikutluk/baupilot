import CoreGraphics
import Foundation

public enum ScheduleState: Equatable, Sendable { case done, running, planned }

public struct ScheduleRowLayout: Equatable, Sendable {
    public let tradeId: String
    public let name: String
    public let state: ScheduleState
    /// Unsnapped chart x of the bar's left edge.
    public let barX: CGFloat
    /// Unsnapped bar width, at least 10.
    public let barWidth: CGFloat
    /// Unsnapped diamond centre; nil = no diamond.
    public let diamondCenterX: CGFloat?

    /// D22: box edges are drawn on whole points.
    public var drawX: CGFloat { ScheduleLayout.snap(barX) }
    public var drawWidth: CGFloat { ScheduleLayout.snap(barX + barWidth) - drawX }
    public var diamondDrawCenterX: CGFloat? { diamondCenterX.map(ScheduleLayout.snap) }
}

public struct MonthLabel: Equatable, Sendable {
    public let x: CGFloat
    public let label: String
}

public struct ScheduleLayout: Equatable, Sendable {
    public let rows: [ScheduleRowLayout]
    /// Unsnapped left edge of the 2-wide today line.
    public let todayX: CGFloat
    public var todayDrawX: CGFloat { ScheduleLayout.snap(todayX) }
    /// Left edge of the "Heute" text.
    public let todayLabelX: CGFloat
    public let monthLabels: [MonthLabel]

    public static let nameColumn: CGFloat = 118
    public static let columnGap: CGFloat = 8
    public static let rowPitch: CGFloat = 34

    /// Chart x where the time axis starts (name column plus gap).
    private static let originX: Double = 126
    private static let minBarWidth: Double = 10

    static func snap(_ x: CGFloat) -> CGFloat {
        CGFloat(Double(x).rounded(.toNearestOrAwayFromZero))
    }

    private static func round2(_ x: Double) -> Double {
        (x * 100).rounded(.toNearestOrAwayFromZero) / 100
    }

    private static func round4(_ x: Double) -> Double {
        (x * 10_000).rounded(.toNearestOrAwayFromZero) / 10_000
    }

    static func make(trades: [Trade], schedule: Schedule, chartWidth: CGFloat) -> ScheduleLayout {
        let span = Double(schedule.spanMonths)
        let today = schedule.todayMonth
        let track = Double(chartWidth) - originX

        func pct(_ month: Double) -> Double { round2(month / span * 100) }
        func x(_ month: Double) -> Double { originX + pct(month) / 100 * track }

        let rows = trades.map { trade -> ScheduleRowLayout in
            let a = trade.window.startMonth
            let b = trade.window.endMonth
            let state: ScheduleState = b <= today ? .done : (a <= today ? .running : .planned)
            let askAt = a - 2
            let showsDiamond = state == .planned && today - 0.5 <= askAt && askAt < today + 2
            return ScheduleRowLayout(
                tradeId: trade.id,
                name: trade.name,
                state: state,
                barX: CGFloat(x(a)),
                barWidth: CGFloat(max(pct(b - a) / 100 * track, minBarWidth)),
                diamondCenterX: showsDiamond ? CGFloat(x(max(askAt, 0))) : nil)
        }

        return ScheduleLayout(
            rows: rows,
            todayX: CGFloat(originX + track * round4(today / span)),
            todayLabelX: CGFloat(x(today) - 14),
            monthLabels: schedule.axis.map { MonthLabel(x: CGFloat(x(Double($0.month))), label: $0.label) })
    }
}
