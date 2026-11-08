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

    enum Gewerk {
        // IosGewerk.dc.html:54
        static let offersHeading = "Angebote"
        // IosGewerk.dc.html:70
        static let bestLabel = "Günstigstes"
        // IosGewerk.dc.html:79
        static let stepsHeading = "Nächste Schritte"
        // IosGewerk.dc.html:94
        static let compareLabel = "Angebote vergleichen"
        // IosGewerk.dc.html:95
        static let addLabel = "Dokument hinzufügen"
        // IosGewerk.dc.html:24
        static let moreLabel = "Weitere Aktionen"
        /// Done state of each step by position (IosGewerk.dc.html:118-120): step 1 done, steps 2 and 3 open.
        static let stepsDone = [true, false, false]
    }

    enum Vergleich {
        // IosVergleich.dc.html:28
        static let title = "Angebote vergleichen"
        // IosVergleich.dc.html:24
        static let deadlineNote = "Frist 15.01."
        // IosVergleich.dc.html:32
        static let tableLabel = "Vergleich der Estrich-Angebote"
        // IosVergleich.dc.html:36
        static let bestLabel = "Günstigstes"
        // IosVergleich.dc.html:62
        static let remindLabel = "Erinnern"
        // IosVergleich.dc.html:67
        static let askLabel = "Rückfrage"
        // IosVergleich.dc.html:68
        static let awardLabel = "Wagner beauftragen"
    }

    enum Dokumente {
        // IosDokumente.dc.html:24
        static let addLabel = "Dokument hinzufügen"
        // IosDokumente.dc.html:34
        static let filtersLabel = "Dokumenttyp"
    }

    enum DokViewer {
        // IosDokViewer.dc.html:57
        static let totalLabel = "Gesamt brutto"
        // IosDokViewer.dc.html:76
        static let compareLabel = "Im Vergleich ansehen"
        // IosDokViewer.dc.html:24
        static let shareLabel = "Teilen"
    }

    enum Mitteilungen {
        // IosMitteilungen.dc.html:24
        static let markAllLabel = "Alle gelesen"

        /// Icon tile per notification kind (IosMitteilungen.dc.html:43-46): alert triangle, receipt, document, calendar.
        static func tile(_ kind: NotificationKind) -> (icon: BaupilotIcon, background: ColorToken, foreground: ColorToken) {
            switch kind {
            case .frist: return (.alertTriangleW22, Tokens.eSoft.mitteilungen, Tokens.eFg)
            case .geld: return (.receiptW22, Tokens.aSoft, Tokens.aFg)
            case .angebot: return (.documentW22, Tokens.iSoft, Tokens.iFg)
            case .termin: return (.calendarW22, Tokens.oSoft, Tokens.oFg)
            }
        }
    }
}
