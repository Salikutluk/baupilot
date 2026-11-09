import SwiftUI

enum DefectsAction {
    case selectView(InspectionsView)
}

/// Offene Mängel & Fristen (IosAbnMaengel.dc.html), view "Offene Mängel". The late-card buttons and the "Behobene
/// Mängel" row are placeholder links without an action (M4 D5).
struct DefectsScreen: View {
    let model: DefectsBoardModel
    let onAction: (DefectsAction) -> Void

    var body: some View {
        ScreenScaffold(kind: .tabRoot) {
            // A content container of its own: on the scaffold the identifier would merge with `screen.inspections`.
            VStack(alignment: .leading, spacing: 14) {
                LargeTitleHeader(title: model.title, trailing: .link(text: model.templatesLabel, action: nil))
                SegmentedControl(
                    segments: model.views, selection: 1, height: .regular,
                    background: .chip(Tokens.chip.abnMaengel), accessibilityLabel: model.viewsLabel,
                    identifiers: InspectionsView.allCases.map { "inspections.segment.\($0.rawValue)" }
                ) { index in
                    onAction(.selectView(InspectionsView.allCases[index]))
                }
                ForEach(model.cards, id: \.id) { card in
                    defectCard(card)
                }
                // IosAbnMaengel:47-54: the link row in a 14-radius card, padding 0 14.
                Card(style: .row, padding: EdgeInsets(top: 0, leading: 14, bottom: 0, trailing: 14)) {
                    CountLinkRow(
                        icon: .checkW26, iconColor: Tokens.ok.core, title: model.resolvedLabel,
                        count: model.resolvedCount, action: nil)
                }
            }
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("inspections.view.defects")
        }
    }

    /// IosAbnMaengel:30-45. A late card has the red 1 pt border; the buttons sit on the meta row's trailing side.
    private func defectCard(_ card: DefectCard) -> some View {
        let late = card.urgency == .late
        return Card(
            padding: EdgeInsets(top: 14, leading: 14, bottom: 14, trailing: 14),
            border: late ? .overdue(Tokens.err.abnMaengel) : nil
        ) {
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text(card.trade)
                        .interText(13, .semibold)
                        .foregroundStyle(Tokens.muted.color)
                    Spacer(minLength: 0)
                    StatusBadge(card.dueLabel, tone: tone(card.urgency), weight: .bold)
                }
                Text(card.title)
                    .interText(16, .semibold)
                    .foregroundStyle(Tokens.text.color)
                HStack {
                    Text(card.meta)
                        .interText(13, .regular)
                        .foregroundStyle(Tokens.muted.color)
                    Spacer(minLength: 0)
                    if card.showsActions {
                        HStack(spacing: 8) {
                            PillButton(
                                model.remindLabel, style: .outline(border: Tokens.border.abnMaengel), action: nil)
                            PillButton(model.resolveLabel, style: .filled, action: nil)
                        }
                    }
                }
            }
        }
        .accessibilityElement(children: card.showsActions ? .contain : .combine)
        .accessibilityIdentifier("defect.\(card.id)")
    }

    /// IosAbnMaengel:57-59: late `eSoft/eFg`, soon `wSoft/wFg`, later `chip/muted`.
    private func tone(_ urgency: DefectUrgency) -> StatusBadge.Tone {
        switch urgency {
        case .late: return .error()
        case .soon: return .warn(fg: Tokens.wFg.abnMaengel)
        case .later: return .neutral(bg: Tokens.chip.abnMaengel)
        }
    }
}
