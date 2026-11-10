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

// --- M5: early trades and electrical. Strict decoding: every fixture key is declared, and every key that
// --- may be absent carries an explicit `= null` (a bare nullable property is still a required key).

@Serializable
data class ProtocolBannerDto(val title: String, val text: String, val documentLabel: String? = null)

@Serializable
data class ProtocolRowDto(val label: PlatformText, val meta: String, val route: String? = null)

@Serializable
data class ProtocolGroupDto(val title: String, val rows: List<ProtocolRowDto>)

/** Mirror of `inspections.byTrade.<earthworks|shell|carpentry>`; `checks` is decoded per check. */
@Serializable
data class TradeProtocolDto(
    val title: String,
    val subtitle: PlatformText,
    val banner: ProtocolBannerDto,
    val groups: List<ProtocolGroupDto>,
    val checks: JsonObject? = null,
)

@Serializable
data class PhaseDto(
    val title: String,
    val statusLabel: PlatformText? = null,
    val dateLabel: PlatformText? = null,
    val progressLabel: String? = null,
)

@Serializable
data class ElectricalRowDto(val name: String, val sub: String, val route: String? = null, val statusLabel: String)

@Serializable
data class ElectricalGroupDto(val title: PlatformText, val more: PlatformText? = null, val rows: List<ElectricalRowDto>)

/** Mirror of `inspections.byTrade.electrical`; `checks` is decoded per check. */
@Serializable
data class ElectricalOverviewDto(
    val title: String,
    val subtitle: PlatformText,
    val phases: List<PhaseDto>,
    val groups: List<ElectricalGroupDto>,
    val checks: JsonObject? = null,
)

/** Mirror of `inspections.byTrade.shell.checks.measure`. */
@Serializable
data class MeasureCheckDto(
    val screen: String,
    val kind: String,
    val measurementKey: String,
    val counter: PlatformText,
    val title: String,
)

@Serializable
data class MeasureParameterDto(val key: String, val default: Int, val min: Int, val max: Int, val unit: String)

@Serializable
data class SizeDto(val w: Int, val h: Int)

@Serializable
data class DimLabelsDto(val w: String, val h: String)

@Serializable
data class MeasureLabelsDto(val within: String, val outside: String)

/** Mirror of `measurements.shellOpening`. */
@Serializable
data class ShellOpeningDto(
    val parameter: MeasureParameterDto,
    val target: SizeDto,
    val actual: SizeDto,
    val stepperDelta: Int,
    val dimLabels: DimLabelsDto,
    val measured: List<String>,
    val labels: MeasureLabelsDto,
)

@Serializable
data class RoomCurrentDto(val n: String, val title: String, val text: String)

@Serializable
data class RoomNextDto(val n: String, val label: PlatformText)

@Serializable
data class SocketDto(val id: String, val label: String, val status: String, val aria: String)

/** Mirror of `inspections.byTrade.electrical.checks.kitchen`. */
@Serializable
data class RoomCheckDto(
    val screen: String,
    val kind: String,
    val counter: PlatformText,
    val title: String,
    val subtitle: PlatformText,
    val done: List<StepDone>,
    val current: RoomCurrentDto,
    val next: List<RoomNextDto>,
    val sockets: List<SocketDto>,
    val progress: PlatformText,
    val defectNote: String,
    val okNote: String,
)

@Serializable
data class PanelCurrentDto(
    val n: String,
    val title: String,
    val text: String,
    val steps: List<String>,
    val outcomes: List<String>,
)

@Serializable
data class PanelNextDto(val n: String, val label: String, val tag: String? = null)

@Serializable
data class FiDto(val name: String, val sub: String, val statusLabel: String)

/** Mirror of `inspections.byTrade.electrical.checks.panel`. */
@Serializable
data class PanelCheckDto(
    val screen: String,
    val kind: String,
    val counter: PlatformText,
    val title: String,
    val subtitle: String,
    val done: List<StepDone>,
    val current: PanelCurrentDto,
    val next: List<PanelNextDto>,
    val fis: List<FiDto>,
)

@Serializable
data class SmokeRoomDto(val name: String, val statusLabel: String)

@Serializable
data class SmokeStepDto(val label: String, val hint: String)

@Serializable
data class SmokeCurrentDto(val room: String, val title: String, val outcomes: List<String>)

/** Mirror of `inspections.byTrade.electrical.checks.smoke`. */
@Serializable
data class SmokeCheckDto(
    val screen: String,
    val kind: String,
    val counter: PlatformText,
    val title: String,
    val subtitle: String,
    val rooms: List<SmokeRoomDto>,
    val checks: List<SmokeStepDto>,
    val current: SmokeCurrentDto,
    val reminder: String,
    val battery: String,
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

/** `inspections.byTrade.<tradeId>`: the whole trade slice (its `checks` stay raw, decoded per check). */
suspend fun <T> BackendRepository.tradeSlice(tradeId: String, serializer: KSerializer<T>): T =
    inspections().decodeInspectionSlice(listOf("byTrade", tradeId), serializer)

/**
 * Decodes `measurements.<key>`. A missing member and a shape error are both a [RepositoryException.Decoding]
 * carrying exactly that path; this differs from [inspectionCheck] only in that a missing member is not
 * a [RepositoryException.FixtureMissing] (documented asymmetry, unchanged).
 */
suspend fun <T> BackendRepository.measurementSlice(key: String, serializer: KSerializer<T>): T {
    val field = "measurements.$key"
    val node = (measurements() as? JsonObject)?.get(key) ?: throw RepositoryException.Decoding(field)
    try {
        return FixtureJson.decodeFromJsonElement(serializer, node)
    } catch (e: SerializationException) {
        throw RepositoryException.Decoding(field)
    } catch (e: IllegalArgumentException) {
        throw RepositoryException.Decoding(field)
    }
}
