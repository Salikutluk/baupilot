import Foundation

protocol BackendRepository: Sendable {
    func now() async throws -> Date
    func project() async throws -> Project
    func trades() async throws -> [Trade]
    func schedule() async throws -> Schedule
    func offers(tradeId: String?) async throws -> [Offer]
    func comparison(tradeId: String) async throws -> Comparison?
    func documents() async throws -> Documents
    func defects() async throws -> [Defect]
    func resolvedDefects() async throws -> ResolvedDefects
    func notifications() async throws -> [NotificationGroup]
    func inspections() async throws -> JSONValue
    func measurements() async throws -> JSONValue
    func screen(_ name: String) async throws -> JSONValue?
}
