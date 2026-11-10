import SwiftUI

/// One check of an object (IosSanBad:48-60): a title (15/600) over a hint (13 × 1.35 `muted`) with gap 2, and the
/// ✕ / ✓ pair of 44 pt buttons (gap 6, r12, 1 pt `outline` border inside). The row is at least 56 tall (content); from
/// the second row on a 1 pt `divider` line sits on top. When `ok` the ✓ is filled `okFill` with an `okInk` tick and no
/// border. Both buttons are shown as buttons without an action.
public struct ObjectCheckRow: View {
    private enum Metrics {
        static let minHeight: CGFloat = 56
        static let button: CGFloat = 44
        static let radius: CGFloat = 12
    }

    private let title: String
    private let hint: String
    private let ok: Bool
    private let showsDivider: Bool
    private let okFill: ColorToken
    private let okInk: ColorToken
    private let outline: ColorToken
    private let divider: ColorToken
    private let okAccessibilityLabel: String
    private let defectAccessibilityLabel: String
    private let identifiers: (ok: String, defect: String)?

    /// The two accessibility labels are the prototype's `aria-label`s ("<title>: in Ordnung" / "<title>: Mangel").
    public init(
        title: String, hint: String, ok: Bool, showsDivider: Bool, okFill: ColorToken, okInk: ColorToken,
        outline: ColorToken, divider: ColorToken, okAccessibilityLabel: String, defectAccessibilityLabel: String,
        identifiers: (ok: String, defect: String)?
    ) {
        self.title = title
        self.hint = hint
        self.ok = ok
        self.showsDivider = showsDivider
        self.okFill = okFill
        self.okInk = okInk
        self.outline = outline
        self.divider = divider
        self.okAccessibilityLabel = okAccessibilityLabel
        self.defectAccessibilityLabel = defectAccessibilityLabel
        self.identifiers = identifiers
    }

    public var body: some View {
        VStack(spacing: 0) {
            if showsDivider {
                Rectangle().fill(divider.color).frame(height: 1)
            }
            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 2) {
                    GreedyLabel(text: title, size: 15, weight: .semibold, color: Tokens.text, lineHeight: .normal)
                    GreedyLabel(text: hint, size: 13, weight: .regular, color: Tokens.muted, lineHeight: .factor(1.35))
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                HStack(spacing: 6) {
                    markButton(.xW26, filled: false, label: defectAccessibilityLabel, identifier: identifiers?.defect)
                    markButton(.checkW30, filled: ok, label: okAccessibilityLabel, identifier: identifiers?.ok)
                }
            }
            .frame(minHeight: Metrics.minHeight)
        }
    }

    private func markButton(_ icon: BaupilotIcon, filled: Bool, label: String, identifier: String?) -> some View {
        let shape = RoundedRectangle(cornerRadius: Metrics.radius)
        return Icon(icon, size: 18, color: (filled ? okInk : Tokens.muted).color)
            .frame(width: Metrics.button, height: Metrics.button)
            .background(filled ? okFill.color : .clear, in: shape)
            .overlay {
                if !filled { shape.strokeBorder(outline.color, lineWidth: 1) }
            }
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(Text(label))
            .accessibilityAddTraits(.isButton)
            .accessibilityIdentifier(identifier ?? "")
    }
}
