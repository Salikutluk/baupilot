import SwiftUI

enum CheckStepAction {
    case open(AppRoute)
}

/// Elektro Raumprüfung Küche (Nr. 21, IosElRaum.dc.html:17-70): the done step, the current card with the socket
/// tiles, the progress row and the two outcome buttons, then the open steps. "Mangel" opens the defect form; the ok
/// button and the socket tiles have no action (I6). Core tokens plus the `elRaum` track of the open step rings.
struct ElRaumScreen: View {
    let model: RoomCheckModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (CheckStepAction) -> Void

    private var identifier: String {
        switch model.route {
        case let .inspectionRoom(tradeId, roomId): return "screen.inspection.\(tradeId).room.\(roomId)"
        case let .inspectionCheck(tradeId, check): return "screen.inspection.\(tradeId).\(check)"
        default: return "screen.inspection.room"
        }
    }

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack, trailing: .counter(model.counter))
            DetailTitle(model.title, subtitle: model.subtitle, style: .size28, subtitleLines: .greedy)
            ForEach(Array(model.done.enumerated()), id: \.offset) { index, step in
                StepRow(number: index + 1, title: step.label, state: .done, meta: .ok(step.meta), minHeight: 48)
            }
            currentCard
            VStack(alignment: .leading, spacing: 2) {
                ForEach(model.next, id: \.n) { step in
                    StepRow(
                        number: step.n, title: step.label, state: .open(),
                        track: Tokens.track.elRaum, titleLines: .greedy)
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier)
    }

    /// IosElRaum:31-62: the current card (gap 14): text, socket tiles (4 columns, gap 8), progress row, buttons (gap 10).
    private var currentCard: some View {
        CurrentStepCard(number: model.current.n, title: model.current.title, spacing: 14) {
            GreedyLabel(
                text: model.current.text, size: 15, weight: .regular, color: Tokens.muted, lineHeight: .factor(1.45))
            SocketGrid(tiles: model.sockets.map(tile))
            HStack {
                Text(model.progressLabel)
                    .interText(13, .regular)
                    .foregroundStyle(Tokens.muted.color)
                Spacer(minLength: 0)
                Text(model.defectNote)
                    .interText(13, .semibold)
                    .foregroundStyle(Tokens.eFg.color)
            }
            HStack(alignment: .top, spacing: 10) {
                DefectButton(
                    model.defectLabel, size: .medium,
                    action: { onAction(.open(model.defectTarget)) }
                )
                .accessibilityIdentifier("step.defect")
                OkButton(model.okNote, size: .medium, action: nil)
                    .accessibilityIdentifier("step.ok")
            }
        }
    }

    private func tile(_ socket: RoomCheckModel.Socket) -> SocketTile {
        SocketTile(
            id: socket.id, label: socket.label, style: Self.style(socket.status),
            accessibilityLabel: socket.accessibilityLabel, identifier: "room.socket.\(socket.id)")
    }

    /// The `K` map of IosElRaum.dc.html:91-96: fill, text colour and the `now` tile's 2 pt accent border.
    private static func style(_ status: SocketStatus) -> SocketTile.Style {
        switch status {
        case .ok: return SocketTile.Style(background: Tokens.oSoft, foreground: Tokens.oFg)
        case .defect: return SocketTile.Style(background: Tokens.eSoft.core, foreground: Tokens.eFg)
        case .now: return SocketTile.Style(background: Tokens.aSoft, foreground: Tokens.aFg, border: Tokens.accent)
        case .open: return SocketTile.Style(background: Tokens.chip.core, foreground: Tokens.muted)
        }
    }
}
