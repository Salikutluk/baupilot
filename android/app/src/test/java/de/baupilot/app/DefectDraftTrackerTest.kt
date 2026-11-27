package de.baupilot.app

import org.junit.Assert.assertEquals
import org.junit.Test

class DefectDraftTrackerTest {
    private val trade = AppRoute.Trade("screed")
    private val inspection = AppRoute.Inspection("windows")

    private fun loggedIn() = AppNavigationState().logIn()

    private fun inspectionsAt(vararg routes: AppRoute): AppNavigationState =
        routes.fold(loggedIn().select(AppTab.Inspections)) { state, route -> state.push(route) }

    private fun effect(before: AppNavigationState, after: AppNavigationState, saved: Boolean = false) =
        DefectDraftTracker.onTransition(before, after, saved)

    @Test
    fun startsWhenNewDefectIsEntered() {
        val before = inspectionsAt(inspection)
        assertEquals(DraftEffect.Start, effect(before, before.push(AppRoute.NewDefect)))
        val fromRoot = loggedIn().select(AppTab.Inspections)
        assertEquals(DraftEffect.Start, effect(fromRoot, fromRoot.open(AppRoute.NewDefect)))
    }

    @Test
    fun discardsWhenPoppedWithoutSave() {
        val before = inspectionsAt(inspection, AppRoute.NewDefect)
        assertEquals(DraftEffect.Discard, effect(before, before.pop()))
    }

    @Test
    fun discardsWhenLeftByOtherNavigationWithoutSave() {
        val before = inspectionsAt(inspection, AppRoute.NewDefect)
        assertEquals(DraftEffect.Discard, effect(before, before.select(AppTab.Inspections)))
        assertEquals(DraftEffect.Discard, effect(before, before.logOut()))
        assertEquals(DraftEffect.Discard, effect(before, before.open(inspection)))
    }

    @Test
    fun keepsWhenSaved() {
        val before = inspectionsAt(inspection, AppRoute.NewDefect)
        assertEquals(DraftEffect.Keep, effect(before, before.saveDefect("windows", hasSignOff = false), saved = true))
    }

    @Test
    fun noneOtherwise() {
        val form = inspectionsAt(inspection, AppRoute.NewDefect)
        assertEquals(DraftEffect.None, effect(form, form))
        assertEquals(DraftEffect.None, effect(form, form.select(AppTab.Home)))
        assertEquals(DraftEffect.None, effect(form, form, saved = true))
        val plain = inspectionsAt(inspection)
        assertEquals(DraftEffect.None, effect(plain, plain.pop()))
        assertEquals(DraftEffect.None, effect(plain, plain.push(trade)))
        assertEquals(DraftEffect.None, effect(plain, plain, saved = true))
    }
}
