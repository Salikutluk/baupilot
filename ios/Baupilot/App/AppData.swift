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

enum DocumentKind: String, Sendable, Equatable { case offer, contract, invoice, plan }

enum NotificationKind: String, Sendable, Equatable { case frist, geld, angebot, termin }

struct LabeledValue: Sendable, Equatable {
    let label: String
    let value: String
}

struct GewerkModel: Sendable {
    struct OfferRow: Sendable, Equatable {
        let id: String
        let firm: String
        let meta: String
        let price: String
        let best: Bool
        let pending: Bool
        let target: AppRoute?
    }

    struct Step: Sendable, Equatable {
        let label: String
        let done: Bool
    }

    let tradeId: String
    let title: String
    let statusLabel: String
    let phaseLabel: String
    let facts: [LabeledValue]
    let offersHeading: String
    let offersSummary: String
    let offers: [OfferRow]
    let bestLabel: String
    let stepsHeading: String
    let steps: [Step]
    let compareLabel: String
    let compareTarget: AppRoute?
    let addLabel: String
    let moreLabel: String
}

struct PendingOffer: Sendable, Equatable {
    let firm: String
    let text: String
}

struct CompareModel: Sendable {
    struct Column: Sendable, Equatable {
        let offerId: String
        let name: String
        let best: Bool
        let target: AppRoute?
    }

    struct Row: Sendable, Equatable {
        let label: String
        let a: String
        let b: String
        let strong: Bool
        let highlighted: Bool
    }

    let tradeId: String
    let title: String
    let subtitle: String
    let deadlineNote: String
    let tableLabel: String
    let columns: [Column]
    let rows: [Row]
    let note: String
    let pending: PendingOffer?
    let remindLabel: String
    let askLabel: String
    let awardLabel: String
}

struct DocumentsModel: Sendable {
    struct Filter: Sendable, Equatable {
        let text: String
        let kind: DocumentKind?
    }

    struct Banner: Sendable, Equatable {
        let title: String
        let sub: String
        let amount: String
    }

    struct Row: Sendable, Equatable {
        let id: String
        let kind: DocumentKind
        let title: String
        let meta: String
        let amount: String?
        let target: AppRoute?
    }

    let kicker: String
    let title: String
    let countBadge: String
    let addLabel: String
    let searchLabel: String
    let searchPlaceholder: String
    let filtersLabel: String
    let filters: [Filter]
    let initialFilter: Int
    let banner: Banner
    let recentLabel: String
    let byTradeLabel: String
    let rows: [Row]

    /// Index 0 shows all; index i >= 1 the kind of filter i; an index out of range shows all.
    func rows(filter: Int) -> [Row] {
        guard filters.indices.contains(filter), let kind = filters[filter].kind else { return rows }
        return rows.filter { $0.kind == kind }
    }
}

struct DocViewerModel: Sendable {
    struct Line: Sendable, Equatable {
        let pos: String
        let text: String
        let sum: String
    }

    let id: String
    let title: String
    let subtitle: String
    let pageLabel: String
    let pageFooter: String
    let issuerName: String
    let issuerAddress: String
    let number: String
    let reference: String
    let lines: [Line]
    let totalLabel: String
    let total: String
    let facts: [LabeledValue]
    let compareLabel: String
    let compareTarget: AppRoute?
    let shareLabel: String
}

struct NotificationsModel: Sendable {
    struct Item: Sendable, Equatable {
        let id: String
        let kind: NotificationKind
        let title: String
        let text: String
        let when: String
        let unread: Bool
        let target: NavigationTarget?
    }

    struct Group: Sendable, Equatable {
        let title: String
        let items: [Item]
    }

    let title: String
    let markAllLabel: String
    let groups: [Group]
    let unreadCount: Int
}

struct AppData: Sendable {
    let login: LoginModel
    let home: HomeModel
    let schedule: ScheduleModel
    let profile: ProfileModel
    let tradeNames: [String: String]
    let routeTitles: RouteTitles
    let trades: [String: GewerkModel]
    let comparisons: [String: CompareModel]
    let documents: DocumentsModel
    let viewers: [String: DocViewerModel]
    let notifications: NotificationsModel

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
        let documents = try await repo.documents()
        let gewerkText = try await repo.screenText("Gewerk", as: GewerkScreenText.self)
        let dokumenteText = try await repo.screenText("Dokumente", as: DokumenteScreenText.self)
        _ = try await repo.screenText("DokViewer", as: EmptyScreenText.self)
        _ = try await repo.screenText("Vergleich", as: EmptyScreenText.self)
        _ = try await repo.screenText("Mitteilungen", as: EmptyScreenText.self)
        let offers = try await repo.offers(tradeId: nil)
        let notificationGroups = try await repo.notifications()
        var comparisonsByTrade: [String: Comparison] = [:]
        for trade in trades {
            if let comparison = try await repo.comparison(tradeId: trade.id) {
                comparisonsByTrade[trade.id] = comparison
            }
        }
        let viewerIds = Set(documents.items.filter { $0.viewer != nil }.map(\.id))

        let tradeNames = Dictionary(trades.map { ($0.id, $0.name) }, uniquingKeysWith: { first, _ in first })
        guard let phaseTitle = tradeNames[project.phase.tradeId] else {
            throw RepositoryError.decoding("project.phase.tradeId")
        }

        let homeTabs = try tabItems(homeText.tabs, in: "Home")
        let routeTitles = RouteTitles(
            tabLabels: Dictionary(homeTabs.map { ($0.tab, $0.label) }, uniquingKeysWith: { first, _ in first }),
            tradeNames: tradeNames,
            documentTitles: Dictionary(
                documents.items.map { ($0.id, $0.title) }, uniquingKeysWith: { first, _ in first }),
            compareTitle: RouteTitles.compareTitle,
            notificationsTitle: ScreenCopy.notificationsTitle)

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
                    ScheduleModel.AskRow(
                        name: ask.name, sub: ask.sub,
                        target: try RouteParser.route(ask.target, field: "schedule.asks.target"))
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
            tradeNames: tradeNames,
            routeTitles: routeTitles,
            trades: try gewerkModels(
                trades: trades, offers: offers, steps: gewerkText.steps,
                comparisonTradeIds: Set(comparisonsByTrade.keys), viewerIds: viewerIds),
            comparisons: try compareModels(comparisonsByTrade, offers: offers, viewerIds: viewerIds),
            documents: try documentsModel(
                documents, text: dokumenteText, viewerIds: viewerIds,
                title: try tabItems(dokumenteText.tabs, in: "Dokumente").first { $0.tab == .documents }?.label),
            viewers: try viewerModels(documents.items, comparisonTradeIds: Set(comparisonsByTrade.keys)),
            notifications: try notificationsModel(notificationGroups))
    }

    private nonisolated static func gewerkModels(
        trades: [Trade], offers: [Offer], steps: [GewerkScreenText.Step],
        comparisonTradeIds: Set<String>, viewerIds: Set<String>
    ) throws -> [String: GewerkModel] {
        var models: [String: GewerkModel] = [:]
        for trade in trades {
            guard let detail = trade.detail else { continue }
            guard let offersSummary = detail.offersSummary.value else {
                throw RepositoryError.decoding("trades.detail.offersSummary")
            }
            guard let statusLabel = trade.statusLabel else { throw RepositoryError.decoding("trades.statusLabel") }
            guard steps.count == ScreenCopy.Gewerk.stepsDone.count else {
                throw RepositoryError.decoding("screens.Gewerk.ios.steps")
            }
            models[trade.id] = GewerkModel(
                tradeId: trade.id,
                title: trade.name,
                statusLabel: statusLabel,
                phaseLabel: detail.phaseLabel,
                facts: detail.facts.map { LabeledValue(label: $0.label, value: $0.value) },
                offersHeading: ScreenCopy.Gewerk.offersHeading,
                offersSummary: offersSummary,
                offers: try offers.filter { $0.tradeId == trade.id }.map { offer in
                    guard let meta = offer.meta.value else { throw RepositoryError.decoding("offers.meta") }
                    return GewerkModel.OfferRow(
                        id: offer.id, firm: offer.firm, meta: meta, price: offer.price, best: offer.best,
                        pending: offer.pending, target: documentTarget(offer.documentId, viewerIds: viewerIds))
                },
                bestLabel: ScreenCopy.Gewerk.bestLabel,
                stepsHeading: ScreenCopy.Gewerk.stepsHeading,
                steps: zip(steps, ScreenCopy.Gewerk.stepsDone).map { GewerkModel.Step(label: $0.label, done: $1) },
                compareLabel: ScreenCopy.Gewerk.compareLabel,
                compareTarget: comparisonTradeIds.contains(trade.id) ? .compare(tradeId: trade.id) : nil,
                addLabel: ScreenCopy.Gewerk.addLabel,
                moreLabel: ScreenCopy.Gewerk.moreLabel)
        }
        return models
    }

    /// `.document(id)` iff the document has a viewer (D5).
    private nonisolated static func documentTarget(_ documentId: String?, viewerIds: Set<String>) -> AppRoute? {
        guard let documentId, viewerIds.contains(documentId) else { return nil }
        return .document(id: documentId)
    }

    private nonisolated static func compareModels(
        _ comparisons: [String: Comparison], offers: [Offer], viewerIds: Set<String>
    ) throws -> [String: CompareModel] {
        try comparisons.mapValues { comparison in
            let columnOffers = comparison.offerIds.compactMap { id in
                offers.first { $0.id == id && $0.tradeId == comparison.tradeId }
            }
            guard columnOffers.count == comparison.offerIds.count, comparison.columns.count == columnOffers.count else {
                throw RepositoryError.decoding("comparisons.offerIds")
            }
            guard let subtitle = comparison.hint.value else { throw RepositoryError.decoding("comparisons.hint") }
            guard let note = comparison.summary.value else { throw RepositoryError.decoding("comparisons.summary") }
            var pending: PendingOffer?
            if let offer = offers.first(where: { $0.tradeId == comparison.tradeId && $0.pending }) {
                guard let text = comparison.pending.value else { throw RepositoryError.decoding("comparisons.pending") }
                pending = PendingOffer(firm: offer.firm, text: text)
            }
            return CompareModel(
                tradeId: comparison.tradeId,
                title: ScreenCopy.Vergleich.title,
                subtitle: subtitle,
                deadlineNote: ScreenCopy.Vergleich.deadlineNote,
                tableLabel: ScreenCopy.Vergleich.tableLabel,
                columns: zip(comparison.columns, columnOffers).map { name, offer in
                    CompareModel.Column(
                        offerId: offer.id, name: name, best: offer.best, target: documentTarget(offer.documentId, viewerIds: viewerIds))
                },
                rows: comparison.rows.map {
                    CompareModel.Row(
                        label: $0.label, a: $0.a, b: $0.b, strong: $0.strong, highlighted: $0.a != $0.b && !$0.strong)
                },
                note: note,
                pending: pending,
                remindLabel: ScreenCopy.Vergleich.remindLabel,
                askLabel: ScreenCopy.Vergleich.askLabel,
                awardLabel: ScreenCopy.Vergleich.awardLabel)
        }
    }

    private nonisolated static func documentsModel(
        _ documents: Documents, text: DokumenteScreenText, viewerIds: Set<String>, title: String?
    ) throws -> DocumentsModel {
        let filterPath = "screens.Dokumente.ios.filters"
        guard text.filters.count == documents.categories.count + 1 else { throw RepositoryError.decoding(filterPath) }
        var filters = [DocumentsModel.Filter(text: text.filters[0].text, kind: nil)]
        for (category, filter) in zip(documents.categories, text.filters.dropFirst()) {
            guard category.label + " " + category.count == filter.text else { throw RepositoryError.decoding(filterPath) }
            guard let kind = DocumentKind(rawValue: category.kind) else {
                throw RepositoryError.decoding("documents.categories.kind")
            }
            filters.append(DocumentsModel.Filter(text: filter.text, kind: kind))
        }
        guard let title else { throw RepositoryError.decoding("screens.Dokumente.ios.tabs.route") }
        return DocumentsModel(
            kicker: text.headerLabel,
            title: title,
            countBadge: String(documents.counts.all),
            addLabel: ScreenCopy.Dokumente.addLabel,
            searchLabel: text.searchLabel,
            searchPlaceholder: text.searchPlaceholder,
            filtersLabel: ScreenCopy.Dokumente.filtersLabel,
            filters: filters,
            initialFilter: text.filters.firstIndex { $0.selected } ?? 0,
            banner: DocumentsModel.Banner(title: text.banner.title, sub: text.banner.sub, amount: text.banner.amount),
            recentLabel: text.recentLabel,
            byTradeLabel: text.byTradeLabel,
            rows: try documents.items.map { item in
                guard let kind = DocumentKind(rawValue: item.kind) else {
                    throw RepositoryError.decoding("documents.items.kind")
                }
                guard let meta = item.meta.value else { throw RepositoryError.decoding("documents.items.meta") }
                return DocumentsModel.Row(
                    id: item.id, kind: kind, title: item.title, meta: meta, amount: item.amount,
                    target: documentTarget(item.id, viewerIds: viewerIds))
            })
    }

    private nonisolated static func viewerModels(
        _ items: [DocumentItem], comparisonTradeIds: Set<String>
    ) throws -> [String: DocViewerModel] {
        var models: [String: DocViewerModel] = [:]
        for item in items {
            guard let viewer = item.viewer else { continue }
            guard let subtitle = viewer.subtitle.value else { throw RepositoryError.decoding("viewer.subtitle") }
            guard let pageFooter = viewer.pageFooter.value else { throw RepositoryError.decoding("viewer.pageFooter") }
            models[item.id] = DocViewerModel(
                id: item.id,
                title: item.title,
                subtitle: subtitle,
                pageLabel: viewer.pageLabel,
                pageFooter: pageFooter,
                issuerName: viewer.issuer.name,
                issuerAddress: viewer.issuer.address,
                number: viewer.number,
                reference: viewer.reference,
                lines: viewer.lines.map { DocViewerModel.Line(pos: $0.pos, text: $0.text, sum: $0.sum) },
                totalLabel: ScreenCopy.DokViewer.totalLabel,
                total: viewer.total,
                facts: viewer.facts.map { LabeledValue(label: $0.label, value: $0.value) },
                compareLabel: ScreenCopy.DokViewer.compareLabel,
                compareTarget: item.tradeId.flatMap { comparisonTradeIds.contains($0) ? .compare(tradeId: $0) : nil },
                shareLabel: ScreenCopy.DokViewer.shareLabel)
        }
        return models
    }

    private nonisolated static func notificationsModel(_ groups: [NotificationGroup]) throws -> NotificationsModel {
        let mapped = try groups.map { group in
            NotificationsModel.Group(
                title: group.title,
                items: try group.items.map { item in
                    guard let kind = NotificationKind(rawValue: item.kind) else {
                        throw RepositoryError.decoding("notifications.items.kind")
                    }
                    return NotificationsModel.Item(
                        id: item.id, kind: kind, title: item.title, text: item.text, when: item.when,
                        unread: item.unread,
                        target: try RouteParser.target(item.target, field: "notifications.items.target"))
                })
        }
        return NotificationsModel(
            title: ScreenCopy.notificationsTitle,
            markAllLabel: ScreenCopy.Mitteilungen.markAllLabel,
            groups: mapped,
            unreadCount: mapped.flatMap(\.items).filter(\.unread).count)
    }

    private nonisolated static func tabItems(_ tabs: [TabText], in screen: String) throws -> [AppTabItem] {
        try tabs.map { item in
            guard let tab = AppTab(rawValue: item.route) else {
                throw RepositoryError.decoding("screens.\(screen).ios.tabs.route")
            }
            return AppTabItem(tab: tab, label: item.label)
        }
    }
}
