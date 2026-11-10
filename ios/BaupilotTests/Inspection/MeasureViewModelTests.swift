import XCTest
@testable import Baupilot

final class MeasureViewModelTests: XCTestCase {
    private func makeModel(tolerance: Int = 12) -> MeasureViewModel {
        MeasureViewModel(
            target: .init(width: 1010, height: 1385),
            actual: .init(width: 1005, height: 1395),
            step: 5,
            toleranceDefault: tolerance,
            toleranceMin: 1,
            toleranceMax: 30,
            unit: "mm"
        )
    }

    func testInitialStateMatchesFixture() {
        let vm = makeModel()
        XCTAssertEqual(vm.diffText(.width), "−5 mm")
        XCTAssertEqual(vm.diffText(.height), "+10 mm")
        XCTAssertTrue(vm.isWithin(.width))
        XCTAssertTrue(vm.isWithin(.height))
        XCTAssertTrue(vm.isWithinTolerance)
        XCTAssertEqual(vm.tolerance, 12)
    }

    func testIncrementAndDecrementMoveByStep() {
        var vm = makeModel()
        vm.increment(.width)
        XCTAssertEqual(vm.actual.width, 1010)
        vm.decrement(.height)
        XCTAssertEqual(vm.actual.height, 1390)
        vm.decrement(.width)
        vm.decrement(.width)
        XCTAssertEqual(vm.actual.width, 1000)
        vm.increment(.height)
        XCTAssertEqual(vm.actual.height, 1395)
    }

    func testDiffTextUsesTypographicSigns() {
        var vm = makeModel()
        XCTAssertEqual(vm.diffText(.width).unicodeScalars.first?.value, 0x2212)
        XCTAssertEqual(vm.diffText(.height).unicodeScalars.first?.value, 0x002B)
        vm.decrement(.height)
        vm.decrement(.height)
        XCTAssertEqual(vm.diffText(.height), "±0 mm")
        XCTAssertEqual(vm.diffText(.height).unicodeScalars.first?.value, 0x00B1)
    }

    func testBoundaryIsInclusive() {
        var vm = makeModel(tolerance: 10)
        XCTAssertEqual(vm.diff(.height), 10)
        XCTAssertTrue(vm.isWithin(.height))
        vm = makeModel(tolerance: 9)
        XCTAssertFalse(vm.isWithin(.height))
        XCTAssertFalse(vm.isWithinTolerance)
    }

    func testOutsideWhenEitherDimensionExceeds() {
        var vm = makeModel()
        vm.decrement(.width)
        vm.decrement(.width)
        XCTAssertEqual(vm.diffText(.width), "−15 mm")
        XCTAssertFalse(vm.isWithin(.width))
        XCTAssertFalse(vm.isWithinTolerance)
        vm.increment(.width)
        XCTAssertTrue(vm.isWithinTolerance)
    }

    func testToleranceIsClampedToRange() {
        XCTAssertEqual(makeModel(tolerance: 0).tolerance, 1)
        XCTAssertEqual(makeModel(tolerance: 31).tolerance, 30)
        XCTAssertEqual(makeModel(tolerance: 12).tolerance, 12)
    }

    func testDecrementStopsAtZero() {
        var vm = MeasureViewModel(
            target: .init(width: 10, height: 10),
            actual: .init(width: 7, height: 5),
            step: 5,
            toleranceDefault: 12, toleranceMin: 1, toleranceMax: 30, unit: "mm"
        )
        vm.decrement(.width)
        XCTAssertEqual(vm.actual.width, 2)
        vm.decrement(.width)
        XCTAssertEqual(vm.actual.width, 2)
        vm.decrement(.height)
        XCTAssertEqual(vm.actual.height, 0)
        vm.decrement(.height)
        XCTAssertEqual(vm.actual.height, 0)
    }

    func testValueTextHasNoGrouping() {
        var vm = makeModel()
        XCTAssertEqual(vm.valueText(.width), "1005")
        for _ in 0..<1799 { vm.increment(.width) }
        XCTAssertEqual(vm.actual.width, 10000)
        XCTAssertEqual(vm.valueText(.width), "10000")
    }
}
