# M7 · iOS · Interaktion, Barrierefreiheit & Feinschliff

**Ziel:** Alle 68 iOS-Snapshots und alle iOS-Klickpfade sind grün; die iOS-App ist zugänglich.

**Voraussetzung:** M6 auf iOS abgeschlossen (alle iOS-Tests und `scripts`-Tests grün). M7 · Android ist keine Voraussetzung; dieser Plan läuft für sich.

**Umfang:** Nur `ios/`, plattformneutrale Teile von `scripts/` und `docs/m7-ios-offene-punkte.md`. Nichts unter `android/` ändern.

## Aufgaben

- [ ] UI-Tests (XCUITest, `BaupilotUITests`) für alle Links mit Ziel (siehe Abschnitt „Links“ je Screen in M2–M6); Platzhalter ohne Aktion.
- [ ] VoiceOver: Labels aus `aria-label`, Diagramme (Zeitplan, Gefälle, Raster) als ein Element mit Beschreibung.
- [ ] Automatischer Check der Touch-Ziele (≥ 44 pt).
- [ ] Diff-Report aller 34 Screens × 2 Modi (`scripts/diff-report --platform ios`); offene Reste beheben oder begründen.
- [ ] Sichtprüfung bei großer Schrift (Dynamic Type XL): nichts überlappt.
- [ ] Gemeinsame Werkzeuge in `scripts/` nur rückwärtskompatibel ändern; `cd scripts && node --test test/` bleibt grün.

## Abnahmekriterien

- [ ] 68/68 iOS-Snapshots grün (precision 0.995, perceptualPrecision 0.98).
- [ ] Alle iOS-UI-Tests grün (`BaupilotUITests`).
- [ ] Accessibility-Audit ohne Fehler (Xcode Accessibility Inspector).
- [ ] `scripts`-Tests grün.

## Auftrag an Claude Code

```text
Arbeite docs/milestones/M7-ios.md ab. Ändere nichts unter android/. Ändere keine Maße, um Tests grün zu bekommen, ohne den Prototyp zu prüfen.
```
