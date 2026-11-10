import SwiftUI

enum FlatnessAction {
    case open(AppRoute)
}

/// Estrich "Ebenheit · Wohnen" (IosEsEbenheit.dc.html:17-64): the straightedge sketch, the current card with the
/// instruction, the gap stepper (local `FlatnessViewModel`, never saved, D4), the live result and the photo / "Messpunkt
/// speichern" row, then the measured points as chips and the summary link. "Messpunkt speichern" appends the gap to
/// the local list; the summary link opens the defect form (D5, also with 0 over); the camera has no action. The
/// instruction, "Spalt Messpunkt / M4" and the summary link wrap greedily like Chrome (D13).
struct EsEbenheitScreen: View {
    let model: FlatnessCheckModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (FlatnessAction) -> Void
    @State private var flatness: FlatnessViewModel

    init(
        model: FlatnessCheckModel, backLabel: String, onBack: @escaping () -> Void,
        onAction: @escaping (FlatnessAction) -> Void
    ) {
        self.model = model
        self.backLabel = backLabel
        self.onBack = onBack
        self.onAction = onAction
        _flatness = State(initialValue: model.initial)
    }

    private var identifier: String {
        guard case let .inspectionCheck(tradeId, check) = model.route else { return "screen.inspection.screed.flatness" }
        return "screen.inspection.\(tradeId).\(check)"
    }

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack, trailing: .counter(model.counter))
            DetailTitle(model.title, subtitle: model.subtitle(limitText: flatness.limitText), style: .size28)
            FlatnessSketch(
                lineHeight: CGFloat(flatness.sketchLineHeight), line: flatness.isWithin ? Tokens.oFg : Tokens.eFg,
                lath: Tokens.accent, floor: Tokens.track.esEbenheit, background: Tokens.surface)
            currentCard
            measured
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier)
    }

    /// IosEsEbenheit:38-55: the current card (2 pt accent border, padding 16, gap 14).
    private var currentCard: some View {
        Card(style: .standard, padding: EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16), border: .current) {
            VStack(alignment: .leading, spacing: 14) {
                instruction
                HStack(alignment: .center, spacing: 12) {
                    GreedyLabel(
                        text: "\(model.stepperLabelPrefix)\(flatness.nextIndex)", size: 15, weight: .semibold,
                        color: Tokens.text, lineHeight: .normal)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    ValueStepper(
                        label: model.stepperAccessibilityNoun, value: flatness.gapText, valueMinWidth: 64,
                        valueSize: 17, onDecrement: { flatness.decrement() }, onIncrement: { flatness.increment() },
                        identifiers: (dec: "flatness.gap.decrement", value: "flatness.gap.value", inc: "flatness.gap.increment"))
                }
                AlertBanner(
                    icon: nil, title: flatness.resultText, subtitle: nil,
                    background: flatness.isWithin ? Tokens.oSoft : Tokens.eSoft.core,
                    foreground: flatness.isWithin ? Tokens.oFg : Tokens.eFg)
                    .accessibilityIdentifier("flatness.result")
                HStack(alignment: .top, spacing: 10) {
                    PhotoButton(label: model.photoLabel, action: nil)
                        .accessibilityIdentifier("flatness.photo")
                    PrimaryButton(model.saveLabel, size: .medium, action: { flatness.save() })
                        .accessibilityIdentifier("flatness.save")
                }
            }
        }
    }

    /// IosEsEbenheit:39: `line-height: 1.45` is 21.75 pt a line, but the reference puts the baselines of the three lines
    /// 22 pt apart (Chrome snaps them to whole points) inside a 65 pt block. A 22 pt line box with 1 pt trimmed off the
    /// bottom reproduces both.
    private var instruction: some View {
        GreedyLabel(
            text: model.instruction, size: 15, weight: .regular, color: Tokens.muted, lineHeight: .factor(22.0 / 15.0))
            .padding(.bottom, -1)
    }

    /// IosEsEbenheit:57-62: the heading, the chips (`oSoft` / `eSoft` when over) and the summary link, gap 8.
    private var measured: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(String(format: model.measuredFormat, flatness.points.count), style: .caps(inset: 4))
                .accessibilityIdentifier("flatness.count")
            FlowLayout(spacing: 8, lineSpacing: 8) {
                ForEach(flatness.points.indices, id: \.self) { index in
                    let over = flatness.isOver(index)
                    MeasureChip(
                        flatness.pointText(index), background: over ? Tokens.eSoft.core : Tokens.oSoft,
                        foreground: over ? Tokens.eFg : Tokens.oFg)
                        .accessibilityIdentifier("flatness.point.\(index)")
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            TextLink(flatness.summaryText, color: Tokens.err.core, action: { onAction(.open(model.defectTarget)) })
                .accessibilityIdentifier("flatness.defect")
        }
    }
}
