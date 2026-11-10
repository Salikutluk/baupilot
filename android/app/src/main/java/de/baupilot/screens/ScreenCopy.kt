package de.baupilot.screens

import de.baupilot.app.BannerTone
import de.baupilot.app.ElectricalTone
import de.baupilot.app.FiTone
import de.baupilot.app.InspectionStatus
import de.baupilot.app.NotificationKind
import de.baupilot.app.OverviewTone
import de.baupilot.app.PaintColor
import de.baupilot.app.RowMark
import de.baupilot.app.SmokeRoomStatus
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

    /** Protocol screens Erd, Rohbau and Zimmerer (M5 D2, D7). */
    object Protocol {
        /** design/screens.md rows 16, 17 and 19: the trades that open a protocol screen. */
        val TRADES = listOf("earthworks", "shell", "carpentry")

        /**
         * Banner tone by the overview status of the trade: accepted is ok with the PDF button (AndErd.dc.html:34),
         * reserved is warn with a chevron towards the Mängel view (AndRohbau.dc.html:28-35). Any other status has no banner.
         */
        fun bannerTone(status: InspectionStatus): BannerTone? = when (status) {
            InspectionStatus.Accepted -> BannerTone.Ok
            InspectionStatus.Reserved -> BannerTone.Warn
            InspectionStatus.Today, InspectionStatus.Planned -> null
        }

        /** AndRohbau.dc.html:71: a row is a defect row exactly when its meta is the defect status label ("Mangel"). */
        fun rowMark(meta: String, defectLabel: String): RowMark = if (meta == defectLabel) RowMark.Defect else RowMark.Ok
    }

    object RohbauMasse {
        /** AndRohbauMasse.dc.html:24: the tolerance in mm replaces the format argument. */
        const val SUBTITLE_FORMAT = "OG Schlafen · Toleranz ± %d mm"

        /** AndRohbauMasse.dc.html:31 */
        const val INSTRUCTION = "Zollstock oben, mittig und unten anlegen. Den kleinsten Wert eintragen – das Fenster muss überall passen."

        /** AndRohbauMasse.dc.html:36: followed by the target, " mm · " and the difference. */
        const val TARGET_PREFIX = "Soll"

        /** AndRohbauMasse.dc.html:39, the minus sign is U+2212. */
        const val DECREMENT_GLYPH = "−"

        /** AndRohbauMasse.dc.html:41 */
        const val INCREMENT_GLYPH = "+"

        /** AndRohbauMasse.dc.html:39: the aria label of the minus button, the dimension label is the argument. */
        const val DECREMENT_ARIA_FORMAT = "%s verringern"

        /** AndRohbauMasse.dc.html:41: the aria label of the plus button, the dimension label is the argument. */
        const val INCREMENT_ARIA_FORMAT = "%s erhöhen"

        /** AndRohbauMasse.dc.html:47, the aria label of the photo button (placeholder, no action) */
        const val PHOTO = "Foto aufnehmen"

        /** AndRohbauMasse.dc.html:51 (placeholder, no action) */
        const val SAVE = "Speichern"

        /** AndRohbauMasse.dc.html:56 */
        const val MEASURED_HEADING = "Bereits gemessen"

        /** AndRohbauMasse.dc.html:67 */
        const val PLUMB_TITLE = "Wände im Lot"

        /** AndRohbauMasse.dc.html:68 */
        const val PLUMB_SUBTITLE = "2-m-Wasserwaage an allen Außenecken"

        /** AndRohbauMasse.dc.html:70 */
        const val PLUMB_VALUE = "8 / 8"
    }

    object ElUebersicht {
        /** AndElUebersicht.dc.html:36: the active phase bar is hard-coded at 36 percent (4/11 would be 0.3636). */
        const val ACTIVE_PHASE_FRACTION = 0.36f

        /**
         * AndElUebersicht.dc.html:66: the status label decides the row tone. Offen is open, Läuft running,
         * In Ordnung and Vorhanden ok, Fehlt missing; any other label is not in the table.
         */
        fun tone(statusLabel: String): ElectricalTone? = when (statusLabel) {
            "Offen" -> ElectricalTone.Open
            "Läuft" -> ElectricalTone.Running
            "In Ordnung", "Vorhanden" -> ElectricalTone.Ok
            "Fehlt" -> ElectricalTone.Missing
            else -> null
        }

        /**
         * AndElUebersicht.dc.html:72-85: Haustechnik draws the meter cabinet (row 0) and the smoke alarm (row 1),
         * Räume a house, Dokumente a document. A group or row beyond the table has no icon.
         */
        fun icon(group: Int, row: Int): BaupilotIcon? = when (group) {
            0 -> listOf(BaupilotIcon.MeterCabinet, BaupilotIcon.SmokeAlarm).getOrNull(row)
            1 -> BaupilotIcon.Home
            2 -> BaupilotIcon.Document
            else -> null
        }
    }

    object ElRaum {
        /** AndElRaum.dc.html:29: the progress bar is hard-coded at 30 percent. */
        const val PROGRESS = 0.30f
    }

    object ElVerteiler {
        /** AndElVerteiler.dc.html:26: the progress bar is hard-coded at 50 percent. */
        const val PROGRESS = 0.50f

        /** AndElVerteiler.dc.html:101-102: Ausgelöst is ok (green), Jetzt testen is open. */
        fun tone(statusLabel: String): FiTone? = when (statusLabel) {
            "Ausgelöst" -> FiTone.Ok
            "Jetzt testen" -> FiTone.Open
            else -> null
        }
    }

    object ElRauchmelder {
        /** AndElRauchmelder.dc.html:81: Getestet is ok, Jetzt is now, Offen is open. */
        fun roomStatus(statusLabel: String): SmokeRoomStatus? = when (statusLabel) {
            "Getestet" -> SmokeRoomStatus.Ok
            "Jetzt" -> SmokeRoomStatus.Now
            "Offen" -> SmokeRoomStatus.Open
            else -> null
        }

        /** AndElRauchmelder.dc.html:85-86: by position the first check is done, the second open. */
        val CHECKS_DONE = listOf(true, false)

        /** AndElRauchmelder.dc.html:66: the reminder switch starts on (aria-checked is true). */
        const val REMINDER_INITIALLY_ON = true
    }

    /** Overviews of the later trades Sanitär, Estrich, Fliesen and Maler (M6 D2, D7, D8). */
    object TradeOverview {
        /** design/screens.md rows 24, 28, 31 and 33: the trades that open an overview with checks. */
        val TRADES = listOf("plumbing", "screed", "tiles", "painting")

        private val DEFECT_LABEL = Regex("^[1-9][0-9]* Mangel$")

        /**
         * AndSanUebersicht.dc.html:66, AndEsUebersicht.dc.html:68, AndFlUebersicht.dc.html:63 and AndMaUebersicht.dc.html:61:
         * the status label decides the row tone; "n Mangel" is a defect. Any other label is not in the table.
         */
        fun tone(statusLabel: String): OverviewTone? = when {
            statusLabel == "In Ordnung" -> OverviewTone.Ok
            statusLabel == "Offen" -> OverviewTone.Open
            statusLabel == "Läuft" -> OverviewTone.Running
            statusLabel == "Heute" -> OverviewTone.Today
            statusLabel == "Geplant" -> OverviewTone.Planned
            statusLabel == "Pflicht" -> OverviewTone.Required
            DEFECT_LABEL.matches(statusLabel) -> OverviewTone.Defect
            else -> null
        }

        /**
         * The active phase bar is hard-coded in the prototypes: plumbing AndSanUebersicht.dc.html:32 (50 percent),
         * screed AndEsUebersicht.dc.html:32 (75 percent), tiles AndFlUebersicht.dc.html:36 (50 percent).
         */
        fun activePhaseFraction(tradeId: String): Float? = when (tradeId) {
            "plumbing" -> 0.50f
            "screed" -> 0.75f
            "tiles" -> 0.50f
            else -> null
        }

        /**
         * AndSanUebersicht.dc.html:68-86: Leitungen pipe, drop, pipe; Heizung heat, heat; Bäder & Küche home, home.
         * A group or row beyond the table has no icon.
         */
        fun plumbingIcon(group: Int, row: Int): BaupilotIcon? = when (group) {
            0 -> listOf(BaupilotIcon.Pipe, BaupilotIcon.Drop, BaupilotIcon.Pipe).getOrNull(row)
            1 -> listOf(BaupilotIcon.Heat, BaupilotIcon.Heat).getOrNull(row)
            2 -> listOf(BaupilotIcon.Home, BaupilotIcon.Home).getOrNull(row)
            else -> null
        }
    }

    object MaUebersicht {
        /** AndMaUebersicht.dc.html: the first word of a row sub names the paint colour of its swatch. */
        fun swatch(word: String): PaintColor? = when (word) {
            "Weiß" -> PaintColor.White
            "Sand" -> PaintColor.Sand
            "Salbei" -> PaintColor.Sage
            else -> null
        }
    }

    object SanGefaelle {
        /** AndSanGefaelle.dc.html:44 */
        const val LENGTH = "Länge"

        /** AndSanGefaelle.dc.html:52 */
        const val DROP = "Höhenunterschied"

        /** AndSanGefaelle.dc.html:46, the minus sign is U+2212. */
        const val DECREMENT_GLYPH = "−"

        /** AndSanGefaelle.dc.html:48 */
        const val INCREMENT_GLYPH = "+"

        /** AndSanGefaelle.dc.html:46: the aria label of the minus button, the label is the argument. */
        const val DECREMENT_ARIA_FORMAT = "%s verringern"

        /** AndSanGefaelle.dc.html:48: the aria label of the plus button, the label is the argument. */
        const val INCREMENT_ARIA_FORMAT = "%s erhöhen"

        /** AndSanGefaelle.dc.html:62, the level line of the schema */
        const val SCHEMA_LEVEL = "waagerecht"

        /** AndSanGefaelle.dc.html:62, the drop mark of the schema, the delta is U+0394. */
        const val SCHEMA_DELTA = "Δh"

        /** AndSanGefaelle.dc.html:74 (placeholder, no action) */
        const val SAVE = "Speichern"

        /** AndSanGefaelle.dc.html:70, the aria label of the photo button (placeholder, no action) */
        const val PHOTO = "Foto aufnehmen"

        /** AndSanGefaelle.dc.html:105: the section label of the current section. */
        const val SECTION_NOW = "jetzt"

        /** AndSanGefaelle.dc.html:106: the section label of a section not yet measured. */
        const val SECTION_OPEN = "offen"
    }

    object SanDruck {
        /** AndSanDruck.dc.html:29: the progress bar is hard-coded at 50 percent. */
        const val PROGRESS = 0.50f

        /** AndSanDruck.dc.html:51: the timer ring is hard-coded at 58 percent. */
        const val RING_FRACTION = 0.58f

        /** AndSanDruck.dc.html:71 (placeholder, no action) */
        const val PHOTO = "Manometer fotografieren"
    }

    object SanBad {
        /** AndSanBad.dc.html:28, the aria label of the tablist */
        const val TABS = "Objekte"
    }

    object EsEbenheit {
        /** AndEsEbenheit.dc.html:24: the limit text ("4 mm") replaces the format argument. */
        const val SUBTITLE_FORMAT = "Grenzwert laut Vertrag: %s unter 2-m-Latte"

        /** AndEsEbenheit.dc.html:31 */
        const val INSTRUCTION = "Richtlatte in mehreren Richtungen auflegen. Größten Spalt darunter mit dem Messkeil ablesen und eintragen."

        /** AndEsEbenheit.dc.html:33: followed by the number of the next point. */
        const val STEPPER_LABEL_PREFIX = "Spalt Messpunkt M"

        /** AndEsEbenheit.dc.html:35: the noun of the stepper aria labels ("Spalt verringern" / "Spalt erhöhen"). */
        const val STEPPER_NOUN = "Spalt"

        /** AndEsEbenheit.dc.html:42, the aria label of the photo button (placeholder, no action) */
        const val PHOTO = "Foto aufnehmen"

        /** AndEsEbenheit.dc.html:45 */
        const val SAVE = "Messpunkt speichern"

        /** AndEsEbenheit.dc.html:50: the count of saved points replaces the format argument. */
        const val MEASURED_FORMAT = "Gemessen (%d)"
    }

    object EsBelegreife {
        /** AndEsBelegreife.dc.html:22 */
        const val TITLE = "Aufheizen & Belegreife"

        /** AndEsBelegreife.dc.html: a bar is `round(value / 45 * 72)` high. */
        const val BAR_SCALE_MAX = 45

        /** AndEsBelegreife.dc.html: the height of a bar at [BAR_SCALE_MAX]. */
        const val BAR_SCALE_HEIGHT = 72
    }

    object FlBad {
        /** AndFlBad.dc.html:58 */
        const val DEFECT = "Als Mangel"

        /** AndFlBad.dc.html:59 (placeholder, no action) */
        const val NONE = "Keine Hohlstellen"

        /** AndFlBad.dc.html:51: the grid has 8 columns. */
        const val COLUMNS = 8

        /** AndFlBad.dc.html:51: the grid has 3 rows. */
        const val ROWS = 3

        /** AndFlBad.dc.html:29: the progress bar is hard-coded at 42 percent. */
        const val PROGRESS = 0.42f
    }

    object MaRaum {
        /** AndMaRaum.dc.html:52, the aria label of the tablist */
        const val WALLS = "Flächen"

        /** AndMaRaum.dc.html:58, the aria label of the photo button (placeholder, no action) */
        const val PHOTO = "Foto aufnehmen"

        /** AndMaRaum.dc.html: the done row of the steps shows the Sand swatch. */
        val DONE_SWATCH = PaintColor.Sand

        /** AndMaRaum.dc.html:29: the progress bar is hard-coded at 30 percent. */
        const val PROGRESS = 0.30f
    }
}
