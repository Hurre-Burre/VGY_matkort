import Foundation

public struct Transaction: Codable, Identifiable, Equatable {
    public let id: UUID
    public var amount: Int
    public var timestamp: Date
    public var isHidden: Bool
    public var description: String?

    public init(
        id: UUID = UUID(),
        amount: Int,
        timestamp: Date = .now,
        isHidden: Bool = false,
        description: String? = nil
    ) {
        self.id = id
        self.amount = amount
        self.timestamp = timestamp
        self.isHidden = isHidden
        self.description = description
    }
}

public struct Preset: Codable, Identifiable, Equatable {
    public let id: UUID
    public var amount: Int
    public var label: String

    public init(id: UUID = UUID(), amount: Int, label: String) {
        self.id = id
        self.amount = amount
        self.label = label
    }
}

public struct Holiday: Codable, Identifiable, Equatable {
    public let id: UUID
    public var startDate: Date
    public var endDate: Date
    public var name: String

    public init(id: UUID = UUID(), startDate: Date, endDate: Date, name: String) {
        self.id = id
        self.startDate = startDate
        self.endDate = endDate
        self.name = name
    }
}

public struct SchoolPeriod: Equatable {
    public var name: String
    public var start: Date
    public var end: Date

    public init(name: String, start: Date, end: Date) {
        self.name = name
        self.start = start
        self.end = end
    }
}

public struct WeekSummary: Equatable {
    public var weekNumber: Int
    public var balance: Int

    public init(weekNumber: Int, balance: Int) {
        self.weekNumber = weekNumber
        self.balance = balance
    }
}

public struct ChartPoint: Equatable {
    public var dayIndex: Int
    public var balance: Int
    public var date: Date

    public init(dayIndex: Int, balance: Int, date: Date) {
        self.dayIndex = dayIndex
        self.balance = balance
        self.date = date
    }
}

public struct UIState: Equatable {
    public var currentBalance: Int = 0
    public var initialBalance: Int = 0
    public var periodName: String = ""
    public var periodEnd: Date?
    public var weeklySummaries: [WeekSummary] = []
    public var totalPeriodBudget: Int = 0
    public var periodBudgetRemaining: Int = 0
    public var daysRemaining: Int = 0
    public var dailyAvailable: Int = 0
    public var chartData: [ChartPoint] = []
    public var currentWeekBalance: Int = 0
    public var currentWeekSpent: Int = 0
    public var currentWeekAccumulated: Int = 0

    public init() {}
}
