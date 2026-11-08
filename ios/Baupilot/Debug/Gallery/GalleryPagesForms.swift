#if DEBUG
import SwiftUI

/// FormField, IconTile, FeatureRow, tall PrimaryButton and InvertedButton (the Login parts).
struct GalleryFormsPage: View {
    @State private var email = ""
    @State private var password = ""

    var body: some View {
        GalleryGroup("FormField · email, border login") {
            FormField(
                label: "E-Mail", text: $email, placeholder: "name@beispiel.de", kind: .email,
                border: Tokens.border.login)
        }
        GalleryGroup("FormField · password with link, core border") {
            FormField(
                label: "Passwort", text: $password, placeholder: "••••••••", kind: .password,
                trailingLink: FormField.Link(title: "Vergessen?", action: nil))
        }
        GalleryGroup("IconTile · brand40 / feature32") {
            HStack(spacing: 12) {
                IconTile(
                    icon: .house, size: .brand40, background: Tokens.accent,
                    foreground: Tokens.onAccent)
                IconTile(
                    icon: .calendar, size: .feature32, background: Tokens.accentSoft,
                    foreground: Tokens.accent)
                Spacer(minLength: 0)
            }
        }
        GalleryGroup("FeatureRow") {
            VStack(alignment: .leading, spacing: 10) {
                FeatureRow(icon: .calendar, text: "Zeitplan: wann welcher Handwerker kommt")
                FeatureRow(icon: .documentCheck, text: "Angebote je Gewerk sammeln & vergleichen")
                FeatureRow(icon: .chartLine, text: "Begleitung von Planung bis Abnahme")
            }
        }
        GalleryGroup("PrimaryButton · tall 52 / InvertedButton · tall with icon") {
            VStack(spacing: 10) {
                PrimaryButton("Anmelden", height: .tall, action: nil)
                InvertedButton("Mit Apple anmelden", icon: .apple, action: nil)
            }
        }
    }
}
#endif
