#if DEBUG
import SwiftUI

/// Mangel-Formular parts: AlertBanner, TextAreaField, DisplayField, OptionChips and FormRow.
struct GalleryDefectFormPage: View {
    @State private var text = "Flügel fällt ab ca. 45° Öffnung selbstständig wieder zu."
    @State private var chip = 1
    @State private var notify = true

    var body: some View {
        GalleryGroup("AlertBanner · eSoft / eFg") {
            AlertBanner(
                icon: .alertTriangleW22, title: "F2 · Wohnen · Schritt 1",
                subtitle: "Drehstellung: Flügel bleibt nicht stehen",
                background: Tokens.eSoft.core, foreground: Tokens.eFg)
        }
        GalleryGroup("TextAreaField · 3 rows, 93.2 tall") {
            TextAreaField(
                label: "Beschreibung", text: $text, border: Tokens.border.abnMangel,
                accessibilityLabel: "Beschreibung")
        }
        GalleryGroup("DisplayField · 48") {
            DisplayField(label: "Behebung bis", value: "Fr, 15.01.2027", border: Tokens.border.abnMangel)
        }
        GalleryGroup("OptionChips · 34, selected / plain") {
            OptionChips(
                items: ["+ 14 Tage", "+ 4 Wochen", "Eigenes Datum"], selection: chip,
                border: Tokens.border.abnMangel
            ) { chip = $0 }
        }
        GalleryGroup("FormRow · value / toggle with divider") {
            Card(style: .grouped, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    FormRow(
                        title: "Zuständig", trailing: .value("Fensterbau Weber GmbH"), showsDivider: false,
                        divider: Tokens.border.abnMangel)
                    FormRow(
                        title: "Handwerker per E-Mail informieren",
                        trailing: .toggle(
                            isOn: notify, onColor: Tokens.ok.abnMangel,
                            accessibilityLabel: "Handwerker per E-Mail informieren"
                        ) { notify.toggle() },
                        showsDivider: true, divider: Tokens.border.abnMangel)
                }
            }
        }
    }
}
#endif
