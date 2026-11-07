import UIKit
import XCTest
@testable import Baupilot

final class FontTests: XCTestCase {
    func testInterFontsAreAvailableAfterRegistration() {
        BaupilotFonts.registerIfNeeded()
        for name in BaupilotFonts.postScriptNames {
            XCTAssertNotNil(UIFont(name: name, size: 16), "Missing font \(name)")
        }
    }
}
