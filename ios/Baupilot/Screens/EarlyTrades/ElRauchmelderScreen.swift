import SwiftUI

/// Elektro Rauchwarnmelder (Nr. 23, IosElRauchmelder.dc.html:17-71): the current card (check hints, two outcome
/// buttons), the room list and the reminder switch. "Kein Piepton" opens the defect form; the ok button, the room
/// rows and the check rows have no action (I6). The switch is local state, saved nowhere (D12). Per-screen tokens:
/// the `elRauchmelder` ok, okInk and track; core tokens for the rest.
struct ElRauchmelderScreen: View {
    let model: SmokeCheckModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (CheckStepAction) -> Void
    @State private var reminderOn: Bool

    init(
        model: SmokeCheckModel, backLabel: String, onBack: @escaping () -> Void,
        onAction: @escaping (CheckStepAction) -> Void
    ) {
        self.model = model
        self.backLabel = backLabel
        self.onBack = onBack
        self.onAction = onAction
        _reminderOn = State(initialValue: model.reminderInitiallyOn)
    }

    private var identifier: String {
        guard case let .inspectionCheck(tradeId, check) = model.route else { return "screen.inspection.smoke" }
        return "screen.inspection.\(tradeId).\(check)"
    }

    var body: some View {
        ScreenScaffold(kind: .protocol, sectionSpacing: .regular) {
            BackBar(label: backLabel, action: onBack, trailing: .counter(model.counter))
            DetailTitle(
                model.title, subtitle: model.subtitle, style: .size28, subtitleLines: .greedy,
                subtitleLineHeight: 1.4, gap: 6)
            currentCard
            roomsCard
            reminderCard
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier)
    }

    /// IosElRauchmelder:28-42: the accent-bordered card (gap 12): the check hints, then the two buttons (grid, gap 10).
    private var currentCard: some View {
        CurrentStepCard(
            header: .note(title: model.room, note: model.nowLabel, noteColor: Tokens.aFg), spacing: 12
        ) {
            ForEach(Array(model.checks.enumerated()), id: \.offset) { index, check in
                CheckHintRow(
                    title: check.label, hint: check.hint, done: check.done, ok: Tokens.ok.elRauchmelder,
                    okInk: Tokens.okInk.elRauchmelder, track: Tokens.track.elRauchmelder
                )
                .accessibilityIdentifier("smoke.check.\(index)")
            }
            HStack(alignment: .top, spacing: 10) {
                DefectButton(
                    model.outcomes[0], size: .small,
                    action: { onAction(.open(model.defectTarget)) }
                )
                .accessibilityIdentifier("step.defect")
                OkButton(
                    model.outcomes[1], size: .small, ok: Tokens.ok.elRauchmelder,
                    okInk: Tokens.okInk.elRauchmelder, action: nil
                )
                .accessibilityIdentifier("step.ok")
            }
        }
    }

    /// IosElRauchmelder:45-51: one dot row per room, 1 pt divider on top of every row but the first.
    private var roomsCard: some View {
        Card(style: .row, padding: EdgeInsets()) {
            VStack(spacing: 0) {
                ForEach(Array(model.rooms.enumerated()), id: \.offset) { index, room in
                    let look = Self.look(room.status)
                    StatusDotRow(
                        dot: look.dot, title: room.name, status: room.statusLabel, statusColor: look.text,
                        showsDivider: index > 0, divider: Tokens.border.core
                    )
                    .accessibilityIdentifier("smoke.room.\(index)")
                }
            }
        }
    }

    /// IosElRauchmelder:84: the `R` map. ok: `ok` dot and `okText`; now: `accent` and `aFg`; open: `track` and `muted`.
    private static func look(_ status: SmokeRoomStatus) -> (dot: ColorToken, text: ColorToken) {
        switch status {
        case .ok: return (Tokens.ok.elRauchmelder, Tokens.okText)
        case .now: return (Tokens.accent, Tokens.aFg)
        case .open: return (Tokens.track.elRauchmelder, Tokens.muted)
        }
    }

    /// IosElRauchmelder:53-62: the reminder row; the state is local and starts on.
    private var reminderCard: some View {
        Card(style: .row, padding: EdgeInsets()) {
            ToggleRow(
                title: model.reminder, subtitle: model.battery, isOn: reminderOn,
                onColor: Tokens.ok.elRauchmelder, accessibilityLabel: model.reminder,
                action: { reminderOn.toggle() }
            )
            .accessibilityIdentifier("smoke.reminder")
        }
    }
}
