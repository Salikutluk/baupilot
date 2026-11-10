import Foundation

/// Tone of an overview row badge; each screen draws it with its own prototype colours.
enum OverviewTone: Sendable, Equatable { case ok, open, running, today, planned, required, defect }

/// Sendable stand-in for the icon of a plumbing overview row.
enum OverviewIcon: Sendable, Equatable {
    case pipe, drop, heat, home

    var symbol: BaupilotIcon {
        switch self {
        case .pipe: return .pipe
        case .drop: return .drop
        case .heat: return .heat
        case .home: return .home
        }
    }
}

/// A paint swatch; the screen layer maps it to the supplement tokens.
enum PaintColor: Sendable, Equatable { case white, sand, sage }

/// What leads an overview row: an icon (plumbing), a paint swatch (painting) or nothing.
enum OverviewLeading: Sendable, Equatable {
    case icon(OverviewIcon)
    case swatch(PaintColor)
    case none
}

struct FactModel: Sendable, Equatable {
    let label: String
    let value: String
}

/// The strip under the title of an overview: phase tiles or a row of facts.
enum OverviewHeader: Sendable, Equatable {
    case phases([PhaseModel])
    case facts([FactModel])
}

struct TradeOverviewModel: Sendable, Equatable {
    struct Row: Sendable, Equatable {
        let name: String
        let sub: String
        let statusLabel: String
        let tone: OverviewTone
        let leading: OverviewLeading
        let target: AppRoute?
    }

    struct Group: Sendable, Equatable {
        let title: String
        let rows: [Row]
    }

    let tradeId: String
    let title: String
    let subtitle: String
    let header: OverviewHeader
    /// The banner text under the header; nil without a banner.
    let notice: String?
    let groups: [Group]
}

enum SectionStatus: Sendable, Equatable { case ok, defect, now, open }

/// State of an object of the bath or a wall of a room.
enum CheckItemState: Sendable, Equatable { case done, now, open }

/// Kind of a bar of the heating chart.
enum HeatingBarKind: Sendable, Equatable { case recorded, today, planned }

struct SlopeCheckModel: Sendable, Equatable {
    struct Section: Sendable, Equatable {
        let id: String
        let label: String
        let status: SectionStatus
    }

    let route: AppRoute
    let counter: String
    let title: String
    let subtitle: String
    let hint: String
    let warning: String
    let sections: [Section]
    let initial: SlopeViewModel
    let defectLabel: String
    let lengthLabel: String = ScreenCopy.SanGefaelle.lengthLabel
    let dropLabel: String = ScreenCopy.SanGefaelle.dropLabel
    let saveLabel: String = ScreenCopy.SanGefaelle.saveLabel
    let photoLabel: String = ScreenCopy.SanGefaelle.photoLabel
    let schemaLevelLabel: String = ScreenCopy.SanGefaelle.schemaLevelLabel
    let schemaDeltaLabel: String = ScreenCopy.SanGefaelle.schemaDeltaLabel
    let defectTarget: AppRoute = .newDefect
}

struct PressureCheckModel: Sendable, Equatable {
    struct Done: Sendable, Equatable {
        let label: String
        let meta: String
    }

    struct Current: Sendable, Equatable {
        let n: Int
        let title: String
    }

    struct Timer: Sendable, Equatable {
        let fraction: Double
        let remaining: String
        /// `timer.progressLabel`, the accessibility label of the ring.
        let accessibilityLabel: String
        let remainingLabel: String
        let note: String
        let startLabel: String
        let startValue: String
        let endLabel: String
        /// `timer.end.value`: the text of the end field until a value is entered.
        let endPlaceholder: String
    }

    struct Next: Sendable, Equatable {
        let n: Int
        let label: String
        let tag: String?
    }

    let route: AppRoute
    let counter: String
    let title: String
    let subtitle: String
    let done: [Done]
    let current: Current
    let timer: Timer
    let next: [Next]
    let photoLabel: String = ScreenCopy.SanDruck.photoLabel
}

struct ObjectCheckModel: Sendable, Equatable {
    struct Object: Sendable, Equatable {
        let name: String
        let state: CheckItemState
    }

    struct Check: Sendable, Equatable {
        let label: String
        let hint: String
        let ok: Bool
    }

    let route: AppRoute
    /// `subtitle.ios`, shown as the trailing text of the back bar (D17).
    let trailing: String
    let counter: String
    let title: String
    let objects: [Object]
    /// The name of the selected object.
    let heading: String
    let checks: [Check]
    let nextHint: String
    let tabsLabel: String = ScreenCopy.SanBad.tabsLabel
}

struct FlatnessCheckModel: Sendable, Equatable {
    let route: AppRoute
    let counter: String
    let title: String
    let instruction: String = ScreenCopy.EsEbenheit.instruction
    let stepperLabelPrefix: String = ScreenCopy.EsEbenheit.stepperLabelPrefix
    let stepperAccessibilityNoun: String = ScreenCopy.EsEbenheit.stepperAccessibilityNoun
    let saveLabel: String = ScreenCopy.EsEbenheit.saveLabel
    let photoLabel: String = ScreenCopy.EsEbenheit.photoLabel
    let measuredFormat: String = ScreenCopy.EsEbenheit.measuredFormat
    let initial: FlatnessViewModel
    let defectTarget: AppRoute = .newDefect

    /// "Grenzwert laut Vertrag: 4 mm Spalt unter 2-m-Latte" for the limit text of the view model.
    func subtitle(limitText: String) -> String {
        String(format: ScreenCopy.EsEbenheit.subtitleFormat, limitText)
    }
}

struct ReadinessCheckModel: Sendable, Equatable {
    struct Bar: Sendable, Equatable {
        let height: Int
        let kind: HeatingBarKind
    }

    struct Chart: Sendable, Equatable {
        let bars: [Bar]
        let accessibilityLabel: String
        let start: String
        let max: String
        let end: String
        let today: String
        let plan: String
        let action: String
    }

    struct CmRow: Sendable, Equatable {
        let label: String
        let meta: String
        let done: Bool
    }

    struct Cm: Sendable, Equatable {
        let title: String
        let tag: String
        let text: String
        let rows: [CmRow]
        let note: String
    }

    let route: AppRoute
    let screenTitle: String = ScreenCopy.EsBelegreife.screenTitle
    /// The title of the card ("Aufheizprotokoll"), `title` of the fixture.
    let cardTitle: String
    let counter: String
    let text: String
    let chart: Chart
    let cm: Cm
}

struct TileCheckModel: Sendable, Equatable {
    struct Current: Sendable, Equatable {
        let n: Int
        let title: String
        let text: String
    }

    struct Next: Sendable, Equatable {
        let n: Int
        let label: String
    }

    struct Tile: Sendable, Equatable {
        let id: String
        let hollow: Bool
        let label: String?
        let accessibilityLabel: String
    }

    let route: AppRoute
    let counter: String
    let subtitle: String
    let title: String
    let done: [String]
    let current: Current
    let next: [Next]
    let columns: Int = ScreenCopy.FlBad.columns
    let tiles: [Tile]
    let wallLabel: String
    let markHint: String
    let gridAccessibilityLabel: String
    let result: String
    let defectLabel: String = ScreenCopy.FlBad.defectLabel
    let noneLabel: String = ScreenCopy.FlBad.noneLabel
    let defectTarget: AppRoute = .newDefect
}

struct PaintCheckModel: Sendable, Equatable {
    struct Current: Sendable, Equatable {
        let n: Int
        let title: String
        let text: String
    }

    struct Wall: Sendable, Equatable {
        let name: String
        let state: CheckItemState
    }

    struct Next: Sendable, Equatable {
        let n: Int
        let label: String
    }

    let route: AppRoute
    let counter: String
    let subtitle: String
    let title: String
    let done: [String]
    let doneSwatch: PaintColor = ScreenCopy.MaRaum.doneSwatch
    let current: Current
    let walls: [Wall]
    let wallsLabel: String = ScreenCopy.MaRaum.wallsLabel
    let next: [Next]
    let defectLabel: String
    let okLabel: String
    let photoLabel: String = ScreenCopy.MaRaum.photoLabel
    let defectTarget: AppRoute = .newDefect
}
