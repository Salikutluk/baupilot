package de.baupilot.capture

import de.baupilot.app.AppRoute
import de.baupilot.protocol.ProtocolHash

/** Identity of an inspection step, derived from the route (D7), and the record id built from it. */
object StepKeys {
    /** `null` for routes that capture nothing. [part] names one input of the screen and is appended as `/part`. */
    fun of(route: AppRoute, part: String? = null): String? {
        val base = when (route) {
            is AppRoute.InspectionStep -> "${route.tradeId}/element/${route.elementId}/step/${route.step}"
            is AppRoute.InspectionCheck -> "${route.tradeId}/check/${route.check}"
            is AppRoute.InspectionRoom -> "${route.tradeId}/room/${route.roomId}"
            is AppRoute.InspectionCheckItem -> "${route.tradeId}/check/${route.check}/${route.item}"
            else -> return null
        }
        return if (part == null) base else "$base/$part"
    }

    /** `st_` + the first 32 hex characters of SHA-256 of `"<projectId>/<stepKey>"`: 35 characters, project scoped. */
    fun recordId(projectId: String, stepKey: String): String =
        "st_" + ProtocolHash.sha256Hex("$projectId/$stepKey".toByteArray(Charsets.UTF_8)).take(32)
}
