import SwiftUI
import UIKit
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsStructureTests: XCTestCase {
    func testBackBarIs44Tall() {
        let link = BackBar(label: "Zurück", action: nil)
        XCTAssertEqual(size(of: link, width: 350).height, 44)

        let counter = BackBar(label: "Zurück", action: nil, trailing: .counter("2 / 5"))
        XCTAssertEqual(size(of: counter, width: 350).height, 44)

        let modal = BackBar.modal(cancel: "Abbrechen", title: "Mangel erfassen", confirm: "Sichern")
        XCTAssertEqual(size(of: modal, width: 350).height, 44)
    }

    /// The trailing buttons are measured through `LargeTitleHeader.TrailingView`, the exact view
    /// the header places in its row, at their intrinsic size (`size(of:width:)` pins the width,
    /// so it cannot report a button's own width), so the measured frame is the real button frame.
    func testHeaderButtonsAre44x44() {
        let bell = LargeTitleHeader.TrailingView(
            trailing: .bell(unread: true, label: "Mitteilungen, 2 neu", action: nil))
        let add = LargeTitleHeader.TrailingView(
            trailing: .add(label: "Dokument hinzufügen", action: nil))
        for view in [AnyView(bell), AnyView(add)] {
            let hosting = UIHostingController(rootView: view.fixedSize())
            let measured = hosting.sizeThatFits(
                in: CGSize(width: CGFloat.greatestFiniteMagnitude, height: .greatestFiniteMagnitude))
            XCTAssertEqual(measured.width, 44)
            XCTAssertEqual(measured.height, 44)
        }
    }
}
