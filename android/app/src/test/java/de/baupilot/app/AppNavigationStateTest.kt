package de.baupilot.app

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigationStateTest {
    private val trade = AppRoute.Trade("screed")

    private fun loggedIn() = AppNavigationState().logIn()

    @Test
    fun startsLoggedOutOnHome() {
        val state = AppNavigationState()
        assertFalse(state.isLoggedIn)
        assertEquals(AppTab.Home, state.selectedTab)
        AppTab.entries.forEach { assertTrue(state.path(it).isEmpty()) }
        assertEquals(
            listOf("home", "schedule", "inspections", "documents", "profile"),
            AppTab.entries.map { it.route },
        )
    }

    @Test
    fun logInSelectsHomeAndClearsStacks() {
        val dirty = AppNavigationState(
            isLoggedIn = true,
            selectedTab = AppTab.Profile,
            paths = mapOf(AppTab.Home to listOf(trade)),
        )
        val state = dirty.logIn()
        assertTrue(state.isLoggedIn)
        assertEquals(AppTab.Home, state.selectedTab)
        AppTab.entries.forEach { assertTrue(state.path(it).isEmpty()) }
    }

    @Test
    fun selectSwitchesWithoutTouchingStacks() {
        val before = loggedIn().push(trade)
        val after = before.select(AppTab.Schedule)
        assertEquals(AppTab.Schedule, after.selectedTab)
        assertEquals(before.paths, after.paths)
        assertEquals(listOf<AppRoute>(trade), after.path(AppTab.Home))
    }

    @Test
    fun pushOnlyAffectsSelectedTab() {
        val state = loggedIn().select(AppTab.Schedule).push(trade).push(AppRoute.Notifications)
        assertEquals(listOf(trade, AppRoute.Notifications), state.path(AppTab.Schedule))
        AppTab.entries.filter { it != AppTab.Schedule }.forEach { assertTrue(state.path(it).isEmpty()) }
    }

    @Test
    fun reselectPopsToRoot() {
        val state = loggedIn().push(trade).push(AppRoute.Notifications)
        val other = state.select(AppTab.Profile).push(trade)
        val reselected = other.select(AppTab.Profile)
        assertTrue(reselected.path(AppTab.Profile).isEmpty())
        assertEquals(listOf(trade, AppRoute.Notifications), reselected.path(AppTab.Home))
        assertEquals(AppTab.Profile, reselected.selectedTab)
    }

    @Test
    fun popOnEmptyIsNoOp() {
        val state = loggedIn()
        assertFalse(state.canPop)
        assertEquals(state, state.pop())
        val pushed = state.push(trade).push(AppRoute.Notifications)
        assertTrue(pushed.canPop)
        assertEquals(listOf<AppRoute>(trade), pushed.pop().path(AppTab.Home))
    }

    @Test
    fun showsTabBarOnlyAtRoot() {
        val state = loggedIn()
        assertTrue(state.showsTabBar)
        val pushed = state.push(trade)
        assertFalse(pushed.showsTabBar)
        assertTrue(pushed.select(AppTab.Documents).showsTabBar)
        assertTrue(pushed.pop().showsTabBar)
    }

    @Test
    fun pushWhileLoggedOutIsNoOp() {
        val state = AppNavigationState()
        assertEquals(state, state.push(trade))
        assertEquals(state, state.pop())
    }

    @Test
    fun openAppendsNewRoute() {
        val state = loggedIn().open(trade).open(AppRoute.Compare("screed"))
        assertEquals(listOf(trade, AppRoute.Compare("screed")), state.path(AppTab.Home))
    }

    @Test
    fun openExistingRoutePopsBackToIt() {
        val state = loggedIn().open(trade).open(AppRoute.Compare("screed")).open(AppRoute.Document("d1"))
        val back = state.open(trade)
        assertEquals(listOf<AppRoute>(trade), back.path(AppTab.Home))
        assertEquals(listOf(trade, AppRoute.Compare("screed")), state.open(AppRoute.Compare("screed")).path(AppTab.Home))
    }

    @Test
    fun openNeverDuplicates() {
        var state = loggedIn()
        listOf(
            trade, AppRoute.Notifications, trade, AppRoute.Document("d1"), AppRoute.Document("d1"),
            AppRoute.Compare("screed"), AppRoute.Notifications,
        ).forEach {
            state = state.open(it)
            val path = state.path(AppTab.Home)
            assertEquals(path.distinct(), path)
            assertEquals(it, path.last())
        }
    }

    @Test
    fun openWhileLoggedOutIsNoOp() {
        val state = AppNavigationState()
        assertEquals(state, state.open(trade))
        assertEquals(state, state.go(NavigationTarget.Push(trade)))
    }

    @Test
    fun goTabSelectsWithoutTouchingStacks() {
        val before = loggedIn().push(trade)
        val after = before.go(NavigationTarget.Tab(AppTab.Documents))
        assertEquals(AppTab.Documents, after.selectedTab)
        assertEquals(before.paths, after.paths)
        assertEquals(before.select(AppTab.Documents), after)
    }

    @Test
    fun goPushOpensOnSelectedTab() {
        val state = loggedIn().select(AppTab.Schedule)
            .go(NavigationTarget.Push(trade))
            .go(NavigationTarget.Push(trade))
        assertEquals(listOf<AppRoute>(trade), state.path(AppTab.Schedule))
        assertTrue(state.path(AppTab.Home).isEmpty())
    }

    @Test
    fun saverRoundTripsAllRoutes() {
        val scope = SaverScope { true }
        val state = loggedIn()
            .open(AppRoute.Trade("screed")).open(AppRoute.Compare("screed"))
            .open(AppRoute.Document("d1")).open(AppRoute.Notifications)
            .select(AppTab.Documents)
            .open(AppRoute.Document("d2")).open(AppRoute.Trade("tiles")).open(AppRoute.Compare("tiles"))
        val saved = with(AppNavigationState.Saver) { scope.save(state) }
        assertEquals(state, AppNavigationState.Saver.restore(saved!!))
    }

    @Test
    fun saverRejectsUnknownRoute() {
        val empty = emptyList<String>()
        val saved = listOf(true, "home", listOf(listOf("elsewhere:1"), empty, empty, empty, empty), "Trades")
        assertNull(AppNavigationState.Saver.restore(saved))
        val bareTrade = listOf(true, "home", listOf(listOf("screed"), empty, empty, empty, empty), "Trades")
        assertNull(AppNavigationState.Saver.restore(bareTrade))
        listOf("step:a/b", "step:a/b/0", "step:a/b/03", "step:a/b/x", "step:a/b/3/4", "defect:old", "inspection", "sign")
            .forEach {
                val malformed = listOf(true, "inspections", listOf(empty, empty, listOf(it), empty, empty), "Trades")
                assertNull(it, AppNavigationState.Saver.restore(malformed))
            }
    }

    @Test
    fun saverRejectsOldThreeElementShape() {
        val empty = emptyList<String>()
        val old = listOf(true, "home", listOf(listOf("trade:screed"), empty, empty, empty, empty))
        assertNull(AppNavigationState.Saver.restore(old))
    }

    @Test
    fun saverRejectsUnknownView() {
        val empty = emptyList<String>()
        assertNull(AppNavigationState.Saver.restore(listOf(true, "home", listOf(empty, empty, empty, empty, empty), "Other")))
    }

    @Test
    fun saverRoundTripsInspectionRoutesAndView() {
        val scope = SaverScope { true }
        val state = loggedIn()
            .select(AppTab.Inspections)
            .open(AppRoute.Inspection("windows"))
            .open(AppRoute.InspectionStep("windows", "F8", 3))
            .open(AppRoute.NewDefect)
            .open(AppRoute.SignOff("windows"))
            .selectInspectionsView(InspectionsView.Defects)
        val saved = with(AppNavigationState.Saver) { scope.save(state) }
        assertEquals(state, AppNavigationState.Saver.restore(saved!!))
        assertEquals(InspectionsView.Defects, AppNavigationState.Saver.restore(saved)!!.inspectionsView)

        val trades = state.selectInspectionsView(InspectionsView.Trades)
        val savedTrades = with(AppNavigationState.Saver) { scope.save(trades) }
        assertEquals(trades, AppNavigationState.Saver.restore(savedTrades!!))
    }

    private val inspection = AppRoute.Inspection("windows")
    private val step = AppRoute.InspectionStep("windows", "F8", 3)
    private val sign = AppRoute.SignOff("windows")

    @Test
    fun showDefectsSelectsInspectionsRootWithDefectsView() {
        val state = loggedIn().showDefects()
        assertEquals(AppTab.Inspections, state.selectedTab)
        assertEquals(InspectionsView.Defects, state.inspectionsView)
        assertTrue(state.path(AppTab.Inspections).isEmpty())
        assertTrue(state.showsTabBar)
    }

    @Test
    fun showDefectsClearsOnlyInspectionsStack() {
        val before = loggedIn().push(trade)
            .select(AppTab.Inspections).open(inspection).open(step)
            .select(AppTab.Documents).push(AppRoute.Document("d1"))
            .select(AppTab.Inspections)
        val after = before.showDefects()
        assertTrue(after.path(AppTab.Inspections).isEmpty())
        assertEquals(listOf<AppRoute>(trade), after.path(AppTab.Home))
        assertEquals(listOf<AppRoute>(AppRoute.Document("d1")), after.path(AppTab.Documents))
        assertEquals(AppTab.Inspections, after.selectedTab)
        // From another tab the target is still the Abnahmen root.
        val fromHome = before.select(AppTab.Home).showDefects()
        assertEquals(AppTab.Inspections, fromHome.selectedTab)
        assertTrue(fromHome.path(AppTab.Inspections).isEmpty())
        assertEquals(listOf<AppRoute>(trade), fromHome.path(AppTab.Home))
    }

    @Test
    fun goDefectsCallsShowDefects() {
        val before = loggedIn().select(AppTab.Inspections).open(inspection)
        assertEquals(before.showDefects(), before.go(NavigationTarget.Defects))
    }

    @Test
    fun selectInspectionsViewKeepsPathsAndTab() {
        val before = loggedIn().select(AppTab.Inspections).open(inspection).push(trade)
        val after = before.selectInspectionsView(InspectionsView.Defects)
        assertEquals(before.paths, after.paths)
        assertEquals(before.selectedTab, after.selectedTab)
        assertEquals(InspectionsView.Defects, after.inspectionsView)
        // Re-tap and a tab round trip never change the view.
        val defects = loggedIn().showDefects()
        assertEquals(InspectionsView.Defects, defects.select(AppTab.Inspections).inspectionsView)
        assertEquals(InspectionsView.Defects, defects.select(AppTab.Home).select(AppTab.Inspections).inspectionsView)
        // Push and pop keep it.
        assertEquals(InspectionsView.Defects, defects.open(inspection).pop().inspectionsView)
    }

    @Test
    fun logInResetsInspectionsView() {
        val state = loggedIn().showDefects()
        assertEquals(InspectionsView.Trades, state.logIn().inspectionsView)
        assertEquals(InspectionsView.Trades, AppNavigationState().inspectionsView)
    }

    @Test
    fun openAboveBuildsAnchorThenRoute() {
        val state = loggedIn().select(AppTab.Inspections).open(inspection).open(step).open(AppRoute.NewDefect)
        assertEquals(listOf(inspection, sign), state.open(sign, above = inspection).path(AppTab.Inspections))
        val fromRoot = loggedIn().select(AppTab.Inspections).open(sign, above = inspection)
        assertEquals(listOf(inspection, sign), fromRoot.path(AppTab.Inspections))
    }

    @Test
    fun openAboveNeverDuplicates() {
        var state = loggedIn().select(AppTab.Inspections)
        repeat(3) {
            state = state.open(sign, above = inspection)
            assertEquals(listOf(inspection, sign), state.path(AppTab.Inspections))
        }
        state = state.open(step).open(AppRoute.NewDefect).open(inspection, above = inspection)
        assertEquals(listOf<AppRoute>(inspection), state.path(AppTab.Inspections))
    }

    @Test
    fun newOpsAreNoOpsWhenLoggedOut() {
        val state = AppNavigationState()
        assertEquals(state, state.showDefects())
        assertEquals(state, state.selectInspectionsView(InspectionsView.Defects))
        assertEquals(state, state.open(sign, above = inspection))
        assertEquals(state, state.go(NavigationTarget.Defects))
    }

    @Test
    fun saverRoundTripsState() {
        val scope = SaverScope { true }
        val state = loggedIn()
            .push(trade).push(AppRoute.Notifications)
            .select(AppTab.Schedule).push(AppRoute.Trade("tiles"))
        val saved = with(AppNavigationState.Saver) { scope.save(state) }
        assertNotNull(saved)
        assertEquals(state, AppNavigationState.Saver.restore(saved!!))

        val loggedOut = AppNavigationState()
        val savedOut = with(AppNavigationState.Saver) { scope.save(loggedOut) }
        assertEquals(loggedOut, AppNavigationState.Saver.restore(savedOut!!))
    }
}
