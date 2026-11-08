import Foundation

/// Text that is either shared by both platforms (a plain string) or given per platform
/// (`{ "ios": ..., "android": ... }`). `value` is the iOS side.
struct PlatformText: Decodable, Sendable, Equatable {
    let value: String?

    private struct Key: CodingKey {
        let stringValue: String
        let intValue: Int? = nil

        init?(stringValue: String) {
            self.stringValue = stringValue
        }

        init?(intValue: Int) {
            return nil
        }
    }

    init(value: String?) {
        self.value = value
    }

    init(from decoder: Decoder) throws {
        if let single = try? decoder.singleValueContainer(), let text = try? single.decode(String.self) {
            value = text
            return
        }
        guard let keyed = try? decoder.container(keyedBy: Key.self),
              Set(keyed.allKeys.map(\.stringValue)) == ["ios", "android"],
              let iosKey = Key(stringValue: "ios"),
              let androidKey = Key(stringValue: "android")
        else {
            throw DecodingError.typeMismatch(
                PlatformText.self,
                DecodingError.Context(
                    codingPath: decoder.codingPath,
                    debugDescription: "Expected a string or an object with exactly the keys ios and android"
                )
            )
        }
        value = try keyed.decode(String?.self, forKey: iosKey)
        _ = try keyed.decode(String?.self, forKey: androidKey)
    }
}
