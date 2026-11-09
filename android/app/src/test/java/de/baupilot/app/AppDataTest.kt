package de.baupilot.app

import de.baupilot.data.MockRepository
import de.baupilot.data.RepositoryException
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.components.StatusChipColors
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDataTest {
    private val fixture: String =
        File(System.getProperty("baupilot.repoRoot"), "fixtures/baupilot.json").readText()

    private val repository = MockRepository(fixture)

    /** The fixture with [edit] applied to the parsed root, as a new repository. */
    private fun edited(edit: (JsonObject) -> JsonObject): MockRepository =
        MockRepository(edit(Json.parseToJsonElement(fixture).jsonObject).toString())

    @Test
    fun homeCarriesAndroidValues() = runTest {
        val home = AppData.load(repository).home
        assertEquals("5 / 11", home.phaseLabel)
        assertEquals("2", home.badge)
        assertEquals("Einzug geplant September 2027", home.moveInLabel)
        assertEquals("Einfamilienhaus · Musterweg 12", home.projectLabel)
        assertEquals("Mitteilungen, 2 neu", home.notificationsAria)
        assertEquals("Fenster & Türen", home.phaseTitle)
        assertEquals(0.45f, home.progress, 0.0001f)
    }

    @Test
    fun homeTradesKeepInitialsIdsAndTones() = runTest {
        val trades = AppData.load(repository).home.trades
        assertEquals(listOf("El", "In", "Es"), trades.map { it.initial })
        assertEquals(listOf("electrical", "plumbing", "screed"), trades.map { it.tradeId })
        assertEquals("ab KW 3 · Rohinstallation", trades[0].`when`)
        assertEquals("Beauftragt", trades[0].statusLabel)
        assertTrue(trades[0].colors is StatusChipColors.Ok)
        assertSame(StatusChipColors.Info, trades[1].colors)
        assertSame(StatusChipColors.Outline, trades[2].colors)
    }

    @Test
    fun homeDocTilesGetIconsByPosition() = runTest {
        val docs = AppData.load(repository).home.docs
        assertEquals(listOf("Angebote", "Verträge", "Rechnungen", "Pläne"), docs.map { it.label })
        assertEquals(listOf("7", "3", "4", "12"), docs.map { it.count })
        assertEquals(
            listOf(BaupilotIcon.Document, BaupilotIcon.Contract, BaupilotIcon.Receipt, BaupilotIcon.Map),
            docs.map { it.icon },
        )
    }

    @Test
    fun tabsComeFromTheFixture() = runTest {
        val tabs = AppData.load(repository).home.tabs
        assertEquals(listOf("Übersicht", "Zeitplan", "Abnahmen", "Dokumente", "Profil"), tabs.map { it.label })
        assertEquals(AppTab.entries.map { it.route }, tabs.map { it.route })
    }

    @Test
    fun tradeNamesMapIdToName() = runTest {
        val names = AppData.load(repository).tradeNames
        assertEquals("Estrich", names["screed"])
        assertEquals("Fenster & Türen", names["windows"])
        assertEquals(11, names.size)
    }

    @Test
    fun scheduleAsksParseTargets() = runTest {
        val schedule = AppData.load(repository).schedule
        assertEquals("Einzug geplant September 2027", schedule.moveInLabel)
        assertTrue(schedule.ariaLabel.startsWith("Zeitplan aller Gewerke"))
        assertEquals(listOf("Estrich", "Fliesen & Maler"), schedule.asks.map { it.name })
        assertEquals(AppRoute.Trade("screed"), schedule.asks[0].target)
        assertNull(schedule.asks[1].target)
        assertEquals("Start KW 8 · Angebote bis 15.01.", schedule.asks[0].sub)
    }

    @Test
    fun scheduleLayoutUsesTheFixedAndroidChartWidth() = runTest {
        val layout = AppData.load(repository).schedule.layout
        assertEquals(11, layout.rows.size)
        assertEquals(listOf("Mär 26", "Jul", "Nov", "Mär 27", "Jul"), layout.monthLabels.map { it.label })
        assertTrue(layout.todayX in 126f..358f)
    }

    @Test
    fun unknownAskTargetIsDecodingError() = runTest {
        val bad = edited { root ->
            val schedule = root.getValue("schedule").jsonObject
            val asks = schedule.getValue("asks").jsonArray.map {
                JsonObject(it.jsonObject + ("target" to JsonPrimitive("elsewhere/1")))
            }
            JsonObject(root + ("schedule" to JsonObject(schedule + ("asks" to JsonArray(asks)))))
        }
        val error = runCatching { AppData.load(bad) }.exceptionOrNull() as RepositoryException.Decoding
        assertEquals("schedule.asks.target", error.detail)
    }

    @Test
    fun missingPhaseTradeIsDecodingError() = runTest {
        val bad = edited { root ->
            val project = root.getValue("project").jsonObject
            val phase = project.getValue("phase").jsonObject
            val moved = JsonObject(phase + ("tradeId" to JsonPrimitive("nowhere")))
            JsonObject(root + ("project" to JsonObject(project + ("phase" to moved))))
        }
        val error = runCatching { AppData.load(bad) }.exceptionOrNull() as RepositoryException.Decoding
        assertEquals("project.phase.tradeId", error.detail)
    }

    @Test
    fun profileCarriesOwnerAndRowsWithSub() = runTest {
        val profile = AppData.load(repository).profile
        assertEquals("MM", profile.initials)
        assertEquals("Max Mustermann", profile.name)
        assertEquals("max@beispiel.de", profile.email)
        assertEquals(listOf("Bauvorhaben", "Benachrichtigungen", "App"), profile.groups.map { it.title })
        val first = profile.groups[0].rows[0]
        assertEquals("Einfamilienhaus Musterweg 12", first.label)
        assertEquals("Baubeginn März 2026", first.sub)
        val toggles = profile.groups[1].rows
        assertEquals(listOf(true, true, false), toggles.map { it.checked })
        assertNull(toggles[1].sub)
        assertNotNull(profile.groups[2].rows[1])
    }

    @Test
    fun loginCarriesFixtureText() = runTest {
        val login = AppData.load(repository).login
        assertEquals("Willkommen bei Baupilot", login.title)
        assertEquals(listOf("Zeitplan", "Angebote", "Dokumente"), login.chips)
        assertEquals("Anmelden", login.submit)
    }

    /** Applies [edit] to the object at `root[key]`. */
    private fun JsonObject.editObject(key: String, edit: (JsonObject) -> JsonObject): JsonObject =
        JsonObject(this + (key to edit(getValue(key).jsonObject)))

    private fun JsonObject.mapArray(key: String, edit: (Int, JsonObject) -> JsonObject): JsonObject =
        JsonObject(this + (key to JsonArray(getValue(key).jsonArray.mapIndexed { i, e -> edit(i, e.jsonObject) })))

    private fun failure(block: suspend () -> Unit): RepositoryException.Decoding = runBlocking {
        runCatching { block() }.exceptionOrNull() as RepositoryException.Decoding
    }

    /** "9.800,00 €" or "420,00" to cents. */
    private fun cents(text: String): Long =
        text.removeSuffix(" €").replace(".", "").replace(",", "").toLong()

    @Test
    fun gewerkModelFromFixture() = runTest {
        val gewerk = AppData.load(repository).trades.getValue("screed")
        assertEquals("screed", gewerk.tradeId)
        assertEquals("Estrich", gewerk.title)
        assertEquals("Angebote einholen", gewerk.statusLabel)
        assertEquals("Phase 8 · Estrich", gewerk.phaseLabel)
        assertEquals(
            listOf("Zeitfenster" to "KW 8–9", "Angebotsfrist" to "15.01.2027", "Wartet auf" to "Installateur"),
            gewerk.facts.map { it.label to it.value },
        )
        assertEquals(listOf(BaupilotIcon.Calendar, BaupilotIcon.Clock, BaupilotIcon.Link), gewerk.facts.map { it.icon })
        assertEquals("Günstigstes", gewerk.bestLabel)
        assertEquals("Angebote vergleichen", gewerk.compareLabel)
    }

    @Test
    fun gewerkTabsComeFromTheAndroidScreenText() = runTest {
        val tabs = AppData.load(repository).trades.getValue("screed").tabs
        assertEquals(listOf("Angebote (3)", "Termine", "Notizen"), tabs.map { it.label })
        assertEquals(listOf(true, false, false), tabs.map { it.selected })
    }

    @Test
    fun offerRowsUseAndroidMetaAndPendingText() = runTest {
        val offers = AppData.load(repository).trades.getValue("screed").offers
        assertEquals(listOf("offer-wagner", "offer-hahn", "offer-lorenz"), offers.map { it.id })
        assertEquals("PDF · 10.12.2026", offers[0].meta)
        assertEquals("PDF · 14.12.2026", offers[1].meta)
        assertEquals("Angefragt am 05.12.", offers[2].meta)
        assertEquals(listOf(true, false, false), offers.map { it.best })
        assertEquals(listOf(false, false, true), offers.map { it.pending })
        assertEquals("Ausstehend", offers[2].price)
    }

    @Test
    fun offerTargetsFollowViewerData() = runTest {
        val offers = AppData.load(repository).trades.getValue("screed").offers
        assertEquals(AppRoute.Document("doc-offer-wagner"), offers[0].target)
        assertNull(offers[1].target)
        assertNull(offers[2].target)
    }

    @Test
    fun gewerkCompareTarget() = runTest {
        assertEquals(AppRoute.Compare("screed"), AppData.load(repository).trades.getValue("screed").compareTarget)
    }

    @Test
    fun onlyTradesWithDetailHaveAGewerkModel() = runTest {
        assertEquals(setOf("screed"), AppData.load(repository).trades.keys)
        assertEquals(setOf("screed"), AppData.load(repository).comparisons.keys)
    }

    @Test
    fun missingPlatformValueIsDecodingError() = runTest {
        val bad = edited { root ->
            root.mapArray("offers") { i, offer ->
                if (i != 0) offer else JsonObject(offer + ("meta" to JsonObject(mapOf("ios" to JsonPrimitive("x"), "android" to JsonNull))))
            }
        }
        assertEquals("offers[0].meta", failure { AppData.load(bad) }.detail)
    }

    @Test
    fun viewerWithFewerThanThreeFactsIsDecodingError() = runTest {
        val bad = edited { root ->
            root.editObject("documents") { documents ->
                documents.mapArray("items") { _, item ->
                    val viewer = item["viewer"] as? JsonObject ?: return@mapArray item
                    JsonObject(item + ("viewer" to JsonObject(viewer + ("facts" to JsonArray(viewer.getValue("facts").jsonArray.take(2))))))
                }
            }
        }
        assertEquals("documents.items.viewer.facts", failure { AppData.load(bad) }.detail)
    }

    @Test
    fun androidIgnoresIosOnlyNullValues() = runTest {
        val item = repository.documents().items.first { it.viewer != null }
        assertNull(item.viewer!!.pageFooter.android)
        assertNull(repository.trades().first { it.detail != null }.detail!!.offersSummary.android)
        assertNotNull(AppData.load(repository).trades["screed"])
        assertNotNull(AppData.load(repository).viewers["doc-offer-wagner"])
    }

    @Test
    fun loadsAllM3ScreenTexts() = runTest {
        for (name in listOf("Gewerk", "Dokumente", "DokViewer", "Vergleich", "Mitteilungen")) {
            val broken = edited { root ->
                root.editObject("screens") { screens ->
                    JsonObject(screens + (name to JsonObject(screens.getValue(name).jsonObject - "android")))
                }
            }
            val error = runCatching { AppData.load(broken) }.exceptionOrNull()
            assertTrue(name, error is RepositoryException.FixtureMissing)
            assertEquals("screens.$name.android", (error as RepositoryException.FixtureMissing).path)
        }
        assertNotNull(AppData.load(repository))
    }

    @Test
    fun compareColumnsAndTargets() = runTest {
        val compare = AppData.load(repository).comparisons.getValue("screed")
        assertEquals("screed", compare.tradeId)
        assertEquals("Angebote vergleichen", compare.title)
        assertEquals("Estrich · 2 von 3 · 112 m²", compare.subtitle)
        assertEquals("Vergleich der Estrich-Angebote", compare.tableLabel)
        assertEquals(listOf("Estrichbau Wagner", "Fließestrich Hahn"), compare.columns.map { it.name })
        assertEquals(listOf(true, false), compare.columns.map { it.best })
        assertEquals(AppRoute.Document("doc-offer-wagner"), compare.columns[0].target)
        assertNull(compare.columns[1].target)
        assertEquals("Hahn enthält das Belegreifheizen (ca. 600 €). Ohne diesen Posten liegen beide nur rund 50 € auseinander.", compare.note)
        assertEquals("Erinnern", compare.remindLabel)
        assertEquals("Rückfrage", compare.askLabel)
        assertEquals("Wagner beauftragen", compare.awardLabel)
    }

    @Test
    fun compareRowsHighlightRule() = runTest {
        val rows = AppData.load(repository).comparisons.getValue("screed").rows
        assertEquals(7, rows.size)
        assertEquals(listOf(true, true, false, false, false, false, false), rows.map { it.strong })
        assertEquals(listOf(false, false, true, true, true, true, true), rows.map { it.highlighted })
        assertEquals(listOf("Preis brutto", "Pro m²", "Estrichart"), rows.take(3).map { it.label })
        assertEquals("9.800 €", rows[0].a)
        assertEquals("10.450 €", rows[0].b)
    }

    @Test
    fun comparePendingRow() = runTest {
        val pending = AppData.load(repository).comparisons.getValue("screed").pending
        assertEquals("Bodenbau Lorenz", pending?.firm)
        assertEquals("Angefragt am 05.12. · noch offen", pending?.text)
    }

    @Test
    fun compareOfferIdMismatchIsDecodingError() = runTest {
        val bad = edited { root ->
            root.mapArray("comparisons") { _, c ->
                JsonObject(c + ("offerIds" to JsonArray(listOf(JsonPrimitive("offer-wagner"), JsonPrimitive("offer-nobody")))))
            }
        }
        assertEquals("comparisons.offerIds", failure { AppData.load(bad) }.detail)
    }

    @Test
    fun documentsHeaderAndSearch() = runTest {
        val documents = AppData.load(repository).documents
        assertEquals("26", documents.countBadge)
        assertEquals("Hochladen", documents.addLabel)
        assertEquals("Hochladen", documents.uploadLabel)
        assertEquals("Dokumente durchsuchen", documents.searchLabel)
        assertEquals("Dokumenttyp", documents.filtersLabel)
        assertEquals("Zuletzt hinzugefügt", documents.recentLabel)
        assertEquals("Nach Gewerk", documents.byTradeLabel)
    }

    @Test
    fun documentsFiltersMapToKinds() = runTest {
        val documents = AppData.load(repository).documents
        assertEquals(listOf("Alle 26", "Angebote 7", "Verträge 3", "Rechnungen 4", "Pläne 12"), documents.filters.map { it.text })
        assertEquals(
            listOf(null, DocumentKind.Offer, DocumentKind.Contract, DocumentKind.Invoice, DocumentKind.Plan),
            documents.filters.map { it.kind },
        )
        assertEquals(0, documents.initialFilter)
    }

    @Test
    fun documentsFilterRows() = runTest {
        val documents = AppData.load(repository).documents
        assertEquals(5, documents.rows(0).size)
        assertEquals(listOf("doc-offer-hahn", "doc-offer-wagner"), documents.rows(1).map { it.id })
        assertEquals(listOf("doc-contract-electrical"), documents.rows(2).map { it.id })
        assertEquals(listOf("doc-invoice-windows-2"), documents.rows(3).map { it.id })
        assertEquals(listOf("doc-plan-floor-og"), documents.rows(4).map { it.id })
        assertEquals(5, documents.rows(5).size)
        assertEquals(5, documents.rows(-1).size)
    }

    @Test
    fun documentRowsCarryAndroidMetaAndTargets() = runTest {
        val rows = AppData.load(repository).documents.rows
        assertEquals("Estrich · 14.12.2026", rows[0].meta)
        assertEquals("Fenster & Türen · offen", rows[2].meta)
        assertEquals("18.400 €", rows[2].amount)
        assertNull(rows[3].amount)
        assertNull(rows[0].target)
        assertEquals(AppRoute.Document("doc-offer-wagner"), rows[1].target)
        assertEquals(DocumentKind.Invoice, rows[2].kind)
    }

    @Test
    fun filterMappingMismatchIsDecodingError() = runTest {
        val bad = edited { root ->
            root.editObject("documents") { docs ->
                docs.mapArray("categories") { i, c ->
                    if (i != 0) c else JsonObject(c + ("count" to JsonPrimitive("8")))
                }
            }
        }
        assertEquals("screens.Dokumente.android.filters", failure { AppData.load(bad) }.detail)
    }

    @Test
    fun documentsBannerFromFixture() = runTest {
        val banner = AppData.load(repository).documents.banner
        assertEquals("Rechnung prüfen & zahlen", banner.title)
        assertEquals("Fenster & Türen · fällig 22.12.", banner.sub)
        assertEquals("18.400 €", banner.amount)
    }

    @Test
    fun viewerLinesAndTotal() = runTest {
        val viewers = AppData.load(repository).viewers
        assertEquals(setOf("doc-offer-wagner"), viewers.keys)
        val viewer = viewers.getValue("doc-offer-wagner")
        assertEquals("Angebot Estrichbau Wagner", viewer.title)
        assertEquals("PDF · 2 Seiten · 10.12.2026", viewer.subtitle)
        assertEquals("Vorschau Seite 1 von 2", viewer.pageLabel)
        assertEquals("Estrichbau Wagner GmbH", viewer.issuerName)
        assertEquals("Industriestraße 4 · 68519 Viernheim", viewer.issuerAddress)
        assertEquals("Angebot Nr. 2026-118", viewer.number)
        assertEquals(listOf("1", "2", "3", "4", "5"), viewer.lines.map { it.pos })
        assertEquals("Zementestrich CT-C25-F4, 65 mm, 112 m²", viewer.lines[2].text)
        assertEquals("Gesamt brutto", viewer.totalLabel)
        assertEquals("9.800,00 €", viewer.total)
        assertEquals(listOf("Gewerk", "Typ", "Betrag"), viewer.facts.map { it.label })
        assertEquals("Im Vergleich ansehen", viewer.compareLabel)
        assertEquals(AppRoute.Compare("screed"), viewer.compareTarget)
        assertEquals("Teilen", viewer.shareLabel)
        assertEquals("Gewerk oder Typ ändern", viewer.changeLabel)
    }

    @Test
    fun viewerLinesSumToTotal() = runTest {
        val viewer = AppData.load(repository).viewers.getValue("doc-offer-wagner")
        assertEquals(980000L, viewer.lines.sumOf { cents(it.sum) })
        assertEquals(980000L, cents(viewer.total))
    }

    @Test
    fun notificationGroupsAndUnread() = runTest {
        val notifications = AppData.load(repository).notifications
        assertEquals("Mitteilungen", notifications.title)
        assertEquals("Alle gelesen", notifications.markAllLabel)
        assertEquals(listOf("Heute", "Diese Woche"), notifications.groups.map { it.title })
        assertEquals(listOf(true, true), notifications.groups[0].items.map { it.unread })
        assertEquals(listOf(false, false, false), notifications.groups[1].items.map { it.unread })
        assertEquals(2, notifications.unreadCount)
        assertEquals(
            listOf(NotificationKind.Frist, NotificationKind.Geld),
            notifications.groups[0].items.map { it.kind },
        )
        assertEquals("08:10", notifications.groups[0].items[0].`when`)
    }

    @Test
    fun notificationTargets() = runTest {
        val items = AppData.load(repository).notifications.groups.flatMap { it.items }
        assertEquals(
            listOf(
                NavigationTarget.Defects,
                NavigationTarget.Tab(AppTab.Documents),
                NavigationTarget.Push(AppRoute.Compare("screed")),
                NavigationTarget.Push(AppRoute.Trade("screed")),
                null,
            ),
            items.map { it.target },
        )
    }

    @Test
    fun unknownNotificationTargetIsDecodingError() = runTest {
        val bad = edited { root ->
            root.mapArray("notifications") { _, group ->
                group.mapArray("items") { _, item -> JsonObject(item + ("target" to JsonPrimitive("elsewhere/1"))) }
            }
        }
        assertEquals("notifications.items.target", failure { AppData.load(bad) }.detail)
    }

    @Test
    fun homeBadgeEqualsUnreadNotificationCount() = runTest {
        val data = AppData.load(repository)
        assertEquals("2", data.home.badge)
        assertEquals(data.notifications.unreadCount.toString(), data.home.badge)
        assertEquals(data.notifications.unreadCount, repository.project().unreadNotifications)
    }

    // --- M4: inspections (step 4) ---

    private val checksPath = "inspections.byTrade.windows.checks"

    /** The fixture with [edit] applied to `inspections.byTrade.windows.checks.<key>`. */
    private fun editCheck(key: String, edit: (JsonObject) -> JsonObject): MockRepository = edited { root ->
        root.editObject("inspections") { inspections ->
            inspections.editObject("byTrade") { byTrade ->
                byTrade.editObject("windows") { windows ->
                    windows.editObject("checks") { checks -> checks.editObject(key, edit) }
                }
            }
        }
    }

    private fun JsonObject.with(key: String, value: kotlinx.serialization.json.JsonElement) = JsonObject(this + (key to value))

    private fun platform(ios: String?, android: String?) = JsonObject(
        mapOf(
            "ios" to (ios?.let { JsonPrimitive(it) } ?: JsonNull),
            "android" to (android?.let { JsonPrimitive(it) } ?: JsonNull),
        ),
    )

    @Test
    fun loadsInspectionsOverview() = runTest {
        val inspections = AppData.load(repository).inspections
        assertEquals("Abnahmen", inspections.title)
        assertEquals("Vorlagen", inspections.templatesLabel)
        assertEquals(listOf("Gewerke", "Offene Mängel (3)"), inspections.views)
        assertEquals("Ansicht", inspections.viewsLabel)
        val rows = inspections.rows
        assertEquals(
            listOf("earthworks", "shell", "carpentry", "roofing", "windows", "electrical", "plumbing", "screed", "tiles", "painting"),
            rows.map { it.tradeId },
        )
        assertEquals(AppRoute.Inspection("windows"), rows[4].target)
        assertEquals(AppRoute.Inspection("earthworks"), rows[0].target)
        assertNull(rows[3].target)
        assertEquals(InspectionStatus.Reserved, rows[1].status)
        assertEquals("Vorbehalt", rows[1].statusLabel)
        assertEquals("9 Prüfschritte · 14.04.", rows[0].subtitle)
        assertEquals("6 Schritte je Element · 7 / 12", rows[4].subtitle)
        assertEquals("Heute", rows[4].statusLabel)
        assertEquals(InspectionStatus.Planned, rows[9].status)
    }

    @Test
    fun loadsDefectsBoard() = runTest {
        val board = AppData.load(repository).defectsBoard
        assertEquals("Abnahmen", board.title)
        assertEquals(listOf("Gewerke", "Offene Mängel (3)"), board.views)
        assertEquals(listOf("defect-shell-crack", "defect-roof-verge", "defect-window-f2"), board.cards.map { it.id })
        assertEquals(listOf(DefectUrgency.Late, DefectUrgency.Soon, DefectUrgency.Later), board.cards.map { it.urgency })
        assertEquals(listOf(true, false, false), board.cards.map { it.showsActions })
        assertEquals("ROHBAU", board.cards[0].trade)
        assertEquals("3 Tage überfällig", board.cards[0].dueLabel)
        assertEquals("Frist 15.01.2027 · 2 Fotos", board.cards[2].meta)
        assertEquals("Erinnern", board.remindLabel)
        assertEquals("Behoben prüfen", board.resolveLabel)
        assertEquals("Behobene Mängel", board.resolvedLabel)
        assertEquals("3", board.resolvedCount)
    }

    @Test
    fun resolvedDefectsComeFromTheRepository() = runTest {
        assertEquals("3", repository.resolvedDefects().count)
    }

    @Test
    fun loadsElements() = runTest {
        val elements = AppData.load(repository).inspectionElements
        assertEquals(setOf("windows"), elements.keys)
        val model = elements.getValue("windows")
        assertEquals("windows", model.tradeId)
        assertEquals("Fenster & Türen", model.title)
        assertEquals("Fensterbau Weber GmbH · 18.12.2026", model.subtitle)
        assertEquals("7 von 12 geprüft", model.counter)
        assertEquals("6 Schritte je Element", model.guide)
        assertEquals("Prüfanleitung", model.helpLabel)
        assertEquals(12, model.progress.size)
        assertEquals(listOf("Alle 12", "Offen 5", "Mängel 1"), model.chips)
        assertEquals(listOf("F1", "F2", "F3", "F7", "F8", "F9"), model.floors.flatMap { it.items }.map { it.id })
        assertEquals("Alle 6", model.floors[0].moreLabel)
        val items = model.floors.flatMap { it.items }.associateBy { it.id }
        assertEquals(AppRoute.NewDefect, items.getValue("F2").target)
        assertEquals(ElementStatus.Defect, items.getValue("F2").status)
        assertNull(items.getValue("F1").target)
        val step = AppRoute.InspectionStep("windows", "F8", 3)
        assertEquals(step, items.getValue("F8").target)
        assertEquals(ElementStatus.Now, items.getValue("F8").status)
        assertEquals(step, model.nextTarget)
        assertEquals("Weiter: F8 · Schlafen", model.nextLabel)
    }

    @Test
    fun loadsStepModel() = runTest {
        val steps = AppData.load(repository).inspectionSteps
        assertEquals(setOf<AppRoute>(AppRoute.InspectionStep("windows", "F8", 3)), steps.keys)
        val step = steps.getValue(AppRoute.InspectionStep("windows", "F8", 3))
        assertEquals("3 / 6", step.counter)
        assertEquals("F8 · Schlafen", step.title)
        assertEquals("OG · Dreh-Kipp-Fenster, 1-flügelig", step.subtitle)
        assertEquals(2, step.done.size)
        assertEquals("In Ordnung · 1 Foto", step.done[0].meta)
        assertEquals(3, step.current.n)
        assertEquals("Foto oben", step.current.photoCaption)
        assertEquals("Foto seitlich", step.current.addPhotoLabel)
        assertEquals("So prüfen", step.guideLabel)
        assertEquals("Mangel", step.defectLabel)
        assertEquals("In Ordnung", step.okLabel)
        assertEquals(listOf("4", "5", "6"), step.next.map { it.n })
        assertEquals("Schließen & Verriegeln", step.next[0].label)
        assertEquals(AppRoute.NewDefect, step.defectTarget)
        assertEquals(0.42f, step.progress, 0.0001f)
    }

    @Test
    fun loadsDefectForm() = runTest {
        val form = AppData.load(repository).defectForm
        assertEquals("windows", form.tradeId)
        assertEquals("Mangel erfassen", form.title)
        assertEquals("Sichern", form.saveLabel)
        assertEquals("F2 · Wohnen · Schritt 1", form.context)
        assertEquals("Drehstellung: Flügel bleibt nicht stehen", form.finding)
        assertEquals("Beschreibung", form.descriptionLabel)
        assertEquals("Flügel fällt ab ca. 45° Öffnung selbstständig wieder zu.", form.description)
        assertEquals("Fotos (2)", form.photosLabel)
        assertEquals(listOf("Foto 1", "Foto 2"), form.photoLabels)
        assertEquals("Foto aufnehmen", form.addPhotoLabel)
        assertEquals("Schwere", form.severityLabel)
        assertEquals(listOf("Gering", "Mittel", "Erheblich"), form.severities)
        assertEquals(1, form.initialSeverity)
        assertEquals("✓ ", form.selectedPrefix)
        assertEquals("Behebung bis", form.deadlineLabel)
        assertEquals(listOf("+ 14 Tage", "+ 4 Wochen", "+ 6 Wochen"), form.deadlineOptions.map { it.label })
        assertEquals(listOf<Int?>(14, 28, 42), form.deadlineOptions.map { it.offsetDays })
        assertEquals(LocalDate.of(2026, 12, 18), form.deadlineBase)
        assertEquals(1, form.initialDeadline)
        assertEquals("01.01.2027", form.deadlineText(0))
        assertEquals("15.01.2027", form.deadlineText(1))
        assertEquals("29.01.2027", form.deadlineText(2))
        assertNull(form.deadlineText(3))
        assertEquals("Zuständig", form.responsibleLabel)
        assertEquals("Fensterbau Weber GmbH", form.responsible)
        assertEquals("Handwerker per E-Mail informieren", form.notifyLabel)
        assertTrue(form.notifyInitial)
    }

    @Test
    fun loadsSignOff() = runTest {
        val signOffs = AppData.load(repository).signOffs
        assertEquals(setOf("windows"), signOffs.keys)
        val sign = signOffs.getValue("windows")
        assertEquals("windows", sign.inspectionId)
        assertEquals("Abnahme abschließen", sign.title)
        assertEquals("Fenster & Türen · 18.12.2026", sign.subtitle)
        assertEquals(listOf("12", "11", "1"), sign.stats.map { it.value })
        assertEquals(listOf("geprüft", "in Ordnung", "Mangel"), sign.stats.map { it.label })
        assertEquals(listOf(CountTone.Surface, CountTone.Ok, CountTone.Error), sign.stats.map { it.tone })
        assertEquals("Vorbehaltene Mängel", sign.reservedHeading)
        assertEquals(listOf("F2 · Flügel fällt zu"), sign.defects.map { it.title })
        assertEquals("bis 15.01.", sign.defects[0].due)
        assertEquals("Mittel · 2 Fotos", sign.defects[0].meta)
        assertEquals("Die Abnahme erfolgt unter Vorbehalt der aufgeführten Mängel und Fristen.", sign.reservation)
        assertEquals("Handwerker", sign.craftsmanLabel)
        assertEquals("Fensterbau Weber", sign.craftsmanName)
        assertEquals("Unterschrieben 14:32", sign.craftsmanStatus)
        assertEquals("Bauherr · Max Mustermann", sign.ownerLabel)
        assertEquals("Zum Unterschreiben tippen", sign.signHereLabel)
        assertEquals("Unterschrift Handwerker", sign.signatureLabel)
        assertEquals("Protokoll abschließen", sign.finishLabel)
        assertEquals("PDF mit Fotos geht an Sie und den Handwerker", sign.note)
        assertEquals(NavigationTarget.Defects, sign.finishTarget)
    }

    @Test
    fun loadsAllM4ScreenTexts() = runTest {
        for (name in listOf("AbnGewerke", "AbnElemente", "AbnPruefung", "AbnMangel", "AbnSignatur", "AbnMaengel")) {
            val broken = edited { root ->
                root.editObject("screens") { screens ->
                    JsonObject(screens + (name to JsonObject(screens.getValue(name).jsonObject - "android")))
                }
            }
            val error = runCatching { AppData.load(broken) }.exceptionOrNull()
            assertTrue(name, error is RepositoryException.FixtureMissing)
            assertEquals("screens.$name.android", (error as RepositoryException.FixtureMissing).path)
        }
        for (name in listOf("AbnGewerke", "AbnMaengel")) {
            val strict = edited { root ->
                root.editObject("screens") { screens ->
                    screens.editObject(name) { screen ->
                        screen.editObject("android") { it.with("extra", JsonPrimitive("x")) }
                    }
                }
            }
            failure { AppData.load(strict) }
        }
        val tabless = edited { root ->
            root.editObject("screens") { screens ->
                screens.editObject("AbnGewerke") { screen -> screen.editObject("android") { JsonObject(it - "tabs") } }
            }
        }
        failure { AppData.load(tabless) }
        assertNotNull(AppData.load(repository))
    }

    @Test
    fun loadsAllM4Routes() = runTest {
        val data = AppData.load(repository)
        val routes = data.inspections.rows.mapNotNull { it.target } +
            data.inspectionElements.getValue("windows").floors.flatMap { it.items }.mapNotNull { it.target }
        assertEquals(
            listOf<AppRoute>(
                AppRoute.Inspection("earthworks"),
                AppRoute.Inspection("shell"),
                AppRoute.Inspection("carpentry"),
                AppRoute.Inspection("windows"),
                AppRoute.Inspection("electrical"),
                AppRoute.Inspection("plumbing"),
                AppRoute.Inspection("screed"),
                AppRoute.Inspection("tiles"),
                AppRoute.Inspection("painting"),
                AppRoute.NewDefect,
                AppRoute.InspectionStep("windows", "F8", 3),
            ),
            routes,
        )
    }

    @Test
    fun windowFiguresAreConsistent() = runTest {
        val data = AppData.load(repository)
        val elements = data.inspectionElements.getValue("windows")
        val sign = data.signOffs.getValue("windows")
        val defect = repository.defects().single { it.tradeId == "windows" }
        val total = elements.progress.size
        val defects = elements.progress.count { it == ElementStatus.Defect }
        assertEquals(total.toString(), sign.stats[0].value)
        assertEquals(defects.toString(), sign.stats[2].value)
        assertEquals((total - defects).toString(), sign.stats[1].value)
        val defectItems = elements.floors.flatMap { it.items }.filter { it.status == ElementStatus.Defect }
        assertEquals(listOf("F2"), defectItems.map { it.id })
        assertTrue(data.defectForm.context.startsWith("F2"))
        assertEquals(defect.title, sign.defects.single().title)
        assertEquals("${defect.severity} · ${defect.photos} Fotos", sign.defects.single().meta)
        val (year, month, day) = defect.deadline.split("-")
        assertEquals("bis $day.$month.", sign.defects.single().due)
        assertEquals("$day.$month.$year", data.defectForm.deadlineText(data.defectForm.initialDeadline))
        assertEquals(data.defectsBoard.cards.size, 3)
        assertEquals("Offene Mängel (${data.defectsBoard.cards.size})", data.defectsBoard.views[1])
        assertEquals("Offene Mängel (${data.defectsBoard.cards.size})", data.inspections.views[1])
    }

    @Test
    fun missingAndroidAllLabelFailsLoad() = runTest {
        val bad = editCheck("elements") { it.with("allLabel", platform("Alle 12", null)) }
        assertEquals("$checksPath.elements.allLabel", failure { AppData.load(bad) }.detail)
    }

    @Test
    fun missingAndroidGuideFailsLoad() = runTest {
        val bad = editCheck("elements") { it.with("guide", platform("x", null)) }
        assertEquals("$checksPath.elements.guide", failure { AppData.load(bad) }.detail)
    }

    @Test
    fun unknownElementStatusFailsLoad() = runTest {
        val bad = editCheck("elements") { elements ->
            elements.with("progress", JsonArray(elements.getValue("progress").jsonArray.map { JsonPrimitive("maybe") }))
        }
        assertEquals("$checksPath.elements.progress", failure { AppData.load(bad) }.detail)
        val badItem = editCheck("elements") { elements ->
            elements.mapArray("floors") { _, floor ->
                floor.mapArray("items") { _, item -> item.with("status", JsonPrimitive("maybe")) }
            }
        }
        assertEquals("$checksPath.elements.floors.items.status", failure { AppData.load(badItem) }.detail)
    }

    @Test
    fun twoNowElementsFailLoad() = runTest {
        val bad = editCheck("elements") { elements ->
            elements.mapArray("floors") { _, floor ->
                floor.mapArray("items") { _, item ->
                    if (item.getValue("id").toString() == "\"F9\"") item.with("status", JsonPrimitive("now")) else item
                }
            }
        }
        assertEquals("$checksPath.elements.floors", failure { AppData.load(bad) }.detail)
    }

    @Test
    fun stepNumberMismatchFailsLoad() = runTest {
        val bad = editCheck("stepF8") { step ->
            step.editObject("current") { it.with("n", JsonPrimitive("4")) }
        }
        assertEquals("$checksPath.stepF8", failure { AppData.load(bad) }.detail)
        val badTitle = editCheck("stepF8") { it.with("title", JsonPrimitive("F9 · Schlafen")) }
        assertEquals("$checksPath.stepF8", failure { AppData.load(badTitle) }.detail)
    }

    @Test
    fun severityNotInOptionsFailsLoad() = runTest {
        val bad = editCheck("defectF2") { form ->
            form.editObject("severity") { it.with("selected", JsonPrimitive("Kritisch")) }
        }
        assertEquals("$checksPath.defectF2.severity", failure { AppData.load(bad) }.detail)
    }

    @Test
    fun deadlineMismatchFailsLoad() = runTest {
        val bad = editCheck("defectF2") { it.with("deadlineLabel", platform("Sa, 16.01.2027", "16.01.2027")) }
        assertEquals("$checksPath.defectF2.deadlineLabel", failure { AppData.load(bad) }.detail)
    }

    @Test
    fun malformedOverviewRouteFailsLoad() = runTest {
        val bad = edited { root ->
            root.editObject("inspections") { inspections ->
                inspections.editObject("overview") { overview ->
                    overview.mapArray("rows") { i, row -> if (i == 0) row.with("route", JsonPrimitive("inspection/")) else row }
                }
            }
        }
        assertEquals("inspections.overview.rows.route", failure { AppData.load(bad) }.detail)
    }

    @Test
    fun statsCountMismatchFailsLoad() = runTest {
        val bad = editCheck("signature") { sign ->
            sign.with("stats", JsonArray(sign.getValue("stats").jsonArray.take(2)))
        }
        assertEquals("$checksPath.signature.stats", failure { AppData.load(bad) }.detail)
    }

    @Test
    fun inspectionDecodingErrorIsRepositoryException() = runTest {
        val bad = editCheck("elements") { it.with("title", JsonPrimitive(7)) }
        assertEquals("$checksPath.elements", failure { AppData.load(bad) }.detail)
        val unknownKey = editCheck("stepF8") { it.with("extra", JsonPrimitive("x")) }
        assertEquals("$checksPath.stepF8", failure { AppData.load(unknownKey) }.detail)
        val missingKey = editCheck("signature") { JsonObject(it - "kind") }
        assertEquals("$checksPath.signature", failure { AppData.load(missingKey) }.detail)
        val badOverview = edited { root ->
            root.editObject("inspections") { inspections ->
                inspections.editObject("overview") { it.with("rows", JsonPrimitive("none")) }
            }
        }
        assertEquals("inspections.overview", failure { AppData.load(badOverview) }.detail)
    }
}
