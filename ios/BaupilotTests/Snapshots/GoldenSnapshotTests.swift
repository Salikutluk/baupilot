import SwiftUI
import XCTest

@MainActor
final class GoldenSnapshotTests: XCTestCase {
    func testMissingGoldenThrowsReferenceMissing() throws {
        // Record mode would write `NoSuchGolden_light.png` into the repo.
        try XCTSkipIf(GoldenSnapshot.isRecording, "record mode writes goldens")
        XCTAssertThrowsError(
            try GoldenSnapshot.verify(Color.clear, name: "NoSuchGolden", mode: .light)
        ) { error in
            guard case ReferenceSnapshotError.referenceMissing = error else {
                return XCTFail("expected referenceMissing, got \(error)")
            }
        }

        let options = ReferenceSnapshot.expectedFailureOptions()
        let matcher = try XCTUnwrap(options.issueMatcher)
        XCTAssertFalse(matcher(XCTIssue(type: .thrownError, compactDescription: "x")))
    }

    func testGoldenURLLayout() {
        let url = GoldenSnapshot.goldenURL(name: "GalleryX", mode: .dark)
        XCTAssertTrue(
            url.path.hasSuffix("ios/BaupilotTests/Snapshots/Goldens/GalleryX_dark.png"), url.path)
        let referenceDirectory = ReferenceSnapshot.repoRootURL
            .appendingPathComponent("design/reference/ios", isDirectory: true)
        XCTAssertTrue(
            FileManager.default.fileExists(atPath: referenceDirectory.path),
            "repoRootURL must be the directory containing design/reference/ios")
        XCTAssertTrue(url.path.hasPrefix(ReferenceSnapshot.repoRootURL.path), url.path)
    }

    func testArtifactDirectoryResolution() {
        XCTAssertEqual(
            ReferenceSnapshot.artifactDirectoryURL(
                env: "/w/build/snapshots/ios", artifactDirectory: "ios-gallery").path,
            "/w/build/snapshots/ios-gallery")
        XCTAssertEqual(
            ReferenceSnapshot.artifactDirectoryURL(
                env: "/w/build/snapshots/ios", artifactDirectory: "ios").path,
            "/w/build/snapshots/ios")
        XCTAssertEqual(
            ReferenceSnapshot.artifactDirectoryURL(env: nil, artifactDirectory: "ios-gallery").path,
            ReferenceSnapshot.repoRootURL.appendingPathComponent("build/snapshots/ios-gallery").path)
        XCTAssertEqual(
            ReferenceSnapshot.artifactDirectoryURL(env: "", artifactDirectory: "ios").path,
            ReferenceSnapshot.repoRootURL.appendingPathComponent("build/snapshots/ios").path)
    }

    func testUsesReferenceTolerances() {
        XCTAssertEqual(ReferenceSnapshot.precision, 0.995)
        XCTAssertEqual(ReferenceSnapshot.perceptualPrecision, 0.98)
    }
}
