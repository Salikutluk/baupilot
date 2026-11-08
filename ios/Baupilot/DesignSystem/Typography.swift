import SwiftUI

public enum InterWeight: Int {
    case regular = 400, medium = 500, semibold = 600, bold = 700

    var postScriptName: String {
        switch self {
        case .regular: return "Inter-Regular"
        case .medium: return "Inter-Medium"
        case .semibold: return "Inter-SemiBold"
        case .bold: return "Inter-Bold"
        }
    }
}

public extension Font {
    /// Inter at a fixed point size. Letter spacing is applied per call site with `.tracking(_:)`.
    static func inter(_ size: CGFloat, _ weight: InterWeight) -> Font {
        Font.custom(weight.postScriptName, size: size)
    }
}

/// Chrome's rounded "normal" line box for Inter: `floor(x + 0.5)` of ascent and descent separately
/// (hhea values of the bundled TTFs, pinned by `FontTests`).
public enum InterMetrics {
    static let ascender: CGFloat = 1984
    static let descender: CGFloat = 494
    static let unitsPerEm: CGFloat = 2048

    private static func rounded(_ units: CGFloat, _ size: CGFloat) -> CGFloat { (units * size / unitsPerEm + 0.5).rounded(.down) }

    static func ascent(_ size: CGFloat) -> CGFloat { rounded(ascender, size) }
    static func descent(_ size: CGFloat) -> CGFloat { rounded(descender, size) }

    static func lineBox(_ size: CGFloat) -> CGFloat { ascent(size) + descent(size) }

    /// Baseline distance from the top of the line box: the rounded ascent. Where rounding shrinks the descent,
    /// Blink's Linux font metrics (the references' renderer) move one unit from ascent to descent so the
    /// glyphs are not cut off; the box keeps its height and the baseline rises by one.
    static func baselineOffset(_ size: CGFloat) -> CGFloat {
        let shrunk = descent(size) < descender * size / unitsPerEm
        return shrunk && ascent(size) >= 1 ? ascent(size) - 1 : ascent(size)
    }

    /// Height of the unrounded content area, which is what iOS lays out for one line.
    static func naturalHeight(_ size: CGFloat) -> CGFloat { (ascender + descender) / unitsPerEm * size }
}

public enum LineHeight {
    /// Chrome's rounded `line-height: normal`.
    case normal
    /// Explicit CSS `line-height: <factor>`: `size × factor`, unrounded.
    case factor(CGFloat)
}

extension LineHeight {
    /// Line box height and the baseline's distance from its top.
    func box(for size: CGFloat) -> (box: CGFloat, baseline: CGFloat) {
        switch self {
        case .normal:
            return (InterMetrics.lineBox(size), InterMetrics.baselineOffset(size))
        case .factor(let factor):
            let box = size * factor
            // Chrome floors the half-leading (LayoutNG `FontHeight::AddLeading`).
            return (box, ((box - InterMetrics.lineBox(size)) / 2).rounded(.down) + InterMetrics.baselineOffset(size))
        }
    }
}

public enum TextLines {
    case single
    case multi
}

public extension View {
    /// Inter text in Chrome's line box. A modifier cannot detect wrapping, so the caller states `lines`.
    /// Text inside a `TextField` keeps `.font(.inter(...))`: its container has a fixed height.
    @ViewBuilder
    func interText(
        _ size: CGFloat, _ weight: InterWeight, tracking: CGFloat = 0,
        lineHeight: LineHeight = .normal, lines: TextLines = .single
    ) -> some View {
        let styled = self.font(.inter(size, weight)).tracking(tracking)
        let (box, baseline) = lineHeight.box(for: size)
        switch lines {
        case .single:
            styled
                .lineLimit(1)
                .alignmentGuide(.top) { $0[.firstTextBaseline] - baseline }
                .frame(height: box, alignment: .top)
                .alignmentGuide(.firstTextBaseline) { _ in baseline }
        case .multi:
            styled.lineSpacing(max(0, box - InterMetrics.naturalHeight(size)))
        }
    }
}
