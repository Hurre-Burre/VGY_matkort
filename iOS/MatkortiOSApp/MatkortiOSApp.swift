import SwiftUI
import MatkortCore

private func accentColor(for theme: AppTheme) -> Color {
    switch theme {
    case .blue: return .blue
    case .green: return .green
    case .red: return .red
    case .orange: return .orange
    case .purple: return .purple
    case .pink: return .pink
    }
}

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
            .tint(accentColor(for: vm.session.theme))
            .preferredColorScheme(vm.session.isDarkTheme ? .dark : .light)
        }
    }
}
