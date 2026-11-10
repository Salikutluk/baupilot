import Foundation

/// Parses the target strings of the fixture (`trade/<id>`, `document/<id>`, tab names, …).
enum RouteParser {
    nonisolated static func target(_ raw: String?, field: String) throws -> NavigationTarget? {
        guard let raw else { return nil }
        if let tab = AppTab(rawValue: raw) { return .tab(tab) }
        switch raw {
        case "defects": return .defects
        case "defect/new": return .push(.newDefect)
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
        case "inspection" where parts.count == 2 && isIdentifier(parts[1]):
            return .push(.inspection(tradeId: parts[1]))
        case "inspection" where parts.count == 3 && isIdentifier(parts[1]) && parts[2] == "sign":
            return .push(.signOff(inspectionId: parts[1]))
        case "inspection" where parts.count == 3 && isIdentifier(parts[1]) && isIdentifier(parts[2])
            && !reservedCheckWords.contains(parts[2]):
            return .push(.inspectionCheck(tradeId: parts[1], check: parts[2]))
        case "inspection" where parts.count == 4 && isIdentifier(parts[1]) && parts[2] == "room"
            && isIdentifier(parts[3]):
            return .push(.inspectionRoom(tradeId: parts[1], roomId: parts[3]))
        case "inspection" where parts.count == 4 && isIdentifier(parts[1]) && isIdentifier(parts[2])
            && isIdentifier(parts[3]) && !reservedCheckWords.contains(parts[2])
            && !reservedCheckWords.contains(parts[3]):
            return .push(.inspectionCheckItem(tradeId: parts[1], check: parts[2], item: parts[3]))
        case "inspection" where parts.count == 5 && isIdentifier(parts[1]) && isIdentifier(parts[2])
            && parts[3] == "step":
            guard let step = stepNumber(parts[4]) else { throw RepositoryError.decoding(field) }
            return .push(.inspectionStep(tradeId: parts[1], elementId: parts[2], step: step))
        default:
            throw RepositoryError.decoding(field)
        }
    }

    private nonisolated static let reservedCheckWords: Set<String> = ["sign", "step", "room"]

    /// A target that must open a pushed screen; a tab or the defects view is rejected.
    nonisolated static func route(_ raw: String?, field: String) throws -> AppRoute? {
        switch try target(raw, field: field) {
        case nil: return nil
        case let .push(route): return route
        case .tab, .defects: throw RepositoryError.decoding(field)
        }
    }

    /// A link of an inspection overview row: a pushed screen or the defects view; a tab is rejected.
    nonisolated static func inspectionLink(_ raw: String, field: String) throws -> NavigationTarget {
        let parsed = try target(raw, field: field)
        if case .tab = parsed { throw RepositoryError.decoding(field) }
        guard let parsed else { throw RepositoryError.decoding(field) }
        return parsed
    }

    /// An id of a trade or document: `^[A-Za-z0-9][A-Za-z0-9_-]*$`.
    private nonisolated static func isIdentifier(_ text: String) -> Bool {
        guard let first = text.unicodeScalars.first, isAlphanumeric(first) else { return false }
        return text.unicodeScalars.allSatisfy { isAlphanumeric($0) || $0 == "_" || $0 == "-" }
    }

    /// A step number: `^[1-9][0-9]*$`.
    private nonisolated static func stepNumber(_ text: String) -> Int? {
        guard let first = text.unicodeScalars.first, ("1"..."9").contains(first),
            text.unicodeScalars.allSatisfy({ ("0"..."9").contains($0) })
        else { return nil }
        return Int(text)
    }

    private nonisolated static func isAlphanumeric(_ scalar: Unicode.Scalar) -> Bool {
        ("A"..."Z").contains(scalar) || ("a"..."z").contains(scalar) || ("0"..."9").contains(scalar)
    }
}
