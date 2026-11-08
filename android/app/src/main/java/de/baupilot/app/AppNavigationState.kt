package de.baupilot.app

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

/** `route` equals the fixture `tabs[].route`. */
enum class AppTab(val route: String) {
    Home("home"),
    Schedule("schedule"),
    Inspections("inspections"),
    Documents("documents"),
    Profile("profile"),
}

sealed interface AppRoute {
    data class Trade(val id: String) : AppRoute
    data object Notifications : AppRoute
}

/**
 * Pure navigation model (D4): one stack per tab, refused navigation changes nothing.
 * Empty stacks are never stored, so equal states compare equal.
 */
data class AppNavigationState(
    val isLoggedIn: Boolean = false,
    val selectedTab: AppTab = AppTab.Home,
    val paths: Map<AppTab, List<AppRoute>> = emptyMap(),
) {
    fun path(tab: AppTab): List<AppRoute> = paths[tab].orEmpty()

    val canPop: Boolean get() = path(selectedTab).isNotEmpty()

    val showsTabBar: Boolean get() = !canPop

    fun logIn(): AppNavigationState = AppNavigationState(isLoggedIn = true)

    /** Another tab: switch only. The same tab: pop to its root. */
    fun select(tab: AppTab): AppNavigationState =
        if (tab == selectedTab) copy(paths = paths - tab) else copy(selectedTab = tab)

    fun push(route: AppRoute): AppNavigationState =
        if (!isLoggedIn) this else copy(paths = paths + (selectedTab to path(selectedTab) + route))

    fun pop(): AppNavigationState {
        if (!isLoggedIn || !canPop) return this
        val rest = path(selectedTab).dropLast(1)
        return copy(paths = if (rest.isEmpty()) paths - selectedTab else paths + (selectedTab to rest))
    }

    companion object {
        /** Survives configuration changes (D17); saved as primitives and lists only. */
        val Saver: Saver<AppNavigationState, Any> = listSaver<AppNavigationState, Any>(
            save = { state ->
                listOf(
                    state.isLoggedIn,
                    state.selectedTab.route,
                    AppTab.entries.map { tab -> state.path(tab).map(::encode) },
                )
            },
            // A malformed or older saved value restores nothing: the caller falls back to the initial state.
            restore = { saved ->
                runCatching {
                    val stacks = saved[2] as List<*>
                    AppNavigationState(
                        isLoggedIn = saved[0] as Boolean,
                        selectedTab = AppTab.entries.first { it.route == saved[1] },
                        paths = AppTab.entries.withIndex()
                            .associate { (i, tab) -> tab to (stacks[i] as List<*>).map { decode(it as String) } }
                            .filterValues { it.isNotEmpty() },
                    )
                }.getOrNull()
            },
        )

        private const val TRADE_PREFIX = "trade:"
        private const val NOTIFICATIONS = "notifications"

        private fun encode(route: AppRoute): String = when (route) {
            is AppRoute.Trade -> TRADE_PREFIX + route.id
            AppRoute.Notifications -> NOTIFICATIONS
        }

        private fun decode(text: String): AppRoute =
            if (text == NOTIFICATIONS) AppRoute.Notifications else AppRoute.Trade(text.removePrefix(TRADE_PREFIX))
    }
}
