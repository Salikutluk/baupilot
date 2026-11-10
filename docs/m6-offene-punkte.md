# M6 · Offene Punkte

## Baseline (Step 0)

Gemessen auf `a2f1798` (Branch `feat/m6`, Worktree sauber; `feat/m6` enthält M5 bereits, es wurde nichts gemergt, D0). Befehle aus Plan §12, iOS mit `-derivedDataPath /tmp/m6-p1-dd`, Android mit JDK 21 aus Android Studio und `ANDROID_HOME=~/Library/Android/sdk`. Referenz für den Vergleich in den späteren M6-Steps. Die Werte sind die beobachteten, nicht die erwarteten.

### Zählung

| Block | Ergebnis |
|---|---|
| `cd scripts && node --test test/` | 114 Tests, 114 grün |
| `scripts/gen-tokens --check`, `scripts/gen-icons --check` | grün (Exit 0) |
| `scripts/lint-hex` | `lint-hex: sauber` |
| `scripts/design-freeze --check` | `design/ matches scripts/design-v1.sha256 (276 files)` |
| iOS `BaupilotTests` (Unit, Gallery, Metrik, Snapshots) | 368 Tests, 322 grün, **46 rot** (alle 46 `ScreenSnapshotTests`) |
| iOS `BaupilotUITests` | 32 / 32 grün (`AppNavigationUITests` 7, `EarlyTradesUITests` 5, `InspectionFlowUITests` 9, `TradeFlowUITests` 11) |
| iOS gesamt | 400 Tests, 354 grün, 46 rot (`xcodebuild` Exit 65) |
| iOS Release-Build (`generic/platform=iOS Simulator`) | `BUILD SUCCEEDED` |
| Android `:app:testDebugUnitTest` | 372 Tests, 335 grün, **37 rot** (alle 37 `*SnapshotTest`), 0 übersprungen |
| Android `:app:compileReleaseKotlin` | grün (Gradle-Gesamtlauf Exit 1 nur wegen der roten Snapshots, `--continue`) |
| `scripts/diff-report --platform ios`, `--platform android` | beide Exit 0 |
| `git status --porcelain` nach allen Läufen | leer |
| `git status --porcelain -- design fixtures scripts/icons.json` | leer |

Nicht-Snapshot-Anteil: iOS 0 rot (`MeasureViewModelTests` 8, `M5ConsistencyTests` 2, `AppNavigationTests`, `AppDataTests` grün). Android 0 rot, darunter `AppDataTest` 78, `AppNavigationStateTest` 37, `MeasureViewModelTest` 9, `M5ConsistencyTest` 4 und die Robolectric-UI-Tests `AppNavigationUiTest` 9, `TradeFlowUiTest` 13, `InspectionFlowUiTest` 9, `EarlyTradesUiTest` 5, `BaupilotRootLoadTest` 3.

### Vergleich mit „Testzahlen“ in `docs/m5-offene-punkte.md`

| Größe | Erwartet (M5-Endstand) | Beobachtet |
|---|---|---|
| scripts | 114 / 114 grün | 114 / 114 grün |
| `lint-hex` | `lint-hex: sauber` | `lint-hex: sauber` |
| `design-freeze --check` | 276 Dateien | 276 Dateien |
| iOS `BaupilotTests` | 368 Tests, 46 rot | 368 Tests, 46 rot |
| iOS `BaupilotUITests` | 32 / 32 | 32 / 32 |
| iOS Release-Build | `BUILD SUCCEEDED` | `BUILD SUCCEEDED` |
| Android `:app:testDebugUnitTest` | 372 Tests, 37 rot | 372 Tests, 37 rot |
| Android `:app:compileReleaseKotlin` | grün | grün |

### Abweichungen

Keine. Alle Zahlen entsprechen dem M5-Endstand (Plan §2: scripts 114/114, iOS 368 Unit / 46 Snapshot-Rote, UI 32/32, Android 372 Tests / 37 Snapshot-Rote).

### Untersuchte Rote

Kein Rot über die bekannten 46 iOS / 37 Android M2–M5-Snapshot-Roten hinaus; es gab nichts zu untersuchen.

iOS (46): alle in `BaupilotTests.ScreenSnapshotTests` (30 M2–M4 + 16 M5). Android (37): alle `*SnapshotTest` in `de.baupilot.snapshot` (22 M2–M4 + 15 M5). Ursachen unverändert wie in `docs/m5-offene-punkte.md` (Glyphen-Raster R1, iOS Pixelraster R19). Die Toleranzen sind unverändert.
