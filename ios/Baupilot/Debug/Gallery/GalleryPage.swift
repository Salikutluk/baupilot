#if DEBUG
import SwiftUI

extension String {
    /// First character upper-cased, the rest untouched ("statusInput" -> "StatusInput").
    var capitalizedFirst: String {
        prefix(1).uppercased() + dropFirst()
    }
}

/// Developer label above a gallery sample (11/600 muted). Not product copy.
struct GalleryCaption: View {
    let text: String

    init(_ text: String) {
        self.text = text
    }

    var body: some View {
        Text(text)
            .font(.inter(11, .semibold))
            .foregroundStyle(Tokens.muted.color)
    }
}

/// A caption over its sample.
struct GalleryGroup<Content: View>: View {
    let caption: String
    let content: Content

    init(_ caption: String, @ViewBuilder content: () -> Content) {
        self.caption = caption
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            GalleryCaption(caption)
            content
        }
    }
}

/// One full-screen page of the component gallery. Each page is one golden pair.
enum GalleryPage: String, CaseIterable {
    case structure, containers, statusInput, buttons, bars, steps, tiles, icons, forms, overview, schedule, settings, trade, documents, compare, notifications, inspections, inspectionElements, inspectionStep, defectForm, signOff, `protocol`, measure, electrical, electricalSteps, electricalSteps2, plumbing, plumbingSteps, screed

    /// ASCII letters only, so the name is a valid file name on every platform.
    var goldenName: String { "Gallery" + rawValue.capitalizedFirst }

    /// The page body including its padding: 20 on the sides, 24 top and bottom.
    @ViewBuilder
    var content: some View {
        VStack(alignment: .leading, spacing: 12) {
            switch self {
            case .structure: GalleryStructurePage()
            case .containers: GalleryContainersPage()
            case .statusInput: GalleryStatusInputPage()
            case .buttons: GalleryButtonsPage()
            case .bars: GalleryBarsPage()
            case .steps: GalleryStepsPage()
            case .tiles: GalleryTilesPage()
            case .icons: GalleryIconsPage()
            case .forms: GalleryFormsPage()
            case .overview: GalleryOverviewPage()
            case .schedule: GallerySchedulePage()
            case .settings: GallerySettingsPage()
            case .trade: GalleryTradePage()
            case .documents: GalleryDocumentsPage()
            case .compare: GalleryComparePage()
            case .notifications: GalleryNotificationsPage()
            case .inspections: GalleryInspectionsPage()
            case .inspectionElements: GalleryInspectionElementsPage()
            case .inspectionStep: GalleryInspectionStepPage()
            case .defectForm: GalleryDefectFormPage()
            case .signOff: GallerySignOffPage()
            case .`protocol`: GalleryProtocolPage()
            case .measure: GalleryMeasurePage()
            case .electrical: GalleryElectricalPage()
            case .electricalSteps: GalleryElectricalStepsPage()
            case .electricalSteps2: GalleryElectricalSteps2Page()
            case .plumbing: GalleryPlumbingPage()
            case .plumbingSteps: GalleryPlumbingStepsPage()
            case .screed: GalleryScreedPage()
            }
        }
        .frame(maxWidth: .infinity, alignment: .topLeading)
        .padding(.horizontal, 20)
        .padding(.top, 24)
        .padding(.bottom, 24)
    }

    /// The page in the golden frame; the background fills the whole 390 x 844 frame.
    var framed: some View {
        content
            .frame(width: 390, height: 844, alignment: .topLeading)
            .background(Tokens.bg.color)
    }
}

/// Sanitär parts: the ListRow with a greedy wrapped subtitle (SanUebersicht row 1) and the slope check's socket tiles,
/// full-width steppers and result (SanGefaelle).
struct GalleryPlumbingPage: View {
    var body: some View {
        GalleryGroup("ListRow · greedy subtitle wraps like Chrome, textGap 1, min 52") {
            Card(style: .row, padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ListRow(
                        title: "Gefälle Abwasser", subtitle: "4 Abschnitte · 2 geprüft, 1 Mangel",
                        leading: .iconTile(.pipe, size: .s34r9), trailing: .badge(StatusBadge("1 Mangel", tone: .error())),
                        density: .minHeight(52), showsDivider: false, subtitleLines: .greedy, textGap: 1,
                        titleLines: .greedy, action: nil)
                    ListRow(
                        title: "Dämmung Warmwasser", subtitle: "Sichtprüfung · 2 Fotos",
                        leading: .iconTile(.pipe, size: .s34r9), trailing: .badge(StatusBadge("In Ordnung", tone: .ok)),
                        density: .minHeight(52), showsDivider: true, subtitleLines: .greedy, textGap: 1,
                        titleLines: .greedy, action: nil)
                }
            }
        }
        GalleryGroup("SocketGrid · 4 columns, tile 52, gap 8 (ok, defect, now, open)") {
            SocketGrid(
                tiles: [
                    slopeTile("A1", "1,4 %", SocketTile.Style(background: Tokens.oSoft, foreground: Tokens.oFg)),
                    slopeTile("A2", "0,4 %", SocketTile.Style(background: Tokens.eSoft.core, foreground: Tokens.eFg)),
                    slopeTile(
                        "A3", "jetzt",
                        SocketTile.Style(background: Tokens.aSoft, foreground: Tokens.aFg, border: Tokens.accent)),
                    slopeTile("A4", "offen", SocketTile.Style(background: Tokens.chip.core, foreground: Tokens.muted)),
                ], height: 52)
        }
        GalleryGroup("ValueStepper · fillsWidth, two columns of 152, gap 10") {
            HStack(spacing: 10) {
                ValueStepper(
                    label: "Länge", value: "2,0 m", fillsWidth: true, onDecrement: nil, onIncrement: nil)
                ValueStepper(
                    label: "Höhenunterschied", value: "2,5 cm", fillsWidth: true, onDecrement: nil,
                    onIncrement: nil)
            }
        }
        GalleryGroup("SlopeResult · sufficient") {
            SlopeResult(
                percent: "1,25 %", verdict: "Ausreichend · Soll ≥ 1,0 %", schemaY2: 15.1, levelLabel: "waagerecht",
                deltaLabel: "Δh", background: Tokens.oSoft, foreground: Tokens.oFg)
        }
        GalleryGroup("SlopeResult · too flat") {
            SlopeResult(
                percent: "0,25 %", verdict: "Zu flach · Soll ≥ 1,0 %", schemaY2: 14.0, levelLabel: "waagerecht",
                deltaLabel: "Δh", background: Tokens.eSoft.core, foreground: Tokens.eFg)
        }
    }

    private func slopeTile(_ id: String, _ label: String, _ style: SocketTile.Style) -> SocketTile {
        SocketTile(
            id: id, label: label, style: style, accessibilityLabel: "Abschnitt \(id): \(label)",
            identifier: "gallery.slope.\(id)", height: 52)
    }
}

/// Sanitär step parts: the Druckprobe timer ring and the reading boxes (SanDruck), the wrapping object chips and the
/// object check rows (SanBad).
struct GalleryPlumbingStepsPage: View {
    @State private var end = ""

    var body: some View {
        GalleryGroup("TimerRing · 72, hole 56, 58 %, accent over the track") {
            TimerRing(
                text: "12:40", fraction: 0.58, accessibilityLabel: "Prüfzeit zu 58 Prozent abgelaufen",
                fill: Tokens.accent, track: Tokens.track.sanDruck, hole: Tokens.surface, holeDiameter: 56,
                textWeight: .bold)
        }
        GalleryGroup("ReadingField · value (chip, inset 14) and input (accent border, placeholder)") {
            HStack(alignment: .top, spacing: 10) {
                ReadingField(label: "Start (10:02)", content: .value("10,0 bar"), identifier: nil)
                ReadingField(label: "Ende", content: .input(text: $end, placeholder: "– bar"), identifier: nil)
            }
            .frame(width: 314)
        }
        GalleryGroup("SelectionChip borderBox in FlowLayout · done, active, open (text + 26), gap 8") {
            FlowLayout(spacing: 8, lineSpacing: 8) {
                SelectionChip("WC", state: .done, openBorder: Tokens.outline, showsTick: true, borderBox: true)
                SelectionChip("Waschtisch", state: .active, openBorder: Tokens.outline, showsTick: true, borderBox: true)
                SelectionChip("Dusche", state: .open, openBorder: Tokens.outline, showsTick: true, borderBox: true)
                SelectionChip("Heizkörper", state: .open, openBorder: Tokens.outline, showsTick: true, borderBox: true)
                SelectionChip("Lüfter", state: .open, openBorder: Tokens.outline, showsTick: true, borderBox: true)
            }
            .frame(width: 350, alignment: .leading)
        }
        GalleryGroup("ObjectCheckRow · ok (filled ✓) and open, divider from row 2, min 56") {
            Card(style: .standard, padding: EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16), border: .current) {
                VStack(alignment: .leading, spacing: 4) {
                    ObjectCheckRow(
                        title: "Warm links, kalt rechts", hint: "Hebel ganz nach links: Wasser wird warm.", ok: true,
                        showsDivider: false, okFill: Tokens.ok.sanBad, okInk: Tokens.okInk.sanBad,
                        outline: Tokens.outline, divider: Tokens.border.core,
                        okAccessibilityLabel: "Warm links, kalt rechts: in Ordnung",
                        defectAccessibilityLabel: "Warm links, kalt rechts: Mangel", identifiers: nil)
                    ObjectCheckRow(
                        title: "Siphon & Anschlüsse trocken",
                        hint: "Mit Papiertuch unten abwischen – muss trocken bleiben.", ok: false, showsDivider: true,
                        okFill: Tokens.ok.sanBad, okInk: Tokens.okInk.sanBad, outline: Tokens.outline,
                        divider: Tokens.border.core, okAccessibilityLabel: "Siphon & Anschlüsse trocken: in Ordnung",
                        defectAccessibilityLabel: "Siphon & Anschlüsse trocken: Mangel", identifiers: nil)
                }
            }
        }
    }
}

/// Estrich parts: the straightedge sketch within and over the limit, the measurement chips in a wrapping row and the
/// greedy text link (EsEbenheit).
struct GalleryScreedPage: View {
    var body: some View {
        GalleryGroup("FlatnessSketch · 64, gap line 8 (within, oFg)") {
            FlatnessSketch(
                lineHeight: 8, line: Tokens.oFg, lath: Tokens.accent, floor: Tokens.track.esEbenheit,
                background: Tokens.surface)
        }
        GalleryGroup("FlatnessSketch · gap line 26 (over, eFg)") {
            FlatnessSketch(
                lineHeight: 26, line: Tokens.eFg, lath: Tokens.accent, floor: Tokens.track.esEbenheit,
                background: Tokens.surface)
        }
        GalleryGroup("MeasureChip · 36, within and over, gap 8") {
            FlowLayout(spacing: 8, lineSpacing: 8) {
                MeasureChip("M1 · 2 mm", background: Tokens.oSoft, foreground: Tokens.oFg)
                MeasureChip("M2 · 3 mm", background: Tokens.oSoft, foreground: Tokens.oFg)
                MeasureChip("M3 · 6 mm", background: Tokens.eSoft.core, foreground: Tokens.eFg)
            }
            .frame(width: 350, alignment: .leading)
        }
        GalleryGroup("TextLink · min 44, greedy wrap, leading") {
            TextLink("1 Messpunkt über Grenzwert → Mangel erfassen", color: Tokens.err.core, action: nil)
        }
    }
}
#endif
