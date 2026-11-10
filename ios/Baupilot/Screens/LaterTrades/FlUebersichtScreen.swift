import SwiftUI

/// Fliesen – Übersicht (IosFlUebersicht.dc.html): a done phase tile (value in `oFg` plus its date) and an active
/// tile at 0.50 on the `flUebersicht` track, then per group a caps header over a card of rows with a greedy subtitle
/// and a status badge (In Ordnung ok, Läuft accent, Offen neutral). Only the room row with a target (Bad OG) opens
/// it; the phase tiles and the other rows have no action (M3 D5). Core tokens plus the `flUebersicht` track.
struct FlUebersichtScreen: View {
    let model: TradeOverviewModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (OverviewAction) -> Void

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack)
            DetailTitle(model.title, subtitle: model.subtitle, style: .size30)
            if case let .phases(phases) = model.header {
                HStack(alignment: .top, spacing: 8) {
                    ForEach(Array(phases.enumerated()), id: \.offset) { index, phase in
                        tile(phase)
                            .accessibilityIdentifier("overview.phase.\(index)")
                    }
                }
                .fixedSize(horizontal: false, vertical: true)
            }
            ForEach(Array(model.groups.enumerated()), id: \.offset) { groupIndex, group in
                section(group, groupIndex: groupIndex)
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.inspection.\(model.tradeId)")
    }

    @ViewBuilder
    private func tile(_ phase: PhaseModel) -> some View {
        switch phase.kind {
        case .done(let status, let date):
            PhaseTile(label: phase.title, value: status, date: date, state: .done, fillsHeight: true)
        case .active(let progressLabel, let fraction):
            PhaseTile(
                label: phase.title, value: progressLabel, state: .active(progress: fraction),
                track: Tokens.track.flUebersicht, fillsHeight: true)
        case .planned(let status):
            PhaseTile(label: phase.title, value: status, state: .planned, fillsHeight: true)
        }
    }

    private func section(_ group: TradeOverviewModel.Group, groupIndex: Int) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(group.title, style: .caps(inset: 4, link: nil))
            Card(style: .row, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ForEach(Array(group.rows.enumerated()), id: \.offset) { rowIndex, row in
                        ListRow(
                            title: row.name, subtitle: row.sub, leading: .none,
                            trailing: .badge(StatusBadge(row.statusLabel, tone: badgeTone(row.tone))),
                            density: .minHeight(56), showsDivider: rowIndex > 0,
                            subtitleLines: .greedy, textGap: 1,
                            action: openAction(row.target)
                        )
                        .accessibilityIdentifier("overview.row.\(groupIndex).\(rowIndex)")
                    }
                }
            }
        }
    }

    /// The prototype's status map `S` (IosFlUebersicht:70): ok oSoft/oFg, Läuft aSoft/aFg, Offen chip/muted.
    private func badgeTone(_ tone: OverviewTone) -> StatusBadge.Tone {
        switch tone {
        case .defect, .required: return .error()
        case .today, .running: return .accent
        case .ok: return .ok
        case .open, .planned: return .neutral()
        }
    }

    /// A target makes the row a button; without one it has no action closure (M3 D5).
    private func openAction(_ target: AppRoute?) -> (() -> Void)? {
        guard let target else { return nil }
        return { onAction(.open(target)) }
    }
}
