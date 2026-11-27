package de.baupilot.push

data class ReminderPayload(val title: String, val body: String, val target: String, val projectId: String?)

object ReminderPayloads {
    const val TARGET_DEFECTS = "defects"
    private val allowedTargets = setOf(TARGET_DEFECTS)

    /** Unknown data keys are ignored. A missing or non-allow-listed target becomes "defects". */
    fun parse(data: Map<String, String>, title: String?, body: String?): ReminderPayload? {
        val t = (title ?: data["title"])?.takeIf { it.isNotBlank() } ?: return null
        val b = (body ?: data["body"])?.takeIf { it.isNotBlank() } ?: return null
        val target = data["target"]?.takeIf { it in allowedTargets } ?: TARGET_DEFECTS
        return ReminderPayload(t, b, target, data["projectId"]?.takeIf { it.isNotBlank() })
    }
}
