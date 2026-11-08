import Foundation

/// Sendable stand-in for `StatusBadge.Tone`, which is not `Sendable`.
enum TradeTone: Sendable, Equatable {
    case ok, info, accent

    var badgeTone: StatusBadge.Tone {
        switch self {
        case .ok: return .ok
        case .info: return .info
        case .accent: return .accent
        }
    }
}

/// Sendable stand-in for the `BaupilotIcon` of a document tile.
enum DocIcon: Sendable, Equatable {
    case document, contract, receipt, map

    var symbol: BaupilotIcon {
        switch self {
        case .document: return .document
        case .contract: return .contract
        case .receipt: return .receipt
        case .map: return .map
        }
    }
}

struct AppTabItem: Sendable, Equatable {
    let tab: AppTab
    let label: String
}

struct LoginModel: Sendable, Equatable {
    let features: [String]
    let title: String
    let headline: [String]
    let intro: String
    let emailLabel: String
    let emailPlaceholder: String
    let passwordLabel: String
    let forgot: String
    let submit: String
    let social: String
    let newHere: String
    let create: String
}

struct HomeModel: Sendable {
    struct TradeRow: Sendable {
        let tradeId: String
        let name: String
        let kw: String
        let when: String
        let statusLabel: String
        let tone: TradeTone
    }

    struct DocTile: Sendable {
        let label: String
        let count: String
        let icon: DocIcon
    }

    let projectLabel: String
    let notificationsAria: String
    let hasUnreadNotifications: Bool
    let phaseLabel: String
    let phaseTitle: String
    let progress: Double
    let constructionLabel: String
    let moveInLabel: String
    let trades: [TradeRow]
    let docs: [DocTile]
    let tabs: [AppTabItem]
}

struct ScheduleModel: Sendable {
    struct AskRow: Sendable {
        let name: String
        let sub: String
        let target: AppRoute?
    }

    let moveInLabel: String
    let ariaLabel: String
    let asks: [AskRow]
    let layout: ScheduleLayout
    let tabs: [AppTabItem]
}

struct ProfileModel: Sendable {
    struct Row: Sendable {
        let label: String
        let value: String?
        let isLink: Bool
        let isToggle: Bool
        let checked: Bool?
    }

    struct Group: Sendable {
        let title: String
        let rows: [Row]
    }

    let initials: String
    let name: String
    let contact: String
    let groups: [Group]
    let tabs: [AppTabItem]
}

struct AppData: Sendable {
    let login: LoginModel
    let home: HomeModel
    let schedule: ScheduleModel
    let profile: ProfileModel
    let tradeNames: [String: String]

    /// Fixed iOS content width of the schedule chart (not measured).
    private static let chartWidth: CGFloat = 350

    nonisolated static func load(from repo: BackendRepository) async throws -> AppData {
        let loginText = try await repo.screenText("Login", as: LoginScreenText.self)
        let homeText = try await repo.screenText("Home", as: HomeScreenText.self)
        let zeitplanText = try await repo.screenText("Zeitplan", as: ZeitplanScreenText.self)
        let profilText = try await repo.screenText("Profil", as: ProfilScreenText.self)
        let project = try await repo.project()
        let trades = try await repo.trades()
        let schedule = try await repo.schedule()

        let tradeNames = Dictionary(trades.map { ($0.id, $0.name) }, uniquingKeysWith: { first, _ in first })
        guard let phaseTitle = tradeNames[project.phase.tradeId] else {
            throw RepositoryError.decoding("project.phase.tradeId")
        }

        return AppData(
            login: LoginModel(
                features: loginText.features, title: loginText.title, headline: loginText.headline,
                intro: loginText.intro, emailLabel: loginText.emailLabel,
                emailPlaceholder: loginText.emailPlaceholder, passwordLabel: loginText.passwordLabel,
                forgot: loginText.forgot, submit: loginText.submit, social: loginText.social,
                newHere: loginText.newHere, create: loginText.create),
            home: HomeModel(
                projectLabel: homeText.projectLabel,
                notificationsAria: homeText.notificationsAria,
                hasUnreadNotifications: project.unreadNotifications > 0,
                phaseLabel: homeText.phaseLabel,
                phaseTitle: phaseTitle,
                progress: Double(project.phase.progressPercent) / 100,
                constructionLabel: homeText.constructionLabel,
                moveInLabel: homeText.moveInLabel,
                trades: homeText.trades.map { trade in
                    HomeModel.TradeRow(
                        tradeId: trade.tradeId, name: trade.name, kw: trade.kw, when: trade.when,
                        statusLabel: trade.statusLabel,
                        tone: ScreenCopy.Home.tradeTones[trade.tradeId] ?? ScreenCopy.Home.defaultTradeTone)
                },
                docs: homeText.docs.enumerated().map { index, doc in
                    HomeModel.DocTile(
                        label: doc.label, count: doc.count,
                        icon: index < ScreenCopy.Home.docIcons.count ? ScreenCopy.Home.docIcons[index] : .document)
                },
                tabs: try tabItems(homeText.tabs, in: "Home")),
            schedule: ScheduleModel(
                moveInLabel: zeitplanText.moveInLabel,
                ariaLabel: zeitplanText.ariaLabel,
                asks: try schedule.asks.map { ask in
                    ScheduleModel.AskRow(name: ask.name, sub: ask.sub, target: try route(forAskTarget: ask.target))
                },
                layout: ScheduleLayout.make(trades: trades, schedule: schedule, chartWidth: chartWidth),
                tabs: try tabItems(zeitplanText.tabs, in: "Zeitplan")),
            profile: ProfileModel(
                initials: project.owner.initials,
                name: project.owner.name,
                contact: project.owner.email + ScreenCopy.Profil.contactSeparator + project.owner.role,
                groups: profilText.groups.map { group in
                    ProfileModel.Group(
                        title: group.title,
                        rows: group.rows.map {
                            ProfileModel.Row(
                                label: $0.label, value: $0.value, isLink: $0.isLink,
                                isToggle: $0.isToggle, checked: $0.checked)
                        })
                },
                tabs: try tabItems(profilText.tabs, in: "Profil")),
            tradeNames: tradeNames)
    }

    private nonisolated static func tabItems(_ tabs: [TabText], in screen: String) throws -> [AppTabItem] {
        try tabs.map { item in
            guard let tab = AppTab(rawValue: item.route) else {
                throw RepositoryError.decoding("screens.\(screen).ios.tabs.route")
            }
            return AppTabItem(tab: tab, label: item.label)
        }
    }

    private nonisolated static func route(forAskTarget target: String?) throws -> AppRoute? {
        guard let target else { return nil }
        let prefix = "trade/"
        guard target.hasPrefix(prefix), target.count > prefix.count else {
            throw RepositoryError.decoding("schedule.asks.target")
        }
        return .trade(id: String(target.dropFirst(prefix.count)))
    }
}
