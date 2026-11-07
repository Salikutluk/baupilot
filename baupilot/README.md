# Baupilot – Design-Paket für die Implementierung

Inhalt (Stand 05.11.2026, aus dem Design-Canvas):

- `design/prototype/` – alle 136 Boards als .dc.html plus canvas.json. Lesbare Spezifikation, nicht eigenständig lauffähig.
- `design/reference/ios/`, `design/reference/android/` – 136 Referenzbilder, je 1170 × 2532 px (390 × 844 @3x), ohne Statusleiste.
  Namensschema: `<Name>_light.png` / `<Name>_dark.png`, Name wie in design/screens.md (iOS-Login = `Login`).
- `design/tokens.json` – Farben je Plattform und Modus (core + Abweichungen je Screen), Radien, Layout-Maße.
- `design/screens.md` – Screen-Inventar mit Quelle, Referenz, Route und Milestone.
- `CLAUDE.md` – Regeln für Claude Code.

Hinweise:
- Die Referenzbilder sind mit Inter (iOS) und Roboto (Android) gerendert. Einzelne Zeichen wie „≥“ fehlen im
  Roboto-Subset und erscheinen in einer Ersatzschrift; das ist im Snapshot-Vergleich zu berücksichtigen.
- Den Implementierungsplan als Markdown aus dem Doc exportieren und unter docs/implementierungsplan.md ablegen.
- Paket in das Repo-Wurzelverzeichnis entpacken; die Struktur entspricht dem Repo-Layout im Plan.
