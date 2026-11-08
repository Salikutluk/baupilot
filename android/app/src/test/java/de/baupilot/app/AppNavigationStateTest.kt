package de.baupilot.app

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
