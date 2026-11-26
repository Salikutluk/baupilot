import XCTest
@testable import Baupilot

@MainActor
final class DebugScreensTests: XCTestCase {
    private func screensMdNames() throws -> [String] {
        let url = ReferenceSnapshot.repoRootURL.appendingPathComponent("design/screens.md")
        let text = try String(contentsOf: url, encoding: .utf8)
        let regex = try NSRegularExpression(pattern: #"reference/\{ios,android\}/(\w+)_\{light,dark\}\.png"#)
        return regex.matches(in: text, range: NSRange(text.startIndex..., in: text)).compactMap {
            Range($0.range(at: 1), in: text).map { String(text[$0]) }
        }
    }

    func testHas34NamesInScreensMdOrder() throws {
        let ordered = try screensMdNames()
        XCTAssertEqual(ordered.count, 34)
        let directory = ReferenceSnapshot.repoRootURL.appendingPathComponent("design/reference/ios")
        let stems = Set(
            try FileManager.default.contentsOfDirectory(atPath: directory.path)
                .filter { $0.hasSuffix("_light.png") }
                .map { String($0.dropLast("_light.png".count)) })
        XCTAssertEqual(stems, Set(ordered))
        XCTAssertEqual(DebugScreens.all.map(\.name), ordered)
        XCTAssertEqual(Set(DebugScreens.all.map(\.name)).count, 34)
    }

    func testUnknownLaunchNameIsNil() throws {
        let suite = "DebugScreensTests-\(UUID().uuidString)"
        let defaults = try XCTUnwrap(UserDefaults(suiteName: suite))
        defer { defaults.removePersistentDomain(forName: suite) }
        XCTAssertNil(DebugScreens.launchNavigation(defaults))
        defaults.set("NoSuchScreen", forKey: "BaupilotScreen")
        XCTAssertNil(DebugScreens.launchNavigation(defaults))
        XCTAssertNil(DebugScreens.navigation(named: "NoSuchScreen"))
    }

    func testLaunchNavigationIsLoggedInExceptLogin() throws {
        let suite = "DebugScreensTests-\(UUID().uuidString)"
        let defaults = try XCTUnwrap(UserDefaults(suiteName: suite))
        defer { defaults.removePersistentDomain(forName: suite) }
        for (name, navigation) in DebugScreens.all {
            defaults.set(name, forKey: "BaupilotScreen")
            let launched = try XCTUnwrap(DebugScreens.launchNavigation(defaults), name)
            XCTAssertEqual(launched, navigation, name)
            XCTAssertEqual(launched.isLoggedIn, name != "Login", name)
        }
    }
}
