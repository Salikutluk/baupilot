import SwiftUI

/// Sanitär Bad OG (IosSanBad.dc.html:17-66): the back bar with the phase on its right, the title with the count, the
/// object tabs (wrapping chips), the card of the selected object with its ✕ / ✓ checks and the note about the next
/// object. The tabs and the ✓ / ✕ buttons are shown as buttons and have no action (D12, §6). The check titles, hints and
/// the note wrap greedily like Chrome (D13).
struct SanBadScreen: View {
    let model: ObjectCheckModel
    let backLabel: String
    let onBack: () -> Void

    private var identifier: String {
        guard case let .inspectionRoom(tradeId, roomId) = model.route else { return "screen.inspection.plumbing.room.bathUpper" }
        return "screen.inspection.\(tradeId).room.\(roomId)"
    }

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack, trailing: .counter(model.trailing))
            DetailTitle(model.title, subtitle: model.counter, style: .size28)
            tabs
            card
            InfoCallout(text: model.nextHint, icon: .info, tone: .neutralSurface, textLines: .greedy)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier)
    }

    /// IosSanBad:33-45: chips of 36 with gap 8 that wrap onto a second line.
    private var tabs: some View {
        FlowLayout(spacing: 8, lineSpacing: 8) {
            ForEach(Array(model.objects.enumerated()), id: \.offset) { index, object in
                SelectionChip(
                    object.name, state: Self.state(object.state), openBorder: Tokens.outline, showsTick: true,
                    borderBox: true, accessibilityLabel: object.name, isButton: true)
                    .accessibilityIdentifier("object.tab.\(index)")
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .accessibilityElement(children: .contain)
        .accessibilityLabel(model.tabsLabel)
    }

    /// IosSanBad:47-64: the current card (2 pt accent border, padding 16, gap 4); the heading's 6 pt bottom margin
    /// makes 10 to the first row.
    private var card: some View {
        Card(style: .standard, padding: EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16), border: .current) {
            VStack(alignment: .leading, spacing: 4) {
                Text(model.heading)
                    .interText(19, .bold)
                    .foregroundStyle(Tokens.text.color)
                    .accessibilityAddTraits(.isHeader)
                    .padding(.bottom, 6)
                ForEach(Array(model.checks.enumerated()), id: \.offset) { index, check in
                    ObjectCheckRow(
                        title: check.label, hint: check.hint, ok: check.ok, showsDivider: index > 0,
                        okFill: Tokens.ok.sanBad, okInk: Tokens.okInk.sanBad, outline: Tokens.outline,
                        divider: Tokens.border.core,
                        okAccessibilityLabel: "\(check.label)\(Self.okSuffix)",
                        defectAccessibilityLabel: "\(check.label)\(Self.defectSuffix)",
                        identifiers: (ok: "object.check.\(index).ok", defect: "object.check.\(index).defect"))
                }
            }
        }
    }

    /// IosSanBad:57-61 `aria-label`s: the check label plus these.
    private static let okSuffix = ": in Ordnung"
    private static let defectSuffix = ": Mangel"

    private static func state(_ state: CheckItemState) -> SelectionChip.State {
        switch state {
        case .done: return .done
        case .now: return .active
        case .open: return .open
        }
    }
}
