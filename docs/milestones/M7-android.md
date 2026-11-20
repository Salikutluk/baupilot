# M7 · Android · Interaktion, Barrierefreiheit & Feinschliff

**Ziel:** Alle 68 Android-Snapshots und alle Android-Klickpfade sind grün; die Android-App ist zugänglich.

**Voraussetzung:** M6 auf Android abgeschlossen (alle Android-Tests und `scripts`-Tests grün). M7 · iOS ist keine Voraussetzung; dieser Plan läuft für sich.

**Umfang:** Nur `android/`, plattformneutrale Teile von `scripts/` und `docs/m7-android-offene-punkte.md`. Nichts unter `ios/` ändern.

## Aufgaben

- [ ] UI-Tests (Compose/Robolectric) für alle Links mit Ziel (siehe Abschnitt „Links“ je Screen in M2–M6); Platzhalter ohne Aktion.
- [ ] TalkBack: Labels aus `aria-label`, Diagramme (Zeitplan, Gefälle, Raster) als ein Element mit Beschreibung.
- [ ] Automatischer Check der Touch-Ziele (≥ 48 dp).
- [ ] Diff-Report aller 34 Screens × 2 Modi (`scripts/diff-report --platform android`); offene Reste beheben oder begründen.
- [ ] Sichtprüfung bei großer Schrift (Schriftgröße 130 %): nichts überlappt.
- [ ] Gemeinsame Werkzeuge in `scripts/` nur rückwärtskompatibel ändern; `cd scripts && node --test test/` bleibt grün.

## Abnahmekriterien

- [ ] 68/68 Android-Snapshots grün (Paparazzi, maxPercentDifference 0.5).
- [ ] Alle Android-UI-Tests grün (`:app:testDebugUnitTest`).
- [ ] Accessibility-Audit ohne Fehler (Android Accessibility Scanner).
- [ ] `scripts`-Tests grün.

## Auftrag an Claude Code

```text
Arbeite docs/milestones/M7-android.md ab. Ändere nichts unter ios/. Ändere keine Maße, um Tests grün zu bekommen, ohne den Prototyp zu prüfen.
```
