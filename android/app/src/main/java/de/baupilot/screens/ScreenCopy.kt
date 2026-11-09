package de.baupilot.screens

import de.baupilot.app.NotificationKind
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
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

    object Gewerk {
        /** AndGewerk.dc.html:63 */
        const val BEST = "Günstigstes"

        /** AndGewerk.dc.html:75 */
        const val COMPARE = "Angebote vergleichen"

        /** AndGewerk.dc.html:72 (aria-label of the attach button) */
        const val ADD = "Dokument hinzufügen"

        /** AndGewerk.dc.html:23 (aria-label of the overflow button) */
        const val MORE = "Weitere Optionen"

        /** AndGewerk.dc.html:44 (aria-label of the tab list) */
        const val TABS = "Bereiche"

        /** Fact icons by position: calendar, clock, link (AndGewerk.dc.html:96-98). */
        val FACT_ICONS = listOf(BaupilotIcon.Calendar, BaupilotIcon.Clock, BaupilotIcon.Link)
    }

    object Vergleich {
        /** AndVergleich.dc.html:23 */
        const val TITLE = "Angebote vergleichen"

        /** AndVergleich.dc.html:29 */
        const val TABLE = "Vergleich der Estrich-Angebote"

        /** AndVergleich.dc.html:59 (placeholder link, no action) */
        const val REMIND = "Erinnern"

        /** AndVergleich.dc.html:64 (placeholder link, no action) */
        const val ASK = "Rückfrage"

        /** AndVergleich.dc.html:65 (placeholder link, no action) */
        const val AWARD = "Wagner beauftragen"
    }

    object Dokumente {
        /** AndDokumente.dc.html:25 */
        const val FILTERS = "Dokumenttyp"

        /** AndDokumente.dc.html:63 */
        const val UPLOAD = "Hochladen"
    }

    object DokViewer {
        /** AndDokViewer.dc.html:54 */
        const val TOTAL = "Gesamt brutto"

        /** AndDokViewer.dc.html:73 */
        const val COMPARE = "Im Vergleich ansehen"

        /** AndDokViewer.dc.html:26 (aria-label) */
        const val SHARE = "Teilen"

        /** AndDokViewer.dc.html:68 */
        const val CHANGE = "Gewerk oder Typ ändern"
    }

    object Mitteilungen {
        /** AndMitteilungen.dc.html:23 (placeholder action) */
        const val MARK_ALL = "Alle gelesen"

        /** Icon circle of a notification kind: icon, background and foreground token. */
        class KindStyle(val icon: BaupilotIcon, val background: ColorToken, val foreground: ColorToken)

        /**
         * Icon circle by kind: frist AndMitteilungen.dc.html:55, geld :56, angebot :57, termin :58.
         */
        fun kindStyle(kind: NotificationKind): KindStyle = when (kind) {
            NotificationKind.Frist -> KindStyle(BaupilotIcon.AlertTriangle, Tokens.errBg, Tokens.errFg)
            NotificationKind.Geld -> KindStyle(BaupilotIcon.Receipt, Tokens.secondaryContainer, Tokens.onSecondaryContainer)
            NotificationKind.Angebot -> KindStyle(BaupilotIcon.Document, Tokens.infoBg, Tokens.infoFg)
            NotificationKind.Termin -> KindStyle(BaupilotIcon.Calendar, Tokens.okBg, Tokens.okFg)
        }
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
