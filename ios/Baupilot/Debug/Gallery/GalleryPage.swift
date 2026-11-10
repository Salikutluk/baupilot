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
    case structure, containers, statusInput, buttons, bars, steps, tiles, icons, forms, overview, schedule, settings, trade, documents, compare, notifications, inspections, inspectionElements, inspectionStep, defectForm, signOff, `protocol`, measure, electrical, electricalSteps, electricalSteps2

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
#endif
