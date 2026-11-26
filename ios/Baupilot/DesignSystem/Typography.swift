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

    /// The line box at a Dynamic Type category. At `.large` and below it is `box(for: size)`; above, the same formula
    /// at the size `Font.custom` renders (the body text-style curve).
    func box(for size: CGFloat, category: UIContentSizeCategory) -> (box: CGFloat, baseline: CGFloat) {
        box(for: InterMetrics.scaledSize(size, category: category))
    }
}

extension InterMetrics {
    /// `size` grown along the body text-style curve and rounded to a whole point, which is what `Font.custom` and
    /// `UIFontMetrics.scaledFont` render; unchanged at `.large` and below.
    static func scaledSize(_ size: CGFloat, category: UIContentSizeCategory) -> CGFloat {
        guard category > .large else { return size }
        return UIFontMetrics(forTextStyle: .body).scaledValue(
            for: size, compatibleWith: UITraitCollection(preferredContentSizeCategory: category)
        ).rounded()
    }
}

extension UIContentSizeCategory {
    init(_ size: DynamicTypeSize) {
        switch size {
        case .xSmall: self = .extraSmall
        case .small: self = .small
        case .medium: self = .medium
        case .large: self = .large
        case .xLarge: self = .extraLarge
        case .xxLarge: self = .extraExtraLarge
        case .xxxLarge: self = .extraExtraExtraLarge
        case .accessibility1: self = .accessibilityMedium
        case .accessibility2: self = .accessibilityLarge
        case .accessibility3: self = .accessibilityExtraLarge
        case .accessibility4: self = .accessibilityExtraExtraLarge
        case .accessibility5: self = .accessibilityExtraExtraExtraLarge
        @unknown default: self = .large
        }
    }
}

public enum TextLines {
    /// One line that may wrap when the text grows past `.large` (Dynamic Type).
    case single
    /// One line that stays one line and truncates at every size. Only where the prototype element has
    /// `white-space: nowrap` / `text-overflow: ellipsis`; cite the prototype line at the call site.
    case singleTruncating
    /// Wrapping text laid out with `lineSpacing` only (the geometry `StepRow` and `BottomActionBar` are tuned to).
    case multi
    /// Wrapping text as a CSS block: n line boxes tall, first baseline where Chrome puts it.
    case block
    /// Wrapping text that breaks greedily like Chrome (no last-word push-down). A modifier cannot read the string,
    /// so a component that owns it renders `.greedy` through `GreedyLabel`; `interText` itself lays the view out as
    /// `.block`, the same n-line-box geometry.
    case greedy
}

public extension View {
    /// Inter text in Chrome's line box. A modifier cannot detect wrapping, so the caller states `lines`.
    /// Text inside a `TextField` keeps `.font(.inter(...))`: its container has a fixed height.
    func interText(
        _ size: CGFloat, _ weight: InterWeight, tracking: CGFloat = 0,
        lineHeight: LineHeight = .normal, lines: TextLines = .single
    ) -> some View {
        modifier(InterTextModifier(size: size, weight: weight, tracking: tracking, lineHeight: lineHeight, lines: lines))
    }

    /// A fixed-height container that holds text: exactly `height` at `.large` and below, at least `height` above, so
    /// larger text is not clipped.
    func scaledFrame(height: CGFloat, alignment: Alignment = .center) -> some View {
        modifier(ScaledFrameModifier(height: height, alignment: alignment))
    }
}

struct ScaledFrameModifier: ViewModifier {
    let height: CGFloat
    let alignment: Alignment

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    // One modifier chain at every size, no `if`/`else` branch: with a conditional body here, rendering Login before
    // FlUebersicht moved FlUebersicht's status badges by 3 pt (docs/m7-ios-offene-punkte.md, "FlUebersicht_light").
    func body(content: Content) -> some View {
        let grows = UIContentSizeCategory(dynamicTypeSize) > .large
        // `fixedSize` so a stack that runs out of room cannot compress the text into an ellipsis.
        return content
            .frame(minHeight: height, maxHeight: grows ? nil : height, alignment: alignment)
            .fixedSize(horizontal: false, vertical: grows)
    }
}

struct InterTextModifier: ViewModifier {
    let size: CGFloat
    let weight: InterWeight
    let tracking: CGFloat
    let lineHeight: LineHeight
    let lines: TextLines

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    @ViewBuilder
    func body(content: Content) -> some View {
        let category = UIContentSizeCategory(dynamicTypeSize)
        let grown = category > .large
        // The glyphs grow with the environment on their own (`Font.custom`); the boxes follow the same curve.
        let effective = InterMetrics.scaledSize(size, category: category)
        let (box, baseline) = lineHeight.box(for: size, category: category)
        let styled = content.font(.inter(size, weight)).tracking(tracking)
        switch lines {
        case .single where grown:
            styled
                .alignmentGuide(.top) { $0[.firstTextBaseline] - baseline }
                .frame(minHeight: box, alignment: .top)
                .fixedSize(horizontal: false, vertical: true)
                .alignmentGuide(.firstTextBaseline) { _ in baseline }
        case .single, .singleTruncating:
            styled
                .lineLimit(1)
                .alignmentGuide(.top) { $0[.firstTextBaseline] - baseline }
                .frame(height: box, alignment: .top)
                .alignmentGuide(.firstTextBaseline) { _ in baseline }
        case .multi:
            styled.lineSpacing(max(0, box - InterMetrics.naturalHeight(effective)))
        case .block, .greedy:
            let natural = InterMetrics.naturalHeight(effective)
            let leading = max(0, box - natural)
            // iOS puts `lineSpacing` between lines only and rounds a multi-line height up to the pixel grid; a CSS
            // block of n lines is exactly n line boxes tall. The layout counts the lines and reports `n × box`.
            BlockTextLayout(
                box: box, natural: natural, leading: leading,
                top: baseline - InterMetrics.ascender / InterMetrics.unitsPerEm * effective
            ) {
                styled.lineSpacing(leading)
            }
        }
    }
}

/// Lays one wrapping `Text` out as a CSS block: n line boxes tall, the first baseline where Chrome puts it.
struct BlockTextLayout: Layout {
    /// Line box height.
    let box: CGFloat
    /// Unrounded height of one line of text.
    let natural: CGFloat
    /// Space SwiftUI adds between lines (`box − natural`).
    let leading: CGFloat
    /// Offset of the text's own top below the block's top (Chrome's baseline minus the text's ascent).
    let top: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        guard let text = subviews.first else { return .zero }
        let fitted = text.sizeThatFits(ProposedViewSize(width: proposal.width, height: nil))
        let lines = max(1, ((fitted.height + leading) / (natural + leading)).rounded())
        return CGSize(width: fitted.width, height: lines * box)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        subviews.first?.place(
            at: CGPoint(x: bounds.minX, y: bounds.minY + top), anchor: .topLeading,
            proposal: ProposedViewSize(width: bounds.width, height: nil))
    }
}
