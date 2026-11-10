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
    let inspections: InspectionsModel
    let defectsBoard: DefectsBoardModel
    /// Elements screen by trade id (only `windows` has one).
    let inspectionElements: [String: ElementsModel]
    /// Step screen by its route (only the step of the `now` element).
    let inspectionSteps: [AppRoute: StepModel]
    let defectForm: DefectFormModel
    /// Sign-off screen by trade id (only `windows` has one).
    let signOffs: [String: SignOffModel]
    /// Protocol screen by trade id (earthworks, shell, carpentry).
    let tradeProtocols: [String: TradeProtocolModel]
    /// Electrical overview by trade id (only `electrical`).
    let electricalOverviews: [String: ElectricalOverviewModel]
    /// Check screens by their route.
    let measureChecks: [AppRoute: MeasureCheckModel]
    let roomChecks: [AppRoute: RoomCheckModel]
    let panelChecks: [AppRoute: PanelCheckModel]
    let smokeChecks: [AppRoute: SmokeCheckModel]
    /// M6 overviews by trade id (plumbing, screed, tiles, painting).
    let tradeOverviews: [String: TradeOverviewModel]
    /// M6 check screens by their route.
    let slopeChecks: [AppRoute: SlopeCheckModel]
    let pressureChecks: [AppRoute: PressureCheckModel]
    let flatnessChecks: [AppRoute: FlatnessCheckModel]
    let readinessChecks: [AppRoute: ReadinessCheckModel]
    let objectChecks: [AppRoute: ObjectCheckModel]
    let tileChecks: [AppRoute: TileCheckModel]
    let paintChecks: [AppRoute: PaintCheckModel]

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

        let gewerkeText = try await repo.screenText("AbnGewerke", as: TabsScreenText.self)
        let maengelText = try await repo.screenText("AbnMaengel", as: TabsScreenText.self)
        for name in ["AbnElemente", "AbnPruefung", "AbnMangel", "AbnSignatur"] {
            _ = try await repo.screenText(name, as: EmptyScreenText.self)
        }
        for name in earlyTradeScreens {
            _ = try await repo.screenText(name, as: EmptyScreenText.self)
        }
        for name in laterTradeScreens {
            _ = try await repo.screenText(name, as: EmptyScreenText.self)
        }
        let overview = try await repo.inspectionSlice(InspectionOverview.self, at: ["overview"])
        let elementsCheck = try await repo.windowsCheck("elements", as: ElementsCheck.self)
        let stepCheck = try await repo.windowsCheck("stepF8", as: StepCheck.self)
        let formCheck = try await repo.windowsCheck("defectF2", as: DefectFormCheck.self)
        let signatureCheck = try await repo.windowsCheck("signature", as: SignatureCheck.self)
        let defects = try await repo.defects()
        let resolved = try await repo.resolvedDefects()
        let now = try await repo.now()

        let tradeNames = Dictionary(trades.map { ($0.id, $0.name) }, uniquingKeysWith: { first, _ in first })
        guard let phaseTitle = tradeNames[project.phase.tradeId] else {
            throw RepositoryError.decoding("project.phase.tradeId")
        }

        let views = [
            ScreenCopy.AbnGewerke.tradesView,
            String(format: ScreenCopy.AbnGewerke.defectsViewFormat, defects.count),
        ]
        let inspectionsModel = try inspectionsModel(overview, tabs: gewerkeText.tabs, views: views)
        let defectsBoard = try defectsBoardModel(
            defects, tabs: maengelText.tabs, views: views, resolvedCount: resolved.count)
        let (elements, stepRoute) = try elementsModel(elementsCheck)
        let step = try stepModel(stepCheck, route: stepRoute, statusLabels: overview.statusLabels)
        let form = try defectFormModel(formCheck, now: now)
        let signOff = try signOffModel(signatureCheck)
        let early = try await earlyTrades(repo, overview: overview)
        let later = try await laterTrades(repo, overview: overview, earlyRoutes: Set(early.checkTitles.keys))

        let homeTabs = try tabItems(homeText.tabs, in: "Home")
        let routeTitles = RouteTitles(
            tabLabels: Dictionary(homeTabs.map { ($0.tab, $0.label) }, uniquingKeysWith: { first, _ in first }),
            tradeNames: tradeNames,
            documentTitles: Dictionary(
                documents.items.map { ($0.id, $0.title) }, uniquingKeysWith: { first, _ in first }),
            compareTitle: RouteTitles.compareTitle,
            notificationsTitle: ScreenCopy.notificationsTitle,
            inspectionTitles: Dictionary(
                overview.rows.map { ($0.tradeId, $0.name) }, uniquingKeysWith: { first, _ in first }),
            stepTitles: [stepRoute: step.title],
            newDefectTitle: ScreenCopy.AbnMangel.title,
            signOffTitles: [signOff.inspectionId: signOff.title],
            checkTitles: early.checkTitles.merging(later.checkTitles) { first, _ in first },
            inspectionBackLabels: Dictionary(
                uniqueKeysWithValues: ScreenCopy.TradeOverview.backLabels.map {
                    (AppRoute.inspection(tradeId: $0.key), $0.value)
                }))

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
            notifications: try notificationsModel(notificationGroups),
            inspections: inspectionsModel,
            defectsBoard: defectsBoard,
            inspectionElements: [elements.tradeId: elements],
            inspectionSteps: [stepRoute: step],
            defectForm: form,
            signOffs: [signOff.inspectionId: signOff],
            tradeProtocols: early.protocols,
            electricalOverviews: early.electrical,
            measureChecks: early.measure,
            roomChecks: early.room,
            panelChecks: early.panel,
            smokeChecks: early.smoke,
            tradeOverviews: later.overviews,
            slopeChecks: later.slope,
            pressureChecks: later.pressure,
            flatnessChecks: later.flatness,
            readinessChecks: later.readiness,
            objectChecks: later.object,
            tileChecks: later.tile,
            paintChecks: later.paint)
    }

    // MARK: - M4 inspection models

    private nonisolated static let windowsId = "windows"
    private nonisolated static let checksPath = "inspections.byTrade.windows.checks"
    private nonisolated static let statusLabelsPath = "inspections.overview.statusLabels"

    /// The iOS side of a `PlatformText`; a null there is a decoding error at `path` (D16).
    private nonisolated static func iosText(_ text: PlatformText, _ path: String) throws -> String {
        guard let value = text.value else { throw RepositoryError.decoding(path) }
        return value
    }

    private nonisolated static func inspectionsTitle(_ tabs: [TabText], screen: String) throws -> String {
        guard let item = try tabItems(tabs, in: screen).first(where: { $0.tab == .inspections }) else {
            throw RepositoryError.decoding("screens.\(screen).ios.tabs")
        }
        return item.label
    }

    private nonisolated static func statusLabel(_ key: String, in labels: [String: PlatformText]) throws -> String {
        guard let text = labels[key] else { throw RepositoryError.decoding(statusLabelsPath) }
        return try iosText(text, statusLabelsPath)
    }

    private nonisolated static func inspectionsModel(
        _ overview: InspectionOverview, tabs: [TabText], views: [String]
    ) throws -> InspectionsModel {
        InspectionsModel(
            title: try inspectionsTitle(tabs, screen: "AbnGewerke"),
            templatesLabel: ScreenCopy.AbnGewerke.templatesLabel,
            views: views,
            viewsLabel: ScreenCopy.AbnGewerke.viewsLabel,
            rows: try overview.rows.map { row in
                guard let status = InspectionStatus(rawValue: row.status) else {
                    throw RepositoryError.decoding("inspections.overview.rows.status")
                }
                return InspectionTradeRow(
                    tradeId: row.tradeId, name: row.name,
                    subtitle: try iosText(row.sub, "inspections.overview.rows.sub"),
                    status: status,
                    statusLabel: try statusLabel(row.status, in: overview.statusLabels),
                    target: try RouteParser.route(row.route, field: "inspections.overview.rows.route"))
            })
    }

    private nonisolated static func defectsBoardModel(
        _ defects: [Defect], tabs: [TabText], views: [String], resolvedCount: String
    ) throws -> DefectsBoardModel {
        DefectsBoardModel(
            title: try inspectionsTitle(tabs, screen: "AbnMaengel"),
            templatesLabel: ScreenCopy.AbnMaengel.templatesLabel,
            views: views,
            viewsLabel: ScreenCopy.AbnMaengel.viewsLabel,
            cards: try defects.map { defect in
                guard let urgency = DefectUrgency(rawValue: defect.urgency) else {
                    throw RepositoryError.decoding("defects.urgency")
                }
                return DefectCard(
                    id: defect.id, trade: try iosText(defect.trade, "defects.trade"), title: defect.title,
                    dueLabel: defect.dueLabel, meta: defect.meta, urgency: urgency,
                    showsActions: ScreenCopy.AbnMaengel.showsActions(urgency))
            },
            remindLabel: ScreenCopy.AbnMaengel.remindLabel,
            resolveLabel: ScreenCopy.AbnMaengel.resolveLabel,
            resolvedLabel: ScreenCopy.AbnMaengel.resolvedLabel,
            resolvedCount: resolvedCount)
    }

    /// The elements model and the route of its single `now` element (the step screen's key).
    private nonisolated static func elementsModel(_ check: ElementsCheck) throws -> (ElementsModel, AppRoute) {
        let path = "\(checksPath).elements"
        let progress: [ElementStatus] = try check.progress.map { raw in
            guard let status = ElementStatus(rawValue: raw), status != .now else {
                throw RepositoryError.decoding("\(path).progress")
            }
            return status
        }
        let legend = try check.summary.map { entry -> ElementsModel.LegendEntry in
            guard let status = ElementStatus(rawValue: entry.status), status != .now else {
                throw RepositoryError.decoding("\(path).summary.status")
            }
            return ElementsModel.LegendEntry(status: status, label: try iosText(entry.label, "\(path).summary.label"))
        }
        let floors = try check.floors.map { floor in
            ElementsModel.FloorSection(
                title: floor.title, moreLabel: floor.more,
                items: try floor.items.map { item in
                    guard let status = ElementStatus(rawValue: item.status) else {
                        throw RepositoryError.decoding("\(path).floors.items.status")
                    }
                    return ElementsModel.ElementRow(
                        id: item.id, room: item.room, type: item.type, label: item.label, status: status,
                        target: try RouteParser.route(item.route, field: "\(path).floors.items.route"))
                })
        }
        let current = floors.flatMap(\.items).filter { $0.status == .now }
        guard current.count == 1, let next = current[0].target, case .inspectionStep = next else {
            throw RepositoryError.decoding("\(path).floors")
        }
        let model = ElementsModel(
            tradeId: windowsId,
            title: check.title,
            subtitle: try iosText(check.subtitle, "\(path).subtitle"),
            counter: try iosText(check.counter, "\(path).counter"),
            guideLinkLabel: ScreenCopy.AbnElemente.guideLinkLabel,
            progress: progress,
            legend: legend,
            floors: floors,
            nextLabel: try iosText(check.nextLabel, "\(path).nextLabel"),
            nextTarget: next)
        return (model, next)
    }

    private nonisolated static func stepModel(
        _ check: StepCheck, route: AppRoute, statusLabels: [String: PlatformText]
    ) throws -> StepModel {
        let path = "\(checksPath).stepF8"
        guard case let .inspectionStep(_, elementId, stepNumber) = route,
            let currentNumber = positiveNumber(check.current.n), currentNumber == stepNumber,
            check.title.hasPrefix("\(elementId) · ")
        else { throw RepositoryError.decoding(path) }
        guard check.current.photos.count == 2 else { throw RepositoryError.decoding("\(path).current.photos") }
        return StepModel(
            route: route,
            counter: try iosText(check.counter, "\(path).counter"),
            title: check.title,
            subtitle: try iosText(check.subtitle, "\(path).subtitle"),
            done: try check.done.map {
                StepModel.Done(label: $0.label, meta: try iosText($0.meta, "\(path).done.meta"))
            },
            current: StepModel.Current(
                n: currentNumber, title: check.current.title, text: check.current.text,
                photoCaption: check.current.photos[0], addPhotoLabel: check.current.photos[1]),
            guideLabel: ScreenCopy.AbnPruefung.guideLabel,
            defectLabel: try statusLabel("defect", in: statusLabels),
            okLabel: try statusLabel("ok", in: statusLabels),
            next: try check.next.map { entry in
                guard let number = positiveNumber(entry.n) else { throw RepositoryError.decoding(path) }
                return StepModel.Next(n: number, label: try iosText(entry.label, "\(path).next.label"))
            },
            defectTarget: .newDefect)
    }

    private nonisolated static func defectFormModel(_ check: DefectFormCheck, now: Date) throws -> DefectFormModel {
        let path = "\(checksPath).defectF2"
        guard let severity = check.severity.options.firstIndex(of: check.severity.selected) else {
            throw RepositoryError.decoding("\(path).severity")
        }
        guard let berlin = TimeZone(identifier: "Europe/Berlin") else {
            throw RepositoryError.fixtureMissing("Europe/Berlin")
        }
        let base = CalendarDay.from(now, timeZone: berlin)
        let offsets = ScreenCopy.AbnMangel.deadlineOffsets
        let options = try check.deadlineOptions.enumerated().map { index, text in
            DefectFormModel.DeadlineOption(
                label: try iosText(text, "\(path).deadlineOptions"),
                offsetDays: index < offsets.count ? offsets[index] : nil)
        }
        let deadline = try iosText(check.deadlineLabel, "\(path).deadlineLabel")
        guard let initial = options.firstIndex(where: {
            $0.offsetDays.map { DeadlineMath.label(base: base, offsetDays: $0, style: .ios) } == deadline
        }) else { throw RepositoryError.decoding("\(path).deadlineLabel") }
        return DefectFormModel(
            tradeId: windowsId,
            title: ScreenCopy.AbnMangel.title,
            saveLabel: ScreenCopy.AbnMangel.saveLabel,
            cancelLabel: ScreenCopy.AbnMangel.cancelLabel,
            context: check.context,
            finding: check.title,
            descriptionLabel: ScreenCopy.AbnMangel.descriptionLabel,
            description: check.description,
            photosLabel: check.photosLabel,
            photoLabels: check.photos,
            addPhotoLabel: ScreenCopy.AbnMangel.addPhotoLabel,
            severityLabel: ScreenCopy.AbnMangel.severityLabel,
            severities: check.severity.options,
            initialSeverity: severity,
            deadlineLabel: ScreenCopy.AbnMangel.deadlineLabel,
            deadlineOptions: options,
            deadlineBase: base,
            initialDeadline: initial,
            responsibleLabel: ScreenCopy.AbnMangel.responsibleLabel,
            responsible: check.responsible,
            notifyLabel: check.notify.label,
            notifyInitial: check.notify.enabled)
    }

    private nonisolated static func signOffModel(_ check: SignatureCheck) throws -> SignOffModel {
        let path = "\(checksPath).signature"
        let tones = ScreenCopy.AbnSignatur.statTones
        guard check.stats.count == tones.count, check.stats.allSatisfy({ positiveNumber($0.n) != nil }) else {
            throw RepositoryError.decoding("\(path).stats")
        }
        return SignOffModel(
            inspectionId: windowsId,
            title: check.title,
            subtitle: try iosText(check.subtitle, "\(path).subtitle"),
            stats: zip(check.stats, tones).map { SignOffModel.Stat(value: $0.n, label: $0.label, tone: $1) },
            defects: check.defects.map {
                SignOffModel.ReservedDefect(
                    title: $0.title, meta: $0.meta, due: ScreenCopy.AbnSignatur.duePrefix + " " + $0.due)
            },
            reservation: check.reservation,
            craftsmanLabel: ScreenCopy.AbnSignatur.craftsmanLabel,
            craftsmanName: check.craftsman.name,
            craftsmanStatus: check.craftsman.signed,
            ownerLabel: ScreenCopy.AbnSignatur.ownerLabel,
            ownerName: try iosText(check.owner, "\(path).owner"),
            ownerPending: ScreenCopy.AbnSignatur.ownerPending,
            signHereLabel: ScreenCopy.AbnSignatur.signHereLabel,
            signatureLabel: ScreenCopy.AbnSignatur.signatureLabel,
            finishLabel: ScreenCopy.AbnSignatur.finishLabel,
            note: check.note)
    }

    // MARK: - M5 early trades

    /// The screens of the M5 trades; their `ios` text is `{}` and loading it proves the screen exists.
    private nonisolated static let earlyTradeScreens = [
        "Erd", "Rohbau", "RohbauMasse", "Zimmerer", "ElUebersicht", "ElRaum", "ElVerteiler", "ElRauchmelder",
    ]
    private nonisolated static let electricalId = "electrical"

    private struct EarlyTrades: Sendable {
        var protocols: [String: TradeProtocolModel] = [:]
        var electrical: [String: ElectricalOverviewModel] = [:]
        var measure: [AppRoute: MeasureCheckModel] = [:]
        var room: [AppRoute: RoomCheckModel] = [:]
        var panel: [AppRoute: PanelCheckModel] = [:]
        var smoke: [AppRoute: SmokeCheckModel] = [:]
        var checkTitles: [AppRoute: String] = [:]
    }

    private nonisolated static func tradePath(_ tradeId: String) -> String { "inspections.byTrade.\(tradeId)" }

    private nonisolated static func earlyTrades(
        _ repo: BackendRepository, overview: InspectionOverview
    ) async throws -> EarlyTrades {
        var result = EarlyTrades()
        var protocolDtos: [String: TradeProtocolDto] = [:]
        for tradeId in ScreenCopy.ProtocolCopy.protocolTrades {
            let dto = try await repo.tradeSlice(TradeProtocolDto.self, tradeId: tradeId)
            protocolDtos[tradeId] = dto
            try await loadChecks(dto.checks, tradeId: tradeId, repo: repo, overview: overview, into: &result)
        }
        let electricalDto = try await repo.tradeSlice(ElectricalOverviewDto.self, tradeId: electricalId)
        try await loadChecks(
            electricalDto.checks, tradeId: electricalId, repo: repo, overview: overview, into: &result)

        let routes = Set(result.checkTitles.keys)
        for tradeId in ScreenCopy.ProtocolCopy.protocolTrades {
            if let dto = protocolDtos[tradeId] {
                result.protocols[tradeId] = try protocolModel(
                    dto, tradeId: tradeId, overview: overview, routes: routes)
            }
        }
        result.electrical[electricalId] = try electricalModel(
            electricalDto, tradeId: electricalId, routes: routes)
        return result
    }

    /// Builds every check of a trade by the fixture `screen` value of the check.
    private nonisolated static func loadChecks(
        _ raw: JSONValue?, tradeId: String, repo: BackendRepository, overview: InspectionOverview,
        into result: inout EarlyTrades
    ) async throws {
        guard let raw else { return }
        let path = "\(tradePath(tradeId)).checks"
        guard case let .object(checks) = raw else { throw RepositoryError.decoding(path) }
        for key in checks.keys.sorted() {
            let checkPath = "\(path).\(key)"
            guard case let .object(members) = checks[key], case let .string(screen)? = members["screen"] else {
                throw RepositoryError.decoding("\(checkPath).screen")
            }
            switch screen {
            case "RohbauMasse":
                let dto = try await repo.tradeCheck(tradeId, key, as: MeasureCheckDto.self)
                let opening = try await repo.measurementSlice(ShellOpeningDto.self, key: dto.measurementKey)
                let route = AppRoute.inspectionCheck(tradeId: tradeId, check: key)
                let model = try measureModel(dto, opening: opening, route: route, overview: overview, path: checkPath)
                result.measure[route] = model
                result.checkTitles[route] = model.title
            case "ElRaum":
                let dto = try await repo.tradeCheck(tradeId, key, as: RoomCheckDto.self)
                let route = AppRoute.inspectionRoom(tradeId: tradeId, roomId: key)
                let model = try roomModel(dto, route: route, overview: overview, path: checkPath)
                result.room[route] = model
                result.checkTitles[route] = model.title
            case "ElVerteiler":
                let dto = try await repo.tradeCheck(tradeId, key, as: PanelCheckDto.self)
                let route = AppRoute.inspectionCheck(tradeId: tradeId, check: key)
                let model = try panelModel(dto, route: route, path: checkPath)
                result.panel[route] = model
                result.checkTitles[route] = model.title
            case "ElRauchmelder":
                let dto = try await repo.tradeCheck(tradeId, key, as: SmokeCheckDto.self)
                let route = AppRoute.inspectionCheck(tradeId: tradeId, check: key)
                let model = try smokeModel(dto, route: route, path: checkPath)
                result.smoke[route] = model
                result.checkTitles[route] = model.title
            default:
                throw RepositoryError.decoding("\(checkPath).screen")
            }
        }
    }

    /// A check, room or check-item route read in M5 or M6 must resolve to a loaded check (D9, M6 D2).
    private nonisolated static func requireCheck(_ route: AppRoute, in routes: Set<AppRoute>, path: String) throws {
        switch route {
        case .inspectionCheck, .inspectionRoom:
            guard routes.contains(route) else { throw RepositoryError.decoding(path) }
        case .inspectionCheckItem:
            guard routes.contains(route) else { throw RepositoryError.decoding(path) }
        default:
            break
        }
    }

    private nonisolated static func protocolModel(
        _ dto: TradeProtocolDto, tradeId: String, overview: InspectionOverview, routes: Set<AppRoute>
    ) throws -> TradeProtocolModel {
        let path = tradePath(tradeId)
        guard let status = overview.rows.first(where: { $0.tradeId == tradeId })?.status,
            let tone = ScreenCopy.ProtocolCopy.bannerTone(overviewStatus: status)
        else { throw RepositoryError.decoding("\(path).banner") }
        let defectLabel = try statusLabel("defect", in: overview.statusLabels)
        let rowPath = "\(path).groups.rows"
        return TradeProtocolModel(
            tradeId: tradeId,
            title: dto.title,
            subtitle: try iosText(dto.subtitle, "\(path).subtitle"),
            banner: TradeProtocolModel.Banner(
                tone: tone, title: dto.banner.title, text: dto.banner.text,
                documentLabel: dto.banner.documentLabel, target: tone == .warn ? .defects : nil),
            groups: try dto.groups.map { group in
                TradeProtocolModel.Group(
                    title: group.title,
                    rows: try group.rows.map { row in
                        var target: NavigationTarget?
                        if let raw = row.route {
                            let link = try RouteParser.inspectionLink(raw, field: "\(rowPath).route")
                            if case let .push(route) = link {
                                try requireCheck(route, in: routes, path: "\(rowPath).route")
                            }
                            target = link
                        }
                        return TradeProtocolModel.Row(
                            label: try iosText(row.label, "\(rowPath).label"), meta: row.meta,
                            mark: ScreenCopy.ProtocolCopy.mark(meta: row.meta, defectLabel: defectLabel),
                            target: target)
                    })
            })
    }

    private nonisolated static func electricalModel(
        _ dto: ElectricalOverviewDto, tradeId: String, routes: Set<AppRoute>
    ) throws -> ElectricalOverviewModel {
        let path = tradePath(tradeId)
        let phases = try dto.phases.map { phase -> PhaseModel in
            switch (phase.statusLabel, phase.progressLabel) {
            case let (status?, nil):
                return PhaseModel(
                    title: phase.title,
                    kind: .done(
                        status: try iosText(status, "\(path).phases.statusLabel"),
                        date: try phase.dateLabel.map { try iosText($0, "\(path).phases.dateLabel") }))
            case let (nil, progress?):
                return PhaseModel(
                    title: phase.title,
                    kind: .active(progressLabel: progress, fraction: ScreenCopy.ElUebersicht.activePhaseFraction))
            default:
                throw RepositoryError.decoding("\(path).phases")
            }
        }
        let rowPath = "\(path).groups.rows"
        let groups = try dto.groups.enumerated().map { groupIndex, group in
            ElectricalOverviewModel.Group(
                title: try iosText(group.title, "\(path).groups.title"),
                moreLabel: try group.more.map { try iosText($0, "\(path).groups.more") },
                rows: try group.rows.enumerated().map { rowIndex, row in
                    guard let icon = ScreenCopy.ElUebersicht.icon(group: groupIndex, row: rowIndex) else {
                        throw RepositoryError.decoding(rowPath)
                    }
                    guard let tone = ScreenCopy.ElUebersicht.tone(statusLabel: row.statusLabel) else {
                        throw RepositoryError.decoding("\(rowPath).statusLabel")
                    }
                    let target = try RouteParser.route(row.route, field: "\(rowPath).route")
                    if let target { try requireCheck(target, in: routes, path: "\(rowPath).route") }
                    return ElectricalOverviewModel.Row(
                        name: row.name, sub: row.sub, icon: icon, statusLabel: row.statusLabel, tone: tone,
                        target: target)
                })
        }
        return ElectricalOverviewModel(
            tradeId: tradeId, title: dto.title, subtitle: try iosText(dto.subtitle, "\(path).subtitle"),
            phases: phases, groups: groups)
    }

    private nonisolated static func measureModel(
        _ dto: MeasureCheckDto, opening: ShellOpeningDto, route: AppRoute, overview: InspectionOverview, path: String
    ) throws -> MeasureCheckModel {
        MeasureCheckModel(
            route: route,
            counter: try iosText(dto.counter, "\(path).counter"),
            title: dto.title,
            instruction: ScreenCopy.RohbauMasse.instruction,
            measuredHeading: ScreenCopy.RohbauMasse.measuredHeading,
            plumbTitle: ScreenCopy.RohbauMasse.plumbTitle,
            plumbSubtitle: ScreenCopy.RohbauMasse.plumbSubtitle,
            plumbValue: ScreenCopy.RohbauMasse.plumbValue,
            saveLabel: ScreenCopy.RohbauMasse.saveLabel,
            photoLabel: ScreenCopy.RohbauMasse.photoLabel,
            defectLabel: try statusLabel("defect", in: overview.statusLabels),
            withinLabel: opening.labels.within,
            outsideLabel: opening.labels.outside,
            dimensions: [
                MeasureCheckModel.Dimension(key: .width, label: opening.dimLabels.w),
                MeasureCheckModel.Dimension(key: .height, label: opening.dimLabels.h),
            ],
            measured: opening.measured,
            initial: MeasureViewModel(
                target: .init(width: opening.target.w, height: opening.target.h),
                actual: .init(width: opening.actual.w, height: opening.actual.h),
                step: opening.stepperDelta,
                toleranceDefault: opening.parameter.default, toleranceMin: opening.parameter.min,
                toleranceMax: opening.parameter.max, unit: opening.parameter.unit))
    }

    private nonisolated static func roomModel(
        _ dto: RoomCheckDto, route: AppRoute, overview: InspectionOverview, path: String
    ) throws -> RoomCheckModel {
        guard let current = positiveNumber(dto.current.n) else { throw RepositoryError.decoding("\(path).current") }
        return RoomCheckModel(
            route: route,
            counter: try iosText(dto.counter, "\(path).counter"),
            title: dto.title,
            subtitle: try iosText(dto.subtitle, "\(path).subtitle"),
            done: try dto.done.map {
                RoomCheckModel.Done(label: $0.label, meta: try iosText($0.meta, "\(path).done.meta"))
            },
            current: RoomCheckModel.Current(n: current, title: dto.current.title, text: dto.current.text),
            sockets: try dto.sockets.map { socket in
                guard let status = SocketStatus(rawValue: socket.status) else {
                    throw RepositoryError.decoding("\(path).sockets")
                }
                return RoomCheckModel.Socket(
                    id: socket.id, label: socket.label, status: status, accessibilityLabel: socket.aria)
            },
            progressLabel: try iosText(dto.progress, "\(path).progress"),
            defectNote: dto.defectNote,
            okNote: dto.okNote,
            defectLabel: try statusLabel("defect", in: overview.statusLabels),
            next: try dto.next.map { entry in
                guard let number = positiveNumber(entry.n) else { throw RepositoryError.decoding("\(path).next") }
                return RoomCheckModel.Next(n: number, label: try iosText(entry.label, "\(path).next.label"))
            })
    }

    private nonisolated static func panelModel(_ dto: PanelCheckDto, route: AppRoute, path: String) throws -> PanelCheckModel {
        guard let current = positiveNumber(dto.current.n) else { throw RepositoryError.decoding("\(path).current") }
        guard dto.current.outcomes.count == 2 else { throw RepositoryError.decoding("\(path).current.outcomes") }
        return PanelCheckModel(
            route: route,
            counter: try iosText(dto.counter, "\(path).counter"),
            title: dto.title,
            callout: dto.subtitle,
            done: dto.done.map { PanelCheckModel.Done(label: $0.label, meta: $0.meta) },
            current: PanelCheckModel.Current(
                n: current, title: dto.current.title, steps: [dto.current.text] + dto.current.steps),
            fis: try dto.fis.map { fi in
                guard let tone = ScreenCopy.ElVerteiler.tone(statusLabel: fi.statusLabel) else {
                    throw RepositoryError.decoding("\(path).fis")
                }
                return PanelCheckModel.Fi(name: fi.name, sub: fi.sub, statusLabel: fi.statusLabel, tone: tone)
            },
            outcomes: dto.current.outcomes,
            next: try dto.next.map { entry in
                guard let number = positiveNumber(entry.n) else { throw RepositoryError.decoding("\(path).next") }
                return PanelCheckModel.Next(n: number, label: entry.label, tag: entry.tag)
            })
    }

    private nonisolated static func smokeModel(_ dto: SmokeCheckDto, route: AppRoute, path: String) throws -> SmokeCheckModel {
        let rooms = try dto.rooms.map { room in
            guard let status = ScreenCopy.ElRauchmelder.status(roomLabel: room.statusLabel) else {
                throw RepositoryError.decoding("\(path).rooms")
            }
            return SmokeCheckModel.Room(name: room.name, statusLabel: room.statusLabel, status: status)
        }
        let now = rooms.filter { $0.status == .now }
        guard now.count == 1, now[0].name == dto.current.room else {
            throw RepositoryError.decoding("\(path).current.room")
        }
        guard dto.current.outcomes.count == 2 else { throw RepositoryError.decoding("\(path).current.outcomes") }
        return SmokeCheckModel(
            route: route,
            counter: try iosText(dto.counter, "\(path).counter"),
            title: dto.title,
            subtitle: dto.subtitle,
            room: dto.current.room,
            nowLabel: dto.current.title,
            checks: dto.checks.enumerated().map { index, check in
                SmokeCheckModel.Check(label: check.label, hint: check.hint, done: index < ScreenCopy.ElRauchmelder.checksDone)
            },
            outcomes: dto.current.outcomes,
            rooms: rooms,
            reminder: dto.reminder,
            battery: dto.battery)
    }

    // MARK: - M6 later trades

    /// The screens of the M6 trades; their `ios` text is `{}` and loading it proves the screen exists.
    private nonisolated static let laterTradeScreens = [
        "SanUebersicht", "SanGefaelle", "SanDruck", "SanBad", "EsUebersicht", "EsEbenheit", "EsBelegreife",
        "FlUebersicht", "FlBad", "MaUebersicht", "MaRaum",
    ]
    private nonisolated static let drainSlopeKey = "drainSlope"
    private nonisolated static let screedFlatnessKey = "screedFlatness"
    private nonisolated static let screedHeatingKey = "screedHeating"
    private nonisolated static let tickSuffix = " ✓"

    private struct LaterTrades: Sendable {
        var overviews: [String: TradeOverviewModel] = [:]
        var slope: [AppRoute: SlopeCheckModel] = [:]
        var pressure: [AppRoute: PressureCheckModel] = [:]
        var flatness: [AppRoute: FlatnessCheckModel] = [:]
        var readiness: [AppRoute: ReadinessCheckModel] = [:]
        var object: [AppRoute: ObjectCheckModel] = [:]
        var tile: [AppRoute: TileCheckModel] = [:]
        var paint: [AppRoute: PaintCheckModel] = [:]
        var checkTitles: [AppRoute: String] = [:]
    }

    /// Loads the four M6 overviews and their checks. A row route must resolve to a check of M5 or M6 (D2, I3).
    private nonisolated static func laterTrades(
        _ repo: BackendRepository, overview: InspectionOverview, earlyRoutes: Set<AppRoute>
    ) async throws -> LaterTrades {
        var result = LaterTrades()
        var dtos: [String: TradeOverviewDto] = [:]
        for tradeId in ScreenCopy.TradeOverview.trades {
            let dto = try await repo.tradeSlice(TradeOverviewDto.self, tradeId: tradeId)
            dtos[tradeId] = dto
            try await loadLaterChecks(dto.checks, tradeId: tradeId, repo: repo, overview: overview, into: &result)
        }
        let routes = earlyRoutes.union(result.checkTitles.keys)
        for tradeId in ScreenCopy.TradeOverview.trades {
            if let dto = dtos[tradeId] {
                result.overviews[tradeId] = try tradeOverviewModel(
                    dto, tradeId: tradeId, overview: overview, routes: routes)
            }
        }
        return result
    }

    /// Builds every check of an M6 trade by the fixture `screen` value of the check (D2).
    private nonisolated static func loadLaterChecks(
        _ raw: JSONValue?, tradeId: String, repo: BackendRepository, overview: InspectionOverview,
        into result: inout LaterTrades
    ) async throws {
        guard let raw else { return }
        let path = "\(tradePath(tradeId)).checks"
        guard case let .object(checks) = raw else { throw RepositoryError.decoding(path) }
        for key in checks.keys.sorted() {
            let checkPath = "\(path).\(key)"
            guard case let .object(members) = checks[key], case let .string(screen)? = members["screen"] else {
                throw RepositoryError.decoding("\(checkPath).screen")
            }
            switch screen {
            case "SanGefaelle":
                let dto = try await repo.tradeCheck(tradeId, key, as: SlopeCheckDto.self)
                guard dto.measurementKey == drainSlopeKey else {
                    throw RepositoryError.decoding("\(checkPath).measurementKey")
                }
                let drain = try await repo.measurementSlice(DrainSlopeDto.self, key: dto.measurementKey)
                let model = try slopeModel(
                    dto, drain: drain, tradeId: tradeId, key: key, overview: overview, path: checkPath)
                result.slope[model.route] = model
                result.checkTitles[model.route] = model.title
            case "SanDruck":
                let dto = try await repo.tradeCheck(tradeId, key, as: PressureCheckDto.self)
                let model = try pressureModel(
                    dto, route: .inspectionCheck(tradeId: tradeId, check: key), path: checkPath)
                result.pressure[model.route] = model
                result.checkTitles[model.route] = model.title
            case "SanBad":
                let dto = try await repo.tradeCheck(tradeId, key, as: ObjectCheckDto.self)
                let model = try objectModel(
                    dto, route: .inspectionRoom(tradeId: tradeId, roomId: key), path: checkPath)
                result.object[model.route] = model
                result.checkTitles[model.route] = model.title
            case "EsEbenheit":
                let dto = try await repo.tradeCheck(tradeId, key, as: FlatnessCheckDto.self)
                guard dto.measurementKey == screedFlatnessKey else {
                    throw RepositoryError.decoding("\(checkPath).measurementKey")
                }
                let flatness = try await repo.measurementSlice(ScreedFlatnessDto.self, key: dto.measurementKey)
                let model = try flatnessModel(
                    dto, flatness: flatness, route: .inspectionCheck(tradeId: tradeId, check: key), path: checkPath)
                result.flatness[model.route] = model
                result.checkTitles[model.route] = model.title
            case "EsBelegreife":
                let dto = try await repo.tradeCheck(tradeId, key, as: ReadinessCheckDto.self)
                guard dto.measurementKey == screedHeatingKey else {
                    throw RepositoryError.decoding("\(checkPath).measurementKey")
                }
                let heating = try await repo.measurementSlice(ScreedHeatingDto.self, key: dto.measurementKey)
                let model = try readinessModel(
                    dto, heating: heating, route: .inspectionCheck(tradeId: tradeId, check: key))
                result.readiness[model.route] = model
                result.checkTitles[model.route] = model.screenTitle
            case "FlBad":
                let dto = try await repo.tradeCheck(tradeId, key, as: TileCheckDto.self)
                let model = try tileModel(dto, route: .inspectionRoom(tradeId: tradeId, roomId: key), path: checkPath)
                result.tile[model.route] = model
                result.checkTitles[model.route] = model.title
            case "MaRaum":
                let dto = try await repo.tradeCheck(tradeId, key, as: PaintCheckDto.self)
                let model = try paintModel(
                    dto, route: .inspectionRoom(tradeId: tradeId, roomId: key), overview: overview, path: checkPath)
                result.paint[model.route] = model
                result.checkTitles[model.route] = model.title
            default:
                throw RepositoryError.decoding("\(checkPath).screen")
            }
        }
    }

    /// The integers of a text, in order ("Abschnitt 3 von 4" gives [3, 4]).
    private nonisolated static func digitRuns(_ text: String) -> [Int] {
        text.split { !("0"..."9").contains($0) }.compactMap { Int($0) }
    }

    /// The step numbers of a step screen: `current.n` follows the done rows and `next` continues it (M5 D7 rule).
    private nonisolated static func stepNumbers(
        current: String, next: [String], doneCount: Int, path: String
    ) throws -> (current: Int, next: [Int]) {
        guard let currentNumber = positiveNumber(current) else { throw RepositoryError.decoding("\(path).current") }
        let nextNumbers = try next.map { text -> Int in
            guard let number = positiveNumber(text) else { throw RepositoryError.decoding("\(path).next") }
            return number
        }
        guard currentNumber == doneCount + 1,
            nextNumbers == (0..<nextNumbers.count).map({ currentNumber + 1 + $0 })
        else { throw RepositoryError.decoding("\(path).next") }
        return (currentNumber, nextNumbers)
    }

    /// A slope section label as tenths of a percent: `^\d+,\d %$`.
    private nonisolated static func percentTenths(_ label: String) -> Int? {
        guard label.hasSuffix(" %") else { return nil }
        let parts = label.dropLast(2).split(separator: ",", omittingEmptySubsequences: false)
        guard parts.count == 2, !parts[0].isEmpty, parts[1].count == 1,
            parts.allSatisfy({ $0.allSatisfy { ("0"..."9").contains($0) } }),
            let whole = Int(parts[0]), let tenth = Int(parts[1])
        else { return nil }
        return whole * 10 + tenth
    }

    private nonisolated static func slopeModel(
        _ dto: SlopeCheckDto, drain: DrainSlopeDto, tradeId: String, key: String, overview: InspectionOverview,
        path: String
    ) throws -> SlopeCheckModel {
        let counter = try iosText(dto.counter, "\(path).counter")
        let initial = SlopeViewModel(
            sollDefault: drain.parameter.default, sollMin: drain.parameter.min, sollMax: drain.parameter.max,
            lenDm: drain.lengthDm, dropMm: drain.dropMm,
            lengthRange: .init(min: drain.lengthRange.min, max: drain.lengthRange.max, step: drain.lengthRange.step),
            dropRange: .init(min: drain.dropRange.min, max: drain.dropRange.max, step: drain.dropRange.step),
            labels: .init(
                sufficient: drain.labels.sufficient, tooFlat: drain.labels.tooFlat,
                sollPrefix: drain.labels.sollPrefix))
        let sections = try drain.sections.map { section -> SlopeCheckModel.Section in
            let status: SectionStatus
            switch section.label {
            case ScreenCopy.SanGefaelle.nowLabel: status = .now
            case ScreenCopy.SanGefaelle.openLabel: status = .open
            default:
                guard let tenths = percentTenths(section.label) else {
                    throw RepositoryError.decoding("measurements.\(drainSlopeKey).sections")
                }
                status = tenths >= initial.sollTenths ? .ok : .defect
            }
            return SlopeCheckModel.Section(id: section.id, label: section.label, status: status)
        }
        let now = sections.enumerated().filter { $0.element.status == .now }
        guard now.count == 1, let current = now.first,
            dto.subtitle.components(separatedBy: " · ").first == current.element.id,
            digitRuns(counter) == [current.offset + 1, sections.count]
        else { throw RepositoryError.decoding(path) }
        return SlopeCheckModel(
            route: .inspectionCheckItem(tradeId: tradeId, check: key, item: current.element.id),
            counter: counter,
            title: dto.title,
            subtitle: dto.subtitle,
            hint: dto.hint,
            warning: dto.warning,
            sections: sections,
            initial: initial,
            defectLabel: try statusLabel("defect", in: overview.statusLabels))
    }

    private nonisolated static func pressureModel(
        _ dto: PressureCheckDto, route: AppRoute, path: String
    ) throws -> PressureCheckModel {
        let steps = try stepNumbers(
            current: dto.current.n, next: dto.next.map(\.n), doneCount: dto.done.count, path: path)
        return PressureCheckModel(
            route: route,
            counter: try iosText(dto.counter, "\(path).counter"),
            title: dto.title,
            subtitle: try iosText(dto.subtitle, "\(path).subtitle"),
            done: dto.done.map { PressureCheckModel.Done(label: $0.label, meta: $0.meta) },
            current: PressureCheckModel.Current(n: steps.current, title: dto.current.title),
            timer: PressureCheckModel.Timer(
                fraction: ScreenCopy.SanDruck.ringFraction,
                remaining: dto.timer.remaining,
                accessibilityLabel: dto.timer.progressLabel,
                remainingLabel: dto.timer.remainingLabel,
                note: dto.timer.note,
                startLabel: dto.timer.start.label,
                startValue: dto.timer.start.value,
                endLabel: dto.timer.end.label,
                endPlaceholder: dto.timer.end.value),
            next: zip(dto.next, steps.next).map { PressureCheckModel.Next(n: $1, label: $0.label, tag: $0.tag) })
    }

    private nonisolated static func objectModel(
        _ dto: ObjectCheckDto, route: AppRoute, path: String
    ) throws -> ObjectCheckModel {
        let objects = try dto.objects.map { object -> ObjectCheckModel.Object in
            guard !(object.done && object.selected) else { throw RepositoryError.decoding("\(path).objects") }
            return ObjectCheckModel.Object(
                name: object.name, state: object.selected ? .now : object.done ? .done : .open)
        }
        let selected = objects.filter { $0.state == .now }
        guard selected.count == 1 else { throw RepositoryError.decoding("\(path).objects") }
        return ObjectCheckModel(
            route: route,
            trailing: try iosText(dto.subtitle, "\(path).subtitle"),
            counter: try iosText(dto.counter, "\(path).counter"),
            title: dto.title,
            objects: objects,
            heading: selected[0].name,
            checks: try dto.checks.map {
                ObjectCheckModel.Check(label: $0.label, hint: try iosText($0.hint, "\(path).checks.hint"), ok: $0.ok)
            },
            nextHint: dto.nextHint)
    }

    private nonisolated static func flatnessModel(
        _ dto: FlatnessCheckDto, flatness: ScreedFlatnessDto, route: AppRoute, path: String
    ) throws -> FlatnessCheckModel {
        FlatnessCheckModel(
            route: route,
            counter: try iosText(dto.counter, "\(path).counter"),
            title: dto.title,
            initial: FlatnessViewModel(
                limitDefault: flatness.parameter.default, limitMin: flatness.parameter.min,
                limitMax: flatness.parameter.max, gap: flatness.gap, points: flatness.points,
                gapMin: flatness.gapMin, gapMax: flatness.gapMax, gapStep: flatness.gapStep,
                labels: .init(
                    within: flatness.labels.within, outside: flatness.labels.outside, none: flatness.labels.none,
                    point: flatness.labels.point, pluralSuffix: flatness.labels.pluralSuffix,
                    overLimit: flatness.labels.overLimit)))
    }

    private nonisolated static func readinessModel(
        _ dto: ReadinessCheckDto, heating: ScreedHeatingDto, route: AppRoute
    ) throws -> ReadinessCheckModel {
        let count = heating.planCelsius.count
        guard heating.todayDay >= 1, heating.todayDay <= count,
            heating.recordedDays >= 0, heating.recordedDays < heating.todayDay
        else { throw RepositoryError.decoding("measurements.\(screedHeatingKey)") }
        let bars = heating.planCelsius.enumerated().map { index, value -> ReadinessCheckModel.Bar in
            let height = Int(
                (Double(value) / Double(ScreenCopy.EsBelegreife.barScaleValue)
                    * Double(ScreenCopy.EsBelegreife.barScaleHeight)).rounded())
            let kind: HeatingBarKind =
                index < heating.recordedDays ? .recorded : index == heating.todayDay - 1 ? .today : .planned
            return ReadinessCheckModel.Bar(height: height, kind: kind)
        }
        return ReadinessCheckModel(
            route: route,
            cardTitle: dto.title,
            counter: dto.counter,
            text: dto.subtitle,
            chart: ReadinessCheckModel.Chart(
                bars: bars, accessibilityLabel: dto.chart.label, start: dto.chart.start, max: dto.chart.max,
                end: dto.chart.end, today: dto.chart.today, plan: dto.chart.plan, action: dto.chart.action),
            cm: ReadinessCheckModel.Cm(
                title: dto.cmTitle, tag: dto.cmTag, text: dto.cmText,
                rows: dto.cm.map { ReadinessCheckModel.CmRow(label: $0.label, meta: $0.meta, done: $0.done) },
                note: dto.cmNote))
    }

    private nonisolated static func tileModel(
        _ dto: TileCheckDto, route: AppRoute, path: String
    ) throws -> TileCheckModel {
        guard dto.tiles.count == ScreenCopy.FlBad.columns * ScreenCopy.FlBad.rows else {
            throw RepositoryError.decoding("\(path).tiles")
        }
        let tiles = try dto.tiles.map { tile -> TileCheckModel.Tile in
            switch tile.status {
            case "ok":
                return TileCheckModel.Tile(
                    id: tile.id, hollow: false, label: nil, accessibilityLabel: tile.aria)
            case "defect":
                return TileCheckModel.Tile(
                    id: tile.id, hollow: true, label: tile.label, accessibilityLabel: tile.aria)
            default:
                throw RepositoryError.decoding("\(path).tiles")
            }
        }
        let steps = try stepNumbers(
            current: dto.current.n, next: dto.next.map(\.n), doneCount: dto.done.count, path: path)
        return TileCheckModel(
            route: route,
            counter: try iosText(dto.counter, "\(path).counter"),
            subtitle: try iosText(dto.subtitle, "\(path).subtitle"),
            title: dto.title,
            done: dto.done.map(\.label),
            current: TileCheckModel.Current(n: steps.current, title: dto.current.title, text: dto.current.text),
            next: zip(dto.next, steps.next).map { TileCheckModel.Next(n: $1, label: $0.label) },
            tiles: tiles,
            wallLabel: dto.wallLabel,
            markHint: dto.markHint,
            gridAccessibilityLabel: dto.gridAria,
            result: dto.result)
    }

    private nonisolated static func paintModel(
        _ dto: PaintCheckDto, route: AppRoute, overview: InspectionOverview, path: String
    ) throws -> PaintCheckModel {
        let selected = dto.walls.filter(\.selected)
        guard selected.count == 1, !selected[0].name.hasSuffix(tickSuffix) else {
            throw RepositoryError.decoding("\(path).walls")
        }
        let steps = try stepNumbers(
            current: dto.current.n, next: dto.next.map(\.n), doneCount: dto.done.count, path: path)
        return PaintCheckModel(
            route: route,
            counter: try iosText(dto.counter, "\(path).counter"),
            subtitle: try iosText(dto.subtitle, "\(path).subtitle"),
            title: dto.title,
            done: dto.done.map(\.label),
            current: PaintCheckModel.Current(n: steps.current, title: dto.current.title, text: dto.current.text),
            walls: dto.walls.map { wall in
                PaintCheckModel.Wall(
                    name: wall.name,
                    state: wall.selected ? .now : wall.name.hasSuffix(tickSuffix) ? .done : .open)
            },
            next: zip(dto.next, steps.next).map { PaintCheckModel.Next(n: $1, label: $0.label) },
            defectLabel: try statusLabel("defect", in: overview.statusLabels),
            okLabel: try statusLabel("ok", in: overview.statusLabels))
    }

    private nonisolated static func tradeOverviewModel(
        _ dto: TradeOverviewDto, tradeId: String, overview: InspectionOverview, routes: Set<AppRoute>
    ) throws -> TradeOverviewModel {
        let path = tradePath(tradeId)
        let header: OverviewHeader
        switch (dto.phases, dto.facts) {
        case let (phases?, nil):
            header = .phases(try phases.map { try laterPhase($0, tradeId: tradeId, overview: overview) })
        case let (nil, facts?):
            header = .facts(facts.map { FactModel(label: $0.label, value: $0.value) })
        default:
            throw RepositoryError.decoding(path)
        }
        if tradeId == "plumbing",
            dto.groups.map({ $0.rows.count }) != ScreenCopy.TradeOverview.plumbingIcons.map(\.count)
        {
            throw RepositoryError.decoding("\(path).groups")
        }
        let rowPath = "\(path).groups.rows"
        let groups = try dto.groups.enumerated().map { groupIndex, group in
            TradeOverviewModel.Group(
                title: group.title,
                rows: try group.rows.enumerated().map { rowIndex, row in
                    let sub = try iosText(row.sub, "\(rowPath).sub")
                    guard let tone = ScreenCopy.TradeOverview.tone(statusLabel: row.statusLabel) else {
                        throw RepositoryError.decoding("\(rowPath).statusLabel")
                    }
                    let leading: OverviewLeading
                    switch tradeId {
                    case "plumbing":
                        leading = .icon(ScreenCopy.TradeOverview.plumbingIcons[groupIndex][rowIndex])
                    case "painting":
                        let word = sub.split(separator: " ").first.map {
                            $0.trimmingCharacters(in: CharacterSet(charactersIn: ",·"))
                        }
                        guard let swatch = word.flatMap(ScreenCopy.MaUebersicht.swatch(word:)) else {
                            throw RepositoryError.decoding("\(rowPath).sub")
                        }
                        leading = .swatch(swatch)
                    default:
                        leading = .none
                    }
                    let target = try RouteParser.route(row.route, field: "\(rowPath).route")
                    if let target { try requireCheck(target, in: routes, path: "\(rowPath).route") }
                    return TradeOverviewModel.Row(
                        name: row.name, sub: sub, statusLabel: row.statusLabel, tone: tone, leading: leading,
                        target: target)
                })
        }
        return TradeOverviewModel(
            tradeId: tradeId, title: dto.title, subtitle: try iosText(dto.subtitle, "\(path).subtitle"),
            header: header, notice: dto.banner?.text, groups: groups)
    }

    /// A phase of an M6 overview: `progressLabel` is the active one, a status label starting with the "accepted"
    /// label is done, any other status label is planned (D7).
    private nonisolated static func laterPhase(
        _ phase: TradeOverviewDto.Phase, tradeId: String, overview: InspectionOverview
    ) throws -> PhaseModel {
        let path = tradePath(tradeId)
        switch (phase.statusLabel, phase.progressLabel) {
        case let (status?, nil):
            let text = try iosText(status, "\(path).phases.statusLabel")
            guard text.hasPrefix(try statusLabel("accepted", in: overview.statusLabels)) else {
                return PhaseModel(title: phase.title, kind: .planned(status: text))
            }
            return PhaseModel(
                title: phase.title,
                kind: .done(
                    status: text,
                    date: try phase.dateLabel.map { try iosText($0, "\(path).phases.dateLabel") }))
        case let (nil, progress?):
            guard let fraction = ScreenCopy.TradeOverview.activePhaseFraction[tradeId] else {
                throw RepositoryError.decoding("\(path).phases")
            }
            return PhaseModel(title: phase.title, kind: .active(progressLabel: progress, fraction: fraction))
        default:
            throw RepositoryError.decoding("\(path).phases")
        }
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
