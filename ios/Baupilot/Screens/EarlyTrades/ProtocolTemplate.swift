import SwiftUI

/// Actions of the protocol screens (Erdarbeiten, Rohbau, Zimmerer); the shell turns them into navigation.
enum ProtocolAction {
    case open(NavigationTarget)
}

/// Shared body of the protocol screens (IosErd:17-54, IosRohbau): back bar, title block, the tone banner and one
/// card of rows per group. The banner follows `model.banner.tone`: `ok` carries the PDF button (no action), `warn`
/// a chevron and the whole banner opens the banner target. A row or banner target of `nil` means no action at all.
struct ProtocolTemplate: View {
    let model: TradeProtocolModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (ProtocolAction) -> Void

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .tight) {
            BackBar(label: backLabel, action: onBack)
            DetailTitle(model.title, subtitle: model.subtitle, style: .size30)
            banner
            ForEach(Array(model.groups.enumerated()), id: \.offset) { groupIndex, group in
                section(group, groupIndex: groupIndex)
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.inspection.\(model.tradeId)")
    }

    private var banner: some View {
        let banner = model.banner
        let isOk = banner.tone == .ok
        return ProtocolBanner(
            icon: isOk ? .checkCircleW24 : .alertTriangleW22,
            title: banner.title, text: banner.text,
            background: isOk ? Tokens.oSoft : Tokens.wSoft,
            foreground: isOk ? Tokens.oFg : Tokens.wFg.core,
            trailing: trailing(banner, isOk: isOk),
            action: isOk ? nil : openAction(banner.target)
        )
        .accessibilityIdentifier("protocol.banner")
    }

    private func trailing(_ banner: TradeProtocolModel.Banner, isOk: Bool) -> ProtocolBanner.Trailing {
        if !isOk { return .chevron }
        guard let label = banner.documentLabel else { return .none }
        return .document(label: label, action: nil)
    }

    private func section(_ group: TradeProtocolModel.Group, groupIndex: Int) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            SectionHeader(group.title, style: .caps(inset: 4))
            Card(style: .row, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ForEach(Array(group.rows.enumerated()), id: \.offset) { rowIndex, row in
                        ProtocolRow(
                            label: row.label, meta: row.meta, mark: row.mark == .ok ? .ok : .defect,
                            showsDivider: rowIndex > 0, action: openAction(row.target)
                        )
                        .accessibilityIdentifier("protocol.row.\(groupIndex).\(rowIndex)")
                    }
                }
            }
        }
    }

    /// A target makes the element a button; without one it has no action closure (M3 D5).
    private func openAction(_ target: NavigationTarget?) -> (() -> Void)? {
        guard let target else { return nil }
        return { onAction(.open(target)) }
    }
}

/// Shared stub body of Rohbau and Zimmerer until their screens use `ProtocolTemplate`: banner, optional PDF
/// button, one line per row.
struct ProtocolStubBody: View {
    let model: TradeProtocolModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (ProtocolAction) -> Void
    let showsDocument: Bool

    var body: some View {
        ScreenScaffold(kind: .protocol) {
            BackBar(label: backLabel, action: onBack)
            DetailTitle(model.title, subtitle: model.subtitle, style: .size28)
            Text("\(model.banner.title) \(model.banner.text)")
                .accessibilityIdentifier("protocol.banner")
            if showsDocument, let label = model.banner.documentLabel {
                actionView(label, target: model.banner.target)
                    .accessibilityIdentifier("protocol.document")
            }
            ForEach(Array(model.groups.enumerated()), id: \.offset) { groupIndex, group in
                Text(group.title)
                ForEach(Array(group.rows.enumerated()), id: \.offset) { rowIndex, row in
                    actionView("\(row.label) \(row.meta)", target: row.target)
                        .accessibilityIdentifier("protocol.row.\(groupIndex).\(rowIndex)")
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.inspection.\(model.tradeId)")
    }

    /// A target makes the line tappable; without one it has no action at all (M3 D5).
    @ViewBuilder
    private func actionView(_ text: String, target: NavigationTarget?) -> some View {
        if let target {
            Button(text) { onAction(.open(target)) }
        } else {
            Text(text)
        }
    }
}
