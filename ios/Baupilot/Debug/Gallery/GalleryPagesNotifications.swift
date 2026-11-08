#if DEBUG
import SwiftUI

/// Mitteilungen parts: NotificationRow unread (dot, 700) and read (clear dot, 600), with and without divider.
struct GalleryNotificationsPage: View {
    var body: some View {
        GalleryGroup("NotificationRow · unread / read, divider on the second") {
            Card(style: .standard, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    NotificationRow(
                        icon: .alertCircleW22, iconBackground: Tokens.eSoft.mitteilungen, iconForeground: Tokens.eFg,
                        title: "Frist läuft ab", when: "09:41",
                        text: "Die Rechnung von Fenster & Türen ist am 22.12. fällig.",
                        unread: true, showsDivider: false, action: nil)
                    NotificationRow(
                        icon: .calendarW22, iconBackground: Tokens.iSoft, iconForeground: Tokens.iFg,
                        title: "Termin bestätigt", when: "Gestern",
                        text: "Der Estrichleger kommt am Montag um 7:30 Uhr.",
                        unread: false, showsDivider: true, action: nil)
                }
            }
        }
    }
}
#endif
