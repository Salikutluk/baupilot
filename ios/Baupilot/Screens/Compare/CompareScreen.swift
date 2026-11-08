import SwiftUI

enum CompareAction {
    case openDocument(String)
}

/// Angebotsvergleich (IosVergleich.dc.html): back bar, title, comparison table, note, pending offer, action bar.
struct CompareScreen: View {
    let model: CompareModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (CompareAction) -> Void

    /// Card width 350 = 390 - 2 x 20 gutter; padding 14, gap 10, label column 104 (IosVergleich.dc.html:33).
    private let widths = ComparisonLayout.columns(cardWidth: 350, padding: 14, gap: 10, labelColumn: 104)

    /// `minmax(0, 1fr) minmax(0, 1.4fr)` over 350 - 10 gap (IosVergleich.dc.html:65).
    private static let barButtonWidths: (ask: CGFloat, award: CGFloat) = {
        let free: CGFloat = 340
        return (free / 2.4, free * 1.4 / 2.4)
    }()

    var body: some View {
        ScreenScaffold(kind: .detail) {
            BackBar(label: backLabel, action: onBack, trailing: .note(model.deadlineNote))
            DetailTitle(model.title, subtitle: model.subtitle, style: .size28)
            ComparisonTable(
                columns: model.columns.map(column),
                rows: model.rows.map {
                    ComparisonTable.Row(
                        label: $0.label, a: $0.a, b: $0.b, strong: $0.strong, highlighted: $0.highlighted)
                },
                widths: widths,
                accessibilityLabel: model.tableLabel)
            InfoCallout(text: model.note, icon: .infoW22, tone: .accent, radius: .r14, textSize: 14, textLines: .block)
            if let pending = model.pending {
                ActionRow(
                    title: pending.firm, subtitle: pending.text, actionTitle: model.remindLabel, action: nil)
            }
        } bottomBar: {
            BottomActionBar {
                HStack(spacing: 10) {
                    SecondaryButton(model.askLabel, size: .medium, chip: Tokens.chip.vergleich, action: nil)
                        .frame(width: Self.barButtonWidths.ask)
                    PrimaryButton(model.awardLabel, size: .medium, action: nil)
                        .frame(width: Self.barButtonWidths.award)
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.compare.\(model.tradeId)")
    }

    private func column(_ column: CompareModel.Column) -> ComparisonTable.Column {
        ComparisonTable.Column(
            name: column.name, badge: column.best ? ScreenCopy.Vergleich.bestLabel : nil,
            action: documentAction(column.target), identifier: "compare.column.\(column.offerId)")
    }

    private func documentAction(_ target: AppRoute?) -> (() -> Void)? {
        guard case let .document(id)? = target else { return nil }
        return { onAction(.openDocument(id)) }
    }
}
