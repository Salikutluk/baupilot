import Foundation

/// Parses the target strings of the fixture (`trade/<id>`, `document/<id>`, tab names, …).
enum RouteParser {
    nonisolated static func target(_ raw: String?, field: String) throws -> NavigationTarget? {
        guard let raw else { return nil }
        if let tab = AppTab(rawValue: raw) { return .tab(tab) }
        switch raw {
        case "defects": return .tab(.inspections)
        case "notifications": return .push(.notifications)
        default: break
        }
        let parts = raw.split(separator: "/", omittingEmptySubsequences: false).map(String.init)
        switch parts.first {
        case "trade" where parts.count == 2 && isIdentifier(parts[1]):
            return .push(.trade(id: parts[1]))
        case "trade" where parts.count == 3 && isIdentifier(parts[1]) && parts[2] == "compare":
            return .push(.compare(tradeId: parts[1]))
        case "document" where parts.count == 2 && isIdentifier(parts[1]):
            return .push(.document(id: parts[1]))
        default:
            throw RepositoryError.decoding(field)
        }
    }

    /// A target that must open a pushed screen; a tab target is rejected.
    nonisolated static func route(_ raw: String?, field: String) throws -> AppRoute? {
        switch try target(raw, field: field) {
        case nil: return nil
        case let .push(route): return route
        case .tab: throw RepositoryError.decoding(field)
        }
    }

    /// An id of a trade or document: `^[A-Za-z0-9][A-Za-z0-9_-]*$`.
    private nonisolated static func isIdentifier(_ text: String) -> Bool {
        guard let first = text.unicodeScalars.first, isAlphanumeric(first) else { return false }
        return text.unicodeScalars.allSatisfy { isAlphanumeric($0) || $0 == "_" || $0 == "-" }
    }

    private nonisolated static func isAlphanumeric(_ scalar: Unicode.Scalar) -> Bool {
        ("A"..."Z").contains(scalar) || ("a"..."z").contains(scalar) || ("0"..."9").contains(scalar)
    }
}
