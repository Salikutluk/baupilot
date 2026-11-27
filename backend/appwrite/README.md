# Baupilot · Appwrite-Backend

Gemeinsames Backend für die Apps (Android zuerst, iOS kann es mitnutzen). Alles liegt als Code in diesem Ordner. **Nichts davon ist deployt**: Das Deployment braucht Ihre Appwrite-Zugangsdaten und veröffentlicht auf einen externen Dienst, deshalb ist es ein Betreiberschritt (siehe unten).

## Inhalt

| Datei | Zweck |
|---|---|
| `appwrite.config.json` | Appwrite-CLI-Konfiguration (CLI 13.3): Endpunkt Frankfurt, Datenbank `baupilot` mit 12 Tabellen, zwei Buckets, drei Teams, E-Mail-Anmeldung, Rechte, zwei Functions |
| `functions/deadline-reminders/` | täglich 06:00 UTC: Push „Fristen-Erinnerung“ über Appwrite Messaging (FCM) |
| `functions/project-cleanup/` | beim Löschen einer `projects`-Zeile: entfernt alle Zeilen und Dateien des Projekts |
| `test/` | `node --test`-Tests für Konfiguration und die reine Logik der Functions |

Tests (ohne Netzwerk, ohne Abhängigkeiten): `cd backend/appwrite && node --test test/`

## Wichtig vor dem ersten Push

- `projectId` in `appwrite.config.json` ist der **Platzhalter `baupilot`**. Er muss vor dem ersten `appwrite push` durch die echte Projekt-ID ersetzt werden, lokal oder in einem von Ihnen freigegebenen Commit.
- Die Konfiguration enthält weder `vars` noch Schlüssel oder Geheimnisse und darf keine enthalten. Der Apple-Anbieter (OAuth) und der FCM-Anbieter stehen nicht in der Datei, weil ihre Geheimnisse nicht in Git gehören; beide legen Sie in der Konsole an.
- Die Functions bekommen ihren API-Schlüssel zur Laufzeit über den dynamischen Schlüssel (`x-appwrite-key`) mit den Bereichen aus der Konfiguration.

## Zugriffsmodell

- Drei Teams: `bauherr`, `architekt`, `handwerker`. Tabellenrechte, `rowSecurity: false`.
- Alle drei Teams: Lesen auf jeder Tabelle; Anlegen und Ändern auf `steps`, `photos`, `signatures`, `protocols`, `defects`, `documents`, `members`, `inspections`.
- `bauherr` und `architekt`: Anlegen und Ändern auf `projects`, `trades`, `offers`, `notifications`. Nur `bauherr`: Löschen auf `projects`.
- Buckets `photos` (jpg/jpeg, 20 MB) und `pdfs` (pdf, 50 MB): Verschlüsselung und Virenscan an, `fileSecurity: false`, Lesen und Anlegen für die drei Teams, kein Löschen durch Clients.
- Bekannte Lücke: Es gibt keine Trennung der Zeilen pro Projekt. Die Mitgliedschaft (`members`) dient nur zum Adressieren der Pushes. Echte Trennung bräuchte ein Team je Projekt plus Zeilensicherheit.
- Der Push enthält nur eine Anzahl und ein Datum („2 Mängel mit Frist bis 20.12.2026“) und die Daten `{target: "defects", projectId}`, keinen Mangeltitel, keine Adresse, keine Namen.

## Betreiber-Checkliste

Das braucht Ihre Konten und wird nicht vom Entwicklungsteam ausgeführt.

1. Appwrite-Cloud-Projekt in der **Region Frankfurt** anlegen. Android-Plattform mit Paket `de.baupilot` hinzufügen.
2. Die Projekt-ID in `backend/appwrite/appwrite.config.json` eintragen (statt `baupilot`). Dann: `cd backend/appwrite && appwrite login && appwrite push settings && appwrite push tables && appwrite push buckets && appwrite push teams && appwrite push functions`.
3. Firebase-Projekt: Android-App `de.baupilot` hinzufügen. Die vier Werte als `baupilot.fcm.*` in `android/local.properties` eintragen. In der Appwrite-Konsole den Messaging-FCM-Anbieter mit der ID **`fcm`** aus der Service-Account-JSON anlegen.
4. Apple-OAuth-Anbieter (gemeinsam mit iOS): Zugangsdaten in der Konsole eintragen.
5. Testnutzer den Teams `bauherr`, `architekt` oder `handwerker` hinzufügen.
6. `baupilot.appwrite.projectId` in `android/local.properties` setzen, dann den Debug-Build auf `Medium_Phone_API_36.1` oder einem Gerät installieren.
7. **Flugmodus-Test (A2):** online anmelden; Flugmodus an; auf dem Fensterschritt F8 „In Ordnung“ mit Foto und eine Messung speichern; App beenden; Flugmodus aus. Binnen weniger Minuten erscheinen die Zeilen in `steps` und `photos` und die Datei im Bucket in der Konsole, und die Outbox ist leer (`adb shell run-as de.baupilot sqlite3 databases/baupilot.db 'select count(*) from outbox'`).
8. **Lösch-Test (A3b):** `deleteProject` über einen reinen Debug-Pfad auslösen (optional ein `adb shell am broadcast`-Empfänger in `src/debug`) oder die `projects`-Zeile in der Konsole löschen. Danach sind alle Zeilen des Projekts und alle Dateien unter `<projectId>/` in beiden Buckets weg.
9. `deadline-reminders` einmal manuell in der Konsole ausführen. Die Benachrichtigung kommt auf dem Gerät an; ein Tipp darauf öffnet die Mängelansicht.

## Hinweise zur Konfiguration

- Spaltentypen nach CLI 13.3: Text und JSON-Zeichenketten sind `varchar` mit Größe, große Nutzlasten (`stamps`, `detail`, `viewer`, `strokes`, `contentJson`) sind `longtext`. `required: true` verlangt in der CLI ausdrücklich `default: null`.
- Jede Tabelle hat den Index `idx_projectId`; `steps` und `members` zusätzlich einen eindeutigen Index auf (`projectId`, `stepKey`) bzw. (`projectId`, `userId`).
- `project-cleanup` hört auf das Ereignis `databases.baupilot.tables.projects.rows.*.delete` (TablesDB-Ereignisformat von Appwrite 1.8); der Konfigurationstest hält die Zeichenkette fest.
- Der Zeitplan `0 6 * * *` gilt in UTC (07:00 bzw. 08:00 Uhr in Berlin). Das „heute“ der Function wird in `Europe/Berlin` berechnet.
- Doppelte Ausführung ist ungefährlich: Die Nachrichten-ID ist je Tag und Projekt fest; ein bekannter Wert (409) zählt als übersprungen.
