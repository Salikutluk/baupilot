import Foundation

enum RepositoryError: Error, Equatable {
    case fixtureMissing(String)
    case decoding(String)
}

struct MockRepository: BackendRepository {
    private static let fixtureName = "baupilot"
    private let fixture: Fixture

    init(data: Data) throws {
        do {
            fixture = try JSONDecoder().decode(Fixture.self, from: data)
        } catch let error as DecodingError {
            throw RepositoryError.decoding(Self.describe(error))
        }
    }

    static func bundled(_ bundle: Bundle = .main) throws -> MockRepository {
        guard let url = bundle.url(forResource: fixtureName, withExtension: "json") else {
            throw RepositoryError.fixtureMissing("\(fixtureName).json")
        }
        return try MockRepository(data: Data(contentsOf: url))
    }

    func now() async throws -> Date {
        var calendar = Calendar(identifier: .gregorian)
        guard let berlin = TimeZone(identifier: "Europe/Berlin") else {
            throw RepositoryError.fixtureMissing("Europe/Berlin")
        }
        calendar.timeZone = berlin
        let components = DateComponents(year: 2026, month: 12, day: 18)
        guard let date = calendar.date(from: components) else {
            throw RepositoryError.decoding("meta.now")
        }
        return date
    }

    func project() async throws -> Project { fixture.project }
    func trades() async throws -> [Trade] { fixture.trades }
    func schedule() async throws -> Schedule { fixture.schedule }

    func offers(tradeId: String?) async throws -> [Offer] {
        guard let tradeId else { return fixture.offers }
        return fixture.offers.filter { $0.tradeId == tradeId }
    }

    func comparison(tradeId: String) async throws -> Comparison? {
        fixture.comparisons.first { $0.tradeId == tradeId }
    }

    func documents() async throws -> Documents { fixture.documents }
    func defects() async throws -> [Defect] { fixture.defects }
    func notifications() async throws -> [NotificationGroup] { fixture.notifications }
    func inspections() async throws -> JSONValue { fixture.inspections }
    func measurements() async throws -> JSONValue { fixture.measurements }

    func screen(_ name: String) async throws -> JSONValue? {
        guard case let .object(screens) = fixture.screens else { return nil }
        return screens[name]
    }

    private static func describe(_ error: DecodingError) -> String {
        let context: DecodingError.Context
        var path: [String]
        switch error {
        case let .typeMismatch(_, ctx), let .valueNotFound(_, ctx), let .dataCorrupted(ctx):
            context = ctx
            path = ctx.codingPath.map(\.stringValue)
        case let .keyNotFound(key, ctx):
            context = ctx
            path = ctx.codingPath.map(\.stringValue) + [key.stringValue]
        @unknown default:
            return "\(error)"
        }
        return "\(path.joined(separator: ".")): \(context.debugDescription)"
    }
}
