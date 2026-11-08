import SwiftUI

enum ScheduleAction {
    case openTrade(String)
}

/// Zeitplan (IosZeitplan.dc.html): header, ask card, legend and the time chart.
struct ScheduleScreen: View {
    let model: ScheduleModel
    let onAction: (ScheduleAction) -> Void

    private func action(for ask: ScheduleModel.AskRow) -> (() -> Void)? {
        guard case let .trade(id)? = ask.target else { return nil }
        return { onAction(.openTrade(id)) }
    }

    private var askCard: some View {
        Card(style: .standard) {
            VStack(alignment: .leading, spacing: 8) {
                Text(ScreenCopy.Zeitplan.askHeading)
                    .interText(13, .semibold)
                    .foregroundStyle(Tokens.aFg.color)
                ForEach(Array(model.asks.enumerated()), id: \.offset) { _, ask in
                    AskRow(title: ask.name, subtitle: ask.sub, action: action(for: ask))
                }
            }
        }
    }

    /// Kicker and title (IosZeitplan:21-22).
    private var header: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(model.moveInLabel)
                .interText(13, .semibold)
                .foregroundStyle(Tokens.muted.color)
            Text(ScreenCopy.Zeitplan.title)
                .interText(32, .bold, tracking: -0.6)
                .foregroundStyle(Tokens.text.color)
                .accessibilityAddTraits(.isHeader)
        }
    }

    private var legend: some View {
        ScheduleLegend(items: [
            .square(ScreenCopy.Zeitplan.legendDone, Tokens.done),
            .square(ScreenCopy.Zeitplan.legendRunning, Tokens.accent),
            .square(ScreenCopy.Zeitplan.legendPlanned, Tokens.plan),
            .diamond(ScreenCopy.Zeitplan.legendAsk),
        ])
    }

    var body: some View {
        ScreenScaffold(kind: .tabRoot, sectionSpacing: .regular) {
            header
            askCard
            legend
            ScheduleChart(
                layout: model.layout, todayLabel: ScreenCopy.Zeitplan.today,
                accessibilityLabel: model.ariaLabel)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.schedule")
    }
}
