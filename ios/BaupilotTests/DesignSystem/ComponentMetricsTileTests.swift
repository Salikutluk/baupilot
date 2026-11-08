import SwiftUI
import XCTest

@testable import Baupilot

@MainActor
final class ComponentMetricsTileTests: XCTestCase {
    override func setUp() {
        super.setUp()
        BaupilotFonts.registerIfNeeded()
    }

    func testPhotoTileHeights() {
        func tile(_ height: PhotoTile.Height) -> PhotoTile {
            PhotoTile(
                kind: .photo(Image(systemName: "photo"), caption: "Foto oben", checked: true),
                height: height)
        }
        XCTAssertEqual(size(of: tile(.h84), width: 80).height, 84)
        XCTAssertEqual(size(of: tile(.h76), width: 80).height, 76)
    }
}
