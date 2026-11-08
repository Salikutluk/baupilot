import XCTest
@testable import Baupilot

final class MockRepositoryTests: XCTestCase {
    private func repository() throws -> MockRepository {
        try MockRepository.bundled()
    }

    func testBundledFixtureDecodes() throws {
        _ = try repository()
    }

    func testProjectAndDocumentCounts() async throws {
        let repo = try repository()
        let project = try await repo.project()
        XCTAssertEqual(project.address, "Musterweg 12")
        let documents = try await repo.documents()
        XCTAssertEqual(documents.counts.all, 26)
    }

    func testNowIsDecember18InBerlin() async throws {
        let repo = try repository()
        let now = try await repo.now()
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.timeZone = TimeZone(identifier: "Europe/Berlin")
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd HH:mm"
        XCTAssertEqual(formatter.string(from: now), "2026-12-18 00:00")
    }

    func testDefectTradeUsesIosSideOfPlatformText() async throws {
        let repo = try repository()
        let defects = try await repo.defects()
        let crack = try XCTUnwrap(defects.first { $0.title == "Riss in Kellerwand Nord" })
        XCTAssertEqual(crack.trade.value, "Rohbau")
    }

    func testRepositoryQueries() async throws {
        let repo = try repository()
        let all = try await repo.offers(tradeId: nil)
        let screed = try await repo.offers(tradeId: "screed")
        XCTAssertEqual(all.count, 3)
        XCTAssertEqual(screed.count, 3)
        let none = try await repo.offers(tradeId: "windows")
        XCTAssertTrue(none.isEmpty)
        let comparison = try await repo.comparison(tradeId: "screed")
        XCTAssertNotNil(comparison)
        let missing = try await repo.comparison(tradeId: "windows")
        XCTAssertNil(missing)
        let home = try await repo.screen("Home")
        XCTAssertNotNil(home)
        let unknown = try await repo.screen("Nope")
        XCTAssertNil(unknown)
    }

    func testMalformedDataThrowsDecodingErrorNamingMissingKey() {
        XCTAssertThrowsError(try MockRepository(data: Data("{}".utf8))) { error in
            guard case let RepositoryError.decoding(message) = error else {
                return XCTFail("Expected RepositoryError.decoding, got \(error)")
            }
            XCTAssertTrue(message.contains("meta"), message)
        }
    }

    func testPlatformTextDecodesFromString() throws {
        let value = try JSONDecoder().decode(PlatformText.self, from: Data(#""Rohbau""#.utf8))
        XCTAssertEqual(value.value, "Rohbau")
    }

    func testPlatformTextDecodesFromObject() throws {
        let json = #"{"ios":"Rohbau","android":"ROHBAU"}"#
        let value = try JSONDecoder().decode(PlatformText.self, from: Data(json.utf8))
        XCTAssertEqual(value.value, "Rohbau")
        let nullIos = try JSONDecoder().decode(PlatformText.self, from: Data(#"{"ios":null,"android":"x"}"#.utf8))
        XCTAssertNil(nullIos.value)
    }

    func testPlatformTextRejectsOtherObjectShapes() {
        for json in [#"{}"#, #"{"ios":"x"}"#, #"{"android":"y"}"#, #"{"ios":"x","andriod":"y"}"#, #"{"ios":"x","android":"y","web":"z"}"#, #"{"ios":1,"android":"y"}"#] {
            XCTAssertThrowsError(try JSONDecoder().decode(PlatformText.self, from: Data(json.utf8)), json) { error in
                guard case DecodingError.typeMismatch = error else {
                    return XCTFail("Expected typeMismatch for \(json), got \(error)")
                }
            }
        }
    }

    func testPlatformTextRejectsNumber() {
        XCTAssertThrowsError(try JSONDecoder().decode(PlatformText.self, from: Data("42".utf8))) { error in
            guard case DecodingError.typeMismatch = error else {
                return XCTFail("Expected typeMismatch, got \(error)")
            }
        }
    }
}
