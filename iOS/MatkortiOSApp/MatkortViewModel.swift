import Foundation
import MatkortCore

@MainActor
final class MatkortViewModel: ObservableObject {
    @Published private(set) var transactions: [Transaction] = []
    @Published private(set) var presets: [Preset] = []
    @Published private(set) var holidays: [Holiday] = []
    @Published private(set) var uiState = UIState()
    @Published var session: AppSession

    private let repository: AppStateRepository
    private let sessionStore: AppSessionStore
    private let engine = MatkortEngine()

    init(repository: AppStateRepository, sessionStore: AppSessionStore) {
        self.repository = repository
        self.sessionStore = sessionStore
        self.session = sessionStore.load()
        let snapshot = repository.load()
        self.transactions = snapshot.transactions.sorted { $0.timestamp > $1.timestamp }
        self.presets = snapshot.presets
        self.holidays = snapshot.holidays.sorted { $0.startDate < $1.startDate }
        refresh()
    }

    func addTransaction(_ amount: Int) {
        transactions.insert(Transaction(amount: amount), at: 0)
        persistAndRefresh()
    }

    func deleteTransaction(_ tx: Transaction) {
        transactions.removeAll { $0.id == tx.id }
        persistAndRefresh()
    }

    func addPreset(amount: Int, label: String) {
        presets.append(Preset(amount: amount, label: label))
        persistAndRefresh()
    }

    func deletePreset(_ preset: Preset) {
        presets.removeAll { $0.id == preset.id }
        persistAndRefresh()
    }

    func setPeriodBudgetRemaining(_ target: Int) {
        let correction = uiState.periodBudgetRemaining - target
        guard correction != 0 else { return }
        transactions.insert(Transaction(amount: correction, isHidden: true, description: "Periodbudget justering"), at: 0)
        persistAndRefresh()
    }

    func addHoliday(start: Date, end: Date, name: String) {
        holidays.append(Holiday(startDate: min(start, end), endDate: max(start, end), name: name))
        holidays.sort { $0.startDate < $1.startDate }
        persistAndRefresh()
    }

    func deleteHoliday(_ holiday: Holiday) {
        holidays.removeAll { $0.id == holiday.id }
        persistAndRefresh()
    }

    func importHolidays() async -> Result<Int, Error> {
        do {
            let imported = try await HolidayImporter().fetchHolidaysFromVGY()
            var count = 0
            for h in imported {
                let exists = holidays.contains { Calendar.current.isDate($0.startDate, inSameDayAs: h.startDate) }
                if !exists {
                    holidays.append(Holiday(startDate: h.startDate, endDate: h.endDate, name: h.name))
                    count += 1
                }
            }
            holidays.sort { $0.startDate < $1.startDate }
            persistAndRefresh()
            return .success(count)
        } catch {
            return .failure(error)
        }
    }

    func updateSession(_ update: (inout AppSession) -> Void) {
        var s = session
        update(&s)
        session = s
        sessionStore.save(s)
    }

    private func refresh() {
        uiState = engine.computeUIState(transactions: transactions, holidays: holidays)
    }

    private func persistAndRefresh() {
        try? repository.save(AppStateSnapshot(transactions: transactions, presets: presets, holidays: holidays))
        refresh()
    }
}
