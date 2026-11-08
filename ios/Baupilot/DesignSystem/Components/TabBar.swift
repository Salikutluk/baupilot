import SwiftUI

/// Bottom tab bar: 84 tall, border-box including the 1 pt top border, five equal columns.
/// Taps only report the selection; navigation is the caller's job.
public struct TabBar: View {
    public enum Tab: CaseIterable {
        case overview, schedule, inspections, documents, profile

        var title: String {
            switch self {
            case .overview: return "Übersicht"
            case .schedule: return "Zeitplan"
            case .inspections: return "Abnahmen"
            case .documents: return "Dokumente"
            case .profile: return "Profil"
            }
        }

        var icon: BaupilotIcon {
            switch self {
            case .overview: return .home
            case .schedule: return .calendar
            case .inspections: return .clipboardCheck
            case .documents: return .folder
            case .profile: return .user
            }
        }
    }

    private let selected: Tab
    private let border: ColorToken
    private let onSelect: (Tab) -> Void

    public init(selected: Tab, border: ColorToken = Tokens.border.core, onSelect: @escaping (Tab) -> Void) {
        self.selected = selected
        self.border = border
        self.onSelect = onSelect
    }

    public var body: some View {
        VStack(spacing: 0) {
            Rectangle()
                .fill(border.color)
                .frame(height: 1)
            HStack(alignment: .top, spacing: 0) {
                ForEach(Tab.allCases, id: \.self) { tab in
                    item(tab)
                }
            }
            .padding(.top, 8)
            .padding(.horizontal, 4)
            .padding(.bottom, 0)
            Spacer(minLength: 0)
        }
        .frame(height: 84, alignment: .top)
        .background(Tokens.bar.color)
        .accessibilityElement(children: .contain)
        .accessibilityLabel("Hauptnavigation")
    }

    private func item(_ tab: Tab) -> some View {
        let isSelected = tab == selected
        let color = isSelected ? Tokens.accent.color : Tokens.muted.color
        return Button(action: { onSelect(tab) }) {
            VStack(spacing: 4) {
                Icon(tab.icon, size: 24, color: color)
                Text(tab.title)
                    .font(.inter(10, .semibold))
                    .foregroundStyle(color)
            }
            .frame(maxWidth: .infinity, minHeight: 44, alignment: .top)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(tab.title)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}
