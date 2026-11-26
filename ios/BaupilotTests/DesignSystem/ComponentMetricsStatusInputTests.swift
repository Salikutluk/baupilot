import SwiftUI
import UIKit
import XCTest

@testable import Baupilot

@MainActor
final class ComponentMetricsStatusInputTests: XCTestCase {
    private let tolerance: CGFloat = 0.5

    override func setUp() {
        super.setUp()
        BaupilotFonts.registerIfNeeded()
    }

    func testStatusBadgeHeights() {
        let compact = size(of: StatusBadge("Offen", tone: .open), width: 200).height
        let roomy = size(of: StatusBadge("Offen", tone: .open, size: .roomy), width: 200).height
        let mini = size(of: StatusBadge("Günstigstes", tone: .ok, size: .mini, weight: .bold), width: 200).height
        // Line box (D1) + vertical padding: 12 pt → 15 + 2×4, 12 pt → 15 + 2×5, 11 pt → 14 + 2×3.
        XCTAssertEqual(compact, InterMetrics.lineBox(12) + 8, accuracy: tolerance)
        XCTAssertEqual(roomy, InterMetrics.lineBox(12) + 10, accuracy: tolerance)
        XCTAssertEqual(mini, InterMetrics.lineBox(11) + 6, accuracy: tolerance)
        XCTAssertEqual([compact, roomy, mini], [23, 25, 20])
    }

    /// Natural size of a view with no width proposal.
    private func intrinsic(_ view: some View) -> CGSize {
        UIHostingController(rootView: view.fixedSize()).sizeThatFits(
            in: CGSize(width: CGFloat.greatestFiniteMagnitude, height: .greatestFiniteMagnitude))
    }

    func testMicroBadgePadding() {
        let micro = intrinsic(StatusBadge("Günstigstes", tone: .ok, size: .micro, weight: .bold))
        // 11 pt text, padding 6 x 2 (IosVergleich:36): line box + 2·2 high, text + 2·6 wide.
        XCTAssertEqual(micro.height, InterMetrics.lineBox(11) + 4, accuracy: tolerance)
        let text = intrinsic(Text("Günstigstes").interText(11, .bold).fixedSize())
        XCTAssertEqual(micro.width, text.width + 12, accuracy: tolerance)
    }

    func testChecklistRowRingIs22() {
        XCTAssertEqual(intrinsic(ChecklistRow.Ring(done: true)), CGSize(width: 22, height: 22))
        XCTAssertEqual(intrinsic(ChecklistRow.Ring(done: false)), CGSize(width: 22, height: 22))
        // The ring is taller than the 15 pt title, so it sets the row height.
        XCTAssertEqual(size(of: ChecklistRow(title: "Angebot wählen", done: true), width: 350).height, 22)
        XCTAssertEqual(size(of: ChecklistRow(title: "Angebot wählen", done: false), width: 350).height, 22)
    }

    func testSegmentedControlHeights() {
        func height(_ h: SegmentedControl.SegmentHeight) -> CGFloat {
            size(
                of: SegmentedControl(
                    segments: ["Alle", "Offen"], selection: 0, height: h,
                    accessibilityLabel: "Filter", onSelect: { _ in }),
                width: 300
            ).height
        }
        XCTAssertEqual(height(.compact), 38)
        XCTAssertEqual(height(.regular), 48)
        XCTAssertEqual(height(.medium), 40)
    }

    func testToggleIs51x31() {
        let s = UIHostingController(
            rootView: IosToggle(isOn: true, accessibilityLabel: "Fristen & Termine", action: {}).fixedSize()
        ).sizeThatFits(in: CGSize(width: CGFloat.greatestFiniteMagnitude, height: CGFloat.greatestFiniteMagnitude))
        XCTAssertEqual(s, CGSize(width: 51, height: 31))
    }

    func testSearchFieldIs44Tall() {
        let s = size(
            of: SearchField(text: .constant(""), placeholder: "Suchen", accessibilityLabel: "Suche"),
            width: 335)
        XCTAssertEqual(s.height, 44)
    }

    func testFilterChipIs36Tall() {
        let s = size(
            of: FilterChips(items: ["Alle", "Pläne"], selection: 0, accessibilityLabel: "Filter", onSelect: { _ in }),
            width: 375)
        XCTAssertEqual(s.height, 36)
    }

    /// The chip draws 36 tall; its hit shape grows 4 pt on every side (44 tall) without moving the layout.
    func testFilterChipHitShapeIs44() {
        let box = CGRect(x: 0, y: 0, width: 60, height: 36)
        let hit = FilterChips.hitShape.path(in: box).boundingRect
        XCTAssertGreaterThanOrEqual(hit.height, 44)
        XCTAssertTrue(hit.contains(box))
    }

    /// The compact segment draws 34 tall; its hit shape grows to 44. The regular segment is already 44.
    func testSegmentedControlCompactHitShapeIs44() {
        let compact = CGRect(x: 0, y: 0, width: 80, height: SegmentedControl.SegmentHeight.compact.points)
        let hit = SegmentedControl.hitShape(for: .compact).path(in: compact).boundingRect
        XCTAssertGreaterThanOrEqual(hit.height, 44)
        XCTAssertTrue(hit.contains(compact))
        let regular = CGRect(x: 0, y: 0, width: 80, height: SegmentedControl.SegmentHeight.regular.points)
        XCTAssertEqual(SegmentedControl.hitShape(for: .regular).path(in: regular).boundingRect, regular)
    }

    func testMinimumHitShapeGrowsASmallBoxToA44CentredBox() {
        func hit(_ box: CGRect) -> CGRect { MinimumHitShape().path(in: box).boundingRect }
        XCTAssertEqual(hit(CGRect(x: 10, y: 20, width: 22, height: 22)), CGRect(x: -1, y: 9, width: 44, height: 44))
        XCTAssertEqual(hit(CGRect(x: 0, y: 0, width: 98, height: 20)), CGRect(x: 0, y: -12, width: 98, height: 44))
        let large = CGRect(x: 0, y: 0, width: 120, height: 48)
        XCTAssertEqual(hit(large), large)
    }

    /// The 51 × 31 track keeps its layout; the hit shape is 44 tall.
    func testToggleHitShapeIs44Tall() {
        let box = CGRect(x: 0, y: 0, width: 51, height: 31)
        let hit = IosToggle.hitShape.path(in: box).boundingRect
        XCTAssertGreaterThanOrEqual(hit.height, 44)
        XCTAssertTrue(hit.contains(box))
    }

    /// A short link ("Alle 6", 36 wide) gets a 44 × 44 hit shape that contains the drawn text box.
    func testSectionHeaderLinkHitShapeIs44By44() {
        let box = CGRect(x: 0, y: 0, width: 36, height: 44)
        let hit = SectionHeader.LinkButton.hitShape.path(in: box).boundingRect
        XCTAssertGreaterThanOrEqual(hit.width, 44)
        XCTAssertGreaterThanOrEqual(hit.height, 44)
        XCTAssertTrue(hit.contains(box))
    }

    /// The back bar's tappable parts keep the 44 pt row: the hit shape is the full 44 tall frame, not the 24 pt icon.
    func testBackBarControlsKeepA44Row() {
        XCTAssertEqual(size(of: BackBar(label: "Abnahmen", action: nil), width: 350).height, 44)
        XCTAssertEqual(
            size(of: BackBar(label: "Dokumente", action: nil, trailing: .icon(.share, size: 22, label: "Teilen", action: nil)), width: 350).height, 44)
        XCTAssertEqual(
            size(of: BackBar.modal(cancel: "Abbrechen", title: "Neuer Mangel", confirm: "Sichern"), width: 350).height, 44)
    }

    /// Identifiers are an accessibility annotation only: the chips keep their size.
    func testFilterChipsWithoutIdentifiersKeepSize() {
        func chips(_ identifiers: [String]?) -> FilterChips {
            FilterChips(
                items: ["Alle", "Angebote", "Verträge"], selection: 0, accessibilityLabel: "Dokumenttyp",
                identifiers: identifiers, onSelect: { _ in })
        }
        let without = size(of: chips(nil), width: 375)
        let with = size(of: chips(["documents.filter.all", "documents.filter.offer", "documents.filter.contract"]), width: 375)
        XCTAssertEqual(with, without)
        XCTAssertEqual(with.height, 36)
    }

    /// Label line box 16 + gap 6 + field 48; the trailing link (44 hit area, margin -14) adds nothing.
    func testFormFieldIs48Tall() {
        func height(_ link: FormField.Link?) -> CGFloat {
            size(
                of: FormField(
                    label: "Passwort", text: .constant(""), placeholder: "••••••••", kind: .password,
                    trailingLink: link),
                width: 342
            ).height
        }
        let label = InterMetrics.lineBox(13)
        XCTAssertEqual(height(nil), label + 6 + 48)
        XCTAssertEqual(height(FormField.Link(title: "Vergessen?", action: nil)), label + 6 + 48)
        XCTAssertEqual(height(nil), 70)
    }

    func testIconTilesAreSquare() {
        func tile(_ size: IconTile.Size) -> CGSize {
            UIHostingController(
                rootView: IconTile(
                    icon: .house, size: size, background: Tokens.accent, foreground: Tokens.onAccent
                ).fixedSize()
            ).sizeThatFits(in: CGSize(width: CGFloat.greatestFiniteMagnitude, height: CGFloat.greatestFiniteMagnitude))
        }
        XCTAssertEqual(tile(.brand40), CGSize(width: 40, height: 40))
        XCTAssertEqual(tile(.feature32), CGSize(width: 32, height: 32))
        XCTAssertEqual(size(of: FeatureRow(icon: .calendar, text: "Zeitplan"), width: 342).height, 32)
    }
}
