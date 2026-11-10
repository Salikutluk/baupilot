import SwiftUI

/// Estrich "Aufheizen & Belegreife" (IosEsBelegreife.dc.html:17-70): the heating protocol card (header with the day
/// counter, the greedy text, the bar chart of the plan with the axis, today's value with the compact "Tag 6 eintragen"
/// button), the CM-Messung card with its three checks and the lock note. "Tag 6 eintragen" has no action (M3 D5). Both
/// texts and "Messstellen / markiert" wrap greedily like Chrome (D13).
struct EsBelegreifeScreen: View {
    let model: ReadinessCheckModel
    let backLabel: String
    let onBack: () -> Void

    private var identifier: String {
        guard case let .inspectionCheck(tradeId, check) = model.route else { return "screen.inspection.screed.readiness" }
        return "screen.inspection.\(tradeId).\(check)"
    }

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack)
            DetailTitle(model.screenTitle, subtitle: nil, style: .size28)
            heatingCard
            cmCard
            InfoCallout(
                text: model.cm.note, icon: .lock, tone: .locked, textSize: 14, textLines: .block,
                textAlignment: .leading, lineHeight: .normal, alignment: .center)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier)
    }

    private func barColor(_ kind: HeatingBarKind) -> ColorToken {
        switch kind {
        case .recorded: return Tokens.ok.core
        case .today: return Tokens.accent
        case .planned: return Tokens.track.esBelegreife
        }
    }

    private func header(_ title: String, @ViewBuilder trailing: () -> some View) -> some View {
        HStack(alignment: .firstTextBaseline) {
            Text(title)
                .interText(18, .bold)
                .foregroundStyle(Tokens.text.color)
                .accessibilityAddTraits(.isHeader)
            Spacer(minLength: 0)
            trailing()
        }
    }

    /// IosEsBelegreife:30, 54: `line-height: 1.4` is 19.6 pt a line, but the reference lays the three-line block out
    /// 59 pt tall (Chrome snaps the block height to a whole point, as in EsEbenheit), which the later blocks follow.
    private func paragraph(_ text: String) -> some View {
        WholePointHeight {
            GreedyLabel(text: text, size: 14, weight: .regular, color: Tokens.muted, lineHeight: .factor(1.4))
        }
    }

    /// IosEsBelegreife:28-49: the current card (2 pt accent border, padding 16, gap 12).
    private var heatingCard: some View {
        Card(style: .standard, padding: EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16), border: .current) {
            VStack(alignment: .leading, spacing: 12) {
                header(model.cardTitle) {
                    Text(model.counter)
                        .interText(13, .semibold)
                        .foregroundStyle(Tokens.aFg.color)
                }
                paragraph(model.text)
                HeatingBarChart(
                    bars: model.chart.bars.map { (height: CGFloat($0.height), color: barColor($0.kind)) },
                    accessibilityLabel: model.chart.accessibilityLabel)
                    .accessibilityIdentifier("readiness.chart")
                HStack {
                    axisLabel(model.chart.start)
                    Spacer(minLength: 0)
                    axisLabel(model.chart.max)
                    Spacer(minLength: 0)
                    axisLabel(model.chart.end)
                }
                todayBox
            }
        }
    }

    private func axisLabel(_ text: String) -> some View {
        Text(text)
            .interText(12, .regular)
            .foregroundStyle(Tokens.muted.color)
    }

    /// IosEsBelegreife:44-49: today's planned value and the compact button, padding 12 14, gap 12, radius 12.
    private var todayBox: some View {
        HStack(alignment: .center, spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text(model.chart.today)
                    .interText(13, .regular)
                    .foregroundStyle(Tokens.muted.color)
                Text(model.chart.plan)
                    .interText(18, .bold)
                    .foregroundStyle(Tokens.text.color)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            PrimaryButton(model.chart.action, size: .compact, action: nil)
                .accessibilityIdentifier("readiness.action")
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 14)
        .background(RoundedRectangle(cornerRadius: 12).fill(Tokens.chip.core.color))
    }

    /// IosEsBelegreife:52-64: the CM card (padding 16, gap 10): header with the "Pflicht" badge, the text, the checks.
    private var cmCard: some View {
        Card(style: .standard, padding: EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16)) {
            VStack(alignment: .leading, spacing: 10) {
                header(model.cm.title) { StatusBadge(model.cm.tag, tone: .error(), size: .compact) }
                paragraph(model.cm.text)
                ForEach(Array(model.cm.rows.enumerated()), id: \.offset) { index, row in
                    CheckMetaRow(
                        title: row.label, meta: row.meta, done: row.done, ok: Tokens.ok.core,
                        okInk: Tokens.okInk.core, openRing: Tokens.track.esBelegreife)
                        .accessibilityIdentifier("readiness.cm.\(index)")
                }
            }
        }
    }
}

/// Reports the height of its single child rounded to a whole point and draws the child at the top.
private struct WholePointHeight: Layout {
    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        guard let child = subviews.first else { return .zero }
        let size = child.sizeThatFits(proposal)
        return CGSize(width: size.width, height: size.height.rounded())
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        subviews.first?.place(
            at: bounds.origin, anchor: .topLeading, proposal: ProposedViewSize(width: bounds.width, height: nil))
    }
}
