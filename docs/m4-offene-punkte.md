# M4 · Offene Punkte

## Baseline (Step 0)

Gemessen auf `1cd6058` (Branch `feat/m4`, Worktree sauber), Befehle aus Plan §12, iOS mit `-derivedDataPath /tmp/m4-FULL-dd`, Android mit JDK 21 aus Android Studio. Referenz für den Vergleich in Step 14.

### Zählung

| Block | Ergebnis |
|---|---|
| `node --test test/` (scripts) | 114 / 114 grün |
| `gen-tokens --check`, `gen-icons --check`, `lint-hex`, `design-freeze --check` | alle grün (design-freeze: 276 Dateien) |
| iOS `BaupilotTests` (Unit, Gallery, Metrik, Snapshots) | 170 Tests, 152 grün, **18 rot** (alle 18 `ScreenSnapshotTests`) |
| iOS `BaupilotUITests` | 18 / 18 grün |
| iOS gesamt | 188 Tests, 170 grün, 18 rot (xcodebuild Exit 65) |
| Android `:app:testDebugUnitTest` | 195 Tests, 185 grün, **10 rot** (alle `*SnapshotTest`) |
| Android `:app:compileReleaseKotlin` | grün |
| `git status --porcelain` nach allen Läufen | leer |

Erwartung des Plans (iOS 18 / Android 10 Snapshot-Rote, sonst grün) ist exakt eingetreten. Kein Rot ohne Snapshot-Bezug.

### Rote Snapshot-Tests

iOS (18, Schwelle: Übereinstimmung ≥ 0.995): `testDokumenteDark/Light`, `testDokViewerDark/Light`, `testGewerkDark/Light`, `testHomeDark/Light`, `testLoginDark/Light`, `testMitteilungenDark/Light`, `testProfilDark/Light`, `testVergleichDark/Light`, `testZeitplanDark/Light`.

Android (10, Schwelle: maxPercentDifference 0.5): `DokumenteDark`, `DokumenteLight`, `HomeLight`, `MitteilungenDark`, `MitteilungenLight`, `ProfilDark`, `ProfilLight`, `VergleichDark`, `VergleichLight`, `ZeitplanLight`.

Grün auf Android: DokViewer (hell/dunkel), Gewerk (hell/dunkel), Home dunkel, Login (hell/dunkel), Zeitplan dunkel.

### Step-0-Tabelle (Vergleichsbasis für Step 14)

- iOS „Match" = vom Test gemeldeter Anteil übereinstimmender Pixel (Soll ≥ 0.995, `perceptualPrecision` 0.98).
- Android „Test-Diff" = von Paparazzi gemeldete Abweichung in % (Soll ≤ 0.5); bei grünen Tests meldet Paparazzi keinen Wert.
- „diff-report" = `scripts/diff-report`, exakter Pixelvergleich ohne Toleranz (roh, nicht die Testschwelle).

| Screen | Modus | iOS Test | iOS Match | iOS diff-report | Android Test | Android Test-Diff % | Android diff-report |
|---|---|---|---|---|---|---|---|
| DokViewer | dunkel | rot | 0.982758 | 257525 px (8.693 %) | grün | ≤ 0.5 (grün) | 50302 px (1.698 %) |
| DokViewer | hell | rot | 0.983185 | 307763 px (10.3888 %) | grün | ≤ 0.5 (grün) | 50271 px (1.6969 %) |
| Dokumente | dunkel | rot | 0.975983 | 366316 px (12.3653 %) | rot | 0.655050 | 82820 px (2.7957 %) |
| Dokumente | hell | rot | 0.976807 | 365704 px (12.3447 %) | rot | 0.717528 | 85617 px (2.8901 %) |
| Gewerk | dunkel | rot | 0.978012 | 266502 px (8.996 %) | grün | ≤ 0.5 (grün) | 54352 px (1.8347 %) |
| Gewerk | hell | rot | 0.979050 | 266017 px (8.9797 %) | grün | ≤ 0.5 (grün) | 54360 px (1.835 %) |
| Home | dunkel | rot | 0.980164 | 350524 px (11.8323 %) | grün | ≤ 0.5 (grün) | 64777 px (2.1866 %) |
| Home | hell | rot | 0.981140 | 348833 px (11.7752 %) | rot | 0.501720 | 67359 px (2.2738 %) |
| Login | dunkel | rot | 0.980209 | 70900 px (2.3933 %) | grün | ≤ 0.5 (grün) | 57456 px (1.9395 %) |
| Login | hell | rot | 0.981430 | 70567 px (2.3821 %) | grün | ≤ 0.5 (grün) | 57571 px (1.9434 %) |
| Mitteilungen | dunkel | rot | 0.974731 | 86013 px (2.9035 %) | rot | 0.522716 | 61851 px (2.0878 %) |
| Mitteilungen | hell | rot | 0.976273 | 85704 px (2.893 %) | rot | 0.548349 | 61883 px (2.0889 %) |
| Profil | dunkel | rot | 0.983292 | 342210 px (11.5516 %) | rot | 0.538532 | 62236 px (2.1008 %) |
| Profil | hell | rot | 0.984055 | 342983 px (11.5777 %) | rot | 0.564745 | 62260 px (2.1016 %) |
| Vergleich | dunkel | rot | 0.977020 | 268587 px (9.0664 %) | rot | 0.520568 | 68244 px (2.3036 %) |
| Vergleich | hell | rot | 0.977982 | 268110 px (9.0503 %) | rot | 0.586829 | 68283 px (2.305 %) |
| Zeitplan | dunkel | rot | 0.984055 | 335205 px (11.3152 %) | grün | ≤ 0.5 (grün) | 57114 px (1.9279 %) |
| Zeitplan | hell | rot | 0.984680 | 334723 px (11.2989 %) | rot | 0.527367 | 56921 px (1.9214 %) |

Grundlage der Werte: iOS-Match aus den `xcodebuild`-Fehlermeldungen, Android-Test-Diff aus den Paparazzi-JUnit-Berichten, beide Spalten „diff-report" aus `scripts/diff-report --platform ios|android` (Ist-Bilder in `build/snapshots/<plattform>`). Die Roh-Pixelzahlen liegen deutlich über den Testschwellen, weil der Test Toleranzen anwendet (iOS perceptualPrecision, Android pro Pixel); sie sind nur untereinander vergleichbar. Ursache der Reste: Glyph-Raster (M2 R11 / M3 R2, CoreText bzw. Layoutlib gegen Skia), siehe `docs/m3-offene-punkte.md`; geändert wurde nichts.
