package de.baupilot.app

import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.inspection.FlatnessViewModel
import de.baupilot.inspection.SlopeViewModel
import java.util.Locale

/** Status of an overview row of the four later trades (D7): the screen picks the colours. */
enum class OverviewTone { Ok, Open, Running, Today, Planned, Required, Defect }

/** Paint colours of the Maler screens; the screen layer maps them to the supplement tokens. */
enum class PaintColor { White, Sand, Sage }

/** What a row of an overview shows left of its text (D7). */
sealed interface OverviewLeading {
    data class Icon(val icon: BaupilotIcon) : OverviewLeading

    data class Swatch(val color: PaintColor) : OverviewLeading

    data object None : OverviewLeading
}

data class OverviewFact(val label: String, val value: String)

/** An overview has phase cards or a fact strip, never both (D7). */
sealed interface OverviewHeader {
    data class Phases(val phases: List<PhaseModel>) : OverviewHeader

    data class Facts(val facts: List<OverviewFact>) : OverviewHeader
}

data class TradeOverviewRow(
    val name: String,
    val sub: String,
    val statusLabel: String,
    val tone: OverviewTone,
    val leading: OverviewLeading,
    val target: AppRoute?,
)

data class TradeOverviewGroup(val title: String, val rows: List<TradeOverviewRow>)

data class TradeOverviewModel(
    val tradeId: String,
    val title: String,
    val subtitle: String,
    val header: OverviewHeader,
    val notice: String?,
    val groups: List<TradeOverviewGroup>,
)

/** Status of a drain slope section (D7). */
enum class SectionStatus { Ok, Defect, Now, Open }

data class SlopeSection(val id: String, val label: String, val status: SectionStatus)

/** [initial] is the screen's starting state; the screen keeps the live value itself. */
data class SlopeCheckModel(
    val route: AppRoute.InspectionCheckItem,
    val counter: String,
    val title: String,
    val subtitle: String,
    val hint: String,
    val warning: String,
    val sections: List<SlopeSection>,
    val initial: SlopeViewModel,
    val defectLabel: String,
    val lengthLabel: String,
    val dropLabel: String,
    val saveLabel: String,
    val photoLabel: String,
    val schemaLevelLabel: String,
    val schemaDeltaLabel: String,
    val defectTarget: AppRoute,
)

data class PressureCurrent(val n: Int, val title: String)

data class PressureNextRow(val n: String, val label: String, val tag: String?)

data class PressureTimer(
    val fraction: Float,
    val remaining: String,
    val accessibilityLabel: String,
    val remainingLabel: String,
    val note: String,
    val startLabel: String,
    val startValue: String,
    val endLabel: String,
    val endPlaceholder: String,
)

data class PressureCheckModel(
    val route: AppRoute.InspectionCheck,
    val counter: String,
    val title: String,
    val subtitle: String,
    val done: List<StepDoneRow>,
    val current: PressureCurrent,
    val timer: PressureTimer,
    val next: List<PressureNextRow>,
    val photoLabel: String,
    val progress: Float,
)

/** State of a bathroom object tab (D7). */
enum class ObjectState { Done, Now, Open }

data class ObjectTab(val name: String, val state: ObjectState)

data class ObjectCheckRow(val label: String, val hint: String, val ok: Boolean)

data class ObjectCheckModel(
    val route: AppRoute.InspectionRoom,
    val counter: String,
    val title: String,
    val objects: List<ObjectTab>,
    val heading: String,
    val checks: List<ObjectCheckRow>,
    val nextHint: String,
    val tabsLabel: String,
)

data class FlatnessCheckModel(
    val route: AppRoute.InspectionCheck,
    val counter: String,
    val title: String,
    val subtitleFormat: String,
    val instruction: String,
    val stepperLabelPrefix: String,
    val stepperAccessibilityNoun: String,
    val saveLabel: String,
    val photoLabel: String,
    val measuredFormat: String,
    val initial: FlatnessViewModel,
    val defectTarget: AppRoute,
) {
    /** The header line with the limit text ("4 mm"). */
    fun subtitle(limitText: String): String = String.format(Locale.ROOT, subtitleFormat, limitText)
}

/** Kind of a bar of the heating chart (D7). */
enum class HeatingBarKind { Recorded, Today, Planned }

data class HeatingBar(val height: Int, val kind: HeatingBarKind)

data class HeatingChart(
    val bars: List<HeatingBar>,
    val accessibilityLabel: String,
    val start: String,
    val max: String,
    val end: String,
    val today: String,
    val plan: String,
    val action: String,
)

data class CmRow(val label: String, val meta: String, val done: Boolean)

data class CmSection(val title: String, val tag: String, val text: String, val rows: List<CmRow>, val note: String)

data class ReadinessCheckModel(
    val route: AppRoute.InspectionCheck,
    val screenTitle: String,
    val cardTitle: String,
    val counter: String,
    val text: String,
    val chart: HeatingChart,
    val cm: CmSection,
)

data class TileModel(val id: String, val hollow: Boolean, val label: String?, val accessibilityLabel: String)

data class TileCurrent(val n: Int, val title: String, val text: String)

data class TileCheckModel(
    val route: AppRoute.InspectionRoom,
    val counter: String,
    val title: String,
    val subtitle: String,
    val done: List<String>,
    val current: TileCurrent,
    val next: List<StepNextRow>,
    val columns: Int,
    val tiles: List<TileModel>,
    val wallLabel: String,
    val markHint: String,
    val gridAccessibilityLabel: String,
    val result: String,
    val defectLabel: String,
    val noneLabel: String,
    val progress: Float,
    val defectTarget: AppRoute,
)

/** State of a wall tab (D7). */
enum class WallState { Done, Now, Open }

data class WallTab(val name: String, val state: WallState)

data class PaintCheckModel(
    val route: AppRoute.InspectionRoom,
    val counter: String,
    val title: String,
    val subtitle: String,
    val done: List<String>,
    val doneSwatch: PaintColor,
    val current: TileCurrent,
    val walls: List<WallTab>,
    val wallsLabel: String,
    val next: List<StepNextRow>,
    val defectLabel: String,
    val okLabel: String,
    val photoLabel: String,
    val progress: Float,
    val defectTarget: AppRoute,
)
