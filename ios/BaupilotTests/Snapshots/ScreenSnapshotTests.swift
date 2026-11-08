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
}
