# VGY Matkort iOS Port (separat från Android)

Detta är en separat iOS-port av Android-projektet `tmp/VGY_matkort`.
Android-koden är **orörd**.

## Struktur

- `MatkortCore/` – Swift Package med portad affärslogik:
  - datamodeller (`Transaction`, `Preset`, `Holiday`)
  - period-/budgetberäkning (motsvarar `MainViewModel` + `SchoolPeriodUtils`)
  - semester/inställningslagring (`AppSessionStore`)
  - lokal state persistence (`AppStateRepository`, JSON-fil)
  - nätverksimport av lov från `https://vgy.se/lasarsdata/` (`HolidayImporter`)
- `MatkortiOSApp/` – SwiftUI-appkod (TabView: Hem/Historik/Statistik/Inställningar + lovhantering)

## Funktionalitet som portats

- Snabbtransaktioner (50/70/90)
- Presets (lägg till, använd, ta bort)
- Historik med borttagning
- Statistik med veckosammanfattning och nyckeltal
- Inställningar: dark mode, haptik-flagga, periodbudget-justering
- Lovhantering:
  - manuellt tillägg/borttagning
  - webbimport med samma regex-strategi som Android-koden
- Session/state-hantering med UserDefaults + lokal JSON

## Körning och test i Linux/container

Kärnlogik kan testas här via Swift Package:

```bash
cd /home/hugo/.openclaw/workspace/tmp/VGY_matkort_ios/MatkortCore
swift test
```

## Körning i macOS/Xcode (exakta steg)

1. Öppna Finder/Terminal till:
   - `/home/hugo/.openclaw/workspace/tmp/VGY_matkort_ios/MatkortCore`
2. Öppna paketet i Xcode:
   - `open Package.swift`
3. Lägg till iOS app-target i Xcode (File -> New -> Target -> iOS App), eller skapa nytt iOS App-projekt och dra in filerna i `MatkortiOSApp/`.
4. Se till att app-target länkar paketprodukten `MatkortCore`.
5. Sätt deployment target till iOS 17+.
6. Välj simulator (t.ex. iPhone 16) och kör.

## Kända begränsningar / blockerare

- I Linux går det inte att bygga/köra SwiftUI iOS-app (saknar Apple SDK/Xcode), därför har bara kärnlogiken verifierats här.
- Android-appen har ingen klassisk auth/login/session-token mot backend; därför finns ingen sådan API-auth att porta. "Session" i iOS-porten avser appinställningar/persistens.
- VGY-sidan är svår att extrahera robust med readability-verktyg, så importern följer Android-strategin (regex på rå HTML).

## Matchning mot Android

Portningen följer Androids centrala beteenden:
- budget 70 kr/skoldag
- holiday-aware periodberäkning
- "hidden" korrigeringstransaktioner påverkar saldo men filtreras i vissa vyer
- periodbudgetjustering genom dold korrigeringstransaktion
