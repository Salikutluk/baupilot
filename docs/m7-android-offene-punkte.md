# M7 Android · Offene Punkte

Dieses Dokument ist das einzige Dokument des M7-Android-Umfangs (Plan D-R3). `README.md`, `docs/m7-offene-punkte.md`, `docs/m5-*` und `docs/m6-*` bleiben unverändert. Jede Zahl steht mit Quelle da: `build/m7/android/notes/*.md` (`step0.md`, `step2.md`, `step6-spike.md`, `levers.md`, `exit-table.md`, `large-text-*.md`), `reports/summary-android.{json,md}` oder ein in diesem Lauf erneut ausgeführter Befehl (benannt). Der Stand ist der Arbeitsbaum von Branch `worca-cc/implement-the-plan-47b609b7` (Basis `048cd7c`); die Schritte 0–9 liegen dort noch nicht als Commits vor (Quelle: `step2.md`, Hinweis zu p2t1/p2t2). Der Abschnitt „Verifikation“ trägt den Lauf von Step 11; der Abschnitt „Nachbesserung nach Review (Zyklus 2)“ am Ende nennt, was danach geändert wurde und gilt anstelle der älteren Zahlen.

## Baseline

### Step 0 · Preflight (Quelle: `build/m7/android/notes/step0.md`)

Gemessen auf `048cd7c` (Branch pristine, `git status --porcelain` vor und nach dem Lauf leer). `android/local.properties` fehlte und wurde (git-ignoriert) mit `sdk.dir=$HOME/Library/Android/sdk` angelegt. JDK = JBR aus Android Studio. Nichts repariert, keine Toleranz und keine Referenz geändert.

| Block | Ergebnis |
|---|---|
| `(cd scripts && node --test test/)` | 114 Tests, 114 grün, 0 rot (6.1 s) |
| `gen-tokens --check`, `gen-icons --check` | Exit 0 |
| `lint-hex` | sauber |
| `design-freeze --check` | 276 Dateien stimmen mit `design-v1.sha256` |
| Android `:app:testDebugUnitTest :app:compileReleaseKotlin` (`--continue`) | 483 Tests, 427 grün, 56 rot, 0 Fehler, 0 übersprungen; `compileReleaseKotlin` erfolgreich |
| Screen-Snapshots | 68 Zeilen, 12 grün, 56 rot |
| Dauer des Gradle-Laufs | 19m17s (erster Versuch brach am 10-min-Hintergrundlimit ab, zweiter mit 60-min-Timeout lief durch) |
| `scripts/diff-report --platform android` | 68 Zeilen nach `reports/android/summary.json`, 2m04s, 0 `sizeMismatch` |

Rot sind ausschließlich Screen-Snapshots (`*LightSnapshotTest` / `*DarkSnapshotTest`); kein Nicht-Snapshot-Test und kein `Gallery*`-Test. Grün sind: DokViewer, Gewerk, Login, MaUebersicht (je hell und dunkel) sowie ElUebersicht, FlUebersicht, Home, Zeitplan (nur dunkel); dazu `GalleryLight/Dark`, `GoldenSnapshotHandlerTest` (5), `ReferenceSnapshotHandlerTest` (5) und `ReferenceCanarySnapshotTest`.

Vergleich mit `git show fe905f7:docs/m7-offene-punkte.md` (Android-Zeilen: Gradle 483/427/56/0, Screens 68/12/56, Grün-Menge, 68 Paparazzi-Differenzwerte auf 1e-6, 68 diff-report-% auf 1e-4): **keine Unterschiede.** Die Baseline ist zur dokumentierten reproduziert (Plan R-c: JDK 21.0.9 statt 21.0.8, ohne Folge).

### Step 2 · Doppellauf und Kalibrierung (Quelle: `build/m7/android/notes/step2.md`)

Befehl je Lauf: `(cd android && ./gradlew --continue :app:testDebugUnitTest --tests 'de.baupilot.snapshot.*SnapshotTest')`, danach `scripts/diff-report --all --platform android`.

| Lauf | Gradle | Dauer | Tests | diff-report | Exit |
|---|---|---|---|---|---|
| 1 | Exit 1 (erwartet, rote Screens) | 16m 30s (991 s) | 71, 56 rot | 12 grün, 56 rot, 0 fehlt | 0 |
| 2 | Exit 1 (erwartet) | 14m 02s (843 s) | 71, 56 rot | 12 grün, 56 rot, 0 fehlt (`--baseline build/m7/android/baseline/summary.json`) | 0 |

Lauf 1 ergab `build/m7/android/baseline/summary.json` (68 Zeilen). Lauf 2 meldet `changed: []`; daraus folgt `build/m7/android/baseline/unstable.json` = `{ "changed": [] }`. Hash, Testverdikt, Testwert, `percentDifferent` und `offByTwoPercent` aller 68 Zeilen sind in beiden Läufen identisch: die Läufe sind stabil.

**Kalibrierung** (`offByTwoPercent` gegen den Sidecar-`value`): 56 rote Zeilen, größte Abweichung 5.17e-7 (Grenze 0.001), 0 Ausreißer; 12 grüne Zeilen alle ≤ 0.5, höchster Wert 0.486722 (ElUebersicht, dunkel). Bestanden. Vergleich mit Step 0 und mit `fe905f7`: keine Unterschiede; der Rundungsunterschied 0.843285 / 0.843286 (ElRauchmelder hell) liegt im Ausgabeformat von `fe905f7`, nicht im Wert.

## Übernahme aus `fe905f7`

D-R1: Es wurde nicht gemergt (`feat/m7` löscht `docs/plans/…` und `docs/milestones/…` und trägt `ios/`-Änderungen), sondern pfadweise mit `git checkout fe905f7 -- <Pfade>` übernommen. Die Liste ist `git diff --name-only 048cd7c fe905f7 -- android scripts`, in diesem Lauf erneut erzeugt: 57 Dateien (25 unter `android/app/src/main`, 23 unter `android/app/src/test`, 9 unter `scripts/`). Jede Datei wurde in genau einem Schritt übernommen:

| Schritt | Dateien (übernommen aus `fe905f7`) |
|---|---|
| 1 · Toleranz, Sidecar, Gesamtbericht | `scripts/diff-report.mjs`, `scripts/lib/offbytwo.mjs`, `scripts/lib/band-shift.mjs`, `scripts/test/{design-system,diff-report,metrics}.test.mjs`; `snapshot/{ReferenceSnapshotHandler.kt, ReferenceSnapshotHandlerTest.kt, GoldenSnapshotHandlerTest.kt, BaupilotPaparazzi.kt}` |
| 3 · Screen-Katalog | `snapshot/{ScreenCatalog.kt, ScreenCatalogTest.kt, ScreenSnapshotTests.kt}` |
| 4 · Verknüpfungen | 17 Dateien unter `src/main`, nur testTag-Änderungen: `ProtocolBanner`, `SectionHeader`, `TopAppBar`, `CompareScreen`, `DefectsScreen`, `DocumentsScreen`, `DocViewerScreen`, `ProtocolTemplate`, `ElementsScreen`, `GewerkScreen`, `HomeScreen`, `InspectionsScreen`, `InspectionStepScreen`, `LoginScreen`, `NotificationsScreen`, `ProfileScreen`, `ScheduleScreen`; Tests `app/{LinkHost.kt, LinkCoverageUiTest.kt}`; `scripts/lib/links.mjs`, `scripts/link-coverage.json`, `scripts/test/links.test.mjs` |
| 5 · Platzhalter, Semantik | 8 Dateien unter `src/main`: `SlopeResult`, `TileGrid`, `TimerRing`, `SignaturePad`, `TextField`, `TextArea`, `ScreenCopy`, `SanGefaelleScreen`; Tests `PlaceholderUiTest`, `InspectionFlowUiTest`, `LaterTradesUiTest`, `SlopeResultSemanticsTest`, `TileGridSemanticsTest`, `TimerRingSemanticsTest`, `ScheduleChartSemanticsTest`, `ScreenCopyTest` |
| 6 · Audit | `app/{SemanticsAudit.kt, SemanticsAuditTest.kt, AccessibilityAuditTest.kt}` |
| 7 · Touch-Ziele | `app/TouchTargetTest.kt` |
| 8 · Large Text | `app/LargeFontLayoutTest.kt`, `snapshot/LargeFontRenderTest.kt` |

Neu in diesem Branch (nicht in `fe905f7`): `AuditAllowlistTest`, `HeatingBarChartSemanticsTest`, `TextFieldSemanticsTest`, `designsystem/LargeFontLineBoxTest` (4 Tests), die Zeilenkasten-Korrektur und die `heightIn`-Umbauten (Large-text-Abschnitt), `TextMotion.Animated` (Hebel 9a), vier `--platform`-Tests in `diff-report.test.mjs` und vier neue Tests in `links.test.mjs`.

### Lücken G-1 … G-12 und wie sie geschlossen wurden

Spalte „Beleg“: was im Baum gegen den Stand dieses Laufs geprüft wurde.

| # | Lücke in `fe905f7` | Schließung | Beleg |
|---|---|---|---|
| G-1 | `diff-report --all` ignoriert `--platform`, läuft immer über beide Plattformen; auf diesem iOS-losen Branch endet jeder Lauf mit Exit 3 | `runAll` kennt `options.platform` (D-R4): nur dessen Zeilen, Exit-Codes und Baseline-Zeilen; Ausgabe `reports/summary-<p>.{json,md}`; ohne `--platform` unverändert (136 Zeilen) | 4 Tests in `diff-report.test.mjs` (`--all --platform android …`, `… ignores missing ios …`, `… --baseline ignores ios rows …`, `--all --platform xx exits 2`); `reports/summary-android.json` mit 68 Zeilen |
| G-2 | Der Fehlertest vergleicht die Meldung, nicht die Instanz | `assertSame` in `ReferenceSnapshotHandlerTest.sidecarWrittenOnFailureAndSameErrorRethrown` | `grep -c assertSame` in der Datei: 4 |
| G-3 | `validateCoverage` verlangt immer iOS-Methoden | Top-Level-Flag `"platforms": { "ios": false, "android": true }` (D-R5); fehlt es, gelten beide als `true` | Manifest; Tests `validateCoverage ignores a disabled platform` und `… without a platforms flag checks both platforms` |
| G-4 | `tabsOpen` und `defectsViewTabsOpenEveryTab` tippen spätere Tabs vom gerade erreichten Screen aus | Neustart über `startAt(source)` vor jedem Tab-Tipp, Quell-Tag vor dem Tipp abgewartet | `LinkCoverageUiTest.kt:74–76` |
| G-5 | Rohbau → Mängel-Zeilen prüfen nur `inspections.view.defects` | zusätzlich `screen.inspections` vorhanden und Rohbau-Tag abwesend | `LinkCoverageUiTest` (Plan Step 4) |
| G-6 | Zwei Tests in `links.test.mjs` lesen `ios/…` | nicht übernommen (D-R6) | `links.test.mjs` enthält 8 Tests, keiner liest `ios/` |
| G-7 | 18 Manifest-Einträge (17 Android-Tags) tragen ein abweichendes `androidElement`; der Wächter liest es nicht | Tags auf den `element`-Namen umbenannt (D-R8), `androidElement` gelöscht, Drift-Prüfung gegen `src/main` (`androidMainTags`, segmentweise, `$name` / `<…>` je ein Segment) | `jq '[.placeholders[]\|select(has("androidElement"))]\|length'` = 0; `links.mjs:100–101`, `:138`; Tests `validateCoverage reports a placeholder element that no android main source contains` und `… matches interpolated tags segment-wise` |
| G-8 | „Nichts geschah“ ignoriert Segment- und Tab-Auswahl | `noAction` vergleicht zusätzlich `selected` von Segmenten und Tabs und die Sichtbarkeit der `inspections.view.*` | `PlaceholderUiTest.kt:76–87` (`snapshot()` vor und nach jedem Tipp) |
| G-9 | `tapTile` prüft nichts (R13) | prüft `StateDescription` von `tiles.grid` und die `screen.*`-Menge unverändert | `LaterTradesUiTest.kt:102–127` |
| G-10 | `HeatingBarChart` nicht gepinnt; `ScheduleChartSemanticsTest` mit festem Label | `HeatingBarChartSemanticsTest` neu; Zeitplan-Test liest `TestAppData.load().schedule.ariaLabel` | beide Dateien im Baum (`ScheduleChartSemanticsTest.kt:32`) |
| G-11 | Keine Prüfung auf veraltete Allowlist-Einträge; Spike-Ergebnis nirgends festgehalten | `AllowedFinding.dark`, `AuditAllowlist.unmatched(…)` je Screen-Test (nicht `@AfterClass`), `AuditAllowlistTest` (3 Tests); Spike im Abschnitt „Audit“ | `AccessibilityAuditTest.kt:27–52` |
| G-12 | Large Text: nur die Tests vorhanden; Zeilenkasten-Korrektur, `heightIn`-Umbauten und Sichtprüfung fehlten | siehe „Large-text review“ | `LargeFontLineBoxTest`, `large-text-heights.md`, `large-text-review.md` |

Pixelneutralität der übernommenen Änderungen: jede Änderung der 25 `main`-Dateien ist eine testTag- oder Semantikänderung über bestehende `modifier`- bzw. `testTag =`-Parameter. Nur der Gate-Lauf (`diff-report … --baseline`) beweist „kein Pixel bewegt“; der Kontrolllauf ohne Hebel auf dem Baum nach Step 8 ergab wieder 12 grün / 56 rot und `diff-report` Exit 0 gegen die Baseline (`levers.md`); erst Hebel 9a ändert Zeilen, erlaubt durch D17.

## Entscheidungen

Aus dem Plan (§3), die Android-relevanten Entscheidungen der Vorlage v3 in Kurzform; Plan-Text von D-R1 … D-R10 sinngemäß:

| # | Entscheidung | Begründung |
|---|---|---|
| D7 (v3) | `SlopeResult` ist ein zusammengeführter Knoten `slope.result`: Prozent, dann Urteil; das Schema ist ausgeblendet | Ein Halt für TalkBack statt mehrerer |
| D8 (v3) | `TileGrid` ist ein Knoten `tiles.grid` (`contentDescription` = `gridAria`, `stateDescription` = die hohlen Kacheln); die Kacheln haben weder Semantik noch Klick | M6 D12: Kacheln ohne Aktion |
| D11 (v3) | `SignaturePad` meldet „leer“ / „unterschrieben“ aus `ScreenCopy.Signature` | einziger neuer, nicht sichtbarer Text |
| D12 (v3) | SanGefaelle-Abschnittskachel heißt „A3, jetzt“ | Prototyp-Zustand hörbar |
| D14 (v3) | Audit mit Allowlist; AC3 gilt nur bei leerer Liste | Ausnahmen nur mit Begründung |
| D15/D16 (v3) | Large Text: Skalierung 1.3, bei Skala 1 Pixel unverändert; beabsichtigte Ellipse nur mit Prototypzeile | Maß nur mit Beleg |
| D17 (v3) | Hebel nur behalten, wenn kein grüner Snapshot rot wird, die Zahl der grünen steigt oder die Summe `offByTwoPercent` fällt (keine Zeile > 0.005 schlechter), und jedes Maß seine Prototypzeile nennt | Toleranzen werden nie gelockert |
| D19 (v3) | Touch-Walk im ungemergten Baum, ≥ 48 dp − 0.5 px, Fenster `w411dp-h3000dp` | Scroll-Viewport soll `touchBoundsInRoot` nicht beschneiden (R9) |
| D20 (v3) | Platzhalter heißen auf beiden Plattformen gleich | Manifest ohne `androidElement` |
| D-R1 | Übernehmen statt mergen oder neu schreiben: pro Schritt `git checkout fe905f7 -- <genaue Pfade>`, dann die übernommenen Tests, dann die Lücken des Schritts, dann das Gate. | Die Arbeit existiert und wurde geprüft; ein Merge brächte `ios/` und löschte Plan-Dokumente. |
| D-R2 | Die Baseline wird auf diesem Branch neu aufgenommen (Step 0, Step 2); die `fe905f7`-Zahlen sind nur Vergleich. | Die `build/m7`-Artefakte waren weg; JDK-Patchstand weicht ab. |
| D-R3 | Dokumente: nur dieses Dokument; es trägt die Befehle, die v3 D21 im README wollte; `README.md`, `docs/m7-offene-punkte.md`, m5/m6 bleiben unberührt; die `minimumInteractiveComponentSize`-Korrektur steht als Notiz hier. | M7-android.md „Umfang“ nennt nur dieses Dokument. |
| D-R4 | `diff-report --all --platform <p>` ist additiv; ohne `--platform` bleibt das Verhalten von `fe905f7`. | Ohne das erreicht dieser iOS-lose Branch nie Exit 0. |
| D-R5 | Manifest-Flag `platforms`; fehlt es, gelten beide Plattformen; die `ios`-Felder bleiben byteidentisch. | Kein iOS-UI-Test auf diesem Branch; der iOS-Plan schaltet es auf seinem Branch ein. |
| D-R6 | Die zwei iOS-Quelltests von `links.test.mjs` werden nicht übernommen. | Nur plattformneutrale Teile von `scripts/` sind im Umfang. |
| D-R7 | `contentDescription = label` am Eingabefeld (`TextField`, `TextArea`) bleibt; die sichtbare Notch-Beschriftung bekommt `clearAndSetSemantics {}`, damit das Label ein Halt ist. | Der Audit (Regel `no-label`) braucht es; kein neuer Text. Pinnt `TextFieldSemanticsTest.labelIsOneStop`. |
| D-R8 | Platzhalter-Tags folgen dem Manifest-`element` (17 Tags umbenannt, drei mit Aufbauänderung: `…more.$floorIndex`, `…remind.${card.id}` / `…resolve.${card.id}`, `ProtocolTemplate(documentTag = …)`); Drift-Prüfung gegen `src/main`. | D20: gleiche Namen; ein testTag zeichnet nichts, das Gate pinnt Pixel. |
| D-R9 | Audit = Fallback `SemanticsAudit`, außer ATF läuft unter Robolectric und fängt den eingepflanzten Fehler. Kontrast prüft der Fallback nicht; das schließt nur der manuelle Accessibility-Scanner-Lauf. | Spike-Ergebnis: siehe „Audit“. |
| D-R10 | Schrittzählung des Plans (0–11) ersetzt die der Vorlage in den Commit-Texten: `M7 step N (android): …`. | Eine Reihenfolge für die Umsetzung. |

## Bekannte Lücken

Je Eintrag: Ursache, Ergebnis und was sie klären würde. Messwerte stehen im nächsten Abschnitt.

- **R4 / R20 (offen, Kontrast und manueller Scanner-Lauf):** Der Fallback-Audit prüft keinen Farbkontrast; es wurde keine Regel dafür erfunden (D-R9, `step6-spike.md`). Ein Token-Kontrastproblem wäre maschinell unentdeckt. Der Accessibility-Scanner-Lauf auf einem Gerät kann nicht von einem Agenten erledigt werden; er steht als Operator-Checkliste unten. **Bekannte Lücke, bis ein Operator ihn ausgeführt hat.**
- **R25 / AC1 (teilweise offen):** Von 68 Android-Snapshots sind 64 grün, 4 rot (EsBelegreife und FlBad, je hell und dunkel; 0.6343–0.6825 %); Quelle `reports/summary-android.json` (`totals`: 64 / 4 / 0). Baseline waren 12 grün (`step0.md`). Toleranz (0.5 %, `BaupilotPaparazzi.kt:11`) und Referenzen sind unverändert (I1, I2). Ursache und Hebel je rote Zeile stehen in der Tabelle unten.
- **R2 (Hebel behandelt):** `TextMotion.Animated` änderte Rendering und Metriken; D17 war erfüllt, der Hebel wurde behalten (`levers.md`, Abschnitt 9a). Die Metrik-Tests (M3 `wholeDpWidth`, M6 D14) blieben grün, nichts wurde neu abgeleitet. Die Galerie-Goldens (60 Dateien unter `android/app/src/test/goldens/`) wurden neu aufgenommen, vorher angesehen: gleiche Größe, nur Glyphkanten, keine Box-, Abstands- oder Farbänderung; `*Gallery*SnapshotTest` danach grün (`levers.md`).
- **R3 (eingetreten, Fallback):** ATF läuft unter Robolectric nicht belastbar (Abschnitt „Audit“).
- **R13 (bekannte Schwäche):** Ein wirkungsloser Koordinaten-Tipp kann nicht beweisen, dass er die Kachel 3 getroffen hat; `tapTile` prüft nur, dass sich `StateDescription` und Screen-Menge nicht ändern.
- **R-f / Large Text (bekannte Lücke):** `LargeFontLayoutTest[DokViewer]` ist rot (19 Befunde im festen Seitenraster der Dokumentvorschau, `DocumentPreview.kt`); siehe „Large-text review“. Nach Hebel 9a kam `LargeFontLayoutTest[Dokumente]` hinzu (beabsichtigte Ellipse, aber noch kein Allowlist-Eintrag; `levers.md`, „Neuer Fund“). Stand der Gesamtsuite nach 9a: 733 Tests, 6 rot (`levers.md`).
- **Step 9b (nach Review nachgeholt):** EsBelegreife-Titel 22 sp → 20 sp (`TitleStyle.Detail`, `AndEsBelegreife.dc.html:22`) und FlBad-Fliesenspalten auf ganze dp gerastert (`TileGrid.kt`, `AndFlBad.dc.html:51`); Messung in „Nachbesserung nach Review (Zyklus 2)“. Der Stand der Tabellen darüber ist der von Step 11.
- **R1 (bekannt, Rest):** Glyphen-Raster (Layoutlib gegen Skia, M2 R11) bleibt Teil der vier roten Zeilen; die Bildband-Zerlegung in `levers.md` zeigt die Anteile.

## Messwerte der 68 Android-Snapshots

Quellen: `reports/summary-android.json` (Lauf nach 9a, `diff-report --all --platform android`, Exit 0 gegen `build/m7/android/after-9a/summary.json`), `reports/summary-android.md`, `build/m7/android/notes/exit-table.md`, `build/m7/android/notes/levers.md`. „Testwert“ ist bei roten Zeilen der Paparazzi-`value`, bei grünen `offByTwoPercent` (Grenze 0.5). „Diff %“ ist die Spalte aus `diff-report` (exakt abweichende Pixel, ohne Toleranz; nur untereinander vergleichbar). „Lagenanteil“ ist `positionShare`. Grüne Zeilen brauchen keine Ursachenklasse („–“). Die Tabelle ist aus den Berichten erzeugt, nicht abgeschrieben.

Summe: 68 Zeilen, 64 grün, 4 rot (`exit-table.md`, `summary-android.json`). Summe `offByTwoPercent`: Baseline 44.2457, nach 9a 18.1061 (`levers.md`; letztere Zahl in diesem Lauf aus `summary-android.json` mit `jq` nachgerechnet: 18.106137…). Keine Zeile wurde schlechter (`levers.md`).

| Nr | Screen | Modus | Testwert % | ✓/✗ | Diff % | Lagenanteil | Ursachenklasse | Versuchte Hebel |
|---|---|---|---|---|---|---|---|---|
| 1 | Login | dark | 0.2215 | ✓ | 1.5806 | 0.0000 | – | – |
| 1 | Login | light | 0.2373 | ✓ | 1.5889 | 0.0000 | – | – |
| 2 | Home | dark | 0.2637 | ✓ | 1.8581 | 0.1021 | – | – |
| 2 | Home | light | 0.2872 | ✓ | 1.8554 | 0.0929 | – | – |
| 3 | Zeitplan | dark | 0.2382 | ✓ | 1.5998 | 0.2096 | – | – |
| 3 | Zeitplan | light | 0.2730 | ✓ | 1.5939 | 0.2147 | – | – |
| 4 | Profil | dark | 0.1654 | ✓ | 1.5814 | 0.1232 | – | – |
| 4 | Profil | light | 0.1811 | ✓ | 1.5807 | 0.1103 | – | – |
| 5 | Gewerk | dark | 0.1661 | ✓ | 1.4122 | 0.0000 | – | – |
| 5 | Gewerk | light | 0.1830 | ✓ | 1.4142 | 0.0000 | – | – |
| 6 | Dokumente | dark | 0.2265 | ✓ | 2.1309 | 0.0000 | – | – |
| 6 | Dokumente | light | 0.2496 | ✓ | 2.2275 | 0.0000 | – | – |
| 7 | DokViewer | dark | 0.1555 | ✓ | 1.3001 | 0.0000 | – | – |
| 7 | DokViewer | light | 0.1603 | ✓ | 1.2984 | 0.0000 | – | – |
| 8 | Vergleich | dark | 0.1564 | ✓ | 1.7669 | 0.0000 | – | – |
| 8 | Vergleich | light | 0.1904 | ✓ | 1.7826 | 0.0000 | – | – |
| 9 | Mitteilungen | dark | 0.1625 | ✓ | 1.5523 | 0.0000 | – | – |
| 9 | Mitteilungen | light | 0.1764 | ✓ | 1.5622 | 0.0000 | – | – |
| 10 | AbnGewerke | dark | 0.2030 | ✓ | 2.1297 | 0.0000 | – | – |
| 10 | AbnGewerke | light | 0.2209 | ✓ | 2.1201 | 0.0000 | – | – |
| 11 | AbnElemente | dark | 0.2541 | ✓ | 1.8226 | 0.0000 | – | – |
| 11 | AbnElemente | light | 0.2734 | ✓ | 1.8277 | 0.0000 | – | – |
| 12 | AbnPruefung | dark | 0.2554 | ✓ | 2.3151 | 0.0000 | – | – |
| 12 | AbnPruefung | light | 0.2708 | ✓ | 2.3161 | 0.0000 | – | – |
| 13 | AbnMangel | dark | 0.2836 | ✓ | 1.6423 | 0.1300 | – | – |
| 13 | AbnMangel | light | 0.3173 | ✓ | 1.6377 | 0.1287 | – | – |
| 14 | AbnSignatur | dark | 0.2638 | ✓ | 1.7555 | 0.0213 | – | – |
| 14 | AbnSignatur | light | 0.2747 | ✓ | 1.7594 | 0.0199 | – | – |
| 15 | AbnMaengel | dark | 0.2113 | ✓ | 1.5969 | 0.0000 | – | – |
| 15 | AbnMaengel | light | 0.2323 | ✓ | 1.6028 | 0.0000 | – | – |
| 16 | Erd | dark | 0.2989 | ✓ | 2.2820 | 0.0000 | – | – |
| 16 | Erd | light | 0.3307 | ✓ | 2.2853 | 0.0000 | – | – |
| 17 | Rohbau | dark | 0.2847 | ✓ | 2.2243 | 0.0000 | – | – |
| 17 | Rohbau | light | 0.3098 | ✓ | 2.2303 | 0.0000 | – | – |
| 18 | RohbauMasse | dark | 0.2302 | ✓ | 1.8430 | 0.0000 | – | – |
| 18 | RohbauMasse | light | 0.2590 | ✓ | 1.8490 | 0.0000 | – | – |
| 19 | Zimmerer | dark | 0.2639 | ✓ | 2.0848 | 0.0000 | – | – |
| 19 | Zimmerer | light | 0.2938 | ✓ | 2.0868 | 0.0000 | – | – |
| 20 | ElUebersicht | dark | 0.1787 | ✓ | 1.9389 | 0.0000 | – | – |
| 20 | ElUebersicht | light | 0.1987 | ✓ | 1.9357 | 0.0000 | – | – |
| 21 | ElRaum | dark | 0.3217 | ✓ | 2.4168 | 0.0412 | – | – |
| 21 | ElRaum | light | 0.3457 | ✓ | 2.4116 | 0.0458 | – | – |
| 22 | ElVerteiler | dark | 0.2877 | ✓ | 2.2802 | 0.0000 | – | – |
| 22 | ElVerteiler | light | 0.3107 | ✓ | 2.2766 | 0.0000 | – | – |
| 23 | ElRauchmelder | dark | 0.2241 | ✓ | 1.7904 | 0.0043 | – | – |
| 23 | ElRauchmelder | light | 0.2603 | ✓ | 1.7962 | 0.0076 | – | – |
| 24 | SanUebersicht | dark | 0.2043 | ✓ | 2.1805 | 0.0000 | – | – |
| 24 | SanUebersicht | light | 0.2225 | ✓ | 2.1857 | 0.0000 | – | – |
| 25 | SanGefaelle | dark | 0.4015 | ✓ | 2.0680 | 0.1766 | – | – |
| 25 | SanGefaelle | light | 0.4272 | ✓ | 2.0961 | 0.1819 | – | – |
| 26 | SanDruck | dark | 0.2240 | ✓ | 1.6625 | 0.0169 | – | – |
| 26 | SanDruck | light | 0.2416 | ✓ | 1.6628 | 0.0159 | – | – |
| 27 | SanBad | dark | 0.2306 | ✓ | 2.2273 | 0.0000 | – | – |
| 27 | SanBad | light | 0.2441 | ✓ | 2.2335 | 0.0000 | – | – |
| 28 | EsUebersicht | dark | 0.1951 | ✓ | 2.0759 | 0.0000 | – | – |
| 28 | EsUebersicht | light | 0.2199 | ✓ | 2.0850 | 0.0000 | – | – |
| 29 | EsEbenheit | dark | 0.2079 | ✓ | 1.5088 | 0.0000 | – | – |
| 29 | EsEbenheit | light | 0.2172 | ✓ | 1.5142 | 0.0000 | – | – |
| 30 | EsBelegreife | dark | 0.6466 | ✗ | 2.6531 | 0.0353 | Layout BaupilotTopAppBar (Titel 22 sp statt 20 px, AndEsBelegreife.dc.html:22) + Glyphen-Raster (R1) | 9a TextMotion.Animated (behalten): 0.9359 → 0.6466; 9b: keiner (Zeile > 0.55, außerhalb Aufgabenumfang) |
| 30 | EsBelegreife | light | 0.6825 | ✗ | 2.6556 | 0.0333 | Layout BaupilotTopAppBar (Titel 22 sp statt 20 px, AndEsBelegreife.dc.html:22) + Glyphen-Raster (R1) | 9a TextMotion.Animated (behalten): 0.9740 → 0.6825; 9b: keiner (Zeile > 0.55, außerhalb Aufgabenumfang) |
| 31 | FlUebersicht | dark | 0.1745 | ✓ | 1.8021 | 0.0000 | – | – |
| 31 | FlUebersicht | light | 0.1974 | ✓ | 1.7999 | 0.0000 | – | – |
| 32 | FlBad | dark | 0.6343 | ✗ | 2.7486 | 0.0000 | Umbruch (Absatz „…leicht auf jede“ bricht vor „jede“) + Layout SocketGrid (8×1fr-Spalten, Beobachtung am Panel: Spalten 1–3 px versetzt, AndFlBad.dc.html:51) + Glyphen-Raster (R1) | 9a TextMotion.Animated (behalten): 0.8712 → 0.6343; 9b: keiner (Zeile > 0.55, außerhalb Aufgabenumfang) |
| 32 | FlBad | light | 0.6622 | ✗ | 2.7504 | 0.0000 | Umbruch (Absatz „…leicht auf jede“ bricht vor „jede“) + Layout SocketGrid (8×1fr-Spalten, Beobachtung am Panel: Spalten 1–3 px versetzt, AndFlBad.dc.html:51) + Glyphen-Raster (R1) | 9a TextMotion.Animated (behalten): 0.8972 → 0.6622; 9b: keiner (Zeile > 0.55, außerhalb Aufgabenumfang) |
| 33 | MaUebersicht | dark | 0.1456 | ✓ | 1.4206 | 0.0000 | – | – |
| 33 | MaUebersicht | light | 0.1499 | ✓ | 1.4110 | 0.0000 | – | – |
| 34 | MaRaum | dark | 0.3023 | ✓ | 3.4010 | 0.0108 | – | – |
| 34 | MaRaum | light | 0.3219 | ✓ | 3.3971 | 0.0371 | – | – |

**Hebel 9a (`levers.md`):** `TextMotion.Animated` in `Typography.kt` (zentraler `BaupilotText`-`TextStyle`), `TextField.kt`, `SearchBar.kt`, `TextArea.kt`; kein Maß, keine Dimension geändert. Grüne Snapshots 12 → 64; rote 56 → 4 (EsBelegreife 0.9359 → 0.6466 dunkel, 0.9740 → 0.6825 hell; FlBad 0.8712 → 0.6343 dunkel, 0.8972 → 0.6622 hell). Ein Kontrolllauf ohne Hebel auf demselben Baum ergab 12 grün / 56 rot und `diff-report` Exit 0: die Verbesserung stammt vom Hebel. **Versucht und verworfen:** nichts (9b hat keinen Hebel ausprobiert).

**Befunde an den roten Zeilen** (`levers.md`, Anteil am Testwert je Bildband; nur gemessen, nicht behoben, weil über 0.55 %):

- *EsBelegreife hell (0.6825):* Band y 0–330 trägt 0.3589. Der Titel „Aufheizen & Belegreife“ ist im Ist etwa 10 % breiter (≈ 220 dp statt ≈ 200 dp): `EsBelegreifeScreen.kt:44` nutzt den Default `TitleStyle.Large` (22 sp), der Prototyp hat `font-size: 20px` (`AndEsBelegreife.dc.html:22`). `TitleStyle.Detail` (20 sp) existiert und wird von MaUebersicht, SanGefaelle, SanBad genutzt. Empfehlung Folgeschritt: `titleStyle = TitleStyle.Detail` mit Kommentar `// AndEsBelegreife.dc.html:22`.
- *FlBad hell (0.6622):* Band y 300–1200 trägt 0.5257. Der Absatz „Mit dem Griff eines Schraubendrehers leicht auf jede Bodenfliese …“ bricht im Ist vor „jede“, in der Referenz danach; die 8 Fliesenspalten (`AndFlBad.dc.html:51`, `repeat(8, minmax(0,1fr))`, Abstand 4, Padding 8) liegen 1–3 px versetzt (Beobachtung am Panel, nicht gemessen); „↑ Wand mit Dusche“ nutzt ein anderes Pfeilglyph (R1).
- Dunkel-Zeilen zeigen dasselbe Muster, nicht einzeln zerlegt.

**Galerie (9c):** bei Step 0 grün, aktuell `GalleryLightSnapshotTest` und `GalleryDarkSnapshotTest` je 1 Test, 0 Fehler; 9c übersprungen.

## Large-text review

Skalierung 1.3. Sammler: Robolectric `GraphicsMode.NATIVE` (`@Config(fontScale = 1.3f, qualifiers = "w411dp-h3000dp")`); er misst Text, der Paparazzi-Sammler als Rückfall war nicht nötig. Paparazzi läuft die parametrisierte `LargeFontRenderTest` unter `forkEvery = 1` (R14), `LargeFontSnapshotTests.kt` wurde nicht angelegt; Ausgabe `build/snapshots/android-large/<Name>_130.png`, 34 Dateien. Quelle: `build/m7/android/notes/large-text-sites.md`.

Ausgangslage (`large-text-sites.md`): `LargeFontLayoutTest` 34 Tests, 23 rot, 11 grün; `LargeFontRenderTest` 34 Tests, 0 rot; die Allowlist war leer. Nach Step 8 (`large-text-heights.md`): 33 von 34 grün, nur DokViewer rot. Nach Hebel 9a (`levers.md`): zusätzlich `[Dokumente]` rot, also 32 von 34 grün (zwei offene Zeilen).

**Sichtprüfung der 34 Screens** (`build/m7/android/notes/large-text-review.md`; PNGs `build/snapshots/android-large/<Name>_130.png`). Gemeinsam bei allen Tab-Screens: „Dokumente“ in der unteren Navigation bricht in „Dokument / e“ (Beschriftung passt nicht in die Tab-Breite); bleibt, keine Überlappung.

| Screen | Ergebnis |
|---|---|
| Login | keine Überlappung; Titel zweizeilig, Felder und Buttons wachsen mit |
| Home | keine Überlappung; Glocken-Badge "2" behoben (Badge `heightIn`); "Rechnungen" bricht in der Dokument-Kachel um (bleibt) |
| Zeitplan | keine Überlappung; Zeilennamen "Fenster & Tür…" / "Sanitär & Heiz…" mit Ellipse (Prototyp nowrap, Allowlist) |
| Dokumente | keine Überlappung; Filter-Chips laufen seitlich aus dem Bild (Scrollleiste, gewollt) |
| Mitteilungen | keine Überlappung |
| Profil | keine Überlappung; Name umgebrochen, Zeile wächst über 80 dp (behoben) |
| Gewerk | keine Überlappung; Faktenzeilen 56 dp wachsen nicht nötig |
| Vergleich | keine Überlappung (Titel x Untertitel behoben durch Zeilenbox) |
| DokViewer | Kopfzeile ok; Dokumentvorschau: Adresse, Kopfzeile 2 und Positionstexte angeschnitten - Lücke (fixes Seitenraster), siehe large-text-heights.md |
| AbnGewerke | keine Überlappung; letzte Zeile hinter der Navigation (Liste scrollt) |
| AbnMaengel | keine Überlappung |
| AbnMangel | keine Überlappung; Textfeld wächst (heightIn), Kalender-Icon mittig; "+ 6 Wochen"-Chip läuft aus dem Bild (Scrollleiste) |
| AbnElemente | keine Überlappung; Untertitel zweizeilig |
| AbnPruefung | keine Überlappung |
| AbnSignatur | keine Überlappung |
| Erd | keine Überlappung; Banner "Abgenommen am 14.04.2026" vierzeilig, wächst mit |
| Rohbau | keine Überlappung |
| RohbauMasse | keine Überlappung; Chip "F7" bricht "F / 7" um, "−5 mm" bricht um (bleibt, kein Text abgeschnitten) |
| Zimmerer | keine Überlappung |
| ElUebersicht | keine Überlappung; "Abgenommen 29.01." zweizeilig, Karte wächst (IntrinsicSize) |
| ElVerteiler | keine Überlappung; "Außen" in Zeile FI 1 liegt knapp über der Trennlinie (bleibt) |
| ElRaum | keine Überlappung |
| ElRauchmelder | keine Überlappung |
| SanUebersicht | keine Überlappung |
| SanGefaelle | keine Überlappung; im Gefälle-Bild liegt das Wort "waagerecht" auf der Linie (feste Skizze, bleibt) |
| SanDruck | keine Überlappung; Untertitel zweizeilig, berührt den Fortschrittsbalken (bleibt) |
| SanBad | keine Überlappung; Ja/Nein-Schalter wachsen mit (Zeilenhöhe) |
| EsUebersicht | keine Überlappung; "Aufheizen ab 01.03." zweizeilig |
| EsEbenheit | keine Überlappung |
| EsBelegreife | keine Überlappung; "Messstellen markiert" dreizeilig |
| FlUebersicht | keine Überlappung |
| FlBad | keine Überlappung; Untertitel zweizeilig, berührt den Balken (bleibt) |
| MaUebersicht | keine Überlappung; "3 bemustert" bricht um |
| MaRaum | keine Überlappung |

### Entscheidungen je `.height`-Stelle (`build/m7/android/notes/large-text-heights.md`)

Klassen: 28 Stellen umgebaut, 7 gelistete Stellen plus Nicht-Text-Höhen fest gelassen, 1 Allowlist-Eintrag, 1 Lücke (DokViewer-Seitenraster).

**Ursache, die nicht in der Stellenliste stand (`Typography.kt`):** Alle 22 „Überlappungs“-Befunde (Titel × Untertitel in `TopAppBar`, Zeilentitel × Untertitel in den Screens) kamen nicht von festen Höhen. `TopAppBar.kt:183` auf `heightIn(min)` zu stellen änderte nichts. Gemessen: Der Textknoten eines `BaupilotText` liegt durch `robotoLineBox` / `cssLineHeightBaseline` `shift` px (Grundlinie − erste) ober- bzw. unterhalb seines äußeren Layoutkastens; `boundsInRoot` ragt dadurch 1–2 px in die Zeile darüber (Home: Titel 38..70, Untertitel 69..89; Vergleich: Titel 41..70, Untertitel 68..88 bei Dichte 1). Korrektur: Oberhalb von `fontScale` 1.05 (die Schwelle `LARGE_FONT_SCALE`, die die Zeilenkasten-Rechnung schon nutzt) sind Beschnitt und Grundlinienversatz aus; bei Skala ≤ 1.05 ist der Pfad byteidentisch. Galerie und Gate bei Skala 1 unverändert. Abweichung von Plan Step 8.3 (dort: `ceil(value/size × size.sp.toPx())` oberhalb 1.05): oberhalb der Schwelle sind Beschnitt und Grundlinienversatz aus (`excess = 0`, `shift = 0`), der `ceil`-Zweig von `lineBoxValuePx` wirkt im Layout nicht und läuft nur im Test. `LargeFontLineBoxTest` (4 Tests: `lineBoxMatchesComposeLineHeightAt130`, `baselineMatchesComposeAt130`, `helpersUnchangedAtScale1`, `helpersUnchangedAt104`).

**Umgebaut auf `heightIn(min = N.dp)`** (bei Skala 1 pixelgleich, weil min = N und der Inhalt kleiner ist):

| Datei:Zeile | Stelle |
|---|---|
| `Buttons.kt` | :98 Filled 48, :131 Outlined 48/50, :189 Compact40, :190 Wide44, :217 Tonal 40 |
| `Chips.kt` | :45 Rahmen 32, :263 StatusChip `size.height` |
| `TabRow.kt` | :59 (48) |
| `TextField.kt` | :59 / :63 (56) |
| `TextArea.kt` | :44 / :48 (`rows*22+32`) |
| `FactRow.kt` | :23 (56) |
| `ProtocolBanner.kt` | :95 (38) |
| `SocketGrid.kt` | :54 (52) |
| `SectionHeader.kt` | :40 (32/40) |
| `TopAppBar.kt` | :183 (Leiste), :224 (`Action.Text`), :279 (Glocken-Badge 16, behebt die „2“ in Home) |
| `NavigationBar.kt` | :84 |
| `SearchBar.kt` | :54 (56) |
| `ValueStepper.kt` | :58 (50) |
| `ExtendedFab.kt` | :53 (56) |
| `DocumentTile.kt` | :40 (56) |
| `PhotoTile.kt` | :63 (Guide, Icon, Add); `Scene` bleibt `.height`, weil sein Inhalt `fillMaxSize()` ist und `heightIn` unter einem unbegrenzten Elternteil Höhe 0 ergäbe |
| `TileGrid.kt` | :76 (44) |
| `ProfileScreen.kt` | :113 (80) |
| `SegmentedButtons.kt` | :46 `.heightIn(min = 42.dp).height(IntrinsicSize.Min)`: Die Zellen mit `fillMaxHeight` füllen die intrinsische Höhe |

`TextField.kt:90` (Trailing-Icon, `TopEnd`, oben 16 dp): Das Feld wächst bei 1.3 nicht (16 sp-Zeile ≈ 27 dp < 56), das Icon bleibt ausgerichtet; AbnMangel „Behebung bis“ im PNG geprüft (mittig auf der Textzeile).

**Fest gelassen:** `ScheduleChart.kt:84/124/127` (Zeilen im `ROW_PITCH`), `DocumentPreview.kt:79–111` (Seitenraster), Füller `SocketGrid.kt:90`, `TileGrid.kt:64`, `ElRaumScreen.kt:140`; Nicht-Text-Höhen (Trenner, Abstandshalter, Icons, Zeichenflächen, 1-dp-Linien, `IntrinsicSize`-Zeilen der `*Uebersicht`-Screens, deren Überlauf-Befunde mit der Zeilenkasten-Korrektur verschwanden).

**Allowlist (1):** `LargeFontAllowlist.ellipsis`: `Zeitplan` / Tag `schedule.row.name` / `AndZeitplan.dc.html:57` (`white-space: nowrap; overflow: hidden; text-overflow: ellipsis`). Bei 1.3 werden „Fenster & Türen“ und „Sanitär & Heizung“ in der 118-dp-Namensspalte zu „Fenster & Tür…“ / „Sanitär & Heiz…“ (PNG geprüft). Die Diagrammzeilen selbst bleiben im `ROW_PITCH`; 33 dp Inhalt fassen eine 13 sp × 1.3-Zeile (≈ 17 dp).

**Lücke DokViewer (Stand Step 8; im Zyklus 2 gemessen und in `fixedGeometryGaps` geführt, siehe Nachbesserung).** `DocumentPreview.kt`, festes Seitenraster 250 × 320 dp, 7-sp-Text bei Zeilenhöhe 1.35: 19 Überläufe bei 1.3 (Absender-Adresse, Angebotskopf Zeile 2, Positionen 1–5, Positionstexte, Preise, „Gesamt brutto“, Summe). Geschätzt aus den Zeilenkästen (**nicht** vom Test gemessen, der meldet nur die Achse): 7-sp-Zeile 9.45 dp → ≈ 12.3 dp bei 1.3; Kopfblock 24 → ≈ 32 dp; Summenzeile 18 → ≈ 22 dp. Das PNG zeigt Adresse, Angebotskopf Zeile 2 und Positionstexte unten angeschnitten. Die Behebung braucht die Entscheidung, ob eine Papiervorschau mit der Schrift skaliert; die Aufgabe hielt das Raster fest.

**Neuer Fund nach 9a: Dokumente (behoben im Zyklus 2, siehe Nachbesserung)** (`levers.md`). Bei 1.3 melden „Angebot Fließestrich Hahn“ und „Angebot Estrichbau Wagner“ (Titel der Dokumentzeilen, `lines=1`) `didOverflowHeight`: mit Hebel 9a sind die Glyph-Advances minimal breiter, der Titel wird bei `maxLines = 1` + `Ellipsis` abgeschnitten (`ListItem.kt:136`). Der Prototyp verlangt das (`AndDokumente.dc.html:53`, `white-space: nowrap; overflow: hidden; text-overflow: ellipsis`), es ist beabsichtigte Ellipse, kein Layoutfehler. Behebung = Eintrag in `LargeFontAllowlist.ellipsis` (`Dokumente`, Tag der Titelzeile, `AndDokumente.dc.html:53`); das lag außerhalb der Dateiliste von p14t1 und ist **nicht** geändert. Stand dieser Datei: `LargeFontAllowlist.ellipsis` enthält nur den Zeitplan-Eintrag; Gate 0 für `Dokumente` bleibt rot, bis ein Folgeschritt den Eintrag setzt.

## Audit

**Spike (Quelle: `build/m7/android/notes/step6-spike.md`): Ergebnis FAIL.** ATF (`androidx.compose.ui:ui-test-junit4-accessibility`, BOM-gepflegt, 1.11.4 aus dem Gradle-Cache) unter Robolectric, ein Test auf Login (`w411dp-h3000dp`) mit `enableAccessibilityChecks()` + `onRoot().tryPerformAccessibilityChecks()` und einer eingepflanzten unbeschrifteten 48-dp-Box:

1. Standard-(Legacy-)Grafik: Der Test bricht vor jeder Prüfung ab: `java.lang.NullPointerException: Cannot invoke "android.graphics.Bitmap.setHasAlpha(boolean)" because "bm" is null` (in `Bitmap.createBitmap` ← `Api26Bitmap` ← `DrawCache.drawCachedImage`; der ATF-Screenshot braucht echte Bitmaps).
2. Eine Wiederholung (D-R9): `@GraphicsMode(NATIVE)`. Der Test läuft und ist grün, meldet die eingepflanzte unbeschriftete Fläche aber **nicht** (auch nicht mit explizitem `AccessibilityValidator().setThrowExceptionFor(ERROR)`). Ein Audit, der einen eingepflanzten Fehler nicht meldet, beweist nichts; der ATF-Pfad wird nicht hinzugefügt.

**Gewählter Pfad:** der Fallback `SemanticsAudit` (`SemanticsAudit.check(root): List<Finding>`; Regeln `no-label`, `image-without-description`, `touch-target` (< 48 dp − 0.5 px), `duplicate-bounds`), über allen 34 Screens in `AccessibilityAuditLightTest` / `AccessibilityAuditDarkTest` (Modus je Test geprüft), mit je einem eingepflanzten Fehler pro Regel in `SemanticsAuditTest`. Es verbleibt keine ATF-Abhängigkeit in den Gradle-Dateien. **Nicht geprüft vom Fallback:** Farbkontrast (Plan D-R9; bekannte Lücke R4, manueller Scanner-Lauf R20).

**Allowlist (Stand `AccessibilityAuditTest.kt`):** `AuditAllowlist.entries` = leer, `AuditAllowlist.staged` = leer. AC3 gilt damit als erfüllt, sofern die Audit-Klassen grün laufen; deren Ergebnis steht unter „Verifikation“ (68 / 0, `TouchTargetTest` 34 grün).

**Touch-Ziele (Step 7):** `TouchTargetTest` (34 Screens, ungemergter Baum, ≥ 48 × 48 dp − 0.5 px, hell). Im Baum steht keine `minimumInteractiveComponentSize`-Verwendung in `android/app/src/main` (`grep` ohne Treffer): Die Ziele sind eigene `Modifier.clickable`, Compose erweitert die Zeigerprüfung auf 48 dp (siehe „Notiz“). Bekannte kleinere Ziele: FlBad-Kacheln 35,25 × 44 dp (M6 R11, ohne Aktion, ohne Klick-Semantik), `VerdictToggle`-Zellen 48 × 40 dp (Berührungsfläche reicht bis 48).

## Link-Manifest

Quelle: `scripts/link-coverage.json` (in diesem Lauf mit `jq` gezählt) und `scripts/test/links.test.mjs`.

| Größe | Wert |
|---|---|
| Verknüpfungspaare (`links`) | 100 |
| davon mit Android-Test (`links[].android`) | 100 von 100 |
| Platzhalter (`placeholders`) | 101 |
| davon mit Android-Test (`placeholders[].android`) | 92 (9 nur iOS: `ph.Profil.row.0.0/0.1/0.2/2.0/2.1/2.2`, `ph.DokViewer.fact.<n>`, `ph.ElUebersicht.more.1`, `flatness.save`; `platforms: ["ios"]`) |
| Platzhalter mit `androidElement` | 0 (D-R8) |
| Manifest-Flag `platforms` | `{ "ios": false, "android": true }` |
| `ios`-Felder gegenüber `fe905f7` (I15) | gleich: `jq -c '[.links[].ios, .placeholders[].ios]'` aus `git show fe905f7:scripts/link-coverage.json` und HEAD ergeben denselben md5 (`31419cbc5783007e56a03b24609b2231`) |
| Drift-Prüfung (I16) | grün: `the real manifest covers every pair and placeholder` meldet 0 Probleme; `readSources` füllt `androidMainTags` (`links.mjs:138`), die Prüfung läuft also gegen `android/app/src/main` |
| `node --test test/links.test.mjs` | 8 Tests, 8 grün |

Die Android-Tests selbst (`LinkCoverageUiTest`, `PlaceholderUiTest`) sind im Gesamtlauf von Step 11 grün (siehe „Verifikation“).

## Testzahlen

Beobachtet; die Quelle steht je Zeile.

| Block | Ergebnis | Quelle |
|---|---|---|
| `(cd scripts && node --test test/)` | 140 Tests, 140 grün, 0 rot (in diesem Lauf erneut ausgeführt) | Befehl |
| davon `links.test.mjs` | 8 Tests, 8 grün (4 übernommen, 4 neu) | Befehl |
| Android Step 0 (`:app:testDebugUnitTest`, pristine) | 483 Tests, 427 grün, 56 rot | `step0.md` |
| Android Snapshot-Lauf | 71 Testfälle (56 rot in Step 2; nach 9a 4 rot) | `step2.md`, `levers.md` |
| Android Gesamtsuite nach 9a | 733 Tests, 6 rot: 4 Snapshots (EsBelegreife, FlBad), `LargeFontLayoutTest[DokViewer]` (19 Funde), `LargeFontLayoutTest[Dokumente]` | `levers.md` |
| `LargeFontLayoutTest` vor p13t1 | 34 Tests, 23 rot, 11 grün | `large-text-sites.md` |
| `LargeFontLayoutTest` nach p13t1 | 33 von 34 grün (nur DokViewer rot) | `large-text-heights.md` |
| `LargeFontRenderTest` | 34 Tests, 0 rot, 34 PNG | `large-text-sites.md` |
| `LargeFontLineBoxTest` | 4 Tests | `@Test`-Zählung im Baum (`grep -c`) |
| `AuditAllowlistTest` | 3 Tests | `@Test`-Zählung im Baum |
| `diff-report --all --platform android` | 68 Zeilen, 64 grün, 4 rot, 0 fehlt | `reports/summary-android.json` |

Zählweise `@Test` ist statisch; nur Zeilen mit „Befehl“ oder einer Notiz-Quelle sind ausgeführte Läufe. Der Gesamtlauf nach dem letzten Stand gehört zu Step 11.

### Invarianten (Plan §9)

Ergebnisspalte von Step 11 eingetragen (beobachtet in diesem Lauf, Quelle je Zeile).

| # | Invariante | Festgehalten durch | Ergebnis |
|---|---|---|---|
| I1 | Toleranz unverändert: 0.5 bei `BaupilotPaparazzi.kt:11`, OffByTwo, kein `app.cash.paparazzi.differ` | T-DS-3 samt Differ-Muster | erfüllt: `BaupilotPaparazzi.kt:11` = `const val MAX_PERCENT_DIFFERENCE = 0.5`; `paparazzi.differ` kommt nur in `scripts/test/design-system.test.mjs` (T-DS-3) und in Dokumenten vor; `scripts` 140/140 grün |
| I2 | Referenzen, `design/`, `fixtures/` unverändert; nichts gegen Referenzen neu aufgenommen | `scripts/design-freeze --check`; `git status --porcelain -- design fixtures scripts/icons.json` leer | erfüllt: `design-freeze --check` Exit 0 (276 Dateien); `git status --porcelain -- design fixtures scripts/icons.json` leer |
| I3 | Steps 3–8 ändern kein gerendertes Pixel der 68 Screens | Gate Exit 0 nach jedem dieser Schritte (und jedem Step-8-Stapel) | nicht neu geprüft: Gate-Exit-Codes der Schritte stehen in `step2.md`, `levers.md`, `exit-table.md` (Notizen der Schritte), nicht in diesem Lauf; die Änderung der Pixel durch den behaltenen Hebel 9a ist erwartet (I10, D17) |
| I4 | Platzhalter bleiben sichtbar und tun nichts (Screen, Segment, Ansicht, Tab unverändert) | `PlaceholderUiTest`; I3 | erfüllt: `PlaceholderUiTest` 29 Tests, 0 rot |
| I5 | Kein Maß ohne Prototypzeile | jedes geänderte numerische Layout-Literal in `android/app/src/main` (Steps 7–9) trägt `// <Prototyp>.dc.html:<Zeile>`; `heightIn(min = N)` behält N | teilweise geprüft (Heuristik): jedes `heightIn(min = N)` im Diff gegen `048cd7c` hat ein entferntes `height(N)`/`heightIn(min = N)` mit gleichem Wert in derselben Datei; `Box(Modifier.size(92.dp, 44.dp)…)` in `SlopeResult.kt` ändert nur Semantik; die Prototypzeile je Literal wurde nicht einzeln gegengelesen |
| I6 | Farben nur über Tokens | `scripts/lint-hex`, `gen-tokens --check`, `gen-icons --check` | erfüllt: `lint-hex` Exit 0 („sauber“), `gen-tokens --check` Exit 0, `gen-icons --check` Exit 0 |
| I7 | Kein sichtbarer Text hinzugefügt; einziger neuer nicht sichtbarer Text ist D11 | `git diff 048cd7c -- android/app/src/main/java/de/baupilot/screens/ScreenCopy.kt` zeigt nur `object Signature`; `ScreenCopyTest` | erfüllt: der Diff von `ScreenCopy.kt` gegen `048cd7c` fügt nur `object Signature` (`STATE_EMPTY`, `STATE_SIGNED`) hinzu; `ScreenCopyTest` grün |
| I8 | Zeilenkasten bei Skala 1 (und ≤ 1.05) unverändert | `LargeFontLineBoxTest.helpersUnchangedAtScale1` / `At104`; Gate nach Step 8 | erfüllt: `LargeFontLineBoxTest` 4 Tests grün, darunter `helpersUnchangedAtScale1` und `helpersUnchangedAt104` |
| I9 | Das Testurteil hängt nie vom Sidecar-Parsen ab; dieselbe Fehlerinstanz wird erneut geworfen | `sidecarWrittenOnFailureAndSameErrorRethrown` (`assertSame`), `sidecarParsesDecimalComma` | erfüllt: `sidecarWrittenOnFailureAndSameErrorRethrown` und `sidecarParsesDecimalComma` (`ReferenceSnapshotHandlerTest`) grün |
| I10 | Kein bei Step 2 grüner Snapshot wird rot | letzter `--baseline build/m7/android/baseline/summary.json`-Lauf: kein `test.passed true → false` in `changed` | erfüllt: letzter `--baseline`-Lauf Exit 4, 312 `geändert`-Zeilen in 34 Screens (je hell/dunkel), 0 × `test.passed: true -> false`, 52 × `false -> true`; die Änderungen stammen vom behaltenen Hebel 9a (`levers.md`) |
| I11 | Jeder M2–M6-Android-UI-Test besteht weiter (Änderungen nur wie in Step 5 gelistet) | Plan §12 | erfüllt mit Einschränkung: Gesamtlauf 733 Tests, 6 rot; keiner der 6 ist ein M2–M6-UI-Test (4 Snapshots EsBelegreife/FlBad, `LargeFontLayoutTest[Dokumente]`, `[DokViewer]`) |
| I12 | Galerie-Goldens und Kanarienvogel schreiben keinen Sidecar; Galerie-Urteile grün; ein Golden ändert sich nur durch einen behaltenen Hebel (D17) | `goldenWritesNoSidecar`, `canaryWithoutArtifactWritesNoSidecar`; `Gallery*SnapshotTest` grün | erfüllt: `goldenWritesNoSidecar`, `canaryWithoutArtifactWritesNoSidecar` grün; `GalleryLightSnapshotTest`, `GalleryDarkSnapshotTest` grün (0 rot) |
| I13 | Unter `ios/`, `docs/plans/`, `docs/milestones/` und `README.md` ändert sich nichts | `git diff --stat 048cd7c -- ios docs/plans docs/milestones README.md` leer | erfüllt: `git diff --stat 048cd7c -- ios docs/plans docs/milestones README.md` leer |
| I14 | Änderungen an `scripts/` sind rückwärtskompatibel | die zwei Kompatibilitätstests (`--all` ohne `--platform` mit 136 Zeilen; `validateCoverage` ohne `platforms`); die übernommenen Tests unverändert im Verhalten | erfüllt: `--all writes summary.md with 136 rows for a full synthetic tree` und `validateCoverage without a platforms flag checks both platforms` grün |
| I15 | Die `ios`-Felder von `link-coverage.json` sind gegenüber `fe905f7` unverändert | `jq -c '[.links[].ios, .placeholders[].ios]'` gleich für `fe905f7` und HEAD | erfüllt: `jq -c '[.links[].ios, .placeholders[].ios]'` für `fe905f7` und HEAD gleich (md5 `31419cbc5783007e56a03b24609b2231`) |
| I16 | Jedes Android-Platzhalter-`element` existiert als Tag in `src/main` | Drift-Prüfung im Test des echten Manifests | erfüllt: `the real manifest covers every pair and placeholder` grün |

## Befehle

An die Stelle des README-Abschnitts (D-R3). Aus dem Repo-Root. Android: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"` (das JBR aus Android Studio; `/usr/libexec/java_home` findet nichts), `ANDROID_HOME=~/Library/Android/sdk`; in einem frischen Worktree fehlt `android/local.properties` (git-ignoriert) und wird mit `sdk.dir=…` angelegt. Gradle immer mit `--continue`, weil rote Snapshots den Lauf sonst abbrechen.

```sh
(cd scripts && node --test test/)
scripts/gen-tokens --check && scripts/gen-icons --check && scripts/lint-hex && scripts/design-freeze --check

(cd android && ./gradlew --continue :app:testDebugUnitTest :app:compileReleaseKotlin)

scripts/diff-report --all --platform android                                   # Exit 0: 68 Zeilen, keine fehlt
scripts/diff-report --all --platform android --baseline build/m7/android/baseline/summary.json --unstable build/m7/android/baseline/unstable.json
# Exit 0, wenn kein Hebel behalten wurde; sonst Exit 4 mit `changed` nur in Zeilen behaltener Hebel und ohne test.passed true→false (I10)

git status --porcelain -- design fixtures scripts/icons.json                 # leer
git diff --stat 048cd7c -- ios docs/plans docs/milestones README.md          # leer
```

**Gate** (nach jedem Schritt 3 bis 8; muss mit Exit 0 enden):

```sh
(cd android && ./gradlew --continue :app:testDebugUnitTest --tests 'de.baupilot.snapshot.*SnapshotTest')
scripts/diff-report --all --platform android --baseline build/m7/android/baseline/summary.json --unstable build/m7/android/baseline/unstable.json
```

Exit-Codes von `diff-report`: 1 Referenz fehlt (gewinnt über 3 und 4), 2 Aufruf- oder PNG-Fehler, 3 fehlendes Ist-Bild oder fehlendes Ergebnis, 4 `--baseline` fand Änderungen (`actualSha256`, `test.passed`, `test.value`, `percentDifferent`, `offByTwoPercent`). Rote Zeilen allein machen den Exit-Code nicht ungleich 0.

**Gezielte Läufe** (je mit `--tests`): `'de.baupilot.app.LinkCoverageUiTest'`, `'*PlaceholderUiTest'`, `'*AccessibilityAudit*'`, `'*TouchTargetTest'`, `'*LargeFont*'`, `'*SemanticsTest'`, `'de.baupilot.snapshot.*SnapshotTest'`.

Laufzeiten (aus den Notizen): Snapshot-Lauf 14m 02s bis 16m 30s (`step2.md`); Gesamtlauf 19m17s (`step0.md`). Ein Lauf über 10 Minuten braucht den Hintergrundmodus mit langem Timeout.

## Operator-Checkliste

Manueller Lauf des Android Accessibility Scanners (R20). Der Fallback-Audit prüft keinen Farbkontrast; dieser Lauf schließt die Lücke R4. Ein Agent kann ihn nicht ausführen.

1. Gerät oder Emulator mit Android 8+ (API 26) vorbereiten; die Debug-App aus `android/` installieren (`./gradlew :app:installDebug` mit dem JBR aus „Befehle“).
2. Den „Accessibility Scanner“ aus dem Play Store installieren; unter Einstellungen → Bedienungshilfen aktivieren und die Berechtigungen erteilen (Schwebender Knopf sichtbar).
3. App starten. Die Screens in der Reihenfolge von `design/screens.md` öffnen (34 Screens, Nummern 1–34 wie in der Tabelle der Messwerte); jeden Screen **zweimal** prüfen: einmal im hellen, einmal im dunklen Modus (Systemeinstellung Design wechseln).
4. Auf jedem Screen den Scanner-Knopf antippen und „Häkchen / Aufnahme“ wählen; die Vorschläge des Scanners lesen.
5. Je Befund Screen, Modus, Element, Kategorie (Kontrast, Touch-Ziel, Beschriftung, Elementbeschreibung) und Scanner-Text notieren. Der Kontrast-Befund nennt Vorder- und Hintergrundfarbe sowie das Verhältnis; mit den Tokens aus `design/tokens.json` vergleichen.
6. Befunde, die nur Platzhalter-Links (`href="#"`) oder die FlBad-Fliesen (35,25 × 44 dp, ohne Aktion, M6 R11) betreffen, mit dieser Begründung vermerken, nicht stillschweigend übergehen.
7. Befunde sammeln und in diesem Dokument unter „Bekannte Lücken“ (R4) mit Datum, Gerät, Android-Version und Scanner-Version eintragen. Ein Befund, der ein Token-Paar betrifft, ist eine Änderung an `design/tokens.json` und damit außerhalb des Umfangs (I2): als eigene Aufgabe melden, die Toleranzen und Referenzen nicht anfassen.
8. Zusätzlich mit TalkBack einen Screen mit Eingabefeldern (Login) durchgehen: Jedes Feld soll als **ein** Halt mit seiner Beschriftung erscheinen (D-R7).
9. Das Ergebnis (keine Befunde / Liste der Befunde) schließt R4 und R20; bis dahin bleiben beide offen.

## Notiz

`docs/m5-offene-punkte.md` Zeile 74 (R11) und `docs/m6-offene-punkte.md` Zeile 80 (R11) sagen, die Android-Touch-Ziele würden mit `minimumInteractiveComponentSize` vergrößert. Das stimmt nicht: In `android/app/src/main` kommt `minimumInteractiveComponentSize` nirgends vor (`grep -rn minimumInteractive android/app/src/main`: keine Treffer; in diesem Lauf geprüft). Die Android-Ziele sind eigene `Modifier.clickable`; Compose erweitert die Zeigerprüfung eines `clickable` auf 48 dp, und `touchBoundsInRoot` enthält diese Erweiterung. `TouchTargetTest` misst genau das (≥ 48 × 48 dp − 0.5 px, ungemergter Baum), und `TextLinkTouchTargetTest` in `ComponentMetricsLaterTradesTest.kt` tut es schon seit M6 (Quelle: `docs/plans/10-11-26-m7-v3-android.md` §2.4). Die genannten Zeilen in m5 und m6 bleiben unverändert (D-R3); dieser Abschnitt ist die Korrektur. Die iOS-Teile der beiden Sätze (`contentShape`) sind davon nicht betroffen.

## Verifikation

Step 11, Arbeitsbaum von Branch `worca-cc/implement-the-plan-47b609b7` (Basis `048cd7c`), alle Befehle aus dem Repo-Root, `JAVA_HOME` auf das JBR aus Android Studio. Keine Quelldatei wurde in Step 11 geändert.

### Befehle und Ergebnisse

| Befehl | Ergebnis |
|---|---|
| `(cd scripts && node --test test/)` | 140 Tests, 140 grün, 0 rot (5,7 s) |
| `scripts/gen-tokens --check` | Exit 0 |
| `scripts/gen-icons --check` | Exit 0 |
| `scripts/lint-hex` | Exit 0 („sauber“) |
| `scripts/design-freeze --check` | Exit 0 (276 Dateien) |
| `(cd android && ./gradlew --continue :app:testDebugUnitTest :app:compileReleaseKotlin)` | `:app:testDebugUnitTest` **FAILED**: 733 Tests, 6 rot, 0 übersprungen (128 Ergebnisdateien); `:app:compileReleaseKotlin` erfolgreich; Gesamtdauer 20 min 33 s |
| `scripts/diff-report --all --platform android` | Exit 0: 68 Zeilen, 64 grün, 4 rot, 0 fehlt |
| `… --baseline build/m7/android/baseline/summary.json --unstable build/m7/android/baseline/unstable.json` | Exit 4 (erwartet, Hebel 9a behalten): 312 `geändert`-Zeilen in 34 Screens; 0 × `test.passed true → false`, 52 × `false → true` |
| `git status --porcelain -- design fixtures scripts/icons.json` | leer |
| `git diff --stat 048cd7c -- ios docs/plans docs/milestones README.md` | leer |

Laufzeit des `diff-report`-Paars: etwa 3 Minuten (zwei Läufe).

### Android-Tests nach Gruppe (aus dem Gesamtlauf, nicht einzeln erneut gestartet)

| Muster der gezielten Läufe | Tests | rot |
|---|---|---|
| `de.baupilot.app.LinkCoverageUiTest` | 19 | 0 |
| `*PlaceholderUiTest` | 29 | 0 |
| `*AccessibilityAudit*` (Hell 34, Dunkel 34) | 68 | 0 |
| `*TouchTargetTest` (`TouchTargetTest` 34, `TextLinkTouchTargetTest` 1) | 35 | 0 |
| `*LargeFont*` | 72 | 2 (`LargeFontLayoutTest[Dokumente]`, `[DokViewer]`, je `noClippedOrOverlappingTextAtFontScale130`) |
| `*SemanticsTest` | 8 | 0 |
| `de.baupilot.snapshot.*SnapshotTest` | 71 | 4 (EsBelegreife, FlBad, je hell/dunkel; 0.6343–0.6825 %) |

Die vier roten Snapshots und die zwei roten `LargeFontLayoutTest`-Fälle sind dieselben sechs wie nach Hebel 9a (`levers.md`).

### Abnahme (Plan §7)

| Kriterium | Stand | Beleg |
|---|---|---|
| AC1: 68 von 68 Android-Snapshots grün | **nicht erfüllt**: 64 von 68 | `totals.android` 64 / 4 / 0; rot: EsBelegreife und FlBad, je hell und dunkel (siehe „Messwerte“) |
| AC2 | Stand Step 11: **nicht erfüllt** (2 Nicht-Snapshot-Tests rot: `LargeFontLayoutTest[Dokumente]`, `[DokViewer]`). Nach Zyklus 2: beide grün (`*LargeFont*` 72 / 0); rot bleiben nur die zwei FlBad-Snapshots (AC1) | Gesamtlauf, „Nachbesserung“ |
| AC3: Audit | erfüllt für den Fallback-Audit: `AuditAllowlist.entries` und `staged` leer (`AccessibilityAuditTest.kt:32,35`), `AccessibilityAuditLightTest`/`DarkTest` je 34 grün, `TouchTargetTest` 34 grün. Farbkontrast ist nicht abgedeckt; der manuelle Scanner-Lauf (R20) ist ein Operator-Schritt und nicht ausgeführt | Gesamtlauf, Abschnitt „Audit“ |
| AC4 | erfüllt: `scripts`-Tests grün (140 in Step 11, 141 nach Zyklus 2) | Befehl oben |
| T1–T6 | `scripts`-Tests 140/140 grün (darunter T-DS-3, Links, `diff-report`) | Befehl oben |

Bekannte Lücken: R4/R20 (Scanner-Lauf, Kontrast), R25/AC1 (4 rote Snapshots), R-f (`LargeFontLayoutTest[DokViewer]`, `[Dokumente]`), R13, R1. Sie stehen unverändert unter „Bekannte Lücken“. Der Einleitungssatz oben ist im Zyklus 2 angepasst.

## Nachbesserung nach Review (Zyklus 2)

Befehle: `(cd android && ./gradlew --offline --continue :app:testDebugUnitTest --tests '*LargeFont*' --tests '*EsBelegreife*' --tests '*DokViewer*' --tests '*Dokumente*' --tests '*FlBad*' …)`, `scripts/diff-report --all --platform android`, `(cd scripts && node --test test/)` (141 grün).

- **Dokumente (M1):** `BaupilotListItem` hat den optionalen Parameter `titleTag`; `DocumentsScreen` setzt `documents.row.title` (ein Test-Tag, keine Pixeländerung). `LargeFontAllowlist.ellipsis` führt `IntendedEllipsis("Dokumente", "documents.row.title", "AndDokumente.dc.html:53")`. `LargeFontLayoutTest[Dokumente]` grün.
- **DokViewer (M2), Entscheidung (b):** Die Papiervorschau wird bereits bei Schriftskala 1 gelegt (`DocumentPreview`, `Density(fontScale = 1f)`; bei Skala 1 pixelgleich). Das senkte die Befunde von 19 auf 13. Gemessen (`TextLayoutResult`, Dichte 1): 11 Texte mit 10.00 dp in 9.00 dp Kasten (7-sp-Zeilen: Adresse, Angebotskopf Zeile 2, Positionen und Preise) und 2 Texte mit 13.00 dp in 12.00 dp (Gesamt brutto, Summe), also je 1.00 dp zu hoch. Ursache: Chrome-Kästen mit gebrochenen CSS-Höhen (7 sp × 1.35 = 9.45 dp) werden auf ganze dp gerastert; das gilt bei Skala 1 unverändert, ist also kein Large-Text-Effekt. Ein Kasten von 10 dp würde die Snapshot-Pixel verschieben. Darum führt `LargeFontAllowlist.fixedGeometryGaps` den Eintrag (`DokViewer`, Tag `docpreview.text`, höchstens 1.00 dp, nur Höhe); die Ellipsen-Liste bleibt unberührt. Mehr als 1.00 dp oder ein Breitenüberlauf bleibt ein Befund. `LargeFontLayoutTest[DokViewer]` grün; DokViewer-Snapshots bleiben grün.
- **EsBelegreife (M3):** `titleStyle = TitleStyle.Detail` (`AndEsBelegreife.dc.html:22`). Messung (`diff-report --all --platform android`): hell 0.6825 → grün, dunkel 0.6466 → grün (Wert `null` = innerhalb der Toleranz). Behalten.
- **FlBad (m5), gemessen am Referenz-PNG (Zeile y = 1103):** linke Kachelkanten Referenz 122 / 239 / 359 / 476 / 593 / 710 / 830 / 947 px, Ist vorher 122 / 239 / 356 / 474 / 592 / 710 / 828 / 946 px (Versatz 0 / 0 / −3 / −2 / −1 / 0 / −2 / −1 px). Chrome rastert jede Spaltenkante des Grids (Spur 35.25 dp, Teilung 39.25 dp) auf ganze dp; `Row` + `weight` rundet dagegen jede Breite auf ein Pixel. Behoben: `TileGrid` legt die Kacheln mit `Layout` und auf ganze dp gerundeten Kanten (`TileGrid.kt`, `AndFlBad.dc.html:51`). Danach stimmen alle acht Kanten und Breiten mit der Referenz überein. Testwert hell 0.6622 → 0.5866, dunkel 0.6343 → 0.5764: weiter über 0.5, also **rot**. Rest: der Absatz „Mit dem Griff eines Schraubendrehers …“ bricht vor „jede“ statt danach (Glyphen-Advances, R1) und das Pfeilglyph „↑“; Band y 800–900 trägt den größten Anteil der abweichenden Pixel.
- **Stand:** AC1 66 von 68 (`totals.android` 66 / 2 / 0; rot nur FlBad hell und dunkel). Galerie-Goldens und `TileGrid`-Tests unverändert grün.
- **Weitere Review-Punkte:** m1 (`AuditAllowlist.isAllowed` beachtet `dark`, neuer Test), m2/m3 (Zeilenkasten-Paare aus `src/main` abgeleitet, Skala wird bestätigt), m7 (`--platform` mit Baseline ohne `platform`: Exit 2; `after` einer fehlenden Zeile ist der Status), s1 (`ProtocolStub` entfernt). Nicht behandelt: m4 (der tote `ceil`-Zweig bleibt, die Abweichung ist oben vermerkt), m6, m9 (der Orchestrator committet), s2–s4.
