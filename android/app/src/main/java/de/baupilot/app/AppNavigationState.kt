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
    data class Compare(val tradeId: String) : AppRoute
    data class Document(val id: String) : AppRoute
    data object Notifications : AppRoute
}

/** A navigation request: push a route on the selected tab, or switch tab (never touches a stack). */
sealed interface NavigationTarget {
    data class Push(val route: AppRoute) : NavigationTarget
    data class Tab(val tab: AppTab) : NavigationTarget
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

    /** Pops back to [route] when it is already in the selected tab's path, otherwise appends it (I3). */
    fun open(route: AppRoute): AppNavigationState {
        if (!isLoggedIn) return this
        val path = path(selectedTab)
        val index = path.indexOf(route)
        return if (index >= 0) copy(paths = paths + (selectedTab to path.take(index + 1))) else push(route)
    }

    fun go(target: NavigationTarget): AppNavigationState = when (target) {
        is NavigationTarget.Push -> open(target.route)
        is NavigationTarget.Tab -> select(target.tab)
    }

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
        private const val COMPARE_PREFIX = "compare:"
        private const val DOCUMENT_PREFIX = "document:"
        private const val NOTIFICATIONS = "notifications"

        private fun encode(route: AppRoute): String = when (route) {
            is AppRoute.Trade -> TRADE_PREFIX + route.id
            is AppRoute.Compare -> COMPARE_PREFIX + route.tradeId
            is AppRoute.Document -> DOCUMENT_PREFIX + route.id
            AppRoute.Notifications -> NOTIFICATIONS
        }

        /** Throws on an unknown string, so [Saver] restores nothing instead of guessing a route (I7). */
        private fun decode(text: String): AppRoute = when {
            text == NOTIFICATIONS -> AppRoute.Notifications
            text.startsWith(TRADE_PREFIX) -> AppRoute.Trade(text.removePrefix(TRADE_PREFIX))
            text.startsWith(COMPARE_PREFIX) -> AppRoute.Compare(text.removePrefix(COMPARE_PREFIX))
            text.startsWith(DOCUMENT_PREFIX) -> AppRoute.Document(text.removePrefix(DOCUMENT_PREFIX))
            else -> throw IllegalArgumentException("Unknown route: $text")
        }
    }
}
