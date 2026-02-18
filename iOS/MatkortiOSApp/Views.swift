import SwiftUI
import UIKit
import MatkortCore

enum AppTab: Int, CaseIterable {
    case home
    case history
    case stats
    case settings
}

struct TutorialStep: Identifiable {
    let id: Int
    let tab: AppTab
    let title: String
    let body: String
}

private let tutorialSteps: [TutorialStep] = [
    .init(id: 0, tab: .home, title: "Välkommen till VGY Matkort", body: "Appen hjälper dig hålla saldo, dagsbudget och periodbudget synkat med skolans lov."),
    .init(id: 1, tab: .home, title: "Snabbval och presets", body: "Använd 50/70/90-knapparna eller egna presets för att registrera köp snabbt."),
    .init(id: 2, tab: .history, title: "Historik", body: "Här ser du alla synliga transaktioner. Dolda korrigeringar påverkar saldo men visas inte här."),
    .init(id: 3, tab: .stats, title: "Interaktiv statistik", body: "Dra fingret över grafen för att se saldo dag för dag, precis som i Android."),
    .init(id: 4, tab: .settings, title: "Inställningar", body: "Byt tema, justera saldo/periodbudget, hantera lov och starta tutorialen igen när som helst.")
]

struct AppContainerView: View {
    @EnvironmentObject private var vm: MatkortViewModel
    @State private var selectedTab: AppTab = .home
    @State private var tutorialIndex = 0
    @State private var showTutorial = false

    var body: some View {
        ZStack {
            TabView(selection: $selectedTab) {
                HomeView(showTutorial: $showTutorial)
                    .tabItem { Label("Hem", systemImage: "house") }
                    .tag(AppTab.home)

                HistoryView()
                    .tabItem { Label("Historik", systemImage: "list.bullet") }
                    .tag(AppTab.history)

                StatsView()
                    .tabItem { Label("Statistik", systemImage: "chart.line.uptrend.xyaxis") }
                    .tag(AppTab.stats)

                SettingsView(showTutorial: $showTutorial)
                    .tabItem { Label("Inställningar", systemImage: "gearshape") }
                    .tag(AppTab.settings)
            }
            .gesture(tabSwipeGesture)

            if showTutorial {
                tutorialOverlay
            }
        }
        .onAppear {
            if !vm.session.hasSeenTutorial {
                tutorialIndex = 0
                selectedTab = tutorialSteps[0].tab
                showTutorial = true
            }
        }
        .onChange(of: tutorialIndex) {
            guard tutorialIndex >= 0, tutorialIndex < tutorialSteps.count else { return }
            withAnimation { selectedTab = tutorialSteps[tutorialIndex].tab }
        }
    }

    private var tabSwipeGesture: some Gesture {
        DragGesture(minimumDistance: 25, coordinateSpace: .local)
            .onEnded { value in
                guard abs(value.translation.width) > abs(value.translation.height), !showTutorial else { return }
                if value.translation.width < -40 {
                    selectedTab = AppTab(rawValue: min((selectedTab.rawValue + 1), AppTab.allCases.count - 1)) ?? selectedTab
                } else if value.translation.width > 40 {
                    selectedTab = AppTab(rawValue: max((selectedTab.rawValue - 1), 0)) ?? selectedTab
                }
            }
    }

    private var tutorialOverlay: some View {
        let step = tutorialSteps[tutorialIndex]
        return ZStack {
            Color.black.opacity(0.5).ignoresSafeArea()
            VStack(spacing: 14) {
                Text(step.title).font(.title3.bold()).multilineTextAlignment(.center)
                Text(step.body).font(.body).multilineTextAlignment(.center)
                HStack {
                    Button(tutorialIndex == 0 ? "Hoppa över" : "Tillbaka") {
                        if tutorialIndex == 0 {
                            finishTutorial()
                        } else {
                            tutorialIndex -= 1
                        }
                    }
                    Spacer()
                    Button(tutorialIndex == tutorialSteps.count - 1 ? "Klar" : "Nästa") {
                        if tutorialIndex == tutorialSteps.count - 1 {
                            finishTutorial()
                        } else {
                            tutorialIndex += 1
                        }
                    }
                    .buttonStyle(.borderedProminent)
                }
            }
            .padding(18)
            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 16))
            .padding(.horizontal, 20)
        }
    }

    private func finishTutorial() {
        showTutorial = false
        vm.updateSession { $0.hasSeenTutorial = true }
    }
}

struct HomeView: View {
    @EnvironmentObject private var vm: MatkortViewModel
    @Binding var showTutorial: Bool
    @State private var showAddPreset = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    TabView {
                        summaryCard("Tillgängligt nu", "\(vm.uiState.currentBalance) kr", "Nuvarande saldo")
                        summaryCard("Daglig budget", "\(vm.uiState.dailyAvailable) kr", "\(vm.uiState.daysRemaining) skoldagar kvar")
                        summaryCard("Denna vecka", "\(vm.uiState.currentWeekBalance) kr", "Ackumulerat: \(vm.uiState.currentWeekAccumulated) kr")
                        summaryCard("Periodens budget", "\(vm.uiState.periodBudgetRemaining) kr", "Kvar till lovet")
                    }
                    .frame(height: 170)
                    .tabViewStyle(.page)

                    HStack(spacing: 12) { quick(50); quick(70); quick(90) }

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Presets").font(.headline)
                        ScrollView(.horizontal) {
                            HStack {
                                ForEach(vm.presets) { p in
                                    Button("\(p.label) \(p.amount) kr") {
                                        hapticTap()
                                        vm.addTransaction(p.amount)
                                    }
                                    .buttonStyle(.bordered)
                                    .contextMenu { Button("Ta bort", role: .destructive) { vm.deletePreset(p) } }
                                }
                                Button("+ Lägg till") { showAddPreset = true }
                                    .buttonStyle(.borderedProminent)
                            }
                        }
                    }
                }.padding()
            }
            .navigationTitle("VGY Matkort")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showTutorial = true
                    } label: {
                        Image(systemName: "info.circle")
                    }
                }
            }
            .sheet(isPresented: $showAddPreset) { AddPresetSheet() }
        }
    }

    private func summaryCard(_ title: String, _ value: String, _ subtitle: String) -> some View {
        VStack(spacing: 8) {
            Text(title).font(.headline)
            Text(value).font(.system(size: 36, weight: .bold))
            Text(subtitle).font(.caption).foregroundStyle(.secondary)
        }
    }

    private func quick(_ amount: Int) -> some View {
        Button("\(amount)") {
            hapticTap()
            vm.addTransaction(amount)
        }
        .buttonStyle(.borderedProminent)
        .frame(maxWidth: .infinity)
    }

    private func hapticTap() {
        guard vm.session.isHapticEnabled else { return }
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
    }
}

struct HistoryView: View {
    @EnvironmentObject private var vm: MatkortViewModel
    var body: some View {
        NavigationStack {
            List {
                let visible = vm.transactions.filter { !$0.isHidden && ($0.amount != 0 || $0.description != nil) }
                ForEach(visible) { tx in
                    HStack {
                        VStack(alignment: .leading) {
                            Text(tx.timestamp.formatted(date: .abbreviated, time: .shortened))
                            if let d = tx.description { Text(d).font(.caption).foregroundStyle(.secondary) }
                        }
                        Spacer()
                        if tx.description == nil {
                            Text(tx.amount == 0 ? "0 kr" : "-\(tx.amount) kr")
                        }
                    }
                    .swipeActions { Button("Ta bort", role: .destructive) { vm.deleteTransaction(tx) } }
                }
            }
            .navigationTitle("Historik")
        }
    }
}

struct StatsView: View {
    @EnvironmentObject private var vm: MatkortViewModel
    @State private var selectedPoint: ChartPoint?

    var body: some View {
        NavigationStack {
            List {
                Section("Översikt") {
                    LabeledContent("Nuvarande", value: "\(vm.uiState.currentBalance) kr")
                    LabeledContent("Per dag", value: "\(vm.uiState.dailyAvailable) kr")
                }

                Section("Saldoutveckling") {
                    InteractiveLineChart(points: vm.uiState.chartData, selectedPoint: $selectedPoint)
                        .frame(height: 220)
                    if let point = selectedPoint {
                        Text("\(point.date.formatted(date: .abbreviated, time: .omitted)): \(point.balance) kr")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }

                Section("Veckovis") {
                    ForEach(vm.uiState.weeklySummaries, id: \.weekNumber) { w in
                        LabeledContent("Vecka \(w.weekNumber)", value: "\(w.balance) kr")
                    }
                }
            }
            .navigationTitle("Statistik")
        }
    }
}

struct InteractiveLineChart: View {
    let points: [ChartPoint]
    @Binding var selectedPoint: ChartPoint?

    var body: some View {
        GeometryReader { geo in
            let width = geo.size.width
            let height = geo.size.height
            let minY = points.map(\.balance).min() ?? 0
            let maxY = points.map(\.balance).max() ?? 1
            let range = CGFloat(max(1, maxY - minY))

            ZStack {
                Path { path in
                    for (index, p) in points.enumerated() {
                        let x = width * CGFloat(index) / CGFloat(max(points.count - 1, 1))
                        let y = height - (CGFloat(p.balance - minY) / range) * height
                        if index == 0 { path.move(to: CGPoint(x: x, y: y)) }
                        else { path.addLine(to: CGPoint(x: x, y: y)) }
                    }
                }
                .stroke(.tint, lineWidth: 2)

                if let selectedPoint,
                   let idx = points.firstIndex(where: { $0.dayIndex == selectedPoint.dayIndex }) {
                    let x = width * CGFloat(idx) / CGFloat(max(points.count - 1, 1))
                    let y = height - (CGFloat(selectedPoint.balance - minY) / range) * height
                    Path { p in
                        p.move(to: CGPoint(x: x, y: 0))
                        p.addLine(to: CGPoint(x: x, y: height))
                    }
                    .stroke(.secondary, style: StrokeStyle(lineWidth: 1, dash: [4, 4]))
                    Circle().fill(.tint).frame(width: 10, height: 10).position(x: x, y: y)
                }
            }
            .contentShape(Rectangle())
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { value in
                        guard !points.isEmpty else { return }
                        let clampedX = min(max(0, value.location.x), width)
                        let ratio = clampedX / max(1, width)
                        let index = Int(round(ratio * CGFloat(points.count - 1)))
                        selectedPoint = points[min(max(0, index), points.count - 1)]
                    }
                    .onEnded { _ in }
            )
        }
    }
}

struct SettingsView: View {
    @EnvironmentObject private var vm: MatkortViewModel
    @Binding var showTutorial: Bool
    @State private var showHolidays = false
    @State private var showSetBalance = false
    @State private var setBalanceText = ""
    @State private var showResetConfirm = false

    var body: some View {
        NavigationStack {
            Form {
                Section("Tema") {
                    Picker("Färgtema", selection: Binding(
                        get: { vm.session.theme },
                        set: { value in vm.updateSession { $0.theme = value } }
                    )) {
                        ForEach(AppTheme.allCases, id: \.self) { theme in
                            Text(theme.rawValue).tag(theme)
                        }
                    }
                    Toggle("Mörkt tema", isOn: Binding(
                        get: { vm.session.isDarkTheme },
                        set: { value in vm.updateSession { $0.isDarkTheme = value } }
                    ))
                }
                Section("Haptik") {
                    Toggle("Haptisk feedback", isOn: Binding(
                        get: { vm.session.isHapticEnabled },
                        set: { value in vm.updateSession { $0.isHapticEnabled = value } }
                    ))
                }
                Section("Saldo") {
                    Button("Ange nuvarande saldo") {
                        setBalanceText = ""
                        showSetBalance = true
                    }
                    Button("Återställ saldo till 0 kr", role: .destructive) { showResetConfirm = true }
                    Button("Sätt periodbudget till 0") { vm.setPeriodBudgetRemaining(0) }
                }
                Section("Lov") {
                    Button("Hantera lov") { showHolidays = true }
                }
                Section("Tutorial") {
                    Button("Visa tutorial igen") {
                        vm.updateSession { $0.hasSeenTutorial = false }
                        showTutorial = true
                    }
                }
            }
            .navigationTitle("Inställningar")
            .sheet(isPresented: $showHolidays) { HolidaysView() }
            .alert("Ange nuvarande saldo", isPresented: $showSetBalance) {
                TextField("Saldo", text: $setBalanceText)
                    .keyboardType(.numberPad)
                Button("Avbryt", role: .cancel) {}
                Button("Spara") {
                    if let amount = Int(setBalanceText.trimmingCharacters(in: .whitespaces)) {
                        vm.setManualBalance(amount)
                    }
                }
            } message: {
                Text("Vill du ta reda på ditt saldo? Ring 08 681 81 37")
            }
            .alert("Återställ saldo", isPresented: $showResetConfirm) {
                Button("Avbryt", role: .cancel) {}
                Button("Återställ", role: .destructive) { vm.resetBalance() }
            } message: {
                Text("Detta lägger till en korrigeringstransaktion i historiken.")
            }
        }
    }
}

struct AddPresetSheet: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var vm: MatkortViewModel
    @State private var name = ""
    @State private var amount = ""

    var body: some View {
        NavigationStack {
            Form {
                TextField("Namn", text: $name)
                TextField("Belopp", text: $amount)
            }
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Spara") {
                        if let value = Int(amount), !name.isEmpty { vm.addPreset(amount: value, label: name); dismiss() }
                    }
                }
                ToolbarItem(placement: .cancellationAction) { Button("Avbryt") { dismiss() } }
            }
        }
    }
}

struct HolidaysView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var vm: MatkortViewModel
    @State private var name = ""
    @State private var start = Date.now
    @State private var end = Date.now
    @State private var message: String?

    var body: some View {
        NavigationStack {
            List {
                Section("Import") {
                    Button("Importera från VGY") {
                        Task {
                            let res = await vm.importHolidays()
                            switch res {
                            case .success(let count): message = count > 0 ? "Importerade \(count) lov" : "Inga nya lov att importera"
                            case .failure(let error): message = error.localizedDescription
                            }
                        }
                    }
                    if let message { Text(message).font(.caption).foregroundStyle(.secondary) }
                }
                Section("Lägg till manuellt") {
                    TextField("Namn", text: $name)
                    DatePicker("Start", selection: $start, displayedComponents: .date)
                    DatePicker("Slut", selection: $end, displayedComponents: .date)
                    Button("Spara") { vm.addHoliday(start: start, end: end, name: name); name = "" }
                }
                Section("Befintliga") {
                    ForEach(vm.holidays) { h in
                        VStack(alignment: .leading) {
                            Text(h.name)
                            Text("\(h.startDate.formatted(date: .abbreviated, time: .omitted)) - \(h.endDate.formatted(date: .abbreviated, time: .omitted))")
                                .font(.caption).foregroundStyle(.secondary)
                        }
                        .swipeActions { Button("Ta bort", role: .destructive) { vm.deleteHoliday(h) } }
                    }
                }
            }
            .navigationTitle("Hantera lov")
            .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Klar") { dismiss() } } }
        }
    }
}
