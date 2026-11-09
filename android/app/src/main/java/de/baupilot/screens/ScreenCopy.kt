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

    /** Abnahmen root, shared by the Gewerke and Mängel views (M4 D6). */
    object AbnGewerke {
        /** AndAbnGewerke.dc.html:20 (placeholder link, no action) */
        const val TEMPLATES = "Vorlagen"

        /** AndAbnGewerke.dc.html:24 */
        const val TRADES_VIEW = "Gewerke"

        /** AndAbnGewerke.dc.html:25 and AndAbnMaengel.dc.html:25: the count is `defects.size`. */
        const val DEFECTS_VIEW_FORMAT = "Offene Mängel (%d)"

        /** AndAbnGewerke.dc.html:23, the aria label of the tablist */
        const val VIEWS = "Ansicht"
    }

    object AbnMaengel {
        /** AndAbnMaengel.dc.html:39 (own entry; the Vergleich prototype has its own, placeholder, no action) */
        const val REMIND = "Erinnern"

        /** AndAbnMaengel.dc.html:40 (placeholder, no action) */
        const val RESOLVE = "Behoben prüfen"

        /** AndAbnMaengel.dc.html:47 (placeholder row, no action) */
        const val RESOLVED = "Behobene Mängel"
    }

    object AbnElemente {
        /** AndAbnElemente.dc.html:26, the aria label of the help button (placeholder, no action) */
        const val GUIDE = "Prüfanleitung"
    }

    object AbnPruefung {
        /** AndAbnPruefung.dc.html:57 */
        const val GUIDE = "So prüfen"

        /** AndAbnPruefung.dc.html:29: the bar is hard-coded at 42 % in the prototype. */
        const val PROGRESS = 0.42f

        /** AndAbnPruefung.dc.html:19, the aria label of the close link */
        const val CLOSE = "Schließen"
    }

    object AbnMangel {
        /** AndAbnMangel.dc.html:19, the aria label of the cancel link */
        const val CANCEL = "Abbrechen"

        /** AndAbnMangel.dc.html:22 */
        const val TITLE = "Mangel erfassen"

        /** AndAbnMangel.dc.html:23 */
        const val SAVE = "Sichern"

        /** AndAbnMangel.dc.html:36 */
        const val DESCRIPTION = "Beschreibung"

        /** AndAbnMangel.dc.html:49, the aria label of the photo button */
        const val ADD_PHOTO = "Foto aufnehmen"

        /** AndAbnMangel.dc.html:56 */
        const val SEVERITY = "Schwere"

        /** AndAbnMangel.dc.html:59, prefix of the selected severity */
        const val SELECTED_PREFIX = "✓ "

        /** AndAbnMangel.dc.html:66 */
        const val DEADLINE = "Behebung bis"

        /** AndAbnMangel.dc.html:80 */
        const val RESPONSIBLE = "Zuständig"

        /** AndAbnMangel.dc.html:71-73: chip i adds `DEADLINE_OFFSET_DAYS[i]` days to `meta.now` (+ 14 Tage, + 4 Wochen, + 6 Wochen). */
        val DEADLINE_OFFSET_DAYS = listOf(14, 28, 42)
    }

    object AbnSignatur {
        /** AndAbnSignatur.dc.html:39 */
        const val RESERVED = "Vorbehaltene Mängel"

        /** AndAbnSignatur.dc.html:47, prefix of the due date */
        const val DUE_PREFIX = "bis"

        /** AndAbnSignatur.dc.html:56 */
        const val CRAFTSMAN = "Handwerker"

        /** AndAbnSignatur.dc.html:60, the aria label of the craftsman signature */
        const val SIGNATURE = "Unterschrift Handwerker"

        /** AndAbnSignatur.dc.html:64 */
        const val SIGN_HERE = "Zum Unterschreiben tippen"

        /** AndAbnSignatur.dc.html:71 */
        const val FINISH = "Protokoll abschließen"
    }
}
