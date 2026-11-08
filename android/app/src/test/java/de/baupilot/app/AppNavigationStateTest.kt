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
        val saved = listOf(true, "home", listOf(listOf("elsewhere:1"), emptyList<String>(), emptyList<String>(), emptyList<String>(), emptyList<String>()))
        assertNull(AppNavigationState.Saver.restore(saved))
        val bareTrade = listOf(true, "home", listOf(listOf("screed"), emptyList<String>(), emptyList<String>(), emptyList<String>(), emptyList<String>()))
        assertNull(AppNavigationState.Saver.restore(bareTrade))
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
