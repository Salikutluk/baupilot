import Foundation

enum AppTab: String, CaseIterable, Sendable { case home, schedule, inspections, documents, profile }

enum AppRoute: Hashable, Sendable {
    case trade(id: String)
    case compare(tradeId: String)
    case document(id: String)
    case notifications
    case inspection(tradeId: String)
    case inspectionStep(tradeId: String, elementId: String, step: Int)
    case newDefect
    case signOff(inspectionId: String)
    case inspectionCheck(tradeId: String, check: String)
    case inspectionRoom(tradeId: String, roomId: String)
    case inspectionCheckItem(tradeId: String, check: String, item: String)
}

/// The two views of the Abnahmen tab root (the "Ansicht" tablist of IosAbnGewerke / IosAbnMaengel).
enum InspectionsView: String, CaseIterable, Sendable { case trades, defects }

/// What a target string from the fixture opens: a pushed route or a whole tab.
enum NavigationTarget: Equatable, Sendable {
    case push(AppRoute)
    case tab(AppTab)
    /// The open-defects view of the Abnahmen tab root.
    case defects
}

struct AppNavigation: Equatable, Sendable {
    var isLoggedIn: Bool = false
    var selectedTab: AppTab = .home
    var paths: [AppTab: [AppRoute]] = [:]
    var inspectionsView: InspectionsView = .trades

    mutating func logIn() {
        isLoggedIn = true
        selectedTab = .home
        paths = [:]
        inspectionsView = .trades
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

    /// Pushes `route` on the selected tab. A route already on that stack is not added again: the stack is
    /// truncated after it, so a path built here never holds a route twice.
    mutating func open(_ route: AppRoute) {
        guard isLoggedIn else { return }
        let path = path(for: selectedTab)
        if let index = path.firstIndex(of: route) {
            paths[selectedTab] = Array(path[...index])
        } else {
            paths[selectedTab] = path + [route]
        }
    }

    /// Pushes `anchor`, then `route` on top of it, in one state change (each through `open`, so no duplicates).
    mutating func open(_ route: AppRoute, above anchor: AppRoute) {
        guard isLoggedIn else { return }
        open(anchor)
        open(route)
    }

    /// Changes the view of the Abnahmen root only; tab and stacks stay.
    mutating func selectInspectionsView(_ view: InspectionsView) {
        guard isLoggedIn else { return }
        inspectionsView = view
    }

    /// The open-defects list is a root view: selects the Abnahmen tab, clears its stack and shows that view.
    mutating func showDefects() {
        guard isLoggedIn else { return }
        selectedTab = .inspections
        paths[.inspections] = nil
        inspectionsView = .defects
    }

    mutating func go(_ target: NavigationTarget) {
        switch target {
        case let .push(route): open(route)
        case let .tab(tab): select(tab)
        case .defects: showDefects()
        }
    }

    mutating func pop() {
        guard isLoggedIn, var path = paths[selectedTab], !path.isEmpty else { return }
        path.removeLast()
        paths[selectedTab] = path.isEmpty ? nil : path
    }

    /// Saving the defect form: pops it. Opened from the element list or a step of `formTradeId`, the M4 flow
    /// continues instead (sign-off above the list when the trade has one, else the list). No-op unless
    /// `.newDefect` is on top of the selected tab's stack.
    mutating func saveDefect(formTradeId: String, hasSignOff: Bool) {
        guard isLoggedIn, let path = paths[selectedTab], path.last == .newDefect else { return }
        let below = path.count >= 2 ? path[path.count - 2] : nil
        switch below {
        case .inspection(formTradeId), .inspectionStep(formTradeId, _, _):
            let list = AppRoute.inspection(tradeId: formTradeId)
            if hasSignOff {
                open(.signOff(inspectionId: formTradeId), above: list)
            } else {
                open(list)
            }
        default:
            pop()
        }
    }

    /// `NavigationStack(path:)` binding writes. Logged out: no-op.
    mutating func setPath(_ path: [AppRoute], for tab: AppTab) {
        guard isLoggedIn else { return }
        paths[tab] = path.isEmpty ? nil : path
    }

    func path(for tab: AppTab) -> [AppRoute] { paths[tab] ?? [] }

    var showsTabBar: Bool { path(for: selectedTab).isEmpty }
}
