# M8 Android · Offene Punkte

Dieses Dokument ist das einzige Dokument des M8-Android-Umfangs (Plan `implement-the-plan-v2`). Aufbau wie `docs/m7-android-offene-punkte.md`. Jede Zahl steht mit Quelle da: ein Gradle-Bericht (Pfad genannt) oder ein in diesem Lauf erneut ausgeführter Befehl (benannt). Der Stand ist der Arbeitsbaum von Branch `feat/implement-the-plan` (Basis `608e6d3`, nicht committet). Der Abschnitt „Verifikation“ trägt den Lauf von Step 15.

## Baseline

### Step 0 · Preflight

Diff-Basis für die Invarianten: `git rev-parse HEAD` = `608e6d31bccb402dd2f6b7366f445eb67f161e1f` (Branch `feat/implement-the-plan`). `android/local.properties` fehlte und wurde (git-ignoriert) mit `sdk.dir=$HOME/Library/Android/sdk` angelegt. JDK = JBR aus Android Studio, `ANDROID_HOME=$HOME/Library/Android/sdk` (Rezept: `docs/m7-android-offene-punkte.md` unter „Befehle“).

Gemessen auf `608e6d3` plus den Änderungen von Step 1 (Abhängigkeiten, `BackendConfig`, `ThrowawayDatabase`). Befehl: `(cd android && ./gradlew --continue :app:compileDebugKotlin :app:compileReleaseKotlin :app:testDebugUnitTest)`. Quelle: `android/app/build/reports/tests/testDebugUnitTest/index.html` und `android/app/build/test-results/testDebugUnitTest/*.xml` (129 Ergebnisdateien).

| Block | Ergebnis |
|---|---|
| `:app:compileDebugKotlin`, `:app:compileReleaseKotlin` | erfolgreich |
| `:app:testDebugUnitTest` | 738 Tests, 736 grün, 2 rot, 0 Fehler, 0 übersprungen; Dauer 18 min 34 s (kalt, Gradle meldet `BUILD FAILED in 18m 34s`) |
| Rot | ausschließlich `FlBadLightSnapshotTest` und `FlBadDarkSnapshotTest` (`flBad`) |
| Neu gegenüber der Planungs-Baseline (735 Tests) | 3 grüne Tests: `BackendConfigTest` (`endpointIsFrankfurt`, `blankProjectIdIsUnconfigured`, `noOtherAppwriteEndpointInSources`) |

Die Planungs-Baseline (735 Tests, 2 rot, etwa 18 min) ist damit reproduziert: dieselben zwei roten Tests wie in `docs/m7-android-offene-punkte.md` (66/68 Snapshots grün), kein neuer roter Test. Die neuen Abhängigkeiten haben kein Pixel bewegt (R2): alle übrigen Snapshot-Tests sind grün.

## Entscheidungen

Die Entscheidungen D1 bis D26 stammen aus dem Plan `implement-the-plan-v2` (§3); die Zeilen D-B1 bis D-B4 sind Abweichungen und Messungen aus Step 0 und 1. D24 steht zuerst, weil sie eine Bestätigung braucht.

| # | Entscheidung | Begründung |
|---|---|---|
| D24 | **Platzhalter-Abweichung von `CLAUDE.md` · Bestätigung durch den Nutzer offen.** `CLAUDE.md` sagt „Platzhalter-Links bleiben sichtbar, ohne Aktion“; `scripts/link-coverage.json` führt `ph.Dokumente.add`, `ph.AbnPruefung.ok`, `ph.Profil.row.6`, `step.ok`, `measure.save`, `slope.save`, `flatness.photo`, `tiles.none` als Platzhalter. Diese Steuerelemente handeln nur über `RootActions`; mit `AppActions.None` (Snapshots, `LinkHost`, `PlaceholderUiTest`) bleiben sie ohne Aktion. `PlaceholderUiTest` und `scripts/link-coverage.json` sind unverändert. | M8 braucht Einstiegspunkte für Import, Prüfschritt-Ergebnisse, Fotos und Abmelden, und diese Steuerelemente sind bei eingefrorener UI die einzigen. Der Meilenstein ist die neuere, speziellere Anweisung. Der unveränderte `PlaceholderUiTest` belegt I2; ihn oder das Manifest zu ändern würde den Konflikt verdecken und `scripts/` berühren. Rücknahme (R31): nur `RootActions` und die Parameter mit Vorgabewert. |
| D1 | `BackendRepository` behält seine Lese-Signaturen. Schreiben läuft über das neue Interface `CaptureRepository`; `LocalRepository` implementiert beide. `MockRepository` bleibt unverändert und bleibt die Snapshot-Quelle. | Die UI sieht nur Repository-Interfaces, nie die Warteschlange. Eine stabile Lese-Schnittstelle lässt `AppData.load`, alle reinen Tests und `TestAppData` unberührt. |
| D2 | Room speichert jede synchronisierte Entität als Record (`table`, `id`, `projectId`, `sortKey`, `fields`, `stamps`, `deleted`). `fields` ist das JSON-Objekt des Fixture-DTOs; gelesen wird mit denselben `@Serializable`-DTOs wie beim Fixture. Neue Erfassungsarten bekommen eigene DTOs. | „Dieselben Modelle wie das Fixture“ gilt wörtlich; Last-Write-Wins je Feld bildet sich auf JSON-Schlüssel ab. Gleichheit mit `MockRepository` gilt per Konstruktion. |
| D3 | Statische Gestaltungsinhalte kommen aus dem gebündelten Asset und sind keine Records (`meta`, `screens`, `schedule`, `comparisons`, `measurements`, `inspections.byTrade`, `statusLabels`, `documents.categories`, Basis-`counts`, `resolvedDefects`). Records: `project`, `trades`, `offers`, `documents.items`, `inspections.overview.rows`, `defects`, `notifications`-Einträge sowie die neuen Arten. | Das sind die M8-Tabellen. Der Rest ist App-Text und Gestaltungsdaten, die mit der App-Version ausgeliefert werden. Dokumentzähler = Basiswert plus importierte Records. |
| D4 | `LocalRepository.now()` liefert `meta.now` (2026-12-18) wie `MockRepository`. Zeitstempel von Erfassungen (`capturedAt`, `signedAt`, `createdAt`, Stamps) kommen von einer injizierten `java.time.Clock`. | Sonst verschieben sich alle Datumslabels in `AppData`, und Äquivalenztest und Robolectric-Klickpfade brechen. |
| D5 | `AppData` wird beim Start geladen und bei `ON_START` neu, nur wenn sich `LocalRepository.dataVersion` geändert hat. Eine Erfassung löst kein sofortiges Neuladen aus. Beim Neuladen bleibt das alte `AppData` sichtbar; ein fehlgeschlagenes Neuladen behält es. | Die UI ändert sich innerhalb einer Sitzung nicht, die bestehenden Klickpfad-Assertions bleiben exakt. |
| D6 | UI-Anbindung über das Interface `AppActions`, als Parameter mit Vorgabewert an `BaupilotAppContent` und `AppShell`. `AppActions.None` behält das M7-Verhalten (`logIn` und `finishSignOff` gelingen sofort, sonst keine Aktion). Nur `BaupilotRoot` übergibt `RootActions`. | Snapshot-, `LinkHost`-, Platzhalter-, Link-Coverage- und Audit-Tests bleiben unverändert. Launcher, Toasts und IO bleiben aus dem Paparazzi-Pfad. |
| D7 | Prüfschritt-Identität aus der Route: `StepKeys.of(route, part?)`. Record-Id `st_` plus die ersten 32 Hex-Zeichen von SHA-256 über `"<projectId>/<stepKey>"`. Weitere Ids: Präfix plus 32 Hex einer zufälligen UUID (`ph_`, `df_`, `dc_`, `sg_`, `pr_`); Mitglieder `m_` plus SHA-256 über `"<projectId>/<userId>"`; Abnahme-Records nutzen die `tradeId` der Übersichtszeile. | Kein neues Modellfeld. Derselbe Schritt auf zwei Geräten trifft dieselbe Zeile, damit gilt die Feld-Zusammenführung. Appwrite-`$id` ist je Tabelle eindeutig, deshalb gehört das Projekt in den Hash. Jede Id hat höchstens 35 Zeichen. |
| D8 | Zusammenführung je Feld mit `FieldStamp(at, by)`; der höhere `(at, by)` gewinnt. Bei gleichem Stamp und verschiedenem Wert gewinnt das lexikographisch größere kanonische JSON. Lokale Stamps: `at = max(clock.millis(), maxSeenAt + 1)` (hybride Uhr). `deleted` ist ein Feld wie jedes andere (Tombstone). | Das ist die Regel „letzte Änderung gewinnt pro Feld mit Zeitstempel“; sie ist kommutativ, idempotent und assoziativ (getestet), die hybride Uhr begrenzt Schäden durch Uhrenabweichung. |
| D9 | Kein `google-services`-Gradle-Plugin. Firebase wird in `BaupilotApplication.onCreate` von Hand mit `FirebaseOptions` aus BuildConfig initialisiert, nur wenn konfiguriert; `FirebaseInitProvider` fehlt im zusammengeführten Manifest. | Das Plugin lässt den Build ohne `google-services.json` scheitern und würde CI, `compileReleaseKotlin` und jeden Testlauf ohne Secrets brechen. |
| D10 | Backend-Konfiguration aus Gradle-Properties oder `android/local.properties`: `baupilot.appwrite.projectId`, `baupilot.fcm.applicationId`, `baupilot.fcm.apiKey`, `baupilot.fcm.projectId`, `baupilot.fcm.senderId`; sie werden BuildConfig-Strings. Der Endpunkt `https://fra.cloud.appwrite.io/v1` ist eine Code-Konstante. | Datenschutz: Frankfurt per Konstruktion. Keine Kennungen im Git. |
| D11 | `AuthSelector.select(config, isDebug)`: konfiguriert → `AppwriteAuth`; unkonfiguriert und Debug → `DemoAuth` (akzeptiert jede Eingabe, M7-Verhalten); unkonfiguriert und Release → `UnconfiguredAuth` (lehnt mit `NotConfigured` ab). Robolectric nutzt `DemoAuth` über `TestBaupilotApplication`. | Die fünf `MainActivity`-UI-Tests melden sich mit leeren Feldern an. Ein Release-Build darf nie stillschweigend jeden zulassen. |
| D12 | Ablehnungen und Fehler zeigen einen System-`Toast` mit festem deutschem Text (Abschnitt „Toast-Texte“). Erfolgreiche Erfassungen geben nur haptische Rückmeldung. | Der Prototyp kennt keine Fehler- oder Erfolgszustände, neue UI im Screen ist verboten. |
| D13 | Appwrite-Zugriffsmodell: drei Rollen-Teams `bauherr`, `architekt`, `handwerker` mit Rechten auf Tabellenebene; `rowSecurity: false`; Projektzugehörigkeit als Tabelle `members` (eine Zeile je Projekt und Nutzer), mit der die Pushes adressiert werden. Isolation je Projekt ist eine bekannte Lücke (R23). | Das nennt der Meilenstein („Teams für Bauherr, Architekt, Handwerker“). Das Datenmodell hat genau ein Projekt. |
| D14 | Kamera über `ActivityResultContracts.TakePicture` in eine `FileProvider`-URI. Keine `CAMERA`-Berechtigung, kein CameraX. GPS-EXIF-Tags werden vor dem Erfassen entfernt. | Die System-Kamera braucht ohne `CAMERA` im Manifest keine Berechtigung. GPS zu entfernen ist die Datenschutz-Vorgabe. |
| D15 | Dokumentimport über `ActivityResultContracts.OpenDocument(["application/pdf"])`. Von der Büroklammer des Gewerk-Screens (`ph.Gewerk.add`): `tradeId` = dieses Gewerk, Art `offer`. Vom FAB der Dokumente (`ph.Dokumente.add`): `tradeId = null`, Art = Art des aktiven Filters, sonst `plan`. Die Datei muss mit `%PDF-` beginnen und höchstens 50 MiB groß sein. | „Mit Gewerk-Zuordnung“ ohne neue UI: Nur der Gewerk-Screen nennt ein Gewerk. Neu klassifizieren („Gewerk oder Typ ändern“) braucht einen Screen, den es nicht gibt (Lücke). |
| D16 | Das Protokoll-PDF schreibt ein reiner Kotlin-PDF-1.4-Writer: Helvetica und Helvetica-Bold, `WinAnsiEncoding`, unkomprimierte Content-Streams, Vektorpfade für Unterschriften. | Deterministische Bytes ohne neue Abhängigkeit, auf der JVM testbar. `android.graphics.pdf.PdfDocument` ist nativ und unter Robolectric nicht belastbar. |
| D17 | `contentSha256` = SHA-256 des kanonischen JSON von `ProtocolDocument` (sortierte Schlüssel, ohne Leerraum, UTF-8); er steht im Fuß jeder Seite und wird mit dem kanonischen JSON gespeichert. `fileSha256` = SHA-256 der PDF-Bytes, im Protokoll-Record gespeichert und synchronisiert. | Ein PDF kann seinen eigenen Datei-Hash nicht enthalten. Den Inhalts-Hash kann jeder aus dem synchronisierten `contentJson` nachrechnen. |
| D18 | Abnahme ohne Unterschrift des Bauherrn wird abgelehnt (Toast „Bitte zuerst unterschreiben.“, keine Navigation, nichts geschrieben). Der Handwerker-Block im PDF zeigt Namen und Label aus dem Fixture nur als Text plus „(keine Unterschrift auf diesem Gerät erfasst)“; die statische Beispielunterschrift wird nie als erfasste gezeichnet. | Ein Protokoll darf keine Unterschrift behaupten, die nicht erfasst wurde. |
| D19 | Bootstrap und Fern-Löschung teilen ein Flag `known:<projectId>`, gesetzt nur, wenn die Engine die Remote-Zeile `projects` beobachtet hat. Flag fehlt, Zeile fehlt, Rolle `bauherr` oder `architekt`: einmalig Upserts aller lokalen Records des Projekts einreihen (eine Transaktion, Projekt zuerst). `handwerker` oder ohne Rolle bootstrappt nie. Flag gesetzt, Zeile fehlt (404): Projekt wurde fern gelöscht → lokal löschen (Records, Ops, Dateien; kein DELETE_PROJECT), `dataVersion` erhöhen. | Ohne Bootstrap sieht die Erinnerungsfunktion die Mängel nie. Ohne Purge behält ein zweites Gerät die Daten und schiebt sie zurück (A3b). Das Flag an eine beobachtete Zeile zu binden verhindert, dass ein gescheiterter Bootstrap als Löschung gilt. |
| D20 | Push-Inhalt enthält nur Zahl und Datum („2 Mängel mit Frist bis 20.12.2026“) und Daten `{target: "defects", projectId}`; keinen Mangel-Titel, keine Adresse, keine Namen. | FCM transportiert außerhalb der EU. Minimaler Inhalt hält personenbezogene Daten in Frankfurt. |
| D21 | Jeder bestehende `testTag` bleibt unverändert. | Tags sind der Link-Coverage-Vertrag (`scripts/link-coverage.json`); `scripts/` liegt außerhalb des Umfangs. |
| D22 | Abmelden löscht die Appwrite-Sitzung, den gecachten Nutzer und das Push-Target und bricht die Sync-Arbeit ab. Lokale Daten und Outbox bleiben (Offline-First; Neuanmeldung setzt den Upload fort). | Wipe beim Abmelden würde nicht synchronisierte Erfassungen verlieren. Wipe auf geteilten Geräten ist eine Lücke (R25). |
| D23 | WorkManager wird bei Bedarf initialisiert (`open class BaupilotApplication : Configuration.Provider` mit `protected open fun createServices(): AppServices`, Standard-`WorkManagerInitializer` entfernt). Robolectric nutzt `TestBaupilotApplication` über `src/test/resources/robolectric.properties`. | Vermeidet Auto-Initialisierung in vielen Robolectric-Klassen und den Konflikt „already initialized“ mit `WorkManagerTestInitHelper`. |
| D25 | Die Rolle kommt aus Appwrite-Teams: Nach dem Login listet `AuthGateway` die Teams und speichert die erste von `bauherr`, `architekt`, `handwerker` als `cachedRole` (null, wenn keine; dann kein `ensureMember`). | `ensureMember(userId, role)` und die Bootstrap-Regel (D19) brauchen sie. |
| D26 | Remote-Zeilen sind nicht vertrauenswürdig. Ein Record, dessen `fixtureFields` das strikte Dekodieren nicht besteht, wird beim Lesen übersprungen (protokolliert, gezählt) statt `AppData.load` scheitern zu lassen. Nur das gebündelte Asset und die statischen D3-Inhalte lösen `RepositoryException.Decoding` aus. | Eine fehlerhafte Zeile eines fehlerhaften Clients darf die App nicht auf allen Geräten leeren. |
| D-I1 | Das Benachrichtigungs-Icon heißt `res/drawable/stat_reminder.xml` statt `ic_stat_reminder.xml` (Manifest und `ReminderNotifier`). | `scripts/gen-icons --check` und `IconsTest.everyIconResourceIsAVectorDrawable` behandeln jede `ic_*.xml` in `res/drawable` als generiert und melden eine Datei, die nicht in `scripts/icons.json` steht (`scripts/` ist gesperrt). Ohne `ic_`-Präfix ist die Datei kein generiertes Icon. Gefunden in Step 15. |
| D-I2 | `TestBaupilotApplication` legt den `FileStore` in `filesDir` statt `cacheDir/baupilot-test-files` an. | Die `FileProvider`-Konfiguration (`file_paths.xml`, `files-path projects/`) deckt wie in Produktion nur `filesDir/projects/` ab; mit `cacheDir` warf `getUriForFile` eine `IllegalArgumentException`, die Kamera startete nie, und vier `CaptureFlowUiTest`-Fälle liefen in den 5-s-Timeout. Gefunden in Step 15. |
| D-B1 | Appwrite-SDK `io.appwrite:sdk-for-android` 25.2.0 statt 31.4.0. | Alle Versionen ab 26.0.0 tragen `minCompileSdk=37` (gemessen an `META-INF/com/android/build/gradle/aar-metadata.properties` der AARs) und ziehen `core-ktx` 1.19.x, `okhttp` 5.5.x und `lifecycle-runtime-compose` 2.11.0. `checkDebugAarMetadata` bricht dann mit 5 Befunden ab; Plattform 37 ist nicht installiert, und ein höheres `compileSdk` samt Compose-nahen Upgrades widerspräche D21 und R2 (kein Pixel bewegen). 25.2.0 ist die höchste Version mit `minCompileSdk=36`; sie enthält `Account.createEmailPasswordSession`, `createPushTarget`, `updatePushTarget`, `deletePushTarget` und `Teams.list` (am Klassenfile geprüft). |
| D-B2 | KSP 2.3.14 läuft unter AGP 9.4.1 mit eingebautem Kotlin; der Fallback `android.builtInKotlin=false` aus §5.0 wurde nicht gebraucht, `android/gradle.properties` bleibt unverändert. | `:app:kspDebugKotlin` und `:app:kspReleaseKotlin` laufen, `ThrowawayDatabase_Impl.kt` wird erzeugt, `app/schemas/de.baupilot.data.local.ThrowawayDatabase/1.json` wird geschrieben. |
| D-B3 | `androidx.core:core-ktx` ist auf 1.18.0 gepinnt (der Stand aus `:app:dependencies` vor dem Eingriff). `androidx.exifinterface` 1.4.1. | Kein Upgrade, das Compose berühren könnte. |
| D-B4 | BuildConfig-Felder: `APPWRITE_PROJECT_ID`, `FCM_APPLICATION_ID`, `FCM_API_KEY`, `FCM_PROJECT_ID`, `FCM_SENDER_ID`; Quelle Gradle-Property, dann `android/local.properties`, sonst `""` (D10). `BackendConfig.fromBuildConfig()` liefert `fcm = null`, solange eines der vier FCM-Felder leer ist. | Ein unvollständiges FCM-Objekt darf nie zu `FirebaseOptions` werden. |

## SDK-Prüfung (javap)

Befehl: `javap -cp classes.jar io.appwrite.services.<Klasse>` (und `io.appwrite.Query$Companion`) auf dem `classes.jar` aus `sdk-for-android-25.2.0.aar` (die Version aus `android/gradle/libs.versions.toml`, siehe D-B1; die 31.4.0 aus dem Plan wird nicht verwendet). `Query.or` und `Query.and` sind vorhanden (R28 ausgeräumt). Bestätigte Signaturen (Kotlin-Sicht, `suspend` = letzter Parameter `Continuation`):

| Klasse | Methode | Signatur |
|---|---|---|
| `TablesDB` | `getRow` | `(databaseId, tableId, rowId, queries: List<String>? = null, transactionId: String? = null)` → `Row<Map<String, Any>>`; Überladung mit `Class<T>` → `Row<T>` |
| `TablesDB` | `upsertRow` | `(databaseId, tableId, rowId, data: Any? = null, permissions: List<String>? = null, transactionId: String? = null)` → `Row<Map<String, Any>>`; Überladung mit `Class<T>` |
| `TablesDB` | `listRows` | `(databaseId, tableId, queries: List<String>? = null, transactionId: String? = null, total: Boolean? = null, ttl: Long? = null)` → `RowList<Map<String, Any>>`; Überladung mit `Class<T>` |
| `TablesDB` | `deleteRow` | `(databaseId, tableId, rowId, transactionId: String? = null)` → `Any` |
| `Storage` | `createFile` | `(bucketId, fileId, file: InputFile, permissions: List<String>? = null, onProgress: ((UploadProgress) -> Unit)? = null)` → `File` |
| `Query.Companion` | `or`, `and` | `(queries: List<String>)` → `String` |
| `Query.Companion` | `greaterThan`, `equal` | `(attribute: String, value: Any)` → `String` |
| `Query.Companion` | `orderAsc`, `limit` | `(attribute: String)` bzw. `(limit: Int)` → `String` |
| `Account` | `createEmailPasswordSession` | `(email, password)` → `Session` (`userId`) |
| `Account` | `deleteSession` | `(sessionId)` → `Any` (`"current"` für die aktuelle Sitzung) |
| `Account` | `createPushTarget` | `(targetId, identifier, providerId: String? = null)` → `Target` |
| `Account` | `updatePushTarget` | `(targetId, identifier)` → `Target` |
| `Account` | `deletePushTarget` | `(targetId)` → `Any` |
| `Teams` | `list` | `(queries: List<String>? = null, search: String? = null, total: Boolean? = null)` → `TeamList<Map<String, Any>>` (`teams[].id`, `teams[].name`) |

Ausnahmen: `AppwriteException(message, code: Int?, type, response)` mit `getCode()`; Netzfehler kommen als `IOException` aus OkHttp. Die Parameternamen sind aus dem Bytecode nicht lesbar (`javap` ohne `-l`); die Reihenfolge und Typen sind es.

## Bekannte Lücken

Je Eintrag: Ursache, Wirkung und was sie schließen würde. Messwerte stehen unter „Testzahlen“.

- **R8 (offen, bekannte Lücke):** Appwrite kennt kein bedingtes Update. Zwischen `getRow` und `upsertRow` kann ein fremder Schreibzugriff ein Feld auf dem Server überschreiben; der nächste Pull und die Zusammenführung des anderen Geräts schicken dessen neuere Stamps erneut. Schließen würde es ein serverseitiger Merge in einer Funktion.
- **R9 (offen):** Die hybride Uhr (`maxSeenAt + 1`) begrenzt Uhrenabweichungen; ein Rest von wenigen Sekunden bleibt.
- **R21 / D4 (offen):** Die Anzeige-„Heute“ bleibt `meta.now` (2026-12-18). Echtes „heute“ in der Darstellung ist eine eigene Aufgabe.
- **R22 (offen):** Erfasste Ergebnisse ändern Status, Fortschritt und Zähler in der UI nicht (UI eingefroren, Lesemodelle fixture-förmig). Neue Daten erscheinen bei Mängeln und Dokumenten beim nächsten `ON_START` (D5), nicht in Statusanzeigen.
- **R23 / D13 (offen):** Keine Isolation je Projekt in Appwrite (`rowSecurity: false`, Teams nach Rolle). Schließen: Teams je Projekt plus `rowSecurity`.
- **R24 (offen):** Importierte oder von anderen Geräten stammende Dateien lassen sich in der App nicht herunterladen oder ansehen (kein Viewer-Screen im Prototyp).
- **R25 / D22 (offen):** Abmelden behält lokale Daten und Outbox. Ein Wipe auf geteilten Geräten fehlt.
- **R26 (offen):** Wird das einzige Projekt gelöscht, bleibt die App auf ihrem leeren Ladehintergrund (`NoProject`). Das geht nur per API, es gibt keinen UI-Auslöser, und der Prototyp hat keinen Leerzustand.
- **R30 / D19 (offen):** Ein Push, der beim Löschen schon läuft, kann bis zum nächsten Drain verwaiste Zeilen anlegen.
- **R31 / D24 (offen, Bestätigung durch den Nutzer):** Die Platzhalter-Abweichung von `CLAUDE.md` ist nicht bestätigt. Rücknahme ändert nur `RootActions` und die Parameter mit Vorgabewert.
- **R1 (bekannt, aus M7):** `FlBadLightSnapshotTest` und `FlBadDarkSnapshotTest` bleiben rot (Basis-Lücke, nicht von M8 verursacht).
- **R4 / R20 (aus M7, offen):** Der manuelle Accessibility-Scanner-Lauf bleibt ein Operator-Schritt (`docs/m7-android-offene-punkte.md`).
- **Operator-Läufe nicht ausgeführt (A2, A3b, T1):** Der manuelle Flugmodus-Test (A2), die End-to-End-Löschung (A3b), die Apple-OAuth-Einrichtung (T1), die Zustellung der Erinnerung per FCM an ein Gerät und das Anlegen des Appwrite-Projekts in Frankfurt brauchen Konten des Nutzers. Dafür stehen die Schritte unter „Operator-Checkliste“. Bis dahin sind A2 und A3b nur durch Unit- und Robolectric-Tests belegt (`OfflineSyncTest`, `SyncEngineTest`, `CaptureRepositoryTest`, `cleanup.test.mjs`).

## Toast-Texte

Nicht aus dem Prototyp (D12). Quelle: `RootToasts` in `android/app/src/main/java/de/baupilot/app/RootActions.kt`.

| Auslöser | Text |
|---|---|
| Login: falsche Zugangsdaten | „Anmeldung fehlgeschlagen. E-Mail und Passwort prüfen.“ |
| Login: offline | „Keine Verbindung. Die Anmeldung braucht Internet.“ |
| Login: zu viele Versuche | „Zu viele Versuche. Bitte später erneut versuchen.“ |
| Login: Backend nicht konfiguriert (Release ohne Properties) | „Backend nicht eingerichtet.“ |
| Login: unbekannter Fehler | „Anmeldung fehlgeschlagen.“ |
| Abnahme ohne Unterschrift | „Bitte zuerst unterschreiben.“ |
| Import: keine PDF-Datei | „Nur PDF-Dateien können importiert werden.“ |
| Import: größer als 50 MiB | „Die Datei ist größer als 50 MB.“ |
| Speicherfehler, fehlende Kamera- oder Dokument-App, unbekanntes Projekt | „Speichern fehlgeschlagen.“ |

## Testzahlen

Beobachtet in Step 15 am Arbeitsbaum (Basis `608e6d3`); die Quelle steht je Zeile. Gradle-Quelle: `android/app/build/test-results/testDebugUnitTest/*.xml` (153 Ergebnisdateien) und das Log des Laufs; Befehl: `(cd android && ./gradlew --continue :app:testDebugUnitTest :app:compileReleaseKotlin)`.

| Block | Ergebnis | Quelle |
|---|---|---|
| `(cd backend/appwrite && node --test test/)` | 27 Tests, 27 grün, 0 rot | Befehl |
| `(cd scripts && node --test test/)` | 141 Tests, 141 grün, 0 rot | Befehl |
| Android `:app:testDebugUnitTest` (Gesamtlauf) | 925 Tests, 923 grün, 2 rot, 0 Fehler, 0 übersprungen; Gradle: `925 tests completed, 2 failed`, `BUILD FAILED in 20m 2s` | Gradle-Log, Ergebnis-XML (Summe der Attribute `tests`, `failures`, `errors`, `skipped`) |
| Rot | ausschließlich `FlBadLightSnapshotTest` und `FlBadDarkSnapshotTest` (`flBad`) | Gradle-Log (`… > flBad FAILED`) |
| `:app:compileReleaseKotlin` | erfolgreich | Gradle-Log |
| Baseline aus Step 0 (Abschnitt „Baseline“) | 738 Tests, 2 rot; Differenz 187 Tests aus den Steps 2 bis 14 | Step 0, Ergebnis-XML |
| Snapshot-Klassen `de.baupilot.snapshot.*SnapshotTest` | 71 Testfälle, 69 grün, 2 rot (FlBad hell/dunkel) | Ergebnis-XML |
| `scripts/diff-report --all --platform android` | Exit 0: 68 Zeilen, 66 grün, 2 rot, 0 fehlt; rot: FlBad hell (Test-Wert 0.586558 %) und dunkel (0.576402 %) bei Grenze 0.5 | `reports/summary-android.json` (`totals`, `rows[].test`) |
| `de.baupilot.sync.*` | 43 Tests, 0 rot (davon `SyncEngineTest` 23, `OfflineSyncTest` 1) | Ergebnis-XML |
| `de.baupilot.data.local.*` | 13 Tests, 0 rot (`LocalRepositoryTest` 13) | Ergebnis-XML |
| `de.baupilot.capture.*` | 33 Tests, 0 rot | Ergebnis-XML |
| `de.baupilot.protocol.*` | 14 Tests, 0 rot | Ergebnis-XML |
| `de.baupilot.backend.*` | 24 Tests, 0 rot | Ergebnis-XML |
| `de.baupilot.push.*` | 9 Tests, 0 rot | Ergebnis-XML |
| `de.baupilot.app.*UiTest` | 138 Tests, 0 rot (`CaptureFlowUiTest` 11, `M8ActionsUiTest` 26, `LinkCoverageUiTest` 19, `PlaceholderUiTest` 29) | Ergebnis-XML |
| `*Accessibility*` (Audit hell und dunkel) / `*TouchTarget*` | 68 / 34 Tests, 0 rot | Ergebnis-XML |

Der erste Gesamtlauf in Step 15 (vor den zwei Korrekturen D-I1 und D-I2) hatte 925 Tests und 7 rot: die zwei FlBad-Tests, `IconsTest.everyIconResourceIsAVectorDrawable` und vier `CaptureFlowUiTest`-Fälle (`cameraCancelledDeletesFile`, `stepPhotoLaunchesCameraWithFileProviderUri`, `defectSaveThenCancelDraftCleanup`, `cameraResultOkRecordsPhoto`); Quelle: Gradle-Log dieses Laufs. Die vier Fälle schlugen auch einzeln gestartet fehl (kein Flake). Nach den Korrekturen ist der Gesamtlauf wie oben.

Nicht ausgeführt (kein Agentenlauf möglich, siehe „Operator-Checkliste“): Flugmodus-Test A2, Lösch-Ende-zu-Ende A3b, Apple-OAuth, FCM-Zustellung an ein Gerät, Accessibility-Scanner.

## Befehle

Aus dem Repo-Root. Android: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"` (JBR aus Android Studio), `ANDROID_HOME=$HOME/Library/Android/sdk`; fehlt `android/local.properties` (git-ignoriert), wird sie mit `sdk.dir=…` angelegt (Rezept: `docs/m7-android-offene-punkte.md`, „Befehle“). Gradle immer mit `--continue`.

```sh
(cd backend/appwrite && node --test test/)
(cd scripts && node --test test/)
scripts/gen-tokens --check && scripts/gen-icons --check && scripts/lint-hex && scripts/design-freeze --check
(cd android && ./gradlew --continue :app:testDebugUnitTest :app:compileReleaseKotlin)
scripts/diff-report --all --platform android
git diff --stat 608e6d3 -- ios design fixtures scripts README.md docs/milestones docs/plans CLAUDE.md                 # leer (I10)
git diff --stat 608e6d3 -- android/app/src/test/goldens android/app/src/test/java/de/baupilot/snapshot             # leer (I1)
git diff 608e6d3 -- android/app/src/test/java/de/baupilot/app/PlaceholderUiTest.kt scripts                           # leer (I2)
git grep -n "baupilot.fcm.apiKey=" -- ':!*.md'                                                                      # leer (I16)
```

Gezielte Läufe (je mit `--tests`): `'de.baupilot.sync.*' 'de.baupilot.data.local.*' 'de.baupilot.capture.*' 'de.baupilot.protocol.*'`, `'de.baupilot.app.*UiTest'`, `'de.baupilot.snapshot.*SnapshotTest'`.

Konfiguration des Debug-Builds gegen ein echtes Backend, in `android/local.properties` (nicht committen): `baupilot.appwrite.projectId`, `baupilot.fcm.applicationId`, `baupilot.fcm.apiKey`, `baupilot.fcm.projectId`, `baupilot.fcm.senderId`.

## Operator-Checkliste

Braucht die Konten des Nutzers; ein Agent führt sie nicht aus. Nicht ausgeführt (Stand dieses Dokuments).

1. Appwrite-Cloud-Projekt in der **Region Frankfurt** anlegen. Android-Plattform mit Paket `de.baupilot` hinzufügen.
2. Die Projekt-Id in `backend/appwrite/appwrite.config.json` eintragen, lokal oder in einem vom Nutzer freigegebenen Commit. Dann: `cd backend/appwrite && appwrite login && appwrite push settings && appwrite push tables && appwrite push buckets && appwrite push teams && appwrite push functions`.
3. Firebase-Projekt: Android-App `de.baupilot` hinzufügen. Die vier Werte als `baupilot.fcm.*` in `android/local.properties` eintragen. In der Appwrite-Konsole den Messaging-FCM-Provider mit der Id **`fcm`** aus der Service-Account-JSON anlegen.
4. Apple-OAuth-Provider (gemeinsam mit iOS): Zugangsdaten in der Konsole eintragen.
5. Die Testnutzer zu Team `bauherr`, `architekt` oder `handwerker` hinzufügen.
6. `baupilot.appwrite.projectId` in `android/local.properties` setzen, dann den Debug-Build auf `Medium_Phone_API_36.1` oder einem Gerät installieren.
7. **Flugmodus-Test (A2):**
   - Online anmelden.
   - Flugmodus einschalten.
   - Auf dem Fenster-Schritt F8 „In Ordnung“ samt Foto erfassen und einmal eine Messung speichern.
   - App beenden.
   - Flugmodus ausschalten.
   - Innerhalb weniger Minuten erscheinen die Zeilen `steps` und `photos` und die Datei im Bucket in der Konsole, und die Outbox ist leer (`adb shell run-as de.baupilot sqlite3 databases/baupilot.db 'select count(*) from outbox'`).
8. **Lösch-Ende-zu-Ende (A3b):**
   - `deleteProject` aus einem reinen Debug-Pfad auslösen (optional ein `adb shell am broadcast`-Receiver in `src/debug`) oder die Zeile `projects` in der Konsole löschen.
   - Prüfen, dass alle Zeilen des Projekts und alle Dateien unter `<projectId>/` in beiden Buckets weg sind.
9. `deadline-reminders` einmal manuell in der Konsole auslösen und prüfen, dass die Benachrichtigung auf dem Gerät ankommt. Ein Tipp darauf öffnet die Mängel-Ansicht.
10. D24 prüfen und bestätigen oder ablehnen (Platzhalter-Abweichung von `CLAUDE.md`).

## Verifikation

Step 15, Arbeitsbaum von Branch `feat/implement-the-plan` (Basis `608e6d3`), alle Befehle aus dem Repo-Root, `JAVA_HOME` auf das JBR aus Android Studio, `ANDROID_HOME=$HOME/Library/Android/sdk`.

| Befehl | Ergebnis |
|---|---|
| `(cd backend/appwrite && node --test test/)` | 27 Tests, 27 grün |
| `(cd scripts && node --test test/)` | 141 Tests, 141 grün |
| `scripts/gen-tokens --check` | Exit 0 |
| `scripts/gen-icons --check` | Exit 0 (nach D-I1; vorher Exit 1 wegen `ic_stat_reminder.xml`) |
| `scripts/lint-hex` | Exit 0 („sauber“) |
| `scripts/design-freeze --check` | Exit 0 (276 Dateien) |
| `(cd android && ./gradlew --continue :app:testDebugUnitTest :app:compileReleaseKotlin)` | `:app:testDebugUnitTest` FAILED: 925 Tests, 2 rot (nur `FlBadLightSnapshotTest`, `FlBadDarkSnapshotTest`); `:app:compileReleaseKotlin` erfolgreich; 20 min 2 s |
| `scripts/diff-report --all --platform android` | Exit 0: 68 Zeilen, 66 grün, 2 rot, 0 fehlt (dieselben zwei roten wie in der Baseline) |
| `git diff --stat 608e6d3 -- ios design fixtures scripts README.md docs/milestones docs/plans CLAUDE.md` | leer (I10) |
| `git diff --stat 608e6d3 -- android/app/src/test/goldens android/app/src/test/java/de/baupilot/snapshot` | leer (I1) |
| `git diff 608e6d3 -- android/app/src/test/java/de/baupilot/app/PlaceholderUiTest.kt scripts` | leer (I2) |
| `git grep -n "baupilot.fcm.apiKey=" -- ':!*.md'` | leer (I16) |

### Abnahme (Plan §7)

| Kriterium | Stand | Beleg |
|---|---|---|
| A1 Snapshots bleiben grün | erfüllt bis auf die bekannte FlBad-Lücke: 66 von 68 grün, dieselben zwei roten wie in der Baseline; Goldens, Referenzen und `snapshot/`-Tests unverändert | `diff-report`, I1-Diff |
| A2 Flugmodus-Test | **nicht ausgeführt** (braucht Gerät und konfiguriertes Backend, Operator-Schritt 7); durch `OfflineSyncTest` und `SyncEngineTest` (grün) nur im Test belegt | Ergebnis-XML |
| A3a Frankfurt | erfüllt in Code und Konfiguration: `BackendConfigTest` und `config.test.mjs` grün; das Projekt in Frankfurt anzulegen ist Operator-Schritt 1 | Tests |
| A3b Löschen entfernt alle Dateien | lokal und im Funktionstest erfüllt (`CaptureRepositoryTest`, `SyncEngineTest`, `cleanup.test.mjs` grün); **Ende-zu-Ende nicht ausgeführt** (Operator-Schritt 8) | Tests |
| T1 | Konfiguration und Schema geprüft (`config.test.mjs`, `SyncSchemaTest`); Apple-Provider: Operator | Tests |
| T2 bis T8 | durch die Tests der Zuordnung in Plan §7 belegt, alle grün im Gesamtlauf | Ergebnis-XML |

### Abweichungen vom Plan

- D-B1: SDK 25.2.0 statt 31.4.0 (Step 1).
- D-I1: Icon ohne `ic_`-Präfix (Step 15).
- D-I2: `TestBaupilotApplication` nutzt `filesDir` (Step 15).
- D24: bewusste Abweichung von `CLAUDE.md`, Bestätigung durch den Nutzer offen.
