package de.baupilot.data

import android.content.Context
import java.io.FileNotFoundException
import java.time.LocalDate
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Strict on purpose: an unknown key in the fixture or a screen text is an error. */
internal val FixtureJson = Json { ignoreUnknownKeys = false }

sealed class RepositoryException(message: String) : Exception(message) {
    class FixtureMissing(val path: String) : RepositoryException("Fixture missing: $path")
    class Decoding(val detail: String) : RepositoryException("Fixture decoding failed: $detail")
}

class MockRepository(json: String) : BackendRepository {
    private val fixture: Fixture = try {
        FixtureJson.decodeFromString(Fixture.serializer(), json)
    } catch (e: SerializationException) {
        throw RepositoryException.Decoding(e.message ?: e.toString())
    } catch (e: IllegalArgumentException) {
        throw RepositoryException.Decoding(e.message ?: e.toString())
    }

    override suspend fun now(): LocalDate = LocalDate.of(2026, 12, 18)

    override suspend fun project(): Project = fixture.project

    override suspend fun trades(): List<Trade> = fixture.trades

    override suspend fun schedule(): Schedule = fixture.schedule

    override suspend fun offers(tradeId: String?): List<Offer> =
        if (tradeId == null) fixture.offers else fixture.offers.filter { it.tradeId == tradeId }

    override suspend fun comparison(tradeId: String): Comparison? =
        fixture.comparisons.firstOrNull { it.tradeId == tradeId }

    override suspend fun documents(): Documents = fixture.documents

    override suspend fun defects(): List<Defect> = fixture.defects

    override suspend fun notifications(): List<NotificationGroup> = fixture.notifications

    override suspend fun inspections(): JsonElement = fixture.inspections

    override suspend fun measurements(): JsonElement = fixture.measurements

    override suspend fun screen(name: String): JsonElement? =
        (fixture.screens as? JsonObject)?.get(name)

    companion object {
        private const val ASSET_PATH = "baupilot.json"

        fun fromAssets(context: Context): MockRepository {
            val text = try {
                context.assets.open(ASSET_PATH).bufferedReader(Charsets.UTF_8).use { it.readText() }
            } catch (e: FileNotFoundException) {
                throw RepositoryException.FixtureMissing(ASSET_PATH)
            }
            return MockRepository(text)
        }
    }
}
