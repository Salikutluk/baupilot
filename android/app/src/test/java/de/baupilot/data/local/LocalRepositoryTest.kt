package de.baupilot.data.local

import androidx.room.Room
import de.baupilot.app.AppData
import de.baupilot.capture.FileStore
import de.baupilot.data.MockRepository
import de.baupilot.data.RepositoryException
import de.baupilot.sync.StampClock
import java.io.File
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LocalRepositoryTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val fixture: String =
        File(System.getProperty("baupilot.repoRoot"), "fixtures/baupilot.json").readText()
    private val mock = MockRepository(fixture)
    private lateinit var db: BaupilotDatabase

    @Before
    fun openDatabase() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), BaupilotDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    private fun repository(json: () -> String = { fixture }): LocalRepository {
        val clock = Clock.systemUTC()
        return LocalRepository(db, json, FileStore(tmp.newFolder()), StampClock(clock, "device") { 0L }, clock) {}
    }

    private suspend fun seeded(): LocalRepository = repository().also { it.ensureSeeded() }

    private suspend inline fun <reified E : Throwable> assertFailsWith(block: suspend () -> Unit) {
        val failure = runCatching { block() }.exceptionOrNull()
        assertTrue("expected ${E::class.simpleName} but got $failure", failure is E)
    }

    private fun root(): JsonObject = Json.parseToJsonElement(fixture).jsonObject

    private fun withRoot(change: (MutableMap<String, JsonElement>) -> Unit): String {
        val map = root().toMutableMap()
        change(map)
        return JsonObject(map).toString()
    }

    private fun renameTrade(trades: JsonElement, index: Int, name: String): JsonArray = JsonArray(
        trades.jsonArray.mapIndexed { i, trade ->
            if (i == index) JsonObject(trade.jsonObject + ("name" to JsonPrimitive(name))) else trade
        },
    )

    @Test
    fun everyReadEqualsMockRepository() = runTest {
        val local = seeded()
        assertEquals(mock.now(), local.now())
        assertEquals(mock.project(), local.project())
        assertEquals(mock.trades(), local.trades())
        assertEquals(mock.schedule(), local.schedule())
        assertEquals(mock.offers(null), local.offers(null))
        assertEquals(mock.offers("windows"), local.offers("windows"))
        for (trade in mock.trades()) {
            assertEquals(mock.comparison(trade.id), local.comparison(trade.id))
            assertEquals(mock.offers(trade.id), local.offers(trade.id))
        }
        assertEquals(mock.documents(), local.documents())
        assertEquals(mock.defects(), local.defects())
        assertEquals(mock.resolvedDefects(), local.resolvedDefects())
        assertEquals(mock.notifications(), local.notifications())
        assertEquals(mock.inspections(), local.inspections())
        assertEquals(mock.measurements(), local.measurements())
        assertEquals(mock.screen("Login"), local.screen("Login"))
        assertNotNull(local.screen("Login"))
        assertNull(local.screen("DoesNotExist"))
        assertNull(local.comparison("no-such-trade"))
        assertEquals(0, local.skippedRecords)
    }

    @Test
    fun appDataEqualsMockAppData() = runTest {
        // StatusChipColors.Ok/Warn are plain classes (identity equality), so compare the rendered models
        // with the object identity hash stripped; every other value is compared by content.
        fun AppData.rendered() = toString().replace(Regex("@[0-9a-f]+"), "@")
        assertEquals(AppData.load(mock).rendered(), AppData.load(seeded()).rendered())
    }

    @Test
    fun nowIsMetaNow() = runTest {
        assertEquals(LocalDate.of(2026, 12, 18), repository().now())
    }

    @Test
    fun readsBeforeSeedingReportNoProject() = runTest {
        assertFailsWith<RepositoryException.NoProject> { repository().project() }
    }

    @Test
    fun seedIsIdempotent() = runTest {
        val local = repository()
        local.ensureSeeded()
        val first = db.records().list("trades", "musterweg-12")
        val count = db.records().projectIds()
        local.ensureSeeded()
        assertEquals(first, db.records().list("trades", "musterweg-12"))
        assertEquals(count, db.records().projectIds())
        assertEquals(listOf("musterweg-12"), count)
        assertEquals(mock.trades(), local.trades())
        assertTrue(first.all { it.stamps == "{}" && !it.deleted && it.projectId == "musterweg-12" })
    }

    @Test
    fun reseedReplacesOnlyUntouchedRecords() = runTest {
        var json = fixture
        val local = repository { json }
        local.ensureSeeded()
        val touched = db.records().get("trades", mock.trades()[1].id)!!
        db.records().upsert(
            touched.copy(
                fields = JsonObject(Converters.stringToFields(touched.fields) + ("name" to JsonPrimitive("Lokal"))).toString(),
                stamps = """{"name":{"at":5,"by":"device"}}""",
            ),
        )
        val untouchedId = mock.trades()[2].id

        json = withRoot { it["trades"] = renameTrade(it.getValue("trades"), 0, "Neu0").let { t -> renameTrade(t, 1, "Neu1") } }
        local.ensureSeeded()

        val trades = local.trades()
        assertEquals("Neu0", trades.first { it.id == mock.trades()[0].id }.name)
        assertEquals("Lokal", trades.first { it.id == mock.trades()[1].id }.name)
        assertEquals(mock.trades()[2], trades.first { it.id == untouchedId })
    }

    @Test
    fun reseedInsertsMissingRecords() = runTest {
        var json = fixture
        val local = repository { json }
        local.ensureSeeded()
        val gone = mock.trades()[3]
        db.records().deleteProject("musterweg-12")
        json = withRoot { it["trades"] = renameTrade(it.getValue("trades"), 3, "Neu3") }
        local.ensureSeeded()
        assertEquals(gone.copy(name = "Neu3"), local.trades()[3])
    }

    @Test
    fun orderFollowsFixture() = runTest {
        val local = seeded()
        assertEquals(mock.trades().map { it.id }, local.trades().map { it.id })
        assertEquals(mock.defects().map { it.id }, local.defects().map { it.id })
        assertEquals(mock.documents().items.map { it.id }, local.documents().items.map { it.id })
        assertEquals(
            mock.notifications().map { g -> g.title to g.items.map { it.id } },
            local.notifications().map { g -> g.title to g.items.map { it.id } },
        )
        val template = db.records().get("defects", mock.defects()[0].id)!!
        for (id in listOf("tie-b", "tie-a")) {
            db.records().upsert(
                template.copy(
                    id = id,
                    sortKey = 100,
                    fields = JsonObject(Converters.stringToFields(template.fields) + ("id" to JsonPrimitive(id))).toString(),
                ),
            )
        }
        assertEquals(mock.defects().map { it.id } + listOf("tie-a", "tie-b"), local.defects().map { it.id })
    }

    @Test
    fun deletedRecordsAreHiddenAndNeverCounted() = runTest {
        val local = seeded()
        val victim = db.records().get("defects", mock.defects()[0].id)!!
        db.records().upsert(victim.copy(deleted = true))
        assertEquals(mock.defects().drop(1), local.defects())
        assertEquals(0, local.skippedRecords)
    }

    @Test
    fun storageFailureMapsToRepositoryException() = runTest {
        val local = seeded()
        db.close()
        assertFailsWith<RepositoryException.Storage> { local.trades() }
        assertFailsWith<RepositoryException.Storage> { local.ensureSeeded() }
    }

    @Test
    fun undecodableRecordIsSkippedAndAppDataStillLoads() = runTest {
        val local = seeded()
        val template = db.records().get("defects", mock.defects()[0].id)!!
        val fields = Converters.stringToFields(template.fields)
        val missingTitle = JsonObject((fields - "title") + ("id" to JsonPrimitive("bad-missing")))
        val wrongType = JsonObject(fields + ("id" to JsonPrimitive("bad-type")) + ("photos" to JsonPrimitive("viele")))
        val extra = JsonObject(fields + ("id" to JsonPrimitive("with-extra")) + ("origin" to JsonPrimitive("capture")))
        for ((id, f) in listOf("bad-missing" to missingTitle, "bad-type" to wrongType, "with-extra" to extra)) {
            db.records().upsert(template.copy(id = id, sortKey = 50, fields = f.toString()))
        }
        val defects = local.defects()
        assertEquals(mock.defects().map { it.id } + "with-extra", defects.map { it.id })
        assertEquals(2, local.skippedRecords)
        AppData.load(local)
    }

    private suspend fun mutate(table: String, id: String, change: (MutableMap<String, JsonElement>) -> Unit) {
        val entity = db.records().get(table, id)!!
        val fields = Converters.stringToFields(entity.fields).toMutableMap()
        change(fields)
        db.records().upsert(entity.copy(fields = JsonObject(fields).toString()))
    }

    // I17 (M4): rows that pass strict decoding but would fail AppData.load are skipped and counted, never blank the app.

    @Test
    fun offerWithoutAndroidMetaIsSkippedAndItsComparisonDisappears() = runTest {
        val local = seeded()
        val comparison = mock.trades().firstNotNullOf { mock.comparison(it.id) }
        val victim = comparison.offerIds.first()
        mutate("offers", victim) { it["meta"] = JsonObject(mapOf("ios" to JsonPrimitive("x"), "android" to kotlinx.serialization.json.JsonNull)) }
        assertTrue(local.offers(null).none { it.id == victim })
        assertNull(local.comparison(comparison.tradeId))
        assertEquals(1, local.skippedRecords)
        AppData.load(local)
    }

    @Test
    fun documentOfUnknownKindIsSkipped() = runTest {
        val local = seeded()
        val victim = mock.documents().items.first().id
        mutate("documents", victim) { it["kind"] = JsonPrimitive("mystery") }
        assertTrue(local.documents().items.none { it.id == victim })
        assertEquals(1, local.skippedRecords)
        AppData.load(local)
    }

    @Test
    fun projectPhaseNamingAMissingTradeFallsBackToTheFixturePhase() = runTest {
        val local = seeded()
        mutate("projects", "musterweg-12") {
            it["phase"] = JsonObject(
                mapOf(
                    "current" to JsonPrimitive(1), "total" to JsonPrimitive(2), "tradeId" to JsonPrimitive("gone"),
                    "progressPercent" to JsonPrimitive(10),
                ),
            )
        }
        assertEquals(mock.project().phase, local.project().phase)
        AppData.load(local)
    }

    @Test
    fun malformedProjectRecordFallsBackToTheFixtureProject() = runTest {
        val local = seeded()
        mutate("projects", "musterweg-12") { it["phase"] = JsonPrimitive("kaputt") }
        assertEquals(mock.project(), local.project())
        assertEquals(1, local.skippedRecords)
        AppData.load(local)
    }

    private val project = "musterweg-12"

    /** The inspections record of the overview row of [tradeId]. */
    private suspend fun overviewRecord(tradeId: String): RecordEntity =
        db.records().list("inspections", project).first {
            Converters.stringToFields(it.fields)["tradeId"] == JsonPrimitive(tradeId)
        }

    private suspend fun mutateOverview(tradeId: String, change: (MutableMap<String, JsonElement>) -> Unit) =
        mutate("inspections", overviewRecord(tradeId).id, change)

    private suspend fun overviewStatus(local: LocalRepository, tradeId: String): String? =
        local.inspections().jsonObject.getValue("overview").jsonObject.getValue("rows").jsonArray
            .map { it.jsonObject }.firstOrNull { it["tradeId"] == JsonPrimitive(tradeId) }
            ?.get("status")?.let { (it as JsonPrimitive).content }

    @Test
    fun overviewRowOfAProtocolTradeWithoutABannerStatusFallsBackToTheFixtureRow() = runTest {
        val local = seeded()
        mutateOverview("earthworks") { it["status"] = JsonPrimitive("today") }
        mutateOverview("shell") { it["status"] = JsonPrimitive("planned") }
        assertEquals("accepted", overviewStatus(local, "earthworks"))
        assertEquals("reserved", overviewStatus(local, "shell"))
        AppData.load(local)
    }

    @Test
    fun tombstonedOverviewRowOfAProtocolTradeFallsBackToTheFixtureRow() = runTest {
        val local = seeded()
        db.records().upsert(overviewRecord("carpentry").copy(deleted = true))
        assertEquals("accepted", overviewStatus(local, "carpentry"))
        assertEquals(0, local.skippedRecords)
        AppData.load(local)
    }

    @Test
    fun overviewRowsThatNoScreenCanShowAreSkipped() = runTest {
        val local = seeded()
        val na = JsonObject(mapOf("ios" to JsonPrimitive("x"), "android" to kotlinx.serialization.json.JsonNull))
        mutateOverview("windows") { it["sub"] = na }
        mutateOverview("electrical") { it["status"] = JsonPrimitive("exotic") }
        mutateOverview("plumbing") { it["route"] = JsonPrimitive("kein/ziel") }
        assertEquals(null, overviewStatus(local, "windows"))
        assertEquals(null, overviewStatus(local, "electrical"))
        assertEquals(null, overviewStatus(local, "plumbing"))
        assertEquals(3, local.skippedRecords)
        AppData.load(local)
    }

    @Test
    fun defectsWithAnUnknownUrgencyOrNoAndroidTradeAreSkipped() = runTest {
        val local = seeded()
        val ids = mock.defects().map { it.id }
        mutate("defects", ids[0]) { it["urgency"] = JsonPrimitive("sofort") }
        mutate("defects", ids[1]) {
            it["trade"] = JsonObject(mapOf("ios" to JsonPrimitive("X"), "android" to kotlinx.serialization.json.JsonNull))
        }
        assertEquals(ids.drop(2), local.defects().map { it.id })
        assertEquals(2, local.skippedRecords)
        AppData.load(local)
    }

    @Test
    fun tradeDetailThatNoScreenCanLayOutIsSkipped() = runTest {
        val local = seeded()
        mutate("trades", "screed") { it.remove("statusLabel") }
        assertTrue(local.trades().none { it.id == "screed" })
        assertEquals(1, local.skippedRecords)
        AppData.load(local)
        mutate("trades", "screed") {
            it["statusLabel"] = JsonPrimitive("Angebote einholen")
            val detail = it.getValue("detail").jsonObject
            val facts = detail.getValue("facts").jsonArray
            it["detail"] = JsonObject(detail + ("facts" to JsonArray(facts + facts.first())))
        }
        assertTrue(local.trades().none { it.id == "screed" })
        AppData.load(local)
    }

    @Test
    fun theFixturePhaseTradeSurvivesItsRemoteDeletion() = runTest {
        val local = seeded()
        db.records().upsert(db.records().get("trades", "windows")!!.copy(deleted = true))
        val phase = local.project().phase
        assertTrue(local.trades().any { it.id == phase.tradeId })
        AppData.load(local)
    }

    @Test
    fun theFixturePhaseTradeSurvivesAnUndecodableRecord() = runTest {
        val local = seeded()
        mutate("trades", "windows") { it.remove("name") }
        assertTrue(local.trades().any { it.id == local.project().phase.tradeId })
        AppData.load(local)
    }

    @Test
    fun viewerThatNoScreenCanShowSkipsItsDocument() = runTest {
        val local = seeded()
        val victim = "doc-offer-wagner"
        mutate("documents", victim) {
            val viewer = it.getValue("viewer").jsonObject
            it["viewer"] = JsonObject(viewer + ("facts" to JsonArray(viewer.getValue("facts").jsonArray.take(2))))
        }
        assertTrue(local.documents().items.none { it.id == victim })
        AppData.load(local)
        mutate("documents", victim) {
            val viewer = it.getValue("viewer").jsonObject
            it["viewer"] = JsonObject(
                viewer + ("facts" to JsonArray(viewer.getValue("facts").jsonArray + viewer.getValue("facts").jsonArray)) +
                    ("subtitle" to JsonObject(mapOf("ios" to JsonPrimitive("x"), "android" to kotlinx.serialization.json.JsonNull))),
            )
        }
        assertTrue(local.documents().items.none { it.id == victim })
        AppData.load(local)
    }

    @Test
    fun documentWithoutAndroidMetaIsSkipped() = runTest {
        val local = seeded()
        val victim = mock.documents().items.first().id
        mutate("documents", victim) {
            it["meta"] = JsonObject(mapOf("ios" to JsonPrimitive("x"), "android" to kotlinx.serialization.json.JsonNull))
        }
        assertTrue(local.documents().items.none { it.id == victim })
        assertEquals(1, local.skippedRecords)
        AppData.load(local)
    }

    @Test
    fun notificationsWithAnUnknownKindOrAMalformedTargetAreSkipped() = runTest {
        val local = seeded()
        mutate("notifications", "notif-defect-overdue") { it["kind"] = JsonPrimitive("alarm") }
        mutate("notifications", "notif-invoice-due") { it["target"] = JsonPrimitive("nirgendwo/hin") }
        val ids = local.notifications().flatMap { group -> group.items.map { it.id } }
        assertTrue("notif-defect-overdue" !in ids && "notif-invoice-due" !in ids)
        assertEquals(2, local.skippedRecords)
        AppData.load(local)
    }

    @Test
    fun tombstonedComparisonOfferRemovesTheComparisonLikeASkippedOne() = runTest {
        val local = seeded()
        val comparison = mock.trades().firstNotNullOf { mock.comparison(it.id) }
        val victim = comparison.offerIds.first()
        db.records().upsert(db.records().get("offers", victim)!!.copy(deleted = true))
        assertNull(local.comparison(comparison.tradeId))
        assertEquals(0, local.skippedRecords)
        AppData.load(local)
    }

    @Test
    fun aSkippedRecordThatIsLaterTombstonedLeavesTheSkipSet() = runTest {
        val local = seeded()
        val victim = mock.defects().first().id
        mutate("defects", victim) { it["urgency"] = JsonPrimitive("sofort") }
        local.defects()
        assertEquals(1, local.skippedRecords)
        db.records().upsert(db.records().get("defects", victim)!!.copy(deleted = true))
        local.defects()
        assertEquals(0, local.skippedRecords)
    }

    @Test
    fun theProjectIdIsTheRecordIdWhateverTheSyncedColumnSays() = runTest {
        val local = seeded()
        mutate("projects", project) { it["id"] = JsonPrimitive("a/../b") }
        assertEquals(project, local.project().id)
        mutate("projects", project) { it["phase"] = JsonPrimitive("kaputt") }
        assertEquals(project, local.project().id)
    }

    @Test
    fun dataVersionFollowsTheSyncState() = runTest {
        val local = repository()
        assertEquals(0L, local.dataVersion.first())
        db.state().put(SyncStateEntity("dataVersion", "7"))
        assertEquals(7L, local.dataVersion.first())
    }

    @Test
    fun enqueueUpsertCollapsesOpenOpsAndKeepsOrder() = runTest {
        val outbox = db.outbox()
        outbox.enqueueUpsert("defects", "a", "p1", 1)
        outbox.enqueueUpsert("defects", "b", "p1", 2)
        outbox.enqueueUpsert("defects", "a", "p1", 3)
        assertEquals(listOf("b", "a"), outbox.all().map { it.rowId })
        assertEquals("b", outbox.head()!!.rowId)

        val head = outbox.head()!!
        outbox.park(head.seq)
        assertEquals("a", outbox.head()!!.rowId)
        outbox.enqueueUpsert("defects", "b", "p1", 4)
        assertEquals(listOf("a", "b"), outbox.all().map { it.rowId })

        val a = outbox.head()!!
        outbox.markFailed(a.seq, "boom")
        outbox.enqueueUpsert("defects", "a", "p1", 5)
        val all = outbox.all()
        assertEquals(listOf(OutboxState.FAILED, OutboxState.PENDING, OutboxState.PENDING), all.map { it.state })
        assertEquals("boom", all[0].lastError)
        assertEquals(1, all[0].attempts)
        assertEquals("{}", all[1].payload)

        outbox.park(all[1].seq)
        outbox.requeueParked()
        assertEquals(OutboxState.PENDING, outbox.all().first { it.seq == all[1].seq }.state)
        outbox.deleteBySeq(all[0].seq)
        outbox.deleteForProject("p1")
        assertTrue(outbox.all().isEmpty())
        assertNull(outbox.head())
    }

}
