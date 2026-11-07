package de.baupilot.data

import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class MockRepositoryTest {
    private val fixture: String =
        File(System.getProperty("baupilot.repoRoot"), "fixtures/baupilot.json").readText()

    private val repository = MockRepository(fixture)

    @Test
    fun readsProjectAddress() = runTest {
        assertEquals("Musterweg 12", repository.project().address)
    }

    @Test
    fun documentCountsAll() = runTest {
        assertEquals(26, repository.documents().counts.all)
    }

    @Test
    fun nowIsTheMockToday() = runTest {
        assertEquals(LocalDate.of(2026, 12, 18), repository.now())
    }

    @Test
    fun defectTradeUsesAndroidText() = runTest {
        val defect = repository.defects().first { it.title == "Riss in Kellerwand Nord" }
        assertEquals("ROHBAU", defect.trade.value)
        assertEquals("Rohbau", defect.trade.ios)
    }

    @Test
    fun emptyObjectFailsWithDecoding() {
        assertThrows(RepositoryException.Decoding::class.java) { MockRepository("{}") }
    }

    @Test
    fun offersFilterByTrade() = runTest {
        val all = repository.offers(null)
        val screed = repository.offers("screed")
        assertEquals(true, screed.isNotEmpty())
        assertEquals(true, screed.all { it.tradeId == "screed" })
        assertEquals(true, screed.size <= all.size)
    }

    @Test
    fun comparisonAndScreenLookups() = runTest {
        assertNotNull(repository.comparison("screed"))
        assertNotNull(repository.screen("Home"))
        assertNull(repository.screen("DoesNotExist"))
    }

    @Test
    fun platformTextAcceptsStringAndObject() {
        val shared = Json.decodeFromString<PlatformText>("\"Estrich\"")
        assertEquals(PlatformText("Estrich", "Estrich"), shared)
        val split = Json.decodeFromString<PlatformText>("""{"ios":"a","android":"b"}""")
        assertEquals(PlatformText("a", "b"), split)
        assertEquals("b", split.value)
        val nullable = Json.decodeFromString<PlatformText>("""{"ios":"a","android":null}""")
        assertNull(nullable.value)
    }

    @Test
    fun platformTextRejectsOtherObjectShapes() {
        listOf(
            """{}""",
            """{"ios":"x"}""",
            """{"android":"y"}""",
            """{"ios":"x","andriod":"y"}""",
            """{"ios":"x","android":"y","web":"z"}""",
            """{"ios":1,"android":"y"}""",
        ).forEach { json ->
            assertThrows(json, SerializationException::class.java) {
                Json.decodeFromString<PlatformText>(json)
            }
        }
    }

    @Test
    fun platformTextRejectsNumber() {
        assertThrows(SerializationException::class.java) {
            Json.decodeFromString<PlatformText>("42")
        }
    }
}
