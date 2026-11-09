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

## Entscheidungen

Aus dem Plan (§3), in Kurzform. D1, D4, D5, D7, D8 (Teil), D12, D15, D16 und D18 (Routen, Gewerke-Zeilen, Verknüpfungen, Snapshot-Zustände, reine Geometrie, Branch-Zuschnitt, Plattform-Nullwerte, Kennungen) stehen nur im Plan.

| # | Entscheidung | Begründung |
|---|---|---|
| D2 | Die Wurzel des Abnahmen-Tabs hat zwei Ansichten („Gewerke“, „Offene Mängel“), gehalten im Navigationsmodell (iOS `InspectionsView`, Android `InspectionsView` in `AppNavigationState`). Neue Operationen `selectInspectionsView(_:)` und `showDefects()`; `go(.defects)` ruft `showDefects()` (Tab Abnahmen, Abnahmen-Stapel geleert, Ansicht Mängel). `RouteParser` bildet `"defects"` auf `.defects` ab (vorher `.tab(.inspections)`). `logIn()` setzt die Ansicht auf „Gewerke“; ein Tab-Wechsel oder erneutes Tippen ändert die Ansicht nie. Der Android-`Saver` hängt die Ansicht als 4. Element an; ein alter 3-Element-Zustand stellt `null` (Startzustand) her. | Die Prototypen zeichnen beide Ansichten als dieselbe Tab-Wurzel mit „Ansicht“-Segmenten; Snapshots rendern über den Navigationszustand. **Abweichung von M3 D15 (R8):** `showDefects` leert den Abnahmen-Stapel, weil das Ziel eine Wurzelansicht ist; ein Tab-Wechsel lässt Stapel weiter unberührt. |
| D3 | „Sichern“ auf Mangel erfassen öffnet die Abnahme-Unterschrift **über** dem Prüfschritt-Elternteil: `open(.signOff(tradeId), above: .inspection(tradeId))`, der Pfad wird `[…, inspection(windows), signOff(windows)]`. Gibt es für das Gewerk kein Sign-off-Modell, öffnet „Sichern“ nur `inspection(tradeId)`. „Abbrechen“ (iOS) / Schließen (Android) = `pop()`. | **Plan-Entscheidung, kein Prototyp-Link (R9, bekannte Abweichung):** Kein Prototyp führt zur Unterschrift, AC2 verlangt aber Mangel → Unterschrift. Verworfen: neue sichtbare Schaltfläche (erfindet UI) und „In Ordnung“ → Unterschrift (falsche Bedeutung). Die iOS-Zurück-Beschriftung „Fenster & Türen“ stimmt so mit der Referenz überein. |
| D6 | Keine Fixture-Änderung. Datenähnliche Texte stammen aus der Fixture; Template-Chrome und positionsabhängige Regeln stehen mit `Datei:Zeile` in `ScreenCopy` (u. a. „Vorlagen“, „Offene Mängel (%d)“, „Behoben?“ / „Behoben prüfen“, „So prüfen“, „Mangel erfassen“, „Sichern“, „Protokoll abschließen“; Android-Chip-Reihenfolge, Statistik-Töne nach Index, Fortschritt 0,42, Frist-Offsets 14/28/42 Tage). | M2 D9 / M3 D6: Die Fixture-Tests (Anti-Erfindung, `renderVals`-Abdeckung, abgetastete AbnSignatur-Chrome-Listen) machen Fixture-Änderungen riskant. |
| D9 | Der Zustand des Mangel-Formulars ist lokal (iOS `@State`, Android `rememberSaveable` je Route). Startwerte aus der Fixture: Schwere = Index von `severity.selected`, Schalter = `notify.enabled`, Frist-Chip = die Option, deren Datum `meta.now + Offset` dem `deadlineLabel` der Plattform entspricht (sonst `decoding("…defectF2.deadlineLabel")`). Ein Chip mit Offset berechnet das Datumsfeld neu (iOS „Wd, dd.MM.yyyy“ mit fester Wochentagstabelle, Android „dd.MM.yyyy“). Datumsrechnung nur auf Kalendertagen: iOS wandelt `now()` in einen `CalendarDay` in `Europe/Berlin` (R21). Nichts wird zurückgeschrieben (I8). | 18.12.2026 + 28 Tage = 15.01.2027; die Auswahl „+ 4 Wochen“ ist damit abgeleitet, nicht erfunden. Eine UTC-Umwandlung ergäbe 17.12. und ließe das Laden scheitern. |
| D10 | Unterschrift: Die Handwerker-Kachel zeichnet den Prototyp-Pfad als 9 absolute Kubik-Segmente in 120 × 48, Strich 2, runde Enden, Farbe `text`. Die Bauherr-Kachel ist eine Zeichenfläche (`SignatureStrokes`: Ziehen = Punkte, Tippen = ein Punkt-Strich). Solange sie leer ist, steht der Platzhalter („Hier unterschreiben“ / „Zum Unterschreiben tippen“); mit dem ersten Strich verschwindet er. Der Bauherr-Status bleibt „Ausstehend“ (iOS). | „Zeichenfläche … (Canvas, Strich 2)“; die Fixture hat keinen Text für einen unterschriebenen Bauherrn, also wird keiner erfunden. |
| D11 | Acht Ergänzungs-Tokens für die Foto-Szene in `scripts/tokens-supplement.json` (hell = dunkel, `source` = Prototypzeile): `photoSkyTop`, `photoSkyMid`, `photoSkyBottom`, `photoFrame`, `photoLevel` unter iOS und Android; `photoBadge`, `onPhoto`, `photoCaption` zusätzlich unter Android (iOS hat sie schon). | Keine Hex-Werte im App-Code (`lint-hex`); gleicher Mechanismus wie M3 D10. Kein Name kollidiert mit einem Kern-Schlüssel. |
| D13 | Android `BarPadding.Modal`: `gap` 0 → 4. Die Galerie-Goldens der Bars-Seite wurden neu aufgenommen und angesehen. | Die Mangel-Referenz setzt den Titel bei 57,7 dp (= 4 + 48 + 4 + Schriftvorlauf); Korrektur an der Komponente statt eines Beinahe-Duplikats. `Modal` wird nur von der Galerie benutzt. |
| D14 | Die iOS-Mangel-Ansicht ist **gepusht**, kein Sheet: eine Route im Abnahmen-`NavigationStack`, Kopfzeile `BackBar.modal`. | Der Snapshot-Host rendert gepushte Pfade (M3 Step 0); in der Referenz ist keine Sheet-Chrome sichtbar. |
| D17 | Typisierte Inspektions-DTOs (`InspectionOverview`, `ElementsCheck`, `StepCheck`, `DefectFormCheck`, `SignatureCheck`), je Abschnitt aus `inspections()` dekodiert; ein Formfehler wird zu `decoding("<Abschnittswurzel>")`, auf beiden Plattformen gleich. Android umhüllt `SerializationException` / `IllegalArgumentException` in `RepositoryException.Decoding`. `BackendRepository` bekommt `resolvedDefects()`. Texte: `screens.AbnGewerke/AbnMaengel` als `TabsScreenText {tabs}`, die anderen vier als `EmptyScreenText`. | Android-Strenge: Eine unbehandelte `SerializationException` würde abstürzen statt den M2-D11-Fallback zu zeigen (I7). |

### Abweichungen, die der Plan benennt

- **D8 (iOS-Zurück-Beschriftungen):** Erweiterung von M3 D3: `inspection(id)` = Zeilenname der Übersicht („Fenster & Türen“), `inspectionStep` = Schritt-Titel („F8 · Schlafen“, Rückfall Element-ID), `newDefect` = „Mangel erfassen“, `signOff(id)` = Titel der Abnahme („Abnahme abschließen“, Rückfall ID). Die neuen gespeicherten Eigenschaften von `RouteTitles` sind `var` mit leeren Vorgaben, damit der Memberwise-Aufruf in `RouteTitlesTests.swift` weiter kompiliert.
- **R8:** `go(.defects)` leert den Abnahmen-Stapel (Verhaltensänderung gegenüber der M3-Platzhalter-Abbildung); behandelt in D2, geprüft durch `testShowDefectsClearsOnlyInspectionsStack`.
- **R9:** „Sichern“ → Unterschrift ist eine Plan-Entscheidung, kein Prototyp-Link (D3).

## Bekannte Lücken

- **R1 / R2 / AC1 (offen):** Alle 24 M4-Snapshots sind rot (siehe Tabelle unten). Die Toleranzen sind unverändert (iOS 0,995 / 0,98, Android 0,5 %). Die in den Screen-Schritten per `diff-report` gefundenen Layoutursachen wurden dort behoben; die verbleibende Abweichung ist **nicht** als reiner Glyphen-Raster-Rest (M2 R11: CoreText bzw. Layoutlib gegen Skia) belegt, sondern nur gemessen. Android Signatur liegt mit 0,94 / 0,97 % beim Doppelten der Schwelle. Roboto hat kein U+2713 (R2): „✓ Mittel“ (Mangel) kommt auf Android aus einer Ersatzschrift und kann anders gerastert sein als in Chrome; es bleibt Text (Pflicht-Text). Der Anteil dieses Effekts an den Android-Werten wurde nicht einzeln gemessen. Kein Test wurde gelockert oder umgeschrieben.
- **R15:** Versteckte iOS-Tabs verlieren ihren `@State` (Mangel-Formular, Striche der Unterschrift) beim Tab-Wechsel. Angenommen: flüchtiges, ungespeichertes Formular (M4 speichert nichts).
- **R16:** Touch-Ziele unter 44 / 48 (Android-Chips 32, iOS-Chips 34 mit Hit-Bereich 44 über `contentShape`) folgen der M1-Konvention; Geräteprüfung in M7.
- **R20:** Android behält die Zustände verlassener Routen (`SaveableStateProvider`, kein `removeState`): Wird Mangel oder Unterschrift in gleicher Tiefe erneut geöffnet, erscheinen die früheren Eingaben bzw. Striche wieder; iOS beginnt frisch. Angenommen (flüchtig, nichts gespeichert, I8 unberührt).
- **Ohne Aktion, sichtbar (Plan §1):** Kamera/Foto, „Eigenes Datum“, „Vorlagen“, „Anleitung“, „Alle 6“, „Behobene Mängel“, „Erinnern“, „Behoben? / Behoben prüfen“, „In Ordnung“ (Schritt), Android-Filter-Chips und Hilfe-Schaltfläche. Gewerke außer Fenster öffnen den gepushten Platzhalter (M5/M6), Dachdecker hat keine Aktion.
- **R13 (Grammatik):** Nur Übersichtszeilen und Fenster-Checks werden in M4 geparst; die Fixture enthält weitere Routen (`inspection/<gewerk>/<sub>`), die M5 in die Grammatik aufnehmen muss, bevor es mehr lädt.

## Messwerte der 24 Snapshots (M4)

Lauf am Ende von Phase 14 auf dem Worktree `feat/m4`: iOS `xcodebuild test … -only-testing:BaupilotTests/ScreenSnapshotTests/testAbn<Name><Modus>` (iPhone 15, 12 Tests, 12 rot), Android `./gradlew :app:testDebugUnitTest --tests 'de.baupilot.snapshot.Abn*SnapshotTest'` (JDK 21 aus Android Studio, 12 Tests, 12 rot), danach `scripts/diff-report --platform ios|android`. „iOS Übereinstimmung“ (gefordert ≥ 0,995) und „Android Abweichung“ (erlaubt ≤ 0,5 %) sind die Testwerte; „Diff“ ist die Spalte aus `diff-report` (exakt abweichende Pixel, ohne Toleranz, nur untereinander vergleichbar).

| # | Screen | Modus | iOS Übereinstimmung | iOS Diff | Android Abweichung | Android Diff |
|---|---|---|---|---|---|---|
| 1 | Abnahmen je Gewerk | hell | 0,9735 rot | 12,7755 % | 0,5713 % rot | 2,5999 % |
| 2 | Abnahmen je Gewerk | dunkel | 0,9722 rot | 12,7885 % | 0,5424 % rot | 2,606 % |
| 3 | Fenster – Elemente | hell | 0,9770 rot | 9,177 % | 0,6418 % rot | 2,4815 % |
| 4 | Fenster – Elemente | dunkel | 0,9738 rot | 9,2188 % | 0,6140 % rot | 2,388 % |
| 5 | Prüfschritt F8 | hell | 0,9713 rot | 4,7354 % | 0,7769 % rot | 3,0165 % |
| 6 | Prüfschritt F8 | dunkel | 0,9699 rot | 4,7478 % | 0,7577 % rot | 3,0217 % |
| 7 | Mangel erfassen | hell | 0,9812 rot | 2,517 % | 0,7311 % rot | 2,1259 % |
| 8 | Mangel erfassen | dunkel | 0,9800 rot | 2,4885 % | 0,6709 % rot | 2,1298 % |
| 9 | Abnahme & Unterschrift | hell | 0,9799 rot | 10,303 % | 0,9711 % rot | 2,7196 % |
| 10 | Abnahme & Unterschrift | dunkel | 0,9784 rot | 10,3172 % | 0,9432 % rot | 2,7181 % |
| 11 | Offene Mängel & Fristen | hell | 0,9838 rot | 11,4542 % | 0,6747 % rot | 2,1796 % |
| 12 | Offene Mängel & Fristen | dunkel | 0,9831 rot | 11,4455 % | 0,6300 % rot | 2,1806 % |

**Stand AC1:** iOS 0 von 12 grün, Android 0 von 12 grün: **0 von 24 grün**, 24 rot. AC1 ist nicht erfüllt. Die Gründe stehen unter „Bekannte Lücken“ (R1/R2). Zum Vergleich mit Step 0 (I9): Die M2/M3-Snapshots wurden in diesem Schritt nicht neu gemessen; deren Werte stehen in der Step-0-Tabelle oben und in `m3-offene-punkte.md`.

Die Bilder liegen unter `build/snapshots/{ios,android}/` (je 30 Dateien: 18 M2/M3 + 12 M4). Die Quellen der Werte: iOS aus den `xcodebuild`-Fehlermeldungen (`/tmp/m4-p14-ios-abn.log`), Android aus den Paparazzi-JUnit-Berichten (`android/app/build/test-results/testDebugUnitTest`).

## Testzahlen

Gemessen in Phase 14; der vollständige Endlauf (Plan §12) gehört zu Phase 15, dort stehen die Gesamtzahlen von iOS `BaupilotTests` und Android `:app:testDebugUnitTest`.

| Block | Ergebnis |
|---|---|
| `cd scripts && node --test test/` | 114 Tests, 114 grün (Baseline 114) |
| `scripts/lint-hex` | `lint-hex: sauber` |
| iOS `BaupilotTests/ScreenSnapshotTests` (nur die 12 M4-Tests) | 12 Tests, 12 rot (Snapshot-Toleranz) |
| iOS `BaupilotUITests` | 27 Tests: `AppNavigationUITests` 7, `TradeFlowUITests` 11, `InspectionFlowUITests` 9. Letzter Lauf von `InspectionFlowUITests`: 9 von 9 grün, darunter `testWindowAcceptanceFlowEndsOnOpenDefects` (AC2). Der vorherige Lauf aller drei Klassen (27 Tests) hatte 1 Fehler in `InspectionFlowUITests` (`screen.inspection.earthworks`, behoben); die beiden anderen Klassen liefen in diesem Lauf grün (26 grün) |
| Android `de.baupilot.snapshot.Abn*SnapshotTest` | 12 Tests, 12 rot (Snapshot-Toleranz) |
| Android `de.baupilot.app.*` | 129 Tests, 129 grün: `AppDataTest` 57, `AppNavigationStateTest` 28, `AppNavigationUiTest` 9, `BaupilotRootLoadTest` 3, `InspectionFlowUiTest` 9 (darunter `windowAcceptanceFlowEndsOnOpenDefects`, AC2), `RouteParserTest` 10, `TradeFlowUiTest` 13 |
| `git status --porcelain -- design fixtures scripts` | leer (I11) |
