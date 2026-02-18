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

    func testCurrentPeriodNameMatchesAndroidFormat() {
        let holidays: [ClosedRange<Date>] = [date(2026, 2, 23)...date(2026, 2, 27)]
        let period = SchoolPeriodUtils.getCurrentPeriod(date: date(2026, 2, 18), holidays: holidays, calendar: calendar)
        XCTAssertEqual(period?.name, "Until Holiday (February 22)")
    }

    func testRepositoryMigratesGenericHolidayNamesAndEnsuresJullov() throws {
        let tempURL = FileManager.default.temporaryDirectory.appendingPathComponent("matkort-test-\(UUID().uuidString).json")
        let repo = AppStateRepository(fileURL: tempURL)

        // Generic holiday that should be renamed + no Jullov in file.
        let snapshot = AppStateSnapshot(
            transactions: [],
            presets: [],
            holidays: [Holiday(startDate: date(2025, 10, 27), endDate: date(2025, 10, 31), name: "Holiday")]
        )
        try repo.save(snapshot)

        let loaded = repo.load()
        XCTAssertTrue(loaded.holidays.contains(where: { $0.name == "Höstlov v.44" }))
        XCTAssertTrue(loaded.holidays.contains(where: { $0.name == "Jullov" }))
    }

    func testWeeklySummariesNewestFirst() {
        let engine = MatkortEngine(calendar: calendar)
        let now = date(2026, 2, 18)
        let state = engine.computeUIState(transactions: [], holidays: [], now: now)

        XCTAssertGreaterThanOrEqual(state.weeklySummaries.count, 1)
        if state.weeklySummaries.count > 1 {
            XCTAssertGreaterThanOrEqual(state.weeklySummaries[0].weekNumber, state.weeklySummaries[1].weekNumber)
        }
    }

    func testDailyAvailableAccountsForFutureIncomeParity() {
        let period = SchoolPeriod(name: "Test", start: date(2026, 2, 16), end: date(2026, 2, 20))
        // Monday with no holidays: remainingDays = 5, futureDays = 4.
        let daily = SchoolPeriodUtils.getDailyAvailable(
            currentBalance: 0,
            period: period,
            holidays: [],
            currentDate: date(2026, 2, 16),
            calendar: calendar
        )

        XCTAssertEqual(daily, (4 * 70) / 5)
    }
}
