package de.baupilot.app

/** What the draft defect (photos taken before "Sichern") needs after a navigation change (I11). */
enum class DraftEffect { Start, Discard, Keep, None }

/** Decides from two consecutive navigation states whether a draft begins, is thrown away or is kept. */
object DefectDraftTracker {
    fun onTransition(before: AppNavigationState, after: AppNavigationState, saved: Boolean): DraftEffect {
        val had = before.hasDraft()
        val has = after.hasDraft()
        return when {
            !had && has -> DraftEffect.Start
            had && !has -> if (saved) DraftEffect.Keep else DraftEffect.Discard
            else -> DraftEffect.None
        }
    }

    /** The form stays alive on a background tab, so every stack counts. */
    private fun AppNavigationState.hasDraft(): Boolean = paths.values.any { AppRoute.NewDefect in it }
}
