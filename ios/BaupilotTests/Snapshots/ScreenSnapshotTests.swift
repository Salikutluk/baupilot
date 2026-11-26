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
        try await verify("Login", mode: .light, navigation: DebugScreens.navigation(named: "Login")!)
    }

    func testLoginDark() async throws {
        try await verify("Login", mode: .dark, navigation: DebugScreens.navigation(named: "Login")!)
    }

    func testHomeLight() async throws {
        try await verify("Home", mode: .light, navigation: DebugScreens.navigation(named: "Home")!)
    }

    func testHomeDark() async throws {
        try await verify("Home", mode: .dark, navigation: DebugScreens.navigation(named: "Home")!)
    }

    func testZeitplanLight() async throws {
        try await verify("Zeitplan", mode: .light, navigation: DebugScreens.navigation(named: "Zeitplan")!)
    }

    func testZeitplanDark() async throws {
        try await verify("Zeitplan", mode: .dark, navigation: DebugScreens.navigation(named: "Zeitplan")!)
    }

    func testProfilLight() async throws {
        try await verify("Profil", mode: .light, navigation: DebugScreens.navigation(named: "Profil")!)
    }

    func testProfilDark() async throws {
        try await verify("Profil", mode: .dark, navigation: DebugScreens.navigation(named: "Profil")!)
    }

    func testGewerkLight() async throws {
        try await verify("Gewerk", mode: .light, navigation: DebugScreens.navigation(named: "Gewerk")!)
    }

    func testGewerkDark() async throws {
        try await verify("Gewerk", mode: .dark, navigation: DebugScreens.navigation(named: "Gewerk")!)
    }

    func testAbnGewerkeLight() async throws {
        try await verify("AbnGewerke", mode: .light, navigation: DebugScreens.navigation(named: "AbnGewerke")!)
    }

    func testAbnGewerkeDark() async throws {
        try await verify("AbnGewerke", mode: .dark, navigation: DebugScreens.navigation(named: "AbnGewerke")!)
    }

    func testAbnElementeLight() async throws {
        try await verify("AbnElemente", mode: .light, navigation: DebugScreens.navigation(named: "AbnElemente")!)
    }

    func testAbnElementeDark() async throws {
        try await verify("AbnElemente", mode: .dark, navigation: DebugScreens.navigation(named: "AbnElemente")!)
    }

    func testAbnPruefungLight() async throws {
        try await verify("AbnPruefung", mode: .light, navigation: DebugScreens.navigation(named: "AbnPruefung")!)
    }

    func testAbnPruefungDark() async throws {
        try await verify("AbnPruefung", mode: .dark, navigation: DebugScreens.navigation(named: "AbnPruefung")!)
    }

    func testAbnMangelLight() async throws {
        try await verify("AbnMangel", mode: .light, navigation: DebugScreens.navigation(named: "AbnMangel")!)
    }

    func testAbnMangelDark() async throws {
        try await verify("AbnMangel", mode: .dark, navigation: DebugScreens.navigation(named: "AbnMangel")!)
    }

    func testAbnSignaturLight() async throws {
        try await verify("AbnSignatur", mode: .light, navigation: DebugScreens.navigation(named: "AbnSignatur")!)
    }

    func testAbnSignaturDark() async throws {
        try await verify("AbnSignatur", mode: .dark, navigation: DebugScreens.navigation(named: "AbnSignatur")!)
    }

    func testAbnMaengelLight() async throws {
        try await verify("AbnMaengel", mode: .light, navigation: DebugScreens.navigation(named: "AbnMaengel")!)
    }

    func testAbnMaengelDark() async throws {
        try await verify("AbnMaengel", mode: .dark, navigation: DebugScreens.navigation(named: "AbnMaengel")!)
    }

    func testDokumenteLight() async throws {
        try await verify("Dokumente", mode: .light, navigation: DebugScreens.navigation(named: "Dokumente")!)
    }

    func testDokumenteDark() async throws {
        try await verify("Dokumente", mode: .dark, navigation: DebugScreens.navigation(named: "Dokumente")!)
    }

    func testDokViewerLight() async throws {
        try await verify("DokViewer", mode: .light, navigation: DebugScreens.navigation(named: "DokViewer")!)
    }

    func testDokViewerDark() async throws {
        try await verify("DokViewer", mode: .dark, navigation: DebugScreens.navigation(named: "DokViewer")!)
    }

    func testVergleichLight() async throws {
        try await verify("Vergleich", mode: .light, navigation: DebugScreens.navigation(named: "Vergleich")!)
    }

    func testVergleichDark() async throws {
        try await verify("Vergleich", mode: .dark, navigation: DebugScreens.navigation(named: "Vergleich")!)
    }

    func testMitteilungenLight() async throws {
        try await verify("Mitteilungen", mode: .light, navigation: DebugScreens.navigation(named: "Mitteilungen")!)
    }

    func testMitteilungenDark() async throws {
        try await verify("Mitteilungen", mode: .dark, navigation: DebugScreens.navigation(named: "Mitteilungen")!)
    }

    func testErdLight() async throws {
        try await verify("Erd", mode: .light, navigation: DebugScreens.navigation(named: "Erd")!)
    }

    func testErdDark() async throws {
        try await verify("Erd", mode: .dark, navigation: DebugScreens.navigation(named: "Erd")!)
    }

    func testRohbauLight() async throws {
        try await verify("Rohbau", mode: .light, navigation: DebugScreens.navigation(named: "Rohbau")!)
    }

    func testRohbauDark() async throws {
        try await verify("Rohbau", mode: .dark, navigation: DebugScreens.navigation(named: "Rohbau")!)
    }

    func testRohbauMasseLight() async throws {
        try await verify("RohbauMasse", mode: .light, navigation: DebugScreens.navigation(named: "RohbauMasse")!)
    }

    func testRohbauMasseDark() async throws {
        try await verify("RohbauMasse", mode: .dark, navigation: DebugScreens.navigation(named: "RohbauMasse")!)
    }

    func testZimmererLight() async throws {
        try await verify("Zimmerer", mode: .light, navigation: DebugScreens.navigation(named: "Zimmerer")!)
    }

    func testZimmererDark() async throws {
        try await verify("Zimmerer", mode: .dark, navigation: DebugScreens.navigation(named: "Zimmerer")!)
    }

    func testElUebersichtLight() async throws {
        try await verify("ElUebersicht", mode: .light, navigation: DebugScreens.navigation(named: "ElUebersicht")!)
    }

    func testElUebersichtDark() async throws {
        try await verify("ElUebersicht", mode: .dark, navigation: DebugScreens.navigation(named: "ElUebersicht")!)
    }

    func testElRaumLight() async throws {
        try await verify("ElRaum", mode: .light, navigation: DebugScreens.navigation(named: "ElRaum")!)
    }

    func testElRaumDark() async throws {
        try await verify("ElRaum", mode: .dark, navigation: DebugScreens.navigation(named: "ElRaum")!)
    }

    func testElVerteilerLight() async throws {
        try await verify("ElVerteiler", mode: .light, navigation: DebugScreens.navigation(named: "ElVerteiler")!)
    }

    func testElVerteilerDark() async throws {
        try await verify("ElVerteiler", mode: .dark, navigation: DebugScreens.navigation(named: "ElVerteiler")!)
    }

    func testElRauchmelderLight() async throws {
        try await verify("ElRauchmelder", mode: .light, navigation: DebugScreens.navigation(named: "ElRauchmelder")!)
    }

    func testElRauchmelderDark() async throws {
        try await verify("ElRauchmelder", mode: .dark, navigation: DebugScreens.navigation(named: "ElRauchmelder")!)
    }

    func testSanUebersichtLight() async throws {
        try await verify("SanUebersicht", mode: .light, navigation: DebugScreens.navigation(named: "SanUebersicht")!)
    }

    func testSanUebersichtDark() async throws {
        try await verify("SanUebersicht", mode: .dark, navigation: DebugScreens.navigation(named: "SanUebersicht")!)
    }

    func testSanGefaelleLight() async throws {
        try await verify("SanGefaelle", mode: .light, navigation: DebugScreens.navigation(named: "SanGefaelle")!)
    }

    func testSanGefaelleDark() async throws {
        try await verify("SanGefaelle", mode: .dark, navigation: DebugScreens.navigation(named: "SanGefaelle")!)
    }

    func testSanDruckLight() async throws {
        try await verify("SanDruck", mode: .light, navigation: DebugScreens.navigation(named: "SanDruck")!)
    }

    func testSanDruckDark() async throws {
        try await verify("SanDruck", mode: .dark, navigation: DebugScreens.navigation(named: "SanDruck")!)
    }

    func testSanBadLight() async throws {
        try await verify("SanBad", mode: .light, navigation: DebugScreens.navigation(named: "SanBad")!)
    }

    func testSanBadDark() async throws {
        try await verify("SanBad", mode: .dark, navigation: DebugScreens.navigation(named: "SanBad")!)
    }

    func testEsUebersichtLight() async throws {
        try await verify("EsUebersicht", mode: .light, navigation: DebugScreens.navigation(named: "EsUebersicht")!)
    }

    func testEsUebersichtDark() async throws {
        try await verify("EsUebersicht", mode: .dark, navigation: DebugScreens.navigation(named: "EsUebersicht")!)
    }

    func testFlUebersichtLight() async throws {
        try await verify("FlUebersicht", mode: .light, navigation: DebugScreens.navigation(named: "FlUebersicht")!)
    }

    func testFlUebersichtDark() async throws {
        try await verify("FlUebersicht", mode: .dark, navigation: DebugScreens.navigation(named: "FlUebersicht")!)
    }

    func testEsEbenheitLight() async throws {
        try await verify("EsEbenheit", mode: .light, navigation: DebugScreens.navigation(named: "EsEbenheit")!)
    }

    func testEsEbenheitDark() async throws {
        try await verify("EsEbenheit", mode: .dark, navigation: DebugScreens.navigation(named: "EsEbenheit")!)
    }

    func testEsBelegreifeLight() async throws {
        try await verify("EsBelegreife", mode: .light, navigation: DebugScreens.navigation(named: "EsBelegreife")!)
    }

    func testEsBelegreifeDark() async throws {
        try await verify("EsBelegreife", mode: .dark, navigation: DebugScreens.navigation(named: "EsBelegreife")!)
    }

    func testFlBadLight() async throws {
        try await verify("FlBad", mode: .light, navigation: DebugScreens.navigation(named: "FlBad")!)
    }

    func testFlBadDark() async throws {
        try await verify("FlBad", mode: .dark, navigation: DebugScreens.navigation(named: "FlBad")!)
    }

    func testMaUebersichtLight() async throws {
        try await verify("MaUebersicht", mode: .light, navigation: DebugScreens.navigation(named: "MaUebersicht")!)
    }

    func testMaUebersichtDark() async throws {
        try await verify("MaUebersicht", mode: .dark, navigation: DebugScreens.navigation(named: "MaUebersicht")!)
    }

    func testMaRaumLight() async throws {
        try await verify("MaRaum", mode: .light, navigation: DebugScreens.navigation(named: "MaRaum")!)
    }

    func testMaRaumDark() async throws {
        try await verify("MaRaum", mode: .dark, navigation: DebugScreens.navigation(named: "MaRaum")!)
    }
}
