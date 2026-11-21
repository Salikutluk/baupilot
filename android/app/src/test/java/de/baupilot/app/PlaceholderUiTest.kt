package de.baupilot.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import de.baupilot.snapshot.ScreenCatalog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Placeholders stay visible and do nothing (plan 5.6, 6.2; invariant I4): every test starts on the source screen through
 * [LinkHost], taps each placeholder and asserts that the screen tags, the selected tab, the selected segment and the shown
 * segment view are unchanged. One method per screen; the Home "Alle" / "Dokument" placeholders are covered
 * by `AppNavigationUiTest.placeholderLinksHaveNoAction`. The window is tall (R9) so no tap needs a scroll.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h3000dp")
class PlaceholderUiTest {
    private val host = LinkHost()

    @get:Rule
    val rule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = host.rule

    private fun exists(tag: String): Boolean = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun awaitTag(tag: String) {
        rule.waitUntil(timeoutMillis = 5_000) { exists(tag) }
        rule.onNodeWithTag(tag).assertExists()
    }

    private fun screenTags(): Set<String> = rule
        .onAllNodes(SemanticsMatcher("screen tag") { it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("screen.") == true })
        .fetchSemanticsNodes()
        .mapNotNull { it.config.getOrNull(SemanticsProperties.TestTag) }
        .toSet()

    private fun tagsWithPrefix(prefix: String): Set<String> = rule
        .onAllNodes(SemanticsMatcher("tag $prefix") { it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(prefix) == true })
        .fetchSemanticsNodes()
        .mapNotNull { it.config.getOrNull(SemanticsProperties.TestTag) }
        .toSet()

    /** Whether the node tagged [tag] is selected; `null` when it is absent or carries no selected state. */
    private fun selectedOf(tag: String): Boolean? = rule.onAllNodesWithTag(tag).fetchSemanticsNodes()
        .firstOrNull()?.config?.getOrNull(SemanticsProperties.Selected)

    /**
     * Everything a placeholder tap must leave alone (G-8, I4): the `screen.*` tag set, the selected tab among `tab.*`
     * (the bar is shown on top-level screens only), and on AbnGewerke / AbnMängel the selected segment plus which of the
     * two segment views is shown.
     */
    private fun snapshot(): Map<String, Any?> {
        val state = mutableMapOf<String, Any?>("screens" to screenTags())
        tagsWithPrefix("tab.").sorted().forEach { state["selected $it"] = selectedOf(it) }
        listOf("trades", "defects").forEach { segment ->
            state["selected inspections.segment.$segment"] = selectedOf("inspections.segment.$segment")
            state["shown inspections.view.$segment"] = exists("inspections.view.$segment")
        }
        return state
    }

    /**
     * Starts at [screen], expects [current] as the only `screen.*` tag, taps each of [tags] and expects the whole
     * [snapshot] (screen, segment, view, tab) unchanged.
     */
    private fun noAction(screen: String, current: String, vararg tags: String) {
        host.start(ScreenCatalog.state(screen))
        awaitTag(current)
        assertEquals("screen tags before any tap", setOf(current), screenTags())
        val before = snapshot()
        tags.forEach { tag ->
            awaitTag(tag)
            rule.onNodeWithTag(tag).performClick()
            rule.waitForIdle()
            assertEquals("state after tapping $tag", before, snapshot())
        }
    }

    @Test
    fun placeholdersLoginNoAction() =
        noAction("Login", "screen.login", "login.forgot", "ph.Login.apple", "ph.Login.create")

    @Test
    fun placeholdersZeitplanNoAction() = noAction("Zeitplan", "screen.schedule", "schedule.ask.1")

    @Test
    fun placeholdersProfilNoAction() {
        // The settings rows are found placeholders too (not in plan 6.2): the tags carry the flat row index.
        host.start(ScreenCatalog.state("Profil"))
        awaitTag("screen.profile")
        val rows = rule
            .onAllNodes(SemanticsMatcher("profile row") { it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("ph.Profil.row.") == true })
            .fetchSemanticsNodes()
            .mapNotNull { it.config.getOrNull(SemanticsProperties.TestTag) }
        assertEquals(listOf("ph.Profil.row.0", "ph.Profil.row.1", "ph.Profil.row.5", "ph.Profil.row.6"), rows.sorted())
        noAction("Profil", "screen.profile", "ph.Profil.card", *rows.toTypedArray())
    }

    @Test
    fun placeholdersGewerkNoAction() =
        // The Hahn offer has no action (m3 D5); the more / attach buttons are found placeholders.
        noAction("Gewerk", "screen.trade.screed", "trade.offer.offer-hahn", "ph.Gewerk.more", "ph.Gewerk.add")

    @Test
    fun placeholdersDokumenteNoAction() = noAction(
        "Dokumente", "screen.documents",
        "ph.Dokumente.hint", "ph.Dokumente.bytrade", "ph.Dokumente.add",
        "documents.row.doc-offer-hahn", "documents.row.doc-invoice-windows-2",
        "documents.row.doc-contract-electrical", "documents.row.doc-plan-floor-og",
    )

    @Test
    fun placeholdersDokViewerNoAction() =
        noAction("DokViewer", "screen.document.doc-offer-wagner", "ph.DokViewer.change", "ph.DokViewer.share")

    @Test
    fun placeholdersVergleichNoAction() = noAction(
        "Vergleich", "screen.compare.screed",
        "compare.column.offer-hahn", "ph.Vergleich.remind", "ph.Vergleich.ask", "ph.Vergleich.award",
    )

    @Test
    fun placeholdersMitteilungenNoAction() =
        noAction("Mitteilungen", "screen.notifications", "notification.notif-electrical-confirmed", "ph.Mitteilungen.markall")

    @Test
    fun placeholdersAbnGewerkeNoAction() =
        noAction("AbnGewerke", "screen.inspections", "inspections.trade.roofing", "ph.AbnGewerke.vorlagen")

    @Test
    fun placeholdersAbnElementeNoAction() = noAction(
        "AbnElemente", "screen.inspection.windows",
        "ph.AbnElemente.guide", "ph.AbnElemente.more.0", "ph.AbnElemente.more.1",
        "ph.AbnElemente.chip.0", "ph.AbnElemente.chip.1", "ph.AbnElemente.chip.2",
        "inspection.element.F1", "inspection.element.F3", "inspection.element.F7", "inspection.element.F9",
    )

    @Test
    fun placeholdersAbnPruefungNoAction() =
        noAction("AbnPruefung", "screen.inspection.windows.F8.step.3", "ph.AbnPruefung.ok")

    @Test
    fun placeholdersAbnMaengelNoAction() = noAction(
        "AbnMaengel", "screen.inspections",
        "ph.AbnMaengel.vorlagen", "ph.AbnMaengel.remind.defect-shell-crack", "ph.AbnMaengel.resolve.defect-shell-crack", "ph.AbnMaengel.resolved",
    )

    @Test
    fun placeholdersErdNoAction() = noAction("Erd", "screen.inspection.earthworks", "ph.Erd.pdf")

    @Test
    fun placeholdersZimmererNoAction() = noAction("Zimmerer", "screen.inspection.carpentry", "ph.Zimmerer.pdf")

    @Test
    fun placeholdersRohbauNoAction() = noAction(
        "Rohbau", "screen.inspection.shell",
        "protocol.row.0.0", "protocol.row.0.1", "protocol.row.1.1", "protocol.row.1.2", "protocol.row.1.3",
        "protocol.row.2.0", "protocol.row.2.1",
    )

    @Test
    fun placeholdersRohbauMasseNoAction() =
        noAction("RohbauMasse", "screen.inspection.shell.measure", "measure.photo", "measure.save")

    @Test
    fun placeholdersElUebersichtNoAction() = noAction(
        "ElUebersicht", "screen.inspection.electrical",
        "electrical.row.1.1", "electrical.row.1.2", "electrical.row.2.0", "electrical.row.2.1",
    )

    @Test
    fun placeholdersElButtonsNoAction() {
        noAction("ElRaum", "screen.inspection.electrical.room.kitchen", "step.ok")
        noAction("ElVerteiler", "screen.inspection.electrical.panel", "step.ok")
        noAction("ElRauchmelder", "screen.inspection.electrical.smoke", "step.ok")
    }

    @Test
    fun placeholdersSanUebersichtNoAction() = noAction(
        "SanUebersicht", "screen.inspection.plumbing",
        "overview.row.0.2", "overview.row.1.0", "overview.row.1.1", "overview.row.2.1",
    )

    @Test
    fun placeholdersSanGefaelleNoAction() = noAction(
        "SanGefaelle", "screen.inspection.plumbing.slope.A3",
        "slope.photo", "slope.save", "slope.section.A1", "slope.section.A2", "slope.section.A3", "slope.section.A4",
    )

    @Test
    fun placeholdersSanDruckNoAction() = noAction("SanDruck", "screen.inspection.plumbing.pressure", "pressure.photo")

    @Test
    fun placeholdersSanBadNoAction() {
        host.start(ScreenCatalog.state("SanBad"))
        val current = "screen.inspection.plumbing.room.bathUpper"
        awaitTag(current)
        val tags = rule
            .onAllNodes(SemanticsMatcher("bath tab or mark") {
                val tag = it.config.getOrNull(SemanticsProperties.TestTag).orEmpty()
                tag.startsWith("object.tab.") || (tag.startsWith("object.check.") && (tag.endsWith(".ok") || tag.endsWith(".defect")))
            })
            .fetchSemanticsNodes()
            .mapNotNull { it.config.getOrNull(SemanticsProperties.TestTag) }
        assertEquals(true, tags.any { it.startsWith("object.tab.") })
        assertEquals(true, tags.any { it.startsWith("object.check.") })
        noAction("SanBad", current, *tags.toTypedArray())
    }

    @Test
    fun placeholdersEsUebersichtNoAction() =
        noAction("EsUebersicht", "screen.inspection.screed", "overview.row.0.0", "overview.row.0.1", "overview.row.0.3")

    @Test
    fun placeholdersEsEbenheitNoAction() = noAction("EsEbenheit", "screen.inspection.screed.flatness", "flatness.photo")

    @Test
    fun placeholdersEsBelegreifeNoAction() =
        noAction("EsBelegreife", "screen.inspection.screed.readiness", "readiness.action")

    @Test
    fun placeholdersFlUebersichtNoAction() = noAction(
        "FlUebersicht", "screen.inspection.tiles",
        "overview.row.0.0", "overview.row.0.1", "overview.row.1.1", "overview.row.1.2", "overview.row.1.3", "overview.row.2.0",
    )

    @Test
    fun placeholdersFlBadNoAction() = noAction("FlBad", "screen.inspection.tiles.room.bath", "tiles.none")

    @Test
    fun placeholdersMaUebersichtNoAction() = noAction(
        "MaUebersicht", "screen.inspection.painting",
        "overview.row.0.1", "overview.row.0.2", "overview.row.1.0", "overview.row.1.1", "overview.row.1.2",
    )

    @Test
    fun placeholdersMaRaumNoAction() = noAction(
        "MaRaum", "screen.inspection.painting.room.living",
        "paint.photo", "step.ok", "paint.wall.0", "paint.wall.1", "paint.wall.2", "paint.wall.3",
    )
}
