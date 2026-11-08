package de.baupilot.app

import de.baupilot.data.MockRepository
import de.baupilot.data.RepositoryException
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.components.StatusChipColors
import java.io.File
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
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
}
