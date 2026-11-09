import SwiftUI

enum InspectionsAction {
    case selectView(InspectionsView)
    case open(AppRoute)
}

/// Abnahmen je Gewerk (IosAbnGewerke.dc.html), view "Gewerke".
struct InspectionsScreen: View {
    let model: InspectionsModel
    let onAction: (InspectionsAction) -> Void

    var body: some View {
        ScreenScaffold(kind: .tabRoot) {
            // A content container of its own: on the scaffold the identifier would merge with `screen.inspections`.
            VStack(alignment: .leading, spacing: 14) {
                LargeTitleHeader(title: model.title, trailing: .link(text: model.templatesLabel, action: nil))
                SegmentedControl(
                    segments: model.views, selection: 0, background: .chip(Tokens.chip.abnGewerke),
                    accessibilityLabel: model.viewsLabel,
                    identifiers: InspectionsView.allCases.map { "inspections.segment.\($0.rawValue)" }
                ) { index in
                    onAction(.selectView(InspectionsView.allCases[index]))
                }
                // IosAbnGewerke:31-38: one anchor per trade, the "today" row on `hi`.
                Card(padding: EdgeInsets()) {
                    VStack(alignment: .leading, spacing: 0) {
                        ForEach(Array(model.rows.enumerated()), id: \.element.tradeId) { index, row in
                            tradeRow(row, index: index)
                        }
                    }
                }
            }
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("inspections.view.trades")
        }
    }

    private func tradeRow(_ row: InspectionTradeRow, index: Int) -> some View {
        let target = row.target
        return ListRow(
            title: row.name, subtitle: row.subtitle,
            trailing: .badgeAndChevron(StatusBadge(row.statusLabel, tone: tone(row.status)), .small),
            titleSize: .large, density: .minHeight(54), showsDivider: index > 0,
            background: row.status == .today ? Tokens.hi : nil, subtitleLines: .block,
            action: target.map { route in { onAction(.open(route)) } }
        )
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("inspections.trade.\(row.tradeId)")
    }

    /// IosAbnGewerke:31-38, `S`: accepted `oSoft/oFg`, reserved `wSoft/wFg`, today `aSoft/aFg`, planned `chip/muted`.
    private func tone(_ status: InspectionStatus) -> StatusBadge.Tone {
        switch status {
        case .accepted: return .ok
        case .reserved: return .warn(fg: Tokens.wFg.abnGewerke)
        case .today: return .accent
        case .planned: return .neutral(bg: Tokens.chip.abnGewerke)
        }
    }
}
