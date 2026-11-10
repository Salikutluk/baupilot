#if DEBUG
import SwiftUI

/// Rohbau, Erdarbeiten and Elektro protocol parts: ProtocolBanner in its three trailing forms, ProtocolRow ok,
/// defect and wrapped.
struct GalleryProtocolPage: View {
    var body: some View {
        GalleryGroup("ProtocolBanner · document button") {
            ProtocolBanner(
                icon: .checkCircleW24, title: "Abgenommen am 14.04.2026", text: "Unterschrieben · keine Mängel",
                background: Tokens.oSoft, foreground: Tokens.oFg,
                trailing: .document(label: "PDF", action: nil), action: nil)
        }
        GalleryGroup("ProtocolBanner · chevron") {
            ProtocolBanner(
                icon: .alertTriangleW22, title: "1 Mangel offen", text: "Bewehrung Fundament",
                background: Tokens.wSoft, foreground: Tokens.wFg.core, trailing: .chevron, action: nil)
        }
        GalleryGroup("ProtocolBanner · no trailing") {
            ProtocolBanner(
                icon: .checkCircleW24, title: "Abgenommen am 14.04.2026", text: "Unterschrieben · keine Mängel",
                background: Tokens.oSoft, foreground: Tokens.oFg, trailing: .none, action: nil)
        }
        GalleryGroup("ProtocolRow · ok, defect, wrapped; divider on the second and third") {
            Card(style: .row, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ProtocolRow(label: "Baugrube ausgehoben", meta: "12.04.", mark: .ok, showsDivider: false, action: nil)
                    ProtocolRow(label: "Sauberkeitsschicht", meta: "Mangel", mark: .defect, showsDivider: true, action: nil)
                    ProtocolRow(
                        label: "Bewehrung, Schalung und Betonierabschnitt gemäß Statik geprüft und dokumentiert",
                        meta: "14.04.", mark: .ok, showsDivider: true, action: nil)
                }
            }
        }
    }
}

/// Maße & Lot parts: ValueStepper, CodeTile grid (4 columns of 83, gap 6), CheckRow and the text-only AlertBanner.
struct GalleryMeasurePage: View {
    private let codes = ["F1", "F2", "F3", "F4", "F5", "F6", "F7"]

    var body: some View {
        GalleryGroup("ValueStepper · 48 high, 150 wide") {
            HStack {
                Text("Breite")
                    .interText(15, .semibold)
                    .foregroundStyle(Tokens.text.color)
                Spacer(minLength: 0)
                ValueStepper(label: "Breite", value: "1005", onDecrement: nil, onIncrement: nil)
            }
        }
        GalleryGroup("CodeTile · 40 high") {
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 6), count: 4), spacing: 6) {
                ForEach(codes, id: \.self) { code in
                    CodeTile(code, background: Tokens.oSoft, foreground: Tokens.oFg)
                }
            }
        }
        GalleryGroup("CheckRow · min 52") {
            Card(style: .row, padding: EdgeInsets()) {
                CheckRow(
                    title: "Wände im Lot", subtitle: "2-m-Wasserwaage an allen Außenecken", trailing: "8 / 8",
                    fill: Tokens.oFg, ink: Tokens.surface, trailingColor: Tokens.oFg)
            }
        }
        GalleryGroup("AlertBanner · text only, ok and error") {
            AlertBanner(
                icon: nil, title: "Innerhalb der Toleranz", subtitle: nil,
                background: Tokens.oSoft, foreground: Tokens.oFg)
            AlertBanner(
                icon: nil, title: "Außerhalb der Toleranz", subtitle: nil,
                background: Tokens.eSoft.core, foreground: Tokens.eFg)
        }
    }
}

/// Elektro overview parts: the equal-height PhaseTile pair (done with date, active with the card bar) and ListRow with
/// `textGap: 1` and a greedy wrapped title.
struct GalleryElectricalPage: View {
    var body: some View {
        GalleryGroup("PhaseTile · fillsHeight, 2 columns of 171, both 81 high") {
            HStack(spacing: 8) {
                PhaseTile(
                    label: "1 · Rohinstallation", value: "Abgenommen", date: "am 29.01.2027", state: .done,
                    fillsHeight: true)
                PhaseTile(
                    label: "2 · Fertiginstallation", value: "4 von 11 geprüft", state: .active(progress: 0.36),
                    track: Tokens.track.elUebersicht, fillsHeight: true)
            }
            .fixedSize(horizontal: false, vertical: true)
        }
        GalleryGroup("ListRow · textGap 1, greedy title, min 52") {
            Card(style: .row, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ListRow(
                        title: "Zählerschrank", subtitle: "Verteiler und FI-Schutzschalter",
                        leading: .iconTile(.home, size: .s34r9), trailing: .badge(StatusBadge("Offen", tone: .open)),
                        density: .minHeight(52), showsDivider: false, textGap: 1, titleLines: .greedy, action: nil)
                    ListRow(
                        title: "Schutzleiter und Fehlerstrom im Gebäude prüfen",
                        subtitle: "Haustechnik",
                        leading: .iconTile(.home, size: .s34r9), trailing: .badge(StatusBadge("Läuft", tone: .accent)),
                        density: .minHeight(52), showsDivider: true, textGap: 1, titleLines: .greedy, action: nil)
                }
            }
        }
    }
}

/// Elektro step parts, first page: SocketGrid in four states, NumberedList, StatusList, CheckHintRow and the Rauchmelder
/// rows (StatusDotRow, ToggleRow).
struct GalleryElectricalStepsPage: View {
    private func socket(_ number: Int, _ label: String, _ style: SocketTile.Style) -> SocketTile {
        SocketTile(
            id: "S\(number)", label: label, style: style,
            accessibilityLabel: "Steckdose S\(number): \(label)", identifier: "room.socket.S\(number)")
    }

    var body: some View {
        let ok = SocketTile.Style(background: Tokens.oSoft, foreground: Tokens.oFg)
        let err = SocketTile.Style(background: Tokens.eSoft.core, foreground: Tokens.eFg)
        let now = SocketTile.Style(background: Tokens.aSoft, foreground: Tokens.aFg, border: Tokens.accent)
        let open = SocketTile.Style(background: Tokens.chip.core, foreground: Tokens.muted)
        GalleryGroup("SocketGrid · 4 columns of 72.5, tiles 56 high, gap 8") {
            SocketGrid(tiles: [
                socket(1, "korrekt", ok), socket(2, "korrekt", ok), socket(3, "korrekt", ok), socket(4, "Fehler", err),
                socket(5, "korrekt", ok), socket(6, "jetzt", now), socket(7, "offen", open), socket(8, "offen", open),
            ])
            .padding(.horizontal, 16)
        }
        GalleryGroup("NumberedList · 15 x 1.45, gap 4, indent 20") {
            NumberedList(
                items: [
                    "Prüftaste „T“ am FI-Schalter drücken.", "Der Hebel muss sofort nach unten springen.",
                    "Hebel wieder nach oben – Strom ist zurück.",
                ], color: Tokens.muted)
        }
        GalleryGroup("StatusList · rows min 48, chip fill, divider") {
            StatusList(
                rows: [
                    StatusList.Row(title: "FI 1", subtitle: "Bad und Küche", badge: StatusBadge("Ausgelöst", tone: .ok)),
                    StatusList.Row(title: "FI 2", subtitle: "Wohnen und Flur", badge: StatusBadge("Jetzt testen", tone: .open)),
                ],
                background: Tokens.chip.elVerteiler, divider: Tokens.border.elVerteiler)
        }
        GalleryGroup("CheckHintRow · done and open, hint wraps") {
            CheckHintRow(
                title: "Prüftaste drücken", hint: "Taste am Melder gedrückt halten, bis der Signalton kommt.",
                done: true, ok: Tokens.ok.elRauchmelder, okInk: Tokens.okInk.elRauchmelder,
                track: Tokens.track.elRauchmelder)
            CheckHintRow(
                title: "Melder piept laut", hint: "Lautstärke und Dauer ohne Unterbrechung.", done: false,
                ok: Tokens.ok.elRauchmelder, okInk: Tokens.okInk.elRauchmelder, track: Tokens.track.elRauchmelder)
        }
        GalleryGroup("StatusDotRow · min 46, ok / now / open") {
            Card(style: .row, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    StatusDotRow(
                        dot: Tokens.ok.elRauchmelder, title: "Schlafzimmer", status: "Geprüft", statusColor: Tokens.oFg,
                        showsDivider: false, divider: Tokens.border.core)
                    StatusDotRow(
                        dot: Tokens.accent, title: "Flur OG", status: "Jetzt", statusColor: Tokens.aFg,
                        showsDivider: true, divider: Tokens.border.core)
                    StatusDotRow(
                        dot: Tokens.track.elRauchmelder, title: "Kinderzimmer", status: "Offen",
                        statusColor: Tokens.muted, showsDivider: true, divider: Tokens.border.core)
                }
            }
        }
        GalleryGroup("ToggleRow · min 52, on") {
            Card(style: .row, padding: EdgeInsets()) {
                ToggleRow(
                    title: "Jährlich an Test erinnern", subtitle: "Batterien laut Hersteller bis 2037", isOn: true,
                    onColor: Tokens.ok.elRauchmelder, accessibilityLabel: "Jährlich an Test erinnern", action: nil)
            }
        }
    }
}

/// Elektro step parts, second page: CurrentStepCard with the `.note` header, the greedy StepRow, DetailTitle variants.
struct GalleryElectricalSteps2Page: View {
    var body: some View {
        GalleryGroup("CurrentStepCard · .note header, no number circle") {
            CurrentStepCard(header: .note(title: "Flur OG", note: "Jetzt prüfen", noteColor: Tokens.aFg), spacing: 12) {
                CheckHintRow(
                    title: "Prüftaste drücken", hint: "Taste am Melder gedrückt halten.", done: false,
                    ok: Tokens.ok.elRauchmelder, okInk: Tokens.okInk.elRauchmelder,
                    track: Tokens.track.elRauchmelder)
            }
        }
        GalleryGroup("CurrentStepCard · numbered, unchanged") {
            CurrentStepCard(number: 2, title: "Jede Steckdose testen", spacing: 14) {
                CurrentStepCard<EmptyView>.body("Steckdosentester einstecken.")
            }
        }
        GalleryGroup("StepRow · open rows, greedy title, gap 2") {
            VStack(spacing: 2) {
                StepRow(
                    number: 3, title: "Jeder Schalter schaltet das richtige Licht", state: .open(),
                    track: Tokens.track.elRaum, titleLines: .greedy)
                StepRow(
                    number: 4, title: "Herd, Spülmaschine & Dunstabzug angeschlossen", state: .open(tag: "Pflicht"),
                    track: Tokens.track.elVerteiler, titleLines: .greedy)
            }
        }
        GalleryGroup("DetailTitle · 2-line subtitle (ElRaum), no subtitle (ElVerteiler), x1.4 and gap 6 (Rauchmelder)") {
            DetailTitle(
                "Küche", subtitle: "EG · laut Elektroplan: 8 Steckdosen, 3 Auslässe, 3 Geräteanschlüsse",
                style: .size28, subtitleLines: .greedy)
            DetailTitle("Zählerschrank", subtitle: nil, style: .size28)
            DetailTitle(
                "Rauchwarnmelder",
                subtitle: "Pflicht in Schlaf- und Kinderzimmern und in Fluren, die als Fluchtweg dienen.",
                style: .size28, subtitleLines: .greedy, subtitleLineHeight: 1.4, gap: 6)
        }
    }
}
#endif
