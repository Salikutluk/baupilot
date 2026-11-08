#if DEBUG
import SwiftUI

/// StatusBadge, Chip, SegmentedControl, IosToggle, SearchField, FilterChips, SelectionChip.
struct GalleryStatusInputPage: View {
    var body: some View {
        GalleryGroup("StatusBadge · compact, every tone") {
            VStack(alignment: .leading, spacing: 6) {
                FlowRow {
                    StatusBadge("In Ordnung", tone: .ok)
                    StatusBadge("Läuft", tone: .accent)
                    StatusBadge("Vergleichen", tone: .info)
                    StatusBadge("1 Mangel", tone: .error())
                }
                FlowRow {
                    StatusBadge("Mit Vorbehalt", tone: .warn())
                    StatusBadge("Geplant", tone: .neutral())
                    StatusBadge("Offen", tone: .open)
                }
            }
        }
        GalleryGroup("StatusBadge · roomy / bold due / mini") {
            FlowRow {
                StatusBadge("Beauftragt", tone: .ok, size: .roomy)
                StatusBadge("3 Tage überfällig", tone: .error(), weight: .bold)
                StatusBadge("Günstigstes", tone: .ok, size: .mini, weight: .bold)
            }
        }
        GalleryGroup("Chip · Gewerk ×2") {
            FlowRow {
                Chip("Angebote einholen", tone: .accent)
                Chip("Beauftragt", tone: .ok)
            }
        }
        GalleryGroup("SegmentedControl · 34 chip / 44 seg / 36 seg") {
            VStack(spacing: 8) {
                SegmentedControl(
                    segments: ["Gewerke", "Offene Mängel (3)"], selection: 0, height: .compact,
                    accessibilityLabel: "Ansicht", onSelect: { _ in })
                SegmentedControl(
                    segments: ["Gewerke", "Offene Mängel (3)"], selection: 1, height: .regular,
                    accessibilityLabel: "Ansicht", onSelect: { _ in })
                SegmentedControl(
                    segments: ["Gering", "Mittel", "Erheblich"], selection: 1, height: .medium,
                    background: .seg, accessibilityLabel: "Schwere", onSelect: { _ in })
            }
        }
        GalleryGroup("IosToggle · on / on ok / off") {
            HStack(spacing: 16) {
                IosToggle(isOn: true, accessibilityLabel: "Fristen & Termine", action: {})
                IosToggle(isOn: true, onColor: Tokens.ok.core, accessibilityLabel: "Neue Dokumente", action: {})
                IosToggle(isOn: false, accessibilityLabel: "Wochenrückblick", action: {})
            }
        }
        GalleryGroup("SearchField · placeholder") {
            SearchField(
                text: .constant(""), placeholder: "Firma, Gewerk oder Betrag suchen",
                accessibilityLabel: "Dokumente durchsuchen")
        }
        GalleryGroup("FilterChips · Dokumente") {
            FilterChips(
                items: ["Alle 26", "Angebote 7", "Verträge 3", "Rechnungen 4", "Pläne 12"],
                selection: 0, accessibilityLabel: "Dokumenttyp", onSelect: { _ in })
        }
        GalleryGroup("SelectionChip · done / active / open / tick") {
            FlowRow {
                SelectionChip("Fenster", state: .done)
                SelectionChip("Bad OG", state: .active)
                SelectionChip("Küche", state: .open)
                SelectionChip("Flur", state: .done, showsTick: true)
            }
        }
    }
}

/// Left-aligned row of fixed-size samples.
struct FlowRow<Content: View>: View {
    let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        HStack(spacing: 8) {
            content
            Spacer(minLength: 0)
        }
    }
}
#endif
