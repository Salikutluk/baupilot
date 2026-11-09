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
import de.baupilot.screens.compare.CompareAction
import de.baupilot.screens.compare.CompareScreen
import de.baupilot.screens.docviewer.DocViewerAction
import de.baupilot.screens.docviewer.DocViewerScreen
import de.baupilot.screens.defectform.DefectFormAction
import de.baupilot.screens.defectform.DefectFormScreen
import de.baupilot.screens.defects.DefectsAction
import de.baupilot.screens.defects.DefectsScreen
import de.baupilot.screens.documents.DocumentsScreen
import de.baupilot.screens.elements.ElementsAction
import de.baupilot.screens.elements.ElementsScreen
import de.baupilot.screens.gewerk.GewerkScreen
import de.baupilot.screens.home.HomeAction
import de.baupilot.screens.home.HomeScreen
import de.baupilot.screens.inspections.InspectionsAction
import de.baupilot.screens.inspections.InspectionsScreen
import de.baupilot.screens.inspectionstep.InspectionStepScreen
import de.baupilot.screens.inspectionstep.StepAction
import de.baupilot.screens.notifications.NotificationsAction
import de.baupilot.screens.notifications.NotificationsScreen
import de.baupilot.screens.placeholder.PlaceholderPresentation
import de.baupilot.screens.placeholder.PlaceholderScreen
import de.baupilot.screens.profile.ProfileScreen
import de.baupilot.screens.schedule.ScheduleAction
import de.baupilot.screens.schedule.ScheduleScreen
import de.baupilot.screens.signoff.SignOffAction
import de.baupilot.screens.signoff.SignOffScreen

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
 * Saveable-state key per route (D9): a parent's state survives a push and pop, and Profil keeps `profile/0/root`.
 * Entries of popped routes are not removed, so re-opening a route at the same depth restores its state.
 */
private fun providerKey(tab: AppTab, path: List<AppRoute>, view: InspectionsView): String {
    val top = when (val route = path.lastOrNull()) {
        null -> if (tab == AppTab.Inspections) "root/${view.name.lowercase()}" else "root"
        is AppRoute.Trade -> "trade:${route.id}"
        is AppRoute.Compare -> "compare:${route.tradeId}"
        is AppRoute.Document -> "document:${route.id}"
        AppRoute.Notifications -> "notifications"
        is AppRoute.Inspection -> "inspection:${route.tradeId}"
        is AppRoute.InspectionStep -> "step:${route.tradeId}/${route.elementId}/${route.step}"
        AppRoute.NewDefect -> "defect:new"
        is AppRoute.SignOff -> "sign:${route.inspectionId}"
    }
    return "${tab.route}/${path.size}/$top"
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
        val path = state.path(tab)
        holder.SaveableStateProvider(key = providerKey(tab, path, state.inspectionsView)) {
            TabContent(
                tab = tab,
                route = path.lastOrNull(),
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
    val onBack = { onStateChange(state.pop()) }
    if (route != null) {
        when (route) {
            is AppRoute.Trade -> {
                val model = data.trades[route.id]
                if (model != null) {
                    GewerkScreen(
                        model = model,
                        onBack = onBack,
                        onNavigate = { onStateChange(state.open(it)) },
                    )
                } else {
                    PushedPlaceholder(data.tradeNames[route.id] ?: route.id, "screen.trade.${route.id}", onBack)
                }
            }

            is AppRoute.Compare -> {
                val model = data.comparisons[route.tradeId]
                if (model != null) {
                    CompareScreen(
                        model = model,
                        onBack = onBack,
                        onAction = { action ->
                            when (action) {
                                is CompareAction.OpenDocument ->
                                    onStateChange(state.open(AppRoute.Document(action.id)))
                            }
                        },
                    )
                } else {
                    PushedPlaceholder(data.tradeNames[route.tradeId] ?: route.tradeId, "screen.compare.${route.tradeId}", onBack)
                }
            }

            is AppRoute.Document -> {
                val model = data.viewers[route.id]
                if (model != null) {
                    DocViewerScreen(
                        model = model,
                        onBack = onBack,
                        onAction = { action ->
                            when (action) {
                                is DocViewerAction.OpenCompare ->
                                    onStateChange(state.open(AppRoute.Compare(action.tradeId)))
                            }
                        },
                    )
                } else {
                    val title = data.documents.rows.firstOrNull { it.id == route.id }?.title ?: route.id
                    PushedPlaceholder(title, "screen.document.${route.id}", onBack)
                }
            }

            is AppRoute.Inspection -> {
                val model = data.inspectionElements[route.tradeId]
                if (model != null) {
                    ElementsScreen(
                        model = model,
                        onBack = onBack,
                        onAction = { action ->
                            when (action) {
                                is ElementsAction.Open -> onStateChange(state.open(action.route))
                            }
                        },
                    )
                } else {
                    val title = data.inspections.rows.firstOrNull { it.tradeId == route.tradeId }?.name ?: route.tradeId
                    PushedPlaceholder(title, "screen.inspection.${route.tradeId}", onBack)
                }
            }

            is AppRoute.InspectionStep -> {
                val model = data.inspectionSteps[route]
                if (model != null) {
                    InspectionStepScreen(
                        model = model,
                        onClose = onBack,
                        onAction = { action ->
                            when (action) {
                                is StepAction.Open -> onStateChange(state.open(action.route))
                            }
                        },
                    )
                } else {
                    PushedPlaceholder(
                        route.elementId,
                        "screen.inspection.${route.tradeId}.${route.elementId}.step.${route.step}",
                        onBack,
                    )
                }
            }

            AppRoute.NewDefect -> DefectFormScreen(
                model = data.defectForm,
                onAction = { action ->
                    onStateChange(
                        when (action) {
                            DefectFormAction.Cancel -> state.pop()
                            is DefectFormAction.Save -> {
                                val inspection = AppRoute.Inspection(action.tradeId)
                                if (data.signOffs.containsKey(action.tradeId)) {
                                    state.open(AppRoute.SignOff(action.tradeId), above = inspection)
                                } else {
                                    state.open(inspection)
                                }
                            }
                        },
                    )
                },
            )

            is AppRoute.SignOff -> {
                val model = data.signOffs[route.inspectionId]
                if (model != null) {
                    SignOffScreen(
                        model = model,
                        onBack = onBack,
                        onAction = { action ->
                            when (action) {
                                is SignOffAction.Finish -> onStateChange(state.go(action.target))
                            }
                        },
                    )
                } else {
                    PushedPlaceholder(route.inspectionId, "screen.inspection.${route.inspectionId}.sign", onBack)
                }
            }

            AppRoute.Notifications -> NotificationsScreen(
                model = data.notifications,
                onBack = onBack,
                onAction = { action ->
                    when (action) {
                        is NotificationsAction.Open -> onStateChange(state.go(action.target))
                    }
                },
            )
        }
        return
    }
    when (tab) {
        AppTab.Home -> HomeScreen(
            model = data.home,
            onAction = { action ->
                onStateChange(
                    when (action) {
                        HomeAction.OpenNotifications -> state.open(AppRoute.Notifications)
                        is HomeAction.OpenTrade -> state.open(AppRoute.Trade(action.id))
                        HomeAction.OpenDocumentsTab -> state.go(NavigationTarget.Tab(AppTab.Documents))
                    },
                )
            },
        )

        AppTab.Schedule -> ScheduleScreen(
            model = data.schedule,
            onAction = { action ->
                when (action) {
                    is ScheduleAction.OpenTrade -> onStateChange(state.open(AppRoute.Trade(action.id)))
                }
            },
        )

        AppTab.Inspections -> when (state.inspectionsView) {
            InspectionsView.Trades -> InspectionsScreen(
                model = data.inspections,
                modifier = Modifier.testTag("screen.inspections"),
                onAction = { action ->
                    onStateChange(
                        when (action) {
                            is InspectionsAction.SelectView -> state.selectInspectionsView(action.view)
                            is InspectionsAction.Open -> state.open(action.route)
                        },
                    )
                },
            )

            InspectionsView.Defects -> DefectsScreen(
                model = data.defectsBoard,
                modifier = Modifier.testTag("screen.inspections"),
                onAction = { action ->
                    when (action) {
                        is DefectsAction.SelectView -> onStateChange(state.selectInspectionsView(action.view))
                    }
                },
            )
        }

        AppTab.Documents -> DocumentsScreen(
            model = data.documents,
            onOpen = { onStateChange(state.open(it)) },
        )

        AppTab.Profile -> ProfileScreen(model = data.profile)
    }
}

@Composable
private fun PushedPlaceholder(title: String, tag: String, onBack: () -> Unit) {
    PlaceholderScreen(
        title = title,
        presentation = PlaceholderPresentation.Pushed(onBack = onBack),
        modifier = Modifier.testTag(tag),
    )
}
