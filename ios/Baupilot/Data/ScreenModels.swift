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

struct GewerkScreenText: Decodable, Sendable, Equatable {
    struct Step: Decodable, Sendable, Equatable {
        let label: String
    }

    let steps: [Step]
}

struct DokumenteScreenText: Decodable, Sendable, Equatable {
    struct Filter: Decodable, Sendable, Equatable {
        let text: String
        let selected: Bool
    }

    struct Banner: Decodable, Sendable, Equatable {
        let title: String
        let sub: String
        let amount: String
        let documentId: String
    }

    let headerLabel: String
    let filters: [Filter]
    let searchLabel: String
    let searchPlaceholder: String
    let banner: Banner
    let recentLabel: String
    let byTradeLabel: String
    let tabs: [TabText]
}

/// AbnGewerke and AbnMaengel: the tab bar entries are the only text the fixture carries for them.
struct TabsScreenText: Decodable, Sendable, Equatable {
    let tabs: [TabText]
}

/// The `ios` side of DokViewer, Vergleich and Mitteilungen is `{}`: loading it proves the screen exists.
struct EmptyScreenText: Decodable, Sendable, Equatable {}

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
