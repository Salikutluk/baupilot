import Foundation

/// Titles of the pushed screens and the back-button labels derived from them.
struct RouteTitles: Sendable, Equatable {
    /// IosGewerk.dc.html:94 (title of the comparison screen; the fixture carries no text for it)
    static let compareTitle = "Angebote vergleichen"

    let tabLabels: [AppTab: String]
    let tradeNames: [String: String]
    let documentTitles: [String: String]
    let compareTitle: String
    let notificationsTitle: String
    /// Overview row names by trade id ("Fenster & Türen").
    var inspectionTitles: [String: String] = [:]
    /// Step model titles by their route ("F8 · Schlafen").
    var stepTitles: [AppRoute: String] = [:]
    var newDefectTitle: String = ""
    /// Sign-off titles by inspection id ("Abnahme abschließen").
    var signOffTitles: [String: String] = [:]

    /// A missing name falls back to the id, so a title is never empty.
    func title(_ route: AppRoute) -> String {
        switch route {
        case let .trade(id): return tradeNames[id] ?? id
        case .compare: return compareTitle
        case let .document(id): return documentTitles[id] ?? id
        case .notifications: return notificationsTitle
        case let .inspection(tradeId): return inspectionTitles[tradeId] ?? tradeId
        case let .inspectionStep(_, elementId, _): return stepTitles[route] ?? elementId
        case .newDefect: return newDefectTitle
        case let .signOff(inspectionId): return signOffTitles[inspectionId] ?? inspectionId
        }
    }

    /// The first entry of a stack goes back to its tab; every other one to the title of the screen below it.
    func backLabel(path: [AppRoute], index: Int, tab: AppTab) -> String {
        guard index > 0, index < path.count else { return tabLabels[tab] ?? "" }
        return title(path[index - 1])
    }
}
