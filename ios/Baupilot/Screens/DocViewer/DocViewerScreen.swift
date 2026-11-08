import SwiftUI

enum DocViewerAction {
    case openCompare(String)
}

/// Dokument-Viewer (IosDokViewer.dc.html): back bar with share, title, page well, page footer,
/// facts card and the compare action bar. Own layout: the well bleeds the gutter.
struct DocViewerScreen: View {
    let model: DocViewerModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (DocViewerAction) -> Void

    var body: some View {
        ZStack(alignment: .bottom) {
            Tokens.bg.color
            ScrollView(.vertical) {
                VStack(alignment: .leading, spacing: 0) {
                    header
                    well
                    footer
                    facts
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.bottom, 110)
            }
            .scrollIndicators(.hidden)
            BottomActionBar {
                PrimaryButton(model.compareLabel, action: compareAction)
                    .accessibilityIdentifier("document.compare")
            }
        }
        .foregroundStyle(Tokens.text.color)
        .ignoresSafeArea()
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.document.\(model.id)")
    }

    private var compareAction: (() -> Void)? {
        guard case let .compare(tradeId)? = model.compareTarget else { return nil }
        return { onAction(.openCompare(tradeId)) }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 12) {
            BackBar(
                label: backLabel, action: onBack,
                trailing: .icon(.share, size: 22, label: model.shareLabel, action: nil))
            DetailTitle(model.title, subtitle: model.subtitle, style: .size22)
        }
        .padding(EdgeInsets(top: 50, leading: 20, bottom: 0, trailing: 20))
    }

    private var well: some View {
        DocumentPreview(
            issuerName: model.issuerName, issuerAddress: model.issuerAddress,
            number: model.number, reference: model.reference,
            lines: model.lines.map { DocumentPreview.Line(pos: $0.pos, text: $0.text, sum: $0.sum) },
            totalLabel: model.totalLabel, total: model.total, accessibilityLabel: model.pageLabel
        )
        .padding(.vertical, 18)
        .frame(maxWidth: .infinity)
        .background(Tokens.well.color)
        .padding(.top, 14)
    }

    private var footer: some View {
        Text(model.pageFooter)
            .interText(12, .semibold)
            .foregroundStyle(Tokens.muted.color)
            .frame(maxWidth: .infinity)
            .padding(.top, 8)
    }

    private var facts: some View {
        Card(padding: EdgeInsets()) {
            VStack(spacing: 0) {
                ForEach(Array(model.facts.enumerated()), id: \.offset) { index, fact in
                    factRow(fact, isAmount: index == model.facts.count - 1, showsDivider: index > 0)
                }
            }
        }
        .padding(EdgeInsets(top: 10, leading: 20, bottom: 0, trailing: 20))
    }

    /// 15 pt label and value, value 600 (700 on the amount row); ListRow's value is 13 pt muted.
    private func factRow(_ fact: LabeledValue, isAmount: Bool, showsDivider: Bool) -> some View {
        VStack(spacing: 0) {
            if showsDivider {
                Rectangle().fill(Tokens.border.color).frame(height: 1)
            }
            HStack(spacing: 12) {
                Text(fact.label)
                    .interText(15, .regular)
                    .foregroundStyle(Tokens.text.color)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text(fact.value)
                    .interText(15, isAmount ? .bold : .semibold)
                    .foregroundStyle(Tokens.text.color)
                    .lineLimit(1)
                    .fixedSize()
                Icon(.chevronRightW24, size: 14, color: Tokens.muted.color)
            }
            .padding(.horizontal, 14)
            .frame(minHeight: 46)
        }
        .accessibilityElement(children: .combine)
    }
}
