# M5 · Offene Punkte

## Baseline (Step 0)

Gemessen auf `8a72c08` (Branch `feat/m5`, per `git merge --ff-only feat/m4` von `1cd6058` vorgezogen, Worktree sauber). Befehle aus Plan §12, iOS mit `-derivedDataPath /tmp/m5-p1-dd`, Android mit JDK 21 aus Android Studio. Referenz für den Vergleich in den späteren M5-Steps. Die Werte sind die beobachteten, nicht die erwarteten.

### Zählung

| Block | Ergebnis |
|---|---|
| `cd scripts && node --test test/` | 114 Tests, 113 grün, **1 rot** (`repo tree is clean`, Ursache siehe „Abweichungen“) |
| `scripts/gen-tokens --check`, `scripts/gen-icons --check` | grün (Exit 0) |
| `scripts/lint-hex` | **1 Fund** (Exit 1), siehe „Abweichungen“ |
| `scripts/design-freeze --check` | `design/ matches scripts/design-v1.sha256 (276 files)` |
| iOS `BaupilotTests` (Unit, Gallery, Metrik, Snapshots) | 280 Tests, 250 grün, **30 rot** (alle 30 `ScreenSnapshotTests`) |
| iOS `BaupilotUITests` | 27 / 27 grün |
| iOS gesamt | 307 Tests, 277 grün, 30 rot (`xcodebuild` Exit 65) |
| Android `:app:testDebugUnitTest` | 291 Tests, 269 grün, **22 rot** (alle `*SnapshotTest`) |
| Android `:app:compileReleaseKotlin` | grün (`--continue`; Gesamtlauf Exit 1 wegen der roten Snapshots) |
| `git status --porcelain` nach allen Läufen | leer |

### Vergleich mit M4 („Verifikation (Step 14)“ in `docs/m4-offene-punkte.md`)

| Größe | Erwartet (M4-Endstand) | Beobachtet |
|---|---|---|
| scripts | 114 / 114 grün | 113 / 114 grün (1 rot, s. u.) |
| iOS Snapshot-Rote | 30 (18 M2/M3 + 12 M4) | 30, alle in `ScreenSnapshotTests` |
| iOS Nicht-Snapshot-Rote | 0 | 0 |
| iOS `BaupilotUITests` | 27 grün | 27 grün |
| Android Snapshot-Rote | 22 | 22, alle `*SnapshotTest` |
| Android Nicht-Snapshot-Rote | 0 | 0 |
| `lint-hex` | `lint-hex: sauber` | 1 Fund |

### Abweichungen

1. **`lint-hex` meldet 1 Fund, und `scripts` hat dadurch 1 roten Test.** Fund: `ios/BaupilotTests/DesignSystem/ComponentMetricsInspectionTests.swift:83:38 named-color Color.gray` (`.scene(AnyView(Color.gray), …)` in `testCheckedSceneTilePlacesTheBadgeAt6FromTheTopRight`). Der rote Test ist `repo tree is clean` in `scripts/test/lint-hex.test.mjs`; er schlägt aus genau diesem einen Fund fehl (`actual: [ { …, rule: 'named-color', match: 'Color.gray' } ]`, `expected: []`). Ursache: Die Zeile kam im letzten M4-Commit `8a72c08` in den Baum (`git log -S'Color.gray'` nennt nur diesen Commit), nach den in `docs/m4-offene-punkte.md` festgehaltenen Läufen (dort „114 / 114“ und „`lint-hex: sauber`“). Beide Rote sind dieselbe Ursache, kein zweiter Fehler. Nicht behoben: Der Baseline-Task darf keinen Code ändern; die Korrektur (Farbe über ein Token oder die Test-Ausnahme von `lint-hex`) gehört in einen späteren M5-Step.
2. **Gesamtzahlen höher als im M4-Bericht.** iOS `BaupilotTests` 280 statt 277 Tests, Android 291 statt 286 Tests. Die Menge der roten Tests entspricht dem Bericht (30 bzw. 22, ausschließlich Snapshots), Nicht-Snapshot-Tests sind alle grün. Die Mehrzahl an Tests ist nicht auf einen Commit zurückgeführt; sie wird nicht als Fehler gewertet, weil kein Test mehr rot ist als erwartet.

### Rote Snapshot-Tests

iOS (30, Schwelle: Übereinstimmung ≥ 0.995): alle in `BaupilotTests.ScreenSnapshotTests` (18 M2/M3, 12 M4). Android (22, Schwelle: maxPercentDifference 0.5): alle `*SnapshotTest` (10 M2/M3, 12 M4). Ursachen unverändert wie in `docs/m4-offene-punkte.md` (Glyphen-Raster R1, Roboto-Glyphe R2).
