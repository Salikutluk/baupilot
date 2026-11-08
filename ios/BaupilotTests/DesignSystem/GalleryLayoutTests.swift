import SwiftUI
import XCTest
@testable import Baupilot

@MainActor
final class GalleryLayoutTests: XCTestCase {
    /// T-GAL-2: every gallery page, padding included, fits the 390 x 844 golden frame.
    func testEveryGalleryPageFitsTheScreen() {
        for page in GalleryPage.allCases {
            let height = size(of: page.content, width: 390).height
            XCTAssertLessThanOrEqual(
                height, 844, "Galerieseite \(page.goldenName) ist \(height) pt hoch, erlaubt sind 844 pt")
        }
    }
}
