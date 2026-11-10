# M6 · Offene Punkte

## Baseline (Step 0)

Gemessen auf `a2f1798` (Branch `feat/m6`, Worktree sauber; `feat/m6` enthält M5 bereits, es wurde nichts gemergt, D0). Befehle aus Plan §12, iOS mit `-derivedDataPath /tmp/m6-p1-dd`, Android mit JDK 21 aus Android Studio und `ANDROID_HOME=~/Library/Android/sdk`. Referenz für den Vergleich in den späteren M6-Steps. Die Werte sind die beobachteten, nicht die erwarteten.

### Zählung

| Block | Ergebnis |
|---|---|
| `cd scripts && node --test test/` | 114 Tests, 114 grün |
| `scripts/gen-tokens --check`, `scripts/gen-icons --check` | grün (Exit 0) |
| `scripts/lint-hex` | `lint-hex: sauber` |
| `scripts/design-freeze --check` | `design/ matches scripts/design-v1.sha256 (276 files)` |
| iOS `BaupilotTests` (Unit, Gallery, Metrik, Snapshots) | 368 Tests, 322 grün, **46 rot** (alle 46 `ScreenSnapshotTests`) |
| iOS `BaupilotUITests` | 32 / 32 grün (`AppNavigationUITests` 7, `EarlyTradesUITests` 5, `InspectionFlowUITests` 9, `TradeFlowUITests` 11) |
| iOS gesamt | 400 Tests, 354 grün, 46 rot (`xcodebuild` Exit 65) |
| iOS Release-Build (`generic/platform=iOS Simulator`) | `BUILD SUCCEEDED` |
| Android `:app:testDebugUnitTest` | 372 Tests, 335 grün, **37 rot** (alle 37 `*SnapshotTest`), 0 übersprungen |
| Android `:app:compileReleaseKotlin` | grün (Gradle-Gesamtlauf Exit 1 nur wegen der roten Snapshots, `--continue`) |
| `scripts/diff-report --platform ios`, `--platform android` | beide Exit 0 |
| `git status --porcelain` nach allen Läufen | leer |
| `git status --porcelain -- design fixtures scripts/icons.json` | leer |

Nicht-Snapshot-Anteil: iOS 0 rot (`MeasureViewModelTests` 8, `M5ConsistencyTests` 2, `AppNavigationTests`, `AppDataTests` grün). Android 0 rot, darunter `AppDataTest` 78, `AppNavigationStateTest` 37, `MeasureViewModelTest` 9, `M5ConsistencyTest` 4 und die Robolectric-UI-Tests `AppNavigationUiTest` 9, `TradeFlowUiTest` 13, `InspectionFlowUiTest` 9, `EarlyTradesUiTest` 5, `BaupilotRootLoadTest` 3.

### Vergleich mit „Testzahlen“ in `docs/m5-offene-punkte.md`

| Größe | Erwartet (M5-Endstand) | Beobachtet |
|---|---|---|
| scripts | 114 / 114 grün | 114 / 114 grün |
| `lint-hex` | `lint-hex: sauber` | `lint-hex: sauber` |
| `design-freeze --check` | 276 Dateien | 276 Dateien |
| iOS `BaupilotTests` | 368 Tests, 46 rot | 368 Tests, 46 rot |
| iOS `BaupilotUITests` | 32 / 32 | 32 / 32 |
| iOS Release-Build | `BUILD SUCCEEDED` | `BUILD SUCCEEDED` |
| Android `:app:testDebugUnitTest` | 372 Tests, 37 rot | 372 Tests, 37 rot |
| Android `:app:compileReleaseKotlin` | grün | grün |

### Abweichungen

Keine. Alle Zahlen entsprechen dem M5-Endstand (Plan §2: scripts 114/114, iOS 368 Unit / 46 Snapshot-Rote, UI 32/32, Android 372 Tests / 37 Snapshot-Rote).

### Untersuchte Rote

Kein Rot über die bekannten 46 iOS / 37 Android M2–M5-Snapshot-Roten hinaus; es gab nichts zu untersuchen.

iOS (46): alle in `BaupilotTests.ScreenSnapshotTests` (30 M2–M4 + 16 M5). Android (37): alle `*SnapshotTest` in `de.baupilot.snapshot` (22 M2–M4 + 15 M5). Ursachen unverändert wie in `docs/m5-offene-punkte.md` (Glyphen-Raster R1, iOS Pixelraster R19). Die Toleranzen sind unverändert.

## Entscheidungen

Aus dem Plan (§3), in Kurzform. D0, D2, D5, D9, D10, D11, D13, D15, D16 und D17 (Basis ohne Merge, neue Maps und Lader, Mangel-Einstiege, Verknüpfungen, Snapshot-Zustände, iOS-Zurück-Beschriftungen, Umbruch, Branch-Zuschnitt, Plattform-Nullwerte, SanBad-Feldzuordnung) stehen nur im Plan.

| # | Entscheidung | Begründung |
|---|---|---|
| D1 | Neue Prüfpunkt-Route: iOS `AppRoute.inspectionCheckItem(tradeId:check:item:)`, Android `AppRoute.InspectionCheckItem(tradeId, check, item)`. Grammatik `inspection/<t>/<check>/<item>` (genau 4 Teile), wenn `parts[2]` nicht `room` / `sign` / `step` und `parts[3]` nicht `sign` / `step` / `room` ist und alle Teile die Id-Regel erfüllen; `inspection/<t>/room/<r>` behält den Vorrang. Android-Kodierung `item:<t>/<c>/<i>`, entschlüsselt durch eine geprüfte Aufteilung in genau 3 Teile; `check:a/b/c` bleibt abgelehnt. Das Fixture-Modell ist `byTrade.<t>.checks.<check>`, `<item>` benennt einen Abschnitt der Messung. | `screens.md` führt `inspection/plumbing/slope/{sec}`; die Fixture-Zeile nutzt `slope/A3`. Ein eigener Fall lässt `inspectionCheck` (3 Teile) und alle M5-Schlüsselmengen unverändert; die reservierten Wörter halten alle bisherigen Negativfälle fehlerhaft. |
| D3 | Gefälle ist ein reiner Werttyp `SlopeViewModel` (iOS Struct, Android unveränderliche Data-Class). Zustand: Ganzzahlen `lenDm`, `dropMm` aus `lengthRange` / `dropRange`; Soll `round(default × 10)` Zehntel, begrenzt auf 5…30. Regeln: ausreichend ⇔ `10 × dropMm ≥ sollTenths × lenDm` (inklusive); Prozent in Hundertsteln `(200 × dropMm + lenDm) / (2 × lenDm)` mit Ganzzahldivision (halb auf, wie JS `toFixed(2)`, auch am exakten Binärgleichstand `lenDm 40` → „1,13 %“); Texte mit Komma ohne Tausendertrennung. Stepper ±0,5 m und ±0,5 cm, an der Grenze ein No-op. Nie `String(format:)`, `%.2f` oder ein Locale-Formatierer. | M6.md: „Logik als ViewModel nachbauen und per Unit-Test absichern“. Ganzzahlen beseitigen Float- und Locale-Abweichungen (R4). |
| D4 | Ebenheit ist ein reiner Werttyp `FlatnessViewModel`. Start aus `measurements.screedFlatness`: Grenzwert `clamp(default, min, max)` (4 mm, 1–15), Spalt 3, Punkte [2, 3, 6], Spalt 0…20 in Schritten von 1. Regeln: innerhalb ⇔ `gap ≤ limit` (inklusive), Punkt über dem Grenzwert ⇔ `p > limit` (strikt); „M1 · 2 mm“ ist ein einziger String; „Messpunkt speichern“ hängt den aktuellen Spalt an und setzt ihn nicht zurück. | M6.md und AC2; „Messpunkt hinzufügen“ ist das `points.concat([gap])` des Prototyps. |
| D6 | Fünf Ergänzungs-Tokens in `scripts/tokens-supplement.json` (hell = dunkel, `source` = Prototypzeile): `paintWhite`, `paintSand`, `paintSage`, `lampGlow` auf beiden Plattformen, `onDangerFill` nur auf iOS. Kein anderer Token, keine Fixture-, Icon- oder Design-Änderung. | Keine Hex-Werte im App-Code (`lint-hex`); der einzige zulässige Weg für Farben außerhalb von `design/tokens.json` ist die Ergänzung (M3 D10 / M4 D11). `onPhoto` hat denselben Wert, heißt aber „Text auf Foto“; ein eigener Name hält die Quellzeile ehrlich. |
| D7 | Statusabbildungen stehen als Tabellen mit Quellzeile in `ScreenCopy`; ein unbekannter Wert ist ein Ladefehler am benannten Pfad: Zeilenton der vier Übersichten („In Ordnung“, „Offen“, „Läuft“, „Heute“, „Geplant“, „Pflicht“, „N Mangel“), Phasenart (nur die M6-Übersichten; eine `statusLabel` ohne „Abgenommen“ ist **geplant**), genau eines von `phases` / `facts`, Sanitär-Zeilensymbole, Maler-Farbfeld aus dem ersten Wort der Zeile, Gefälle-Abschnittsstatus, SanBad-Objekt- und MaRaum-Wandstatus, Fliesenstatus (3 × 8), Heizbalkenart. | Die Fixture führt Beschriftungen statt Statusschlüssel (M5 D7). Eine ausdrückliche Tabelle mit Ladefehler ist sicherer als ein stiller Standard. |
| D8 | Fest verdrahtete Anteile und Konstanten in `ScreenCopy`, mit Quellzeile: aktive Phase Sanitär **0,50**, Estrich **0,75**, Fliesen **0,50**; Timer-Ring **0,58**; Android-Fortschritt oben SanDruck **0,50**, FlBad **0,42**, MaRaum **0,30**; Heizbalken `round(v / 45 × 72)`; FlBad-Raster 8 × 3; MaRaum-Farbfeld `paintSand`. | Der Prototyp verdrahtet sie. Ein Konsistenztest (`M6ConsistencyTests` / `M6ConsistencyTest`) prüft jeden Anteil gegen die Fixture, wo ein Verhältnis besteht; abgeleitet werden sie nicht. |
| D12 | Lokaler, ungespeicherter Zustand (wie M5 D12): `SlopeViewModel`, `FlatnessViewModel` und das SanDruck-Feld „Ende“ (echte Eingabe, leer, Platzhalter „– bar“). **Ohne Aktion, sichtbar und als Schaltflächen erreichbar:** die FlBad-Fliesen, „Keine Hohlstellen“, „Speichern“, Foto, „Tag 6 eintragen“, SanBad-Tabs und ✓/✕, MaRaum-Wandchips und „In Ordnung“, Gefälle-Abschnittskacheln. **Antwort auf die Rückfrage (Q&A „flbad-tile-tap“):** M6.md verlangt „antippbare Fliesen“, der Prototyp hat keinen Tap-Handler; entschieden wurde, dass die Fliesen zugängliche Schaltflächen ohne Aktion sind (wie die M5-Steckdosenkacheln), „antippbar“ ist also nur über die Schaltflächenrolle erfüllt. | Die Antwort des Nutzers für die Fliesen; der Rest hat im Prototyp keinen Handler. Das Feld ist eine echte Eingabe, wie M4 die Formularfelder interaktiv gemacht hat. |
| D14 | Android-Schaltflächenaufteilung wie CSS-Flex, als `Modifier.weight`-Paare mit Kommentar zur Rechnung: SanGefaelle und MaRaum **126/136**, FlBad „Als Mangel“ / „Keine Hohlstellen“ **152/166** (die `<a>` hat keinen Rahmen, der `<button>` fügt 1+1 Rahmen und 6+6 UA-Padding hinzu). Jeder Screen deklariert sein eigenes Paar. | In den Referenzen gemessen; gleiche Gewichte wären 5–7 dp daneben. |

### Abweichungen, die der Plan benennt

- **R6:** Das Mangel-Formular aus einem M6-Screen zeigt den Fenster-F2-Kontext (D5), weil die Fixture genau ein Formular enthält.
- **Ohne Aktion (D12 / Q&A):** FlBad-Fliesen sind Schaltflächen ohne Handler.

## Bekannte Lücken

Je Eintrag: Ursache und was sie klären würde. Messwerte stehen unten.

- **R1 / AC1 (bekannt, teilweise offen):** Das Glyphen-Raster (CoreText bzw. Layoutlib gegen Skia, M2 R11) hält Snapshots außerhalb der Toleranz, auch bei exaktem Layout. M5 endete bei 1 von 32 grün, M6 endet bei **3 von 44 grün** (alle Android, siehe unten). AC1 ist nicht erfüllt; das wird berichtet, nicht verborgen. Die Toleranzen sind unverändert (iOS 0,995 / 0,98, Android 0,5 %), keine Referenz wurde neu aufgenommen. Geklärt wäre es nur durch eine Änderung der Render-Umgebung oder der Toleranz, beides nicht vorgesehen.
- **R2 (behandelt, Restrisiko):** SwiftUI bricht einzelne letzte Wörter anders als Chrome. Behandelt mit `GreedyLabel` / `TextLines.greedy` (D13) und `ListRow` mit gierigem Untertitel, je Screen mit `diff-report` geprüft; ein Restrisiko bleibt, weil CoreText und Chrome Breiten minimal verschieden messen.
- **R3 (behandelt):** Chip-Breiten folgen der CSS-Border-Box (`SelectionChip(borderBox:)`, `ChipFrame` in `ObjectChip`), damit SanBad, MaRaum und Ebenheit wie die Referenz umbrechen. Geprüft auf den Galerieseiten und in den Snapshots; die Umbrüche sind kein gemessener Rest.
- **R4 / R5 / R7 / R8 / R9 / R18 / R19 / R20 (behandelt):** Ganzzahlarithmetik (D3), explizite `= null`-Vorgaben der Android-DTOs, Laufzeitprüfung der Prüfpunkt-Route in beiden Plattformen, Saver mit `decodeTriple`, Fliesenraster aus gewichteten `Row`s, geprüfte Ergänzungs-Tokens (`validateSupplement`, `gen-tokens --check`), Begrenzung von Soll und Grenzwert. Jeweils durch die in Plan §8 genannten Tests abgedeckt.
- **R6 (angenommen, D5):** Siehe oben. Geklärt wäre es durch ein Formular je Prüfung in der Fixture (M8).
- **R10 (offen, nicht einzeln vermessen):** Die gebündelte Roboto hat kein U+2713 („Nord ✓“, MaRaum Android). Der Text wurde nicht geändert. Ob Paparazzis Ersatzglyphe von Chromes abweicht, wurde nicht isoliert geprüft; die MaRaum-Abweichung (0,6285 % / 0,6087 %) liegt im Bereich der übrigen Android-Raster-Reste (0,50–0,97 %), ein Glyphenfehler wäre darin nicht zu trennen. Geklärt wäre es durch einen Ausschnittsvergleich der Wand-Chips.
- **R11 (Touch-Ziele, bekannte Lücke):** Die FlBad-Fliesen messen 33,75 × 44 pt (iOS) bzw. 35,25 × 44 dp (Android) bei Abstand 4 (festgehalten durch die Metriktests `testTileGridTilesAre33_75WideAndFollowTheirStyle` und die Android-Entsprechung). Sie können nicht wachsen, ohne die Nachbarn zu überlappen; sie haben keine Aktion (Q&A), also ist die Unterschreitung von 44 pt / 48 dp ohne Folge, bleibt aber unter der Konvention M3 R13 als Lücke stehen. Geklärt wäre es durch die Geräteprüfung in M7. Die übrigen Treffflächen (iOS Stepper 40, Android `VerdictToggle` 48 × 40 und `TextLink` 44) werden ohne Layoutänderung vergrößert (`contentShape` / `minimumInteractiveComponentSize`).
- **R14 (offen bis M5 gemergt ist):** M5 liegt noch nicht auf `main`. Wird M5 per Squash gemergt, trägt `feat/m6` die M5-Commits doppelt; vor dem PR hilft `git rebase --onto main a2f1798`. Wird M5 als Merge-Commit übernommen, zeigt der M6-PR nur M6-Commits.
- **R15 (angenommen):** Versteckte iOS-Tabs verlieren ihren `@State` (Rechner, Ende-Feld) beim Tab-Wechsel; Android behält Zustände verlassener Routen (M4 R20). Flüchtig und ungespeichert wie in M5.
- **R16 (angenommen):** `forkEvery = 1`: 22 neue Paparazzi-Klassen und eine Robolectric-Klasse (`LaterTradesUiTest`) verlängern den Lauf. Der Android-Snapshotlauf dieser Phase dauerte 13 Minuten.
- **R17 (offen, iOS, aus M5 R19):** Chrome rundet Boxkanten und Textgrundlinien auf ganze pt, SwiftUI malt auf dem 1/3-pt-Raster; bei den Zeilenhöhen 1,35 / 1,4 / 1,45 bleibt ein Lage-Versatz von 0,33–0,67 pt. Der Versatz ist bei sechs M6-Screens der größte Anteil am iOS-Rest (siehe „Ursache der Reste je Screen“). Klärung: allgemeines Malen auf ganze Punkte in den betroffenen Komponenten, gegen alle M2–M6-Goldens gemessen; das berührt bestehende Goldens und liegt außerhalb von M6.
- **G1 (neu, offen, Android):** `GalleryDarkSnapshotTest.allPages` ist im Endlauf rot: nur die Galerieseite `GalleryElectricalSteps2` weicht um **0,5145 %** ab (erlaubt ≤ 0,5 %); `GalleryLightSnapshotTest` ist grün. Beim Basislauf (Step 0) war die Galerie grün, ihr Golden ist seit M5 unverändert (`git diff a2f1798` nennt es nicht). Der Lauf wurde isoliert wiederholt (`--tests 'de.baupilot.snapshot.Gallery*'`), dasselbe Ergebnis. Die Ursache ist nicht isoliert belegt; in M6 geänderte gemeinsame Komponenten, die diese Seite zeichnet (`VerticalStepper` / `Step.trailing`, `Buttons`), sind die naheliegende Stelle. Das Golden wurde nicht neu aufgenommen. Der Befund gehört in die Integrationsprüfung (Phase 20) und verletzt I8 für diese eine Seite.
- **Ohne Aktion, sichtbar (Plan §1 / D12):** FlBad-Fliesen, „Keine Hohlstellen“, „Speichern“, Foto, „Tag 6 eintragen“, SanBad-Tabs und ✓/✕, MaRaum-Wandchips und „In Ordnung“, Zeilen mit `null`-Route. Die Platzhalter-Links bleiben sichtbar und tun nichts.

## Messwerte der 44 Snapshots (M6)

Lauf von Phase 19 auf `feat/m6` (HEAD `d7bdd51`, Worktree mit den Dateien dieser Phase unverändert). Befehle: iOS `xcodebuild test … -only-testing:BaupilotTests/ScreenSnapshotTests` (iPhone 15, `-derivedDataPath /tmp/m6-p19-dd`), Android `./gradlew --continue :app:testDebugUnitTest --tests 'de.baupilot.snapshot.*SnapshotTest'`, danach `scripts/diff-report --platform ios|android` auf den frisch geschriebenen Bildern. „iOS Übereinstimmung“ (gefordert ≥ 0,995, perceptualPrecision 0,98) und „Android Abweichung“ (erlaubt ≤ 0,5 %) sind die Testwerte; „Diff“ ist die Spalte aus `diff-report` (exakt abweichende Pixel, ohne Toleranz, nur untereinander vergleichbar). Paparazzi meldet bei grünen Tests keinen Wert. Alle Referenzen und Toleranzen sind unverändert.

| Nr. | Screen | Modus | iOS Übereinstimmung | iOS diff-report | Android Abweichung | Android diff-report |
|---|---|---|---|---|---|---|
| 24 | Sanitär & Heizung – Übersicht | hell | 0,9735 rot | 3,373 % | 0,5993 % rot | 2,6861 % |
| 24 | Sanitär & Heizung – Übersicht | dunkel | 0,9726 rot | 3,3976 % | 0,5650 % rot | 2,6896 % |
| 25 | Gefälle Abwasser | hell | 0,9740 rot | 3,2037 % | 0,9067 % rot | 2,7673 % |
| 25 | Gefälle Abwasser | dunkel | 0,9722 rot | 3,2191 % | 0,8768 % rot | 2,7478 % |
| 26 | Druckprobe Trinkwasser | hell | 0,9764 rot | 2,8238 % | 0,6917 % rot | 2,2948 % |
| 26 | Druckprobe Trinkwasser | dunkel | 0,9754 rot | 2,8419 % | 0,6657 % rot | 2,2958 % |
| 27 | Bad OG – Objekte | hell | 0,9654 rot | 4,2758 % | 0,7924 % rot | 2,9675 % |
| 27 | Bad OG – Objekte | dunkel | 0,9629 rot | 4,2834 % | 0,7672 % rot | 2,9707 % |
| 28 | Estrich – Übersicht | hell | 0,9766 rot | 2,984 % | 0,7281 % rot | 2,7898 % |
| 28 | Estrich – Übersicht | dunkel | 0,9757 rot | 3,003 % | 0,6820 % rot | 2,7876 % |
| 29 | Ebenheit messen | hell | 0,9847 rot | 2,0943 % | 0,6886 % rot | 2,1995 % |
| 29 | Ebenheit messen | dunkel | 0,9837 rot | 2,1035 % | 0,6735 % rot | 2,1953 % |
| 30 | Aufheizen & Belegreife | hell | 0,9709 rot | 3,4899 % | 0,9740 % rot | 3,092 % |
| 30 | Aufheizen & Belegreife | dunkel | 0,9697 rot | 3,5064 % | 0,9359 % rot | 3,0942 % |
| 31 | Fliesen – Übersicht | hell | 0,9774 rot | 2,8323 % | 0,5040 % rot | 2,2221 % |
| 31 | Fliesen – Übersicht | dunkel | 0,9764 rot | 2,8578 % | grün (≤ 0,5 %) | 2,2197 % |
| 32 | Raumprüfung Bad | hell | 0,9676 rot | 3,7936 % | 0,8972 % rot | 3,1019 % |
| 32 | Raumprüfung Bad | dunkel | 0,9664 rot | 3,8067 % | 0,8712 % rot | 3,1044 % |
| 33 | Maler – Übersicht | hell | 0,9855 rot | 1,994 % | grün (≤ 0,5 %) | 1,8078 % |
| 33 | Maler – Übersicht | dunkel | 0,9842 rot | 2,0165 % | grün (≤ 0,5 %) | 1,8178 % |
| 34 | Raumprüfung Wohnen | hell | 0,9761 rot | 6,8706 % | 0,6285 % rot | 3,8631 % |
| 34 | Raumprüfung Wohnen | dunkel | 0,9746 rot | 7,3568 % | 0,6087 % rot | 3,8702 % |

**Stand AC1 (M6):** iOS 0 von 22 grün (Bestwert `MaUebersicht` hell 0,9855), Android 3 von 22 grün (`FlUebersicht` dunkel, `MaUebersicht` hell und dunkel): **3 von 44 grün, 41 rot.** `FlUebersicht` hell liegt mit 0,5040 % nur 0,004 Punkte über der Grenze. AC1 ist nicht erfüllt. Die Toleranzen wurden nicht gelockert und keine Referenz neu aufgenommen. Alle 44 Snapshots rendern in der Referenzgröße (1170 × 2532 px auf beiden Plattformen; kein `sizeMismatch` in `reports/*/summary.json`).

**Ursache der Reste je Screen.** Verfahren wie in M5: das gerenderte Bild (`build/snapshots/`) gegen die Referenz in 60-px-Bändern (iOS, 43 Bänder) bzw. 14 Bändern (Android); je Band die beste senkrechte Verschiebung von −6 bis +6 px nach mittlerer absoluter RGB-Abweichung. „Lagenanteil“ ist der Anteil der Abweichung, der durch diese Verschiebung verschwindet, also Lage-Versatz und nicht Glyphen-Raster; „Bänder“ zählt die Bänder, in denen eine Verschiebung die Abweichung um mindestens 20 % senkt. Ein Rest, den keine Verschiebung senkt, ist Glyphen-Raster (R1).

| Screen | iOS Lagenanteil (hell / dunkel) | iOS Bänder (hell) | Android Lagenanteil (hell / dunkel) | Ursache |
|---|---|---|---|---|
| Sanitär & Heizung – Übersicht | 10 % / 10 % | 7 von 43 | 0 % / 0 % | überwiegend Raster; kleiner Versatz ab y = 960 px (320 pt) |
| Gefälle Abwasser | 41 % / 39 % | 17 von 43 | 5 % / 5 % | iOS: Lage-Versatz ab y = 540 px (180 pt) (R17). Android: Raster |
| Druckprobe Trinkwasser | 25 % / 25 % | 11 von 43 | 0 % / 0 % | iOS: Lage-Versatz ab y = 420 px (140 pt), Rest Raster. Android: Raster |
| Bad OG – Objekte | 47 % / 45 % | 19 von 43 | 0 % / 0 % | iOS: Lage-Versatz ab y = 240 px (80 pt) (R17). Android: Raster |
| Estrich – Übersicht | 5 % / 5 % | 2 von 43 | 0 % / 0 % | nur Glyphen-Raster (R1), kleiner Versatz ab 2160 px |
| Ebenheit messen | 0 % / 0 % | 0 von 43 | 0 % / 0 % | nur Glyphen-Raster (R1) |
| Aufheizen & Belegreife | 29 % / 30 % | 9 von 43 | 2 % / 2 % | iOS: Lage-Versatz ab y = 660 px (220 pt), Rest Raster. Android: Raster, das höchste Android-Ergebnis (0,974 %) |
| Fliesen – Übersicht | 13 % / 13 % | 6 von 43 | 0 % / 0 % | überwiegend Raster; kleiner Versatz ab 960 px |
| Raumprüfung Bad | 48 % / 47 % | 28 von 43 | 0 % / 0 % | iOS: Lage-Versatz ab y = 420 px (140 pt), über zwei Drittel der Bänder (R17). Android: Raster |
| Maler – Übersicht | 0 % / 0 % | 0 von 43 | 0 % / 0 % | nur Glyphen-Raster (R1); Android grün |
| Raumprüfung Wohnen | 32 % / 32 % | 10 von 43 | 1 % / 0 % | iOS: Lage-Versatz ab y = 540 px (180 pt), Rest Raster; der Diff-Wert (6,87 % / 7,36 %) liegt weit über der Testabweichung (2,4 % / 2,5 %), weil viele Pixel nur leicht abweichen und von `perceptualPrecision` toleriert werden (vermutlich Verlaufsflächen von Lichtskizze und Lampenschein, nicht isoliert belegt). Android: Raster |

Android hat bei allen 22 Screens höchstens 5 % Lagenanteil (`SanGefaelle` im obersten Band): Boxen und Zeilen stehen auf den Referenzpixeln, der Rest ist Glyphen-Raster (Layoutlib gegen Skia, R1). Die Android-Abweichung steigt mit der Textmenge, am höchsten bei `EsBelegreife` (0,974 %), `SanGefaelle` (0,907 %) und `FlBad` (0,897 %). Bei iOS stammt bei sechs Screens (`SanGefaelle`, `SanDruck`, `SanBad`, `EsBelegreife`, `FlBad`, `MaRaum`) ein Viertel bis knapp die Hälfte der Abweichung aus dem Lage-Versatz (R17, Verfahren und Beleg in `docs/m5-offene-punkte.md`, „iOS-Ursache des Lage-Versatzes“); Boxen und Zeilen darunter stimmen auf das Pixel, wenn man sie verschiebt. Ein Layoutfehler einer Komponente wurde dabei nicht gefunden.

**I8 (M2–M5 nicht schlechter als Step 0):** Die 34 Zeilen der Tabellen in `docs/m4-offene-punkte.md` und `docs/m5-offene-punkte.md` (iOS- und Android-`diff-report`) wurden maschinell gegen den Endlauf verglichen: jeder Wert steht unverändert im Endlauf (keine Abweichung). Die Testwerte der M5-Screens (iOS Übereinstimmung, Android Abweichung) entsprechen der M5-Tabelle, die der M2–M4-Screens der M4-Tabelle. Die 8 Android-Snapshots, die dort grün waren, und `ElUebersicht` dunkel sind weiter grün; kein Android-Screen ist neu rot. **Ausnahme:** G1 (Galerieseite `GalleryElectricalSteps2`, dunkel). iOS-Galerie-Goldens und die übrigen Suiten wurden in dieser Phase nicht gelaufen (Phase 20).

Bilder unter `build/snapshots/{ios,android}/` (je 68 Dateien: 18 M2/M3 + 12 M4 + 16 M5 + 22 M6).

## Testzahlen

Gemessen in dieser Phase (nur die in „Messwerte“ genannten Läufe und `cd scripts && node --test test/`). Der Gesamtlauf aller Suiten und der Release-Build gehören zu Phase 20; seine Zahlen stehen nicht hier, damit keine Zahl aus dem Plan übernommen wird.

| Block | Ergebnis |
|---|---|
| `cd scripts && node --test test/` | 114 Tests, 114 grün |
| iOS `BaupilotTests/ScreenSnapshotTests` | 68 Tests, 68 rot (46 M2–M5 + 22 M6), 0 unerwartete Fehler; `xcodebuild` Exit 65 |
| Android `de.baupilot.snapshot.*SnapshotTest` | 71 Testfälle, 57 rot (56 Screen-Snapshots: 37 M2–M5 + 19 M6; dazu `GalleryDarkSnapshotTest`, G1), 14 grün (12 Screen-Snapshots: 9 M2–M5 + 3 M6; dazu `GalleryLightSnapshotTest` und der Referenz-Kanarienvogel) |
| Android `de.baupilot.snapshot.Gallery*` (isoliert wiederholt) | `GalleryDarkSnapshotTest.allPages` rot (G1), `GalleryLightSnapshotTest` grün |

Testmethoden der M6-Suiten, **statisch gezählt, in dieser Phase nicht ausgeführt**: iOS `SlopeViewModelTests` 9, `FlatnessViewModelTests` 9, `M6ConsistencyTests` 9, `LaterTradesUITests` 6; Android `SlopeViewModelTest` 10, `FlatnessViewModelTest` 10, `M6ConsistencyTest` 9, `LaterTradesUiTest` 6 (keine `@Ignore`). Zählweise: `func test` bzw. `@Test`.

### Invarianten (Plan §11)

| # | Ergebnis |
|---|---|
| I1, I2 | Die Tests stehen im Baum (`testSaveDefectFromSlopePops`, `testSaveDefectFromTileRoomPops`, `testDefectFromM6ScreensReturnsToOpener` iOS `LaterTradesUITests`; Android `LaterTradesUiTest`). In dieser Phase nicht ausgeführt (Phase 20) |
| I3, I4, I5 | Fixture-Dekodierung und Routenauflösung stehen in `AppDataTests` / `AppDataTest` und `M6ConsistencyTests` / `M6ConsistencyTest`. In dieser Phase nicht ausgeführt (Phase 20) |
| I6 | Kein Schreibzugriff auf `BackendRepository`; Prüfung der Protokolldateien in Phase 20 |
| I7 | Grenztests der ViewModels (`SlopeViewModelTests`, `FlatnessViewModelTests` und Android-Entsprechungen). In dieser Phase nicht ausgeführt (Phase 20) |
| I8 | Gemessen: siehe oben. Alle M2–M5-Diff-Werte unverändert; eine neue Abweichung (G1) |
| I9 | `cd scripts && node --test test/`: 114 / 114 grün (T-DS-1 eingeschlossen) |
| I10 | `scripts/design-freeze --check`, `gen-tokens --check`, `gen-icons --check` und `git status --porcelain -- design fixtures scripts/icons.json` gehören zum Endlauf (Phase 20); diese Phase hat nichts davon geändert |
| I11 | Die Tests stehen im Baum (`LaterTradesUITests` / `LaterTradesUiTest`, Fliesen-Tap ohne Aktion). In dieser Phase nicht ausgeführt (Phase 20) |
