import XCTest
@testable import Baupilot

final class FlatnessViewModelTests: XCTestCase {
    private func makeModel(limit: Int = 4, gap: Int = 3, points: [Int] = [2, 3, 6]) -> FlatnessViewModel {
        FlatnessViewModel(
            limitDefault: limit, limitMin: 1, limitMax: 15,
            gap: gap, points: points,
            gapMin: 0, gapMax: 20, gapStep: 1,
            labels: .init(
                within: "Innerhalb der Toleranz",
                outside: "Über Grenzwert – als Mangel erfassen",
                none: "Kein Messpunkt über Grenzwert",
                point: "Messpunkt",
                pluralSuffix: "e",
                overLimit: "über Grenzwert → Mangel erfassen"
            )
        )
    }

    func testInitialStateMatchesFixture() {
        let vm = makeModel()
        XCTAssertEqual(vm.gapText, "3 mm")
        XCTAssertEqual(vm.limitText, "4 mm")
        XCTAssertTrue(vm.isWithin)
        XCTAssertEqual(vm.resultText, "Innerhalb der Toleranz")
        XCTAssertEqual(vm.pointText(0), "M1 · 2 mm")
        XCTAssertEqual(vm.pointText(1), "M2 · 3 mm")
        XCTAssertEqual(vm.pointText(2), "M3 · 6 mm")
        XCTAssertEqual(vm.overCount, 1)
        XCTAssertEqual(vm.summaryText, "1 Messpunkt über Grenzwert → Mangel erfassen")
        XCTAssertEqual(vm.nextIndex, 4)
    }

    func testWithinIsInclusive() {
        var vm = makeModel(gap: 3)
        vm.increment()
        XCTAssertEqual(vm.gap, 4)
        XCTAssertTrue(vm.isWithin)
        vm.increment()
        XCTAssertEqual(vm.gap, 5)
        XCTAssertFalse(vm.isWithin)
        XCTAssertEqual(vm.resultText, "Über Grenzwert – als Mangel erfassen")
    }

    func testPointOverIsStrict() {
        let vm = makeModel(points: [4, 5])
        XCTAssertFalse(vm.isOver(0))
        XCTAssertTrue(vm.isOver(1))
        XCTAssertEqual(vm.overCount, 1)
    }

    func testSaveAppendsWithoutReset() {
        var vm = makeModel()
        vm.increment()
        vm.increment()
        vm.save()
        XCTAssertEqual(vm.points, [2, 3, 6, 5])
        XCTAssertEqual(vm.pointText(3), "M4 · 5 mm")
        XCTAssertEqual(vm.overCount, 2)
        XCTAssertEqual(vm.summaryText, "2 Messpunkte über Grenzwert → Mangel erfassen")
        XCTAssertEqual(vm.gap, 5)
        XCTAssertEqual(vm.nextIndex, 5)
    }

    func testSummaryWithNoneOver() {
        let vm = makeModel(points: [1, 2, 4])
        XCTAssertEqual(vm.overCount, 0)
        XCTAssertEqual(vm.summaryText, "Kein Messpunkt über Grenzwert")
    }

    func testClampsGap() {
        var vm = makeModel(gap: 0)
        vm.decrement()
        XCTAssertEqual(vm.gap, 0)
        vm = makeModel(gap: 20)
        vm.increment()
        XCTAssertEqual(vm.gap, 20)
        vm.decrement()
        XCTAssertEqual(vm.gap, 19)
    }

    func testLimitIsClampedToRange() {
        XCTAssertEqual(makeModel(limit: 0).limit, 1)
        XCTAssertEqual(makeModel(limit: 16).limit, 15)
        XCTAssertEqual(makeModel(limit: 16).limitText, "15 mm")
    }

    func testSketchLineHeight() {
        XCTAssertEqual(makeModel(gap: 3).sketchLineHeight, 8)
        XCTAssertEqual(makeModel(gap: 12).sketchLineHeight, 26)
        XCTAssertEqual(makeModel(gap: 20).sketchLineHeight, 26)
    }

    func testChipTextIsOneString() {
        let text = makeModel().pointText(0)
        XCTAssertEqual(text.unicodeScalars.map(\.value), Array("M1 \u{00B7} 2 mm".unicodeScalars.map(\.value)))
        XCTAssertTrue(text.contains(" \u{00B7} "))
    }
}
