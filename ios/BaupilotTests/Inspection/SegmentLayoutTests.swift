import XCTest
@testable import Baupilot

final class SegmentLayoutTests: XCTestCase {
    private let tolerance: CGFloat = 0.0005

    func testTwelveSegmentsOn322WideAtScale3() {
        let edges = SegmentLayout.edges(count: 12, width: 322, gap: 3, scale: 3)
        XCTAssertEqual(edges.count, 12)
        XCTAssertEqual(edges.first?.start, 0)
        XCTAssertEqual(edges.last?.end, 322)
    }

    func testEdgesLieOnTheThirdPointGrid() {
        for edge in SegmentLayout.edges(count: 12, width: 322, gap: 3, scale: 3) {
            for value in [edge.start, edge.end] {
                XCTAssertEqual(value * 3, (value * 3).rounded(), accuracy: tolerance)
            }
        }
    }

    func testStartsFollowTheCumulativeExactPositions() {
        let exactWidth: CGFloat = (322 - 33) / 12
        let edges = SegmentLayout.edges(count: 12, width: 322, gap: 3, scale: 3)
        for (index, edge) in edges.enumerated() {
            let exact = CGFloat(index) * (exactWidth + 3)
            XCTAssertEqual(edge.start, (exact * 3).rounded() / 3, accuracy: tolerance)
            XCTAssertEqual(edge.end, ((exact + exactWidth) * 3).rounded() / 3, accuracy: tolerance)
        }
    }

    func testWidthsAndGapsDifferByAtMostOneDevicePixel() {
        let edges = SegmentLayout.edges(count: 12, width: 322, gap: 3, scale: 3)
        let pixel: CGFloat = 1.0 / 3.0
        let widths = edges.map { $0.end - $0.start }
        XCTAssertLessThanOrEqual(widths.max()! - widths.min()!, pixel + tolerance)
        let gaps = zip(edges, edges.dropFirst()).map { $1.start - $0.end }
        XCTAssertLessThanOrEqual(gaps.max()! - gaps.min()!, pixel + tolerance)
        XCTAssertEqual(gaps.reduce(0, +) / CGFloat(gaps.count), 3, accuracy: pixel)
    }

    func testSingleSegmentFillsTheWidth() {
        let edges = SegmentLayout.edges(count: 1, width: 100, gap: 3, scale: 2)
        XCTAssertEqual(edges, [SegmentLayout.Edge(start: 0, end: 100)])
    }

    func testNoSegmentsGiveNoEdges() {
        XCTAssertTrue(SegmentLayout.edges(count: 0, width: 100, gap: 3, scale: 3).isEmpty)
    }
}
