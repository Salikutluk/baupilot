import SwiftUI

/// Five navigation stacks, one per tab, with the tab bar drawn over them while the selected stack is at its root.
struct AppShell: View {
    @Binding var navigation: AppNavigation
    let data: AppData
    @State private var profileToggles: [String: Bool]

    init(navigation: Binding<AppNavigation>, data: AppData) {
        _navigation = navigation
        self.data = data
        _profileToggles = State(initialValue: ProfileScreen.initialToggles(for: data.profile))
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            ZStack {
                ForEach(AppTab.allCases, id: \.self) { tab in
                    stack(for: tab)
                        .opacity(tab == navigation.selectedTab ? 1 : 0)
                        .allowsHitTesting(tab == navigation.selectedTab)
                        .hiddenUnless(tab == navigation.selectedTab)
                        .accessibilityHidden(tab != navigation.selectedTab)
                }
            }
            if navigation.showsTabBar {
                TabBar(selected: Self.barTab(navigation.selectedTab), onSelect: { navigation.select(Self.appTab($0)) })
            }
        }
        .ignoresSafeArea()
    }

    private func stack(for tab: AppTab) -> some View {
        NavigationStack(path: pathBinding(for: tab)) {
            root(for: tab)
                .toolbar(.hidden, for: .navigationBar)
                .navigationDestination(for: AppRoute.self) { route in
                    destination(route, from: tab)
                        .toolbar(.hidden, for: .navigationBar)
                }
        }
    }

    private func pathBinding(for tab: AppTab) -> Binding<[AppRoute]> {
        Binding(
            get: { navigation.path(for: tab) },
            set: { navigation.setPath($0, for: tab) })
    }

    @ViewBuilder
    private func root(for tab: AppTab) -> some View {
        switch tab {
        case .home:
            HomeScreen(model: data.home, onAction: handle)
        case .schedule:
            ScheduleScreen(model: data.schedule, onAction: handle)
        case .profile:
            ProfileScreen(model: data.profile, toggles: $profileToggles)
        case .inspections, .documents:
            PlaceholderScreen(title: label(of: tab), presentation: .tabRoot)
                .accessibilityIdentifier("screen.\(tab.rawValue)")
        }
    }

    @ViewBuilder
    private func destination(_ route: AppRoute, from tab: AppTab) -> some View {
        let back = PlaceholderScreen.Presentation.pushed(backLabel: label(of: tab), onBack: { navigation.pop() })
        switch route {
        case .notifications:
            PlaceholderScreen(title: ScreenCopy.notificationsTitle, presentation: back)
                .accessibilityIdentifier("screen.notifications")
        case let .trade(id):
            PlaceholderScreen(title: data.tradeNames[id] ?? "", presentation: back)
                .accessibilityIdentifier("screen.trade.\(id)")
        }
    }

    /// Tab label from the screen models; every tab bar of the fixture lists the same five.
    private func label(of tab: AppTab) -> String {
        data.home.tabs.first { $0.tab == tab }?.label ?? ""
    }

    private func handle(_ action: HomeAction) {
        switch action {
        case .openNotifications: navigation.push(.notifications)
        case .openScheduleTab: navigation.select(.schedule)
        case let .openTrade(id): navigation.push(.trade(id: id))
        case .openDocumentsTab: navigation.select(.documents)
        }
    }

    private func handle(_ action: ScheduleAction) {
        switch action {
        case let .openTrade(id): navigation.push(.trade(id: id))
        }
    }

    private static func barTab(_ tab: AppTab) -> TabBar.Tab {
        switch tab {
        case .home: return .overview
        case .schedule: return .schedule
        case .inspections: return .inspections
        case .documents: return .documents
        case .profile: return .profile
        }
    }

    private static func appTab(_ tab: TabBar.Tab) -> AppTab {
        switch tab {
        case .overview: return .home
        case .schedule: return .schedule
        case .inspections: return .inspections
        case .documents: return .documents
        case .profile: return .profile
        }
    }
}

private extension View {
    /// `.hidden()` also takes the stack's UIKit-hosted subtree out of the accessibility tree, which
    /// `accessibilityHidden` alone does not (the UI test lists every tab's `screen.*` identifier without it).
    /// It switches the view's identity, so a hidden tab's `@State` and scroll position are not kept: state that
    /// must survive a tab switch lives in the shell (`profileToggles`) or the navigation model.
    @ViewBuilder
    func hiddenUnless(_ visible: Bool) -> some View {
        if visible { self } else { hidden() }
    }
}
