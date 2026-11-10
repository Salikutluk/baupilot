import SwiftUI
import XCTest
@testable import Baupilot

/// One light and one dark test per screen: `AppContent` with fixture data against design/reference.
@MainActor
final class ScreenSnapshotTests: XCTestCase {
    private func verify(
        _ name: String, mode: SnapshotMode, navigation: AppNavigation,
        file: StaticString = #filePath, line: UInt = #line
    ) async throws {
        BaupilotFonts.registerIfNeeded()
        let data = try await AppData.load(from: MockRepository.bundled())
        let reference = try ReferenceSnapshot.loadReference(name: name, mode: mode)
        var state = navigation
        let view = AppContent(navigation: Binding(get: { state }, set: { state = $0 }), data: data)
            .background(Tokens.bg.color)
        try ReferenceSnapshot.assertMatches(
            view, reference: reference, name: name, mode: mode, file: file, line: line)
    }

    func testLoginLight() async throws {
        try await verify("Login", mode: .light, navigation: AppNavigation())
    }

    func testLoginDark() async throws {
        try await verify("Login", mode: .dark, navigation: AppNavigation())
    }

    private var home: AppNavigation {
        var navigation = AppNavigation()
        navigation.logIn()
        return navigation
    }

    func testHomeLight() async throws {
        try await verify("Home", mode: .light, navigation: home)
    }

    func testHomeDark() async throws {
        try await verify("Home", mode: .dark, navigation: home)
    }

    private var schedule: AppNavigation {
        var navigation = home
        navigation.select(.schedule)
        return navigation
    }

    func testZeitplanLight() async throws {
        try await verify("Zeitplan", mode: .light, navigation: schedule)
    }

    func testZeitplanDark() async throws {
        try await verify("Zeitplan", mode: .dark, navigation: schedule)
    }

    private var profile: AppNavigation {
        var navigation = home
        navigation.select(.profile)
        return navigation
    }

    func testProfilLight() async throws {
        try await verify("Profil", mode: .light, navigation: profile)
    }

    func testProfilDark() async throws {
        try await verify("Profil", mode: .dark, navigation: profile)
    }

    /// D7: logged in, Estrich opened from Home (back label "Übersicht").
    private var gewerk: AppNavigation {
        var navigation = home
        navigation.open(.trade(id: "screed"))
        return navigation
    }

    func testGewerkLight() async throws {
        try await verify("Gewerk", mode: .light, navigation: gewerk)
    }

    func testGewerkDark() async throws {
        try await verify("Gewerk", mode: .dark, navigation: gewerk)
    }

    /// D7: logged in, Abnahmen tab (view "Gewerke").
    private var abnGewerke: AppNavigation {
        var navigation = home
        navigation.select(.inspections)
        return navigation
    }

    func testAbnGewerkeLight() async throws {
        try await verify("AbnGewerke", mode: .light, navigation: abnGewerke)
    }

    func testAbnGewerkeDark() async throws {
        try await verify("AbnGewerke", mode: .dark, navigation: abnGewerke)
    }

    /// D7: logged in, Abnahmen tab, Fenster & Türen opened (back label "Abnahmen").
    private var abnElemente: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "windows"))
        return navigation
    }

    func testAbnElementeLight() async throws {
        try await verify("AbnElemente", mode: .light, navigation: abnElemente)
    }

    func testAbnElementeDark() async throws {
        try await verify("AbnElemente", mode: .dark, navigation: abnElemente)
    }

    /// D7: logged in, Abnahmen tab, Fenster & Türen, Prüfschritt F8 opened (back label "Fenster & Türen").
    private var abnPruefung: AppNavigation {
        var navigation = abnElemente
        navigation.open(.inspectionStep(tradeId: "windows", elementId: "F8", step: 3))
        return navigation
    }

    func testAbnPruefungLight() async throws {
        try await verify("AbnPruefung", mode: .light, navigation: abnPruefung)
    }

    func testAbnPruefungDark() async throws {
        try await verify("AbnPruefung", mode: .dark, navigation: abnPruefung)
    }

    /// D7: logged in, Abnahmen tab, Fenster & Türen, Prüfschritt F8, Mangel erfassen opened (pushed, D14).
    private var abnMangel: AppNavigation {
        var navigation = abnPruefung
        navigation.open(.newDefect)
        return navigation
    }

    func testAbnMangelLight() async throws {
        try await verify("AbnMangel", mode: .light, navigation: abnMangel)
    }

    func testAbnMangelDark() async throws {
        try await verify("AbnMangel", mode: .dark, navigation: abnMangel)
    }

    /// D7: logged in, Abnahmen tab, Fenster & Türen opened, then the sign-off (back label "Fenster & Türen").
    private var abnSignatur: AppNavigation {
        var navigation = abnElemente
        navigation.open(.signOff(inspectionId: "windows"))
        return navigation
    }

    func testAbnSignaturLight() async throws {
        try await verify("AbnSignatur", mode: .light, navigation: abnSignatur)
    }

    func testAbnSignaturDark() async throws {
        try await verify("AbnSignatur", mode: .dark, navigation: abnSignatur)
    }

    /// D7: logged in, then the open-defects view of the Abnahmen root (`go(.defects)`).
    private var abnMaengel: AppNavigation {
        var navigation = home
        navigation.go(.defects)
        return navigation
    }

    func testAbnMaengelLight() async throws {
        try await verify("AbnMaengel", mode: .light, navigation: abnMaengel)
    }

    func testAbnMaengelDark() async throws {
        try await verify("AbnMaengel", mode: .dark, navigation: abnMaengel)
    }

    /// D7: logged in, Dokumente tab.
    private var documents: AppNavigation {
        var navigation = home
        navigation.select(.documents)
        return navigation
    }

    func testDokumenteLight() async throws {
        try await verify("Dokumente", mode: .light, navigation: documents)
    }

    func testDokumenteDark() async throws {
        try await verify("Dokumente", mode: .dark, navigation: documents)
    }

    /// D7: logged in, Dokumente tab, Angebot Wagner opened (back label "Dokumente").
    private var docViewer: AppNavigation {
        var navigation = documents
        navigation.open(.document(id: "doc-offer-wagner"))
        return navigation
    }

    func testDokViewerLight() async throws {
        try await verify("DokViewer", mode: .light, navigation: docViewer)
    }

    func testDokViewerDark() async throws {
        try await verify("DokViewer", mode: .dark, navigation: docViewer)
    }

    /// D7: logged in, Estrich opened from Home, Angebote vergleichen opened (back label "Estrich").
    private var vergleich: AppNavigation {
        var navigation = gewerk
        navigation.open(.compare(tradeId: "screed"))
        return navigation
    }

    func testVergleichLight() async throws {
        try await verify("Vergleich", mode: .light, navigation: vergleich)
    }

    func testVergleichDark() async throws {
        try await verify("Vergleich", mode: .dark, navigation: vergleich)
    }

    /// D7: logged in, Mitteilungen opened from Home (back label "Übersicht").
    private var mitteilungen: AppNavigation {
        var navigation = home
        navigation.open(.notifications)
        return navigation
    }

    func testMitteilungenLight() async throws {
        try await verify("Mitteilungen", mode: .light, navigation: mitteilungen)
    }

    func testMitteilungenDark() async throws {
        try await verify("Mitteilungen", mode: .dark, navigation: mitteilungen)
    }

    /// D10: logged in, Abnahmen tab, Erdarbeiten opened (back label "Abnahmen").
    private var erd: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "earthworks"))
        return navigation
    }

    func testErdLight() async throws {
        try await verify("Erd", mode: .light, navigation: erd)
    }

    func testErdDark() async throws {
        try await verify("Erd", mode: .dark, navigation: erd)
    }

    /// D10: logged in, Abnahmen tab, Rohbau opened (back label "Abnahmen").
    private var rohbau: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "shell"))
        return navigation
    }

    func testRohbauLight() async throws {
        try await verify("Rohbau", mode: .light, navigation: rohbau)
    }

    func testRohbauDark() async throws {
        try await verify("Rohbau", mode: .dark, navigation: rohbau)
    }

    /// D10: Rohbau opened, then Maße & Lot (back label "Rohbau"); initial ViewModel state.
    private var rohbauMasse: AppNavigation {
        var navigation = rohbau
        navigation.open(.inspectionCheck(tradeId: "shell", check: "measure"))
        return navigation
    }

    func testRohbauMasseLight() async throws {
        try await verify("RohbauMasse", mode: .light, navigation: rohbauMasse)
    }

    func testRohbauMasseDark() async throws {
        try await verify("RohbauMasse", mode: .dark, navigation: rohbauMasse)
    }

    /// D10: logged in, Abnahmen tab, Zimmerer opened (back label "Abnahmen").
    private var zimmerer: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "carpentry"))
        return navigation
    }

    func testZimmererLight() async throws {
        try await verify("Zimmerer", mode: .light, navigation: zimmerer)
    }

    func testZimmererDark() async throws {
        try await verify("Zimmerer", mode: .dark, navigation: zimmerer)
    }

    /// D10: logged in, Abnahmen tab, Elektro opened (back label "Abnahmen").
    private var elUebersicht: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "electrical"))
        return navigation
    }

    func testElUebersichtLight() async throws {
        try await verify("ElUebersicht", mode: .light, navigation: elUebersicht)
    }

    func testElUebersichtDark() async throws {
        try await verify("ElUebersicht", mode: .dark, navigation: elUebersicht)
    }

    /// D10: Elektro opened, then Raumprüfung Küche (back label "Elektro").
    private var elRaum: AppNavigation {
        var navigation = elUebersicht
        navigation.open(.inspectionRoom(tradeId: "electrical", roomId: "kitchen"))
        return navigation
    }

    func testElRaumLight() async throws {
        try await verify("ElRaum", mode: .light, navigation: elRaum)
    }

    func testElRaumDark() async throws {
        try await verify("ElRaum", mode: .dark, navigation: elRaum)
    }

    /// D10: Elektro opened, then Zählerschrank & FI-Test (back label "Elektro").
    private var elVerteiler: AppNavigation {
        var navigation = elUebersicht
        navigation.open(.inspectionCheck(tradeId: "electrical", check: "panel"))
        return navigation
    }

    func testElVerteilerLight() async throws {
        try await verify("ElVerteiler", mode: .light, navigation: elVerteiler)
    }

    func testElVerteilerDark() async throws {
        try await verify("ElVerteiler", mode: .dark, navigation: elVerteiler)
    }

    /// D10: Elektro opened, then Rauchwarnmelder (back label "Elektro").
    private var elRauchmelder: AppNavigation {
        var navigation = elUebersicht
        navigation.open(.inspectionCheck(tradeId: "electrical", check: "smoke"))
        return navigation
    }

    func testElRauchmelderLight() async throws {
        try await verify("ElRauchmelder", mode: .light, navigation: elRauchmelder)
    }

    func testElRauchmelderDark() async throws {
        try await verify("ElRauchmelder", mode: .dark, navigation: elRauchmelder)
    }

    /// D10: logged in, Abnahmen tab, Sanitär & Heizung opened (back label "Abnahmen").
    private var sanUebersicht: AppNavigation {
        var navigation = abnGewerke
        navigation.open(.inspection(tradeId: "plumbing"))
        return navigation
    }

    func testSanUebersichtLight() async throws {
        try await verify("SanUebersicht", mode: .light, navigation: sanUebersicht)
    }

    func testSanUebersichtDark() async throws {
        try await verify("SanUebersicht", mode: .dark, navigation: sanUebersicht)
    }

    /// D10: Sanitär & Heizung opened, then Gefälle Abwasser A3 (back label "Sanitär"), calculator in its initial state.
    private var sanGefaelle: AppNavigation {
        var navigation = sanUebersicht
        navigation.open(.inspectionCheckItem(tradeId: "plumbing", check: "slope", item: "A3"))
        return navigation
    }

    func testSanGefaelleLight() async throws {
        try await verify("SanGefaelle", mode: .light, navigation: sanGefaelle)
    }

    func testSanGefaelleDark() async throws {
        try await verify("SanGefaelle", mode: .dark, navigation: sanGefaelle)
    }
}
