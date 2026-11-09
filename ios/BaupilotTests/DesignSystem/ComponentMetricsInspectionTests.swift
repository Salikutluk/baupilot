import SwiftUI
import XCTest
@testable import Baupilot

@MainActor
final class ComponentMetricsInspectionTests: XCTestCase {
    func testSegmentedProgressIsEightTallAndFillsTheWidth() {
        let bar = SegmentedProgress(
            segments: [Tokens.ok.core, Tokens.err.abnElemente, Tokens.track.abnElemente], height: 8,
            accessibilityLabel: "3 Segmente")
        let measured = size(of: bar, width: 322)
        XCTAssertEqual(measured.height, 8)
        XCTAssertEqual(measured.width, 322)
    }

    func testProgressLegendIsOneLineBoxTall() {
        let legend = ProgressLegend(items: [
            .init(label: "6 in Ordnung", color: Tokens.ok.core),
            .init(label: "1 Mangel", color: Tokens.err.abnElemente),
            .init(label: "5 offen", color: Tokens.track.abnElemente),
        ])
        XCTAssertEqual(size(of: legend, width: 322).height, InterMetrics.lineBox(13))
    }

    func testDetailTitleSize30StacksTitleAndSubtitleWithGap4() {
        let title = DetailTitle("Fenster & Türen", subtitle: "Fensterbau Weber GmbH · Fr, 18.12.2026", style: .size30)
        XCTAssertEqual(size(of: title, width: 350).height, InterMetrics.lineBox(30) + 4 + InterMetrics.lineBox(14))
    }

    func testCapsHeaderWithLinkKeepsTheLinkAtTheLineHeight() {
        let link = SectionHeader.Link(title: "Alle 6", size: 13, action: nil)
        let header = SectionHeader("Erdgeschoss", style: .caps(inset: 4, link: link))
        // min-height 44 with a -14 margin on both sides = 16, above the 13 pt line box.
        XCTAssertEqual(size(of: header, width: 350).height, max(InterMetrics.lineBox(13), 44 - 28))
    }

    func testCapsHeaderWithoutLinkIsUnchanged() {
        XCTAssertEqual(size(of: SectionHeader("Heute", style: .caps(inset: 4)), width: 350).height, InterMetrics.lineBox(13))
    }

    func testSectionHeaderLinkSizeDefaultsTo15() {
        XCTAssertEqual(SectionHeader.Link(title: "Alle", action: nil).size, 15)
    }

    func testInlineLinkKeepsA44HitAreaAndTrimsTheMargin() {
        XCTAssertEqual(size(of: InlineLink("Anleitung", size: 14, action: nil), width: 120).height, 44 - 24)
    }

    /// IosAbnPruefung:51: the tick badge is 20 pt, 6 pt from the top and the right edge.
    func testPhotoTileBadgeIs20At6() {
        XCTAssertEqual(size(of: PhotoTile.Badge(), width: 20), CGSize(width: 20, height: 20))
        XCTAssertEqual(PhotoTile.Badge.inset, 6)
    }

    func testPhotoTileKindsKeepTheirHeight() {
        let kinds: [PhotoTile.Kind] = [
            .scene(AnyView(PhotoPlaceholder()), caption: "Foto oben", checked: true),
            .icon(.camera),
            .icon(.image, fill: Tokens.chip.abnGewerke),
            .add(label: nil),
            .add(label: "Foto seitlich"),
        ]
        for kind in kinds {
            XCTAssertEqual(size(of: PhotoTile(kind: kind, height: .h84), width: 99.333).height, 84)
            XCTAssertEqual(size(of: PhotoTile(kind: kind, height: .h76), width: 99.333).height, 76)
        }
    }

    func testWindowLevelSketchIs56By44() {
        let sketch = WindowLevelSketchView(stroke: Tokens.muted, level: Tokens.accent, bubble: Tokens.chip.core)
        XCTAssertEqual(size(of: sketch, width: 56), CGSize(width: 56, height: 44))
    }
}
