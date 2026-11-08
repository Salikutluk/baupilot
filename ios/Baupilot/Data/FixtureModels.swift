import Foundation

struct Fixture: Decodable, Sendable {
    let meta: Meta
    let project: Project
    let trades: [Trade]
    let schedule: Schedule
    let offers: [Offer]
    let comparisons: [Comparison]
    let documents: Documents
    let inspections: JSONValue
    let measurements: JSONValue
    let defects: [Defect]
    let resolvedDefects: ResolvedDefects
    let notifications: [NotificationGroup]
    let screens: JSONValue
}

struct Meta: Decodable, Sendable {
    let schemaVersion: Int
    let source: String
    let now: String
}

struct Project: Decodable, Sendable {
    struct Phase: Decodable, Sendable {
        let current: Int
        let total: Int
        let tradeId: String
        let progressPercent: Int
    }

    struct Owner: Decodable, Sendable {
        let name: String
        let initials: String
        let email: String
        let role: String
    }

    let id: String
    let buildingType: String
    let address: String
    let constructionStart: String
    let moveIn: String
    let phase: Phase
    let unreadNotifications: Int
    let owner: Owner
}

struct Trade: Decodable, Sendable {
    struct Window: Decodable, Sendable {
        let startMonth: Double
        let endMonth: Double
    }

    struct Fact: Decodable, Sendable {
        let label: String
        let value: String
    }

    struct Detail: Decodable, Sendable {
        let phaseLabel: String
        let facts: [Fact]
        let offersSummary: PlatformText
    }

    let id: String
    let name: String
    let phase: Int
    let window: Window
    let scheduleState: String
    let askNow: Bool
    let weekLabel: String?
    let statusLabel: String?
    let detail: Detail?
}

struct Schedule: Decodable, Sendable {
    struct AxisLabel: Decodable, Sendable {
        let month: Int
        let label: String
    }

    struct Ask: Decodable, Sendable {
        let tradeIds: [String]
        let name: String
        let sub: String
        let target: String?
    }

    let originLabel: String
    let spanMonths: Int
    let todayMonth: Double
    let axis: [AxisLabel]
    let asks: [Ask]
}

struct Offer: Decodable, Sendable {
    let id: String
    let tradeId: String
    let firm: String
    let price: String
    let best: Bool
    let pending: Bool
    let meta: PlatformText
    let documentId: String?
}

struct Comparison: Decodable, Sendable {
    struct Row: Decodable, Sendable {
        let label: String
        let a: String
        let b: String
        let strong: Bool
    }

    let tradeId: String
    let offerIds: [String]
    let columns: [String]
    let rows: [Row]
    let summary: PlatformText
    let hint: PlatformText
    let pending: PlatformText
}

struct Documents: Decodable, Sendable {
    struct Counts: Decodable, Sendable {
        let all: Int
        let offer: Int
        let contract: Int
        let invoice: Int
        let plan: Int
        let trades: Int
    }

    struct Category: Decodable, Sendable {
        let kind: String
        let label: String
        let count: String
    }

    let counts: Counts
    let categories: [Category]
    let items: [DocumentItem]
}

struct DocumentItem: Decodable, Sendable {
    struct Viewer: Decodable, Sendable {
        struct Issuer: Decodable, Sendable {
            let name: String
            let address: String
        }

        struct Line: Decodable, Sendable {
            let pos: String
            let text: String
            let sum: String
        }

        struct Fact: Decodable, Sendable {
            let label: String
            let value: String
        }

        let subtitle: PlatformText
        let pageLabel: String
        let pageFooter: PlatformText
        let issuer: Issuer
        let number: String
        let reference: String
        let total: String
        let lines: [Line]
        let facts: [Fact]
    }

    let id: String
    let kind: String
    let tradeId: String?
    let title: String
    let meta: PlatformText
    let amount: String?
    let viewer: Viewer?
    let statusLabel: String?
    let due: String?
}

struct Defect: Decodable, Sendable {
    let id: String
    let tradeId: String
    let trade: PlatformText
    let title: String
    let deadline: String
    let deadlineLabel: String
    let dueLabel: String
    let urgency: String
    let meta: String
    let photos: Int?
    let severity: String?
    let description: String?
    let elementId: String?
    let responsible: String?
    let notifyByEmail: Bool?
}

struct ResolvedDefects: Decodable, Sendable {
    let count: String
}

struct NotificationGroup: Decodable, Sendable {
    let title: String
    let items: [NotificationItem]
}

struct NotificationItem: Decodable, Sendable {
    let id: String
    let kind: String
    let title: String
    let text: String
    let when: String
    let unread: Bool
    let target: String?
}
