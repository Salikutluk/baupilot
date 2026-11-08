import SwiftUI
import UniformTypeIdentifiers
import XCTest

@MainActor
enum GoldenSnapshot {
    static func goldenURL(name: String, mode: SnapshotMode) -> URL {
        ReferenceSnapshot.repoRootURL
            .appendingPathComponent("ios/BaupilotTests/Snapshots/Goldens/\(name)_\(mode.rawValue).png")
    }

    static var isRecording: Bool {
        ProcessInfo.processInfo.environment["BAUPILOT_RECORD_GOLDENS"] == "1"
    }

    /// Gallery tests never use `expectRedUntil`: the record-mode `XCTFail` is an assertion failure and would be absorbed.
    static func verify<V: View>(
        _ view: V, name: String, mode: SnapshotMode,
        file: StaticString = #filePath, line: UInt = #line
    ) throws {
        let url = goldenURL(name: name, mode: mode)
        if isRecording {
            let image = try ReferenceSnapshot.render(view, mode: mode)
            do {
                try FileManager.default.createDirectory(
                    at: url.deletingLastPathComponent(), withIntermediateDirectories: true)
                guard let data = image.pngData() else {
                    throw ReferenceSnapshotError.artifactWriteFailed("\(url.path): PNG encoding failed")
                }
                try data.write(to: url)
            } catch let error as ReferenceSnapshotError {
                throw error
            } catch {
                throw ReferenceSnapshotError.artifactWriteFailed("\(url.path): \(error)")
            }
            XCTFail(
                "Golden aufgenommen: \(url.path) – ohne BAUPILOT_RECORD_GOLDENS erneut ausführen",
                file: file, line: line)
            return
        }
        let golden = try ReferenceSnapshot.loadImage(at: url)
        try ReferenceSnapshot.assertMatches(
            view, reference: golden, name: name, mode: mode,
            artifactDirectory: "ios-gallery", file: file, line: line)
    }
}
