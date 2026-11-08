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

    /// <repo>/ios/BaupilotTests/Snapshots/ReferenceSnapshot.swift, four levels up from this file.
    static let repoRootURL: URL = URL(fileURLWithPath: "\(#filePath)")
        .deletingLastPathComponent()
        .deletingLastPathComponent()
        .deletingLastPathComponent()
        .deletingLastPathComponent()

    /// A wrong-size render is a thrown error, so `expectRedUntil` cannot absorb it as a pixel difference (I7).
    static func checkSize(_ image: UIImage) throws {
        guard let cgImage = image.cgImage, cgImage.width == 1170, cgImage.height == 2532 else {
            throw ReferenceSnapshotError.sizeMismatch(actual: image.cgImage.map {
                CGSize(width: $0.width, height: $0.height)
            } ?? image.size)
        }
    }

    static func loadImage(at url: URL) throws -> UIImage {
        guard let data = try? Data(contentsOf: url), let image = UIImage(data: data, scale: scale) else {
            throw ReferenceSnapshotError.referenceMissing(url.path)
        }
        return image
    }

    /// `file` is kept for source compatibility; the path is resolved from `repoRootURL`.
    static func loadReference(
        name: String, mode: SnapshotMode, file: StaticString = #filePath
    ) throws -> UIImage {
        try loadImage(
            at: repoRootURL.appendingPathComponent("design/reference/ios/\(name)_\(mode.rawValue).png"))
    }

    static func render<V: View>(_ view: V, mode: SnapshotMode) throws -> UIImage {
        let traits = UITraitCollection { traits in
            traits.displayScale = scale
            traits.userInterfaceStyle = mode.interfaceStyle
            traits.preferredContentSizeCategory = .large
        }
        let strategy = Snapshotting<V, UIImage>.image(
            precision: precision,
            perceptualPrecision: perceptualPrecision,
            layout: .fixed(width: size.width, height: size.height),
            traits: traits
        )

        let box = RenderedImageBox()
        let expectation = XCTestExpectation(description: "render \(mode.rawValue)")
        strategy.snapshot(view).run { image in
            box.image = image
            expectation.fulfill()
        }
        guard XCTWaiter.wait(for: [expectation], timeout: 5) == .completed, let actual = box.image else {
            throw ReferenceSnapshotError.renderTimeout
        }
        try checkSize(actual)
        return actual
    }

    static func assertMatches<V: View>(
        _ view: V,
        reference: UIImage,
        name: String,
        mode: SnapshotMode,
        writeArtifact: Bool = true,
        artifactDirectory: String = "ios",
        file: StaticString = #filePath,
        line: UInt = #line
    ) throws {
        let actual = try render(view, mode: mode)

        if writeArtifact {
            try writeArtifactPNG(
                actual, name: name, mode: mode,
                directory: artifactDirectoryURL(
                    env: ProcessInfo.processInfo.environment["SNAPSHOT_ARTIFACTS"],
                    artifactDirectory: artifactDirectory))
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

    /// `$SNAPSHOT_ARTIFACTS` names the "ios" directory; other artifact directories are its siblings.
    static func artifactDirectoryURL(env: String?, artifactDirectory: String) -> URL {
        if let env, !env.isEmpty {
            let base = URL(fileURLWithPath: env, isDirectory: true)
            return artifactDirectory == "ios"
                ? base
                : base.deletingLastPathComponent()
                    .appendingPathComponent(artifactDirectory, isDirectory: true)
        }
        return repoRootURL.appendingPathComponent("build/snapshots/\(artifactDirectory)", isDirectory: true)
    }

    private static func writeArtifactPNG(
        _ image: UIImage, name: String, mode: SnapshotMode, directory: URL
    ) throws {
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
