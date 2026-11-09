package de.baupilot.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** One overview row of `inspections.overview.rows`. */
@Serializable
data class OverviewRow(
    val tradeId: String,
    val name: String,
    val sub: PlatformText,
    val status: String,
    val route: String?,
)

/** Mirror of `inspections.overview`. */
@Serializable
data class InspectionOverview(val rows: List<OverviewRow>, val statusLabels: Map<String, PlatformText>)

@Serializable
data class ElementSummary(val status: String, val label: PlatformText)

@Serializable
data class ElementItem(
    val id: String,
    val room: String,
    val type: String,
    val label: String,
    val status: String,
    val route: String?,
)

@Serializable
data class ElementFloor(val title: String, val more: String, val items: List<ElementItem>)

/** Mirror of `inspections.byTrade.windows.checks.elements`; every key is listed (strict decoding). */
@Serializable
data class ElementsCheck(
    val screen: String,
    val kind: String,
    val counter: PlatformText,
    val title: String,
    val subtitle: PlatformText,
    val progress: List<String>,
    val summary: List<ElementSummary>,
    val allLabel: PlatformText,
    val guide: PlatformText,
    val nextLabel: PlatformText,
    val floors: List<ElementFloor>,
)

@Serializable
data class StepDone(val label: String, val meta: PlatformText)

/** `n` is a JSON string in the fixture ("3"); the model parses it. */
@Serializable
data class StepCurrent(val n: String, val title: String, val text: String, val photos: List<String>)

@Serializable
data class StepNext(val n: String, val label: PlatformText)

/** Mirror of `inspections.byTrade.windows.checks.stepF8`. */
@Serializable
data class StepCheck(
    val screen: String,
    val kind: String,
    val counter: PlatformText,
    val title: String,
    val subtitle: PlatformText,
    val done: List<StepDone>,
    val current: StepCurrent,
    val next: List<StepNext>,
)

@Serializable
data class SeverityCheck(val options: List<String>, val selected: String)

@Serializable
data class NotifyCheck(val label: String, val enabled: Boolean)

/** Mirror of `inspections.byTrade.windows.checks.defectF2`. */
@Serializable
data class DefectFormCheck(
    val screen: String,
    val kind: String,
    val context: String,
    val title: String,
    val description: String,
    val photosLabel: String,
    val photos: List<String>,
    val severity: SeverityCheck,
    val deadlineLabel: PlatformText,
    val deadlineOptions: List<PlatformText>,
    val responsible: String,
    val notify: NotifyCheck,
)

@Serializable
data class SignatureStat(val n: String, val label: String)

@Serializable
data class SignatureDefect(val title: String, val meta: String, val due: String)

@Serializable
data class SignatureCraftsman(val name: String, val signed: String)

/** Mirror of `inspections.byTrade.windows.checks.signature`. */
@Serializable
data class SignatureCheck(
    val screen: String,
    val kind: String,
    val title: String,
    val subtitle: PlatformText,
    val stats: List<SignatureStat>,
    val defects: List<SignatureDefect>,
    val reservation: String,
    val craftsman: SignatureCraftsman,
    val owner: PlatformText,
    val note: String,
)

/**
 * Decodes the slice at [path] (member names below `inspections`) with the strict [FixtureJson] (D17).
 * A missing member or any shape error is a [RepositoryException.Decoding] carrying exactly the slice path,
 * never a raw `SerializationException` (I7).
 */
internal fun <T> JsonElement.decodeInspectionSlice(path: List<String>, serializer: KSerializer<T>): T {
    val field = (listOf("inspections") + path).joinToString(".")
    var node: JsonElement = this
    for (key in path) {
        node = (node as? JsonObject)?.get(key) ?: throw RepositoryException.Decoding(field)
    }
    try {
        return FixtureJson.decodeFromJsonElement(serializer, node)
    } catch (e: SerializationException) {
        throw RepositoryException.Decoding(field)
    } catch (e: IllegalArgumentException) {
        throw RepositoryException.Decoding(field)
    }
}

suspend fun BackendRepository.inspectionOverview(): InspectionOverview =
    inspections().decodeInspectionSlice(listOf("overview"), InspectionOverview.serializer())

/** `inspections.byTrade.<tradeId>.checks.<key>`. */
suspend fun <T> BackendRepository.inspectionCheck(tradeId: String, key: String, serializer: KSerializer<T>): T =
    inspections().decodeInspectionSlice(listOf("byTrade", tradeId, "checks", key), serializer)
