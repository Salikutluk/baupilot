import Foundation

struct TabText: Decodable, Sendable, Equatable {
    let label: String
    let route: String
}

struct LoginScreenText: Decodable, Sendable, Equatable {
    let features: [String]
    let title: String
    let headline: [String]
    let intro: String
    let emailLabel: String
    let emailPlaceholder: String
    let passwordLabel: String
    let forgot: String
    let submit: String
    let social: String
    let newHere: String
    let create: String
}

struct HomeScreenText: Decodable, Sendable, Equatable {
    struct Trade: Decodable, Sendable, Equatable {
        let name: String
        let kw: String
        let when: String
        let statusLabel: String
        let tradeId: String
    }

    struct Doc: Decodable, Sendable, Equatable {
        let label: String
        let count: String
    }

    let projectLabel: String
    let notificationsAria: String
    let phaseLabel: String
    let constructionLabel: String
    let moveInLabel: String
    let trades: [Trade]
    let docs: [Doc]
    let tabs: [TabText]
}

struct ZeitplanScreenText: Decodable, Sendable, Equatable {
    let moveInLabel: String
    let ariaLabel: String
    let tabs: [TabText]
}

struct ProfilScreenText: Decodable, Sendable, Equatable {
    struct Row: Decodable, Sendable, Equatable {
        let label: String
        let value: String?
        let isLink: Bool
        let isToggle: Bool
        let checked: Bool?
    }

    struct Group: Decodable, Sendable, Equatable {
        let title: String
        let rows: [Row]
    }

    let groups: [Group]
    let tabs: [TabText]
}

extension BackendRepository {
    /// Decodes the `ios` member of `screens.<name>`.
    func screenText<T: Decodable & Sendable>(_ name: String, as type: T.Type) async throws -> T {
        let location = "screens.\(name).ios"
        guard case let .object(members)? = try await screen(name), let ios = members["ios"] else {
            throw RepositoryError.fixtureMissing(location)
        }
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.sortedKeys]
        do {
            return try JSONDecoder().decode(T.self, from: encoder.encode(ios))
        } catch let error as DecodingError {
            throw RepositoryError.decoding(Self.describeShapeError(error))
        }
    }

    private static func describeShapeError(_ error: DecodingError) -> String {
        let key: [CodingKey]
        switch error {
        case let .typeMismatch(_, ctx), let .valueNotFound(_, ctx), let .dataCorrupted(ctx):
            key = ctx.codingPath
        case let .keyNotFound(missing, ctx):
            key = ctx.codingPath + [missing]
        @unknown default:
            return "\(error)"
        }
        return key.map(\.stringValue).joined(separator: ".")
    }
}
