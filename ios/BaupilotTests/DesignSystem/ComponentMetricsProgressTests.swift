import SwiftUI
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsProgressTests: XCTestCase {
    func testProgressBarHeights() {
        XCTAssertEqual(size(of: ProgressBar(value: 0.5, style: .phase), width: 200).height, 8)
        XCTAssertEqual(size(of: ProgressBar(value: 0.5, style: .card), width: 200).height, 4)
    }
}
