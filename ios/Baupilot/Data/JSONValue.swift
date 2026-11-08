import Foundation

/// Untyped JSON for fixture sections that are typed in a later milestone.
enum JSONValue: Decodable, Sendable, Equatable {
    case null
    case bool(Bool)
    case number(Double)
    case string(String)
    case array([JSONValue])
    case object([String: JSONValue])

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if container.decodeNil() {
            self = .null
        } else if let value = try? container.decode(Bool.self) {
            self = .bool(value)
        } else if let value = try? container.decode(Double.self) {
            self = .number(value)
        } else if let value = try? container.decode(String.self) {
            self = .string(value)
        } else if let value = try? container.decode([JSONValue].self) {
            self = .array(value)
        } else {
            self = .object(try container.decode([String: JSONValue].self))
        }
    }
}

extension JSONValue: Encodable {
    private struct Key: CodingKey {
        let stringValue: String
        let intValue: Int? = nil

        init(_ stringValue: String) { self.stringValue = stringValue }
        init?(stringValue: String) { self.stringValue = stringValue }
        init?(intValue: Int) { return nil }
    }

    func encode(to encoder: Encoder) throws {
        switch self {
        case .null:
            var container = encoder.singleValueContainer()
            try container.encodeNil()
        case let .bool(value):
            var container = encoder.singleValueContainer()
            try container.encode(value)
        case let .number(value):
            var container = encoder.singleValueContainer()
            try container.encode(value)
        case let .string(value):
            var container = encoder.singleValueContainer()
            try container.encode(value)
        case let .array(values):
            var container = encoder.unkeyedContainer()
            for value in values { try container.encode(value) }
        case let .object(members):
            var container = encoder.container(keyedBy: Key.self)
            for name in members.keys.sorted() {
                try container.encode(members[name], forKey: Key(name))
            }
        }
    }
}
