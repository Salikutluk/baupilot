package de.baupilot.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BaupilotNavigationBar
import de.baupilot.designsystem.components.Tab
import de.baupilot.screens.home.HomeAction
import de.baupilot.screens.ScreenCopy
import de.baupilot.screens.home.HomeScreen
import de.baupilot.screens.placeholder.PlaceholderPresentation
import de.baupilot.screens.placeholder.PlaceholderScreen
import de.baupilot.screens.profile.ProfileScreen
import de.baupilot.screens.schedule.ScheduleAction
import de.baupilot.screens.schedule.ScheduleScreen

private fun AppTab.designSystemTab(): Tab = when (this) {
    AppTab.Home -> Tab.Overview
    AppTab.Schedule -> Tab.Schedule
    AppTab.Inspections -> Tab.Acceptance
    AppTab.Documents -> Tab.Documents
    AppTab.Profile -> Tab.Profile
}

private fun Tab.appTab(): AppTab = when (this) {
    Tab.Overview -> AppTab.Home
    Tab.Schedule -> AppTab.Schedule
    Tab.Acceptance -> AppTab.Inspections
    Tab.Documents -> AppTab.Documents
    Tab.Profile -> AppTab.Profile
}

/**
 * Logged-in shell (D4, D5): composes only the selected tab's top route, keeps each tab's saved state in a
 * [androidx.compose.runtime.saveable.SaveableStateHolder], and pins the navigation bar at the bottom while the
 * selected tab is at its root.
 */
@Composable
fun AppShell(
    state: AppNavigationState,
    onStateChange: (AppNavigationState) -> Unit,
    data: AppData,
) {
    val holder = rememberSaveableStateHolder()
    val tab = state.selectedTab
    Box(Modifier.fillMaxSize().background(Tokens.bg.color())) {
        holder.SaveableStateProvider(key = tab.route) {
            TabContent(
                tab = tab,
                route = state.path(tab).lastOrNull(),
                state = state,
                onStateChange = onStateChange,
                data = data,
            )
        }
        if (state.showsTabBar) {
            Box(Modifier.align(Alignment.BottomCenter)) {
                BaupilotNavigationBar(
                    selected = tab.designSystemTab(),
                    onSelect = { onStateChange(state.select(it.appTab())) },
                )
            }
        }
    }
}

@Composable
private fun TabContent(
    tab: AppTab,
    route: AppRoute?,
    state: AppNavigationState,
    onStateChange: (AppNavigationState) -> Unit,
    data: AppData,
) {
    if (route != null) {
        val title = when (route) {
            is AppRoute.Trade -> data.tradeNames[route.id] ?: route.id
            AppRoute.Notifications -> ScreenCopy.NOTIFICATIONS_TITLE
        }
        val tag = when (route) {
            is AppRoute.Trade -> "screen.trade.${route.id}"
            AppRoute.Notifications -> "screen.notifications"
        }
        PlaceholderScreen(
            title = title,
            presentation = PlaceholderPresentation.Pushed(onBack = { onStateChange(state.pop()) }),
            modifier = Modifier.testTag(tag),
        )
        return
    }
    when (tab) {
        AppTab.Home -> HomeScreen(
            model = data.home,
            onAction = { action ->
                onStateChange(
                    when (action) {
                        HomeAction.OpenNotifications -> state.push(AppRoute.Notifications)
                        is HomeAction.OpenTrade -> state.push(AppRoute.Trade(action.id))
                        HomeAction.OpenDocumentsTab -> state.select(AppTab.Documents)
                    },
                )
            },
        )

        AppTab.Schedule -> ScheduleScreen(
            model = data.schedule,
            onAction = { action ->
                when (action) {
                    is ScheduleAction.OpenTrade -> onStateChange(state.push(AppRoute.Trade(action.id)))
                }
            },
        )

        AppTab.Inspections -> PlaceholderScreen(
            title = Tab.Acceptance.label,
            presentation = PlaceholderPresentation.TabRoot,
            modifier = Modifier.testTag("screen.inspections"),
        )

        AppTab.Documents -> PlaceholderScreen(
            title = Tab.Documents.label,
            presentation = PlaceholderPresentation.TabRoot,
            modifier = Modifier.testTag("screen.documents"),
        )

        AppTab.Profile -> ProfileScreen(model = data.profile)
    }
}
