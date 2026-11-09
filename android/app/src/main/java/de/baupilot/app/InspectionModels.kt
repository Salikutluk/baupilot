package de.baupilot.app

import java.time.LocalDate
import de.baupilot.data.RepositoryException
import de.baupilot.inspection.DeadlineMath
import de.baupilot.inspection.DeadlineStyle

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
