import SwiftUI
import XCTest
@testable import Baupilot

final class TokensTests: XCTestCase {
    func testBarIsTranslucentWhite() {
        XCTAssertEqual(Tokens.bar.light, 0xFFFFFFF2)
        let resolved = UIColor(Tokens.bar.color(.light))
        var alpha: CGFloat = 0
        XCTAssertTrue(resolved.getRed(nil, green: nil, blue: nil, alpha: &alpha))
        XCTAssertEqual(Double(alpha), 242.0 / 255.0, accuracy: 0.001)
    }

    func testTrackPerScreenVariantDiffersOnlyInDark() {
        XCTAssertEqual(Tokens.track.abnPruefung.dark, 0x4A5057FF)
        XCTAssertEqual(Tokens.track.dark, 0x3A3F45FF)
        XCTAssertEqual(Tokens.track.abnPruefung.light, Tokens.track.light)
    }

    func testLayoutConstants() {
        XCTAssertEqual(Tokens.Layout.screen, CGSize(width: 390, height: 844))
        XCTAssertEqual(Tokens.Layout.actionBarPadding.bottom, 34)
    }
}
