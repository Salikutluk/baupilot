import SwiftUI
import XCTest
@testable import Baupilot

/// T-GAL-1: one golden pair per gallery page. No `expectRedUntil`: `GoldenSnapshot` documents why.
@MainActor
final class GallerySnapshotTests: XCTestCase {
    func testGalleryLight() throws {
        for page in GalleryPage.allCases {
            try GoldenSnapshot.verify(page.framed, name: page.goldenName, mode: .light)
        }
    }

    func testGalleryDark() throws {
        for page in GalleryPage.allCases {
            try GoldenSnapshot.verify(page.framed, name: page.goldenName, mode: .dark)
        }
    }
}
