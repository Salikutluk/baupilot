import SwiftUI
import XCTest
@testable import Baupilot

@MainActor
final class LoginExampleSnapshotTests: XCTestCase {
    func testLoginLightAgainstReference() throws {
        let reference = try ReferenceSnapshot.loadReference(name: "Login", mode: .light)
        ReferenceSnapshot.expectRedUntil("M2")
        try ReferenceSnapshot.assertMatches(
            RootView(), reference: reference, name: "Login", mode: .light, writeArtifact: true)
    }
}
