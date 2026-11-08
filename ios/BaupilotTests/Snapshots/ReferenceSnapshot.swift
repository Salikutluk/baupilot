import SnapshotTesting
import SwiftUI
import UniformTypeIdentifiers
import XCTest

enum SnapshotMode: String {
    case light, dark

    var interfaceStyle: UIUserInterfaceStyle {
        switch self {
        case .light: return .light
        case .dark: return .dark
        }
    }
}

enum ReferenceSnapshotError: Error {
    case referenceMissing(String)
    case renderTimeout
    case sizeMismatch(actual: CGSize)
    case artifactWriteFailed(String)
}

private final class RenderedImageBox: @unchecked Sendable {
    var image: UIImage?
}

@MainActor
enum ReferenceSnapshot {
    static let precision: Float = 0.995
    static let perceptualPrecision: Float = 0.98
    static let size = CGSize(width: 390, height: 844)
    static let scale: CGFloat = 3

    private static func repoRoot(from file: StaticString) -> URL {
        // <repo>/ios/BaupilotTests/Snapshots/<file>.swift
        URL(fileURLWithPath: "\(file)")
            .deletingLastPathComponent()
            .deletingLastPathComponent()
            .deletingLastPathComponent()
            .deletingLastPathComponent()
    }

    /// A wrong-size render is a thrown error, so `expectRedUntil` cannot absorb it as a pixel difference (I7).
    static func checkSize(_ image: UIImage) throws {
        guard let cgImage = image.cgImage, cgImage.width == 1170, cgImage.height == 2532 else {
            throw ReferenceSnapshotError.sizeMismatch(actual: image.cgImage.map {
                CGSize(width: $0.width, height: $0.height)
            } ?? image.size)
        }
    }

    static func loadReference(
        name: String, mode: SnapshotMode, file: StaticString = #filePath
    ) throws -> UIImage {
        let url = repoRoot(from: file)
            .appendingPathComponent("design/reference/ios/\(name)_\(mode.rawValue).png")
        guard let data = try? Data(contentsOf: url), let image = UIImage(data: data, scale: scale) else {
            throw ReferenceSnapshotError.referenceMissing(url.path)
        }
        return image
    }

    static func assertMatches<V: View>(
        _ view: V,
        reference: UIImage,
        name: String,
        mode: SnapshotMode,
        writeArtifact: Bool = true,
        file: StaticString = #filePath,
        line: UInt = #line
    ) throws {
        let traits = UITraitCollection { traits in
            traits.displayScale = scale
            traits.userInterfaceStyle = mode.interfaceStyle
        }
        let strategy = Snapshotting<V, UIImage>.image(
            precision: precision,
            perceptualPrecision: perceptualPrecision,
            layout: .fixed(width: size.width, height: size.height),
            traits: traits
        )

        let box = RenderedImageBox()
        let expectation = XCTestExpectation(description: "render \(name)_\(mode.rawValue)")
        strategy.snapshot(view).run { image in
            box.image = image
            expectation.fulfill()
        }
        guard XCTWaiter.wait(for: [expectation], timeout: 5) == .completed, let actual = box.image else {
            throw ReferenceSnapshotError.renderTimeout
        }

        try checkSize(actual)

        if writeArtifact {
            try writeArtifactPNG(actual, name: name, mode: mode, file: file)
        }

        let diffing = Diffing<UIImage>.image(
            precision: precision, perceptualPrecision: perceptualPrecision, scale: scale)
        guard let (message, attachments) = diffing.diffV2(reference, actual) else { return }
        for attachment in attachments {
            if case let .data(data, attachmentName) = attachment {
                let xcAttachment = XCTAttachment(
                    data: data, uniformTypeIdentifier: UTType.png.identifier)
                xcAttachment.name = attachmentName
                xcAttachment.lifetime = .keepAlways
                XCTContext.runActivity(named: attachmentName) { $0.add(xcAttachment) }
            }
        }
        XCTFail(message, file: file, line: line)
    }

    private static func writeArtifactPNG(
        _ image: UIImage, name: String, mode: SnapshotMode, file: StaticString
    ) throws {
        let directory: URL
        if let custom = ProcessInfo.processInfo.environment["SNAPSHOT_ARTIFACTS"], !custom.isEmpty {
            directory = URL(fileURLWithPath: custom, isDirectory: true)
        } else {
            directory = repoRoot(from: file).appendingPathComponent("build/snapshots/ios", isDirectory: true)
        }
        let format = UIGraphicsImageRendererFormat()
        format.scale = scale
        format.preferredRange = .standard
        let renderer = UIGraphicsImageRenderer(size: image.size, format: format)
        let data = renderer.pngData { _ in image.draw(at: .zero) }
        let target = directory.appendingPathComponent("\(name)_\(mode.rawValue).png")
        do {
            try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
            try data.write(to: target)
        } catch {
            throw ReferenceSnapshotError.artifactWriteFailed("\(target.path): \(error)")
        }
    }

    static func expectedFailureOptions() -> XCTExpectedFailure.Options {
        let options = XCTExpectedFailure.Options()
        options.isStrict = false
        options.issueMatcher = { $0.type == .assertionFailure }
        return options
    }

    static func expectRedUntil(_ milestone: String) {
        XCTExpectFailure(
            "M0: Login-Screen folgt in \(milestone)", options: expectedFailureOptions())
    }
}
