import XCTest
@testable import MatkortCore

final class MatkortCoreTests: XCTestCase {
    private var calendar: Calendar {
        var c = Calendar(identifier: .gregorian)
        c.timeZone = TimeZone(secondsFromGMT: 0)!
        return c
    }

    func date(_ y: Int, _ m: Int, _ d: Int) -> Date {
        calendar.date(from: DateComponents(year: y, month: m, day: d))!
    }

    func testHolidayReducesBudget() {
        let engine = MatkortEngine(calendar: calendar)
        let now = date(2025, 11, 19)
        let tx: [Transaction] = []

        let withoutHoliday = engine.computeUIState(transactions: tx, holidays: [], now: now)
        let withHoliday = engine.computeUIState(
            transactions: tx,
            holidays: [Holiday(startDate: date(2025, 11, 18), endDate: date(2025, 11, 18), name: "Test")],
            now: now
        )

        XCTAssertEqual(withoutHoliday.currentBalance - 70, withHoliday.currentBalance)
    }

    func testHiddenTransactionAffectsBalanceNotWeeklySpent() {
        let engine = MatkortEngine(calendar: calendar)
        let now = date(2026, 2, 18)
        let tx = [
            Transaction(amount: 100, timestamp: date(2026, 2, 18), isHidden: false),
            Transaction(amount: 50, timestamp: date(2026, 2, 18), isHidden: true)
        ]
        let state = engine.computeUIState(transactions: tx, holidays: [], now: now)
        XCTAssertEqual(state.currentWeekSpent, 100)
    }

    func testHolidayImporterParsesCommonRange() {
        let html = "<p>27/10 – 31/10 Höstlov</p><p>22/12 - 7/1 Jullov</p>"
        let importer = HolidayImporter(calendar: calendar)
        let parsed = importer.parseHolidays(htmlContent: html, now: date(2025, 8, 1))
        XCTAssertEqual(parsed.count, 2)
        XCTAssertEqual(parsed.first?.name, "Höstlov")
    }
}
