# VGY Matkort iOS Port

Detta är iOS-porten av Android-projektet i samma repo (`tmp/VGY_matkort`).
Android-koden är orörd; allt arbete ligger under `iOS/`.

## Paritetschecklista mot Android (feature-by-feature)

### Kärnlogik
- [x] 70 kr/skoldag med holiday-aware periodberäkning
- [x] Beräkning från dagen efter senaste avslutade lov (samma princip som Android)
- [x] Nuvarande saldo, daglig tillgänglig budget, återstående skoldagar
- [x] Veckosammanfattningar (nyast först)
- [x] Chart-data dag-för-dag
- [x] Hidden transaktioner påverkar saldo men filtreras i relevanta vyer
- [x] Periodbudget-justering via dold korrigeringstransaktion
- [x] Manuell saldosättning via dold korrigeringstransaktion
- [x] Återställ saldo skapar synlig transaktion (`Återställt saldo`)
- [x] Periodnamn-format uppdaterat till Android-format (`Until Holiday (Month Day)` / `Until Semester End`)

### Data/migrering
- [x] Lokal state persistence (JSON)
- [x] Default holidays seeded vid tom state
- [x] Migrering: generiska `Holiday`-namn döps om när datum matchar default
- [x] Skydd: säkerställer att `Jullov` finns om det saknas (som Android-init)

### Lovhantering
- [x] Manuell tillägg/borttagning av lov
- [x] Import från `https://vgy.se/lasarsdata/`
- [x] Dublettskydd vid import (match på startdatum)

### UI/UX
- [x] Flikar: Hem/Historik/Statistik/Inställningar
- [x] Snabbval 50/70/90
- [x] Presets (lägg till/använd/ta bort)
- [x] Historik med borttagning och filtrering av hidden
- [x] Inställningar: mörkt tema + haptik-flaggor
- [x] Inställningar: tema-val (Blue/Green/Red/Orange/Purple/Pink)
- [x] Inställningar: manuell saldosättning + återställning + periodbudget till 0

### Ej full parity ännu (dokumenterade gap)
- [ ] Androids fulla visuella designparitet (gradienter, kortstil, exakt spacing, haptic på varje interaktion)
- [ ] Interaktiv statistikgraf med touch-indikator/tooltip som i Android
- [ ] Androids tutorial-overlay med highlight-register + steg-för-steg navigation
- [ ] Androids sid-navigering med horizontal pager + tutorial auto-routing
- [ ] Androids custom dialogs (keypad/date wizard) är förenklade i iOS-versionen

## Tester (paritetskritisk logik)
- `testHolidayReducesBudget`
- `testHiddenTransactionAffectsBalanceNotWeeklySpent`
- `testHolidayImporterParsesCommonRange`
- `testCurrentPeriodNameMatchesAndroidFormat`
- `testRepositoryMigratesGenericHolidayNamesAndEnsuresJullov`

## Kör tester

```bash
cd /home/hugo/.openclaw/workspace/tmp/VGY_matkort/iOS/MatkortCore
swift test
```

## Kända plattformsbegränsningar
- Linux/container kan köra Swift Package-tester men inte bygga/köra SwiftUI iOS-app (Xcode/Apple SDK krävs).
- Exakt visuell parity behöver verifieras och fintrimmas i Xcode på macOS-enhet/simulator.
