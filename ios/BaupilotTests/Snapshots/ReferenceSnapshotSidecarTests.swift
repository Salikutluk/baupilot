import XCTest

@MainActor
final class ReferenceSnapshotSidecarTests: XCTestCase {
    func testSidecarParsesPixelMatchFromPerceptualMessage() {
        let message = """
            The percentage of pixels that match 0.9912 is less than required 0.995
            The lowest perceptual color precision 0.9734 is less than required 0.98
            """
        let result = ReferenceSnapshot.result(passed: false, message: message)
        XCTAssertFalse(result.passed)
        XCTAssertEqual(result.message, message)
        XCTAssertEqual(try XCTUnwrap(result.pixelMatch), 0.9912, accuracy: 1e-9)
        XCTAssertEqual(result.required, 0.995, accuracy: 1e-9)
    }

    func testSidecarOnPassHasNullValue() {
        let result = ReferenceSnapshot.result(passed: true, message: "")
        XCTAssertTrue(result.passed)
        XCTAssertEqual(result.message, "")
        XCTAssertNil(result.pixelMatch)
        XCTAssertEqual(result.required, 0.995, accuracy: 1e-9)
    }

    func testSidecarSizeMismatchHasNullValue() {
        let message = "Expected image size (1170, 2532) to match actual size (390, 844)"
        let result = ReferenceSnapshot.result(passed: false, message: message)
        XCTAssertFalse(result.passed)
        XCTAssertEqual(result.message, message)
        XCTAssertNil(result.pixelMatch)
    }

    func testSidecarOnlyForScreenArtifacts() {
        XCTAssertTrue(ReferenceSnapshot.writesSidecar(artifactDirectory: "ios", writeArtifact: true))
        XCTAssertFalse(ReferenceSnapshot.writesSidecar(artifactDirectory: "ios-gallery", writeArtifact: true))
        XCTAssertFalse(ReferenceSnapshot.writesSidecar(artifactDirectory: "ios", writeArtifact: false))
        XCTAssertFalse(ReferenceSnapshot.writesSidecar(artifactDirectory: "ios-gallery", writeArtifact: false))
    }
}
