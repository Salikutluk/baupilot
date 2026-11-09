import SwiftUI

enum DefectFormAction {
    case cancel
    case save(tradeId: String)
}

/// Mangel erfassen (IosAbnMangel.dc.html:18-83). Every control keeps local state (D9) and writes nothing (I8).
struct DefectFormScreen: View {
    let model: DefectFormModel
    let onAction: (DefectFormAction) -> Void

    @State private var severity: Int
    @State private var deadline: Int
    @State private var deadlineText: String
    @State private var notify: Bool
    @State private var description: String

    init(model: DefectFormModel, onAction: @escaping (DefectFormAction) -> Void) {
        self.model = model
        self.onAction = onAction
        _severity = State(initialValue: model.initialSeverity)
        _deadline = State(initialValue: model.initialDeadline)
        _deadlineText = State(initialValue: model.deadlineText(option: model.initialDeadline) ?? "")
        _notify = State(initialValue: model.notifyInitial)
        _description = State(initialValue: model.description)
    }

    var body: some View {
        ScreenScaffold(kind: .modal, sectionSpacing: .medium) {
            BackBar.modal(
                cancel: model.cancelLabel, title: model.title, confirm: model.saveLabel,
                onCancel: { onAction(.cancel) }, onConfirm: { onAction(.save(tradeId: model.tradeId)) },
                cancelIdentifier: "defect.cancel", confirmIdentifier: "defect.save")
            AlertBanner(
                icon: .alertTriangleW22, title: model.context, subtitle: model.finding,
                background: Tokens.eSoft.core, foreground: Tokens.eFg)
            TextAreaField(
                label: model.descriptionLabel, text: $description, border: Tokens.border.abnMangel,
                accessibilityLabel: model.descriptionLabel)
            photos
            VStack(alignment: .leading, spacing: 6) {
                Text(model.severityLabel)
                    .interText(13, .semibold)
                    .foregroundStyle(Tokens.muted.color)
                SegmentedControl(
                    segments: model.severities, selection: severity, height: .medium, background: .seg,
                    accessibilityLabel: model.severityLabel,
                    identifiers: model.severities.indices.map { "defect.severity.\($0)" }
                ) { severity = $0 }
            }
            VStack(alignment: .leading, spacing: 8) {
                DisplayField(
                    label: model.deadlineLabel, value: deadlineText, border: Tokens.border.abnMangel,
                    valueIdentifier: "defect.deadline.value")
                OptionChips(
                    items: model.deadlineOptions.map(\.label), selection: deadline,
                    border: Tokens.border.abnMangel,
                    identifiers: model.deadlineOptions.indices.map { "defect.deadline.\($0)" },
                    enabled: model.deadlineOptions.map { $0.offsetDays != nil }
                ) { selectDeadline($0) }
            }
            Card(style: .grouped, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    FormRow(
                        title: model.responsibleLabel, trailing: .value(model.responsible), showsDivider: false,
                        divider: Tokens.border.abnMangel)
                    FormRow(
                        title: model.notifyLabel,
                        trailing: .toggle(
                            isOn: notify, onColor: Tokens.ok.abnMangel, accessibilityLabel: model.notifyLabel
                        ) { notify.toggle() },
                        showsDivider: true, divider: Tokens.border.abnMangel, toggleIdentifier: "defect.notify")
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.defect.new")
    }

    /// IosAbnMangel:38-48: a 4-column grid of 8 pt gaps holding the photo tiles and the add tile.
    private var photos: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(model.photosLabel)
                .interText(13, .semibold)
                .foregroundStyle(Tokens.muted.color)
            HStack(spacing: 8) {
                ForEach(model.photoLabels, id: \.self) { label in
                    PhotoTile(
                        kind: .icon(.image), height: .h76, chip: Tokens.chip.abnMangel,
                        accessibilityLabel: label)
                }
                PhotoTile(kind: .add(label: nil), height: .h76, accessibilityLabel: model.addPhotoLabel)
                Color.clear
                    .frame(maxWidth: .infinity, maxHeight: 0)
            }
        }
    }

    /// A chip without an offset ("Eigenes Datum") changes nothing: the date picker is out of scope.
    private func selectDeadline(_ index: Int) {
        guard let text = model.deadlineText(option: index) else { return }
        deadline = index
        deadlineText = text
    }
}
