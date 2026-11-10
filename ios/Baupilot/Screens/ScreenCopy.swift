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

    enum AbnGewerke {
        // IosAbnGewerke.dc.html:21
        static let templatesLabel = "Vorlagen"
        // IosAbnGewerke.dc.html:25 (first segment of the view tablist)
        static let tradesView = "Gewerke"
        // IosAbnGewerke.dc.html:26 (second segment; the count is the number of open defects)
        static let defectsViewFormat = "Offene Mängel (%d)"
        // IosAbnGewerke.dc.html:24 (aria-label of the view tablist)
        static let viewsLabel = "Ansicht"
    }

    enum AbnMaengel {
        // IosAbnMaengel.dc.html:21
        static let templatesLabel = "Vorlagen"
        // IosAbnMaengel.dc.html:24 (aria-label of the view tablist)
        static let viewsLabel = "Ansicht"
        // IosAbnMaengel.dc.html:40 (own entry: Vergleich.remindLabel cites the other prototype)
        static let remindLabel = "Erinnern"
        // IosAbnMaengel.dc.html:41
        static let resolveLabel = "Behoben?"
        // IosAbnMaengel.dc.html:50
        static let resolvedLabel = "Behobene Mängel"
        /// Buttons "Erinnern" and "Behoben?" only on a late card (IosAbnMaengel.dc.html:38-42, `late` flag of the template).
        static func showsActions(_ urgency: DefectUrgency) -> Bool { urgency == .late }
    }

    enum AbnElemente {
        // IosAbnElemente.dc.html:34
        static let guideLinkLabel = "Anleitung"
    }

    enum AbnPruefung {
        // IosAbnPruefung.dc.html:50
        static let guideLabel = "So prüfen"
    }

    enum AbnMangel {
        // IosAbnMangel.dc.html:21
        static let title = "Mangel erfassen"
        // IosAbnMangel.dc.html:22
        static let saveLabel = "Sichern"
        // IosAbnMangel.dc.html:20
        static let cancelLabel = "Abbrechen"
        // IosAbnMangel.dc.html:34
        static let descriptionLabel = "Beschreibung"
        // IosAbnMangel.dc.html:54
        static let severityLabel = "Schwere"
        // IosAbnMangel.dc.html:63
        static let deadlineLabel = "Behebung bis"
        // IosAbnMangel.dc.html:74
        static let responsibleLabel = "Zuständig"
        // IosAbnMangel.dc.html:47 (aria-label of the add-photo button)
        static let addPhotoLabel = "Foto aufnehmen"
        /// Days added to the base day by deadline chip position; the third chip ("Eigenes Datum") has no offset
        /// (D9; the chips are drawn at IosAbnMangel.dc.html:63-72).
        static let deadlineOffsets: [Int?] = [14, 28, nil]
    }

    enum AbnSignatur {
        // IosAbnSignatur.dc.html:55
        static let craftsmanLabel = "Handwerker"
        // IosAbnSignatur.dc.html:63
        static let ownerLabel = "Bauherr"
        // IosAbnSignatur.dc.html:66
        static let ownerPending = "Ausstehend"
        // IosAbnSignatur.dc.html:64
        static let signHereLabel = "Hier unterschreiben"
        // IosAbnSignatur.dc.html:47 (prefix of the deadline, followed by a space and the fixture date)
        static let duePrefix = "bis"
        // IosAbnSignatur.dc.html:72
        static let finishLabel = "Protokoll abschließen"
        // IosAbnSignatur.dc.html:57 (aria-label of the craftsman's signature)
        static let signatureLabel = "Unterschrift Handwerker"
        /// Tone of the three stat tiles by position: surface, ok, error (renderVals of IosAbnSignatur.dc.html:81-83).
        static let statTones: [CountTone] = [.surface, .ok, .error]
    }

    /// Trades that open the shared protocol screen (design/screens.md rows 16, 17 and 19: Erd, Rohbau, Zimmerer).
    enum ProtocolCopy {
        static let protocolTrades = ["earthworks", "shell", "carpentry"]

        /// Banner tone by overview status (IosErd.dc.html:17-54 accepted, IosRohbau.dc.html:30 reserved); nil for any other.
        static func bannerTone(overviewStatus: String) -> ProtocolTone? {
            switch overviewStatus {
            case "accepted": return .ok
            case "reserved": return .warn
            default: return nil
            }
        }

        /// Row mark: a row whose meta is the "defect" status label shows the defect mark, every other row the ok mark.
        static func mark(meta: String, defectLabel: String) -> ProtocolMark {
            meta == defectLabel ? .defect : .ok
        }
    }

    enum RohbauMasse {
        // IosRohbauMasse.dc.html:28 (subtitle, with the tolerance in mm)
        static let subtitleFormat = "OG Schlafen · Toleranz laut Vertrag ± %d mm"
        // IosRohbauMasse.dc.html:32
        static let instruction = "Zollstock oben, mittig und unten anlegen. Den kleinsten Wert eintragen – das Fenster muss überall passen."
        // IosRohbauMasse.dc.html:37 (prefix of the target line, followed by the diff)
        static let targetFormat = "Soll %d mm · "
        // IosRohbauMasse.dc.html:40 (stepper glyph, U+2212)
        static let decrementGlyph = "−"
        // IosRohbauMasse.dc.html:42
        static let incrementGlyph = "+"
        // IosRohbauMasse.dc.html:40 (aria-label of the decrement button)
        static let decrementAriaFormat = "%@ verringern"
        // IosRohbauMasse.dc.html:42 (aria-label of the increment button)
        static let incrementAriaFormat = "%@ erhöhen"
        // IosRohbauMasse.dc.html:48 (aria-label of the photo button)
        static let photoLabel = "Foto aufnehmen"
        // IosRohbauMasse.dc.html:52
        static let saveLabel = "Speichern"
        // IosRohbauMasse.dc.html:57
        static let measuredHeading = "Bereits gemessen"
        // IosRohbauMasse.dc.html:70
        static let plumbTitle = "Wände im Lot"
        // IosRohbauMasse.dc.html:71
        static let plumbSubtitle = "2-m-Wasserwaage an allen Außenecken"
        // IosRohbauMasse.dc.html:73
        static let plumbValue = "8 / 8"
    }

    enum ElUebersicht {
        // IosElUebersicht.dc.html:39 (width of the active phase bar: 36 %, not 4 / 11)
        static let activePhaseFraction = 0.36

        /// Status label to tone (IosElUebersicht.dc.html:76, map `S`); nil for a label the prototype does not know.
        static func tone(statusLabel: String) -> ElectricalTone? {
            switch statusLabel {
            case "Offen": return .open
            case "Läuft": return .running
            case "In Ordnung", "Vorhanden": return .ok
            case "Fehlt": return .missing
            default: return nil
            }
        }

        /// Row icon by group and row index (IosElUebersicht.dc.html:78-85): Haustechnik by row, Räume, Dokumente.
        static func icon(group: Int, row: Int) -> ElectricalIcon? {
            switch (group, row) {
            case (0, 0): return .meterCabinet
            case (0, 1): return .smokeAlarm
            case (1, _): return .home
            case (2, _): return .document
            default: return nil
            }
        }
    }

    /// IosElRaum.dc.html carries no platform-specific chrome on iOS (the Android bar fraction is Android only).
    enum ElRaum {}

    enum ElVerteiler {
        /// FI status label to tone (IosElVerteiler.dc.html:99-100): Ausgelöst is ok, Jetzt testen is open.
        static func tone(statusLabel: String) -> FiTone? {
            switch statusLabel {
            case "Ausgelöst": return .ok
            case "Jetzt testen": return .open
            default: return nil
            }
        }
    }

    enum ElRauchmelder {
        // IosElRauchmelder.dc.html:88-89 (the first check is done, the second is open)
        static let checksDone = 1
        // IosElRauchmelder.dc.html:68 (aria-checked="true" on the reminder switch)
        static let reminderInitiallyOn = true

        /// Room status label to status (IosElRauchmelder.dc.html:84, map `R`); nil for a label the prototype does not know.
        static func status(roomLabel: String) -> SmokeRoomStatus? {
            switch roomLabel {
            case "Getestet": return .ok
            case "Jetzt": return .now
            case "Offen": return .open
            default: return nil
            }
        }
    }
}
