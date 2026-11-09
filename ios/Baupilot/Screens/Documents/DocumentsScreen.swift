import SwiftUI

enum DocumentsAction {
    case openDocument(String)
}

/// Dokumente (IosDokumente.dc.html): title with add button, search, type filters, invoice hint and the document list.
struct DocumentsScreen: View {
    let model: DocumentsModel
    @Binding var filter: Int
    let onAction: (DocumentsAction) -> Void

    /// D8: the search field starts empty and filters nothing.
    @State private var query = ""

    private var filterIdentifiers: [String] {
        model.filters.map { "documents.filter.\($0.kind?.rawValue ?? "all")" }
    }

    private struct Style {
        let icon: BaupilotIcon
        let background: ColorToken
        let foreground: ColorToken
    }

    private func style(_ kind: DocumentKind) -> Style {
        switch kind {
        case .offer: return Style(icon: DocIcon.document.symbol, background: Tokens.iSoft, foreground: Tokens.iFg)
        case .contract: return Style(icon: DocIcon.contract.symbol, background: Tokens.oSoft, foreground: Tokens.oFg)
        case .invoice: return Style(icon: DocIcon.receipt.symbol, background: Tokens.aSoft, foreground: Tokens.aFg)
        case .plan: return Style(icon: DocIcon.map.symbol, background: Tokens.pSoft, foreground: Tokens.pFg)
        }
    }

    private var banner: some View {
        HintCard(
            icon: .clockW22, title: model.banner.title, subtitle: model.banner.sub, amount: model.banner.amount,
            background: Tokens.aSoft, tileBackground: Tokens.accent, tileForeground: Tokens.onAccent,
            subtitleColor: Tokens.aFg, action: nil)
    }

    private func row(_ row: DocumentsModel.Row, index: Int) -> some View {
        let style = style(row.kind)
        var action: (() -> Void)?
        if case let .document(id)? = row.target {
            action = { onAction(.openDocument(id)) }
        }
        return ListRow(
            title: row.title, subtitle: row.meta,
            leading: .iconTile(style.icon, size: .s38, bg: style.background, fg: style.foreground),
            trailing: row.amount.map {
                .amount($0, size: 14, weight: .semibold, color: row.kind == .invoice ? Tokens.aFg : Tokens.text)
            } ?? .none,
            density: .padded(v: 11),
            showsDivider: index > 0,
            action: action
        )
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("documents.row.\(row.id)")
    }

    private var recent: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionHeader(
                model.recentLabel,
                style: .title(link: SectionHeader.Link(title: model.byTradeLabel, action: nil)))
            Card(padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    let rows = model.rows(filter: filter)
                    ForEach(Array(rows.enumerated()), id: \.element.id) { index, item in
                        row(item, index: index)
                    }
                }
            }
        }
    }

    var body: some View {
        ScreenScaffold(kind: .tabRoot) {
            LargeTitleHeader(
                title: model.title, kicker: model.kicker,
                trailing: .add(label: model.addLabel, action: nil))
            SearchField(
                text: $query, placeholder: model.searchPlaceholder, accessibilityLabel: model.searchLabel,
                background: Tokens.chip.dokumente)
            FilterChips(
                items: model.filters.map(\.text), selection: filter, accessibilityLabel: model.filtersLabel,
                inactive: Tokens.chip.dokumente, identifiers: filterIdentifiers
            ) { filter = $0 }
            banner
            recent
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.documents")
    }
}
