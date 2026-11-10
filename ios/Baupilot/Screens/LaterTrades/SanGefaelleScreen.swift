import SwiftUI

enum SlopeAction {
    case open(AppRoute)
}

/// Sanitär "Gefälle Abwasser" (IosSanGefaelle.dc.html:17-83): four section tiles, then the current card with the
/// instruction, the length and drop steppers (local `SlopeViewModel`, never saved, D3/D12), the live result and the
/// photo / "Mangel" / "Speichern" row, then the note. Only "Mangel" and the back bar act (D5); the section tiles, the
/// camera and "Speichern" have no action (D12). The instruction and the note wrap greedily like Chrome (D13).
struct SanGefaelleScreen: View {
    let model: SlopeCheckModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (SlopeAction) -> Void
    @State private var slope: SlopeViewModel

    init(
        model: SlopeCheckModel, backLabel: String, onBack: @escaping () -> Void,
        onAction: @escaping (SlopeAction) -> Void
    ) {
        self.model = model
        self.backLabel = backLabel
        self.onBack = onBack
        self.onAction = onAction
        _slope = State(initialValue: model.initial)
    }

    private var identifier: String {
        guard case let .inspectionCheckItem(tradeId, check, item) = model.route else {
            return "screen.inspection.plumbing.slope.A3"
        }
        return "screen.inspection.\(tradeId).\(check).\(item)"
    }

    /// `grid-template-columns: 50px repeat(2, 1fr)`, gap 10: both text buttons are 122 wide in the 314 content box.
    private static let buttonWidth: CGFloat = 122

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack, trailing: .counter(model.counter))
            DetailTitle(model.title, subtitle: model.subtitle, style: .size28)
            SocketGrid(tiles: model.sections.map(tile), height: 52)
            currentCard
            note
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier)
    }

    /// IosSanGefaelle:42-77: the current card (2 pt accent border, padding 16, gap 14).
    private var currentCard: some View {
        Card(style: .standard, padding: EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16), border: .current) {
            VStack(alignment: .leading, spacing: 14) {
                GreedyLabel(
                    text: model.hint, size: 15, weight: .regular, color: Tokens.muted, lineHeight: .factor(1.45))
                HStack(alignment: .top, spacing: 10) {
                    stepper(
                        label: model.lengthLabel, value: slope.lengthText, key: "length",
                        onDecrement: { slope.decrementLength() }, onIncrement: { slope.incrementLength() })
                    stepper(
                        label: model.dropLabel, value: slope.dropText, key: "drop",
                        onDecrement: { slope.decrementDrop() }, onIncrement: { slope.incrementDrop() })
                }
                result
                HStack(alignment: .top, spacing: 10) {
                    PhotoButton(label: model.photoLabel, action: nil)
                        .accessibilityIdentifier("slope.photo")
                    DefectButton(model.defectLabel, size: .small, action: { onAction(.open(model.defectTarget)) })
                        .frame(width: Self.buttonWidth)
                        .accessibilityIdentifier("slope.defect")
                    OkButton(model.saveLabel, size: .small, action: nil)
                        .frame(width: Self.buttonWidth)
                        .accessibilityIdentifier("slope.save")
                }
            }
        }
    }

    /// A caption (12/600 `muted`, gap 6) over a stepper that fills its 152 pt column.
    private func stepper(
        label: String, value: String, key: String, onDecrement: @escaping () -> Void,
        onIncrement: @escaping () -> Void
    ) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .interText(12, .semibold)
                .foregroundStyle(Tokens.muted.color)
            ValueStepper(
                label: label, value: value, fillsWidth: true, onDecrement: onDecrement, onIncrement: onIncrement,
                identifiers: (dec: "slope.\(key).decrement", value: "slope.\(key).value", inc: "slope.\(key).increment"))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    /// IosSanGefaelle:68: `oSoft`/`oFg` when sufficient, `eSoft`/`eFg` when too flat.
    private var result: some View {
        let sufficient = slope.isSufficient
        return SlopeResult(
            percent: slope.percentText, verdict: slope.verdictText, schemaY2: CGFloat(slope.schemaY2),
            levelLabel: model.schemaLevelLabel, deltaLabel: model.schemaDeltaLabel,
            background: sufficient ? Tokens.oSoft : Tokens.eSoft.core,
            foreground: sufficient ? Tokens.oFg : Tokens.eFg,
            identifiers: (percent: "slope.percent", verdict: "slope.verdict"))
    }

    private var note: some View {
        InfoCallout(text: model.warning, icon: .info, tone: .neutralSurface, textLines: .block)
    }

    private func tile(_ section: SlopeCheckModel.Section) -> SocketTile {
        SocketTile(
            id: section.id, label: section.label, style: Self.style(section.status),
            accessibilityLabel: "Abschnitt \(section.id): \(section.label)",
            identifier: "slope.section.\(section.id)", height: 52)
    }

    /// The prototype's `S` map (IosSanGefaelle:98-103): fill, text colour and the `now` tile's 2 pt accent border.
    private static func style(_ status: SectionStatus) -> SocketTile.Style {
        switch status {
        case .ok: return SocketTile.Style(background: Tokens.oSoft, foreground: Tokens.oFg)
        case .defect: return SocketTile.Style(background: Tokens.eSoft.core, foreground: Tokens.eFg)
        case .now: return SocketTile.Style(background: Tokens.aSoft, foreground: Tokens.aFg, border: Tokens.accent)
        case .open: return SocketTile.Style(background: Tokens.chip.core, foreground: Tokens.muted)
        }
    }
}
