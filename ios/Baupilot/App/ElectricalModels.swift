import Foundation

/// Sendable stand-in for the icon of an electrical overview row.
enum ElectricalIcon: Sendable, Equatable {
    case meterCabinet, smokeAlarm, home, document

    var symbol: BaupilotIcon {
        switch self {
        case .meterCabinet: return .meterCabinet
        case .smokeAlarm: return .smokeAlarm
        case .home: return .home
        case .document: return .document
        }
    }
}

enum ElectricalTone: Sendable, Equatable { case open, running, ok, missing }

enum SocketStatus: String, Sendable, Equatable { case ok, defect, now, open }

enum FiTone: Sendable, Equatable { case ok, open }

enum SmokeRoomStatus: Sendable, Equatable { case ok, now, open }

struct PhaseModel: Sendable, Equatable {
    enum Kind: Sendable, Equatable {
        case done(status: String, date: String?)
        case active(progressLabel: String, fraction: Double)
    }

    let title: String
    let kind: Kind
}

struct ElectricalOverviewModel: Sendable, Equatable {
    struct Row: Sendable, Equatable {
        let name: String
        let sub: String
        let icon: ElectricalIcon
        let statusLabel: String
        let tone: ElectricalTone
        let target: AppRoute?
    }

    struct Group: Sendable, Equatable {
        let title: String
        /// The "Alle 9" link of a group; nil without one.
        let moreLabel: String?
        let rows: [Row]
    }

    let tradeId: String
    let title: String
    let subtitle: String
    let phases: [PhaseModel]
    let groups: [Group]
}

struct RoomCheckModel: Sendable, Equatable {
    struct Done: Sendable, Equatable {
        let label: String
        let meta: String
    }

    struct Current: Sendable, Equatable {
        let n: Int
        let title: String
        let text: String
    }

    struct Socket: Sendable, Equatable {
        let id: String
        let label: String
        let status: SocketStatus
        let accessibilityLabel: String
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
    let sockets: [Socket]
    let progressLabel: String
    let defectNote: String
    let okNote: String
    let defectLabel: String
    let next: [Next]
    let defectTarget: AppRoute = .newDefect
}

struct PanelCheckModel: Sendable, Equatable {
    struct Done: Sendable, Equatable {
        let label: String
        let meta: String
    }

    struct Current: Sendable, Equatable {
        let n: Int
        let title: String
        /// The instruction text followed by its steps.
        let steps: [String]
    }

    struct Fi: Sendable, Equatable {
        let name: String
        let sub: String
        let statusLabel: String
        let tone: FiTone
    }

    struct Next: Sendable, Equatable {
        let n: Int
        let label: String
        let tag: String?
    }

    let route: AppRoute
    let counter: String
    let title: String
    let callout: String
    let done: [Done]
    let current: Current
    let fis: [Fi]
    /// Exactly two: the defect outcome first, the ok outcome second.
    let outcomes: [String]
    let next: [Next]
    let defectTarget: AppRoute = .newDefect
}

struct SmokeCheckModel: Sendable, Equatable {
    struct Check: Sendable, Equatable {
        let label: String
        let hint: String
        let done: Bool
    }

    struct Room: Sendable, Equatable {
        let name: String
        let statusLabel: String
        let status: SmokeRoomStatus
    }

    let route: AppRoute
    let counter: String
    let title: String
    let subtitle: String
    let room: String
    let nowLabel: String
    let checks: [Check]
    /// Exactly two: the defect outcome first, the ok outcome second.
    let outcomes: [String]
    let rooms: [Room]
    let reminder: String
    let battery: String
    let reminderInitiallyOn: Bool = ScreenCopy.ElRauchmelder.reminderInitiallyOn
    let defectTarget: AppRoute = .newDefect
}
