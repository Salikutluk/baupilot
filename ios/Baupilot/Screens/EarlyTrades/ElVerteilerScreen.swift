import SwiftUI

/// Elektro Zählerschrank & FI-Test (Nr. 22, IosElVerteiler.dc.html:17-80): the warning callout, the two done rows, the
/// current card (instructions, FI status list, the two outcome buttons) and the open steps. "Löst nicht aus" opens the
/// defect form; the ok button and the FI rows have no action (I6). Per-screen tokens: the `elVerteiler` chip, divider
/// and track; core tokens for the rest.
struct ElVerteilerScreen: View {
    let model: PanelCheckModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (CheckStepAction) -> Void

    private var identifier: String {
        guard case let .inspectionCheck(tradeId, check) = model.route else { return "screen.inspection.panel" }
        return "screen.inspection.\(tradeId).\(check)"
    }

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .tight) {
            BackBar(label: backLabel, action: onBack, trailing: .counter(model.counter))
            DetailTitle(model.title, subtitle: nil, style: .size28)
            InfoCallout(
                text: model.callout, icon: .boltW22, tone: .warn, textSize: 14, iconSize: 20, textLines: .block)
            VStack(spacing: 6) {
                ForEach(Array(model.done.enumerated()), id: \.offset) { index, step in
                    StepRow(number: index + 1, title: step.label, state: .done, meta: .text(step.meta))
                }
            }
            currentCard
            VStack(spacing: 2) {
                ForEach(model.next, id: \.n) { step in
                    StepRow(
                        number: step.n, title: step.label, state: .open(tag: step.tag),
                        track: Tokens.track.elVerteiler, titleLines: .greedy)
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier)
    }

    /// IosElVerteiler:38-64: the current card (gap 12): numbered instructions, the FI list, two buttons (grid, gap 10).
    private var currentCard: some View {
        CurrentStepCard(number: model.current.n, title: model.current.title, spacing: 12) {
            NumberedList(items: model.current.steps, color: Tokens.muted)
            StatusList(
                rows: model.fis.map { fi in
                    StatusList.Row(
                        title: fi.name, subtitle: fi.sub,
                        badge: StatusBadge(fi.statusLabel, tone: Self.badgeTone(fi.tone)))
                },
                background: Tokens.chip.elVerteiler, divider: Tokens.border.elVerteiler)
            HStack(alignment: .top, spacing: 10) {
                DefectButton(
                    model.outcomes[0], size: .small,
                    action: { onAction(.open(model.defectTarget)) }
                )
                .accessibilityIdentifier("step.defect")
                OkButton(model.outcomes[1], size: .small, action: nil)
                    .accessibilityIdentifier("step.ok")
            }
        }
    }

    /// IosElVerteiler:92-95 (D7): Ausgelöst is the green `oSoft`/`oFg` badge, Jetzt testen the grey `open` one.
    private static func badgeTone(_ tone: FiTone) -> StatusBadge.Tone {
        switch tone {
        case .ok: return .ok
        case .open: return .open
        }
    }
}
