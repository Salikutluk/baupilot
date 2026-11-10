package de.baupilot.app

import java.time.LocalDate
import de.baupilot.data.RepositoryException
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.inspection.DeadlineMath
import de.baupilot.inspection.DeadlineStyle
import de.baupilot.inspection.MeasureDimension
import de.baupilot.inspection.MeasureViewModel
import java.util.Locale

/** Status of an overview row; the fixture key is `key`. */
enum class InspectionStatus(val key: String) {
    Accepted("accepted"),
    Reserved("reserved"),
    Today("today"),
    Planned("planned"),
    ;

    companion object {
        fun of(key: String, field: String): InspectionStatus =
            entries.firstOrNull { it.key == key } ?: throw RepositoryException.Decoding(field)
    }
}

data class InspectionTradeRow(
    val tradeId: String,
    val name: String,
    val subtitle: String,
    val status: InspectionStatus,
    val statusLabel: String,
    val target: AppRoute?,
)

/** Header of the Abnahmen root and its Gewerke view. */
data class InspectionsModel(
    val title: String,
    val templatesLabel: String,
    val views: List<String>,
    val viewsLabel: String,
    val rows: List<InspectionTradeRow>,
)

enum class DefectUrgency(val key: String) {
    Late("late"),
    Soon("soon"),
    Later("later"),
    ;

    companion object {
        fun of(key: String, field: String): DefectUrgency =
            entries.firstOrNull { it.key == key } ?: throw RepositoryException.Decoding(field)
    }
}

/** `showsActions` is true for urgency `late` only (the late card carries the two buttons). */
data class DefectCard(
    val id: String,
    val trade: String,
    val title: String,
    val dueLabel: String,
    val meta: String,
    val urgency: DefectUrgency,
    val showsActions: Boolean,
)

/** The Mängel view of the Abnahmen root. */
data class DefectsBoardModel(
    val title: String,
    val templatesLabel: String,
    val views: List<String>,
    val viewsLabel: String,
    val cards: List<DefectCard>,
    val remindLabel: String,
    val resolveLabel: String,
    val resolvedLabel: String,
    val resolvedCount: String,
)

enum class ElementStatus(val key: String) {
    Ok("ok"),
    Defect("defect"),
    Now("now"),
    Open("open"),
    ;

    companion object {
        fun of(key: String, field: String): ElementStatus =
            entries.firstOrNull { it.key == key } ?: throw RepositoryException.Decoding(field)
    }
}

data class ElementRow(
    val id: String,
    val room: String,
    val type: String,
    val label: String,
    val status: ElementStatus,
    val target: AppRoute?,
)

data class FloorSection(val title: String, val moreLabel: String, val items: List<ElementRow>)

/** `progress` holds ok / defect / open only; `chips` is [all, open, defect] (AndAbnElemente.dc.html:44-49). */
data class ElementsModel(
    val tradeId: String,
    val title: String,
    val subtitle: String,
    val counter: String,
    val guide: String,
    val helpLabel: String,
    val progress: List<ElementStatus>,
    val chips: List<String>,
    val floors: List<FloorSection>,
    val nextLabel: String,
    val nextTarget: AppRoute,
)

data class StepDoneRow(val label: String, val meta: String)

data class StepCurrent(val n: Int, val title: String, val text: String, val photoCaption: String, val addPhotoLabel: String)

data class StepNextRow(val n: String, val label: String)

data class StepModel(
    val route: AppRoute.InspectionStep,
    val counter: String,
    val title: String,
    val subtitle: String,
    val done: List<StepDoneRow>,
    val current: StepCurrent,
    val guideLabel: String,
    val defectLabel: String,
    val okLabel: String,
    val next: List<StepNextRow>,
    val defectTarget: AppRoute,
    val progress: Float,
)

data class DeadlineOption(val label: String, val offsetDays: Int?)

data class DefectFormModel(
    val tradeId: String,
    val title: String,
    val saveLabel: String,
    val context: String,
    val finding: String,
    val descriptionLabel: String,
    val description: String,
    val photosLabel: String,
    val photoLabels: List<String>,
    val addPhotoLabel: String,
    val severityLabel: String,
    val severities: List<String>,
    val initialSeverity: Int,
    val selectedPrefix: String,
    val deadlineLabel: String,
    val deadlineOptions: List<DeadlineOption>,
    val deadlineBase: LocalDate,
    val initialDeadline: Int,
    val responsibleLabel: String,
    val responsible: String,
    val notifyLabel: String,
    val notifyInitial: Boolean,
) {
    /** The date field text for deadline chip [option]; null when the option has no offset or does not exist. */
    fun deadlineText(option: Int): String? {
        val offset = deadlineOptions.getOrNull(option)?.offsetDays ?: return null
        return DeadlineMath.label(deadlineBase, offset, DeadlineStyle.Android)
    }
}

enum class CountTone { Surface, Ok, Error }

data class SignOffStat(val value: String, val label: String, val tone: CountTone)

data class SignOffDefect(val title: String, val meta: String, val due: String)

data class SignOffModel(
    val inspectionId: String,
    val title: String,
    val subtitle: String,
    val stats: List<SignOffStat>,
    val reservedHeading: String,
    val defects: List<SignOffDefect>,
    val reservation: String,
    val craftsmanLabel: String,
    val craftsmanName: String,
    val craftsmanStatus: String,
    val ownerLabel: String,
    val signHereLabel: String,
    val signatureLabel: String,
    val finishLabel: String,
    val note: String,
    val finishTarget: NavigationTarget,
)

// --- M5: early trades and electrical ---

/** `defect` for a protocol row whose meta is the defect status label, else `ok` (D7). */
enum class RowMark { Ok, Defect }

/** `Ok` for an accepted trade, `Warn` for an accepted-with-reservation trade (D7). */
enum class BannerTone { Ok, Warn }

/** [documentLabel] is the button text of an accepted banner; [target] is the Mängel view of a reserved one. */
data class ProtocolBanner(
    val tone: BannerTone,
    val title: String,
    val text: String,
    val documentLabel: String?,
    val target: NavigationTarget?,
)

data class ProtocolRow(val label: String, val meta: String, val mark: RowMark, val target: NavigationTarget?)

data class ProtocolGroup(val title: String, val rows: List<ProtocolRow>)

/** Erdarbeiten, Rohbau and Zimmerer share one screen shape (D2). */
data class TradeProtocolModel(
    val tradeId: String,
    val title: String,
    val subtitle: String,
    val banner: ProtocolBanner,
    val groups: List<ProtocolGroup>,
)

/** Status of an electrical overview row (D7): the screen picks the colours. */
enum class ElectricalTone { Open, Running, Ok, Missing }

sealed interface PhaseKind {
    /** [date] is null on Android: the date is part of [status] there (D16). */
    data class Done(val status: String, val date: String?) : PhaseKind

    data class Active(val progressLabel: String, val fraction: Float) : PhaseKind
}

data class PhaseModel(val title: String, val kind: PhaseKind)

data class ElectricalRow(
    val name: String,
    val sub: String,
    val icon: BaupilotIcon,
    val statusLabel: String,
    val tone: ElectricalTone,
    val target: AppRoute?,
)

/** [moreLabel] is read on iOS only; it is always null on Android (D16). */
data class ElectricalGroup(val title: String, val moreLabel: String?, val rows: List<ElectricalRow>)

data class ElectricalOverviewModel(
    val tradeId: String,
    val title: String,
    val subtitle: String,
    val phases: List<PhaseModel>,
    val groups: List<ElectricalGroup>,
)

data class MeasureDimensionModel(val key: MeasureDimension, val label: String)

/** [initial] is the screen's starting state; the screen keeps the live value itself (D3, D12). */
data class MeasureCheckModel(
    val route: AppRoute.InspectionCheck,
    val counter: String,
    val title: String,
    val subtitleFormat: String,
    val instruction: String,
    val measuredHeading: String,
    val plumbTitle: String,
    val plumbSubtitle: String,
    val plumbValue: String,
    val saveLabel: String,
    val photoLabel: String,
    val defectLabel: String,
    val withinLabel: String,
    val outsideLabel: String,
    val dimensions: List<MeasureDimensionModel>,
    val measured: List<String>,
    val initial: MeasureViewModel,
    val defectTarget: AppRoute,
) {
    /** The header line with the tolerance in mm. */
    fun subtitle(tolerance: Int): String = String.format(Locale.ROOT, subtitleFormat, tolerance)
}

enum class SocketStatus(val key: String) {
    Ok("ok"),
    Defect("defect"),
    Now("now"),
    Open("open"),
    ;

    companion object {
        fun of(key: String, field: String): SocketStatus =
            entries.firstOrNull { it.key == key } ?: throw RepositoryException.Decoding(field)
    }
}

data class SocketTile(val id: String, val label: String, val status: SocketStatus, val accessibilityLabel: String)

data class RoomCurrent(val n: Int, val title: String, val text: String)

data class RoomCheckModel(
    val route: AppRoute.InspectionRoom,
    val counter: String,
    val title: String,
    val subtitle: String,
    val done: List<StepDoneRow>,
    val current: RoomCurrent,
    val sockets: List<SocketTile>,
    val progressLabel: String,
    val defectNote: String,
    val okNote: String,
    val defectLabel: String,
    val next: List<StepNextRow>,
    val progress: Float,
    val defectTarget: AppRoute,
)

/** Status of an FI row (D7). */
enum class FiTone { Ok, Open }

data class FiRow(val name: String, val sub: String, val statusLabel: String, val tone: FiTone)

data class PanelCurrent(val n: Int, val title: String, val text: String, val steps: List<String>)

data class PanelNextRow(val n: String, val label: String, val tag: String?)

data class PanelCheckModel(
    val route: AppRoute.InspectionCheck,
    val counter: String,
    val title: String,
    val callout: String,
    val done: List<StepDoneRow>,
    val current: PanelCurrent,
    val fis: List<FiRow>,
    val outcomes: List<String>,
    val next: List<PanelNextRow>,
    val progress: Float,
    val defectTarget: AppRoute,
)

/** Status of a smoke alarm room (D7). */
enum class SmokeRoomStatus { Ok, Now, Open }

data class SmokeCheckRow(val label: String, val hint: String, val done: Boolean)

data class SmokeRoom(val name: String, val statusLabel: String, val status: SmokeRoomStatus)

data class SmokeCheckModel(
    val route: AppRoute.InspectionCheck,
    val counter: String,
    val title: String,
    val subtitle: String,
    val room: String,
    val nowLabel: String,
    val checks: List<SmokeCheckRow>,
    val outcomes: List<String>,
    val rooms: List<SmokeRoom>,
    val reminder: String,
    val battery: String,
    val reminderInitiallyOn: Boolean,
    val defectTarget: AppRoute,
)
