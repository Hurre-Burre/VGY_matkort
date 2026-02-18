import SwiftUI
import MatkortCore

@main
struct MatkortiOSApp: App {
    @StateObject private var vm: MatkortViewModel = {
        let base = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
        let repo = AppStateRepository(fileURL: base.appendingPathComponent("matkort/state.json"))
        return MatkortViewModel(repository: repo, sessionStore: AppSessionStore())
    }()

    var body: some Scene {
        WindowGroup {
            TabView {
                HomeView().tabItem { Label("Hem", systemImage: "house") }
                HistoryView().tabItem { Label("Historik", systemImage: "list.bullet") }
                StatsView().tabItem { Label("Statistik", systemImage: "chart.line.uptrend.xyaxis") }
                SettingsView().tabItem { Label("Inställningar", systemImage: "gearshape") }
            }
            .environmentObject(vm)
            .preferredColorScheme(vm.session.isDarkTheme ? .dark : .light)
        }
    }
}
