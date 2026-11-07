# M0 – Offene Punkte

Die Punkte 1–12 sind die Liste aus dem Plan (S10). Alles, was bei der Umsetzung fehlgeschlagen ist oder übersprungen wurde, hängt der Integrationsschritt im Abschnitt „Verifikation“ an.

1. 21 von 26 Dokumenten sind in der Dokumente-Liste nicht benannt (nur Zählwerte). Ob die drei auf den Prüfungs-Screens benannten Dokumente zu den 26 gehören, ist unbekannt (D9).
2. Variantenbenennung pro Screen statt semantischer Namen (D4), z. B. `Tokens.track.abnPruefung`, `Tokens.border.login`.
3. Die Zuordnung Elektriker → Elektro und Installateur → Sanitär & Heizung ist eine Schlussfolgerung (D10).
4. Tests laufen auf iOS 26.2; es ist keine iOS-17-Runtime installiert (D14). Das Deployment Target bleibt 17.0.
5. Paparazzi ist eine Alpha-Version (`2.0.0-alpha05.1`, D15). Genommener Fallback: Compose-BOM auf `2026.06.01` gepinnt (Compose 1.11.4), weil die neuere BOM `compileSdk` 37 verlangt und nur die SDK-Plattformen 34 und 36 installiert sind (`compileSdk` bleibt 36). AGP-9.0.x-Fallback: keiner genommen.
6. Das Tag `design-v1` und der Branch sind nicht gepusht; CI wurde auf GitHub noch nicht beobachtet.
7. Die beiden Zip-Dateien (`baupilot-design-paket.zip`, `baupilot-milestones.zip`) sind weiterhin im Wurzelverzeichnis getrackt.
8. Die Login-Beispiele sind bis M2 erwartet rot (D17).
9. Elemente 7/12 gegenüber Signatur 12/11/1 sowie Detail-Daten gegenüber „Geplant“ sind im Design widersprüchlich und werden wörtlich übernommen (R5).
10. Intel-lokales und arm64-CI-Rendering können voneinander abweichen.
11. Versionswahl: AGP 9.4.1 bleibt, obwohl 9.5.1 existiert; Roboto v3.015 bleibt, obwohl v3.016 existiert; welcher Roboto-Build für die Referenzbilder verwendet wurde, ist nicht angegeben (das Design-README vermerkt, dass „≥“ auf eine Ersatzschrift ausweicht – relevant für Snapshots ab M1).
12. iOS-Fixture-Bündelung (S7): als Einzeldatei, nicht als Ordnerreferenz `fixtures`. In `ios/project.yml` steht `- path: ../fixtures/baupilot.json` mit `buildPhase: resources` unter den `sources` des App-Targets; die Datei liegt im gebauten `Baupilot.app`. Die Ordner-Variante (`type: folder`) war nicht nötig.

## Verifikation (integration)

Alle Schritte liefen am 2026-11-07 im Worktree. Es wurde nichts repariert; es gab keinen trivialen Integrationsbruch.

| # | Befehl | Exit | Ergebnis |
|---|--------|------|----------|
| 1 | `cd scripts && node --test test/` | 0 | pass – 54 Tests, 54 pass, 0 fail, 0 skipped |
| 2 | `scripts/gen-tokens`, dann `scripts/gen-tokens --check`, dann `git status --porcelain` | 0 / 0 | pass – `git status --porcelain` nach `gen-tokens` und nach `--check` ist zeilengleich mit dem Stand davor (79 Zeilen, alle `A`/` M`/` A`; nur die vorhandenen Änderungen dieses Runs, keine neue Datei und keine neue Änderung) |
| 3 | `scripts/design-freeze --check` | 0 | pass – „design/ matches scripts/design-v1.sha256 (276 files)“ |
| 4 | `git diff -M --name-status 56a50fa design-v1 -- design \| grep -v '^R100'` | – | **wörtlich ausgeführt nicht leer**, siehe Hinweis unten; inhaltlich pass |
| 5 | `git ls-files \| grep 'baupilot.json'` | 0 | pass – genau eine Zeile: `fixtures/baupilot.json` |
| 6 | `git ls-files \| grep -c DS_Store` | – | pass – `0` |
| 7 | `cd ios && xcodegen generate && xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15'` | 0 | pass – `Executed 16 tests, with 0 failures (0 unexpected)`, `** TEST SUCCEEDED **` |
| 8 | `cd android && JAVA_HOME=<sdkman 21.0.8-tem> ./gradlew :app:testDebugUnitTest` (vorher `rm -rf app/build/paparazzi/failures`) | 0 | pass – `BUILD SUCCESSFUL in 10s` |
| 9 | `scripts/diff-report --platform ios` / `--platform android` | 0 / 0 | pass – `Login_light: 773338 px (26.1048 %)` (iOS), `Login_light: 305276 px (10.3049 %)` (Android); beide `reports/<platform>/Login_light.png` zeigen drei Panels: Referenz, Ist, Diff (rot) |
| 10 | `ruby -ryaml -e 'YAML.load_file(".github/workflows/ci.yml"); puts "ok"'` | 0 | pass – `ok` |
| 11 | Invarianten | – | I3 pass (Schritt 3). I8 pass – `find ios android -name 'Login_light*.png'` findet nichts. I5 pass – `ios/project.yml:17: - path: ../fixtures/baupilot.json`, `android/app/build.gradle.kts:48: inputs.file(File(repoRoot, "fixtures/baupilot.json"))` |

### Hinweise, Fallbacks, Übersprungenes

- **Schritt 4, wörtlicher Befehl:** `git diff -M … -- design` gibt 277 Zeilen mit `A` aus. Der Grund ist die Pfadeinschränkung: In `56a50fa` lag das Design noch unter `baupilot/design/`, und die Rename-Erkennung findet die Quelle nicht, wenn sie außerhalb des Pfadfilters liegt. Ohne Pfadfilter (`git diff -M --name-status 56a50fa design-v1 | grep 'design/' | grep -v '^R100'`) bleiben nur `D baupilot/design/.DS_Store` und `D baupilot/design/reference/.DS_Store` übrig; die 276 übrigen Dateien sind `R100`, also unverändert. Der Plan-Befehl sollte ohne `-- design` oder mit `-- baupilot/design design` laufen.
- **Schritt 4, Tag:** `design-v1` zeigt auf `e147ec2`, `HEAD` ist `609b7f2`. `git diff design-v1 HEAD -- design` ist leer.
- **Schritt 7, iOS:** Das Login-Beispiel meldet einen erwarteten Fehler (kein Error): `XCTExpectFailure: matcher accepted … The percentage of pixels that match 0.8338623 is less than required 0.995`, danach `testLoginLightAgainstReference … passed`. Simulator-Runtime ist iOS 26.2 (Punkt 4 oben).
- **Schritt 8, JDK:** Es gibt kein `~/.sdkman`; die JDKs liegen unter `/usr/local/opt/sdkman-cli/libexec/candidates/java/` (21.0.8-tem, 25.0.4-tem). Verwendet wurde `JAVA_HOME=/usr/local/opt/sdkman-cli/libexec/candidates/java/21.0.8-tem`. Das System-`java` ist 25.0.4.
- **Schritt 8, Tests:** MockRepositoryTest 9, BaupilotFontsTest 2, TokensTest 3, ReferenceCanarySnapshotTest 1, ReferenceSnapshotHandlerTest 4: alle ohne Fehler. `LoginExampleSnapshotTest`: 1 Test, **skipped** (`AssumptionViolatedException: Erwartet rot bis M2: Images differ (by 6,126919%)`), wie erwartet. Der Gradle-Lauf war fast vollständig `UP-TO-DATE` (1 von 26 Tasks ausgeführt), die Ergebnis-XML stammen aber aus diesem Lauf.
- **Schritt 9:** `reports/` ist in `.gitignore`. Exit 1 (fehlende Referenz) trat nicht auf.
- Die Login-Beispiele sind bis M2 erwartet rot (Punkt 8 oben). Die Differenzen zur Referenz (iOS 26,1 %, Android 10,3 %) sind sichtbar: Icons und Eingabefeld-Inhalte fehlen, der Platzhaltertext „Baupilot“ liegt mittig über dem Screen.
- Alle weiteren Fallbacks sind in den Punkten 4, 5, 10, 11 und 12 oben aufgeführt; in der Integration kam keiner dazu.
- Nichts übersprungen außer dem erwarteten Android-Login-Beispiel; nichts fehlgeschlagen.
