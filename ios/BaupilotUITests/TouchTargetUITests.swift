import XCTest

/// M7 Step 9: every hittable control on every screen is at least 44 × 44 pt, or its identifier is in
/// `HitShapeAllowlist` because its reported frame is smaller while a unit-tested `contentShape` grows the hit area
/// (the list is empty: the frame includes `MinimumHitShape`).
@MainActor
final class TouchTargetUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUp() async throws {
        continueAfterFailure = true
        app = XCUIApplication()
    }

    /// The 34 screen names in design/screens.md order, shared with the audit (same literal list, guarded by links.test.mjs).
    private static let screens = AccessibilityAuditUITests.screens

    private static let controlTypes: [XCUIElement.ElementType] = [
        .button, .link, .switch, .textField, .secureTextField, .cell, .slider, .segmentedControl,
    ]

    private static let minimum: CGFloat = 44

    /// Frames come back as layout floats (43.99999999999994 pt is 44 pt); a real miss is far larger than this.
    private static let tolerance: CGFloat = 0.01

    private func launch(_ screen: String) {
        if app.state != .notRunning { app.terminate() }
        app.launchArguments = ["-BaupilotScreen", screen]
        app.launch()
    }

    private func misses(screen: String, pass: String) -> [String] {
        let predicate = NSPredicate(format: "elementType IN %@", Self.controlTypes.map { NSNumber(value: $0.rawValue) })
        let query = app.descendants(matching: .any).matching(predicate)
        var result: [String] = []
        let window = app.windows.firstMatch.frame
        for element in query.allElementsBoundByIndex where element.exists {
            let frame = element.frame
            // `isHittable` raises a test failure for an element with no activation point (a chip scrolled out of a
            // horizontal scroll view), so only elements fully inside the window are asked.
            guard window.contains(frame), element.isHittable else { continue }
            if frame.width >= Self.minimum - Self.tolerance && frame.height >= Self.minimum - Self.tolerance { continue }
            if HitShapeAllowlist.test(forIdentifier: element.identifier, type: element.elementType) != nil { continue }
            result.append(
                "\(screen) \(pass): id='\(element.identifier)' label='\(element.label)' "
                    + "type=\(element.elementType.rawValue) \(frame.width) x \(frame.height)")
        }
        return result
    }

    /// The `.hitRegion` audit on the current scroll position; findings on a hit-shape-allowlisted control are accepted.
    private func hitRegionFindings(screen: String, pass: String) -> [String] {
        var result: [String] = []
        do {
            try app.performAccessibilityAudit(for: .hitRegion) { issue in
                if AuditAllowlist.matches(issue, screen: screen)
                    || HitShapeAllowlist.test(
                        forIdentifier: issue.element?.identifier ?? "",
                        type: issue.element?.elementType ?? .any) != nil
                {
                    return true
                }
                result.append(
                    "\(screen) \(pass) hitRegion: id='\(issue.element?.identifier ?? "")' "
                        + "label='\(issue.element?.label ?? "")' \(issue.compactDescription)")
                return true
            }
        } catch {
            result.append("\(screen) \(pass) hitRegion audit: \(error)")
        }
        return result
    }

    func testAllScreensMeetMinimumTargets() throws {
        var all: [String] = []
        for name in Self.screens {
            XCTContext.runActivity(named: name) { _ in
                launch(name)
                XCTAssertTrue(
                    app.descendants(matching: .any).matching(NSPredicate(format: "identifier BEGINSWITH 'screen.'"))
                        .firstMatch.waitForExistence(timeout: 10),
                    name)
                all += misses(screen: name, pass: "top")
                all += hitRegionFindings(screen: name, pass: "top")
                app.swipeUp()
                all += misses(screen: name, pass: "scrolled")
                all += hitRegionFindings(screen: name, pass: "scrolled")
            }
        }
        XCTAssertTrue(all.isEmpty, "targets below 44 pt:\n" + all.joined(separator: "\n"))
    }
}

/// Identifier prefix → the unit test that proves the control's hit shape is at least 44 pt (`Class.testName`).
/// Empty: `XCUIElement.frame` includes a `MinimumHitShape` enlargement (the Profil switch draws 51 × 31 and measures
/// 51 × 44), and the only walk misses seen with an empty allowlist were float noise 1e-13 pt below 44, which
/// `minimum - tolerance` absorbs. A new entry needs a control whose reported frame is genuinely smaller than its hit
/// area; `scripts/test/links.test.mjs` checks that every named test exists in `ios/BaupilotTests`.
enum HitShapeAllowlist {
    static let entries: [String: String] = [:]

    static func test(forIdentifier identifier: String, type: XCUIElement.ElementType) -> String? {
        guard !identifier.isEmpty else { return nil }
        return entries.first { identifier.hasPrefix($0.key) }?.value
    }
}
