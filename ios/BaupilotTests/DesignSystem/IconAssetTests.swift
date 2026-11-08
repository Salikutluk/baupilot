import UIKit
import XCTest
@testable import Baupilot

final class IconAssetTests: XCTestCase {
    func testEveryBaupilotIconLoadsAsTemplateImage() {
        XCTAssertFalse(BaupilotIcon.allCases.isEmpty)
        for icon in BaupilotIcon.allCases {
            let image = UIImage(named: icon.rawValue)
            XCTAssertNotNil(image, "Missing image asset \(icon.rawValue)")
            XCTAssertEqual(image?.renderingMode, .alwaysTemplate, "\(icon.rawValue) must be a template image")
        }
    }
}
