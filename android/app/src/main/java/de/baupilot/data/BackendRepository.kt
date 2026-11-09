package de.baupilot.data

import java.time.LocalDate
import kotlinx.serialization.json.JsonElement

interface BackendRepository {
    suspend fun now(): LocalDate
    suspend fun project(): Project
    suspend fun trades(): List<Trade>
    suspend fun schedule(): Schedule
    suspend fun offers(tradeId: String?): List<Offer>
    suspend fun comparison(tradeId: String): Comparison?
    suspend fun documents(): Documents
    suspend fun defects(): List<Defect>
    suspend fun resolvedDefects(): ResolvedDefects
    suspend fun notifications(): List<NotificationGroup>
    suspend fun inspections(): JsonElement
    suspend fun measurements(): JsonElement
    suspend fun screen(name: String): JsonElement?
}
