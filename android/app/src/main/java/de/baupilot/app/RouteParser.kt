package de.baupilot.app

import de.baupilot.data.RepositoryException

/** Parses fixture target strings into navigation targets; anything outside the grammar is a decoding error. */
object RouteParser {
    private val ID = Regex("^[A-Za-z0-9][A-Za-z0-9_-]*$")

    private val STEP_NUMBER = Regex("^[1-9][0-9]*$")

    private val tabs: Map<String, AppTab> = AppTab.entries.associateBy { it.route }

    fun target(raw: String?, field: String): NavigationTarget? {
        if (raw == null) return null
        if (raw == "defects") return NavigationTarget.Defects
        tabs[raw]?.let { return NavigationTarget.Tab(it) }
        if (raw == "notifications") return NavigationTarget.Push(AppRoute.Notifications)
        val parts = raw.split("/")
        val route = when {
            parts.size == 2 && parts[0] == "trade" && ID.matches(parts[1]) -> AppRoute.Trade(parts[1])
            parts.size == 3 && parts[0] == "trade" && parts[2] == "compare" && ID.matches(parts[1]) ->
                AppRoute.Compare(parts[1])
            parts.size == 2 && parts[0] == "document" && ID.matches(parts[1]) -> AppRoute.Document(parts[1])
            parts.size == 2 && parts[0] == "inspection" && ID.matches(parts[1]) -> AppRoute.Inspection(parts[1])
            parts.size == 3 && parts[0] == "inspection" && parts[2] == "sign" && ID.matches(parts[1]) ->
                AppRoute.SignOff(parts[1])
            parts.size == 5 && parts[0] == "inspection" && parts[3] == "step" && ID.matches(parts[1]) &&
                ID.matches(parts[2]) && STEP_NUMBER.matches(parts[4]) ->
                AppRoute.InspectionStep(parts[1], parts[2], parts[4].toIntOrNull() ?: throw RepositoryException.Decoding(field))
            raw == "defect/new" -> AppRoute.NewDefect
            else -> throw RepositoryException.Decoding(field)
        }
        return NavigationTarget.Push(route)
    }

    fun route(raw: String?, field: String): AppRoute? =
        when (val target = target(raw, field)) {
            null -> null
            is NavigationTarget.Push -> target.route
            is NavigationTarget.Tab, NavigationTarget.Defects -> throw RepositoryException.Decoding(field)
        }
}
