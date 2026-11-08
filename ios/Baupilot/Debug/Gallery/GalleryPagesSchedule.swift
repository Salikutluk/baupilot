#if DEBUG
import SwiftUI

/// AskRow, DiamondMarker, ScheduleLegend and ScheduleChart (the Zeitplan parts). The chart sample is four
/// trades of the fixture at the screen's content width, so the snapping of D22 is exercised.
struct GallerySchedulePage: View {
    private static let sample: ScheduleLayout = {
        func trade(_ id: String, _ name: String, _ start: Double, _ end: Double) -> Trade {
            Trade(
                id: id, name: name, phase: 1,
                window: Trade.Window(startMonth: start, endMonth: end),
                scheduleState: "", askNow: false, weekLabel: nil, statusLabel: nil, detail: nil)
        }
        let schedule = Schedule(
            originLabel: "", spanMonths: 19, todayMonth: 9.6,
            axis: [
                Schedule.AxisLabel(month: 0, label: "Mär 26"),
                Schedule.AxisLabel(month: 8, label: "Nov"),
                Schedule.AxisLabel(month: 16, label: "Jul"),
            ],
            asks: [])
        return ScheduleLayout.make(
            trades: [
                trade("earthworks", "Erdarbeiten", 0, 1.5),
                trade("windows", "Fenster & Türen", 9.4, 10),
                trade("electrical", "Elektro", 10.1, 12),
                trade("screed", "Estrich", 11.6, 13),
            ],
            schedule: schedule, chartWidth: 350)
    }()

    var body: some View {
        GalleryGroup("DiamondMarker · 10 / 8 / 8 with outline") {
            HStack(spacing: 24) {
                DiamondMarker(size: .s10)
                DiamondMarker(size: .s8)
                DiamondMarker(size: .s8, outline: Tokens.bg)
            }
            .padding(6)
        }
        GalleryGroup("AskRow · in a card") {
            Card(style: .standard) {
                VStack(alignment: .leading, spacing: 8) {
                    AskRow(title: "Estrich", subtitle: "Start KW 8 · Angebote bis 15.01.", action: nil)
                    AskRow(title: "Fliesen & Maler", subtitle: "Start März · jetzt je 3 Angebote einholen", action: {})
                }
            }
        }
        GalleryGroup("ScheduleLegend") {
            ScheduleLegend(items: [
                .square(ScreenCopy.Zeitplan.legendDone, Tokens.done),
                .square(ScreenCopy.Zeitplan.legendRunning, Tokens.accent),
                .square(ScreenCopy.Zeitplan.legendPlanned, Tokens.plan),
                .diamond(ScreenCopy.Zeitplan.legendAsk),
            ])
        }
        GalleryGroup("ScheduleChart · 4 trades") {
            ScheduleChart(layout: Self.sample, todayLabel: ScreenCopy.Zeitplan.today, accessibilityLabel: "Zeitplan")
        }
    }
}
#endif
