import SwiftUI
import XCTest
@testable import Baupilot

final class TokensTests: XCTestCase {
    func testBarIsTranslucentWhite() {
        XCTAssertEqual(Tokens.bar.light, 0xFFFFFFF2) // lint-hex: allow token value assertion
        let resolved = UIColor(Tokens.bar.color(.light))
        var alpha: CGFloat = 0
        XCTAssertTrue(resolved.getRed(nil, green: nil, blue: nil, alpha: &alpha))
        XCTAssertEqual(Double(alpha), 242.0 / 255.0, accuracy: 0.001)
    }

    func testTrackPerScreenVariantDiffersOnlyInDark() {
        XCTAssertEqual(Tokens.track.abnPruefung.dark, 0x4A5057FF) // lint-hex: allow token value assertion
        XCTAssertEqual(Tokens.track.dark, 0x3A3F45FF) // lint-hex: allow token value assertion
        XCTAssertEqual(Tokens.track.abnPruefung.light, Tokens.track.light)
    }

    func testSupplementTokensExist() {
        XCTAssertEqual(Tokens.knob.light, Tokens.knob.dark)
        XCTAssertEqual(Tokens.placeholder.light, Tokens.placeholder.dark)
    }

    func testDocumentPageTokensAreThemeIndependent() {
        let pairs = [
            Tokens.docPaper, Tokens.docInk, Tokens.docBrand, Tokens.docMeta,
            Tokens.docPos, Tokens.docRule, Tokens.docRuleLight, Tokens.docShadow,
        ]
        for pair in pairs { XCTAssertEqual(pair.light, pair.dark) }
        XCTAssertEqual(Tokens.docPaper.light, 0xFFFFFFFF) // lint-hex: allow token value assertion
        XCTAssertEqual(Tokens.docShadow.light & 0xFF, 0x2E) // lint-hex: allow token value assertion
    }

    func testLayoutConstants() {
        XCTAssertEqual(Tokens.Layout.screen, CGSize(width: 390, height: 844))
        XCTAssertEqual(Tokens.Layout.actionBarPadding.bottom, 34)
    }
}
