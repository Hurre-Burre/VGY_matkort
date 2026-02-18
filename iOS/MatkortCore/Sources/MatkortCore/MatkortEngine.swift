import Foundation

public struct MatkortEngine {
    public var calendar: Calendar

    public init(calendar: Calendar = .current) {
        self.calendar = calendar
    }

    public func computeUIState(transactions: [Transaction], holidays: [Holiday], now: Date = .now) -> UIState {
        var state = UIState()
        let dayNow = calendar.startOfDay(for: now)
        let holidayRanges = holidays.map { calendar.startOfDay(for: $0.startDate)...calendar.startOfDay(for: $0.endDate) }
        guard let period = SchoolPeriodUtils.getCurrentPeriod(date: dayNow, holidays: holidayRanges, calendar: calendar) else {
            return state
        }

        let lastHolidayEnd = holidayRanges.filter { $0.upperBound < dayNow }.map(\ .upperBound).max()
        let calculationStart = lastHolidayEnd.flatMap { calendar.date(byAdding: .day, value: 1, to: $0) } ?? period.start

        var accumulatedBudget = 0
        var d = calculationStart
        while d <= min(dayNow, period.end) {
            if SchoolPeriodUtils.isSchoolDay(d, holidays: holidayRanges, calendar: calendar) {
                accumulatedBudget += SchoolPeriodUtils.dailyBudget
            }
            d = calendar.date(byAdding: .day, value: 1, to: d)!
        }

        let periodTransactions = transactions.filter {
            let txDate = calendar.startOfDay(for: $0.timestamp)
            return txDate >= calculationStart && txDate <= period.end
        }
        let visible = periodTransactions.filter { !$0.isHidden }
        let spent = periodTransactions.reduce(0) { $0 + $1.amount }
        let currentBalance = accumulatedBudget - spent

        state.currentBalance = currentBalance
        state.initialBalance = accumulatedBudget
        state.periodName = period.name
        state.periodEnd = period.end

        state.weeklySummaries = weeklySummaries(from: calculationStart, to: min(dayNow, period.end), visibleTransactions: visible, holidays: holidayRanges)

        var totalPeriodBudget = 0
        d = calculationStart
        while d <= period.end {
            if SchoolPeriodUtils.isSchoolDay(d, holidays: holidayRanges, calendar: calendar) {
                totalPeriodBudget += SchoolPeriodUtils.dailyBudget
            }
            d = calendar.date(byAdding: .day, value: 1, to: d)!
        }

        state.totalPeriodBudget = totalPeriodBudget
        state.periodBudgetRemaining = totalPeriodBudget - spent
        state.daysRemaining = SchoolPeriodUtils.getRemainingSchoolDays(period: period, holidays: holidayRanges, currentDate: dayNow, calendar: calendar)
        state.dailyAvailable = SchoolPeriodUtils.getDailyAvailable(currentBalance: currentBalance, period: period, holidays: holidayRanges, currentDate: dayNow, calendar: calendar)

        let weekStart = max(startOfWeek(dayNow), calculationStart)
        state.currentWeekAccumulated = countSchoolDays(from: weekStart, to: dayNow, holidays: holidayRanges) * SchoolPeriodUtils.dailyBudget
        state.currentWeekSpent = visible.filter {
            let txDate = calendar.startOfDay(for: $0.timestamp)
            return txDate >= weekStart && txDate <= dayNow
        }.reduce(0) { $0 + $1.amount }
        state.currentWeekBalance = state.currentWeekAccumulated - state.currentWeekSpent

        state.chartData = chartData(from: calculationStart, to: min(dayNow, period.end), transactions: periodTransactions, holidays: holidayRanges)

        return state
    }

    private func weeklySummaries(from start: Date, to end: Date, visibleTransactions: [Transaction], holidays: [ClosedRange<Date>]) -> [WeekSummary] {
        guard start <= end else { return [] }
        var results: [WeekSummary] = []
        var cursor = start
        while calendar.component(.weekday, from: cursor) != 1 && cursor <= end { // Sunday=1
            cursor = calendar.date(byAdding: .day, value: 1, to: cursor)!
        }

        while cursor <= end {
            let budget = countSchoolDays(from: start, to: cursor, holidays: holidays) * SchoolPeriodUtils.dailyBudget
            let spent = visibleTransactions.filter { calendar.startOfDay(for: $0.timestamp) <= cursor }.reduce(0) { $0 + $1.amount }
            let week = calendar.component(.weekOfYear, from: cursor)
            results.append(WeekSummary(weekNumber: week, balance: budget - spent))
            cursor = calendar.date(byAdding: .day, value: 7, to: cursor)!
        }

        return results.reversed()
    }

    private func chartData(from start: Date, to end: Date, transactions: [Transaction], holidays: [ClosedRange<Date>]) -> [ChartPoint] {
        guard start <= end else { return [] }
        var result: [ChartPoint] = []
        var cursor = start
        var idx = 0
        while cursor <= end {
            let budget = countSchoolDays(from: start, to: cursor, holidays: holidays) * SchoolPeriodUtils.dailyBudget
            let spent = transactions.filter { calendar.startOfDay(for: $0.timestamp) <= cursor }.reduce(0) { $0 + $1.amount }
            result.append(ChartPoint(dayIndex: idx, balance: budget - spent, date: cursor))
            idx += 1
            cursor = calendar.date(byAdding: .day, value: 1, to: cursor)!
        }
        return result
    }

    private func countSchoolDays(from start: Date, to end: Date, holidays: [ClosedRange<Date>]) -> Int {
        var count = 0
        var d = start
        while d <= end {
            if SchoolPeriodUtils.isSchoolDay(d, holidays: holidays, calendar: calendar) { count += 1 }
            d = calendar.date(byAdding: .day, value: 1, to: d)!
        }
        return count
    }

    private func startOfWeek(_ date: Date) -> Date {
        let components = calendar.dateComponents([.yearForWeekOfYear, .weekOfYear], from: date)
        return calendar.date(from: components) ?? date
    }
}
