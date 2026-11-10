import SwiftUI

enum ElectricalAction {
    case open(AppRoute)
}

/// Elektro – Übersicht (Nr. 20, IosElUebersicht.dc.html): two phase tiles, then per group a caps header (with the
/// placeholder "more" link on iOS) over a card of icon rows with a status badge. A row with a target opens it; the
/// phase tiles, the "more" link and rows without a target have no action (M3 D5). Core tokens plus the
/// `elUebersicht` track of the active phase bar.
struct ElUebersichtScreen: View {
    let model: ElectricalOverviewModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (ElectricalAction) -> Void

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack)
            DetailTitle(model.title, subtitle: model.subtitle, style: .size30)
            HStack(alignment: .top, spacing: 8) {
                ForEach(Array(model.phases.enumerated()), id: \.offset) { index, phase in
                    tile(phase)
                        .accessibilityIdentifier("electrical.phase.\(index)")
                }
            }
            .fixedSize(horizontal: false, vertical: true)
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
                track: Tokens.track.elUebersicht, fillsHeight: true)
        case .planned(let status):
            PhaseTile(label: phase.title, value: status, state: .planned, fillsHeight: true)
        }
    }

    private func section(_ group: ElectricalOverviewModel.Group, groupIndex: Int) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(
                group.title,
                style: .caps(
                    inset: 4,
                    link: group.moreLabel.map { SectionHeader.Link(title: $0, size: 13, action: nil) }))
            Card(style: .row, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ForEach(Array(group.rows.enumerated()), id: \.offset) { rowIndex, row in
                        ListRow(
                            title: row.name, subtitle: row.sub,
                            leading: .iconTile(row.icon.symbol, size: .s34r9),
                            trailing: .badge(StatusBadge(row.statusLabel, tone: badgeTone(row.tone))),
                            density: .minHeight(52), showsDivider: rowIndex > 0,
                            subtitleLines: .single, textGap: 1, titleLines: .greedy,
                            action: openAction(row.target)
                        )
                        .accessibilityIdentifier("electrical.row.\(groupIndex).\(rowIndex)")
                    }
                }
            }
        }
    }

    /// The prototype's status map `S` (IosElUebersicht:76): Offen chip/muted, Läuft aSoft/aFg, ok oSoft/oFg, Fehlt eSoft/eFg.
    private func badgeTone(_ tone: ElectricalTone) -> StatusBadge.Tone {
        switch tone {
        case .open: return .neutral()
        case .running: return .accent
        case .ok: return .ok
        case .missing: return .error()
        }
    }

    /// A target makes the row a button; without one it has no action closure (M3 D5).
    private func openAction(_ target: AppRoute?) -> (() -> Void)? {
        guard let target else { return nil }
        return { onAction(.open(target)) }
    }
}
