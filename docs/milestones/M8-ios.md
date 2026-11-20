# M8 · iOS · Echte Daten (nach visueller Abnahme)

**Ziel:** Die iOS-App speichert lokal, synchronisiert mit Appwrite und erzeugt Abnahmeprotokolle als PDF.

**Voraussetzung:** M7 · iOS abgeschlossen und visuell abgenommen; Backend-Entscheidung freigegeben. M8 · Android ist keine Voraussetzung; dieser Plan läuft für sich.

**Umfang:** `ios/`, das gemeinsame Appwrite-Backend (nur anlegen oder erweitern, nie Bestehendes brechen) und `docs/m8-ios-offene-punkte.md`. Nichts unter `android/` ändern.

## Aufgaben

### Backend (gemeinsam mit Android; überspringen, was schon besteht)

- [ ] Appwrite Cloud (Region Frankfurt): Auth (E-Mail, Apple), Tabellen für Projekte, Gewerke, Angebote, Dokumente, Abnahmen, Prüfschritte, Mängel, Unterschriften, Mitteilungen; Buckets für Fotos und PDFs; Teams für Bauherr, Architekt, Handwerker.
- [ ] Appwrite Function für Fristen-Erinnerungen (täglich); Versand an iOS über APNs. Ist die Function schon da (aus M8 · Android), nur den APNs-Weg ergänzen.

### iOS-App

- [ ] Lokale Persistenz mit SwiftData, mit denselben Modellen wie das Fixture.
- [ ] Sync-Warteschlange zu Appwrite hinter `BackendRepository`; Konflikte zunächst „letzte Änderung gewinnt“ pro Feld mit Zeitstempel.
- [ ] Anmeldung über Appwrite Auth (E-Mail, Apple).
- [ ] Kamera und Fotoablage je Prüfschritt; Dokument-Import (PDF) mit Gewerk-Zuordnung.
- [ ] Push-Empfang der Fristen-Erinnerungen über APNs.
- [ ] PDF-Export des Abnahmeprotokolls mit Unterschriften, Zeitstempel und Hash.

## Abnahmekriterien

- [ ] iOS-Snapshot-Tests laufen weiter gegen das Fixture und bleiben grün.
- [ ] Offline erfasste Prüfschritte und Fotos werden nach Wiederverbindung übertragen (Test im Flugmodus auf iOS).
- [ ] Datenschutz: Datenstandort Frankfurt, Löschung eines Projekts entfernt alle Dateien.

## Auftrag an Claude Code

```text
Arbeite docs/milestones/M8-ios.md ab. Ändere nichts unter android/. Die UI darf sich nicht ändern; alle iOS-Snapshots müssen grün bleiben.
```
