/// Estrich "Ebenheit" stepper state (prototype IosEsEbenheit). Pure value type; the screen holds it in `@State`.
struct FlatnessViewModel: Equatable {
    struct Labels: Equatable {
        var within: String
        var outside: String
        var none: String
        var point: String
        var pluralSuffix: String
        var overLimit: String
    }

    /// `limitDefault` clamped to `limitMin...limitMax`.
    let limit: Int
    private(set) var gap: Int
    private(set) var points: [Int]
    let gapMin: Int
    let gapMax: Int
    let gapStep: Int
    let labels: Labels

    init(limitDefault: Int, limitMin: Int, limitMax: Int,
         gap: Int, points: [Int], gapMin: Int, gapMax: Int, gapStep: Int, labels: Labels) {
        self.limit = Swift.min(Swift.max(limitDefault, limitMin), limitMax)
        self.gap = gap
        self.points = points
        self.gapMin = gapMin
        self.gapMax = gapMax
        self.gapStep = gapStep
        self.labels = labels
    }

    mutating func decrement() {
        gap = Swift.max(gapMin, gap - gapStep)
    }

    mutating func increment() {
        gap = Swift.min(gapMax, gap + gapStep)
    }

    /// Appends the current gap and keeps it (prototype `points.concat([gap])`).
    mutating func save() {
        points.append(gap)
    }

    /// Inclusive boundary (I7).
    var isWithin: Bool {
        gap <= limit
    }

    /// Strict: a point equal to the limit is not over.
    func isOver(_ index: Int) -> Bool {
        points[index] > limit
    }

    var overCount: Int {
        points.filter { $0 > limit }.count
    }

    var nextIndex: Int {
        points.count + 1
    }

    var gapText: String {
        "\(gap) mm"
    }

    var limitText: String {
        "\(limit) mm"
    }

    func pointText(_ index: Int) -> String {
        "M\(index + 1) · \(points[index]) mm"
    }

    var resultText: String {
        isWithin ? labels.within : labels.outside
    }

    var summaryText: String {
        let n = overCount
        guard n > 0 else { return labels.none }
        return "\(n) \(labels.point)\(n > 1 ? labels.pluralSuffix : "") \(labels.overLimit)"
    }

    var sketchLineHeight: Int {
        Swift.min(26, 2 + 2 * gap)
    }
}
