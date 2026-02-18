import Foundation

public enum AppTheme: String, Codable, CaseIterable {
    case blue = "Blue"
    case green = "Green"
    case red = "Red"
    case orange = "Orange"
    case purple = "Purple"
    case pink = "Pink"
}

public struct AppSession: Codable, Equatable {
    public var isDarkTheme: Bool
    public var isHapticEnabled: Bool
    public var hasSeenTutorial: Bool
    public var theme: AppTheme

    public init(
        isDarkTheme: Bool = false,
        isHapticEnabled: Bool = true,
        hasSeenTutorial: Bool = false,
        theme: AppTheme = .blue
    ) {
        self.isDarkTheme = isDarkTheme
        self.isHapticEnabled = isHapticEnabled
        self.hasSeenTutorial = hasSeenTutorial
        self.theme = theme
    }
}

public final class AppSessionStore {
    private let defaults: UserDefaults
    private let key = "vgy_matkort_session"

    public init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    public func load() -> AppSession {
        guard let data = defaults.data(forKey: key),
              let session = try? JSONDecoder().decode(AppSession.self, from: data) else {
            return AppSession()
        }
        return session
    }

    public func save(_ session: AppSession) {
        if let data = try? JSONEncoder().encode(session) {
            defaults.set(data, forKey: key)
        }
    }
}

public struct AppStateSnapshot: Codable {
    public var transactions: [Transaction]
    public var presets: [Preset]
    public var holidays: [Holiday]

    public init(transactions: [Transaction] = [], presets: [Preset] = [], holidays: [Holiday] = []) {
        self.transactions = transactions
        self.presets = presets
        self.holidays = holidays
    }
}

public final class AppStateRepository {
    private let fileURL: URL
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    public init(fileURL: URL) {
        self.fileURL = fileURL
        encoder.dateEncodingStrategy = .millisecondsSince1970
        decoder.dateDecodingStrategy = .millisecondsSince1970
    }

    public func load() -> AppStateSnapshot {
        guard let data = try? Data(contentsOf: fileURL),
              let decoded = try? decoder.decode(AppStateSnapshot.self, from: data) else {
            return AppStateSnapshot(holidays: SchoolPeriodUtils.defaultHolidays.map {
                Holiday(startDate: $0.0.lowerBound, endDate: $0.0.upperBound, name: $0.1)
            })
        }
        return decoded
    }

    public func save(_ snapshot: AppStateSnapshot) throws {
        let dir = fileURL.deletingLastPathComponent()
        try FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        let data = try encoder.encode(snapshot)
        try data.write(to: fileURL, options: .atomic)
    }
}
