import Foundation
#if canImport(FoundationNetworking)
import FoundationNetworking
#endif

public struct ImportedHoliday: Equatable {
    public var name: String
    public var startDate: Date
    public var endDate: Date

    public init(name: String, startDate: Date, endDate: Date) {
        self.name = name
        self.startDate = startDate
        self.endDate = endDate
    }
}

public struct HolidayImporter {
    public var vgyURL = URL(string: "https://vgy.se/lasarsdata/")!
    public var calendar: Calendar

    public init(calendar: Calendar = .current) {
        self.calendar = calendar
    }

    public func fetchHolidaysFromVGY() async throws -> [ImportedHoliday] {
        let (data, _) = try await URLSession.shared.data(from: vgyURL)
        guard let html = String(data: data, encoding: .utf8) else { return [] }
        return parseHolidays(htmlContent: html)
    }

    public func parseHolidays(htmlContent: String, now: Date = .now) -> [ImportedHoliday] {
        let major = ["Höstlov", "Jullov", "Sportlov", "Påsklov"]
        let pattern = #"(\d{1,2})/(\d{1,2})\s*[–-]\s*(\d{1,2})/(\d{1,2})\s+([A-Za-zÅÄÖåäö]+)"#
        let regex = try? NSRegularExpression(pattern: pattern)
        let nsRange = NSRange(htmlContent.startIndex..<htmlContent.endIndex, in: htmlContent)
        let currentYear = calendar.component(.year, from: now)
        let currentMonth = calendar.component(.month, from: now)
        let currentDay = calendar.component(.day, from: now)

        var imported: [ImportedHoliday] = []
        regex?.matches(in: htmlContent, range: nsRange).forEach { match in
            guard match.numberOfRanges == 6,
                  let sDay = int(match.range(at: 1), in: htmlContent),
                  let sMonth = int(match.range(at: 2), in: htmlContent),
                  let eDay = int(match.range(at: 3), in: htmlContent),
                  let eMonth = int(match.range(at: 4), in: htmlContent),
                  let rawName = substring(match.range(at: 5), in: htmlContent) else { return }

            guard major.contains(where: { rawName.localizedCaseInsensitiveContains($0) }) else { return }

            var startYear = currentYear
            var endYear = currentYear
            if sMonth > eMonth { endYear += 1 }
            if currentMonth > eMonth || (currentMonth == eMonth && currentDay > eDay) {
                startYear += 1
                endYear += 1
            }

            guard let start = calendar.date(from: DateComponents(year: startYear, month: sMonth, day: sDay)),
                  let end = calendar.date(from: DateComponents(year: endYear, month: eMonth, day: eDay)) else { return }

            let name: String
            switch rawName.lowercased() {
            case let s where s.contains("höstlov"): name = "Höstlov"
            case let s where s.contains("jullov"): name = "Jullov"
            case let s where s.contains("sportlov"): name = "Sportlov"
            case let s where s.contains("påsklov"): name = "Påsklov"
            default: name = rawName
            }

            imported.append(ImportedHoliday(name: name, startDate: start, endDate: end))
        }

        return imported
    }

    private func int(_ range: NSRange, in text: String) -> Int? {
        substring(range, in: text).flatMap(Int.init)
    }

    private func substring(_ range: NSRange, in text: String) -> String? {
        guard let r = Range(range, in: text) else { return nil }
        return String(text[r])
    }
}
