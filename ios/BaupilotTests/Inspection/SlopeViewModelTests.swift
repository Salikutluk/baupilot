import XCTest
@testable import Baupilot

final class SlopeViewModelTests: XCTestCase {
    private func makeModel(lenDm: Int = 20, dropMm: Int = 25, soll: Double = 1) -> SlopeViewModel {
        SlopeViewModel(
            sollDefault: soll, sollMin: 0.5, sollMax: 3,
            lenDm: lenDm, dropMm: dropMm,
            lengthRange: .init(min: 5, max: 100, step: 5),
            dropRange: .init(min: 0, max: 300, step: 5),
            labels: .init(sufficient: "Ausreichend", tooFlat: "Zu flach", sollPrefix: "Soll ≥")
        )
    }

    func testInitialStateMatchesFixture() {
        let vm = makeModel()
        XCTAssertEqual(vm.lengthText, "2,0 m")
        XCTAssertEqual(vm.dropText, "2,5 cm")
        XCTAssertEqual(vm.percentText, "1,25 %")
        XCTAssertEqual(vm.verdictText, "Ausreichend · Soll ≥ 1,0 %")
        XCTAssertTrue(vm.isSufficient)
        XCTAssertEqual(vm.schemaY2, 24.5)
    }

    func testSteppersMoveByHalfUnits() {
        var vm = makeModel()
        vm.incrementLength()
        XCTAssertEqual(vm.lengthText, "2,5 m")
        vm.decrementDrop()
        XCTAssertEqual(vm.dropText, "2,0 cm")
        vm.decrementLength()
        vm.incrementDrop()
        XCTAssertEqual(vm.lengthText, "2,0 m")
        XCTAssertEqual(vm.dropText, "2,5 cm")
    }

    func testBoundaryIsInclusive() {
        var vm = makeModel(lenDm: 20, dropMm: 20)
        XCTAssertEqual(vm.percentText, "1,00 %")
        XCTAssertTrue(vm.isSufficient)
        vm = makeModel(lenDm: 20, dropMm: 15)
        XCTAssertEqual(vm.percentText, "0,75 %")
        XCTAssertFalse(vm.isSufficient)
        XCTAssertEqual(vm.verdictText, "Zu flach · Soll ≥ 1,0 %")
    }

    func testClampsAtRangeEnds() {
        var vm = makeModel(lenDm: 100, dropMm: 300)
        vm.incrementLength()
        vm.incrementDrop()
        XCTAssertEqual(vm.lenDm, 100)
        XCTAssertEqual(vm.dropMm, 300)
        vm = makeModel(lenDm: 5, dropMm: 0)
        vm.decrementLength()
        vm.decrementDrop()
        XCTAssertEqual(vm.lenDm, 5)
        XCTAssertEqual(vm.dropMm, 0)
        vm = makeModel(lenDm: 98, dropMm: 298)
        vm.incrementLength()
        vm.incrementDrop()
        XCTAssertEqual(vm.lenDm, 100)
        XCTAssertEqual(vm.dropMm, 300)
        vm = makeModel(lenDm: 7, dropMm: 3)
        vm.decrementLength()
        vm.decrementDrop()
        XCTAssertEqual(vm.lenDm, 5)
        XCTAssertEqual(vm.dropMm, 0)
    }

    func testSchemaY2IsClamped() {
        XCTAssertEqual(makeModel(lenDm: 100, dropMm: 0).schemaY2, 14)
        XCTAssertEqual(makeModel(lenDm: 5, dropMm: 300).schemaY2, 38)
        XCTAssertEqual(makeModel(lenDm: 30, dropMm: 25).schemaY2, 20.3)
    }

    func testPercentRoundsHalfUpLikeToFixed() {
        XCTAssertEqual(makeModel(lenDm: 40, dropMm: 45).percentText, "1,13 %")
        XCTAssertEqual(makeModel(lenDm: 40, dropMm: 15).percentText, "0,38 %")
    }

    func testTextsUseCommaWithoutGrouping() {
        let vm = makeModel(lenDm: 100, dropMm: 300)
        XCTAssertEqual(vm.lengthText, "10,0 m")
        XCTAssertEqual(vm.dropText, "30,0 cm")
        XCTAssertEqual(vm.percentText, "3,00 %")
        for text in [vm.lengthText, vm.dropText, vm.percentText, vm.verdictText] {
            XCTAssertFalse(text.contains("."), text)
        }
    }

    func testSollIsClampedToRange() {
        XCTAssertEqual(makeModel(soll: 0.2).verdictText, "Ausreichend · Soll ≥ 0,5 %")
        XCTAssertEqual(makeModel(lenDm: 100, dropMm: 300, soll: 4).verdictText, "Ausreichend · Soll ≥ 3,0 %")
        XCTAssertEqual(makeModel(soll: 4).sollTenths, 30)
        XCTAssertEqual(makeModel(soll: 0.2).sollTenths, 5)
    }

    func testVerdictUsesTypographicGlyphs() {
        let scalars = makeModel().verdictText.unicodeScalars.map(\.value)
        XCTAssertTrue(scalars.contains(0x2265))
        XCTAssertTrue(scalars.contains(0x00B7))
    }
}
