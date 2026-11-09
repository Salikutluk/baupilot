import SwiftUI

enum NotificationsAction {
    case open(NavigationTarget)
}

/// Mitteilungen (IosMitteilungen.dc.html): back bar with "Alle gelesen", title, one card of rows per group.
struct NotificationsScreen: View {
    let model: NotificationsModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (NotificationsAction) -> Void

    var body: some View {
        ScreenScaffold(kind: .detailNoBar, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack, trailing: .text(model.markAllLabel, action: nil))
            Text(model.title)
                .interText(32, .bold, tracking: -0.6)
                .foregroundStyle(Tokens.text.color)
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityAddTraits(.isHeader)
            ForEach(model.groups, id: \.title) { group in
                section(group)
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.notifications")
    }

    private func section(_ group: NotificationsModel.Group) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(group.title, style: .caps(inset: 4))
            Card(padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ForEach(Array(group.items.enumerated()), id: \.element.id) { index, item in
                        row(item, index: index)
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func row(_ item: NotificationsModel.Item, index: Int) -> some View {
        let tile = ScreenCopy.Mitteilungen.tile(item.kind)
        NotificationRow(
            icon: tile.icon, iconBackground: tile.background, iconForeground: tile.foreground,
            title: item.title, when: item.when, text: item.text, unread: item.unread,
            showsDivider: index > 0, action: action(item))
        .accessibilityIdentifier("notification.\(item.id)")
    }

    private func action(_ item: NotificationsModel.Item) -> (() -> Void)? {
        guard let target = item.target else { return nil }
        return { onAction(.open(target)) }
    }
}
