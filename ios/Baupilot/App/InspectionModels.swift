import Foundation

/// Sendable stand-in for `CountTile.Tone` (the tone of a stat tile of the sign-off screen).
enum CountTone: Sendable, Equatable { case surface, ok, error }

enum InspectionStatus: String, Sendable, Equatable { case accepted, reserved, today, planned }

enum ElementStatus: String, Sendable, Equatable { case ok, defect, now, open }

enum DefectUrgency: String, Sendable, Equatable { case late, soon, later }

/// A positive number written as a JSON string: `^[1-9][0-9]*$`.
nonisolated func positiveNumber(_ text: String) -> Int? {
    guard let first = text.unicodeScalars.first, ("1"..."9").contains(first),
        text.unicodeScalars.allSatisfy({ ("0"..."9").contains($0) })
    else { return nil }
    return Int(text)
}

struct InspectionTradeRow: Sendable, Equatable {
    let tradeId: String
    let name: String
    let subtitle: String
    let status: InspectionStatus
    let statusLabel: String
    let target: AppRoute?
}

/// Abnahmen tab root, view "Gewerke".
struct InspectionsModel: Sendable, Equatable {
    let title: String
    let templatesLabel: String
    let views: [String]
    let viewsLabel: String
    let rows: [InspectionTradeRow]
}

struct DefectCard: Sendable, Equatable {
    let id: String
    let trade: String
    let title: String
    let dueLabel: String
    let meta: String
    let urgency: DefectUrgency
    let showsActions: Bool
}

/// Abnahmen tab root, view "Offene Mängel".
struct DefectsBoardModel: Sendable, Equatable {
    let title: String
    let templatesLabel: String
    let views: [String]
    let viewsLabel: String
    let cards: [DefectCard]
    let remindLabel: String
    let resolveLabel: String
    let resolvedLabel: String
    let resolvedCount: String
}

struct ElementsModel: Sendable, Equatable {
    struct LegendEntry: Sendable, Equatable {
        let status: ElementStatus
        let label: String
    }

    struct ElementRow: Sendable, Equatable {
        let id: String
        let room: String
        let type: String
        let label: String
        let status: ElementStatus
        let target: AppRoute?
    }

    struct FloorSection: Sendable, Equatable {
        let title: String
        let moreLabel: String
        let items: [ElementRow]
    }

    let tradeId: String
    let title: String
    let subtitle: String
    let counter: String
    let guideLinkLabel: String
    let progress: [ElementStatus]
    let legend: [LegendEntry]
    let floors: [FloorSection]
    let nextLabel: String
    let nextTarget: AppRoute
}

struct StepModel: Sendable, Equatable {
    struct Done: Sendable, Equatable {
        let label: String
        let meta: String
    }

    struct Current: Sendable, Equatable {
        let n: Int
        let title: String
        let text: String
        let photoCaption: String
        let addPhotoLabel: String
    }

    struct Next: Sendable, Equatable {
        let n: Int
        let label: String
    }

    let route: AppRoute
    let counter: String
    let title: String
    let subtitle: String
    let done: [Done]
    let current: Current
    let guideLabel: String
    let defectLabel: String
    let okLabel: String
    let next: [Next]
    let defectTarget: AppRoute
}

struct DefectFormModel: Sendable, Equatable {
    struct DeadlineOption: Sendable, Equatable {
        let label: String
        let offsetDays: Int?
    }

    let tradeId: String
    let title: String
    let saveLabel: String
    let cancelLabel: String
    let context: String
    let finding: String
    let descriptionLabel: String
    let description: String
    let photosLabel: String
    let photoLabels: [String]
    let addPhotoLabel: String
    let severityLabel: String
    let severities: [String]
    let initialSeverity: Int
    let deadlineLabel: String
    let deadlineOptions: [DeadlineOption]
    let deadlineBase: CalendarDay
    let initialDeadline: Int
    let responsibleLabel: String
    let responsible: String
    let notifyLabel: String
    let notifyInitial: Bool

    /// The date text of a deadline chip ("Fr, 15.01.2027"); nil for a chip without an offset or out of range.
    func deadlineText(option: Int) -> String? {
        guard deadlineOptions.indices.contains(option), let offset = deadlineOptions[option].offsetDays else {
            return nil
        }
        return DeadlineMath.label(base: deadlineBase, offsetDays: offset, style: .ios)
    }
}

struct SignOffModel: Sendable, Equatable {
    struct Stat: Sendable, Equatable {
        let value: String
        let label: String
        let tone: CountTone
    }

    struct ReservedDefect: Sendable, Equatable {
        let title: String
        let meta: String
        let due: String
    }

    let inspectionId: String
    let title: String
    let subtitle: String
    let stats: [Stat]
    let defects: [ReservedDefect]
    let reservation: String
    let craftsmanLabel: String
    let craftsmanName: String
    let craftsmanStatus: String
    let ownerLabel: String
    let ownerName: String
    let ownerPending: String
    let signHereLabel: String
    let signatureLabel: String
    let finishLabel: String
    let note: String
    let finishTarget: NavigationTarget = .defects
}

enum ProtocolTone: Sendable, Equatable { case ok, warn }

enum ProtocolMark: Sendable, Equatable { case ok, defect }

/// Earthworks, shell and carpentry protocol screens.
struct TradeProtocolModel: Sendable, Equatable {
    struct Banner: Sendable, Equatable {
        let tone: ProtocolTone
        let title: String
        let text: String
        /// The label of the PDF button; nil without one.
        let documentLabel: String?
        let target: NavigationTarget?
    }

    struct Row: Sendable, Equatable {
        let label: String
        let meta: String
        let mark: ProtocolMark
        let target: NavigationTarget?
    }

    struct Group: Sendable, Equatable {
        let title: String
        let rows: [Row]
    }

    let tradeId: String
    let title: String
    let subtitle: String
    let banner: Banner
    let groups: [Group]
}

/// Rohbau "Maße & Lot".
struct MeasureCheckModel: Sendable, Equatable {
    struct Dimension: Sendable, Equatable {
        let key: MeasureViewModel.Dimension
        let label: String
    }

    let route: AppRoute
    let counter: String
    let title: String
    let instruction: String
    let measuredHeading: String
    let plumbTitle: String
    let plumbSubtitle: String
    let plumbValue: String
    let saveLabel: String
    let photoLabel: String
    let defectLabel: String
    let withinLabel: String
    let outsideLabel: String
    let dimensions: [Dimension]
    let measured: [String]
    let initial: MeasureViewModel
    let defectTarget: AppRoute = .newDefect

    func subtitle(tolerance: Int) -> String {
        String(format: ScreenCopy.RohbauMasse.subtitleFormat, tolerance)
    }
}
