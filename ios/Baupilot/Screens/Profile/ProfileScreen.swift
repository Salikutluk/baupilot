import SwiftUI

/// Profil (IosProfil.dc.html): title, profile card and three settings groups. The card and the link
/// rows have no action; toggles flip local, non-persisted state seeded from the fixture (D16).
struct ProfileScreen: View {
    let model: ProfileModel

    /// Owned by the shell, so the switches keep their state while the tab's view is rebuilt.
    @Binding var toggles: [String: Bool]

    /// Switch states seeded from the fixture's `checked` values.
    static func initialToggles(for model: ProfileModel) -> [String: Bool] {
        var initial: [String: Bool] = [:]
        for (groupIndex, group) in model.groups.enumerated() {
            for (rowIndex, row) in group.rows.enumerated() where row.isToggle {
                initial[key(groupIndex, rowIndex)] = row.checked ?? false
            }
        }
        return initial
    }

    private static func key(_ group: Int, _ row: Int) -> String { "\(group).\(row)" }

    private func isOn(_ group: Int, _ row: Int) -> Binding<Bool> {
        let key = Self.key(group, row)
        return Binding(get: { toggles[key] ?? false }, set: { toggles[key] = $0 })
    }

    private var profileCard: some View {
        Card(style: .standard, padding: EdgeInsets(top: 12, leading: 14, bottom: 12, trailing: 14)) {
            HStack(spacing: 12) {
                Avatar(initials: model.initials, size: .s48)
                VStack(alignment: .leading, spacing: 2) {
                    Text(model.name)
                        .interText(17, .semibold)
                        .foregroundStyle(Tokens.text.color)
                    Text(model.contact)
                        .interText(14, .regular)
                        .foregroundStyle(Tokens.muted.color)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                Icon(.chevronRightW22, size: 16, color: Tokens.muted.color)
            }
        }
    }

    private func trailing(_ row: ProfileModel.Row, group: Int, index: Int) -> SettingsRow.Trailing {
        if row.isToggle { return .toggle(isOn: isOn(group, index)) }
        if row.isLink { return .link(value: row.value) }
        return .none
    }

    private func section(_ group: ProfileModel.Group, index groupIndex: Int) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(group.title, style: .caps)
            Card(style: .standard, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ForEach(Array(group.rows.enumerated()), id: \.offset) { index, row in
                        SettingsRow(
                            label: row.label,
                            trailing: trailing(row, group: groupIndex, index: index),
                            tone: row.label == ScreenCopy.Profil.signOut ? .danger : .regular,
                            showsDivider: index > 0)
                    }
                }
            }
        }
    }

    var body: some View {
        ScreenScaffold(kind: .tabRoot, sectionSpacing: .loose) {
            LargeTitleHeader(title: ScreenCopy.Profil.title)
            profileCard
            ForEach(Array(model.groups.enumerated()), id: \.offset) { index, group in
                section(group, index: index)
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.profile")
    }
}
