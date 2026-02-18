import Foundation

public enum SchoolPeriodUtils {
    public static let dailyBudget = 70

    public static let defaultHolidays: [(ClosedRange<Date>, String)] = {
        let c = Calendar(identifier: .gregorian)
        func d(_ y: Int, _ m: Int, _ day: Int) -> Date { c.date(from: DateComponents(year: y, month: m, day: day))! }
        return [
            (d(2024, 10, 28)...d(2024, 11, 1), "Höstlov v.44"),
            (d(2024, 12, 23)...d(2025, 1, 7), "Jullov"),
            (d(2025, 2, 17)...d(2025, 2, 21), "Sportlov v.8"),
            (d(2025, 4, 14)...d(2025, 4, 17), "Påsklov"),
            (d(2025, 10, 27)...d(2025, 10, 31), "Höstlov v.44"),
            (d(2025, 12, 23)...d(2026, 1, 7), "Jullov"),
            (d(2026, 2, 23)...d(2026, 2, 27), "Sportlov v.9"),
            (d(2026, 4, 3)...d(2026, 4, 10), "Påsklov"),
            (d(2026, 5, 14)...d(2026, 5, 15), "Lov (Kristi Himmelsfärd)")
        ]
    }()

    public static func getCurrentPeriod(date: Date = .now, holidays: [ClosedRange<Date>] = [], calendar: Calendar = .current) -> SchoolPeriod? {
        let semesterStart = getSemesterStart(for: date, calendar: calendar)
        let semesterEnd = getSemesterEnd(for: date, calendar: calendar)
        let searchStartDate = date < semesterStart ? semesterStart : date
        let nextHoliday = holidays
            .filter { $0.lowerBound >= searchStartDate && $0.lowerBound <= semesterEnd }
            .min { $0.lowerBound < $1.lowerBound }

        let periodEnd = nextHoliday?.lowerBound.addingTimeInterval(-86_400) ?? semesterEnd
        let title: String = nextHoliday == nil ? "Until Semester End" : "Until Holiday"
        return SchoolPeriod(name: title, start: semesterStart, end: periodEnd)
    }

    public static func isSchoolDay(_ date: Date, holidays: [ClosedRange<Date>], calendar: Calendar = .current) -> Bool {
        let weekday = calendar.component(.weekday, from: date)
        if weekday == 1 || weekday == 7 { return false }
        return !holidays.contains { $0.contains(date) }
    }

    public static func getRemainingSchoolDays(period: SchoolPeriod, holidays: [ClosedRange<Date>], currentDate: Date = .now, calendar: Calendar = .current) -> Int {
        if currentDate > period.end { return 0 }
        var days = 0
        var date = max(startOfDay(currentDate, calendar), startOfDay(period.start, calendar))
        while date <= period.end {
            if isSchoolDay(date, holidays: holidays, calendar: calendar) { days += 1 }
            date = calendar.date(byAdding: .day, value: 1, to: date)!
        }
        return days
    }

    public static func getDailyAvailable(currentBalance: Int, period: SchoolPeriod, holidays: [ClosedRange<Date>], currentDate: Date = .now, calendar: Calendar = .current) -> Int {
        let remainingDays = getRemainingSchoolDays(period: period, holidays: holidays, currentDate: currentDate, calendar: calendar)
        if remainingDays == 0 { return 0 }
        let tomorrow = calendar.date(byAdding: .day, value: 1, to: currentDate)!
        let futureDays = getRemainingSchoolDays(period: period, holidays: holidays, currentDate: tomorrow, calendar: calendar)
        let futureIncome = futureDays * dailyBudget
        return (currentBalance + futureIncome) / remainingDays
    }

    private static func getSemesterStart(for date: Date, calendar: Calendar) -> Date {
        let y = calendar.component(.year, from: date)
        let m = calendar.component(.month, from: date)
        return calendar.date(from: DateComponents(year: y, month: m >= 7 ? 8 : 1, day: m >= 7 ? 19 : 8))!
    }

    private static func getSemesterEnd(for date: Date, calendar: Calendar) -> Date {
        let y = calendar.component(.year, from: date)
        let m = calendar.component(.month, from: date)
        return calendar.date(from: DateComponents(year: y, month: m >= 7 ? 12 : 6, day: m >= 7 ? 22 : 12))!
    }

    static func startOfDay(_ date: Date, _ calendar: Calendar) -> Date {
        calendar.startOfDay(for: date)
    }
}
