#if DEBUG
import SwiftUI

/// Avatar and SettingsRow (the Profil parts): link with and without value, toggles on and off, danger tone.
struct GallerySettingsPage: View {
    @State private var notifyDeadlines = true
    @State private var notifyWeekly = false

    var body: some View {
        GalleryGroup("Avatar · s48") {
            Avatar(initials: "MM", size: .s48)
        }
        GalleryGroup("SettingsRow · link with value, link without value, toggles") {
            Card(style: .standard, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    SettingsRow(label: "Projekt", trailing: .link(value: "Musterweg 12"), showsDivider: false)
                    SettingsRow(label: "Daten exportieren", trailing: .link(value: nil), showsDivider: true)
                    SettingsRow(
                        label: "Fristen & Termine", trailing: .toggle(isOn: $notifyDeadlines), showsDivider: true)
                    SettingsRow(
                        label: "Wochenrückblick", trailing: .toggle(isOn: $notifyWeekly), showsDivider: true)
                }
            }
        }
        GalleryGroup("SettingsRow · danger") {
            Card(style: .standard, padding: EdgeInsets()) {
                SettingsRow(label: "Abmelden", trailing: .link(value: nil), tone: .danger, showsDivider: false)
            }
        }
    }
}
#endif
