import UIKit
import XCTest
@testable import Baupilot

@MainActor
final class FontTests: XCTestCase {
    func testInterFontsAreAvailableAfterRegistration() {
        BaupilotFonts.registerIfNeeded()
        for name in BaupilotFonts.postScriptNames {
            XCTAssertNotNil(UIFont(name: name, size: 16), "Missing font \(name)")
        }
    }

    /// Reads hhea (ascender int16 @4, descender int16 @6) and head.unitsPerEm (uint16 @18) from the TTFs.
    func testInterHheaMatchesLineBoxConstants() throws {
        let fonts = ReferenceSnapshot.repoRootURL.appendingPathComponent("ios/Baupilot/Resources/Fonts")
        for name in BaupilotFonts.postScriptNames {
            let data = try Data(contentsOf: fonts.appendingPathComponent("\(name).ttf"))
            let hhea = try table("hhea", in: data)
            let head = try table("head", in: data)
            XCTAssertEqual(CGFloat(int16(data, hhea + 4)), InterMetrics.ascender, name)
            XCTAssertEqual(abs(CGFloat(int16(data, hhea + 6))), InterMetrics.descender, name)
            XCTAssertEqual(CGFloat(uint16(data, head + 18)), InterMetrics.unitsPerEm, name)
        }
    }

    private func table(_ tag: String, in data: Data) throws -> Int {
        let count = Int(uint16(data, 4))
        for index in 0..<count {
            let record = 12 + index * 16
            let name = String(decoding: data[record..<record + 4], as: UTF8.self)
            if name == tag { return Int(uint32(data, record + 8)) }
        }
        XCTFail("Table \(tag) missing")
        return 0
    }

    private func uint16(_ data: Data, _ offset: Int) -> UInt16 {
        UInt16(data[data.startIndex + offset]) << 8 | UInt16(data[data.startIndex + offset + 1])
    }

    private func int16(_ data: Data, _ offset: Int) -> Int16 {
        Int16(bitPattern: uint16(data, offset))
    }

    private func uint32(_ data: Data, _ offset: Int) -> UInt32 {
        UInt32(uint16(data, offset)) << 16 | UInt32(uint16(data, offset + 2))
    }
}
