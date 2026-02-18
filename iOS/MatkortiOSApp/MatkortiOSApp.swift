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
            AppContainerView()
                .environmentObject(vm)
                .tint(accentColor(for: vm.session.theme))
                .preferredColorScheme(vm.session.isDarkTheme ? .dark : .light)
        }
    }
}
