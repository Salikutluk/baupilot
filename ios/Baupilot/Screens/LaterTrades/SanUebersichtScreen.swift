import SwiftUI

enum OverviewAction {
    case open(AppRoute)
}

/// Sanitär & Heizung – Übersicht (IosSanUebersicht.dc.html): two phase tiles (active 0.50 on the `sanUebersicht`
/// track, then planned), then per group a caps header over a card of icon rows with a status badge. A row with a
/// target opens it; the phase tiles and rows without a target have no action (M3 D5). The first row's subtitle wraps
/// greedily like Chrome (D13). Core tokens plus the `sanUebersicht` track.
struct SanUebersichtScreen: View {
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
                track: Tokens.track.sanUebersicht, fillsHeight: true)
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
                            title: row.name, subtitle: row.sub,
                            leading: leading(row.leading),
                            trailing: .badge(StatusBadge(row.statusLabel, tone: badgeTone(row.tone))),
                            density: .minHeight(52), showsDivider: rowIndex > 0,
                            subtitleLines: .greedy, textGap: 1, titleLines: .greedy,
                            action: openAction(row.target)
                        )
                        .accessibilityIdentifier("overview.row.\(groupIndex).\(rowIndex)")
                    }
                }
            }
        }
    }

    private func leading(_ leading: OverviewLeading) -> ListRow.Leading {
        switch leading {
        case .icon(let icon): return .iconTile(icon.symbol, size: .s34r9)
        case .swatch, .none: return .none
        }
    }

    /// The prototype's status map `S` (IosSanUebersicht:72): defect eSoft/eFg, Heute and Läuft aSoft/aFg, ok oSoft/oFg,
    /// Offen and Geplant chip/muted.
    private func badgeTone(_ tone: OverviewTone) -> StatusBadge.Tone {
        switch tone {
        case .defect: return .error()
        case .today, .running: return .accent
        case .ok: return .ok
        case .open, .planned, .required: return .neutral()
        }
    }

    /// A target makes the row a button; without one it has no action closure (M3 D5).
    private func openAction(_ target: AppRoute?) -> (() -> Void)? {
        guard let target else { return nil }
        return { onAction(.open(target)) }
    }
}
