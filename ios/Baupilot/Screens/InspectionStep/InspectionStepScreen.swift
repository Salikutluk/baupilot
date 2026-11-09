import SwiftUI

enum StepAction {
    case open(AppRoute)
}

/// Prüfschritt F8 (IosAbnPruefung.dc.html:18-70).
struct InspectionStepScreen: View {
    let model: StepModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (StepAction) -> Void

    private var identifier: String {
        guard case let .inspectionStep(tradeId, elementId, step) = model.route else { return "screen.inspection.step" }
        return "screen.inspection.\(tradeId).\(elementId).step.\(step)"
    }

    var body: some View {
        ScreenScaffold(kind: .detailNoBar) {
            BackBar(label: backLabel, action: onBack, trailing: .counter(model.counter))
            DetailTitle(model.title, subtitle: model.subtitle, style: .size28)
            VStack(alignment: .leading, spacing: 8) {
                ForEach(Array(model.done.enumerated()), id: \.offset) { index, step in
                    StepRow(number: index + 1, title: step.label, state: .done, meta: .text(step.meta))
                }
                currentCard
                ForEach(model.next, id: \.n) { step in
                    nextRow(step)
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier)
    }

    /// IosAbnPruefung:61-65: an open step whose label wraps (`StepRow` keeps one line, and is outside this task).
    private func nextRow(_ step: StepModel.Next) -> some View {
        HStack(spacing: 12) {
            StepRow.OpenRing(number: step.n, track: Tokens.track.abnPruefung)
            Text(step.label)
                .interText(15, .regular, lines: .block)
                .foregroundStyle(Tokens.text.color)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.horizontal, 14)
        .frame(minHeight: 44)
        .accessibilityElement(children: .combine)
    }

    /// IosAbnPruefung:43-69: the tile grid (3 columns, gap 8) and the two buttons (2 columns, gap 10).
    private var currentCard: some View {
        CurrentStepCard(number: model.current.n, title: model.current.title) {
            // 15 muted, line-height 1.45 as a CSS block: n line boxes tall (`.block`; `CurrentStepCard.body` adds the
            // leading between lines only and ends 6 px short of the reference).
            Text(model.current.text)
                .interText(15, .regular, lineHeight: .factor(1.45), lines: .block)
                .foregroundStyle(Tokens.muted.color)
            HStack(spacing: 8) {
                PhotoTile(
                    kind: .guide(
                        title: model.guideLabel,
                        illustration: AnyView(
                            WindowLevelSketchView(
                                stroke: Tokens.muted, level: Tokens.accent, bubble: Tokens.chip.core))),
                    height: .h84)
                PhotoTile(
                    kind: .scene(AnyView(PhotoPlaceholder()), caption: model.current.photoCaption, checked: true),
                    height: .h84)
                PhotoTile(kind: .add(label: model.current.addPhotoLabel), height: .h84)
            }
            HStack(alignment: .top, spacing: 10) {
                DefectButton(
                    model.defectLabel, size: .medium, showsIcon: true,
                    action: { onAction(.open(model.defectTarget)) }
                )
                .accessibilityIdentifier("step.defect")
                OkButton(model.okLabel, size: .medium, showsIcon: true, action: nil)
            }
        }
    }
}
