import SwiftUI
import MatkortCore

struct HomeView: View {
    @EnvironmentObject private var vm: MatkortViewModel
    @State private var showAddPreset = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    VStack(spacing: 8) {
                        Text("Tillgängligt nu").font(.headline)
                        Text("\(vm.uiState.currentBalance) kr").font(.system(size: 42, weight: .bold))
                        Text("Per dag: \(vm.uiState.dailyAvailable) kr").foregroundStyle(.secondary)
                    }
                    HStack(spacing: 12) {
                        quick(50); quick(70); quick(90)
                    }
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Presets").font(.headline)
                        ScrollView(.horizontal) {
                            HStack {
                                ForEach(vm.presets) { p in
                                    Button("\(p.label) \(p.amount) kr") { vm.addTransaction(p.amount) }
                                        .buttonStyle(.bordered)
                                        .contextMenu { Button("Ta bort", role: .destructive) { vm.deletePreset(p) } }
                                }
                                Button("+ Lägg till") { showAddPreset = true }.buttonStyle(.borderedProminent)
                            }
                        }
                    }
                }.padding()
            }
            .navigationTitle("VGY Matkort")
            .sheet(isPresented: $showAddPreset) { AddPresetSheet() }
        }
    }

    private func quick(_ amount: Int) -> some View {
        Button("\(amount)") { vm.addTransaction(amount) }
            .buttonStyle(.borderedProminent)
            .frame(maxWidth: .infinity)
    }
}

struct HistoryView: View {
    @EnvironmentObject private var vm: MatkortViewModel
    var body: some View {
        NavigationStack {
            List {
                ForEach(vm.transactions.filter { !$0.isHidden && ($0.amount != 0 || $0.description != nil) }) { tx in
                    HStack {
                        VStack(alignment: .leading) {
                            Text(tx.timestamp.formatted(date: .abbreviated, time: .shortened))
                            if let d = tx.description { Text(d).font(.caption).foregroundStyle(.secondary) }
                        }
                        Spacer()
                        Text(tx.description == nil ? "-\(tx.amount) kr" : "")
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
    var body: some View {
        NavigationStack {
            List {
                Section("Översikt") {
                    LabeledContent("Nuvarande", value: "\(vm.uiState.currentBalance) kr")
                    LabeledContent("Per dag", value: "\(vm.uiState.dailyAvailable) kr")
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

struct SettingsView: View {
    @EnvironmentObject private var vm: MatkortViewModel
    @State private var showHolidays = false

    var body: some View {
        NavigationStack {
            Form {
                Section("Tema") {
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
                    Button("Sätt periodbudget till 0") { vm.setPeriodBudgetRemaining(0) }
                }
                Section("Lov") {
                    Button("Hantera lov") { showHolidays = true }
                }
            }
            .navigationTitle("Inställningar")
            .sheet(isPresented: $showHolidays) { HolidaysView() }
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
                            case .success(let count): message = "Importerade \(count) lov"
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
