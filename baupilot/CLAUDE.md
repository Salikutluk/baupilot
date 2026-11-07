# Baupilot – Regeln für Claude Code

Plan: siehe docs/implementierungsplan.md (Export des Docs „Baupilot – Implementierungsplan für Claude Code“).

## Spezifikation
- Quelle der Wahrheit = design/prototype/*.dc.html. Maße aus den Inline-Styles, Inhalte aus renderVals().
- Theme-Werte stehen im Objekt `t` jeder Datei (hell = zweiter Zweig, dunkel = erster Zweig von `d ? {…} : {…}`).
- Dunkel-Varianten (*Dark.dc.html) sind nur Hüllen um denselben Screen mit dark = true.
- Werte 1:1 übernehmen: px → pt (iOS) bzw. dp (Android). Nicht runden, nicht „verbessern“.
- Farben nur über generierte Tokens aus design/tokens.json (core + perScreen). Keine Hex-Werte in Screens.
- Texte wörtlich aus dem Prototyp, inkl. Sonderzeichen (·, –, ≥, €, Δ).
- Daten nur aus fixtures/baupilot.json über MockRepository (bis M7).

## Fertig heißt
- Snapshot-Tests hell + dunkel gegen design/reference/<ios|android>/<Name>_<light|dark>.png sind grün
  (iOS: precision 0.995, perceptualPrecision 0.98 · Android/Paparazzi: maxPercentDifference 0.5).
- Toleranzen nie lockern. Bei Abweichung: scripts/diff-report, Ursache beheben, erneut testen.
- Touch-Ziele ≥ 44 pt (iOS) / ≥ 48 dp (Android); Accessibility-Labels aus aria-label.
- Platzhalter-Links (href="#") bleiben sichtbar, ohne Aktion.

## Arbeitsweise
- Ein Screen = ein Branch + PR. Reihenfolge laut design/screens.md und Milestone.
- Fehlt eine Komponente: zuerst im Design-System anlegen, mit Galerie-Snapshot.
- Plattformen: iOS 17+ (SwiftUI), Android 8+ / API 26 (Jetpack Compose, Material 3).
- Schriften: Inter (iOS) und Roboto (Android) als TTF einbetten, auch im Testpfad.
