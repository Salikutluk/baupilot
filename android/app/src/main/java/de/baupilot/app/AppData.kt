package de.baupilot.app

import de.baupilot.data.BackendRepository
import de.baupilot.data.DefectFormCheck
import de.baupilot.data.DokumenteScreenText
import de.baupilot.data.ElementsCheck
import de.baupilot.data.EmptyScreenText
import de.baupilot.data.Fact
import de.baupilot.data.GewerkScreenText
import de.baupilot.data.HomeScreenText
import de.baupilot.data.LoginScreenText
import de.baupilot.data.PlatformText
import de.baupilot.data.ProfilScreenText
import de.baupilot.data.RepositoryException
import de.baupilot.data.SignatureCheck
import de.baupilot.data.StepCheck
import de.baupilot.data.TabText
import de.baupilot.data.TabsScreenText
import de.baupilot.data.ZeitplanScreenText
import de.baupilot.data.inspectionCheck
import de.baupilot.data.inspectionOverview
import de.baupilot.data.screenText
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.inspection.DeadlineMath
import de.baupilot.inspection.DeadlineStyle
import de.baupilot.schedule.ScheduleLayout
import de.baupilot.screens.ScreenCopy
import java.util.Locale

data class LoginModel(
    val chips: List<String>,
    val title: String,
    val intro: String,
    val emailLabel: String,
    val emailPlaceholder: String,
    val passwordLabel: String,
    val forgot: String,
    val submit: String,
    val social: String,
    val create: String,
)

data class HomeTradeRow(
    val initial: String,
    val name: String,
    val `when`: String,
    val statusLabel: String,
    val tradeId: String,
    val colors: StatusChipColors,
)

data class HomeDocTile(val label: String, val count: String, val icon: BaupilotIcon)

data class HomeModel(
    val projectLabel: String,
    val notificationsAria: String,
    val badge: String,
    val phaseLabel: String,
    val phaseTitle: String,
    val progress: Float,
    val moveInLabel: String,
    val trades: List<HomeTradeRow>,
    val docs: List<HomeDocTile>,
    val tabs: List<TabText>,
)

data class ScheduleAskRow(val name: String, val sub: String, val target: AppRoute?)

data class ScheduleModel(
    val moveInLabel: String,
    val ariaLabel: String,
    val asks: List<ScheduleAskRow>,
    val layout: ScheduleLayout,
    val tabs: List<TabText>,
)

data class ProfileRow(val label: String, val sub: String?, val isToggle: Boolean, val checked: Boolean?)

data class ProfileGroup(val title: String, val rows: List<ProfileRow>)

data class ProfileModel(
    val initials: String,
    val name: String,
    val email: String,
    val groups: List<ProfileGroup>,
    val tabs: List<TabText>,
)

enum class DocumentKind(val key: String) {
    Offer("offer"),
    Contract("contract"),
    Invoice("invoice"),
    Plan("plan"),
    ;

    companion object {
        fun of(key: String, field: String): DocumentKind =
            entries.firstOrNull { it.key == key } ?: throw RepositoryException.Decoding(field)
    }
}

enum class NotificationKind(val key: String) {
    Frist("frist"),
    Geld("geld"),
    Angebot("angebot"),
    Termin("termin"),
    ;

    companion object {
        fun of(key: String, field: String): NotificationKind =
            entries.firstOrNull { it.key == key } ?: throw RepositoryException.Decoding(field)
    }
}

data class GewerkFact(val label: String, val value: String, val icon: BaupilotIcon)

data class OfferRow(
    val id: String,
    val firm: String,
    val meta: String,
    val price: String,
    val best: Boolean,
    val pending: Boolean,
    val target: AppRoute?,
)

data class GewerkTab(val label: String, val selected: Boolean)

data class GewerkModel(
    val tradeId: String,
    val title: String,
    val statusLabel: String,
    val phaseLabel: String,
    val facts: List<GewerkFact>,
    val offers: List<OfferRow>,
    val bestLabel: String,
    val tabs: List<GewerkTab>,
    val compareLabel: String,
    val compareTarget: AppRoute?,
    val addLabel: String,
    val moreLabel: String,
)

data class CompareColumn(val offerId: String, val name: String, val best: Boolean, val target: AppRoute?)

/** `highlighted` is `a != b && !strong` (D12); views do no maths. */
data class CompareRow(val label: String, val a: String, val b: String, val strong: Boolean, val highlighted: Boolean)

data class PendingOffer(val firm: String, val text: String)

data class CompareModel(
    val tradeId: String,
    val title: String,
    val subtitle: String,
    val tableLabel: String,
    val columns: List<CompareColumn>,
    val rows: List<CompareRow>,
    val note: String,
    val pending: PendingOffer?,
    val remindLabel: String,
    val askLabel: String,
    val awardLabel: String,
)

data class DocumentFilter(val text: String, val kind: DocumentKind?)

data class DocumentBanner(val title: String, val sub: String, val amount: String)

data class DocumentRow(
    val id: String,
    val kind: DocumentKind,
    val title: String,
    val meta: String,
    val amount: String?,
    val target: AppRoute?,
)

data class DocumentsModel(
    val countBadge: String,
    val addLabel: String,
    val uploadLabel: String,
    val searchLabel: String,
    val filtersLabel: String,
    val filters: List<DocumentFilter>,
    val initialFilter: Int,
    val banner: DocumentBanner,
    val recentLabel: String,
    val byTradeLabel: String,
    val rows: List<DocumentRow>,
) {
    /** Index 0 shows all, i >= 1 the kind of filter i; out of range shows all. */
    fun rows(filter: Int): List<DocumentRow> {
        val kind = filters.getOrNull(filter)?.kind ?: return rows
        return rows.filter { it.kind == kind }
    }
}

data class ViewerLineRow(val pos: String, val text: String, val sum: String)

data class DocViewerModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val pageLabel: String,
    val issuerName: String,
    val issuerAddress: String,
    val number: String,
    val reference: String,
    val lines: List<ViewerLineRow>,
    val totalLabel: String,
    val total: String,
    val facts: List<Fact>,
    val compareLabel: String,
    val compareTarget: AppRoute?,
    val shareLabel: String,
    val changeLabel: String,
)

data class NotificationRow(
    val id: String,
    val kind: NotificationKind,
    val title: String,
    val text: String,
    val `when`: String,
    val unread: Boolean,
    val target: NavigationTarget?,
)

data class NotificationGroupRow(val title: String, val items: List<NotificationRow>)

data class NotificationsModel(
    val title: String,
    val markAllLabel: String,
    val groups: List<NotificationGroupRow>,
    val unreadCount: Int,
)

/** Every screen model, built once from the repository (D11). */
data class AppData(
    val login: LoginModel,
    val home: HomeModel,
    val schedule: ScheduleModel,
    val profile: ProfileModel,
    val trades: Map<String, GewerkModel>,
    val comparisons: Map<String, CompareModel>,
    val documents: DocumentsModel,
    val viewers: Map<String, DocViewerModel>,
    val notifications: NotificationsModel,
    val tradeNames: Map<String, String>,
    val inspections: InspectionsModel,
    val defectsBoard: DefectsBoardModel,
    val inspectionElements: Map<String, ElementsModel>,
    val inspectionSteps: Map<AppRoute, StepModel>,
    val defectForm: DefectFormModel,
    val signOffs: Map<String, SignOffModel>,
) {
    companion object {
        /** The one trade with inspection models in M4 (the other trades are M5/M6). */
        private const val INSPECTED_TRADE = "windows"

        private const val CHECKS = "inspections.byTrade.$INSPECTED_TRADE.checks"

        private const val OVERVIEW = "inspections.overview"

        private val STEP_NUMBER = Regex("^[1-9][0-9]*$")

        /** Content width of the Zeitplan chart on the 390 dp screen (AndZeitplan.dc.html), fixed, not measured. */
        private const val CHART_WIDTH = 358f

        /** Document tile icons by position: document, contract, receipt, map (AndHome.dc.html:106-109). */
        private val DOC_ICONS = listOf(BaupilotIcon.Document, BaupilotIcon.Contract, BaupilotIcon.Receipt, BaupilotIcon.Map)

        suspend fun load(repository: BackendRepository): AppData {
            val project = repository.project()
            val trades = repository.trades()
            val schedule = repository.schedule()
            val loginText = repository.screenText("Login", LoginScreenText.serializer())
            val homeText = repository.screenText("Home", HomeScreenText.serializer())
            val zeitplanText = repository.screenText("Zeitplan", ZeitplanScreenText.serializer())
            val profilText = repository.screenText("Profil", ProfilScreenText.serializer())

            val phaseTitle = trades.firstOrNull { it.id == project.phase.tradeId }?.name
                ?: throw RepositoryException.Decoding("project.phase.tradeId")

            val login = LoginModel(
                chips = loginText.chips,
                title = loginText.title,
                intro = loginText.intro,
                emailLabel = loginText.emailLabel,
                emailPlaceholder = loginText.emailPlaceholder,
                passwordLabel = loginText.passwordLabel,
                forgot = loginText.forgot,
                submit = loginText.submit,
                social = loginText.social,
                create = loginText.create,
            )
            val home = HomeModel(
                projectLabel = homeText.projectLabel,
                notificationsAria = homeText.notificationsAria,
                badge = homeText.badge,
                phaseLabel = homeText.phaseLabel,
                phaseTitle = phaseTitle,
                progress = project.phase.progressPercent / 100f,
                moveInLabel = homeText.moveInLabel,
                trades = homeText.trades.map { row ->
                    HomeTradeRow(
                        initial = row.initial,
                        name = row.name,
                        `when` = row.`when`,
                        statusLabel = row.statusLabel,
                        tradeId = row.tradeId,
                        colors = ScreenCopy.Home.tone(row.tradeId)
                            ?: throw RepositoryException.Decoding("screens.Home.android.trades.tradeId"),
                    )
                },
                docs = homeText.docs.mapIndexed { index, doc ->
                    HomeDocTile(
                        doc.label,
                        doc.count,
                        DOC_ICONS.getOrNull(index) ?: throw RepositoryException.Decoding("screens.Home.android.docs"),
                    )
                },
                tabs = homeText.tabs,
            )
            val scheduleModel = ScheduleModel(
                moveInLabel = zeitplanText.moveInLabel,
                ariaLabel = zeitplanText.ariaLabel,
                asks = schedule.asks.map { ask -> ScheduleAskRow(ask.name, ask.sub, RouteParser.route(ask.target, "schedule.asks.target")) },
                layout = ScheduleLayout.make(trades, schedule, CHART_WIDTH),
                tabs = zeitplanText.tabs,
            )
            val profile = ProfileModel(
                initials = project.owner.initials,
                name = project.owner.name,
                email = project.owner.email,
                groups = profilText.groups.map { group ->
                    ProfileGroup(
                        group.title,
                        group.rows.map { ProfileRow(it.label, it.sub, it.isToggle, it.checked) },
                    )
                },
                tabs = profilText.tabs,
            )
            val offers = repository.offers(null)
            val documentItems = repository.documents()
            val notificationGroups = repository.notifications()
            val comparisonList = trades.mapNotNull { repository.comparison(it.id) }
            val withViewer = documentItems.items.filter { it.viewer != null }.map { it.id }.toSet()
            fun documentTarget(documentId: String?): AppRoute? =
                documentId?.takeIf { it in withViewer }?.let { AppRoute.Document(it) }

            val gewerkText = repository.screenText("Gewerk", GewerkScreenText.serializer())
            val dokumenteText = repository.screenText("Dokumente", DokumenteScreenText.serializer())
            repository.screenText("DokViewer", EmptyScreenText.serializer())
            repository.screenText("Vergleich", EmptyScreenText.serializer())
            repository.screenText("Mitteilungen", EmptyScreenText.serializer())

            val compareByTrade = comparisonList.map { it.tradeId }.toSet()
            val gewerke = trades.filter { it.detail != null }.associate { trade ->
                val detail = trade.detail!!
                val tradeOffers = offers.filter { it.tradeId == trade.id }
                trade.id to GewerkModel(
                    tradeId = trade.id,
                    title = trade.name,
                    statusLabel = trade.statusLabel ?: throw RepositoryException.Decoding("trades.statusLabel"),
                    phaseLabel = detail.phaseLabel,
                    facts = detail.facts.mapIndexed { index, fact ->
                        GewerkFact(
                            fact.label,
                            fact.value,
                            ScreenCopy.Gewerk.FACT_ICONS.getOrNull(index) ?: throw RepositoryException.Decoding("trades.detail.facts"),
                        )
                    },
                    offers = tradeOffers.map { offer ->
                        OfferRow(
                            id = offer.id,
                            firm = offer.firm,
                            meta = offer.meta.value ?: throw RepositoryException.Decoding("offers[${offers.indexOf(offer)}].meta"),
                            price = offer.price,
                            best = offer.best,
                            pending = offer.pending,
                            target = documentTarget(offer.documentId),
                        )
                    },
                    bestLabel = ScreenCopy.Gewerk.BEST,
                    tabs = gewerkText.tabs.map { GewerkTab(it.label, it.selected) },
                    compareLabel = ScreenCopy.Gewerk.COMPARE,
                    compareTarget = if (trade.id in compareByTrade) AppRoute.Compare(trade.id) else null,
                    addLabel = ScreenCopy.Gewerk.ADD,
                    moreLabel = ScreenCopy.Gewerk.MORE,
                )
            }

            val comparisons = comparisonList.associate { comparison ->
                val columnOffers = comparison.offerIds.map { id -> offers.firstOrNull { it.id == id } }
                if (columnOffers.size != comparison.columns.size || columnOffers.any { it == null }) {
                    throw RepositoryException.Decoding("comparisons.offerIds")
                }
                val pendingOffer = offers.firstOrNull { it.tradeId == comparison.tradeId && it.pending }
                comparison.tradeId to CompareModel(
                    tradeId = comparison.tradeId,
                    title = ScreenCopy.Vergleich.TITLE,
                    subtitle = comparison.hint.value ?: throw RepositoryException.Decoding("comparisons.hint"),
                    tableLabel = ScreenCopy.Vergleich.TABLE,
                    columns = comparison.columns.mapIndexed { index, name ->
                        val offer = columnOffers[index]!!
                        CompareColumn(offer.id, name, offer.best, documentTarget(offer.documentId))
                    },
                    rows = comparison.rows.map { CompareRow(it.label, it.a, it.b, it.strong, it.a != it.b && !it.strong) },
                    note = comparison.summary.value ?: throw RepositoryException.Decoding("comparisons.summary"),
                    pending = pendingOffer?.let {
                        PendingOffer(
                            it.firm,
                            comparison.pending.value ?: throw RepositoryException.Decoding("comparisons.pending"),
                        )
                    },
                    remindLabel = ScreenCopy.Vergleich.REMIND,
                    askLabel = ScreenCopy.Vergleich.ASK,
                    awardLabel = ScreenCopy.Vergleich.AWARD,
                )
            }

            val filterField = "screens.Dokumente.android.filters"
            val categories = documentItems.categories
            if (dokumenteText.filters.size != categories.size + 1 ||
                categories.indices.any { "${categories[it].label} ${categories[it].count}" != dokumenteText.filters[it + 1].text }
            ) {
                throw RepositoryException.Decoding(filterField)
            }
            val documents = DocumentsModel(
                countBadge = dokumenteText.countBadge,
                addLabel = ScreenCopy.Dokumente.UPLOAD,
                uploadLabel = ScreenCopy.Dokumente.UPLOAD,
                searchLabel = dokumenteText.searchLabel,
                filtersLabel = ScreenCopy.Dokumente.FILTERS,
                filters = dokumenteText.filters.mapIndexed { index, chip ->
                    DocumentFilter(chip.text, if (index == 0) null else DocumentKind.of(categories[index - 1].kind, filterField))
                },
                initialFilter = dokumenteText.filters.indexOfFirst { it.selected }.takeIf { it >= 0 }
                    ?: throw RepositoryException.Decoding(filterField),
                banner = DocumentBanner(dokumenteText.banner.title, dokumenteText.banner.sub, dokumenteText.banner.amount),
                recentLabel = dokumenteText.recentLabel,
                byTradeLabel = dokumenteText.byTradeLabel,
                rows = documentItems.items.map { item ->
                    DocumentRow(
                        id = item.id,
                        kind = DocumentKind.of(item.kind, "documents.items.kind"),
                        title = item.title,
                        meta = item.meta.value ?: throw RepositoryException.Decoding("documents.items.meta"),
                        amount = item.amount,
                        target = documentTarget(item.id),
                    )
                },
            )

            val viewers = documentItems.items.filter { it.viewer != null }.associate { item ->
                val viewer = item.viewer!!
                if (viewer.facts.size < 3) throw RepositoryException.Decoding("documents.items.viewer.facts")
                item.id to DocViewerModel(
                    id = item.id,
                    title = item.title,
                    subtitle = viewer.subtitle.value ?: throw RepositoryException.Decoding("viewer.subtitle"),
                    pageLabel = viewer.pageLabel,
                    issuerName = viewer.issuer.name,
                    issuerAddress = viewer.issuer.address,
                    number = viewer.number,
                    reference = viewer.reference,
                    lines = viewer.lines.map { ViewerLineRow(it.pos, it.text, it.sum) },
                    totalLabel = ScreenCopy.DokViewer.TOTAL,
                    total = viewer.total,
                    facts = viewer.facts,
                    compareLabel = ScreenCopy.DokViewer.COMPARE,
                    compareTarget = item.tradeId?.takeIf { it in compareByTrade }?.let { AppRoute.Compare(it) },
                    shareLabel = ScreenCopy.DokViewer.SHARE,
                    changeLabel = ScreenCopy.DokViewer.CHANGE,
                )
            }

            val notificationsModel = NotificationsModel(
                title = ScreenCopy.NOTIFICATIONS_TITLE,
                markAllLabel = ScreenCopy.Mitteilungen.MARK_ALL,
                groups = notificationGroups.map { group ->
                    NotificationGroupRow(
                        group.title,
                        group.items.map { item ->
                            NotificationRow(
                                id = item.id,
                                kind = NotificationKind.of(item.kind, "notifications.items.kind"),
                                title = item.title,
                                text = item.text,
                                `when` = item.`when`,
                                unread = item.unread,
                                target = RouteParser.target(item.target, "notifications.items.target"),
                            )
                        },
                    )
                },
                unreadCount = notificationGroups.sumOf { group -> group.items.count { it.unread } },
            )
            val inspection = loadInspections(repository)
            return AppData(
                login = login,
                home = home,
                schedule = scheduleModel,
                profile = profile,
                trades = gewerke,
                comparisons = comparisons,
                documents = documents,
                viewers = viewers,
                notifications = notificationsModel,
                tradeNames = trades.associate { it.id to it.name },
                inspections = inspection.inspections,
                defectsBoard = inspection.defectsBoard,
                inspectionElements = mapOf(INSPECTED_TRADE to inspection.elements),
                inspectionSteps = mapOf<AppRoute, StepModel>(inspection.step.route to inspection.step),
                defectForm = inspection.defectForm,
                signOffs = mapOf(INSPECTED_TRADE to inspection.signOff),
            )
        }

        private class InspectionParts(
            val inspections: InspectionsModel,
            val defectsBoard: DefectsBoardModel,
            val elements: ElementsModel,
            val step: StepModel,
            val defectForm: DefectFormModel,
            val signOff: SignOffModel,
        )

        /** The platform's side of [text]; null there is a decoding error at [field] (D16, I6). */
        private fun side(text: PlatformText, field: String): String = text.value ?: throw RepositoryException.Decoding(field)

        private suspend fun loadInspections(repository: BackendRepository): InspectionParts {
            val now = repository.now()
            val defects = repository.defects()
            val resolved = repository.resolvedDefects()
            val gewerkeText = repository.screenText("AbnGewerke", TabsScreenText.serializer())
            val maengelText = repository.screenText("AbnMaengel", TabsScreenText.serializer())
            for (name in listOf("AbnElemente", "AbnPruefung", "AbnMangel", "AbnSignatur")) {
                repository.screenText(name, EmptyScreenText.serializer())
            }
            val overview = repository.inspectionOverview()
            val elementsCheck = repository.inspectionCheck(INSPECTED_TRADE, "elements", ElementsCheck.serializer())
            val stepCheck = repository.inspectionCheck(INSPECTED_TRADE, "stepF8", StepCheck.serializer())
            val formCheck = repository.inspectionCheck(INSPECTED_TRADE, "defectF2", DefectFormCheck.serializer())
            val signatureCheck = repository.inspectionCheck(INSPECTED_TRADE, "signature", SignatureCheck.serializer())

            fun tabTitle(text: TabsScreenText, screen: String): String =
                text.tabs.firstOrNull { it.route == AppTab.Inspections.route }?.label
                    ?: throw RepositoryException.Decoding("screens.$screen.android.tabs")

            fun statusLabel(key: String): String =
                overview.statusLabels[key]?.value ?: throw RepositoryException.Decoding("$OVERVIEW.statusLabels")

            val views = listOf(
                ScreenCopy.AbnGewerke.TRADES_VIEW,
                String.format(Locale.ROOT, ScreenCopy.AbnGewerke.DEFECTS_VIEW_FORMAT, defects.size),
            )
            val inspections = InspectionsModel(
                title = tabTitle(gewerkeText, "AbnGewerke"),
                templatesLabel = ScreenCopy.AbnGewerke.TEMPLATES,
                views = views,
                viewsLabel = ScreenCopy.AbnGewerke.VIEWS,
                rows = overview.rows.map { row ->
                    InspectionTradeRow(
                        tradeId = row.tradeId,
                        name = row.name,
                        subtitle = side(row.sub, "$OVERVIEW.rows.sub"),
                        status = InspectionStatus.of(row.status, "$OVERVIEW.rows.status"),
                        statusLabel = statusLabel(row.status),
                        target = RouteParser.route(row.route, "$OVERVIEW.rows.route"),
                    )
                },
            )
            val defectsBoard = DefectsBoardModel(
                title = tabTitle(maengelText, "AbnMaengel"),
                templatesLabel = ScreenCopy.AbnGewerke.TEMPLATES,
                views = views,
                viewsLabel = ScreenCopy.AbnGewerke.VIEWS,
                cards = defects.map { defect ->
                    val urgency = DefectUrgency.of(defect.urgency, "defects.urgency")
                    DefectCard(
                        id = defect.id,
                        trade = side(defect.trade, "defects.trade"),
                        title = defect.title,
                        dueLabel = defect.dueLabel,
                        meta = defect.meta,
                        urgency = urgency,
                        showsActions = urgency == DefectUrgency.Late,
                    )
                },
                remindLabel = ScreenCopy.AbnMaengel.REMIND,
                resolveLabel = ScreenCopy.AbnMaengel.RESOLVE,
                resolvedLabel = ScreenCopy.AbnMaengel.RESOLVED,
                resolvedCount = resolved.count,
            )

            val elements = buildElements(elementsCheck)
            val step = buildStep(stepCheck, elements.nextTarget as? AppRoute.InspectionStep
                ?: throw RepositoryException.Decoding("$CHECKS.elements.floors.items.route"), ::statusLabel)
            return InspectionParts(
                inspections = inspections,
                defectsBoard = defectsBoard,
                elements = elements,
                step = step,
                defectForm = buildDefectForm(formCheck, now),
                signOff = buildSignOff(signatureCheck),
            )
        }

        private fun buildElements(check: ElementsCheck): ElementsModel {
            val path = "$CHECKS.elements"
            val progress = check.progress.map { key ->
                ElementStatus.of(key, "$path.progress").also {
                    if (it == ElementStatus.Now) throw RepositoryException.Decoding("$path.progress")
                }
            }
            val floors = check.floors.map { floor ->
                FloorSection(
                    title = floor.title,
                    moreLabel = floor.more,
                    items = floor.items.map { item ->
                        ElementRow(
                            id = item.id,
                            room = item.room,
                            type = item.type,
                            label = item.label,
                            status = ElementStatus.of(item.status, "$path.floors.items.status"),
                            target = RouteParser.route(item.route, "$path.floors.items.route"),
                        )
                    },
                )
            }
            val current = floors.flatMap { it.items }.filter { it.status == ElementStatus.Now }
            if (current.size != 1) throw RepositoryException.Decoding("$path.floors")
            val nextTarget = current.single().target ?: throw RepositoryException.Decoding("$path.floors.items.route")
            fun summaryLabel(status: String): String =
                side(
                    check.summary.firstOrNull { it.status == status }?.label ?: throw RepositoryException.Decoding("$path.summary"),
                    "$path.summary.label",
                )
            return ElementsModel(
                tradeId = INSPECTED_TRADE,
                title = check.title,
                subtitle = side(check.subtitle, "$path.subtitle"),
                counter = side(check.counter, "$path.counter"),
                guide = side(check.guide, "$path.guide"),
                helpLabel = ScreenCopy.AbnElemente.GUIDE,
                progress = progress,
                // AndAbnElemente.dc.html:44-49: all, open, defect.
                chips = listOf(side(check.allLabel, "$path.allLabel"), summaryLabel("open"), summaryLabel("defect")),
                floors = floors,
                nextLabel = side(check.nextLabel, "$path.nextLabel"),
                nextTarget = nextTarget,
            )
        }

        private fun buildStep(check: StepCheck, route: AppRoute.InspectionStep, statusLabel: (String) -> String): StepModel {
            val path = "$CHECKS.stepF8"
            val number = check.current.n.takeIf { STEP_NUMBER.matches(it) }?.toIntOrNull()
            if (number != route.step || !check.title.startsWith("${route.elementId} · ")) {
                throw RepositoryException.Decoding(path)
            }
            if (check.current.photos.size != 2) throw RepositoryException.Decoding("$path.current.photos")
            return StepModel(
                route = route,
                counter = side(check.counter, "$path.counter"),
                title = check.title,
                subtitle = side(check.subtitle, "$path.subtitle"),
                done = check.done.map { StepDoneRow(it.label, side(it.meta, "$path.done.meta")) },
                current = StepCurrent(
                    n = number,
                    title = check.current.title,
                    text = check.current.text,
                    photoCaption = check.current.photos[0],
                    addPhotoLabel = check.current.photos[1],
                ),
                guideLabel = ScreenCopy.AbnPruefung.GUIDE,
                defectLabel = statusLabel("defect"),
                okLabel = statusLabel("ok"),
                next = check.next.map { StepNextRow(it.n, side(it.label, "$path.next.label")) },
                defectTarget = AppRoute.NewDefect,
                progress = ScreenCopy.AbnPruefung.PROGRESS,
            )
        }

        private fun buildDefectForm(check: DefectFormCheck, now: java.time.LocalDate): DefectFormModel {
            val path = "$CHECKS.defectF2"
            val severity = check.severity.options.indexOf(check.severity.selected)
            if (severity < 0) throw RepositoryException.Decoding("$path.severity")
            val offsets = ScreenCopy.AbnMangel.DEADLINE_OFFSET_DAYS
            if (check.deadlineOptions.size > offsets.size) throw RepositoryException.Decoding("$path.deadlineOptions")
            val options = check.deadlineOptions.mapIndexed { index, option ->
                DeadlineOption(side(option, "$path.deadlineOptions"), offsets[index])
            }
            val deadline = side(check.deadlineLabel, "$path.deadlineLabel")
            val initialDeadline = options.indexOfFirst { option ->
                DeadlineMath.label(now, option.offsetDays!!, DeadlineStyle.Android) == deadline
            }
            if (initialDeadline < 0) throw RepositoryException.Decoding("$path.deadlineLabel")
            return DefectFormModel(
                tradeId = INSPECTED_TRADE,
                title = ScreenCopy.AbnMangel.TITLE,
                saveLabel = ScreenCopy.AbnMangel.SAVE,
                context = check.context,
                finding = check.title,
                descriptionLabel = ScreenCopy.AbnMangel.DESCRIPTION,
                description = check.description,
                photosLabel = check.photosLabel,
                photoLabels = check.photos,
                addPhotoLabel = ScreenCopy.AbnMangel.ADD_PHOTO,
                severityLabel = ScreenCopy.AbnMangel.SEVERITY,
                severities = check.severity.options,
                initialSeverity = severity,
                selectedPrefix = ScreenCopy.AbnMangel.SELECTED_PREFIX,
                deadlineLabel = ScreenCopy.AbnMangel.DEADLINE,
                deadlineOptions = options,
                deadlineBase = now,
                initialDeadline = initialDeadline,
                responsibleLabel = ScreenCopy.AbnMangel.RESPONSIBLE,
                responsible = check.responsible,
                notifyLabel = check.notify.label,
                notifyInitial = check.notify.enabled,
            )
        }

        private fun buildSignOff(check: SignatureCheck): SignOffModel {
            val path = "$CHECKS.signature"
            // Tones by position, as the renderVals stats of AndAbnSignatur.dc.html:85-88.
            val tones = listOf(CountTone.Surface, CountTone.Ok, CountTone.Error)
            if (check.stats.size != tones.size) throw RepositoryException.Decoding("$path.stats")
            return SignOffModel(
                inspectionId = INSPECTED_TRADE,
                title = check.title,
                subtitle = side(check.subtitle, "$path.subtitle"),
                stats = check.stats.mapIndexed { index, stat -> SignOffStat(stat.n, stat.label, tones[index]) },
                reservedHeading = ScreenCopy.AbnSignatur.RESERVED,
                defects = check.defects.map {
                    SignOffDefect(it.title, it.meta, "${ScreenCopy.AbnSignatur.DUE_PREFIX} ${it.due}")
                },
                reservation = check.reservation,
                craftsmanLabel = ScreenCopy.AbnSignatur.CRAFTSMAN,
                craftsmanName = check.craftsman.name,
                craftsmanStatus = check.craftsman.signed,
                ownerLabel = side(check.owner, "$path.owner"),
                signHereLabel = ScreenCopy.AbnSignatur.SIGN_HERE,
                signatureLabel = ScreenCopy.AbnSignatur.SIGNATURE,
                finishLabel = ScreenCopy.AbnSignatur.FINISH,
                note = check.note,
                finishTarget = NavigationTarget.Defects,
            )
        }
    }
}
