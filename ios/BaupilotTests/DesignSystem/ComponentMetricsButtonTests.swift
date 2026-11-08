import SwiftUI
import UIKit
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsButtonTests: XCTestCase {
    private let width: CGFloat = 350

    /// Natural size with no width constraint, so a fixed-width button reports its own width.
    private func naturalSize(of view: some View) -> CGSize {
        UIHostingController(rootView: view.fixedSize())
            .sizeThatFits(in: CGSize(width: CGFloat.greatestFiniteMagnitude,
                                     height: CGFloat.greatestFiniteMagnitude))
    }

    func testButtonsAre50Tall() {
        BaupilotFonts.registerIfNeeded()
        XCTAssertEqual(size(of: PrimaryButton("Weiter", action: nil), width: width).height, 50)
        for textSize in [ButtonSize.large, .medium, .small] {
            XCTAssertEqual(
                size(of: PrimaryButton("Weiter", size: textSize, action: nil), width: width).height, 50)
        }
        XCTAssertEqual(size(of: SecondaryButton("Zurück", action: nil), width: width).height, 50)
        XCTAssertEqual(size(of: OkButton("In Ordnung", action: nil), width: width).height, 50)
        XCTAssertEqual(
            size(of: OkButton("In Ordnung", showsIcon: true, action: nil), width: width).height, 50)
        XCTAssertEqual(size(of: PhotoButton.wide("Manometer fotografieren", action: nil), width: width).height, 50)
    }

    /// IosAbnPruefung:55 is a content-box `<a>`: 50 pt plus a 1 pt border on each side
    /// (reference AbnPruefung_light.png x=190, y 1548-1703 px).
    func testDefectButtonIs52TallWithOuterBorder() {
        BaupilotFonts.registerIfNeeded()
        XCTAssertEqual(size(of: DefectButton("Mangel", action: nil), width: width).height, 52)
        XCTAssertEqual(
            size(of: DefectButton("Mangel", size: .medium, showsIcon: true, action: nil), width: width).height, 52)
    }

    func testSquareButtonsAre50x50() {
        BaupilotFonts.registerIfNeeded()
        XCTAssertEqual(naturalSize(of: SecondaryIconButton(icon: .plusW22, label: "Hinzufügen", action: nil)),
                       CGSize(width: 50, height: 50))
        XCTAssertEqual(naturalSize(of: PhotoButton(label: "Foto aufnehmen", action: nil)),
                       CGSize(width: 50, height: 50))
    }

    /// Login's primary and inverted buttons are 52 tall; the primary default stays 50.
    func testTallPrimaryAndInvertedButtonsAre52() {
        BaupilotFonts.registerIfNeeded()
        XCTAssertEqual(size(of: PrimaryButton("Anmelden", height: .tall, action: nil), width: width).height, 52)
        XCTAssertEqual(size(of: PrimaryButton("Anmelden", height: .regular, action: nil), width: width).height, 50)
        XCTAssertEqual(size(of: InvertedButton("Mit Apple anmelden", icon: .apple, action: nil), width: width).height, 52)
        XCTAssertEqual(size(of: InvertedButton("Mit Apple anmelden", action: nil), width: width).height, 52)
    }

    func testActionRowMinHeight44() {
        BaupilotFonts.registerIfNeeded()
        let row = ActionRow(
            title: "Bodenbau Lorenz", subtitle: "Angefragt am 05.12. · noch kein Angebot",
            actionTitle: "Erinnern", action: nil)
        XCTAssertEqual(size(of: row, width: width).height, 44)
    }
}

