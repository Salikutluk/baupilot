# M5 · Offene Punkte

## Baseline (Step 0)

Gemessen auf `8a72c08` (Branch `feat/m5`, per `git merge --ff-only feat/m4` von `1cd6058` vorgezogen, Worktree sauber). Befehle aus Plan §12, iOS mit `-derivedDataPath /tmp/m5-p1-dd`, Android mit JDK 21 aus Android Studio. Referenz für den Vergleich in den späteren M5-Steps. Die Werte sind die beobachteten, nicht die erwarteten.

### Zählung

| Block | Ergebnis |
|---|---|
| `cd scripts && node --test test/` | 114 Tests, 113 grün, **1 rot** (`repo tree is clean`, Ursache siehe „Abweichungen“) |
| `scripts/gen-tokens --check`, `scripts/gen-icons --check` | grün (Exit 0) |
| `scripts/lint-hex` | **1 Fund** (Exit 1), siehe „Abweichungen“ |
| `scripts/design-freeze --check` | `design/ matches scripts/design-v1.sha256 (276 files)` |
| iOS `BaupilotTests` (Unit, Gallery, Metrik, Snapshots) | 280 Tests, 250 grün, **30 rot** (alle 30 `ScreenSnapshotTests`) |
| iOS `BaupilotUITests` | 27 / 27 grün |
| iOS gesamt | 307 Tests, 277 grün, 30 rot (`xcodebuild` Exit 65) |
| Android `:app:testDebugUnitTest` | 291 Tests, 269 grün, **22 rot** (alle `*SnapshotTest`) |
| Android `:app:compileReleaseKotlin` | grün (`--continue`; Gesamtlauf Exit 1 wegen der roten Snapshots) |
| `git status --porcelain` nach allen Läufen | leer |

### Vergleich mit M4 („Verifikation (Step 14)“ in `docs/m4-offene-punkte.md`)

| Größe | Erwartet (M4-Endstand) | Beobachtet |
|---|---|---|
| scripts | 114 / 114 grün | 113 / 114 grün (1 rot, s. u.) |
| iOS Snapshot-Rote | 30 (18 M2/M3 + 12 M4) | 30, alle in `ScreenSnapshotTests` |
| iOS Nicht-Snapshot-Rote | 0 | 0 |
| iOS `BaupilotUITests` | 27 grün | 27 grün |
| Android Snapshot-Rote | 22 | 22, alle `*SnapshotTest` |
| Android Nicht-Snapshot-Rote | 0 | 0 |
| `lint-hex` | `lint-hex: sauber` | 1 Fund |

### Abweichungen

1. **`lint-hex` meldet 1 Fund, und `scripts` hat dadurch 1 roten Test.** Fund: `ios/BaupilotTests/DesignSystem/ComponentMetricsInspectionTests.swift:83:38 named-color Color.gray` (`.scene(AnyView(Color.gray), …)` in `testCheckedSceneTilePlacesTheBadgeAt6FromTheTopRight`). Der rote Test ist `repo tree is clean` in `scripts/test/lint-hex.test.mjs`; er schlägt aus genau diesem einen Fund fehl (`actual: [ { …, rule: 'named-color', match: 'Color.gray' } ]`, `expected: []`). Ursache: Die Zeile kam im letzten M4-Commit `8a72c08` in den Baum (`git log -S'Color.gray'` nennt nur diesen Commit), nach den in `docs/m4-offene-punkte.md` festgehaltenen Läufen (dort „114 / 114“ und „`lint-hex: sauber`“). Beide Rote sind dieselbe Ursache, kein zweiter Fehler. Nicht behoben: Der Baseline-Task darf keinen Code ändern; die Korrektur (Farbe über ein Token oder die Test-Ausnahme von `lint-hex`) gehört in einen späteren M5-Step.
2. **Gesamtzahlen höher als im M4-Bericht.** iOS `BaupilotTests` 280 statt 277 Tests, Android 291 statt 286 Tests. Die Menge der roten Tests entspricht dem Bericht (30 bzw. 22, ausschließlich Snapshots), Nicht-Snapshot-Tests sind alle grün. Die Mehrzahl an Tests ist nicht auf einen Commit zurückgeführt; sie wird nicht als Fehler gewertet, weil kein Test mehr rot ist als erwartet.

### Rote Snapshot-Tests

iOS (30, Schwelle: Übereinstimmung ≥ 0.995): alle in `BaupilotTests.ScreenSnapshotTests` (18 M2/M3, 12 M4). Android (22, Schwelle: maxPercentDifference 0.5): alle `*SnapshotTest` (10 M2/M3, 12 M4). Ursachen unverändert wie in `docs/m4-offene-punkte.md` (Glyphen-Raster R1, Roboto-Glyphe R2).

## Entscheidungen

Aus dem Plan (§3), in Kurzform. D1, D2, D6, D9, D10, D11, D15 und D16 (Routen, Ziele nach Fixture-`screen`, keine Fixture-Änderung, Verknüpfungen, Snapshot-Zustände, iOS-Zurück-Beschriftungen, Branch-Zuschnitt, Plattform-Nullwerte) stehen nur im Plan.

| # | Entscheidung | Begründung |
|---|---|---|
| D0 | Step 0 bringt M4 per `git merge --ff-only feat/m4` in `feat/m5`. Ist der Fast-Forward nicht möglich, weil `feat/m5` weitergewandert ist, wird gestoppt und berichtet; kein Rebase, kein Merge mit Konflikten. Wurde M4 per Squash nach `main` übernommen, wird `feat/m5` vor dem PR mit `git rebase --onto main 8a72c08` umgesetzt (R14). | M5.md: „Voraussetzung: M4 abgeschlossen“. M5 braucht die Routen, die Shell, das Mangel-Formular (Nr. 13), die Mängel-Ansicht (Nr. 15) und die Design-System-Komponenten aus M4. |
| D3 | „Maße & Lot“ ist ein reiner Werttyp `MeasureViewModel` (iOS Struct mit `mutating`-Operationen, Android unveränderliche Data-Class, Operationen geben eine Kopie zurück). Startwerte aus `measurements.shellOpening`: Soll, Ist, `stepperDelta` als Schritt, Toleranz = `parameter.default`, begrenzt auf `parameter.min…max`. Regeln wie im Prototyp: `diff = ist − soll`, innerhalb ⇔ `abs(diff) ≤ Toleranz` (inklusive), Differenztext mit `+` / `−` (U+2212) / `±` und `mm`, Werte als einfache Ganzzahl ohne Tausendertrennung. Stepper ±Schritt je Tipp, keine Obergrenze. **Abweichung:** Eine Verringerung unter 0 ist ein No-op (R8). Der Screen hält ihn in iOS `@State` bzw. Android `rememberSaveable` mit `Saver`. | M5.md: „Logik als ViewModel nachbauen und per Unit-Test absichern“. Das Repository hat kein ViewModel-Framework; ein Werttyp ist ohne Umgebung testbar und folgt `SignatureStrokes`. Die Toleranz kommt aus der Fixture, weil der Prototyp keine Bedienung dafür hat (keine erfundene UI). |
| D4 | „Sichern“ im Mangel-Formular kehrt zum Aufrufer zurück: neue reine Operation `saveDefect(formTradeId:hasSignOff:)` (iOS `AppNavigation`, Android `AppNavigationState`). Steht unter `.newDefect` (Pfad des gewählten Tabs) `.inspection(formTradeId)` oder `.inspectionStep(formTradeId, …)`, bleibt das M4-Verhalten (D3 dort: Unterschrift über dem Elternteil, sonst `open(.inspection)`); sonst `pop()`. Abgemeldet oder `.newDefect` nicht oben: No-op. | Ohne diese Regel würde „Sichern“ aus Rohbau die Fenster-Unterschrift auf den Rohbau-Stapel legen. Zurück zum Aufrufer ist die risikoärmste Wirkung (nichts wird gespeichert, M4 I8); der M4-Ablauf (AC2) bleibt unverändert. |
| D5 | RohbauMasse „Mangel“, ElRaum „Mangel“, ElVerteiler „Löst nicht aus“ und ElRauchmelder „Kein Piepton“ öffnen das eine Fixture-Formular (`open(.newDefect)`, `checks.defectF2`, Kontext „F2 · Wohnen · Schritt 1“). Das Label „Mangel“ kommt aus `inspections.overview.statusLabels.defect`. | Die Verknüpfungen stammen aus dem Prototyp (M5.md „Links: Mangel erfassen (Nr. 13)“). Ein Formular je Prüfung bräuchte erfundenen Fixture-Inhalt. Inhaltliche Abweichung als R6 festgehalten. |
| D7 | Statusabbildungen stehen als Tabellen mit Quellzeile in `ScreenCopy`; ein unbekannter Wert ist ein Ladefehler am benannten Pfad: Protokollzeile (`defect` ⇔ `meta == statusLabels.defect`, sonst `ok`), Protokoll-Banner nach Übersichtsstatus (`accepted` → ok, `reserved` → Warnung mit Ziel `.defects`, sonst `decoding`), Elektro-Zeilenstatus (Offen / Läuft / In Ordnung, Vorhanden / Fehlt), Zeilensymbol nach Gruppenindex, Phasenart (genau eines von `statusLabel` / `progressLabel`), Steckdosenstatus, FI-Label, Rauchmelder-Raumlabel, „erledigt“-Markierungen nach Position. | Die Fixture führt für diese Listen Beschriftungen statt Statusschlüssel. Eine ausdrückliche Tabelle mit Ladefehler ist sicherer als ein stiller Standard (Geist von M4 D16). |
| D8 | Fest verdrahtete Fortschrittsanteile in `ScreenCopy`: ElUebersicht aktive Phase **0,36**; Android ElRaum **0,30**; Android ElVerteiler **0,50**. | Der Prototyp verdrahtet sie. 4/11 = 0,3636 würde die Balkenkante um etwa 0,5 pt/dp verschieben. |
| D12 | Lokaler, ungespeicherter Zustand (wie M4 D9): das Maß-ViewModel (D3) und der Erinnerungsschalter in ElRauchmelder (startet an, wie `aria-checked=true` im Prototyp; speichert nichts). Steckdosenkacheln, „In Ordnung“-Schaltflächen, „Speichern“ und „PDF“ haben keine Aktion. | Der Schalter ist ein echtes Bedienelement; der Rest sind Schaltflächen ohne Ziel im Prototyp. |
| D13 | iOS-Zeilenumbruch: Jeder Text, der in den iOS-Referenzen umbricht, läuft über `GreedyLabel` (Chrome-artiger gieriger Umbruch) statt über SwiftUI `Text`. Dazu ein neuer Fall `.greedy` in `TextLines`; die bestehenden Fälle `.single/.multi/.block` bleiben unverändert (I8). | M4 hat gemessen, dass SwiftUI ein einzelnes letztes Wort anders umbricht als Chrome; allein die Protokoll-Screens haben acht solche Zeilen in den Referenzen. |
| D14 | Android-Schaltflächenaufteilung wie CSS-Flex: RohbauMasse Mangel/Speichern **126/136**, ElRaum **150/160**, ElVerteiler und ElRauchmelder **154/164**, als `Modifier.weight` mit Kommentar zur Rechnung (Content-Box-Rahmen auf `<a>` plus UA-Padding 6+6). Jeder Screen deklariert sein eigenes Paar; keine gemeinsame Konstante. | Beide Schaltflächen sind im Prototyp `flex: 1`, ihre Außenmaße unterscheiden sich aber um Rahmen und Padding; gleiche Gewichte wären 5 dp daneben. |

### Abweichungen, die der Plan benennt

- **R8:** Eine Verringerung des Maßes unter 0 ist ein No-op (D3); der Prototyp hat keine Grenze. Geprüft durch `decrementStopsAtZero`.
- **R6:** Das Mangel-Formular aus einem M5-Screen zeigt den Fenster-F2-Kontext (D5).

## Bekannte Lücken

Je Eintrag: Ursache und was sie klären würde. Messwerte stehen unten (Endlauf Phase 18).

- **R1 / AC1 (bekannt, ggf. teilweise offen):** Das Glyphen-Raster (CoreText bzw. Layoutlib gegen Skia, M2 R11 / M4 R1) hält Snapshots außerhalb der Toleranz, auch bei exaktem Layout. M4 endete bei 0 von 24 grün. Layoutursachen, die `diff-report` findet, werden behoben; der Rest wird je Screen gemessen und dokumentiert. Die Toleranzen sind unverändert (iOS 0,995 / 0,98, Android 0,5 %). AC1 kann teilweise unerfüllt bleiben; das wird berichtet, nicht verborgen. Geklärt wäre es nur durch eine Änderung der Render-Umgebung oder der Toleranz, beides nicht vorgesehen.
- **R2 (behandelt, Restrisiko):** SwiftUI bricht ein einzelnes letztes Wort anders als Chrome. Behandelt mit `GreedyLabel` und `TextLines.greedy` (D13), je Screen mit `diff-report` geprüft; ein Restrisiko bleibt, weil CoreText und Chrome Breiten minimal unterschiedlich messen.
- **R3 (geschlossen, gemessen):** Die iOS-ElRaum-Zeile „Jeder Schalter schaltet das richtige Licht“ bricht wie die Referenz um; die Zeilenköpfe stimmen auf das Pixel überein (Messung unter „Ursache der Reste je Screen“). Der Text wurde nie gekürzt, keine Breite geändert.
- **R6 (angenommen, D5):** Das Mangel-Formular aus einem M5-Screen zeigt den Fenster-F2-Kontext „F2 · Wohnen · Schritt 1“, weil die Fixture genau ein Formular enthält. Geklärt wäre es durch ein Formular je Prüfung in der Fixture (M8).
- **R8 (dokumentierte Abweichung von D3):** Das Verringern unter 0 ist ein No-op; der Prototyp hat keine Grenze, eine negative Öffnung ist aber bedeutungslos. Geklärt wäre es durch eine Vorgabe des Designs.
- **R19 (offen, iOS):** Chrome rundet Boxkanten und Textgrundlinien auf ganze pt, SwiftUI malt auf dem 1/3-pt-Raster; bei den Zeilenhöhen 1,4 und 1,45 bleibt dadurch ein Lage-Versatz von 0,33–0,67 pt (iOS-Lagenanteil am Rest 31–46 % bei `ElRaum`, `ElVerteiler`, `ElRauchmelder`, `RohbauMasse`). Klärung: allgemeines Malen auf ganze Punkte in den betroffenen Komponenten, gegen alle M2–M5-Goldens gemessen (I8).
- **R11 (Android-PDF-Trefferfläche, offen):** Die Touch-Ziele (iOS Stepper 40, PDF 36; Android PDF 38) werden ohne Layoutänderung vergrößert (`contentShape` / `minimumInteractiveComponentSize`). Verschiebt der Modifier auf Android das Layout der PDF-Schaltfläche, bleibt sie eine Lücke (Konvention M3 R13). Geklärt wäre es durch die Geräteprüfung in M7.
- **R14 (offen bis M4 gemergt ist):** M4 liegt noch nicht auf `main`. Wird M4 per Squash gemergt, trägt `feat/m5` die M4-Commits doppelt; vor dem PR hilft `git rebase --onto main 8a72c08`. Wird M4 als Merge-Commit übernommen, zeigt der M5-PR nur M5-Commits.
- **R15 (angenommen):** Versteckte iOS-Tabs verlieren ihren `@State` (Stepper, Erinnerungsschalter) beim Tab-Wechsel; Android behält Zustände verlassener Routen (M4 R20), sodass Maße & Lot in gleicher Tiefe die früheren Werte wieder zeigt. Flüchtig und ungespeichert wie in M4; würde mit der Persistenz in M8 entfallen.
- **Ohne Aktion, sichtbar (Plan §1):** „Speichern“, Kamera/Foto, „PDF“, „Alle 9“, Steckdosenkacheln, „S6 in Ordnung“, „FI 2 ausgelöst“, „Piept – OK“ und Zeilen mit `null`-Route. Die M6-Gewerke öffnen weiter den gepushten Platzhalter.

## Messwerte der 32 Snapshots (M5)

Lauf am Ende von Phase 18 auf `feat/m5` (Befehle aus Plan §12; iOS `xcodebuild test` iPhone 15, Android `./gradlew --continue :app:testDebugUnitTest :app:compileReleaseKotlin`, danach `scripts/diff-report --platform ios|android`). „iOS Übereinstimmung“ (gefordert ≥ 0,995) und „Android Abweichung“ (erlaubt ≤ 0,5 %) sind die Testwerte; „Diff“ ist die Spalte aus `diff-report` (exakt abweichende Pixel, ohne Toleranz, nur untereinander vergleichbar). Alle Referenzen und Toleranzen sind unverändert.

| Screen | Modus | iOS Übereinstimmung | iOS diff-report | Android Abweichung | Android diff-report |
|---|---|---|---|---|---|
| Elektro – Übersicht | hell | 0,9800 rot | 2,7643 % | 0,5227 % rot | 2,3812 % |
| Elektro – Übersicht | dunkel | 0,9789 rot | 2,7953 % | grün (≤ 0,5 %) | 2,3890 % |
| Elektro – Raum | hell | 0,9735 rot | 3,3469 % | 0,8571 % rot | 3,1035 % |
| Elektro – Raum | dunkel | 0,9717 rot | 3,3709 % | 0,8268 % rot | 3,1119 % |
| Elektro – Verteiler | hell | 0,9684 rot | 3,8661 % | 0,7553 % rot | 2,9348 % |
| Elektro – Verteiler | dunkel | 0,9659 rot | 3,8859 % | 0,7122 % rot | 2,9361 % |
| Elektro – Rauchmelder | hell | 0,9756 rot | 2,9782 % | 0,8433 % rot | 2,5459 % |
| Elektro – Rauchmelder | dunkel | 0,9747 rot | 2,9777 % | 0,7490 % rot | 2,5431 % |
| Rohbau – Maße & Lot | hell | 0,9757 rot | 3,0465 % | 0,5214 % rot | 2,1491 % |
| Rohbau – Maße & Lot | dunkel | 0,9733 rot | 3,0609 % | 0,5046 % rot | 2,1463 % |
| Rohbau | hell | 0,9758 rot | 2,9846 % | 0,7841 % rot | 2,8574 % |
| Rohbau | dunkel | 0,9751 rot | 3,0063 % | 0,7380 % rot | 2,8539 % |
| Erdarbeiten | hell | 0,9752 rot | 3,0372 % | 0,7058 % rot | 2,8154 % |
| Erdarbeiten | dunkel | 0,9744 rot | 3,0576 % | 0,6542 % rot | 2,8156 % |
| Zimmerer | hell | 0,9784 rot | 2,7035 % | 0,6965 % rot | 2,6331 % |
| Zimmerer | dunkel | 0,9774 rot | 2,7206 % | 0,6426 % rot | 2,6343 % |

**Stand AC1 (M5):** iOS 0 von 16 grün, Android 1 von 16 grün (nur `ElUebersicht` dunkel, Test grün; Paparazzi meldet bei grünen Tests keinen Wert): **1 von 32 grün, 31 rot.** AC1 ist nicht erfüllt. Die Toleranzen wurden nicht gelockert und keine Referenz neu aufgenommen.

**Ursache der Reste je Screen (gemessen im Review-Fix, Zyklus 2):** Verfahren: das gerenderte Bild (`build/snapshots/`) gegen die Referenz in 60-px-Bändern (iOS, 3×) bzw. 14 Bändern (Android); je Band die beste senkrechte Verschiebung von −6 bis +6 px. „Lagenanteil“ ist der Anteil der mittleren Pixelabweichung, der durch diese Verschiebung verschwindet, also Lage-Versatz und nicht Glyphen-Raster; „Bänder“ zählt die Bänder, in denen eine Verschiebung die Abweichung um mindestens 20 % senkt. Ein Rest, den keine Verschiebung senkt, ist Glyphen-Raster (R1).

| Screen | iOS Lagenanteil (hell / dunkel) | iOS Bänder (hell) | Android Lagenanteil (hell / dunkel) | Ursache |
|---|---|---|---|---|
| Elektro – Übersicht | 0 % / 0 % | 0 von 42 | 0 % / 0 % | nur Glyphen-Raster (R1) |
| Elektro – Raum | 31 % / 31 % | 11 von 42 | 2 % / 2 % | iOS: Lage-Versatz ab y = 420 px (140 pt), Rest Raster. Android: Raster |
| Elektro – Verteiler | 42 % / 41 % | 19 von 42 | 1 % / 1 % | iOS: Lage-Versatz ab y = 540 px (180 pt), siehe unten. Android: Raster |
| Elektro – Rauchmelder | 46 % / 45 % | 16 von 42 | 1 % / 1 % | iOS: Lage-Versatz ab y = 540 px (180 pt). Android: Raster |
| Rohbau – Maße & Lot | 37 % / 36 % | 23 von 42 | 0 % / 0 % | iOS: Lage-Versatz ab y = 240 px (80 pt). Android: Raster |
| Rohbau | 13 % / 13 % | 4 von 42 | 0 % / 0 % | überwiegend Raster; kleiner Versatz ab 1320 px |
| Erdarbeiten | 15 % / 16 % | 6 von 42 | 0 % / 0 % | überwiegend Raster; kleiner Versatz ab 960 px |
| Zimmerer | 10 % / 10 % | 3 von 42 | 0 % / 0 % | überwiegend Raster |

Android hat bei allen 16 Screens höchstens 2 % Lagenanteil: Boxen und Zeilen stehen auf den Referenzpixeln, der gesamte Rest ist Glyphen-Raster (Layoutlib gegen Skia, R1). Auch Android `ElRaum` (0,857 % / 0,827 %) und `ElRauchmelder` (0,843 %) liegen im Raster, nicht im Layout.

**iOS-Ursache des Lage-Versatzes (belegt an `ElVerteiler`):** Chrome rundet Boxkanten und Textgrundlinien beim Zeichnen auf ganze CSS-Pixel (pt), während der Fluss mit Bruchteilen weiterrechnet. SwiftUI zeichnet auf dem 1/3-pt-Raster der Bildschirmpixel. Die Zeilenhöhen 1,4 (14 px → 19,6) und 1,45 (15 px → 21,75) ergeben Bruchteile:
- Die Warnbox (2 × 19,6 + 24 = 63,2 pt) steht bei y = 152 pt; die Referenz malt sie 63 pt hoch (Kante bei 645 px), iOS 63,2 pt (646 px). Alle Boxkanten darunter (Erledigt-Zeilen 681 → 682 px, Karte, Verteilerliste) liegen dadurch um 1 px tiefer; die Referenz rundet jede Kante einzeln, der Fluss ist in beiden gleich.
- Die Textzeilen der Anleitung („1. Prüftaste …“, 21,75 + 4 pt Abstand) liegen in der Referenz alle auf ganzen pt (Zeilenkopf bei 1181, 1259, 1400, 1466 px, jeweils ≡ 2 mod 3); iOS setzt sie auf 1/3 pt (1182, 1260, 1402, 1467 px): 1–2 px (0,33–0,67 pt) Versatz, wie die Verschiebungsanalyse ab ca. 330 pt zeigt (y = 990 px: Abweichung 4,67 → 0,20 bei +1 px).
- Die Verteilerliste und die Schaltflächen darunter (1572–1697 px, 1701–1826 px) stimmen exakt überein; der Versatz ist also kein Höhenfehler einer Komponente, sondern die unterschiedliche Rundung.

Versuche (beide zurückgenommen, damit M2–M4 unverändert bleiben): (a) Warnbox-Layouthöhe auf ganze pt runden: Übereinstimmung 0,9684 → 0,9677, schlechter, weil der Text darunter 1 px zu hoch steht (Chrome rundet nur das Malen, nicht den Fluss). (b) nur den Hintergrund der Warnbox auf 63 pt malen, Fluss unverändert: 0,9684 → 0,9687, die Kante stimmt, die Textzeilen bleiben versetzt. Eine vollständige Lösung ist ein allgemeines „auf ganze Punkte malen“ für Boxen und Grundlinien in den Komponenten (`InfoCallout`, `StepRow`, `CurrentStepCard`, `NumberedList`, `StatusList`); das berührt die Goldens von M2–M4 (I8) und liegt außerhalb von M5. Sie steht unter „Bekannte Lücken“ (R19). Die Toleranzen und Referenzen bleiben unverändert.

**R3 geschlossen (gemessen):** iOS `ElRaum` bricht „Jeder Schalter schaltet das richtige Licht“ wie die Referenz um („… das richtige“ / „Licht“): die Textzeilen dieses Abschnitts (Zeilenköpfe 2009, 2066, 2147, 2205, 2285, 2343 px) stimmen mit der Referenz auf das Pixel überein. Der Umbruch ist also kein Rest von `ElRaum`; dessen Abweichung (0,9735 / 0,9717) stammt aus dem Lage-Versatz der Hinweis- und Prüfzeilen und aus dem Raster.

**I8 (M2–M4 nicht schlechter als Step 0):** Alle 30 iOS- und 22 Android-Werte der M2–M4-Snapshots stimmen im Endlauf exakt mit den Werten aus `docs/m4-offene-punkte.md` (Step 0 / Endstand M4) überein, bei iOS (Übereinstimmung, `diff-report`) und bei Android (Abweichung, `diff-report`); kein Wert ist schlechter. Die 8 Android-Snapshots, die dort grün waren (DokViewer hell/dunkel, Gewerk hell/dunkel, Login hell/dunkel, Home dunkel, Zeitplan dunkel), sind weiter grün.

Bilder unter `build/snapshots/{ios,android}/` (je 46 Dateien: 18 M2/M3 + 12 M4 + 16 M5).

## Testzahlen

Beobachtet im Endlauf (Phase 18), nicht aus dem Plan übernommen.

| Block | Ergebnis |
|---|---|
| `cd scripts && node --test test/` | 114 Tests, 114 grün (vorher 1 rot, siehe unten) |
| `gen-tokens --check`, `gen-icons --check`, `lint-hex`, `design-freeze --check` | alle grün (`lint-hex: sauber`; `design/ matches scripts/design-v1.sha256 (276 files)`) |
| iOS `BaupilotTests` (Unit, Gallery, Metrik, Snapshots) | 368 Tests, 322 grün, **46 rot** (alle 46 `ScreenSnapshotTests`: 30 M2–M4 + 16 M5) |
| iOS `BaupilotUITests` | 32 / 32 grün (`AppNavigationUITests` 7, `EarlyTradesUITests` 5, `InspectionFlowUITests` 9, `TradeFlowUITests` 11) |
| iOS gesamt | 400 Tests, 354 grün, 46 rot (`xcodebuild` Exit 65) |
| iOS Release-Build (`generic/platform=iOS Simulator`) | `BUILD SUCCEEDED` |
| Android `:app:testDebugUnitTest` | 372 Tests, 335 grün, **37 rot** (alle 37 `*SnapshotTest`: 22 M2–M4 + 15 M5) |
| Android `:app:compileReleaseKotlin` | grün (Gradle-Gesamtlauf Exit 1 nur wegen der roten Snapshots) |
| Android Nicht-Snapshot-Anteil | 0 rot. Darunter `AppDataTest` 78, `AppNavigationStateTest` 37, `MeasureViewModelTest` 9, `M5ConsistencyTest` 4, UI (Robolectric): `AppNavigationUiTest` 9, `TradeFlowUiTest` 13, `InspectionFlowUiTest` 9, `EarlyTradesUiTest` 5, `BaupilotRootLoadTest` 3 |
| `git status --porcelain -- design fixtures scripts` | leer |

iOS `MeasureViewModelTests` 8, `M5ConsistencyTests` 2, `AppNavigationTests` und `AppDataTests` grün. Zu den Zahlen gegenüber Step 0: iOS `BaupilotTests` 280 → 368 (+88), Android 291 → 372 (+81), iOS UI 27 → 32, scripts 113/114 → 114/114.

**Korrektur in dieser Phase:** `lint-hex` meldete seit Step 0 einen Fund (`ComponentMetricsInspectionTests.swift:83`, `Color.gray`), und `scripts/test/lint-hex.test.mjs` (`repo tree is clean`) war deshalb rot. Behoben mit `Color.clear` (derselbe Platzhalter wie in den Nachbartests; das Abzeichen-Maß im Test hängt nur am Differenzbild zwischen „abgehakt“ und „nicht abgehakt“, `testCheckedSceneTilePlacesTheBadgeAt6FromTheTopRight` bleibt grün). Kein Skript, keine Fixture, kein Design geändert.

### Invarianten (Plan §11)

| # | Ergebnis |
|---|---|
| I1, I2 | `testSaveDefect*` (iOS `AppNavigationTests`, Android `AppNavigationStateTest` `saveDefect*`) und der M4-AC2-Ablauf (iOS `testWindowAcceptanceFlowEndsOnOpenDefects`, Android `InspectionFlowUiTest`) grün |
| I3 | `testLoadsAllM5Routes` (iOS) / `loadsAllM5Routes` (Android) grün |
| I4, I5 | Fixture-Dekodierung: Android `m5DecodingErrorIsRepositoryException`, `missingPlatformValueIsDecodingError`; iOS `testMissingPlatformValueThrows`, `testMissingIosMoreFailsLoad`, `testMalformedDataThrowsDecodingErrorNamingMissingKey` grün. Die D16-Prüfung (nur die eigene Plattform wird gelesen) steht als Kommentar in `AppDataTest.kt:1097` |
| I6 | Kein Schreibzugriff auf `BackendRepository`: beide Protokolle (`ios/Baupilot/Data/BackendRepository.swift`, `android/.../data/BackendRepository.kt`) enthalten nur lesende Methoden; `git diff HEAD` auf beide Dateien ist leer |
| I7 | `boundaryIsInclusive`, `toleranceIsClampedToRange` grün (iOS und Android) |
| I8 | Siehe oben: alle M2–M4-Werte identisch mit Step 0, keiner schlechter |
| I9 | `scripts/lint-hex` sauber; `node --test test/` 114 / 114 (T-DS-1 eingeschlossen) |
| I10 | `git status --porcelain -- design fixtures scripts` leer; `design-freeze --check`, `gen-tokens --check`, `gen-icons --check` grün |
| I11 | `testShellLinks` und `testElectricalLinks` (iOS `EarlyTradesUITests`) sowie `shellLinks`, `electricalLinks` (Android) grün |
