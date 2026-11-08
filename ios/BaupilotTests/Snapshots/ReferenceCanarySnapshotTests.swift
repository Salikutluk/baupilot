import SwiftUI
import XCTest

@MainActor
final class ReferenceCanarySnapshotTests: XCTestCase {
    func testCanaryMatchesLoginLight() throws {
        let reference = try ReferenceSnapshot.loadReference(name: "Login", mode: .light)
        let view = Image(uiImage: reference)
            .resizable()
            .interpolation(.none)
            .frame(width: 390, height: 844)
        try ReferenceSnapshot.assertMatches(
            view, reference: reference, name: "Login", mode: .light, writeArtifact: false)
    }

    func testMissingReferenceIsNotAbsorbed() throws {
        XCTAssertThrowsError(try ReferenceSnapshot.loadReference(name: "Nope", mode: .light)) { error in
            guard case ReferenceSnapshotError.referenceMissing = error else {
                return XCTFail("expected referenceMissing, got \(error)")
            }
        }

        let options = ReferenceSnapshot.expectedFailureOptions()
        XCTAssertFalse(options.isStrict)
        let matcher = try XCTUnwrap(options.issueMatcher)
        XCTAssertFalse(matcher(XCTIssue(type: .thrownError, compactDescription: "x")))
        XCTAssertTrue(matcher(XCTIssue(type: .assertionFailure, compactDescription: "x")))
    }

    func testWrongRenderSizeThrowsAndIsNotAbsorbed() throws {
        let format = UIGraphicsImageRendererFormat()
        format.scale = 1
        let small = UIGraphicsImageRenderer(size: CGSize(width: 10, height: 10), format: format).image { _ in }
        XCTAssertThrowsError(try ReferenceSnapshot.checkSize(small)) { error in
            guard case let ReferenceSnapshotError.sizeMismatch(actual) = error else {
                return XCTFail("expected sizeMismatch, got \(error)")
            }
            XCTAssertEqual(actual, CGSize(width: 10, height: 10))
        }

        let reference = try ReferenceSnapshot.loadReference(name: "Login", mode: .light)
        XCTAssertNoThrow(try ReferenceSnapshot.checkSize(reference))
    }
}
