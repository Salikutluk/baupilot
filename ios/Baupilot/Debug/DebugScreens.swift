#if DEBUG
import Foundation

/// The 34 reference screens (design/screens.md order), each with the navigation its snapshot uses.
/// Shared by `ScreenSnapshotTests`, the unit tests and, through the launch argument
/// `-BaupilotScreen <Name>`, the UI tests. DEBUG only: nothing here compiles into Release.
enum DebugScreens {
    /// The 34 reference names in design/screens.md order, each with the navigation its snapshot uses.
    static let all: [(name: String, navigation: AppNavigation)] = [
        (name: "Login", navigation: AppNavigation()),
        (name: "Home", navigation: home),
        (name: "Zeitplan", navigation: schedule),
        (name: "Profil", navigation: profile),
        (name: "Gewerk", navigation: gewerk),
        (name: "Dokumente", navigation: documents),
        (name: "DokViewer", navigation: docViewer),
        (name: "Vergleich", navigation: vergleich),
        (name: "Mitteilungen", navigation: mitteilungen),
        (name: "AbnGewerke", navigation: abnGewerke),
        (name: "AbnElemente", navigation: abnElemente),
        (name: "AbnPruefung", navigation: abnPruefung),
        (name: "AbnMangel", navigation: abnMangel),
        (name: "AbnSignatur", navigation: abnSignatur),
        (name: "AbnMaengel", navigation: abnMaengel),
        (name: "Erd", navigation: erd),
        (name: "Rohbau", navigation: rohbau),
        (name: "RohbauMasse", navigation: rohbauMasse),
        (name: "Zimmerer", navigation: zimmerer),
        (name: "ElUebersicht", navigation: elUebersicht),
        (name: "ElRaum", navigation: elRaum),
        (name: "ElVerteiler", navigation: elVerteiler),
        (name: "ElRauchmelder", navigation: elRauchmelder),
        (name: "SanUebersicht", navigation: sanUebersicht),
        (name: "SanGefaelle", navigation: sanGefaelle),
        (name: "SanDruck", navigation: sanDruck),
        (name: "SanBad", navigation: sanBad),
        (name: "EsUebersicht", navigation: esUebersicht),
        (name: "EsEbenheit", navigation: esEbenheit),
        (name: "EsBelegreife", navigation: esBelegreife),
        (name: "FlUebersicht", navigation: flUebersicht),
        (name: "FlBad", navigation: flBad),
        (name: "MaUebersicht", navigation: maUebersicht),
        (name: "MaRaum", navigation: maRaum),
    ]

    static func navigation(named name: String) -> AppNavigation? {
        all.first { $0.name == name }?.navigation
    }

    /// Reads the user default "BaupilotScreen" (set by the launch argument `-BaupilotScreen <Name>`).
    /// Nil when unset or unknown: the app then starts normally (login).
    static func launchNavigation(_ defaults: UserDefaults = .standard) -> AppNavigation? {
        guard let name = defaults.string(forKey: "BaupilotScreen") else { return nil }
        return navigation(named: name)
    }

    private static var home: AppNavigation {
        var navigation = AppNavigation()
        navigation.logIn()
        return navigation
    }

    private static var schedule: AppNavigation {
        var navigation = home
        navigation.select(.schedule)
        return navigation
    }

    private static var profile: AppNavigation {
        var navigation = home
        navigation.select(.profile)
        return navigation
    }

    /// D7: logged in, Estrich opened from Home (back label "Übersicht").
    private static var gewerk: AppNavigation {
        var navigation = home
        navigation.open(.trade(id: "screed"))
        return navigation
    }

    /// D7: logged in, Abnahmen tab (view "Gewerke").
    private static var abnGewerke: AppNavigation {
        var navigation = home
        navigation.select(.inspections)
        return navigation
    }

    /// D7: logged in, Abnahmen tab, Fenster & Türen opened (back label "Abnahmen").
    private static var abnElemente: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "windows"))
        return navigation
    }

    /// D7: logged in, Abnahmen tab, Fenster & Türen, Prüfschritt F8 opened (back label "Fenster & Türen").
    private static var abnPruefung: AppNavigation {
        var navigation = abnElemente
        navigation.open(.inspectionStep(tradeId: "windows", elementId: "F8", step: 3))
        return navigation
    }

    /// D7: logged in, Abnahmen tab, Fenster & Türen, Prüfschritt F8, Mangel erfassen opened (pushed, D14).
    private static var abnMangel: AppNavigation {
        var navigation = abnPruefung
        navigation.open(.newDefect)
        return navigation
    }

    /// D7: logged in, Abnahmen tab, Fenster & Türen opened, then the sign-off (back label "Fenster & Türen").
    private static var abnSignatur: AppNavigation {
        var navigation = abnElemente
        navigation.open(.signOff(inspectionId: "windows"))
        return navigation
    }

    /// D7: logged in, then the open-defects view of the Abnahmen root (`go(.defects)`).
    private static var abnMaengel: AppNavigation {
        var navigation = home
        navigation.go(.defects)
        return navigation
    }

    /// D7: logged in, Dokumente tab.
    private static var documents: AppNavigation {
        var navigation = home
        navigation.select(.documents)
        return navigation
    }

    /// D7: logged in, Dokumente tab, Angebot Wagner opened (back label "Dokumente").
    private static var docViewer: AppNavigation {
        var navigation = documents
        navigation.open(.document(id: "doc-offer-wagner"))
        return navigation
    }

    /// D7: logged in, Estrich opened from Home, Angebote vergleichen opened (back label "Estrich").
    private static var vergleich: AppNavigation {
        var navigation = gewerk
        navigation.open(.compare(tradeId: "screed"))
        return navigation
    }

    /// D7: logged in, Mitteilungen opened from Home (back label "Übersicht").
    private static var mitteilungen: AppNavigation {
        var navigation = home
        navigation.open(.notifications)
        return navigation
    }

    /// D10: logged in, Abnahmen tab, Erdarbeiten opened (back label "Abnahmen").
    private static var erd: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "earthworks"))
        return navigation
    }

    /// D10: logged in, Abnahmen tab, Rohbau opened (back label "Abnahmen").
    private static var rohbau: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "shell"))
        return navigation
    }

    /// D10: Rohbau opened, then Maße & Lot (back label "Rohbau"); initial ViewModel state.
    private static var rohbauMasse: AppNavigation {
        var navigation = rohbau
        navigation.open(.inspectionCheck(tradeId: "shell", check: "measure"))
        return navigation
    }

    /// D10: logged in, Abnahmen tab, Zimmerer opened (back label "Abnahmen").
    private static var zimmerer: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "carpentry"))
        return navigation
    }

    /// D10: logged in, Abnahmen tab, Elektro opened (back label "Abnahmen").
    private static var elUebersicht: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "electrical"))
        return navigation
    }

    /// D10: Elektro opened, then Raumprüfung Küche (back label "Elektro").
    private static var elRaum: AppNavigation {
        var navigation = elUebersicht
        navigation.open(.inspectionRoom(tradeId: "electrical", roomId: "kitchen"))
        return navigation
    }

    /// D10: Elektro opened, then Zählerschrank & FI-Test (back label "Elektro").
    private static var elVerteiler: AppNavigation {
        var navigation = elUebersicht
        navigation.open(.inspectionCheck(tradeId: "electrical", check: "panel"))
        return navigation
    }

    /// D10: Elektro opened, then Rauchwarnmelder (back label "Elektro").
    private static var elRauchmelder: AppNavigation {
        var navigation = elUebersicht
        navigation.open(.inspectionCheck(tradeId: "electrical", check: "smoke"))
        return navigation
    }

    /// D10: logged in, Abnahmen tab, Sanitär & Heizung opened (back label "Abnahmen").
    private static var sanUebersicht: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "plumbing"))
        return navigation
    }

    /// D10: Sanitär & Heizung opened, then Gefälle Abwasser A3 (back label "Sanitär"), calculator in its initial state.
    private static var sanGefaelle: AppNavigation {
        var navigation = sanUebersicht
        navigation.open(.inspectionCheckItem(tradeId: "plumbing", check: "slope", item: "A3"))
        return navigation
    }

    /// D10: Sanitär & Heizung opened, then Druckprobe (back label "Sanitär"), the end field empty.
    private static var sanDruck: AppNavigation {
        var navigation = sanUebersicht
        navigation.open(.inspectionCheck(tradeId: "plumbing", check: "pressure"))
        return navigation
    }

    /// D10: Sanitär & Heizung opened, then Bad OG (back label "Sanitär").
    private static var sanBad: AppNavigation {
        var navigation = sanUebersicht
        navigation.open(.inspectionRoom(tradeId: "plumbing", roomId: "bathUpper"))
        return navigation
    }

    /// D10: logged in, Abnahmen tab, Estrich opened (back label "Abnahmen").
    private static var esUebersicht: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "screed"))
        return navigation
    }

    /// D10: logged in, Abnahmen tab, Fliesen opened (back label "Abnahmen").
    private static var flUebersicht: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "tiles"))
        return navigation
    }

    /// D10: Estrich opened from Abnahmen, then Ebenheit (back label "Estrich").
    private static var esEbenheit: AppNavigation {
        var navigation = esUebersicht
        navigation.open(.inspectionCheck(tradeId: "screed", check: "flatness"))
        return navigation
    }

    /// D10: Estrich opened from Abnahmen, then Aufheizen & Belegreife (back label "Estrich").
    private static var esBelegreife: AppNavigation {
        var navigation = esUebersicht
        navigation.open(.inspectionCheck(tradeId: "screed", check: "readiness"))
        return navigation
    }

    /// D10: Fliesen opened from Abnahmen, then Bad OG (back label "Fliesen").
    private static var flBad: AppNavigation {
        var navigation = flUebersicht
        navigation.open(.inspectionRoom(tradeId: "tiles", roomId: "bath"))
        return navigation
    }

    /// D10: logged in, Abnahmen tab, Maler opened (back label "Abnahmen").
    private static var maUebersicht: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "painting"))
        return navigation
    }

    /// D10: Maler opened from Abnahmen, then Wohnen / Essen (back label "Maler").
    private static var maRaum: AppNavigation {
        var navigation = maUebersicht
        navigation.open(.inspectionRoom(tradeId: "painting", roomId: "living"))
        return navigation
    }
}
#endif
