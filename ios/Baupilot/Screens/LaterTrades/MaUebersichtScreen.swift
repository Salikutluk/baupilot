import SwiftUI

/// Maler – Übersicht (IosMaUebersicht.dc.html:17-60): the title, a grid of three facts, then per group a caps header
/// over a card of rows with a paint swatch, a one-line subtitle and a status badge (In Ordnung ok, Läuft accent,
/// Offen neutral, "1 Mangel" error). Only Wohnen / Essen has a target; the facts and the other rows have no action
/// (M3 D5). The swatch colours are the D6 supplement tokens; the dividers use `border.maUebersicht`.
struct MaUebersichtScreen: View {
    let model: TradeOverviewModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (OverviewAction) -> Void

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack)
            DetailTitle(model.title, subtitle: model.subtitle, style: .size30)
            if case let .facts(facts) = model.header {
                factGrid(facts)
            }
            ForEach(Array(model.groups.enumerated()), id: \.offset) { groupIndex, group in
                section(group, groupIndex: groupIndex)
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.inspection.\(model.tradeId)")
    }

    /// The `FactStrip(.grid)` geometry (IosMaUebersicht:30: padding 14, gap 8, label 12/600, value 16/700, gap 3)
    /// with one identifier per fact, which `FactStrip` cannot carry.
    private func factGrid(_ facts: [FactModel]) -> some View {
        HStack(alignment: .top, spacing: 8) {
            ForEach(Array(facts.enumerated()), id: \.offset) { index, fact in
                VStack(alignment: .leading, spacing: 3) {
                    Text(fact.label)
                        .interText(12, .semibold)
                        .foregroundStyle(Tokens.muted.color)
                    Text(fact.value)
                        .interText(16, .bold)
                        .foregroundStyle(Tokens.text.color)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityElement(children: .combine)
                .accessibilityIdentifier("overview.fact.\(index)")
            }
        }
        .padding(14)
        .frame(maxWidth: .infinity)
        .background(Tokens.surface.color, in: RoundedRectangle(cornerRadius: 14))
    }

    private func section(_ group: TradeOverviewModel.Group, groupIndex: Int) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(group.title, style: .caps(inset: 4, link: nil))
            Card(style: .row, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ForEach(Array(group.rows.enumerated()), id: \.offset) { rowIndex, row in
                        ListRow(
                            title: row.name, subtitle: row.sub, leading: leading(row.leading),
                            trailing: .badge(StatusBadge(row.statusLabel, tone: badgeTone(row.tone))),
                            density: .minHeight(52), showsDivider: rowIndex > 0,
                            dividerColor: Tokens.border.maUebersicht, textGap: 1,
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
        case .swatch(let paint):
            return .swatch(paintToken(paint), side: 14, radius: 4, border: Tokens.border.maUebersicht)
        case .icon, .none:
            return .none
        }
    }

    private func paintToken(_ paint: PaintColor) -> ColorToken {
        switch paint {
        case .white: return Tokens.paintWhite
        case .sand: return Tokens.paintSand
        case .sage: return Tokens.paintSage
        }
    }

    /// The prototype's status map `S` (IosMaUebersicht:67): ok oSoft/oFg, Läuft aSoft/aFg, Offen chip/muted, Mangel
    /// eSoft/eFg.
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
