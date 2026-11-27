package de.baupilot.app

/**
 * What the Mangel form hands out on "Sichern": the trade the finding belongs to and the form's local state, read at
 * the moment of the tap. The two indices point into [DefectFormModel.severities] and
 * [DefectFormModel.deadlineOptions].
 */
data class DefectFormValues(
    val tradeId: String,
    val description: String,
    val severityIndex: Int,
    val deadlineIndex: Int,
    val notify: Boolean,
)
