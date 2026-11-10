import SwiftUI

enum MeasureAction {
    case open(AppRoute)
}

/// Rohbau "Maße & Lot" (Nr. 18, IosRohbauMasse.dc.html:17-74). The measure value is local `@State` and is never
/// written (I6): the stepper changes the diff text and the result live. Only "Mangel" and the back bar act; the
/// camera, "Speichern", the code tiles and the plumb row have no action.
struct RohbauMasseScreen: View {
    let model: MeasureCheckModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (MeasureAction) -> Void
    @State private var measure: MeasureViewModel

    init(model: MeasureCheckModel, backLabel: String, onBack: @escaping () -> Void,
         onAction: @escaping (MeasureAction) -> Void) {
        self.model = model
        self.backLabel = backLabel
        self.onBack = onBack
        self.onAction = onAction
        _measure = State(initialValue: model.initial)
    }

    private var identifier: String {
        guard case let .inspectionCheck(tradeId, check) = model.route else { return "screen.inspection.check" }
        return "screen.inspection.\(tradeId).\(check)"
    }

    /// `grid-template-columns: 50px repeat(2, 1fr)`, gap 10: both text buttons are 122 wide in the 314 content box.
    private static let buttonWidth: CGFloat = 122

    var body: some View {
        ScreenScaffold(kind: .protocol) {
            BackBar(label: backLabel, action: onBack, trailing: .counter(model.counter))
            DetailTitle(model.title, subtitle: model.subtitle(tolerance: measure.tolerance), style: .size28)
            measureCard
            measuredSection
            Card(style: .row, padding: EdgeInsets()) {
                CheckRow(
                    title: model.plumbTitle, subtitle: model.plumbSubtitle, trailing: model.plumbValue,
                    fill: Tokens.oFg, ink: Tokens.surface, trailingColor: Tokens.oFg)
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier)
    }

    /// IosRohbauMasse:31-54: the current card (2 pt accent border, padding 16, gap 12).
    private var measureCard: some View {
        Card(style: .standard, padding: EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16), border: .current) {
            VStack(alignment: .leading, spacing: 12) {
                Text(model.instruction)
                    .interText(15, .regular, lineHeight: .factor(1.45), lines: .block)
                    .foregroundStyle(Tokens.muted.color)
                ForEach(model.dimensions, id: \.label) { dimension in
                    row(dimension)
                }
                result
                HStack(alignment: .top, spacing: 10) {
                    PhotoButton(label: model.photoLabel, action: nil)
                        .accessibilityIdentifier("measure.photo")
                    DefectButton(
                        model.defectLabel, size: .small,
                        action: { onAction(.open(model.defectTarget)) }
                    )
                    .frame(width: Self.buttonWidth)
                    .accessibilityIdentifier("measure.defect")
                    PrimaryButton(model.saveLabel, size: .small, action: nil)
                        .frame(width: Self.buttonWidth)
                        .accessibilityIdentifier("measure.save")
                }
            }
        }
    }

    /// Label over "Soll … mm · diff" on the left, the stepper on the right (gap 10).
    private func row(_ dimension: MeasureCheckModel.Dimension) -> some View {
        let key = Self.name(dimension.key)
        return HStack(spacing: 10) {
            VStack(alignment: .leading, spacing: 2) {
                Text(dimension.label)
                    .interText(15, .semibold)
                    .foregroundStyle(Tokens.text.color)
                HStack(alignment: .firstTextBaseline, spacing: 0) {
                    Text(String(format: ScreenCopy.RohbauMasse.targetFormat, Self.target(model.initial, dimension.key)))
                        .interText(13, .regular)
                        .foregroundStyle(Tokens.muted.color)
                    Text(measure.diffText(dimension.key))
                        .interText(13, .bold)
                        .foregroundStyle((measure.isWithin(dimension.key) ? Tokens.oFg : Tokens.eFg).color)
                        .accessibilityIdentifier("measure.\(key).diff")
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            ValueStepper(
                label: dimension.label, value: measure.valueText(dimension.key),
                onDecrement: { measure.decrement(dimension.key) },
                onIncrement: { measure.increment(dimension.key) },
                identifiers: (
                    dec: "measure.\(key).decrement", value: "measure.\(key).value",
                    inc: "measure.\(key).increment"
                ))
        }
    }

    /// IosRohbauMasse:46: the live result (`oSoft`/`oFg` within, `eSoft`/`eFg` outside).
    private var result: some View {
        let within = measure.isWithinTolerance
        return AlertBanner(
            icon: nil, title: within ? model.withinLabel : model.outsideLabel, subtitle: nil,
            background: within ? Tokens.oSoft : Tokens.eSoft.core,
            foreground: within ? Tokens.oFg : Tokens.eFg)
            .accessibilityIdentifier("measure.result")
    }

    /// IosRohbauMasse:56-62: "Bereits gemessen" and the 4-column tile grid (gap 6).
    private var measuredSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(model.measuredHeading, style: .caps(inset: 4))
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 6), count: 4), spacing: 6) {
                ForEach(model.measured, id: \.self) { code in
                    CodeTile(code, background: Tokens.oSoft, foreground: Tokens.oFg)
                }
            }
        }
    }

    private static func target(_ model: MeasureViewModel, _ dimension: MeasureViewModel.Dimension) -> Int {
        switch dimension {
        case .width: return model.target.width
        case .height: return model.target.height
        }
    }

    private static func name(_ dimension: MeasureViewModel.Dimension) -> String {
        switch dimension {
        case .width: return "width"
        case .height: return "height"
        }
    }
}
