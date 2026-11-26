import XCTest

/// M7 D15/D16: at Dynamic Type XL no text is clipped (audit) and no two texts overlap (frame test), on every screen,
/// as launched and after one scroll. A truncation the prototype itself styles (`nowrap` + `ellipsis`) is listed in
/// `IntendedEllipsis` (P6); findings that cannot be fixed without breaking a project rule go to `AuditAllowlist`.
@MainActor
final class LargeTextUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUp() async throws {
        continueAfterFailure = false
        app = XCUIApplication()
    }

    private func launch(_ screen: String) {
        if app.state != .notRunning { app.terminate() }
        app.launchArguments = [
            "-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryXL", "-BaupilotScreen", screen,
        ]
        app.launch()
    }

    /// Two texts overlap when their frames intersect by more than 1 pt in both axes and neither contains the other.
    /// Containment is taken with half a point of slack: a combined element ("Gewerk, Estrich") and its own text differ
    /// by float noise only.
    static func overlaps(_ a: CGRect, _ b: CGRect) -> Bool {
        let intersection = a.intersection(b)
        guard !intersection.isNull, intersection.width > 1, intersection.height > 1 else { return false }
        return !a.insetBy(dx: -0.5, dy: -0.5).contains(b) && !b.insetBy(dx: -0.5, dy: -0.5).contains(a)
    }

    /// The pairs of visible static texts (label, frame) that overlap.
    private func overlappingTexts() -> [String] {
        let screen = app.windows.firstMatch.frame
        let texts = app.staticTexts.allElementsBoundByIndex.compactMap { element -> (String, CGRect)? in
            guard element.exists, element.frame.width > 0, element.frame.height > 0, element.frame.intersects(screen)
            else { return nil }
            return (element.label, element.frame)
        }
        var found: [String] = []
        for i in texts.indices {
            for j in texts.indices where j > i && Self.overlaps(texts[i].1, texts[j].1) {
                found.append("\"\(texts[i].0)\" \(texts[i].1) x \"\(texts[j].0)\" \(texts[j].1)")
            }
        }
        return found
    }

    /// `TEST_RUNNER_LARGE_TEXT_SCREENS=Erd,Login` narrows a run to those screens (a development aid; unset = all 34).
    /// A variable that selects no screen is a failure (see `AccessibilityAuditUITests.selectedScreens`).
    private static var selection: (screens: [String], error: String?) {
        AccessibilityAuditUITests.selectedScreens(variable: "LARGE_TEXT_SCREENS")
    }

    /// True when an audit finding is accepted: an intended ellipsis (P6) or an `AuditAllowlist` entry. Everything else
    /// is logged with its frame and element so a failing run says which view to fix.
    private static func filtered(_ issue: XCUIAccessibilityAuditIssue, screen: String) -> Bool {
        if IntendedEllipsis.matches(issue, screen: screen) { return true }
        if AuditAllowlist.matches(issue, screen: screen) { return true }
        NSLog("XL-FINDING screen=%@ type=%ld frame=%@ | %@ | %@", screen, issue.auditType.rawValue,
              issue.element.map { NSCoder.string(for: $0.frame) } ?? "-", issue.element?.debugDescription ?? "-",
              issue.detailedDescription)
        return false
    }

    private func check(_ name: String) {
        XCTContext.runActivity(named: "\(name) XL") { _ in
            launch(name)
            XCTAssertTrue(app.wait(for: .runningForeground, timeout: 10), name)
            XCTAssertTrue(
                app.descendants(matching: .any).matching(NSPredicate(format: "identifier BEGINSWITH 'screen.'"))
                    .firstMatch.waitForExistence(timeout: 10),
                name)
            for pass in ["top", "scrolled"] {
                if pass == "scrolled" { app.swipeUp() }
                do {
                    try app.performAccessibilityAudit(for: [.textClipped, .dynamicType]) { issue in
                        Self.filtered(issue, screen: name)
                    }
                } catch {
                    XCTFail("\(name) XL \(pass): \(error)")
                }
                for pair in overlappingTexts() {
                    XCTFail("\(name) XL \(pass): overlapping texts \(pair)")
                }
            }
        }
    }

    func testNoClippedOrOverlappingTextAtXL() throws {
        continueAfterFailure = true
        let selection = Self.selection
        if let error = selection.error {
            XCTFail(error)
            return
        }
        for name in selection.screens { check(name) }
    }
}

/// P6: a text that the prototype styles `white-space: nowrap` + `text-overflow: ellipsis` truncates at XL on purpose.
/// Each entry is (screen, element identifier or label, prototype file:line); the line must carry `nowrap` or `ellipsis`
/// (guarded by scripts/test/links.test.mjs). Only `.textClipped` findings are filtered.
enum IntendedEllipsis {
    static let entries: [(screen: String, identifier: String, prototypeRef: String)] = [
    ]

    static func matches(_ issue: XCUIAccessibilityAuditIssue, screen: String) -> Bool {
        guard issue.auditType == .textClipped else { return false }
        let identifier = issue.element?.identifier ?? ""
        let label = issue.element?.label ?? ""
        return entries.contains { $0.screen == screen && ($0.identifier == identifier || $0.identifier == label) }
    }
}
