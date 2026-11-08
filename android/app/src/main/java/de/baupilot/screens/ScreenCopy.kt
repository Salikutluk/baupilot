package de.baupilot.screens

import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.components.StatusChipColors

/**
 * Template chrome the fixture does not carry (D9), one entry per prototype string.
 * Each comment names the prototype `file:line`. Data-like text comes from the fixture instead.
 */
object ScreenCopy {
    /** AndMitteilungen.dc.html:22; the fixture carries no text for the placeholder (D8). */
    const val NOTIFICATIONS_TITLE = "Mitteilungen"

    object Login {
        /** AndLogin.dc.html:40 */
        const val PASSWORD_PLACEHOLDER = "••••••••"

        /** AndLogin.dc.html:47 */
        const val SOCIAL_MARK = "G"
    }

    object Home {
        /** AndHome.dc.html:20 */
        const val TITLE = "Übersicht"

        /** AndHome.dc.html:32 */
        const val PHASE_KICKER = "Aktuelle Phase"

        /** AndHome.dc.html:44 */
        const val NEXT_NEEDED = "Als Nächstes benötigt"

        /** AndHome.dc.html:45 (placeholder link, no action) */
        const val ALL = "Alle"

        /** AndHome.dc.html:60 */
        const val DOCUMENTS = "Dokumente"

        /** AndHome.dc.html:75 (placeholder link, no action) */
        const val DOCUMENT_FAB = "Dokument"

        /**
         * Status chip by `tradeId`, as the three rows of AndHome.dc.html:101-103 (Beauftragt ok, Vergleichen info,
         * Anfragen outline). A trade not listed here has no prototype tone.
         */
        fun tone(tradeId: String): StatusChipColors? = when (tradeId) {
            "electrical" -> StatusChipColors.Ok()
            "plumbing" -> StatusChipColors.Info
            "screed" -> StatusChipColors.Outline
            else -> null
        }
    }

    object Zeitplan {
        /** AndZeitplan.dc.html:19 */
        const val TITLE = "Zeitplan"

        /** AndZeitplan.dc.html:25 */
        const val ASK_HEADING = "Jetzt Handwerker anfragen"

        /** AndZeitplan.dc.html:39 */
        const val LEGEND_DONE = "Erledigt"

        /** AndZeitplan.dc.html:40 */
        const val LEGEND_RUNNING = "Läuft"

        /** AndZeitplan.dc.html:41 */
        const val LEGEND_PLANNED = "Geplant"

        /** AndZeitplan.dc.html:42 */
        const val LEGEND_ASK = "Anfragen"

        /** AndZeitplan.dc.html:70 */
        const val TODAY = "Heute"
    }

    object Profil {
        /** AndProfil.dc.html:19 */
        const val TITLE = "Profil"

        /** AndProfil.dc.html:28 (placeholder link, no action) */
        const val EDIT = "Bearbeiten"

        /**
         * Row icons by position over all three groups: Bauvorhaben (AndProfil.dc.html:81 home, :82 userProfile),
         * Benachrichtigungen (:85 bellSimple, :86 document, :87 calendar), App (:90 moon, :91 logout).
         */
        val ROW_ICONS = listOf(
            BaupilotIcon.Home,
            BaupilotIcon.UserProfile,
            BaupilotIcon.BellSimple,
            BaupilotIcon.Document,
            BaupilotIcon.Calendar,
            BaupilotIcon.Moon,
            BaupilotIcon.Logout,
        )

        /** AndProfil.dc.html:91, the row drawn in `error` without an action */
        const val SIGN_OUT = "Abmelden"
    }
}
