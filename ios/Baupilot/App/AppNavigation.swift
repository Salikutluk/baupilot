import Foundation

enum AppTab: String, CaseIterable, Sendable { case home, schedule, inspections, documents, profile }

enum AppRoute: Hashable, Sendable {
    case trade(id: String)
    case notifications
}

struct AppNavigation: Equatable, Sendable {
    var isLoggedIn: Bool = false
    var selectedTab: AppTab = .home
    var paths: [AppTab: [AppRoute]] = [:]

    mutating func logIn() {
        isLoggedIn = true
        selectedTab = .home
        paths = [:]
    }

    /// Another tab: switch only. The same tab: pop to its root.
    mutating func select(_ tab: AppTab) {
        if tab == selectedTab {
            paths[tab] = nil
        } else {
            selectedTab = tab
        }
    }

    mutating func push(_ route: AppRoute) {
        guard isLoggedIn else { return }
        paths[selectedTab, default: []].append(route)
    }

    mutating func pop() {
        guard isLoggedIn, var path = paths[selectedTab], !path.isEmpty else { return }
        path.removeLast()
        paths[selectedTab] = path.isEmpty ? nil : path
    }

    /// `NavigationStack(path:)` binding writes. Logged out: no-op.
    mutating func setPath(_ path: [AppRoute], for tab: AppTab) {
        guard isLoggedIn else { return }
        paths[tab] = path.isEmpty ? nil : path
    }

    func path(for tab: AppTab) -> [AppRoute] { paths[tab] ?? [] }

    var showsTabBar: Bool { path(for: selectedTab).isEmpty }
}
