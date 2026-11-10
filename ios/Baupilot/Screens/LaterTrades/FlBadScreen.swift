import SwiftUI

/// Fliesen Raumprüfung Bad (IosFlBad.dc.html:17-68): the done rows, the current card (greedy instruction, legend, the
/// 8 × 3 tile raster, the result line, "Als Mangel" and "Keine Hohlstellen") and the open steps. "Als Mangel" opens the
/// defect form (D5); the tiles and "Keine Hohlstellen" are accessible buttons without an action (D12).
struct FlBadScreen: View {
    let model: TileCheckModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (CheckStepAction) -> Void

    private var identifier: String {
        guard case let .inspectionRoom(tradeId, roomId) = model.route else { return "screen.inspection.tiles.room.bath" }
        return "screen.inspection.\(tradeId).room.\(roomId)"
    }

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack, trailing: .counter(model.counter))
            DetailTitle(model.title, subtitle: model.subtitle, style: .size28, subtitleLines: .greedy)
            VStack(spacing: 6) {
                ForEach(Array(model.done.enumerated()), id: \.offset) { index, label in
                    StepRow(number: index + 1, title: label, state: .done)
                }
            }
            currentCard
            VStack(spacing: 2) {
                ForEach(model.next, id: \.n) { step in
                    StepRow(number: step.n, title: step.label, state: .open(), minHeight: 40, track: Tokens.track.flBad)
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier)
    }

    /// IosFlBad:38-62: the current card (gap 12); the legend pulls the raster up by 6 (`margin-bottom: -6px`).
    private var currentCard: some View {
        CurrentStepCard(number: model.current.n, title: model.current.title, spacing: 12) {
            GreedyLabel(
                text: model.current.text, size: 15, weight: .regular, color: Tokens.muted, lineHeight: .factor(1.45))
            LegendRow(
                leading: model.wallLabel, swatchLabel: model.markHint, swatchFill: Tokens.eSoft.flBad,
                swatchBorder: Tokens.err.flBad, color: Tokens.muted)
                .padding(.bottom, -6)
            TileGrid(
                tiles: model.tiles.map(tile), columns: model.columns, radius: 10, tileRadius: 3,
                background: Tokens.grout, accessibilityLabel: model.gridAccessibilityLabel)
            Text(model.result)
                .interText(13, .semibold)
                .foregroundStyle(Tokens.eFg.color)
            HStack(spacing: 10) {
                DefectButton(
                    model.defectLabel, style: .filled(fill: Tokens.err.flBad, ink: Tokens.onDangerFill),
                    action: { onAction(.open(model.defectTarget)) })
                    .accessibilityIdentifier("tiles.defect")
                NeutralOutlineButton(model.noneLabel, action: nil)
                    .accessibilityIdentifier("tiles.none")
            }
        }
    }

    private func tile(_ tile: TileCheckModel.Tile) -> TileGrid.Tile {
        TileGrid.Tile(
            id: tile.id,
            style: tile.hollow
                ? TileGrid.Tile.Style(fill: Tokens.eSoft.flBad, border: Tokens.err.flBad, labelColor: Tokens.eFg)
                : TileGrid.Tile.Style(fill: Tokens.tile, border: nil, labelColor: Tokens.eFg),
            label: tile.label, accessibilityLabel: tile.accessibilityLabel, identifier: "tiles.tile.\(tile.id)")
    }
}
