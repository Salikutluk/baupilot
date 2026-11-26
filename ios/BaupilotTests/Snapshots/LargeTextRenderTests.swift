import SwiftUI
import XCTest
@testable import Baupilot

/// M7 D15: the 34 screens rendered at Dynamic Type XL and xxxL (light) as review artifacts in
/// build/snapshots/ios-large. Nothing is compared; only the size (390 x 844 @3) is asserted.
@MainActor
final class LargeTextRenderTests: XCTestCase {
    private func renderAll(_ contentSize: UIContentSizeCategory, suffix: String) async throws {
        BaupilotFonts.registerIfNeeded()
        let data = try await AppData.load(from: MockRepository.bundled())
        let directory = ReferenceSnapshot.artifactDirectoryURL(
            env: ProcessInfo.processInfo.environment["SNAPSHOT_ARTIFACTS"], artifactDirectory: "ios-large")
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        for screen in DebugScreens.all {
            var state = screen.navigation
            let view = AppContent(navigation: Binding(get: { state }, set: { state = $0 }), data: data)
                .background(Tokens.bg.color)
            let image = try ReferenceSnapshot.render(view, mode: .light, contentSize: contentSize)
            try ReferenceSnapshot.checkSize(image)
            let format = UIGraphicsImageRendererFormat()
            format.scale = ReferenceSnapshot.scale
            format.preferredRange = .standard
            let png = UIGraphicsImageRenderer(size: image.size, format: format).pngData { _ in image.draw(at: .zero) }
            try png.write(to: directory.appendingPathComponent("\(screen.name)_\(suffix).png"))
        }
    }

    func testRenderAllScreensXL() async throws {
        try await renderAll(.extraLarge, suffix: "xl")
    }

    func testRenderAllScreensXXXL() async throws {
        try await renderAll(.extraExtraExtraLarge, suffix: "xxxl")
    }
}
