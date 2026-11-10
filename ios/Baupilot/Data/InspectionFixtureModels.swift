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

/// `inspections.byTrade.<earthworks|shell|carpentry>`: a protocol of an accepted or reserved trade.
struct TradeProtocolDto: Decodable, Sendable, Equatable {
    struct Banner: Decodable, Sendable, Equatable {
        let title: String
        let text: String
        let documentLabel: String?
    }

    struct Row: Decodable, Sendable, Equatable {
        let label: PlatformText
        let meta: String
        let route: String?
    }

    struct Group: Decodable, Sendable, Equatable {
        let title: String
        let rows: [Row]
    }

    let title: String
    let subtitle: PlatformText
    let banner: Banner
    let groups: [Group]
    /// Raw: every check is decoded on its own at `byTrade.<t>.checks.<key>`.
    let checks: JSONValue?
}

/// `inspections.byTrade.electrical`.
struct ElectricalOverviewDto: Decodable, Sendable, Equatable {
    struct Phase: Decodable, Sendable, Equatable {
        let title: String
        let statusLabel: PlatformText?
        let dateLabel: PlatformText?
        let progressLabel: String?
    }

    struct Row: Decodable, Sendable, Equatable {
        let name: String
        let sub: String
        let route: String?
        let statusLabel: String
    }

    struct Group: Decodable, Sendable, Equatable {
        let title: PlatformText
        let more: PlatformText?
        let rows: [Row]
    }

    let title: String
    let subtitle: PlatformText
    let phases: [Phase]
    let groups: [Group]
    /// Raw: every check is decoded on its own at `byTrade.electrical.checks.<key>`.
    let checks: JSONValue
}

/// `inspections.byTrade.shell.checks.measure`.
struct MeasureCheckDto: Decodable, Sendable, Equatable {
    let screen: String
    let kind: String
    let measurementKey: String
    let counter: PlatformText
    let title: String
}

/// `measurements.shellOpening`.
struct ShellOpeningDto: Decodable, Sendable, Equatable {
    struct Parameter: Decodable, Sendable, Equatable {
        let key: String
        let `default`: Int
        let min: Int
        let max: Int
        let unit: String
    }

    struct Size: Decodable, Sendable, Equatable {
        let w: Int
        let h: Int
    }

    struct DimLabels: Decodable, Sendable, Equatable {
        let w: String
        let h: String
    }

    struct Labels: Decodable, Sendable, Equatable {
        let within: String
        let outside: String
    }

    let parameter: Parameter
    let target: Size
    let actual: Size
    let stepperDelta: Int
    let dimLabels: DimLabels
    let measured: [String]
    let labels: Labels
}

/// `inspections.byTrade.electrical.checks.kitchen`. Every `n` is a JSON string.
struct RoomCheckDto: Decodable, Sendable, Equatable {
    struct Done: Decodable, Sendable, Equatable {
        let label: String
        let meta: PlatformText
    }

    struct Current: Decodable, Sendable, Equatable {
        let n: String
        let title: String
        let text: String
    }

    struct Next: Decodable, Sendable, Equatable {
        let n: String
        let label: PlatformText
    }

    struct Socket: Decodable, Sendable, Equatable {
        let id: String
        let label: String
        let status: String
        let aria: String
    }

    let screen: String
    let kind: String
    let counter: PlatformText
    let title: String
    let subtitle: PlatformText
    let done: [Done]
    let current: Current
    let next: [Next]
    let sockets: [Socket]
    let progress: PlatformText
    let defectNote: String
    let okNote: String
}

/// `inspections.byTrade.electrical.checks.panel`. Every `n` is a JSON string.
struct PanelCheckDto: Decodable, Sendable, Equatable {
    struct Done: Decodable, Sendable, Equatable {
        let label: String
        let meta: String
    }

    struct Current: Decodable, Sendable, Equatable {
        let n: String
        let title: String
        let text: String
        let steps: [String]
        let outcomes: [String]
    }

    struct Next: Decodable, Sendable, Equatable {
        let n: String
        let label: String
        let tag: String?
    }

    struct Fi: Decodable, Sendable, Equatable {
        let name: String
        let sub: String
        let statusLabel: String
    }

    let screen: String
    let kind: String
    let counter: PlatformText
    let title: String
    let subtitle: String
    let done: [Done]
    let current: Current
    let next: [Next]
    let fis: [Fi]
}

/// `inspections.byTrade.electrical.checks.smoke`.
struct SmokeCheckDto: Decodable, Sendable, Equatable {
    struct Room: Decodable, Sendable, Equatable {
        let name: String
        let statusLabel: String
    }

    struct Check: Decodable, Sendable, Equatable {
        let label: String
        let hint: String
    }

    struct Current: Decodable, Sendable, Equatable {
        let room: String
        let title: String
        let outcomes: [String]
    }

    let screen: String
    let kind: String
    let counter: PlatformText
    let title: String
    let subtitle: String
    let rooms: [Room]
    let checks: [Check]
    let current: Current
    let reminder: String
    let battery: String
}

extension BackendRepository {
    /// Decodes one slice of a JSON tree (a path below `root`, named `rootName` in errors). A missing member is
    /// `fixtureMissing`, a shape error `decoding`, both with exactly the slice location (D17).
    private func decodeSlice<T: Decodable & Sendable>(
        _ type: T.Type, from root: JSONValue, rootName: String, at path: [String]
    ) throws -> T {
        let location = ([rootName] + path).joined(separator: ".")
        var node = root
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

    /// Decodes one slice of `inspections()` (a path below its root, e.g. `["overview"]`).
    func inspectionSlice<T: Decodable & Sendable>(_ type: T.Type, at path: [String]) async throws -> T {
        try decodeSlice(type, from: try await inspections(), rootName: "inspections", at: path)
    }

    /// Decodes `measurements.<key>`.
    func measurementSlice<T: Decodable & Sendable>(_ type: T.Type, key: String) async throws -> T {
        try decodeSlice(type, from: try await measurements(), rootName: "measurements", at: [key])
    }

    /// A trade of the inspections: `inspections.byTrade.<tradeId>`.
    func tradeSlice<T: Decodable & Sendable>(_ type: T.Type, tradeId: String) async throws -> T {
        try await inspectionSlice(type, at: ["byTrade", tradeId])
    }

    /// A check of a trade: `inspections.byTrade.<tradeId>.checks.<key>`.
    func tradeCheck<T: Decodable & Sendable>(_ tradeId: String, _ key: String, as type: T.Type) async throws -> T {
        try await inspectionSlice(type, at: ["byTrade", tradeId, "checks", key])
    }

    /// A check of the windows inspection: `inspections.byTrade.windows.checks.<key>`.
    func windowsCheck<T: Decodable & Sendable>(_ key: String, as type: T.Type) async throws -> T {
        try await tradeCheck("windows", key, as: type)
    }
}
