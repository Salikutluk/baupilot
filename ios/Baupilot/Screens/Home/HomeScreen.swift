import SwiftUI

enum HomeAction {
    case openNotifications, openScheduleTab, openTrade(String), openDocumentsTab
}

/// Übersicht (IosHome.dc.html): header with bell, phase card, trade list and document grid.
struct HomeScreen: View {
    let model: HomeModel
    let onAction: (HomeAction) -> Void

    private var phaseCard: some View {
        Card(style: .hero, padding: EdgeInsets(top: 18, leading: 18, bottom: 18, trailing: 18)) {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Text(ScreenCopy.Home.currentPhase)
                    Spacer(minLength: 0)
                    Text(model.phaseLabel)
                }
                .interText(13, .semibold)
                .foregroundStyle(Tokens.muted.color)
                Text(model.phaseTitle)
                    .interText(24, .bold, tracking: -0.4)
                    .foregroundStyle(Tokens.text.color)
                ProgressBar(value: model.progress, style: .phase, track: Tokens.track.home)
                HStack {
                    Text(model.constructionLabel)
                    Spacer(minLength: 0)
                    Text(model.moveInLabel)
                }
                .interText(13, .regular)
                .foregroundStyle(Tokens.muted.color)
            }
        }
    }

    private var tradeList: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionHeader(
                ScreenCopy.Home.nextNeeded,
                style: .title(
                    link: SectionHeader.Link(
                        title: ScreenCopy.Home.scheduleLink, action: { onAction(.openScheduleTab) })))
            Card(style: .hero, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ForEach(Array(model.trades.enumerated()), id: \.offset) { index, trade in
                        ListRow(
                            title: trade.name, subtitle: trade.when,
                            leading: .textTile(trade.kw, .s48x40),
                            trailing: .badgeAndChevron(
                                StatusBadge(trade.statusLabel, tone: trade.tone.badgeTone, size: .roomy),
                                .regular),
                            titleSize: .large,
                            showsDivider: index > 0,
                            action: { onAction(.openTrade(trade.tradeId)) })
                    }
                }
            }
        }
    }

    private var documents: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionHeader(ScreenCopy.Home.documents, style: .title())
            Grid(horizontalSpacing: 10, verticalSpacing: 10) {
                ForEach(0..<((model.docs.count + 1) / 2), id: \.self) { row in
                    GridRow {
                        ForEach(model.docs[(row * 2)..<min(row * 2 + 2, model.docs.count)].indices, id: \.self) { index in
                            let doc = model.docs[index]
                            DocumentTile(
                                icon: doc.icon.symbol, label: doc.label, count: doc.count,
                                action: { onAction(.openDocumentsTab) })
                        }
                    }
                }
            }
        }
    }

    /// Kicker, title and bell. The header row (44, title bottom-aligned with the bell) is pulled up under the
    /// kicker: the stack is as tall as the header with its own kicker.
    private var header: some View {
        let titleRow: CGFloat = 44
        return VStack(alignment: .leading, spacing: 2 - (titleRow - InterMetrics.lineBox(32))) {
            Text(model.projectLabel)
                .interText(13, .semibold)
                .foregroundStyle(Tokens.muted.color)
            LargeTitleHeader(
                title: ScreenCopy.Home.title,
                trailing: .bell(
                    unread: model.hasUnreadNotifications, label: model.notificationsAria,
                    action: { onAction(.openNotifications) }))
        }
    }

    var body: some View {
        ScreenScaffold(kind: .tabRoot, sectionSpacing: .loose) {
            header
            phaseCard
            tradeList
            documents
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.home")
    }
}
