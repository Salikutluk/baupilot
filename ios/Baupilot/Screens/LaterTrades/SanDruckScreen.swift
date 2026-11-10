import SwiftUI

/// Sanitär Druckprobe Trinkwasser (IosSanDruck.dc.html:17-82): the two done rows, the current card (timer ring with
/// its note, the read-only start and the editable end reading, the photo button) and the two open steps. The end
/// field is a local, never saved text field (D12); the photo button has no action (D12). The subtitle, the note and
/// the open step titles wrap greedily like Chrome (D13).
struct SanDruckScreen: View {
    let model: PressureCheckModel
    let backLabel: String
    let onBack: () -> Void
    @State private var end = ""

    private var identifier: String {
        guard case let .inspectionCheck(tradeId, check) = model.route else { return "screen.inspection.plumbing.pressure" }
        return "screen.inspection.\(tradeId).\(check)"
    }

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack, trailing: .counter(model.counter))
            DetailTitle(model.title, subtitle: model.subtitle, style: .size28, subtitleLines: .greedy)
            VStack(spacing: 6) {
                ForEach(Array(model.done.enumerated()), id: \.offset) { index, step in
                    StepRow(number: index + 1, title: step.label, state: .done, meta: .strong(step.meta))
                }
            }
            currentCard
            VStack(spacing: 2) {
                ForEach(model.next, id: \.n) { step in
                    StepRow(
                        number: step.n, title: step.label, state: .open(tag: step.tag),
                        track: Tokens.track.sanDruck, titleLines: .greedy)
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier)
    }

    /// IosSanDruck:38-79: the current card (gap 14): ring and note, the two reading columns of 152 (gap 10), the photo.
    private var currentCard: some View {
        CurrentStepCard(number: model.current.n, title: model.current.title, spacing: 14) {
            HStack(spacing: 14) {
                TimerRing(
                    text: model.timer.remaining, fraction: model.timer.fraction,
                    accessibilityLabel: model.timer.accessibilityLabel, fill: Tokens.accent,
                    track: Tokens.track.sanDruck, hole: Tokens.surface, holeDiameter: 56, textWeight: .bold)
                    .accessibilityIdentifier("pressure.timer")
                VStack(alignment: .leading, spacing: 2) {
                    Text(model.timer.remainingLabel)
                        .interText(15, .semibold)
                        .foregroundStyle(Tokens.text.color)
                    GreedyLabel(
                        text: model.timer.note, size: 13, weight: .regular, color: Tokens.muted,
                        lineHeight: .factor(1.4))
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            HStack(alignment: .top, spacing: 10) {
                ReadingField(
                    label: model.timer.startLabel, content: .value(model.timer.startValue),
                    identifier: "pressure.start")
                ReadingField(
                    label: model.timer.endLabel,
                    content: .input(text: $end, placeholder: model.timer.endPlaceholder),
                    identifier: "pressure.end")
            }
            PhotoButton.wide(model.photoLabel, action: nil)
                .accessibilityIdentifier("pressure.photo")
        }
    }
}
