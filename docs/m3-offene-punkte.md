# M3 – Offene Punkte

Bekannte Lücken, Entscheidungen und Messwerte zu M3 (Gewerk, Dokumente, Dokumentenansicht, Angebote vergleichen, Mitteilungen). Gleicher Aufbau wie `m2-offene-punkte.md`.

## Baseline (vor M3, Branch `feature/m3`, Stand `54621c5`)

Befehle laut Plan §12, Android mit JDK 21 aus Android Studio (`JAVA_HOME`) und `ANDROID_HOME=~/Library/Android/sdk` (es gibt kein `local.properties` im Worktree).

| Suite | Ergebnis |
|---|---|
| `cd scripts && node --test test/` | 114 Tests, 114 grün |
| `gen-tokens --check`, `gen-icons --check`, `lint-hex`, `design-freeze --check` | alle grün (`lint-hex: sauber`; 276 Dateien in `design/`) |
| iOS `xcodebuild test` (iPhone 15, iOS 26.2) | 105 Tests, 8 rot, 97 grün |
| Android `./gradlew :app:testDebugUnitTest` | 112 Tests, 6 rot, 106 grün |

### Rote Tests (alle bekannte M2-R11-Reds, nichts Neues)

Gemeint ist der Glyphen-Raster-Rest aus `m2-offene-punkte.md` R11; die Toleranzen bleiben unverändert.

iOS (`ScreenSnapshotTests`, Übereinstimmung gemessen, gefordert 0,995):

| Test | Übereinstimmung |
|---|---|
| Login hell / dunkel | 0,9814 / 0,9802 |
| Home hell / dunkel | 0,9811 / 0,9802 |
| Zeitplan hell / dunkel | 0,9847 / 0,9841 |
| Profil hell / dunkel | 0,9841 / 0,9833 |

Android (Paparazzi, Abweichung gemessen, erlaubt 0,5 %):

| Test | Abweichung |
|---|---|
| Home hell / dunkel | 0,614 % / 0,578 % |
| Profil hell / dunkel | 0,748 % / 0,718 % |
| Zeitplan hell / dunkel | 0,562 % / 0,504 % |
| Login hell / dunkel | grün |

Alle übrigen Tests (Galerie, Goldens, Navigation, Schedule, Daten, Tokens, Fonts) sind grün. `scripts/diff-report` lief für beide Plattformen; Berichte liegen in `reports/` (git-ignoriert).

## Step 0 – iOS-Spike: gepushter Pfad im Snapshot-Host

**Frage:** Kann der iOS-Snapshot-Host (`ReferenceSnapshot.render` über `AppContent`) ein gepushtes Ziel rendern?

**Versuch:** Wegwerf-Test `PushedPathSpikeTests` (inzwischen gelöscht). Er baut `AppContent` wie `ScreenSnapshotTests`, mit `logIn(); push(.notifications)`, rendert hell über `ReferenceSnapshot.render` (390 × 844 pt, Scale 3) und schreibt das PNG.

**Beobachtung am PNG (1170 × 2532):** Es zeigt die Zurück-Leiste „‹ Übersicht“ und darunter den Titel „Mitteilungen“ (fett). Der Rest der Fläche ist leer (Hintergrund). Weder Home noch ein Animationszwischenbild, und keine Tab-Leiste (passend zu `showsTabBar == false`). Der Test lief grün (1 Test, 0 Fehler); die Konsole meldet nur „Unbalanced calls to begin/end appearance transitions“, ohne Wirkung auf das Bild.

**Ergebnis: bestanden.** Der Host rendert den gepushten Pfad (`NavigationStack(path:)`) ohne Fallback. Der in Plan Step 0.2 vorgesehene Fallback `AppShell.destinationFrame(route:tab:)` ist **nicht nötig** und wird in Phase 4 nicht gebaut.

Einschränkung: Der Spike hat nur den leeren Rumpf von „Mitteilungen“ gezeigt. Ob längere, scrollende Inhalte und Zeilenabstände im Host stabil rendern, zeigen erst die M3-Screen-Snapshots.

## Entscheidungen

Aus dem Plan (§3), in Kurzform. D1, D7, D11, D13 und D14 (Routen, Snapshot-Zustände, Zeilenhöhen, andere Gewerke, Branch-Zuschnitt) stehen nur im Plan.

| # | Entscheidung | Begründung |
|---|---|---|
| D2 | Jede UI-Navigation nutzt `open(route)` statt `push`: Steht die Route schon im Pfad des gewählten Tabs, wird der Pfad dahinter gekürzt (zurück zu ihr), sonst angehängt. `go(.push(r))` ruft `open(r)`, `go(.tab(t))` ruft `select(t)`. `push` bleibt als Primitiv (M2-Tests). | „Im Vergleich ansehen“ (Dokumentenansicht) und die Wagner-Spalte (Vergleich) verlinken gegenseitig. Mit `push` wüchse der Pfad ohne Grenze und hielte Duplikate; mit `open` enthält ein Pfad nie eine Route zweimal (I3). |
| D3 | Die iOS-Zurück-Beschriftung ist der Titel des vorherigen Pfadeintrags, beim ersten Eintrag das Tab-Label. Titel: Gewerk → Gewerkname, Vergleich → „Angebote vergleichen“, Dokument → Dokumenttitel, Mitteilungen → „Mitteilungen“. Reine Hilfsfunktion `RouteTitles.backLabel(path:index:tab:)`; ein `nil`-Index (die Ansicht wird gerade entfernt) fällt auf das Tab-Label zurück. Android bleibt bei „Zurück“ als Symbol. | Die Referenzen zeigen „Übersicht“, „Estrich“ und „Dokumente“. Die M2-Regel (immer Tab-Label) ergäbe auf dem Vergleich „Übersicht“. |
| D4 | Ein Ziel-Parser je Plattform (`RouteParser.target(_:field:)`) ersetzt `route(forAskTarget:)` / `parseTarget`. `defects` wird zu `.tab(.inspections)` (Platzhalter bis M4), `documents` zu `.tab(.documents)`. Unbekannte Zeichenketten schlagen beim Laden fehl, nicht beim Tippen. | Die M2-Parser lasen `trade/screed/compare` falsch. Ein Tab-Ziel muss ein Wechsel sein, kein Push. |
| D5 | Verknüpfungen folgen den Daten. Eine Zeile öffnet ein Dokument nur, wenn die Fixture dafür ein `viewer` hat (Wagner ja; Hahn und das ausstehende Lorenz-Angebot ohne Aktion). „Angebote vergleichen“ und „Im Vergleich ansehen“ öffnen den Vergleich nur, wenn es für das Gewerk einen gibt. Hinweiskarte, „Nach Gewerk“, Rechnungs-/Vertrags-/Planzeilen, „Termin bestätigt“, Teilen/Mehr/Hinzufügen, FAB, „Alle gelesen“, „Erinnern“, „Rückfrage“, „Wagner beauftragen“, „Gewerk oder Typ ändern“, die Android-Gewerk-Tabs und die iOS-Faktenzeilen haben keine Aktion. | Der Prototyp verlinkt Hahn auf den Wagner-Beispielviewer, der Vergleich hat für Hahn `href="#"`. Ein Hahn-Viewer bräuchte erfundenen Inhalt. `href="#"` bleibt sichtbar ohne Aktion (`CLAUDE.md`). |
| D6 | Keine Fixture-Änderung. Datenähnliche Texte kommen aus der Fixture, Template-Chrome und positionsabhängige Template-Regeln stehen mit `Datei:Zeile` in `ScreenCopy` (Gewerk-Schritte erledigt/offen, Android-Gewerk-Icons nach Position, iOS-Notiz „Frist 15.01.“, Art → Icon/Farbe). | M2-Präzedenz (D9). Die Fixture-Tests (Anti-Erfindung, `renderVals`-Abdeckung, exakte `documents`-Form) machen Fixture-Änderungen riskant. |
| D8 | Die Filter-Chips der Dokumente sind wählbar und filtern „Zuletzt hinzugefügt“ nach `kind` (Index 0 = alle, Index i ≥ 1 = `documents.categories[i−1].kind`). Die Zuordnung wird beim Laden geprüft (`label + " " + count == filters[i].text`, sonst `decoding("screens.Dokumente.<platform>.filters")`). Start ist der `selected`-Chip der Fixture, ein Index außerhalb zeigt alle. Die Suche nimmt Text an, filtert nicht (M7/M8). Zustand: iOS in `AppShell` (überlebt Tab-Wechsel), Android `rememberSaveable` im Provider je Route (D9). | M3 verlangt Filter-Chips; ein filternder Chip ist die unauffälligste Wirkung. Die Zähler bleiben die Fixture-Summen. |
| D9 | Android: Der `SaveableStateProvider`-Schlüssel ist `"<tab.route>/<Tiefe>/<kodierte oberste Route oder root>"`. Der Zustand des Elternbildschirms (Scroll, Filter) überlebt Push und Pop. Verwaiste Einträge geöffneter und wieder verlassener Routen werden nicht entfernt (kein `removeState`). Profil-Schalter überleben Tab-Wechsel weiter (Schlüssel `profile/0/root`). | Falle in `AppShell.kt`: ohne Tiefe im Schlüssel verlöre der Elternbildschirm seinen Zustand. Gesichert durch `documentsFilterSurvivesViewerRoundTrip`. Folge siehe R20. |
| D10 | Die Dokumentenseite nutzt acht Ergänzungs-Tokens (`scripts/tokens-supplement.json`, gleiche Einträge unter `ios.colors` und `android.colors`, hell = dunkel): `docPaper`, `docInk`, `docBrand`, `docMeta`, `docPos`, `docRule`, `docRuleLight`, `docShadow`; jeweils mit `source` = Prototypzeile. Kein Name kollidiert mit einem Kern-Schlüssel. | Die Seite ist immer weiß; keine Hex-Werte im App-Code (`lint-hex`). Gleicher Mechanismus wie `knob`/`fabShadow`; ein eigenes `docShadow` lässt `fabShadow` unberührt. |
| D12 | Die Tabellengeometrie ist rein: `ComparisonLayout.columns(cardWidth:padding:gap:labelColumn:)` liefert `(Label 104, Wert, Wert)` mit `Wert = (Kartenbreite − 2·Padding − Labelspalte − 2·Gap) / 2`. iOS 350/14/10 → 104 / 99 / 99, Android 358/16/10 → 104 / 101 / 101. Hervorhebung im Modell: `highlighted = a != b && !strong` (Zeilen 3–7: Estrichart, Dicke, Aufheizen, Termin, Zahlung). Gewicht: iOS fett 700 / sonst 500, Android 500 / 400. | Die Views rechnen nicht (M2 I9). Die Regel ist die „unterschiedliche, nicht fettgedruckte Werte“ aus `M3.md`. |
| D15 | Das Mitteilungen-Ziel `documents` wechselt zum Dokumente-Tab und lässt den Stapel von Home (`[notifications]`) unberührt. Wer später Home wählt, sieht wieder Mitteilungen. | M2 D6/I6: Ein Tab-Wechsel berührt nie einen Stapel. |
| D16 | Ein `PlatformText` wird nur auf der Plattform gelesen, die ihn zeigt (iOS: `offersSummary`, `pageFooter`; beide: Angebots-`meta`, Vergleich `hint`/`summary`/`pending`, Viewer-`subtitle`). Fehlt dort der Wert, wirft das Laden `decoding("<JSON-Pfad>")`, z. B. `decoding("trades.detail.offersSummary")`. Die andere Plattform liest den Wert nie. | Die Fixture hat `android: null` bei `offersSummary` und `pageFooter`. Ohne die Regel scheitert `AppData.load` auf Android oder iOS zeigt still „“. |
| D17 | Jede Kennung aus Plan §5.4 ist erreichbar, mit oder ohne Aktion. iOS: Elemente ohne Aktion bekommen vor ihrer Kennung `.accessibilityElement(children: .combine)`; `FilterChips` hat dafür den Parameter `identifiers:`. Android: `Modifier.testTag` am `modifier` der Komponente. | UI-Tests tippen auch auf Elemente ohne Aktion (Lorenz-Angebot, Mitteilung ohne Ziel, Filter-Chips). |

## Bekannte Lücken

- **R2 / AC1 (offen):** Die Screen-Snapshots der fünf M3-Screens liegen teils außerhalb der Toleranz. Aktueller Stand: siehe „Nachbesserung nach Review (Zyklus 2)“ (iOS 10 von 10 rot, Android 6 von 10 rot). Die Toleranzen sind unverändert (iOS 0,995 / 0,98, Android 0,5 %). Die gemessenen Layoutursachen sind behoben; was bleibt, ist je Plattform als Rest mit Zahlen benannt (iOS: R11-Glyphenraster, Android: ganzpixelige Glyphenvorschübe von Layoutlib), kein bekannter Layoutversatz mehr.
- **R15 / D15:** Das Ziel `documents` einer Mitteilung lässt Home auf Mitteilungen stehen. Wer vom Dokumente-Tab zurück zu Home wechselt, sieht wieder die Mitteilungen. Angenommen und dokumentiert.
- **R20 / D9:** Android behält die Zustände verlassener Routen. Wird dieselbe Route in gleicher Tiefe erneut geöffnet, stellt sie ihre letzte Scrollposition wieder her statt bei null zu beginnen. Es gibt nur wenige verschiedene Routen, daher angenommen; kein Test prüft eine frische Scrollposition.
- **R13:** Touch-Ziele unter 44/48 (iOS-Chips 36 mit Hit-Bereich −4, Android-Chips 32) folgen der M1-Konvention; Geräteprüfung in M7.

## Step 0 – Ergebnis für die Umsetzung

Der Spike war bestanden; der Fallback `AppShell.destinationFrame(route:tab:)` wurde **nicht** gebaut und nicht benutzt. Alle fünf M3-Screens werden im Snapshot-Host über den echten `NavigationStack` mit gepushtem Pfad gerendert (Zustände nach D7).

## Verifikation

Stand am Ende von Phase 13, **vor** den Reviews (überholt durch die Tabellen unter „Nachbesserung nach Review“); Lauf von `ScreenSnapshotTests` (iOS, iPhone 15) und `de.baupilot.snapshot.*SnapshotTest` (Android, JDK 21 aus Android Studio). Gemessen aus den Testausgaben; „Diff“ ist die Spalte „differentPixels in %“ aus `scripts/diff-report --platform ios|android` (exakt abweichende Pixel, strenger als die Toleranz und nicht damit vergleichbar). Die Toleranz-Messwerte sind iOS „Übereinstimmung“ (gefordert ≥ 0,995) und Android „Abweichung“ (erlaubt ≤ 0,5 %).

| # | Screen | Modus | iOS Übereinstimmung | iOS Diff | Android Abweichung | Android Diff |
|---|---|---|---|---|---|---|
| 1 | Gewerk | hell | 0,9759 rot | 9,2692 % | 0,5039 % rot | 1,8883 % |
| 2 | Gewerk | dunkel | 0,9749 rot | 9,2855 % | grün (≤ 0,5 %) | 1,8880 % |
| 3 | Dokumente | hell | 0,9738 rot | 12,6354 % | 1,1891 % rot | 3,6340 % |
| 4 | Dokumente | dunkel | 0,9729 rot | 12,6544 % | 1,0858 % rot | 3,5410 % |
| 5 | Dokumentenansicht | hell | 0,9674 rot | 11,9007 % | 0,8181 % rot | 2,8082 % |
| 6 | Dokumentenansicht | dunkel | 0,9670 rot | 10,2049 % | 0,8019 % rot | 2,8097 % |
| 7 | Angebote vergleichen | hell | 0,9582 rot | 10,9520 % | 0,7744 % rot | 2,5915 % |
| 8 | Angebote vergleichen | dunkel | 0,9568 rot | 10,9624 % | 0,7004 % rot | 2,5901 % |
| 9 | Mitteilungen | hell | 0,9429 rot | 8,5708 % | 0,5650 % rot | 2,1095 % |
| 10 | Mitteilungen | dunkel | 0,9171 rot | 8,5684 % | 0,5390 % rot | 2,1088 % |

Das sind 5 Screens × 2 Plattformen × 2 Modi = 20 Messungen. **Grün ist 1 von 20** (Android Gewerk dunkel), **19 sind rot**. AC1 ist nicht erfüllt. Für Android Gewerk dunkel meldet Paparazzi nur „kein Fehler“; ein Prozentwert unter der Schwelle wird nicht ausgegeben.

Zur Einordnung (gleiche Läufe, M2-Screens): iOS Login 0,9814 / 0,9802, Home 0,9811 / 0,9802, Profil 0,9841 / 0,9833, Zeitplan 0,9847 / 0,9841 (alle rot, unverändert gegenüber der Baseline); Android Login hell/dunkel grün, Home 0,614 / 0,578 %, Profil 0,748 / 0,718 %, Zeitplan 0,562 / 0,504 % (rot, unverändert gegenüber der Baseline). iOS-Lauf: 18 Tests, 18 rot. Android-Lauf: 21 Tests (18 Screens, 2 Galerie, 1 Referenz-Kanarienvogel), 15 rot; Galerie und Kanarienvogel grün.

### Testzahlen des Endlaufs (Phase 14)

Befehle laut Plan §12 am Ende von Phase 14, `JAVA_HOME` = JDK 21 aus Android Studio. Es wurde nichts repariert: Kein Befehl scheiterte aus einem Integrationsgrund, alle Reds sind Snapshot-Toleranzverletzungen.

| # | Schritt | Exit | Ergebnis |
|---|---|---|---|
| 1 | `cd scripts && node --test test/` | 0 | 114 Tests, 114 grün (Baseline 114) |
| 2 | `gen-tokens --check`, `gen-icons --check`, `lint-hex`, `design-freeze --check` | 0 | alle grün (`lint-hex: sauber`; 276 Dateien in `design/`) |
| 3 | `xcodegen generate`; `xcodebuild test` (iPhone 15) | xcodegen 0, xcodebuild 65 | `BaupilotTests`: 169 Tests, 18 rot, 151 grün. `BaupilotUITests`: 18 Tests, 0 rot (`AppNavigationUITests` 7, `TradeFlowUITests` 11) |
| 4 | `xcodebuild build -configuration Release` (generic iOS Simulator) | 0 | BUILD SUCCEEDED |
| 5 | `./gradlew :app:testDebugUnitTest :app:compileReleaseKotlin` | 1 | 190 Tests, 15 rot, 175 grün; `testDebugUnitTest` schlug fehl, `compileReleaseKotlin` lief deshalb im selben Aufruf nicht. Einzeln gestartet: Exit 0, BUILD SUCCESSFUL |
| 6 | `scripts/diff-report --platform ios` / `--platform android` | 0 / 0 | beide Berichte erzeugt (18 Screens je Plattform) |
| 7 | `git status --porcelain` | | 106 Einträge, alle `A`/`M`; keine Änderung unter `design/` oder `fixtures/`; die Spike-Datei `PushedPathSpikeTests` ist weg |

Vergleich mit der Baseline: iOS 105 → 169 Unit-Tests (8 → 18 rot, die zehn neuen Reds sind die zehn M3-Snapshots), plus 18 UI-Tests grün. Android 112 → 190 Tests (6 → 15 rot; Gewerk dunkel ist grün; die neun neuen Reds sind M3-Snapshots: Gewerk hell, Dokumente, DokViewer, Vergleich, Mitteilungen hell/dunkel). Die Werte der M2-Screens sind gegenüber der Baseline unverändert.

**Rote Tests iOS (18, alle `ScreenSnapshotTests`):** Login, Home, Zeitplan, Profil, Gewerk, Dokumente, DokViewer, Vergleich, Mitteilungen, jeweils hell und dunkel. Übereinstimmung wie in der Tabelle oben; M2-Screens wie in der Baseline.

**Rote Tests Android (15, Paparazzi):** Home, Profil, Zeitplan, Gewerk (nur hell), Dokumente, DokViewer, Vergleich, Mitteilungen, jeweils hell und dunkel. Abweichung wie in der Tabelle oben, Zeitplan dunkel 0,5038 % (Baseline 0,504 %). Grün: Login hell/dunkel, Gewerk dunkel, Galerie, Goldens, Referenz-Kanarienvogel.

**Akzeptanz:** AC1 ist **nicht erfüllt**: 1 von 20 M3-Snapshots grün (Android Gewerk dunkel), 19 rot. Die 20 M3-Bilder liegen unter `build/snapshots/{ios,android}/` (je 18 Dateien mit den M2-Screens). Die UI-Tests `TradeFlowUITests` (iOS, 11) und `TradeFlowUiTest` (Android, 13) liefen und sind grün. Die Reds sind nicht als reiner R11-Glyphenrest belegt (siehe R2).

**Beobachtung:** Der Eintrag `android/.kotlin/errors/errors-1794171013647.log` (Kotlin-Daemon-Fehlerprotokoll) stand im Index. Er wurde aus dem Index genommen, gelöscht, und `android/.kotlin/` steht jetzt in `.gitignore`.

## Nachbesserung nach Review (Zyklus 1)

Behoben (Ursache gemessen am Referenz-Render, 3 px = 1 pt):

- **iOS `.multi`-Text (C1):** `interText(..., lines: .block)` (neuer Fall; `.multi` bleibt für `StepRow` und `BottomActionBar`, deren Goldens darauf abgestimmt sind) setzt jetzt die Zeilenbox wie CSS: Der erste Grundlinienabstand entspricht dem von Chrome (oben `baseline − natürlicher Ascent`), der Rest des Zeilenabstands (`box − natürliche Höhe`) liegt unterhalb des Blocks, ein Block aus n Zeilen ist so n Zeilenboxen hoch. Die `DocumentPreview`-Zeilen sind Einzeiler und nutzen `.single`. Genutzt wird `.block` von `NotificationRow`, `ComparisonTable` und (über den Parameter `textLines`) vom Vergleich-`InfoCallout`; die `InfoCallout`s der M2-Galerie bleiben bei `.multi`. Neu aufgenommen (`BAUPILOT_RECORD_GOLDENS=1`, bei `xcodebuild` als `TEST_RUNNER_BAUPILOT_RECORD_GOLDENS=1`) wurden die vier M3-Galerie-Goldens (Trade, Dokumente, Vergleich, Mitteilungen) sowie `GalleryStatusInput` und `GalleryTiles`, hell und dunkel: Dort stecken `SearchField` und `FactStrip(.divided)`, deren Position sich mit M1 (2 pt / 1 pt) bewusst ändert. Die M2-Screens verwenden beide nicht; ihre Snapshot-Werte blieben unverändert.
- **iOS Fakten-Streifen (M1):** Spalten ab Index 1 beginnen 1 pt später (`border-left` liegt in der Spalte). Suchfeld: 2 pt Innenabstand wie beim `<input>`.
- **iOS Vergleich Hahn-Kopf (M4):** Der Kopf ohne Aktion trägt `compare.column.offer-hahn` mit `.accessibilityElement(children: .combine)`.
- **Android (M2):** Zeilen der Dokumentvorschau aus der laufenden Summe der Bruchhöhe (9,45 dp + 8 dp je Zeile) statt je Text aufgerundet; Vergleich-Schaltflächen teilen die Breite nach Abzug der 2 dp Rand (`ASK_WEIGHT`/`AWARD_WEIGHT`); Suchfeld 2 dp Innenabstand.
- **Android `DetailScreenScaffold` (M3):** Die Leiste wird zuerst gemessen (`SubcomposeLayout`), ihre Höhe kommt als Fußabstand unter den scrollenden Inhalt; Test `detailScaffoldReservesTheBottomBar` (400 dp hohe Fläche).
- **Kleinteile:** m2 (`RouteTitles`), m3 (Filter-Kennungen aus `kind`), m4 (Titel des Vergleich-Platzhalters), m5 (Prüfung `facts ≥ 3` beim Laden), m6, m7, m8, m9, m10.

iOS-Übereinstimmung nach der Nachbesserung (vorher in Klammern; M2-Screens unverändert: Login 0,9814, Home 0,9811, Profil 0,9841):

| Screen | hell | dunkel |
|---|---|---|
| Gewerk | 0,9790 (0,9759) | 0,9780 (0,9749) |
| Dokumente | 0,9768 (0,9738) | 0,9760 (0,9729) |
| Dokumentenansicht | 0,9832 (0,9674) | 0,9828 (0,9670) |
| Angebote vergleichen | 0,9686 (0,9582) | 0,9673 (0,9568) |
| Mitteilungen | 0,9656 (0,9429) | 0,9622 (0,9171) |

Android-Abweichung nach der Nachbesserung (vorher in Klammern; erlaubt 0,5 %; alle weiterhin rot außer Gewerk dunkel): Gewerk hell 0,5039 % (0,5039 %), Dokumente 1,100 % / 1,027 % (1,189 / 1,086), Dokumentenansicht 0,786 % / 0,770 % (0,818 / 0,802), Vergleich 0,691 % / 0,626 % (0,774 / 0,700), Mitteilungen 0,565 % / 0,539 % (unverändert). Die halbpixelgenaue Zentrierung (m1, Titel 19,5 dp, `ListItem` 17,5 dp, `ActionRow` 9,5 dp) ist nicht untersucht und bleibt als Verdacht beim Glyphen-Rest R11.

**Verbleibender Rest (iOS), Stand Zyklus 1:** Mitteilungen und Vergleich lagen noch unter M2-Niveau (Zeilenkante bis 6 px, andere Umbrüche). Das ist in Zyklus 2 behoben, siehe unten.

## Nachbesserung nach Review (Zyklus 2)

Behoben (Ursache gemessen am Referenz-Render gegen den Snapshot, Pixelprofile von Referenz und Snapshot, 3 px = 1 pt/dp):

- **iOS `.block` (Major 1):** `interText(..., lines: .block)` legt den Text jetzt in `BlockTextLayout` (`Layout`). Es misst den `Text`, zählt die Zeilen (`n = round((h + leading) / (natürliche Zeile + leading))`) und meldet genau `n × Zeilenbox`; SwiftUI rundet die Höhe eines mehrzeiligen `Text` auf das Pixelraster auf, und bisher addierte sich das je Zeile (⅓ pt). Test `testInterTextBlockIsExactlyNLineBoxesTall` (14/1,5-Text, 1 und mehrere Zeilen: Höhe ein Vielfaches von 21 pt).
- **iOS Mitteilungen-Text (Major 1):** Der Text der `NotificationRow` läuft über `GreedyLabel` (`lineBreakStrategy = []`): Chrome bricht „… angefragt am / 05.12.“ und „… ab KW / 3“. `GreedyLabel` nimmt jetzt ein `LineHeight` (Grundlinie mit abgerundetem Halb-Leading wie `LineHeight.box`) und meldet `round(h / box) · box`. Dazu `TopAlignedLabel`: `UILabel` zentriert den Text senkrecht und ließ in drei von fünf Zeilen die letzte Zeile weg, wenn SwiftUI den Rahmen um ein Pixel unter 37,8 pt rundete; der Text wird jetzt oben im Rahmen gezeichnet.
- **iOS Vergleich-Wertzellen (Major 1):** 1 pt Innenabstand oben (Zelle 18 statt 17 pt, mittig in 40 → Text bei 12 statt 11,5, wie Chrome). Neu aufgenommen wurden die M3-Galerie-Goldens (Trade, Dokumente, Vergleich, Mitteilungen); die M2-Goldens bleiben unverändert grün.
- **Android Zentrierung auf ganze dp (Major 2):** `RowScope.wholeDpCentered()` (`WholeDpCenter.kt`, `offsetPx = (floor((space − size) / (2 · density) + 0.5) · density).roundToInt()`) an den Textspalten von `TopAppBar`, `ListItem`, `SearchBar`, `HintCard`, `FactRow`, `ComparisonTable`, `ActionRow` und am Preis der Dokumentenansicht. Boxen (Zeilen, Icon-Kreise, Karten) bleiben bei `CenterVertically`; sie waren pixelgenau. Test `WholeDpCenterTest`. Die Galerie-Goldens wurden neu aufgenommen (Bars, BarsMore, Chips, Inputs, Lists, Settings und die M3-Seiten).
- **Android `DocumentPreview` (Major 3):** Alle Blöcke der Seite laufen über `WholeDpGrid`: Je Block `round(Ende) − round(Anfang)` der laufenden Bruchsumme (86,55 dp Tabellenanfang, 18,45 dp je Zeile). Die Linien liegen bei `round(86,55 + 18,45 k)` dp ab Seitenanfang (87/105/123/142/160/179) und die Summenlinie bei `round(306 − 20,15) = 286`. Test `previewGridPutsTheRulesOnChromesWholeDp`.
- **Android Chips (Major 3):** `FilterChip` und `TagChip` sind so breit wie das ganze dp unter ihrer natürlichen Breite (`wholeDpWidth`); die Dokumente-Filterzeile hat in der Referenz 67/98/91 dp, die natürliche 67,67/98,33/92. Das ist eine gemessene Näherung (Chromes Vorschübe sind schmaler als die ganzpixeligen von Layoutlib), keine exakte Regel: das dritte Chip ist um 1 dp zu breit (92 statt 91). `AssistChip` bleibt wie M1/M2.
- **Android Vergleich-Schaltflächen (Major 3):** Gewichte 145 : 201 dp (Chrome malt die Naht bei 161,33 als 161).

iOS-Übereinstimmung (gefordert ≥ 0,995; Zyklus 1 in Klammern; M2-Screens unverändert: Login 0,9814 / 0,9802, Home 0,9811 / 0,9802, Profil 0,9841 / 0,9833, Zeitplan 0,9847 / 0,9841):

| Screen | hell | dunkel | Diff hell / dunkel (`diff-report`) |
|---|---|---|---|
| Gewerk | 0,9790 (0,9790) | 0,9780 (0,9780) | 8,98 % / 9,00 % |
| Dokumente | 0,9768 (0,9768) | 0,9760 (0,9760) | 12,34 % / 12,37 % |
| Dokumentenansicht | 0,9832 (0,9832) | 0,9828 (0,9828) | 10,39 % / 8,69 % |
| Angebote vergleichen | 0,9780 (0,9686) | 0,9770 (0,9673) | 9,05 % / 9,07 % |
| Mitteilungen | 0,9763 (0,9656) | 0,9747 (0,9622) | 2,89 % / 2,90 % |

Android-Abweichung (erlaubt ≤ 0,5 %; Zyklus 1 in Klammern; „grün“ = Paparazzi meldet keinen Fehler):

| Screen | hell | dunkel | Diff hell / dunkel (`diff-report`) |
|---|---|---|---|
| Gewerk | grün (0,5039 %) | grün | 1,84 % / 1,83 % |
| Dokumente | 0,7175 % (1,100 %) | 0,6551 % (1,027 %) | 2,89 % / 2,80 % |
| Dokumentenansicht | grün (0,786 %) | grün (0,770 %) | 1,70 % / 1,70 % |
| Angebote vergleichen | 0,5868 % (0,691 %) | 0,5206 % (0,626 %) | 2,31 % / 2,30 % |
| Mitteilungen | 0,5483 % (0,565 %) | 0,5227 % (0,539 %) | 2,09 % / 2,09 % |

M2-Screens Android (Baseline in Klammern, I14 erlaubt Verbesserungen): Home hell 0,5017 % (0,614 %), dunkel grün (0,578 %); Profil 0,5647 % / 0,5385 % (0,748 % / 0,718 %); Zeitplan hell 0,5274 % (0,562 %), dunkel grün (0,504 %); Login grün. M2-Screens iOS unverändert.

**Stand AC1:** iOS 0 von 10 grün, Android 4 von 10 grün (Gewerk, Dokumentenansicht, je hell und dunkel): 4 von 20, 16 rot. Die Toleranzen sind nicht gelockert.

**Rest, nach Ursache getrennt:**

- **Layout (behoben):** Messung per Zeilenband-Verschiebung (Schritte ±3 px je 24-px-Band, Referenz gegen Snapshot) findet auf Android in Dokumente, Vergleich und Mitteilungen kein Band mehr, dessen Abweichung eine senkrechte Verschiebung um ≥ 20 % senkt. iOS: Mitteilungen und Vergleich liegen im Mittel auf M2-Niveau (0,975–0,983); einzelne Kanten sind ±1 px entfernt, weil Chrome Zeilen mit 83,78 pt setzt und SwiftUI auf das Pixelraster rundet (84 pt).
- **Rest R11 / R2 (Glyphen):** iOS: Glyphenraster von CoreText gegen Skia, wie in M2 (0,975–0,983). Android: Layoutlib setzt Glyphenvorschübe ganzpixelig, Chrome mit Bruchteilen; dadurch laufen Wörter bis ± 1 px auseinander. Zahlen oben (Android Dokumente 0,66–0,72 %, Vergleich 0,52–0,59 %, Mitteilungen 0,52–0,55 %). `TextMotion.Animated` ist ein ungetesteter Hebel dafür.
- **Offen, kleiner:** Das dritte Chip der Dokumente-Filterzeile ist 1 dp zu breit (Chrome 91, hier 92; Näherung siehe oben). `CompareScreen` übergibt weiter 358 dp an `ComparisonLayout.columns` (für Geräte unter 390 dp breit; Suggestion s1 aus Zyklus 1).

### Testzahlen nach Zyklus 2

| Schritt | Ergebnis |
|---|---|
| iOS `xcodebuild test` (iPhone 15) | `BaupilotTests`: 170 Tests, 18 rot (alle `ScreenSnapshotTests`), 152 grün; `BaupilotUITests`: 18 Tests grün |
| Android `./gradlew :app:testDebugUnitTest :app:compileReleaseKotlin --continue` | 195 Tests, 10 rot (alle Paparazzi-Screens: Dokumente, Vergleich, Mitteilungen je hell/dunkel, Home hell, Profil hell/dunkel, Zeitplan hell), Galerien, Metriken, `WholeDpCenterTest`, `AppDataTest`, `TradeFlowUiTest` grün; `compileReleaseKotlin` grün |
