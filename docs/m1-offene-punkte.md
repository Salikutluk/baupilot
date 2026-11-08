# M1 – Offene Punkte

Bekannte Lücken und offene Entscheidungen nach M1. Die Maßnachweise stehen in `docs/m1-masspruefung.md`.

## 1. Widersprüche im Prototyp und gewählter Standard

Mehrere M1-Zahlen stammen aus einem einzelnen Board und widersprechen dem Rest des Prototyps. Jede Komponente hat deshalb benannte Fälle. Der Standard ist der häufigste Prototypwert; der Wert aus `docs/milestones/M1.md` bleibt als eigener Fall verfügbar, wenn er abweicht. Freie Zahlen gibt es nicht, nur gemessene Fälle.

| Thema | Widerspruch | Standard in der Komponente | Fall im Code |
|---|---|---|---|
| StatusBadge (iOS) | M1.md: Padding 5 × 9, r8. Das gilt nur auf `IosHome.dc.html:58`; neun Boards haben 4 × 8, r7 (z. B. `IosAbnGewerke.dc.html:36`) | 4 × 8, r7 | `StatusBadge.Size.compact` (Standard); `.roomy` = 5 × 9, r8; `.mini` = 11 / 700, 3 × 7, r6 (`IosGewerk.dc.html:70`) |
| Segmente (iOS) | M1.md: 44. Dieselbe Steuerung ist 44 auf `IosAbnMaengel.dc.html:25`, 34 auf `IosAbnGewerke.dc.html:25` und 36 auf `IosAbnMangel.dc.html:56-57` (Container `seg`, `:55`) | 34 | `SegmentedControl.SegmentHeight.compact` (Standard, 34); `.regular` = 44; `.medium` = 36 mit `Background.seg` |
| ListRow-Kachel (iOS) | M1.md: 38 × 38, r10. Das gilt nur auf `IosGewerk.dc.html:60` und `IosDokumente.dc.html:59`; sonst 34 r9, 34 r10, 36 r10 | 38, r10 | `ListRow.TileSize.s38` (Standard); `.s34r9`, `.s34r10`, `.s36r10` |
| „Mangel“ / „In Ordnung“ (iOS) | M1.md: 16–17. Im Prototyp sind es 15 / 600 auf sechs Boards und 16 / 600 auf drei | Standardgröße je Button im Code | `ButtonSize` (`.large` 17, `.medium` 16, `.small` 15): `DefectButton` Standard `.small` (15), `OkButton` Standard `.medium` (16). Jede Größe ist wählbar |
| Toggle „an“ (iOS) | Die Farbe ist `t.ok` (dunkel `#22A355`, perScreen) auf `IosAbnMangel`/`IosElRauchmelder` und `t.on` (dunkel `#4ADE80`) auf `IosProfil` | `Tokens.on` (Profil) | `IosToggle(onColor:)`, Standard `Tokens.on`. Die Screens übergeben `Tokens.ok.core` bzw. die perScreen-Variante |
| Outlined-Button (Android) | M1.md: 48. Die als `<a>` gebauten Outlined-Buttons rendern 50, weil der Rahmen außerhalb liegt (gemessen in `Vergleich_light.png`, Bildzeilen y 770–820) | 48 | `OutlinedButton(borderOutside = false)` (Standard, 48); `borderOutside = true` = 50 |
| Switch (Android) | M1.md: „M3-Switch 52 × 32“. Der Prototyp hat eine eigene Geometrie (Rahmen 2, Padding `0 4px`, Knopf 24 / 16); der Standard-Switch von Material 3 liegt um 2 dp daneben (gemessen an `Profil_light.png`) | eigene Geometrie | `BaupilotSwitch` (kein M3-Switch) |
| LinearProgress (Android) | Der Prototyp zeichnet weder Lücke noch Stopp-Punkt; der M3-Indikator zeichnet beides | zwei Boxen ohne Lücke und Punkt | `BaupilotLinearProgress` (kein M3-Indikator) |

## 2. Zurückgestellte Komponenten (D13)

Keine dieser Komponenten gehört zu den M1-Aufgaben. Der erste Milestone, der sie braucht, legt sie im Design-System an und ergänzt die Galerie (Regel aus `CLAUDE.md`).

**iOS**

- Abschnittskopf 20 / 700 mit Link, und Kleinschrift 13 / 600, Laufweite +0,4
- Detailtitel 30 / 28 / 22
- Formularfeld und Textbereich
- Zahlen-Stepper
- Avatar
- Status-Kachel-Raster
- Segmentierter Fortschritt (AbnElemente)
- Invertiertes Mangel-Paar (`IosFlBad.dc.html:56`)

**Android**

- Outlined Textfeld
- Zahlen-Stepper als Pille
- Sekundäre Tab-Leiste
- Segmentierte Buttons
- Banner
- Checkbox
- Tonaler Button
- Abschnittsköpfe
- Screen-Gerüst

## 3. Touch-Ziele erst in M7 geprüft (D14, R11)

- iOS: Interaktive Komponenten unter 44 pt Höhe (FilterChip 36, Segmente 34) vergrößern ihre Trefferfläche mit `.contentShape(Rectangle().inset(by: -n))`, n = (44 − h) / 2. Das Layout bleibt unverändert.
- Android: Klickbare Elemente nutzen `Modifier.clickable` ohne `minimumInteractiveComponentSize()`. Die Erweiterung auf 48 dp übernimmt Compose selbst über `LocalViewConfiguration.minimumTouchTargetSize`.
- Offen: Beides ist **nicht auf einem Gerät geprüft**. Die Prüfung gehört zu den UI-Tests in M7. Die Maßtests messen nur die sichtbaren Größen.

## 4. Schatten auf API 26–27 (D12, R10)

Auf API 26 und 27 zeichnen Hardware-Layer keinen Schatten-Layer auf Formen. Der Schatten des `ExtendedFab` (`Modifier.cssShadow`) fehlt dort. Die Abbildung CSS-Unschärfe → Skia-Radius ist hergeleitet und nur über die Galerie-Goldens festgenagelt, nicht gemessen (Herleitung in `docs/m1-masspruefung.md`).

## 5. Rendering lokal gegen CI (R1)

- Die iOS-Goldens wurden lokal aufgenommen. Sie können vom Rendering des CI-Runners (`macos-15`, arm64) abweichen (M0 Punkt 10). Die Toleranzen bleiben unverändert (0,995 / 0,98).
- CI wurde noch nie grün beobachtet (M0 Punkt 6). Eine rote CI nach dem ersten Lauf kann daher an den Goldens liegen, ohne dass sich etwas geändert hat.
- Vorgehen bei Abweichung: Das CI-Artefakt `build/snapshots/ios-gallery` (wird vor dem Vergleich geschrieben) ansehen, bei Eignung als neues Golden übernehmen und die Übernahme hier dokumentieren.
- Android (Paparazzi, Layoutlib mit eingebetteten Schriften) soll stabil sein. Das ist nicht beobachtet.

## 6. Symbole (T-IC-5)

`scripts/icons.json` enthält **65 Assets** (`icons`), **1 Alias** (`aliases`: `ic_info`, `AndVergleich.dc.html:50`) und **8 Nicht-Symbole** (`nonIcons`: Tür-, Fenster-, Unterschrift- und Gefälle-Zeichnungen je Plattform, mit `Datei:Zeile`). Der Test `T-IC-5` in `scripts/test/gen-icons.test.mjs` (alle Symbolgeometrien des Prototyps abgedeckt) ist grün; er ist maßgeblich, nicht diese Zahl. Die Zahl liegt über der Schätzung der Planung (45 Geometrien nach dem Zusammenführen), weil jede verwendete Strichstärke ein eigenes Asset ist (`ic_<name>_w<10×Breite>`, z. B. `ic_alert_circle_w22`).

## 7. Übersprungenes in der Umsetzung

Zum Zeitpunkt dieser Datei liegt keine Meldung über übersprungene Schritte vor, die sich hier prüfen ließe. Der Integrationsschritt trägt Übersprungenes, Fallbacks und Fehlschläge in einem eigenen Abschnitt am Ende dieser Datei nach.

## Verifikation

Alle Schritte liefen am 2026-11-08 im Worktree (Plan §10). Goldens wurden nicht aufgenommen, Toleranzen nicht geändert, `design/` nicht angefasst.

| # | Befehl | Exit | Ergebnis |
|---|--------|------|----------|
| 1 | `cd scripts && node --test test/` | 0 | pass – 114 Tests, 114 pass, 0 fail (`repo tree is clean` grün). Der erste Lauf hatte 1 Fehler (T-DS-4), siehe Reparaturen. |
| 2 | `scripts/gen-tokens --check`, `scripts/gen-icons --check`, `scripts/lint-hex`, `scripts/design-freeze --check` | 0 / 0 / 0 / 0 | pass – `lint-hex: sauber`, „design/ matches scripts/design-v1.sha256 (276 files)“ |
| 3 | `cd ios && xcodegen generate && xcodebuild test … 'platform=iOS Simulator,name=iPhone 15'` | 65 | **fail** – `GallerySnapshotTests.testGalleryLight/testGalleryDark`: `referenceMissing(…/Goldens/GalleryStructure_{light,dark}.png)`. `Executed 49 tests, with 2 failures`, `** TEST FAILED **`. Alle übrigen Tests grün, `LoginExampleSnapshotTests` als erwarteter Fehlschlag wie in M0 (`0.8338623 is less than required 0.995`). Der Lauf mit `.structure` temporär aus der Schleife genommen (danach zurückgesetzt): `Executed 2 tests, with 0 failures`, `** TEST SUCCEEDED **`, d. h. die übrigen 7 Seiten stimmen mit ihren Goldens überein. |
| 4 | `cd android && ./gradlew :app:testDebugUnitTest` (vorher `rm -rf app/build/paparazzi/failures`) | 0 | pass – `BUILD SUCCESSFUL`, 45 Tests, 1 übersprungen (`LoginExampleSnapshotTest`), 0 Fehler. `GalleryLightSnapshotTest.allPages` und `GalleryDarkSnapshotTest.allPages` grün. |
| 5 | `scripts/diff-report` iOS (`ios-gallery`) und Android (`android-gallery`) | 0 / 0 | pass – alle 14 iOS- und alle 16 Android-Seiten `0 px (0 %)` Abweichung (iOS ohne `GalleryStructure`, siehe Schritt 3) |
| 6 | Planted-Hex: `Color(0xFF000000)` in `android/app/src/main/java/de/baupilot/scratch/Planted.kt` | 1, danach 0 | pass – `Planted.kt:3:15 color-ctor Color(0x`, `Planted.kt:3:21 int-color 0xFF000000`, `lint-hex: 2 Fund(e)`; nach Entfernen von Datei und Ordner `lint-hex: sauber` |
| 7 | `./gradlew :app:compileReleaseKotlin`; `xcodebuild build … -configuration Release -destination 'generic/platform=iOS Simulator'` | 0 / 0 | pass – `BUILD SUCCESSFUL`; `** BUILD SUCCEEDED **` |
| 8 | `git status --porcelain` | – | pass – nur die erwarteten Änderungen; `reports/`, `build/` und `ios/Baupilot.xcodeproj` sind ignoriert (`git check-ignore`) |

AC-Zuordnung: AC2 (`testStatusBadgeHeights`, `testSegmentedControlHeights`, `testToggleIs51x31`, `testTabBarIs84Tall`, `testEveryGalleryPageFitsTheScreen` auf iOS; `navigationBarIs80AndPillIs56x32`, `switchIs52x32AndThumbPositions`, `extendedFabIs56Tall`, `searchBarIs56` auf Android) grün. AC3 grün. AC1 ist auf iOS **nicht erfüllt**: das Golden der Seite `structure` fehlt.

### Reparaturen

- `ios/Baupilot/DesignSystem/Components/IosToggle.swift`: `accessibilityRepresentation { Toggle(…) }` durch `.accessibilityLabel`, `.accessibilityValue` und `.accessibilityAddTraits(.isToggle)` ersetzt. T-DS-4 verbietet `Toggle(` in den Komponenten. Das Aussehen bleibt unverändert.
- `ios/project.yml`: `ASSETCATALOG_COMPILER_APPICON_NAME: ""` im App-Target. XcodeGen setzt `AppIcon` sobald ein Asset-Katalog existiert; es gibt kein App-Icon, und der Build brach mit `None of the input catalogs contained a matching … named "AppIcon"` ab (Schritt 3, erster Lauf).

### Offen

- Die 16 iOS-Gallery-Goldens (`GalleryStructure_*` eingeschlossen) sind aufgenommen, angesehen und werden grün geprüft (`xcodebuild test`: 51 Tests, 0 Fehler).
- Android: `GalleryPage.kt` misst die Seite mit unbegrenzter Höhe und ruft `checkPageHeight` auf (Grenze 2532 px). `GalleryPageHeightTest` (T-AGAL-2) prüft, dass der Wächter oberhalb der Grenze auslöst.
- Lokal wurde `ANDROID_HOME=$HOME/Library/Android/sdk` gesetzt, weil keine `android/local.properties` existiert (ignoriert).
