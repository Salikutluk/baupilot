import XCTest

/// M7 accessibility pins (D9, D10): a diagram is one element with a description. The phase-12 audit extends this class.
@MainActor
final class AccessibilityAuditUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUp() async throws {
        continueAfterFailure = false
        app = XCUIApplication()
    }

    /// P8: the audit switches the simulator-wide appearance; it never leaves the simulator in dark mode.
    override func tearDown() async throws {
        XCUIDevice.shared.appearance = .light
    }

    private func launchApp() {
        if app.state != .notRunning { app.terminate() }
        app.launchArguments = []
        app.launch()
    }

    private func element(_ identifier: String) -> XCUIElement {
        app.descendants(matching: .any)[identifier]
    }

    private func logIn() {
        XCTAssertTrue(element("screen.login").waitForExistence(timeout: 10))
        let submit = app.buttons["login.submit"]
        XCTAssertTrue(submit.waitForExistence(timeout: 5))
        submit.tap()
        XCTAssertTrue(element("screen.home").waitForExistence(timeout: 10))
    }

    /// D9: the Zeitplan chart is one image element carrying the fixture's `ariaLabel`, and its bars and month labels
    /// are not elements of their own.
    func testScheduleChartIsOneImageElement() throws {
        launchApp()
        logIn()
        let tab = element("tab.schedule").firstMatch
        XCTAssertTrue(tab.waitForExistence(timeout: 5))
        tab.tap()
        XCTAssertTrue(element("screen.schedule").waitForExistence(timeout: 5))
        let label = "Zeitplan aller Gewerke von März 2026 bis September 2027, heute Mitte Dezember 2026"
        let charts = app.images.matching(NSPredicate(format: "label == %@", label))
        XCTAssertTrue(charts.firstMatch.waitForExistence(timeout: 5))
        XCTAssertEqual(charts.count, 1)
        let anyChart = app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", label))
        XCTAssertEqual(anyChart.count, 1)
    }

    /// The 34 screen names in design/screens.md order (guarded against `parseScreens` by links.test.mjs).
    static let screens: [String] = [
        "Login",
        "Home",
        "Zeitplan",
        "Profil",
        "Gewerk",
        "Dokumente",
        "DokViewer",
        "Vergleich",
        "Mitteilungen",
        "AbnGewerke",
        "AbnElemente",
        "AbnPruefung",
        "AbnMangel",
        "AbnSignatur",
        "AbnMaengel",
        "Erd",
        "Rohbau",
        "RohbauMasse",
        "Zimmerer",
        "ElUebersicht",
        "ElRaum",
        "ElVerteiler",
        "ElRauchmelder",
        "SanUebersicht",
        "SanGefaelle",
        "SanDruck",
        "SanBad",
        "EsUebersicht",
        "EsEbenheit",
        "EsBelegreife",
        "FlUebersicht",
        "FlBad",
        "MaUebersicht",
        "MaRaum",
    ]

    /// Audit types: every type (.all), including .dynamicType, .textClipped and .hitRegion.
    private static let auditTypes: XCUIAccessibilityAuditType = .all

    /// `TEST_RUNNER_AUDIT_SCREENS=ElRauchmelder,Login` narrows a run to those screens (a development aid; unset = all 34).
    /// A variable that names an unknown screen, or no screen at all, selects nothing and is a failure, never a silent pass.
    static func selectedScreens(variable: String) -> (screens: [String], error: String?) {
        guard let only = ProcessInfo.processInfo.environment[variable] else { return (screens, nil) }
        let wanted = only.split(separator: ",").map(String.init)
        let unknown = wanted.filter { !screens.contains($0) }
        if wanted.isEmpty || !unknown.isEmpty {
            return ([], "\(variable)='\(only)' selects no screen or an unknown one (\(unknown)); known: \(screens)")
        }
        return (screens.filter(wanted.contains), nil)
    }

    private func launch(_ screen: String, dark: Bool, appearanceSwitch: Bool) {
        if app.state != .notRunning { app.terminate() }
        var arguments = ["-BaupilotScreen", screen]
        if dark && !appearanceSwitch { arguments += ["-AppleInterfaceStyle", "Dark"] }
        app.launchArguments = arguments
        app.launch()
    }

    /// D14: `performAccessibilityAudit` on every screen, light and then dark (one method: XCTest runs methods
    /// alphabetically, so the stale-entry check at the end sees both passes). Each screen audits twice: as launched
    /// and after one scroll down. Findings that cannot be fixed without breaking a project rule are in `AuditAllowlist`.
    func testAuditAllScreens() throws {
        continueAfterFailure = true
        let selection = Self.selectedScreens(variable: "AUDIT_SCREENS")
        if let error = selection.error {
            XCTFail(error)
            return
        }
        AuditAllowlist.resetHits()
        for dark in [false, true] {
            // Preferred: the simulator-wide appearance. Fallback (recorded in the notes): the launch argument.
            XCUIDevice.shared.appearance = dark ? .dark : .light
            let appearanceSwitch = XCUIDevice.shared.appearance == (dark ? .dark : .light)
            for name in selection.screens {
                XCTContext.runActivity(named: "\(name) \(dark ? "dark" : "light")") { _ in
                    launch(name, dark: dark, appearanceSwitch: appearanceSwitch)
                    XCTAssertTrue(app.wait(for: .runningForeground, timeout: 10), name)
                    XCTAssertTrue(
                        app.descendants(matching: .any).matching(NSPredicate(format: "identifier BEGINSWITH 'screen.'"))
                            .firstMatch.waitForExistence(timeout: 10),
                        name)
                    do {
                        try app.performAccessibilityAudit(for: Self.auditTypes) { issue in
                            AuditAllowlist.matches(issue, screen: name, dark: dark)
                        }
                        app.swipeUp()
                        try app.performAccessibilityAudit(for: Self.auditTypes) { issue in
                            AuditAllowlist.matches(issue, screen: name, dark: dark)
                        }
                    } catch {
                        XCTFail("\(name) \(dark ? "dark" : "light"): \(error)")
                    }
                }
            }
        }
        XCUIDevice.shared.appearance = .light
        guard ProcessInfo.processInfo.environment["AUDIT_SCREENS"] == nil else { return }
        let stale = AuditAllowlist.entries.filter { !AuditAllowlist.wasHit($0) }
        XCTAssertTrue(stale.isEmpty, "stale allowlist entries: \(stale)")
    }
}

/// D14: findings that cannot be fixed without breaking a project rule (e.g. the contrast of a design-token colour
/// pair). `matches` is true only for an exact (screen, audit type, element identifier or label) entry; an entry with
/// `orNilElement` additionally accepts a finding the audit reports without any element, but only on the entry's own
/// screen, audit type and appearance, and every such match is logged as `AUDIT-ALLOWED ... path=nil-element`.
enum AuditAllowlist {
    struct Entry: Equatable, CustomStringConvertible {
        let screen: String
        let auditType: XCUIAccessibilityAuditType
        let element: String
        let reason: String
        /// The appearance the finding belongs to; the entry never matches in the other one.
        let dark: Bool
        /// The audit reports one finding sometimes with the element and sometimes with `issue.element` nil;
        /// `orNilElement` lets the entry match the element-less report too (never an element without a name).
        var orNilElement = false
        var description: String { "(\(screen), \(auditType.rawValue), \(element))" }
    }

    static let entries: [Entry] = [
        Entry(screen: "Dokumente", auditType: .textClipped, element: "Dokumente durchsuchen", reason: "the single-line search TextField is reported as clipped at every size, light and dark (one entry per appearance; removing the `.frame(maxHeight: .infinity)` on the field does not clear it, measured); `TextField(axis: .vertical).lineLimit(1)` clears it but the form moves the text by 1.3 pt (IosDokumente.dc.html search row, 44 pt, 16 pt text) and changes the StatusInput gallery golden", dark: false),
        Entry(screen: "Dokumente", auditType: .textClipped, element: "Dokumente durchsuchen", reason: "same finding in the dark appearance (see the light entry)", dark: true),
        Entry(screen: "ElRauchmelder", auditType: .contrast, element: "step.ok", reason: "dark: the OK button label is Tokens.okInk.elRauchmelder (white) on Tokens.ok.elRauchmelder (the dark ok green), 3.26:1 (IosElRauchmelder.dc.html:80, dark branch of t); a generated design-token pair that must not be changed (CLAUDE.md). The audit reports it as 'Contrast nearly passed', in some runs for step.ok (\"Piept – OK\") and in others with issue.element nil", dark: true, orNilElement: true),
    ]

    nonisolated(unsafe) private static var hits: [Int] = []

    static func resetHits() { hits = [] }

    static func wasHit(_ entry: Entry) -> Bool {
        guard let index = entries.firstIndex(of: entry) else { return false }
        return hits.contains(index)
    }

    static func matches(_ issue: XCUIAccessibilityAuditIssue, screen: String, dark: Bool = false) -> Bool {
        let identifier = issue.element?.identifier ?? ""
        let label = issue.element?.label ?? ""
        let found = entries.enumerated().first { _, entry in
            guard entry.screen == screen, entry.auditType == issue.auditType, entry.dark == dark else { return false }
            if !identifier.isEmpty && entry.element == identifier { return true }
            if !label.isEmpty && entry.element == label { return true }
            return entry.orNilElement && issue.element == nil
        }
        guard let (index, entry) = found else {
            NSLog("AUDIT-FINDING screen=%@ type=%ld id=%@ label=%@ | %@ | %@", screen, issue.auditType.rawValue, identifier, label, issue.compactDescription, issue.detailedDescription)
            return false
        }
        NSLog("AUDIT-ALLOWED screen=%@ type=%ld path=%@ entry=%@", screen, issue.auditType.rawValue,
              issue.element == nil ? "nil-element" : "named", entry.description)
        hits.append(index)
        return true
    }
}
