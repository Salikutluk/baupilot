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

    func testScreenTextDecodesLogin() async throws {
        let login = try await repository().screenText("Login", as: LoginScreenText.self)
        XCTAssertEqual(login.title, "Baupilot")
        XCTAssertEqual(login.headline, ["Ihr Bauvorhaben.", "Ein Plan."])
        XCTAssertEqual(login.features.count, 3)
        XCTAssertEqual(login.social, "Mit Apple anmelden")
        XCTAssertEqual(login.newHere, "Neu hier?")
        XCTAssertEqual(login.create, "Bauvorhaben anlegen")
    }

    func testScreenTextMissingScreenThrowsFixtureMissing() async throws {
        let repo = try repository()
        do {
            _ = try await repo.screenText("Nope", as: LoginScreenText.self)
            XCTFail("Expected RepositoryError.fixtureMissing")
        } catch {
            XCTAssertEqual(error as? RepositoryError, .fixtureMissing("screens.Nope.ios"))
        }
    }

    func testScreenTextShapeErrorThrowsDecoding() async throws {
        let repo = try repository()
        do {
            _ = try await repo.screenText("Home", as: LoginScreenText.self)
            XCTFail("Expected RepositoryError.decoding")
        } catch {
            guard case let RepositoryError.decoding(path)? = error as? RepositoryError else {
                return XCTFail("Expected RepositoryError.decoding, got \(error)")
            }
            XCTAssertTrue(path.contains("features"), path)
        }
    }

    func testJSONValueRoundTrips() async throws {
        let repo = try repository()
        let url = try XCTUnwrap(Bundle.main.url(forResource: "baupilot", withExtension: "json"))
        struct Screens: Decodable { let screens: JSONValue }
        let original = try JSONDecoder().decode(Screens.self, from: Data(contentsOf: url)).screens
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.sortedKeys]
        let encoded = try encoder.encode(original)
        let decoded = try JSONDecoder().decode(JSONValue.self, from: encoded)
        XCTAssertEqual(decoded, original)
        XCTAssertEqual(try encoder.encode(decoded), encoded)
        let projected = try await repo.screen("Login")
        XCTAssertNotNil(projected)
    }
}
