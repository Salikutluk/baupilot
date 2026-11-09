import SwiftUI

enum SignOffAction {
    case finish(NavigationTarget)
}

/// Abnahme & Unterschrift (IosAbnSignatur.dc.html:18-74). The owner's strokes live in the screen (D10) and write
/// nothing (I8); the owner status stays "Ausstehend" because the fixture has no signed text for the owner.
struct SignOffScreen: View {
    let model: SignOffModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (SignOffAction) -> Void

    @State private var strokes = SignaturePad.emptyStrokes

    var body: some View {
        ScreenScaffold(kind: .detail) {
            BackBar(label: backLabel, action: onBack)
            DetailTitle(model.title, subtitle: model.subtitle, style: .size28)
            HStack(spacing: 8) {
                ForEach(Array(model.stats.enumerated()), id: \.offset) { _, stat in
                    CountTile(value: stat.value, label: stat.label, tone: tone(stat.tone))
                }
            }
            Card(style: .row, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ForEach(Array(model.defects.enumerated()), id: \.offset) { index, defect in
                        DotRow(
                            dot: Tokens.err.abnSignatur, title: defect.title, subtitle: defect.meta,
                            trailing: defect.due, showsDivider: index > 0, divider: Tokens.border.abnSignatur)
                    }
                }
            }
            Text(model.reservation)
                .interText(13, .regular, lineHeight: .factor(1.4), lines: .block)
                .foregroundStyle(Tokens.muted.color)
                .padding(.top, -4)
                .padding(.horizontal, 4)
            HStack(alignment: .top, spacing: 10) {
                SignatureCard(
                    style: .signed(
                        name: model.craftsmanName, status: model.craftsmanStatus, statusColor: Tokens.oFg),
                    caption: model.craftsmanLabel, divider: Tokens.border.abnSignatur,
                    padAccessibilityLabel: model.signatureLabel, identifier: "sign.craftsman")
                SignatureCard(
                    style: .pending(
                        name: model.ownerName, status: model.ownerPending, placeholder: model.signHereLabel,
                        strokes: $strokes),
                    caption: model.ownerLabel, divider: Tokens.border.abnSignatur,
                    padAccessibilityLabel: model.ownerLabel, identifier: "sign.owner")
            }
            .fixedSize(horizontal: false, vertical: true)
        } bottomBar: {
            BottomActionBar(bottomPadding: .withCaption, caption: model.note, border: Tokens.border.abnSignatur) {
                PrimaryButton(model.finishLabel, action: { onAction(.finish(model.finishTarget)) })
                    .accessibilityIdentifier("sign.finish")
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.inspection.\(model.inspectionId).sign")
    }

    private func tone(_ tone: CountTone) -> CountTile.Tone {
        switch tone {
        case .surface: return .surface
        case .ok: return .ok
        case .error: return .error
        }
    }
}
