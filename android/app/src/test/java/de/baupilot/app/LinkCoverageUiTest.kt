package de.baupilot.app

import androidx.activity.ComponentActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import de.baupilot.snapshot.ScreenCatalog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Link coverage (plan 5.5, D5): every test starts on the source screen through [LinkHost], taps the link element and
 * asserts the target screen identifier. Helpers are copied per class, like the other UI-test classes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class LinkCoverageUiTest {
    private val host = LinkHost()

    @get:Rule
    val rule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = host.rule

    private fun startAt(screen: String) = host.start(ScreenCatalog.state(screen))

    private fun exists(tag: String): Boolean = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun awaitScreen(tag: String) {
        rule.waitUntil(timeoutMillis = 5_000) { exists(tag) }
        rule.onNodeWithTag(tag).assertExists()
    }

    private fun assertAbsent(tag: String) {
        rule.waitForIdle()
        assertEquals("$tag must not be in the composition", 0, rule.onAllNodesWithTag(tag).fetchSemanticsNodes().size)
    }

    private fun tap(tag: String) {
        awaitScreen(tag)
        rule.onNodeWithTag(tag).performClick()
        rule.waitForIdle()
    }

    private fun scrollAndTap(tag: String) {
        awaitScreen(tag)
        rule.onNodeWithTag(tag).performScrollTo().performClick()
        rule.waitForIdle()
    }

    private fun systemBack() {
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    private val tabScreens = mapOf(
        "tab.home" to "screen.home",
        "tab.schedule" to "screen.schedule",
        "tab.inspections" to "screen.inspections",
        "tab.documents" to "screen.documents",
        "tab.profile" to "screen.profile",
    )

    /**
     * Taps each tab of the screen under test and expects its screen; `tab.inspections` also lands on the trades view.
     * Every tab is tapped from a fresh start at [source]: the source screen is awaited before the tap, so a tab that
     * is only reachable from the screen just left cannot pass.
     */
    private fun tabsOpen(source: String, sourceTags: List<String>, tabs: List<String>) {
        tabs.forEach { tab ->
            startAt(source)
            sourceTags.forEach { awaitScreen(it) }
            tap(tab)
            awaitScreen(tabScreens.getValue(tab))
            if (tab == "tab.inspections") awaitScreen("inspections.view.trades")
            tabScreens.values.filter { it != tabScreens.getValue(tab) }.forEach { assertAbsent(it) }
        }
    }

    private fun backReturnsToInspections(source: String, sourceTag: String) {
        startAt(source)
        awaitScreen(sourceTag)
        tap("nav.back")
        awaitScreen("screen.inspections")
        awaitScreen("inspections.view.trades")
        assertAbsent(sourceTag)
    }

    /** 12 -> 11 and the other pushed screens: tap [leave], expect [target] and no [sourceTag]. */
    private fun leaveTo(source: String, sourceTag: String, leave: String, target: String) {
        startAt(source)
        awaitScreen(sourceTag)
        tap(leave)
        awaitScreen(target)
        assertAbsent(sourceTag)
    }

    private fun defectButtonOpensForm(source: String, sourceTag: String, button: String) {
        startAt(source)
        awaitScreen(sourceTag)
        tap(button)
        awaitScreen("screen.defect.new")
        assertAbsent(sourceTag)
    }

    @Test
    fun scheduleTabsOpenEveryTab() = tabsOpen("Zeitplan", listOf("screen.schedule"), listOf("tab.home", "tab.profile", "tab.documents"))

    @Test
    fun scheduleAskOpensScreedTrade() {
        startAt("Zeitplan")
        // fixtures/baupilot.json schedule.asks[0].target = "trade/screed"
        tap("schedule.ask.0")
        awaitScreen("screen.trade.screed")
        assertAbsent("screen.schedule")
    }

    @Test
    fun profileTabsOpenEveryTab() = tabsOpen("Profil", listOf("screen.profile"), listOf("tab.schedule", "tab.documents", "tab.inspections"))

    @Test
    fun documentsTabsOpenScheduleAndInspections() =
        tabsOpen("Dokumente", listOf("screen.documents"), listOf("tab.schedule", "tab.inspections"))

    @Test
    fun inspectionsTabsOpenHomeScheduleProfile() =
        tabsOpen("AbnGewerke", listOf("screen.inspections", "inspections.view.trades"), listOf("tab.home", "tab.schedule", "tab.profile"))

    @Test
    fun carpentryRowOpensZimmererAndBack() {
        startAt("AbnGewerke")
        awaitScreen("inspections.view.trades")
        tap("inspections.trade.carpentry")
        awaitScreen("screen.inspection.carpentry")
        assertAbsent("screen.inspections")
        tap("nav.back")
        awaitScreen("screen.inspections")
        awaitScreen("inspections.view.trades")
        assertAbsent("screen.inspection.carpentry")
    }

    @Test
    fun elementsBackReturnsToInspections() = backReturnsToInspections("AbnElemente", "screen.inspection.windows")

    @Test
    fun stepCloseReturnsToElements() =
        leaveTo("AbnPruefung", "screen.inspection.windows.F8.step.3", "nav.close", "screen.inspection.windows")

    @Test
    fun defectsViewTabsOpenEveryTab() {
        listOf("tab.home", "tab.schedule", "tab.profile", "tab.documents").forEach { tab ->
            startAt("AbnMaengel")
            awaitScreen("inspections.view.defects")
            tap(tab)
            awaitScreen(tabScreens.getValue(tab))
            tabScreens.values.filter { it != tabScreens.getValue(tab) }.forEach { assertAbsent(it) }
        }
    }

    @Test
    fun electricalBackReturnsToInspections() = backReturnsToInspections("ElUebersicht", "screen.inspection.electrical")

    @Test
    fun panelDefectOpensDefectForm() =
        defectButtonOpensForm("ElVerteiler", "screen.inspection.electrical.panel", "step.defect")

    @Test
    fun smokeDefectOpensDefectForm() =
        defectButtonOpensForm("ElRauchmelder", "screen.inspection.electrical.smoke", "step.defect")

    @Test
    fun screedBackReturnsToInspections() = backReturnsToInspections("EsUebersicht", "screen.inspection.screed")

    @Test
    fun flatnessDefectOpensDefectForm() =
        defectButtonOpensForm("EsEbenheit", "screen.inspection.screed.flatness", "flatness.defect")

    @Test
    fun paintingBackReturnsToInspections() = backReturnsToInspections("MaUebersicht", "screen.inspection.painting")

    @Test
    fun paintingRoomDefectOpensDefectForm() =
        defectButtonOpensForm("MaRaum", "screen.inspection.painting.room.living", "step.defect")

    @Test
    fun homeTradeRowsOpenTheirTargets() {
        // fixtures/baupilot.json home trades: electrical, plumbing, screed, each opens trade/<id>.
        startAt("Home")
        listOf("electrical", "plumbing", "screed").forEach { tradeId ->
            scrollAndTap("home.trade.$tradeId")
            awaitScreen("screen.trade.$tradeId")
            assertAbsent("screen.home")
            systemBack()
            awaitScreen("screen.home")
        }
    }

    @Test
    fun homeDocumentTilesOpenDocuments() {
        startAt("Home")
        (0..3).forEach { index ->
            scrollAndTap("home.doc.$index")
            awaitScreen("screen.documents")
            assertAbsent("screen.home")
            tap("tab.home")
            awaitScreen("screen.home")
        }
    }

    /**
     * Rows with a non-null route in `fixtures/baupilot.json` under `inspections.byTrade.<t>.groups[g].rows[r].route`
     * (screens 17, 20, 24, 28, 31, 33). A literal table: source screen, its tag, row tag, expected screen tag. Every
     * row starts on a fresh source screen.
     */
    @Test
    fun fixtureRowsOpenTheirRoutes() {
        listOf(
            // 17: shell groups[1].rows[0] = inspection/shell/measure, groups[0].rows[2] = defects
            listOf("Rohbau", "screen.inspection.shell", "protocol.row.1.0", "screen.inspection.shell.measure"),
            listOf("Rohbau", "screen.inspection.shell", "protocol.row.0.2", "inspections.view.defects"),
            // 20: electrical groups[0].rows[0..1], groups[1].rows[0]
            listOf("ElUebersicht", "screen.inspection.electrical", "electrical.row.0.0", "screen.inspection.electrical.panel"),
            listOf("ElUebersicht", "screen.inspection.electrical", "electrical.row.0.1", "screen.inspection.electrical.smoke"),
            listOf("ElUebersicht", "screen.inspection.electrical", "electrical.row.1.0", "screen.inspection.electrical.room.kitchen"),
            // 24: plumbing groups[0].rows[0..1], groups[2].rows[0]
            listOf("SanUebersicht", "screen.inspection.plumbing", "overview.row.0.0", "screen.inspection.plumbing.slope.A3"),
            listOf("SanUebersicht", "screen.inspection.plumbing", "overview.row.0.1", "screen.inspection.plumbing.pressure"),
            listOf("SanUebersicht", "screen.inspection.plumbing", "overview.row.2.0", "screen.inspection.plumbing.room.bathUpper"),
            // 28: screed groups[0].rows[2], groups[1].rows[0..1]
            listOf("EsUebersicht", "screen.inspection.screed", "overview.row.0.2", "screen.inspection.screed.flatness"),
            listOf("EsUebersicht", "screen.inspection.screed", "overview.row.1.0", "screen.inspection.screed.readiness"),
            listOf("EsUebersicht", "screen.inspection.screed", "overview.row.1.1", "screen.inspection.screed.readiness"),
            // 31: tiles groups[1].rows[0]
            listOf("FlUebersicht", "screen.inspection.tiles", "overview.row.1.0", "screen.inspection.tiles.room.bath"),
            // 33: painting groups[0].rows[0]
            listOf("MaUebersicht", "screen.inspection.painting", "overview.row.0.0", "screen.inspection.painting.room.living"),
        ).forEach { (source, sourceTag, row, target) ->
            startAt(source)
            awaitScreen(sourceTag)
            scrollAndTap(row)
            awaitScreen(target)
            // The defects row stays inside the Abnahmen screen (view switch), so that screen is present.
            if (target == "inspections.view.defects") awaitScreen("screen.inspections")
            assertAbsent(sourceTag)
        }
    }
}
