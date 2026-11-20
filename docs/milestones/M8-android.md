# M8 · Android · Echte Daten (nach visueller Abnahme)

**Ziel:** Die Android-App speichert lokal, synchronisiert mit Appwrite und erzeugt Abnahmeprotokolle als PDF.

**Voraussetzung:** M7 · Android abgeschlossen und visuell abgenommen; Backend-Entscheidung freigegeben. M8 · iOS ist keine Voraussetzung; dieser Plan läuft für sich.

**Umfang:** `android/`, das gemeinsame Appwrite-Backend (nur anlegen oder erweitern, nie Bestehendes brechen) und `docs/m8-android-offene-punkte.md`. Nichts unter `ios/` ändern.

## Aufgaben

### Backend (gemeinsam mit iOS; überspringen, was schon besteht)

- [ ] Appwrite Cloud (Region Frankfurt): Auth (E-Mail, Apple), Tabellen für Projekte, Gewerke, Angebote, Dokumente, Abnahmen, Prüfschritte, Mängel, Unterschriften, Mitteilungen; Buckets für Fotos und PDFs; Teams für Bauherr, Architekt, Handwerker.
- [ ] Appwrite Function für Fristen-Erinnerungen (täglich); Versand an Android über FCM. Ist die Function schon da (aus M8 · iOS), nur den FCM-Weg ergänzen.

### Android-App

- [ ] Lokale Persistenz mit Room, mit denselben Modellen wie das Fixture.
- [ ] Sync-Warteschlange zu Appwrite hinter `BackendRepository`; Konflikte zunächst „letzte Änderung gewinnt“ pro Feld mit Zeitstempel.
- [ ] Anmeldung über Appwrite Auth.
- [ ] Kamera und Fotoablage je Prüfschritt; Dokument-Import (PDF) mit Gewerk-Zuordnung.
- [ ] Push-Empfang der Fristen-Erinnerungen über FCM.
- [ ] PDF-Export des Abnahmeprotokolls mit Unterschriften, Zeitstempel und Hash.

## Abnahmekriterien

- [ ] Android-Snapshot-Tests laufen weiter gegen das Fixture und bleiben grün.
- [ ] Offline erfasste Prüfschritte und Fotos werden nach Wiederverbindung übertragen (Test im Flugmodus auf Android).
- [ ] Datenschutz: Datenstandort Frankfurt, Löschung eines Projekts entfernt alle Dateien.

## Auftrag an Claude Code

```text
Arbeite docs/milestones/M8-android.md ab. Ändere nichts unter ios/. Die UI darf sich nicht ändern; alle Android-Snapshots müssen grün bleiben.
```
