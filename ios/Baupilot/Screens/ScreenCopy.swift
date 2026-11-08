import Foundation

/// Template chrome that the fixture does not carry (D9). Each entry names its prototype line.
enum ScreenCopy {
    // IosMitteilungen.dc.html:26 (title of the notifications placeholder; the fixture carries no text for it)
    static let notificationsTitle = "Mitteilungen"

    // Main.dc.html:48 (iOS login password input placeholder)
    static let passwordPlaceholder = "••••••••"

    enum Login {
        /// Feature bullet icons by position (Main.dc.html:71-73): calendar, document with check, rising line.
        static var featureIcons: [BaupilotIcon] { [.calendar, .documentCheck, .chartLine] }
        /// Brand tile icon (Main.dc.html:20).
        static var brandIcon: BaupilotIcon { .house }
        // Main.dc.html:57 (separator between "Neu hier?" and the link)
        static let newHereSeparator = " "
    }

    enum Home {
        // IosHome.dc.html:22
        static let title = "Übersicht"
        // IosHome.dc.html:32
        static let currentPhase = "Aktuelle Phase"
        // IosHome.dc.html:47
        static let nextNeeded = "Als Nächstes benötigt"
        // IosHome.dc.html:48 (link to the schedule tab)
        static let scheduleLink = "Zeitplan"
        // IosHome.dc.html:66
        static let documents = "Dokumente"

        /// Status tone per trade row, keyed by tradeId (IosHome.dc.html:100-102):
        /// Beauftragt uses the ok tone, Vergleichen the info tone, Anfragen the accent tone.
        static let tradeTones: [String: TradeTone] = [
            "electrical": .ok,
            "plumbing": .info,
            "screed": .accent,
        ]
        /// Tone of a trade that the prototype template does not list.
        static let defaultTradeTone: TradeTone = .accent

        /// Doc tile icons by position (IosHome.dc.html:109-112): Angebote, Verträge, Rechnungen, Pläne.
        static let docIcons: [DocIcon] = [.document, .contract, .receipt, .map]
    }

    enum Zeitplan {
        // IosZeitplan.dc.html:21
        static let title = "Zeitplan"
        // IosZeitplan.dc.html:25
        static let askHeading = "Jetzt Handwerker anfragen"
        // IosZeitplan.dc.html:39
        static let legendDone = "Erledigt"
        // IosZeitplan.dc.html:40
        static let legendRunning = "Läuft"
        // IosZeitplan.dc.html:41
        static let legendPlanned = "Geplant"
        // IosZeitplan.dc.html:42
        static let legendAsk = "Anfragen"
        // IosZeitplan.dc.html:70
        static let today = "Heute"
    }

    enum Profil {
        // IosProfil.dc.html:19
        static let title = "Profil"
        // IosProfil.dc.html:25 (separator between e-mail and role)
        static let contactSeparator = " · "
        /// Row whose label takes the danger tone (IosProfil.dc.html:80, `link('Abmelden', '', t.danger)`).
        static let signOut = "Abmelden"
    }
}
