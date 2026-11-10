/// Bad "Gefälle" stepper state (prototype IosSanGefaelle). Pure value type; the screen holds it in `@State`.
/// Integer arithmetic only, so ties round like JS `toFixed` (R4).
struct SlopeViewModel: Equatable {
    struct Range: Equatable {
        var min: Int
        var max: Int
        var step: Int
    }

    struct Labels: Equatable {
        var sufficient: String
        var tooFlat: String
        var sollPrefix: String
    }

    private(set) var lenDm: Int
    private(set) var dropMm: Int
    /// Soll in tenths of a percent, `round(default × 10)` clamped to `round(min × 10)...round(max × 10)`.
    let sollTenths: Int
    let lengthRange: Range
    let dropRange: Range
    let labels: Labels

    init(sollDefault: Double, sollMin: Double, sollMax: Double,
         lenDm: Int, dropMm: Int, lengthRange: Range, dropRange: Range, labels: Labels) {
        let lo = Int((sollMin * 10).rounded())
        let hi = Int((sollMax * 10).rounded())
        self.sollTenths = Swift.min(Swift.max(Int((sollDefault * 10).rounded()), lo), hi)
        self.lenDm = lenDm
        self.dropMm = dropMm
        self.lengthRange = lengthRange
        self.dropRange = dropRange
        self.labels = labels
    }

    mutating func incrementLength() {
        lenDm = Swift.min(lengthRange.max, lenDm + lengthRange.step)
    }

    mutating func decrementLength() {
        lenDm = Swift.max(lengthRange.min, lenDm - lengthRange.step)
    }

    mutating func incrementDrop() {
        dropMm = Swift.min(dropRange.max, dropMm + dropRange.step)
    }

    mutating func decrementDrop() {
        dropMm = Swift.max(dropRange.min, dropMm - dropRange.step)
    }

    /// Exact integer form of `dropMm / lenDm >= soll` (inclusive, I7).
    var isSufficient: Bool {
        10 * dropMm >= sollTenths * lenDm
    }

    /// Half-up rounding of `dropMm / lenDm` in hundredths of a percent.
    var percentHundredths: Int {
        (200 * dropMm + lenDm) / (2 * lenDm)
    }

    var percentText: String {
        let h = percentHundredths
        let fraction = h % 100
        return "\(h / 100),\(fraction < 10 ? "0" : "")\(fraction) %"
    }

    var lengthText: String {
        "\(lenDm / 10),\(lenDm % 10) m"
    }

    var dropText: String {
        "\(dropMm / 10),\(dropMm % 10) cm"
    }

    var sollText: String {
        "\(sollTenths / 10),\(sollTenths % 10)"
    }

    var verdictText: String {
        "\(isSufficient ? labels.sufficient : labels.tooFlat) · \(labels.sollPrefix) \(sollText) %"
    }

    /// Prototype `12 + min(26, max(2, pct × 10))`, one decimal; used only for drawing.
    var schemaY2: Double {
        Double(120 + Swift.min(260, Swift.max(20, percentHundredths))) / 10
    }
}
