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
}
