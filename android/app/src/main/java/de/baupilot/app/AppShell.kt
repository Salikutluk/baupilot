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
import de.baupilot.screens.earlytrades.CheckStepAction
import de.baupilot.screens.earlytrades.ElRaumScreen
import de.baupilot.screens.earlytrades.ElRauchmelderScreen
import de.baupilot.screens.earlytrades.ElUebersichtScreen
import de.baupilot.screens.earlytrades.ElVerteilerScreen
import de.baupilot.screens.earlytrades.ElectricalAction
import de.baupilot.screens.earlytrades.ErdScreen
import de.baupilot.screens.earlytrades.MeasureAction
import de.baupilot.screens.earlytrades.ProtocolAction
import de.baupilot.screens.earlytrades.RohbauMasseScreen
import de.baupilot.screens.earlytrades.RohbauScreen
import de.baupilot.screens.earlytrades.ZimmererScreen
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
import de.baupilot.screens.latertrades.EsBelegreifeScreen
import de.baupilot.screens.latertrades.EsEbenheitScreen
import de.baupilot.screens.latertrades.EsUebersichtScreen
import de.baupilot.screens.latertrades.FlBadScreen
import de.baupilot.screens.latertrades.FlUebersichtScreen
import de.baupilot.screens.latertrades.FlatnessAction
import de.baupilot.screens.latertrades.MaRaumScreen
import de.baupilot.screens.latertrades.MaUebersichtScreen
import de.baupilot.screens.latertrades.OverviewAction
import de.baupilot.screens.latertrades.SanBadScreen
import de.baupilot.screens.latertrades.SanDruckScreen
import de.baupilot.screens.latertrades.SanGefaelleScreen
import de.baupilot.screens.latertrades.SanUebersichtScreen
import de.baupilot.screens.latertrades.SlopeAction
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
        is AppRoute.InspectionCheck -> "check:${route.tradeId}/${route.check}"
        is AppRoute.InspectionRoom -> "room:${route.tradeId}/${route.roomId}"
        is AppRoute.InspectionCheckItem -> "item:${route.tradeId}/${route.check}/${route.item}"
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
                // D2 lookup chain: windows (M4), a trade protocol, the electrical overview, else the placeholder.
                val elements = data.inspectionElements[route.tradeId]
                val protocol = data.tradeProtocols[route.tradeId]
                val electrical = data.electricalOverviews[route.tradeId]
                val overview = data.tradeOverviews[route.tradeId]
                val onProtocol = { action: ProtocolAction ->
                    when (action) {
                        is ProtocolAction.Open -> onStateChange(state.go(action.target))
                    }
                }
                if (elements != null) {
                    ElementsScreen(
                        model = elements,
                        onBack = onBack,
                        onAction = { action ->
                            when (action) {
                                is ElementsAction.Open -> onStateChange(state.open(action.route))
                            }
                        },
                    )
                } else if (protocol != null) {
                    when (route.tradeId) {
                        "earthworks" -> ErdScreen(model = protocol, onBack = onBack, onAction = onProtocol)
                        "shell" -> RohbauScreen(model = protocol, onBack = onBack, onAction = onProtocol)
                        "carpentry" -> ZimmererScreen(model = protocol, onBack = onBack, onAction = onProtocol)
                        else -> InspectionPlaceholder(data, route.tradeId, onBack)
                    }
                } else if (electrical != null) {
                    ElUebersichtScreen(
                        model = electrical,
                        onBack = onBack,
                        onAction = { action ->
                            when (action) {
                                is ElectricalAction.Open -> onStateChange(state.open(action.route))
                            }
                        },
                    )
                } else if (overview != null) {
                    val onOverview = { action: OverviewAction ->
                        when (action) {
                            is OverviewAction.Open -> onStateChange(state.open(action.route))
                        }
                    }
                    when (route.tradeId) {
                        "plumbing" -> SanUebersichtScreen(model = overview, onBack = onBack, onAction = onOverview)
                        "screed" -> EsUebersichtScreen(model = overview, onBack = onBack, onAction = onOverview)
                        "tiles" -> FlUebersichtScreen(model = overview, onBack = onBack, onAction = onOverview)
                        "painting" -> MaUebersichtScreen(model = overview, onBack = onBack, onAction = onOverview)
                        else -> InspectionPlaceholder(data, route.tradeId, onBack)
                    }
                } else {
                    InspectionPlaceholder(data, route.tradeId, onBack)
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
                            is DefectFormAction.Save -> state.saveDefect(
                                formTradeId = action.tradeId,
                                hasSignOff = data.signOffs[action.tradeId] != null,
                            )
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

            is AppRoute.InspectionCheck -> {
                val measure = data.measureChecks[route]
                val panel = data.panelChecks[route]
                val smoke = data.smokeChecks[route]
                val pressure = data.pressureChecks[route]
                val flatness = data.flatnessChecks[route]
                val readiness = data.readinessChecks[route]
                val onStep = { action: CheckStepAction ->
                    when (action) {
                        is CheckStepAction.Open -> onStateChange(state.open(action.route))
                    }
                }
                if (measure != null) {
                    RohbauMasseScreen(
                        model = measure,
                        onBack = onBack,
                        onAction = { action ->
                            when (action) {
                                is MeasureAction.Open -> onStateChange(state.open(action.route))
                            }
                        },
                    )
                } else if (panel != null) {
                    ElVerteilerScreen(model = panel, onClose = onBack, onAction = onStep)
                } else if (smoke != null) {
                    ElRauchmelderScreen(model = smoke, onBack = onBack, onAction = onStep)
                } else if (pressure != null) {
                    SanDruckScreen(model = pressure, onClose = onBack)
                } else if (flatness != null) {
                    EsEbenheitScreen(
                        model = flatness,
                        onClose = onBack,
                        onAction = { action ->
                            when (action) {
                                is FlatnessAction.Open -> onStateChange(state.open(action.route))
                            }
                        },
                    )
                } else if (readiness != null) {
                    EsBelegreifeScreen(model = readiness, onBack = onBack)
                } else {
                    PushedPlaceholder(route.check, "screen.inspection.${route.tradeId}.${route.check}", onBack)
                }
            }

            is AppRoute.InspectionRoom -> {
                val room = data.roomChecks[route]
                val objects = data.objectChecks[route]
                val tiles = data.tileChecks[route]
                val paint = data.paintChecks[route]
                val onStep = { action: CheckStepAction ->
                    when (action) {
                        is CheckStepAction.Open -> onStateChange(state.open(action.route))
                    }
                }
                if (room != null) {
                    ElRaumScreen(
                        model = room,
                        onClose = onBack,
                        onAction = { action ->
                            when (action) {
                                is CheckStepAction.Open -> onStateChange(state.open(action.route))
                            }
                        },
                    )
                } else if (objects != null) {
                    SanBadScreen(model = objects, onClose = onBack)
                } else if (tiles != null) {
                    FlBadScreen(model = tiles, onClose = onBack, onAction = onStep)
                } else if (paint != null) {
                    MaRaumScreen(model = paint, onClose = onBack, onAction = onStep)
                } else {
                    PushedPlaceholder(route.roomId, "screen.inspection.${route.tradeId}.room.${route.roomId}", onBack)
                }
            }

            is AppRoute.InspectionCheckItem -> {
                val slope = data.slopeChecks[route]
                if (slope != null) {
                    SanGefaelleScreen(
                        model = slope,
                        onClose = onBack,
                        onAction = { action ->
                            when (action) {
                                is SlopeAction.Open -> onStateChange(state.open(action.route))
                            }
                        },
                    )
                } else {
                    PushedPlaceholder(
                        route.item,
                        "screen.inspection.${route.tradeId}.${route.check}.${route.item}",
                        onBack,
                    )
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

@Composable
private fun InspectionPlaceholder(data: AppData, tradeId: String, onBack: () -> Unit) {
    val title = data.inspections.rows.firstOrNull { it.tradeId == tradeId }?.name ?: tradeId
    PushedPlaceholder(title, "screen.inspection.$tradeId", onBack)
}
