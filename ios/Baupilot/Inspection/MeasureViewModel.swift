/// Rohbau "Maße & Lot" stepper state (prototype IosRohbauMasse). Pure value type; the screen holds it in `@State`.
struct MeasureViewModel: Equatable {
    struct Dimensions: Equatable {
        var width: Int
        var height: Int
    }

    enum Dimension {
        case width, height
    }

    let target: Dimensions
    private(set) var actual: Dimensions
    let step: Int
    /// `toleranceDefault` clamped to `toleranceMin...toleranceMax`.
    let tolerance: Int
    let unit: String

    init(target: Dimensions, actual: Dimensions, step: Int,
         toleranceDefault: Int, toleranceMin: Int, toleranceMax: Int, unit: String) {
        self.target = target
        self.actual = actual
        self.step = step
        self.tolerance = min(max(toleranceDefault, toleranceMin), toleranceMax)
        self.unit = unit
    }

    mutating func increment(_ d: Dimension) {
        set(d, value(actual, d) + step)
    }

    /// Deviation from the prototype (R8): a decrement that would go below 0 is a no-op.
    mutating func decrement(_ d: Dimension) {
        let next = value(actual, d) - step
        if next >= 0 { set(d, next) }
    }

    func diff(_ d: Dimension) -> Int {
        value(actual, d) - value(target, d)
    }

    /// Sign (+, − U+2212, ± for zero), absolute value, space, unit.
    func diffText(_ d: Dimension) -> String {
        let delta = diff(d)
        let sign = delta > 0 ? "+" : (delta < 0 ? "−" : "±")
        return "\(sign)\(abs(delta)) \(unit)"
    }

    /// Plain integer, no grouping separator.
    func valueText(_ d: Dimension) -> String {
        String(value(actual, d))
    }

    /// Inclusive boundary (I7).
    func isWithin(_ d: Dimension) -> Bool {
        abs(diff(d)) <= tolerance
    }

    var isWithinTolerance: Bool {
        isWithin(.width) && isWithin(.height)
    }

    private func value(_ dims: Dimensions, _ d: Dimension) -> Int {
        switch d {
        case .width: return dims.width
        case .height: return dims.height
        }
    }

    private mutating func set(_ d: Dimension, _ v: Int) {
        switch d {
        case .width: actual.width = v
        case .height: actual.height = v
        }
    }
}
