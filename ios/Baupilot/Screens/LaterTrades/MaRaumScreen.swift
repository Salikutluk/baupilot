import SwiftUI

/// Maler Raumprüfung Wohnen (IosMaRaum.dc.html:17-70): the done row with the paint swatch, the current card (raking-light
/// sketch, greedy instruction, the wall chips, the camera, "Mangel" and "In Ordnung") and the open steps. "Mangel"
/// opens the defect form (D5); the wall chips, the camera and "In Ordnung" are accessible buttons without an action (D12).
struct MaRaumScreen: View {
    let model: PaintCheckModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (CheckStepAction) -> Void

    /// Width of "Mangel" and "In Ordnung": (314 − 50 − 2 × 10) / 2 in the card's content box (grid 50 + 2 × 1fr, gap 10).
    private static let buttonWidth: CGFloat = 122

    private var identifier: String {
        guard case let .inspectionRoom(tradeId, roomId) = model.route else { return "screen.inspection.painting.room.living" }
        return "screen.inspection.\(tradeId).room.\(roomId)"
    }

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack, trailing: .counter(model.counter))
            DetailTitle(model.title, subtitle: model.subtitle, style: .size28)
            VStack(spacing: 6) {
                ForEach(Array(model.done.enumerated()), id: \.offset) { index, label in
                    StepRow(
                        number: index + 1, title: label, state: .done,
                        meta: .swatchSquare(
                            Self.paintToken(model.doneSwatch), side: 18, radius: 5, border: Tokens.border.maRaum))
                }
            }
            currentCard
            VStack(spacing: 2) {
                ForEach(model.next, id: \.n) { step in
                    StepRow(
                        number: step.n, title: step.label, state: .open(), minHeight: 40, track: Tokens.track.maRaum,
                        titleLines: .greedy)
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier)
    }

    /// IosMaRaum:38-62: the current card (gap 12).
    private var currentCard: some View {
        CurrentStepCard(number: model.current.n, title: model.current.title, spacing: 12) {
            RakingLightSketch(
                lit: Tokens.lit, wall: Tokens.wall, lamp: Tokens.text, glow: Tokens.lampGlow, shadow: Tokens.shadow)
            GreedyLabel(
                text: model.current.text, size: 15, weight: .regular, color: Tokens.muted, lineHeight: .factor(1.45))
            walls
            HStack(alignment: .top, spacing: 10) {
                PhotoButton(label: model.photoLabel, action: nil)
                    .accessibilityIdentifier("paint.photo")
                DefectButton(model.defectLabel, size: .small, action: { onAction(.open(model.defectTarget)) })
                    .frame(width: Self.buttonWidth)
                    .accessibilityIdentifier("step.defect")
                OkButton(model.okLabel, size: .small, action: nil)
                    .frame(width: Self.buttonWidth)
                    .accessibilityIdentifier("step.ok")
            }
        }
    }

    /// IosMaRaum:50-54: chips of 36 with gap 6 that wrap ("Decke" lands on the second line).
    private var walls: some View {
        FlowLayout(spacing: 6, lineSpacing: 6) {
            ForEach(Array(model.walls.enumerated()), id: \.offset) { index, wall in
                SelectionChip(
                    wall.name, state: Self.state(wall.state), openBorder: Tokens.border.maRaum, showsTick: false,
                    borderBox: true, accessibilityLabel: wall.name, isButton: true)
                    .accessibilityIdentifier("paint.wall.\(index)")
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .accessibilityElement(children: .contain)
        .accessibilityLabel(model.wallsLabel)
    }

    private static func state(_ state: CheckItemState) -> SelectionChip.State {
        switch state {
        case .done: return .done
        case .now: return .active
        case .open: return .open
        }
    }

    private static func paintToken(_ paint: PaintColor) -> ColorToken {
        switch paint {
        case .white: return Tokens.paintWhite
        case .sand: return Tokens.paintSand
        case .sage: return Tokens.paintSage
        }
    }
}
