import Foundation

/// `inspections.overview`: the trade list of the Abnahmen tab.
struct InspectionOverview: Decodable, Sendable, Equatable {
    struct Row: Decodable, Sendable, Equatable {
        let tradeId: String
        let name: String
        let sub: PlatformText
        let status: String
        let route: String?
    }

    let rows: [Row]
    let statusLabels: [String: PlatformText]
}

/// `inspections.byTrade.windows.checks.elements`.
struct ElementsCheck: Decodable, Sendable, Equatable {
    struct Summary: Decodable, Sendable, Equatable {
        let status: String
        let label: PlatformText
    }

    struct Item: Decodable, Sendable, Equatable {
        let id: String
        let room: String
        let type: String
        let label: String
        let status: String
        let route: String?
    }

    struct Floor: Decodable, Sendable, Equatable {
        let title: String
        let more: String
        let items: [Item]
    }

    let screen: String
    let kind: String
    let counter: PlatformText
    let title: String
    let subtitle: PlatformText
    let progress: [String]
    let summary: [Summary]
    let allLabel: PlatformText
    let guide: PlatformText
    let nextLabel: PlatformText
    let floors: [Floor]
}

/// `inspections.byTrade.windows.checks.stepF8`. Every `n` is a JSON string.
struct StepCheck: Decodable, Sendable, Equatable {
    struct Done: Decodable, Sendable, Equatable {
        let label: String
        let meta: PlatformText
    }

    struct Current: Decodable, Sendable, Equatable {
        let n: String
        let title: String
        let text: String
        let photos: [String]
    }

    struct Next: Decodable, Sendable, Equatable {
        let n: String
        let label: PlatformText
    }

    let screen: String
    let kind: String
    let counter: PlatformText
    let title: String
    let subtitle: PlatformText
    let done: [Done]
    let current: Current
    let next: [Next]
}

/// `inspections.byTrade.windows.checks.defectF2`.
struct DefectFormCheck: Decodable, Sendable, Equatable {
    struct Severity: Decodable, Sendable, Equatable {
        let options: [String]
        let selected: String
    }

    struct Notify: Decodable, Sendable, Equatable {
        let label: String
        let enabled: Bool
    }

    let screen: String
    let kind: String
    let context: String
    let title: String
    let description: String
    let photosLabel: String
    let photos: [String]
    let severity: Severity
    let deadlineLabel: PlatformText
    let deadlineOptions: [PlatformText]
    let responsible: String
    let notify: Notify
}

/// `inspections.byTrade.windows.checks.signature`.
struct SignatureCheck: Decodable, Sendable, Equatable {
    struct Stat: Decodable, Sendable, Equatable {
        let n: String
        let label: String
    }

    struct Defect: Decodable, Sendable, Equatable {
        let title: String
        let meta: String
        let due: String
    }

    struct Craftsman: Decodable, Sendable, Equatable {
        let name: String
        let signed: String
    }

    let screen: String
    let kind: String
    let title: String
    let subtitle: PlatformText
    let stats: [Stat]
    let defects: [Defect]
    let reservation: String
    let craftsman: Craftsman
    let owner: PlatformText
    let note: String
}

extension BackendRepository {
    /// Decodes one slice of `inspections()` (a path below its root, e.g. `["overview"]`). A missing member is
    /// `fixtureMissing`, a shape error `decoding` with exactly the slice root (D17).
    func inspectionSlice<T: Decodable & Sendable>(_ type: T.Type, at path: [String]) async throws -> T {
        let location = (["inspections"] + path).joined(separator: ".")
        var node = try await inspections()
        for key in path {
            guard case let .object(members) = node, let child = members[key] else {
                throw RepositoryError.fixtureMissing(location)
            }
            node = child
        }
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.sortedKeys]
        do {
            return try JSONDecoder().decode(T.self, from: encoder.encode(node))
        } catch is DecodingError {
            throw RepositoryError.decoding(location)
        }
    }

    /// A check of the windows inspection: `inspections.byTrade.windows.checks.<key>`.
    func windowsCheck<T: Decodable & Sendable>(_ key: String, as type: T.Type) async throws -> T {
        try await inspectionSlice(type, at: ["byTrade", "windows", "checks", key])
    }
}
