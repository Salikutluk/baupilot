import UIKit
import XCTest
@testable import Baupilot

final class TypographyTests: XCTestCase {
    func testInterWeightsResolveToRegisteredFonts() {
        BaupilotFonts.registerIfNeeded()
        let weights: [InterWeight] = [.regular, .medium, .semibold, .bold]
        for weight in weights {
            XCTAssertNotNil(
                UIFont(name: weight.postScriptName, size: 16), "Missing font for \(weight)")
        }
        XCTAssertEqual(Set(weights.map(\.postScriptName)), Set(BaupilotFonts.postScriptNames))
    }
}
