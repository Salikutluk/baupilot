import SwiftUI

/// Five navigation stacks, one per tab, with the tab bar drawn over them while the selected stack is at its root.
struct AppShell: View {
    @Binding var navigation: AppNavigation
    let data: AppData
    @State private var profileToggles: [String: Bool]
    @State private var documentsFilter: Int

    init(navigation: Binding<AppNavigation>, data: AppData) {
        _navigation = navigation
        self.data = data
        _profileToggles = State(initialValue: ProfileScreen.initialToggles(for: data.profile))
        _documentsFilter = State(initialValue: data.documents.initialFilter)
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
                TabBar(
                    selected: Self.barTab(navigation.selectedTab), border: tabBarBorder,
                    onSelect: { navigation.select(Self.appTab($0)) })
            }
        }
        .ignoresSafeArea()
    }

    /// The open-defects view draws the tab bar's top border in its own token (IosAbnMaengel.dc.html:56).
    private var tabBarBorder: ColorToken {
        navigation.selectedTab == .inspections && navigation.inspectionsView == .defects && navigation.showsTabBar
            ? Tokens.border.abnMaengel : Tokens.border.core
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
        case .documents:
            DocumentsScreen(model: data.documents, filter: $documentsFilter, onAction: handle)
        case .inspections:
            inspectionsRoot
                .accessibilityElement(children: .contain)
                .accessibilityIdentifier("screen.\(tab.rawValue)")
        }
    }

    /// Only the visible view is composed, so the hidden one never reaches the accessibility tree.
    @ViewBuilder
    private var inspectionsRoot: some View {
        switch navigation.inspectionsView {
        case .trades:
            InspectionsScreen(model: data.inspections, onAction: handle)
        case .defects:
            DefectsScreen(model: data.defectsBoard, onAction: handle)
        }
    }

    @ViewBuilder
    private func destination(_ route: AppRoute, from tab: AppTab) -> some View {
        let backLabel = backLabel(for: route, from: tab)
        let onBack = { navigation.pop() }
        let fallback = PlaceholderScreen.Presentation.pushed(backLabel: backLabel, onBack: onBack)
        switch route {
        case .notifications:
            NotificationsScreen(
                model: data.notifications, backLabel: backLabel, onBack: onBack, onAction: handle)
        case let .trade(id):
            if let model = data.trades[id] {
                GewerkScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle)
            } else {
                PlaceholderScreen(title: data.routeTitles.title(route), presentation: fallback)
                    .accessibilityIdentifier("screen.trade.\(id)")
            }
        case let .compare(tradeId):
            if let model = data.comparisons[tradeId] {
                CompareScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle)
            } else {
                PlaceholderScreen(title: data.routeTitles.title(route), presentation: fallback)
                    .accessibilityIdentifier("screen.compare.\(tradeId)")
            }
        case let .document(id):
            if let model = data.viewers[id] {
                DocViewerScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle)
            } else {
                PlaceholderScreen(title: data.routeTitles.title(route), presentation: fallback)
                    .accessibilityIdentifier("screen.document.\(id)")
            }
        case let .inspection(tradeId):
            inspectionDestination(tradeId, route: route, backLabel: backLabel, onBack: onBack, fallback: fallback)
        case let .inspectionStep(tradeId, elementId, step):
            if let model = data.inspectionSteps[route] {
                InspectionStepScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle)
            } else {
                PlaceholderScreen(title: data.routeTitles.title(route), presentation: fallback)
                    .accessibilityIdentifier("screen.inspection.\(tradeId).\(elementId).step.\(step)")
            }
        case .newDefect:
            DefectFormScreen(model: data.defectForm, onAction: handle)
        case let .signOff(inspectionId):
            if let model = data.signOffs[inspectionId] {
                SignOffScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle)
            } else {
                PlaceholderScreen(title: data.routeTitles.title(route), presentation: fallback)
                    .accessibilityIdentifier("screen.inspection.\(inspectionId).sign")
            }
        case let .inspectionCheck(tradeId, check):
            if let model = data.measureChecks[route] {
                RohbauMasseScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle)
            } else if let model = data.roomChecks[route] {
                ElRaumScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle)
            } else if let model = data.panelChecks[route] {
                ElVerteilerScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle)
            } else if let model = data.smokeChecks[route] {
                ElRauchmelderScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle)
            } else {
                PlaceholderScreen(title: data.routeTitles.title(route), presentation: fallback)
                    .accessibilityIdentifier("screen.inspection.\(tradeId).\(check)")
            }
        case let .inspectionRoom(tradeId, roomId):
            if let model = data.roomChecks[route] {
                ElRaumScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle)
            } else {
                PlaceholderScreen(title: data.routeTitles.title(route), presentation: fallback)
                    .accessibilityIdentifier("screen.inspection.\(tradeId).room.\(roomId)")
            }
        }
    }

    /// The lookup chain of an inspection trade (D2): element list, protocol, electrical overview, placeholder.
    @ViewBuilder
    private func inspectionDestination(
        _ tradeId: String, route: AppRoute, backLabel: String, onBack: @escaping () -> Void,
        fallback: PlaceholderScreen.Presentation
    ) -> some View {
        if let model = data.inspectionElements[tradeId] {
            ElementsScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle)
        } else if let model = data.tradeProtocols[tradeId], let screen = protocolScreen(
            model, backLabel: backLabel, onBack: onBack) {
            screen
        } else if let model = data.electricalOverviews[tradeId] {
            ElUebersichtScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle)
        } else {
            PlaceholderScreen(title: data.routeTitles.title(route), presentation: fallback)
                .accessibilityIdentifier("screen.inspection.\(tradeId)")
        }
    }

    /// Earthworks, shell and carpentry each have their own protocol screen.
    private func protocolScreen(
        _ model: TradeProtocolModel, backLabel: String, onBack: @escaping () -> Void
    ) -> AnyView? {
        switch model.tradeId {
        case "earthworks":
            return AnyView(ErdScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle))
        case "shell":
            return AnyView(RohbauScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle))
        case "carpentry":
            return AnyView(ZimmererScreen(model: model, backLabel: backLabel, onBack: onBack, onAction: handle))
        default:
            return nil
        }
    }

    /// Title of the screen below `route` on its stack, or the tab label for the first entry.
    private func backLabel(for route: AppRoute, from tab: AppTab) -> String {
        let path = navigation.path(for: tab)
        return data.routeTitles.backLabel(path: path, index: path.firstIndex(of: route) ?? 0, tab: tab)
    }

    private func handle(_ action: HomeAction) {
        switch action {
        case .openNotifications: navigation.open(.notifications)
        case .openScheduleTab: navigation.go(.tab(.schedule))
        case let .openTrade(id): navigation.open(.trade(id: id))
        case .openDocumentsTab: navigation.go(.tab(.documents))
        }
    }

    private func handle(_ action: ScheduleAction) {
        switch action {
        case let .openTrade(id): navigation.open(.trade(id: id))
        }
    }

    private func handle(_ action: GewerkAction) {
        switch action {
        case let .openDocument(id): navigation.open(.document(id: id))
        case let .openCompare(tradeId): navigation.open(.compare(tradeId: tradeId))
        }
    }

    private func handle(_ action: DocumentsAction) {
        switch action {
        case let .openDocument(id): navigation.open(.document(id: id))
        }
    }

    private func handle(_ action: DocViewerAction) {
        switch action {
        case let .openCompare(tradeId): navigation.open(.compare(tradeId: tradeId))
        }
    }

    private func handle(_ action: CompareAction) {
        switch action {
        case let .openDocument(id): navigation.open(.document(id: id))
        }
    }

    private func handle(_ action: NotificationsAction) {
        switch action {
        case let .open(target): navigation.go(target)
        }
    }

    private func handle(_ action: InspectionsAction) {
        switch action {
        case let .selectView(view): navigation.selectInspectionsView(view)
        case let .open(route): navigation.open(route)
        }
    }

    private func handle(_ action: DefectsAction) {
        switch action {
        case let .selectView(view): navigation.selectInspectionsView(view)
        }
    }

    private func handle(_ action: ElementsAction) {
        switch action {
        case let .open(route): navigation.open(route)
        }
    }

    private func handle(_ action: StepAction) {
        switch action {
        case let .open(route): navigation.open(route)
        }
    }

    private func handle(_ action: DefectFormAction) {
        switch action {
        case .cancel:
            navigation.pop()
        case let .save(tradeId):
            navigation.saveDefect(formTradeId: tradeId, hasSignOff: data.signOffs[tradeId] != nil)
        }
    }

    private func handle(_ action: ProtocolAction) {
        switch action {
        case let .open(target): navigation.go(target)
        }
    }

    private func handle(_ action: ElectricalAction) {
        switch action {
        case let .open(route): navigation.open(route)
        }
    }

    private func handle(_ action: MeasureAction) {
        switch action {
        case let .open(route): navigation.open(route)
        }
    }

    private func handle(_ action: CheckStepAction) {
        switch action {
        case let .open(route): navigation.open(route)
        }
    }

    private func handle(_ action: SignOffAction) {
        switch action {
        case let .finish(target): navigation.go(target)
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
