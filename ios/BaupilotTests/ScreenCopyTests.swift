import XCTest

@testable import Baupilot

final class ScreenCopyTests: XCTestCase {
    /// M7 D11 (I8): the only non-visible words M7 adds; the prototype has no text for the signature (role=img).
    func testSignatureStateWords() {
        XCTAssertEqual(ScreenCopy.Signature.empty, "leer")
        XCTAssertEqual(ScreenCopy.Signature.signed, "unterschrieben")
    }
}
