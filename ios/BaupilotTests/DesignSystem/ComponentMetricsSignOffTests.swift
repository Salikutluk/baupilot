import SwiftUI
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsSignOffTests: XCTestCase {
    private func signed() -> SignatureCard {
        SignatureCard(
            style: .signed(name: "Fensterbau Weber", status: "Unterschrieben 14:32", statusColor: Tokens.oFg),
            caption: "Handwerker", divider: Tokens.border.abnSignatur, padAccessibilityLabel: "Unterschrift Handwerker")
    }

    private func pending() -> SignatureCard {
        SignatureCard(
            style: .pending(
                name: "Max Mustermann", status: "Ausstehend", placeholder: "Hier unterschreiben",
                strokes: .constant(SignaturePad.emptyStrokes)),
            caption: "Bauherr", divider: Tokens.border.abnSignatur, padAccessibilityLabel: "Bauherr")
    }

    /// Both cards stack caption, 64 pad, name and status with padding 12 and gap 6 (IosAbnSignatur:54-67).
    private func stack(pad: CGFloat) -> CGFloat {
        12 + InterMetrics.lineBox(12) + 6 + pad + 6 + InterMetrics.lineBox(13) + 6 + InterMetrics.lineBox(12) + 12
    }

    /// The pending tile adds the dashed border (1 pt both sides) around a 64 high pad: the grid's 154.
    func testPendingSignatureCardIs154Tall() {
        XCTAssertEqual(size(of: pending(), width: 170).height, stack(pad: 64) + 2, accuracy: 1.0 / 3)
        XCTAssertEqual(size(of: pending(), width: 170).height, 154, accuracy: 1.0 / 3)
    }

    /// The signed pad is 64 plus its 1 pt bottom border.
    func testSignedSignatureCardPadIs64PlusBorder() {
        XCTAssertEqual(size(of: signed(), width: 170).height, stack(pad: 65), accuracy: 1.0 / 3)
    }

    /// Grid rows stretch: the signed tile grows to the pending tile's height.
    func testSignatureGridStretchesBothTilesTo154() {
        let grid = HStack(alignment: .top, spacing: 10) { signed(); pending() }
        XCTAssertEqual(size(of: grid, width: 350).height, 154, accuracy: 1.0 / 3)
    }

    func testDotRowIsAtLeast52Tall() {
        let row = DotRow(dot: Tokens.err.abnSignatur, title: "F2 · Flügel fällt zu", subtitle: "Mittel · 2 Fotos", trailing: "bis 15.01.")
        XCTAssertEqual(size(of: row, width: 350).height, 52)
    }

    func testDotRowDividerAddsOne() {
        let row = DotRow(
            dot: Tokens.err.abnSignatur, title: "F2", subtitle: "Mittel", trailing: "bis 15.01.",
            showsDivider: true, divider: Tokens.border.abnSignatur)
        XCTAssertEqual(size(of: row, width: 350).height, 53)
    }

    func testSignaturePadClampsPointsToItsSize() {
        let size = CGSize(width: 146, height: 64)
        XCTAssertEqual(SignaturePad.clamp(CGPoint(x: -5, y: 70), to: size), CGPoint(x: 0, y: 64))
        XCTAssertEqual(SignaturePad.clamp(CGPoint(x: 20, y: 30), to: size), CGPoint(x: 20, y: 30))
    }
}
