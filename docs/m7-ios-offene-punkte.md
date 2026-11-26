# M7 iOS – Offene Punkte

## Baseline (Schritt 0)

Beobachtete Werte auf `feat/m7-ios` (HEAD `608e6d3`, sauberer Worktree), Simulator iPhone 15. Rohlog: `.scratch/step0-xcodebuild.log` (nicht im Repo).

### Skripte und Design-Prüfungen

| Prüfung | Ergebnis |
|---|---|
| `cd scripts && node --test test/` | 141 Tests, 141 pass, 0 fail, 0 cancelled, 0 skipped |
| `scripts/gen-tokens --check` | Exit 0 |
| `scripts/gen-icons --check` | Exit 0 |
| `scripts/lint-hex` | Exit 0 |
| `scripts/design-freeze --check` | Exit 0 |

### iOS-Testlauf (`xcodebuild test`, Schema Baupilot)

| Target | Tests | bestanden | fehlgeschlagen |
|---|---|---|---|
| BaupilotTests (Unit + Snapshots) | 492 | 424 | 68 |
| – davon `ScreenSnapshotTests` | 68 | 0 | 68 |
| BaupilotUITests | 38 | 38 | 0 |
| gesamt | 530 | 462 | 68 |

Alle 68 Fehlschläge sind `ScreenSnapshotTests` (Toleranzen precision 0.995 / perceptualPrecision 0.98). Kein Nicht-Snapshot-Test schlägt fehl. Hinweis: Das Log endet mit `** BUILD INTERRUPTED **`, weil die Sitzung nach Abschluss beider Test-Bundles (`All tests` bzw. `BaupilotUITests.xctest` vollständig ausgeführt) unterbrochen wurde; das Ergebnis ist davon nicht betroffen.

### Fehlschlagende Screen-Snapshots (Anteil übereinstimmender Pixel)

| Test | pixels that match |
|---|---|
| testAbnElementeDark | 0.9737854 |
| testAbnElementeLight | 0.977005 |
| testAbnGewerkeDark | 0.9721832 |
| testAbnGewerkeLight | 0.9734955 |
| testAbnMaengelDark | 0.98313904 |
| testAbnMaengelLight | 0.98384094 |
| testAbnMangelDark | 0.97998047 |
| testAbnMangelLight | 0.98121643 |
| testAbnPruefungDark | 0.96987915 |
| testAbnPruefungLight | 0.9713135 |
| testAbnSignaturDark | 0.9784241 |
| testAbnSignaturLight | 0.97991943 |
| testDokViewerDark | 0.98275757 |
| testDokViewerLight | 0.9831848 |
| testDokumenteDark | 0.97598267 |
| testDokumenteLight | 0.97680664 |
| testElRauchmelderDark | 0.97468567 |
| testElRauchmelderLight | 0.97558594 |
| testElRaumDark | 0.9716644 |
| testElRaumLight | 0.9734955 |
| testElUebersichtDark | 0.97891235 |
| testElUebersichtLight | 0.9799652 |
| testElVerteilerDark | 0.96585083 |
| testElVerteilerLight | 0.9683533 |
| testErdDark | 0.97435 |
| testErdLight | 0.9751587 |
| testEsBelegreifeDark | 0.9696655 |
| testEsBelegreifeLight | 0.9708557 |
| testEsEbenheitDark | 0.98365784 |
| testEsEbenheitLight | 0.98472595 |
| testEsUebersichtDark | 0.9757385 |
| testEsUebersichtLight | 0.9766388 |
| testFlBadDark | 0.9663696 |
| testFlBadLight | 0.96759033 |
| testFlUebersichtDark | 0.9763794 |
| testFlUebersichtLight | 0.97743225 |
| testGewerkDark | 0.9780121 |
| testGewerkLight | 0.9790497 |
| testHomeDark | 0.9801636 |
| testHomeLight | 0.98114014 |
| testLoginDark | 0.98020935 |
| testLoginLight | 0.98143005 |
| testMaRaumDark | 0.97457886 |
| testMaRaumLight | 0.97613525 |
| testMaUebersichtDark | 0.98420715 |
| testMaUebersichtLight | 0.9854965 |
| testMitteilungenDark | 0.97473145 |
| testMitteilungenLight | 0.9762726 |
| testProfilDark | 0.9832916 |
| testProfilLight | 0.98405457 |
| testRohbauDark | 0.97514343 |
| testRohbauLight | 0.9757538 |
| testRohbauMasseDark | 0.9732666 |
| testRohbauMasseLight | 0.975708 |
| testSanBadDark | 0.96292114 |
| testSanBadLight | 0.9654236 |
| testSanDruckDark | 0.97540283 |
| testSanDruckLight | 0.97636414 |
| testSanGefaelleDark | 0.97224426 |
| testSanGefaelleLight | 0.97398376 |
| testSanUebersichtDark | 0.97257996 |
| testSanUebersichtLight | 0.9734802 |
| testVergleichDark | 0.97702026 |
| testVergleichLight | 0.97798157 |
| testZeitplanDark | 0.98405457 |
| testZeitplanLight | 0.9846802 |
| testZimmererDark | 0.9773712 |
| testZimmererLight | 0.9784088 |

Bester Wert: testMaUebersichtLight 0.9854965; schlechtester: testSanBadDark 0.96292114.

### Vergleich mit `feat/m7:docs/m7-offene-punkte.md` (Z. 23–110)

- Gesamtzahlen iOS identisch: BaupilotTests 492 / 424 / 68, davon ScreenSnapshotTests 68 / 0 / 68, BaupilotUITests 38 / 38 / 0, gesamt 530 / 462 / 68.
- Alle 68 `iOS match`-Werte der Referenztabelle stimmen mit den beobachteten Werten überein (Abweichung ≤ 1e-6; das Log gibt teils eine Stelle mehr aus, z. B. 0.96292114 statt 0.9629211). Keine Differenzen.
- Die Skriptprüfungen (gen-tokens, gen-icons, lint-hex, design-freeze) verhalten sich wie dort beschrieben (Exit 0). Die Android-Werte der Quelle werden hier nicht übernommen.

## Schritt 1 — Sidecar und Baseline

- Portiert aus `fe905f7`: `ReferenceSnapshotSidecarTests.swift` (zuerst), dann `ReferenceSnapshot.swift`. `git diff 608e6d3 -- ios/BaupilotTests/Snapshots/ReferenceSnapshot.swift` zeigt nur Sidecar-Ausgabe und den `contentSize`-Default (`.large`); Toleranzen 0.995 / 0.98 unverändert.
- Rot vor dem Port: Kompilierfehler `type 'ReferenceSnapshot' has no member 'result'` bzw. `'writesSidecar'` in `ReferenceSnapshotSidecarTests.swift` (Exit 65).
- Grün nach dem Port: `ReferenceSnapshotSidecarTests` 2 Tests, `GallerySnapshotTests` / `ReferenceCanarySnapshotTests` ohne Fehler (9 Tests gesamt, 0 Fehler); in `build/snapshots/ios-gallery` liegt kein `*.result.json`.
- `ScreenSnapshotTests`: 68 Tests, 68 erwartete Fehlschläge (0 unexpected, wie Schritt 0); 68 `*.result.json` in `build/snapshots/ios`.
- `scripts/diff-report --all --platform ios` → Exit 0 (68 rot, 0 fehlt); Kopie nach `build/m7/ios/baseline/summary.json` (git-ignoriert).
- Kreuzprüfung: Sidecar-`value` aller 68 Zeilen = „pixels that match X“ aus dem Schritt-0-Log (`step0-failures.txt`), Abweichung ≤ 1e-6. Keine Differenzen.
- Zweiter Lauf von `ScreenSnapshotTests` + `diff-report --baseline build/m7/ios/baseline/summary.json` → Exit 0, `changed` leer (deterministisch); Report als `build/m7/ios/baseline/unstable.json` abgelegt.
- Gate (Schritte 2–7): `scripts/diff-report --all --platform ios --baseline build/m7/ios/baseline/summary.json --unstable build/m7/ios/baseline/unstable.json` → Exit 0.
- `cd scripts && node --test test/`: 141 pass, 0 fail.

## Schritt 2 — Katalog und Launch-Hook

- Portiert aus `fe905f7` (je ein Pfad): `DebugScreensTests.swift` (zuerst), dann `Debug/DebugScreens.swift`, `App/RootView.swift`, `ScreenSnapshotTests.swift`, `AppNavigationUITests.swift`.
- Rot vor dem Port: Kompilierfehler `cannot find 'DebugScreens' in scope` in `DebugScreensTests.swift` (Exit 65).
- Grün nach dem Port: `DebugScreensTests` 3/3; `AppNavigationUITests/testLaunchHookOpensScreenDirectly` bestanden.
- `ScreenSnapshotTests`: 68 Methoden, Namen identisch zu HEAD (sortierter Vergleich); Lauf: 68 erwartete Fehlschläge (0 unexpected, wie Baseline).
- Gate: `scripts/diff-report --all --platform ios --baseline build/m7/ios/baseline/summary.json --unstable build/m7/ios/baseline/unstable.json` → Exit 0 (keine Pixeländerung).
- Release (I7): `strings` auf `Release-iphonesimulator/Baupilot.app/Baupilot` → 0 Treffer für `BaupilotScreen`. Gegenprobe: im Debug-Build liegt der String in `Baupilot.debug.dylib` (1 Treffer; das Haupt-Binary ist dort nur ein Stub).
- Simulator: `iPhone 15` (wie Baseline).
- `cd scripts && node --test test/`: 141 pass, 0 fail.

## Schritt 3 — Semantik, Identifier, Trefferflächen

- Portiert aus `fe905f7` (je ein Pfad): zuerst die fünf Tests (`ScreenCopyTests`, `ComponentMetricsStatusInputTests`, `ComponentMetricsTileTests`, `InspectionFlowUITests`, `LaterTradesUITests`), dann die 15 Komponenten (`Avatar`, `BackBar`, `FilterChips`, `FormField`, `InlineLink`, `IosToggle`, `LargeTitleHeader`, `MinimumHitShape`, `ProtocolBanner`, `SearchField`, `SectionHeader`, `SegmentedControl`, `SignaturePad`, `SlopeResult`, `TileGrid`), `ScreenCopy.swift` und die 16 Screens.
- Rot vor dem Port: `xcodebuild build-for-testing` gegen HEAD-Produktion → Kompilierfehler `type 'ScreenCopy' has no member 'Signature'` (`ScreenCopyTests.swift:8`, `:9`), Exit 65. Der Compiler bricht nach dem ersten Modulfehler ab; die weiteren in §2.1 genannten Symbole (`FilterChips.hitShape`, `SegmentedControl.hitShape(for:)`, `MinimumHitShape`, `IosToggle.hitShape`, `SectionHeader.LinkButton.hitShape`, `TileGrid.accessibilityValue(tiles:)`) fehlen in HEAD ebenfalls. Der Rot-Lauf der UI-Tests (`LaterTradesUITests`, `InspectionFlowUITests`) wurde nicht separat ausgeführt, da die Kompilierung der Unit-Tests bereits rot war.
- Grün nach dem Port: Unit-Tests `ScreenCopyTests`, `ComponentMetricsStatusInputTests`, `ComponentMetricsTileTests`, `ComponentMetricsLaterTradesTests` → 65 Tests, 0 Fehler. UI-Tests `LaterTradesUITests`, `InspectionFlowUITests`, `TradeFlowUITests`, `EarlyTradesUITests`, `AppNavigationUITests` → 40 Tests, 0 Fehler (ein erster Lauf wurde durch das Zeitlimit des Hintergrundbefehls abgebrochen, 21 Tests ohne Fehler; der Wiederholungslauf lief vollständig durch).
- §2.4-Korrekturen: (1) `LoginScreen`: `ph.Login.forgot` → `login.forgot`; `grep -rn 'ph.Login.forgot' ios/Baupilot` leer. (2) `HomeScreen`: `home.schedule` sitzt auf `SectionHeader.Link(identifier:)`, Container-Identifier und `.contain` entfernt; `LinkCoverageUITests` (`:232`) wird in Schritt 4 angepasst. (3) `TileGrid.Tile.identifier` entfernt samt Argument an den vier Aufrufstellen (`FlBadScreen`, `GalleryPage`, `ComponentMetricsLaterTradesTests`, `ComponentMetricsTileTests`). (5) `SanGefaelleScreen`: Abschnittskachel-Label filtert leere Teile vor dem Verbinden (wie `SanGefaelleScreen.kt:91`); Ausgabe auf der heutigen Fixture unverändert („A3, jetzt“).
- §2.4-4: `SearchField` behält `.frame(maxHeight: .infinity)`; das Gate zeigt keine geänderte Zeile, daher keine Bisektion nötig.
- I8: `git diff 608e6d3 -- ios/Baupilot/Screens/ScreenCopy.swift` zeigt nur `Signature` („leer“ / „unterschrieben“, wie `ScreenCopy.kt:283–288`).
- `GallerySnapshotTests`: 2 Tests, 0 Fehler, ohne Neuaufnahme.
- `ScreenSnapshotTests`: 68 erwartete Fehlschläge (0 unexpected, wie Baseline).
- Gate: `scripts/diff-report --all --platform ios --baseline build/m7/ios/baseline/summary.json --unstable build/m7/ios/baseline/unstable.json` → Exit 0 (0 grün, 68 rot, 0 fehlt; keine Pixeländerung).
- `cd scripts && node --test test/`: 141 pass, 0 fail.
- Simulator: `iPhone 15`. Nicht committet (Vorgabe des Laufs: nur Dateien und Tests ändern).

## Schritt 4 — Links und Platzhalter

- `scripts/link-coverage.json`: nur `platforms.ios` `false` → `true`; `git diff 608e6d3 -- scripts` zeigt ausschließlich diese Zeile, Android-Einträge unverändert.
- Rot nach dem Flip: „the real manifest covers every pair and placeholder“ → 121 Probleme (18 verschiedene fehlende iOS-Methoden in `LinkCoverageUITests` plus Platzhalter ohne iOS-Test, wie in §2.2 vorhergesagt).
- Portiert aus `fe905f7` (je ein Pfad): `LinkCoverageUITests` (18 Methoden), `PlaceholderUITests` (31 Methoden); `xcodegen generate` ausgeführt. `home.schedule`-Lookup (`LinkCoverageUITests:232`): `.buttons.firstMatch` entfernt, da der Identifier jetzt auf dem Button sitzt.
- Identifier-Grep: Alle Manifest-Platzhalter mit `ios`-Feld sind in `ios/Baupilot` vorhanden; Vorlagen (`<n>`, `<id>`, Fixture-Ids wie `offer-hahn`) segmentweise über das statische Präfix (z. B. `trade.offer.`, `documents.row.`, `inspection.element.`, `protocol.row.`, `overview.row.`, `notification.`). `tiles.tile.tile-3` ist eine Nicht-Existenz-Prüfung in `LaterTradesUITests`. Keine zusätzlichen Produktionsänderungen nötig (nur §2.4-1 und §2.4-2 aus Schritt 3).
- Grün: `LinkCoverageUITests` 18 + `PlaceholderUITests` 31 → 49 Tests, 0 Fehler.
- `cd scripts && node --test test/`: 141 pass, 0 fail.
- Gate (Produktion in diesem Schritt unverändert, `ScreenSnapshotTests` nicht neu gelaufen): `scripts/diff-report --all --platform ios --baseline … --unstable …` → Exit 0 (0 grün, 68 rot, 0 fehlt; wie Baseline).

## Schritt 5 — Accessibility-Audit

- Guard `audit screen list equals screens.md order` in `scripts/test/links.test.mjs` (von Hand aus `fe905f7:scripts/test/links.test.mjs` l.59–83 übernommen, `parseScreens` aus `../lib/screens.mjs`, `skip` bei `platforms.ios` = false). Rot vorher: `ENOENT … AccessibilityAuditUITests.swift` (Datei fehlte; ein fehlendes Swift-File ist ein Fehler, kein Skip). Grün nach dem Port.
- `AccessibilityAuditUITests` aus `fe905f7` portiert (`xcodegen generate`), nach §5.5 angepasst: `AuditAllowlist.entries` zu Beginn `[]`; Audit-Typen `.all.subtracting([.dynamicType, .textClipped])` (`.hitRegion` enthalten); `setUp` startet die App nicht (`testScheduleChartIsOneImageElement` ruft `launchApp()` selbst auf); `tearDown` und das Ende von `testAuditAllScreens` setzen `XCUIDevice.shared.appearance = .light` zurück; unpassende Findings werden als `AUDIT-FINDING screen=… type=… id=… label=…` geloggt.
- Dunkel-Modus: `XCUIDevice.shared.appearance = .dark` funktioniert (R5-Fallback `-AppleInterfaceStyle Dark` wurde nicht gebraucht, bleibt als Code-Fallback erhalten).
- Erster Lauf mit leerer Allowlist: genau die 10 bekannten Findings (je hell + dunkel, je vor und nach `swipeUp`), `AUDIT-FINDING`:
  - `screen=DokViewer type=1 label=Industriestraße 4 · 68519 Viernheim | Contrast nearly passed`
  - `screen=DokViewer type=1 label=BV Einfamilienhaus Musterweg 12 · gültig bis 31.01.2027 | Contrast nearly passed`
  - `screen=DokViewer type=1 label=1 | Contrast failed`, `label=2 | nearly passed`, `label=3 | nearly passed`, `label=4 | Contrast failed`, `label=5 | nearly passed`
  - `screen=AbnElemente type=4 label=7 von 12 Elementen geprüft | Hit area is too small`
  - `screen=AbnPruefung type=1 label=Foto oben | Contrast nearly passed`
  - `screen=ElRauchmelder type=1 id=step.ok label=Piept – OK | Contrast nearly passed` (nur dunkel)
- Ursache (per `app.debugDescription`): `.accessibilityElement(children: .ignore)` auf einem Container versteckt dessen Texte im iOS-26-Simulator nicht vor dem Audit: die Skizzentexte erscheinen als `StaticText`-Kinder des `Image`-Elements „Vorschau Seite 1 von 2“, ebenso „Foto oben“ unter dem Foto-Tile-`Image`. Fix an der Ursache: Inhalt `.accessibilityHidden(true)`, das Element selbst ist ein `Color.clear`-Overlay mit Label und `.isImage` (Pixel und Layout unverändert).
  - `ios/Baupilot/DesignSystem/Components/DocumentPreview.swift` (`IosDokViewer.dc.html:35`, `role="img"`, `aria-label="Vorschau Seite 1 von 2"`) → 7 DokViewer-Findings behoben.
  - `ios/Baupilot/DesignSystem/Components/PhotoTile.swift` (`IosAbnPruefung.dc.html:51`, Foto-/Szenen-Tile mit Caption) → „Foto oben“ behoben (kein Allowlist-Eintrag nötig).
  - `ios/Baupilot/DesignSystem/Components/SegmentedProgress.swift` (`IosAbnElemente.dc.html:27-31`, Fortschrittsbalken ohne Aktion): `.isImage` ergänzt; das Element hatte keine Interaktions-Trait, wurde aber als label-tragendes „Other“ (325 × 8 pt) für `.hitRegion` geprüft. Als Bild wird es nicht mehr geprüft → Finding behoben.
- Finale Allowlist (1 Eintrag statt 10): `(ElRauchmelder, .contrast, step.ok)` — dunkel: `okInk.elRauchmelder` 0xFFFFFF auf `ok.elRauchmelder` 0x22A355 = 3,26 : 1 (`IosElRauchmelder.dc.html:80`, `ok: '#22A355'`, `okInk: '#FFFFFF'`). Generiertes Token-Paar, nach CLAUDE.md nicht änderbar (R4) → AC3 für diesen einen Button offen und hier gemeldet.
- Grün: `AccessibilityAuditUITests` (2 Tests: `testAuditAllScreens` inkl. Stale-Entry-Prüfung, `testScheduleChartIsOneImageElement`), 0 Fehler.
- Produktionsänderungen geprüft mit `ScreenSnapshotTests` + `ComponentMetrics{Inspection,Structure,Tile,StatusInput}Tests`: 113 Tests, 68 rote Snapshots (identisch zur Baseline, 0 unerwartet).
- Gate: `scripts/diff-report --all --platform ios --baseline build/m7/ios/baseline/summary.json --unstable build/m7/ios/baseline/unstable.json` → Exit 0 (0 grün, 68 rot, 0 fehlt; unverändert zur Baseline).
- Nicht committet (Vorgabe des Laufs: nur Dateien und Tests ändern).

## Schritt 6 — Touch-Ziele

- Guard `every HitShapeAllowlist test exists` in `scripts/test/links.test.mjs` (Regex auf `static let entries: [String: String]` in `TouchTargetUITests.swift`; sucht `class <Cls>` und `func <method>(` rekursiv unter `ios/BaupilotTests`; `skip` bei `platforms.ios` = false). Rot vorher: `ENOENT … TouchTargetUITests.swift` (Datei fehlte). Grün nach dem Port.
- `TouchTargetUITests` aus `fe905f7` portiert (`xcodegen generate`), nach §5.6 angepasst: `.hitRegion`-Audit auf **beiden** Durchläufen (oben und nach `swipeUp`), Findings werden gesammelt statt beim ersten abzubrechen.
- Lauf mit **leerer** Allowlist (Rohlog `.scratch/step6-walk2.log`): genau zwei Elemente unter 44 pt, je oben und gescrollt; das `.hitRegion`-Audit meldet nichts:
  - `ph.AbnElemente.more.1` (Link „Alle 6“, `type=9`): 44.00000000000006 × 43.99999999999994 pt — Rundungsrest unter 44, kein echter Mangel.
  - Switch „Wochenrückblick“ auf Profil (ohne Identifier, `type=40`): 51 × 43.99999999999994 pt — gleiche Ursache.
- Allowlist-Schlüssel (nur diese zwei sind durch ein fehlschlagendes Element begründet; die fünf aus dem HEAD-Port sind auf zwei gekürzt):
  - `ph.AbnElemente.more.` → `ComponentMetricsStatusInputTests.testSectionHeaderLinkHitShapeIs44By44`
  - `@switch` → `ComponentMetricsStatusInputTests.testToggleHitShapeIs44Tall`
  - Entfernt, weil die Läufe ohne Allowlist sie nie melden: `documents.filter.`, `protocol.document`, `ph.ElUebersicht.more.` (deren Unit-Tests `testFilterChipHitShapeIs44`, `testBannerDocumentButtonIs36TallWithA44HitArea` bleiben bestehen).
- Keine echten Treffer-Mängel, daher keine Produktionsänderung und keine neuen Hit-Shape-Tests nötig. Keine Lücken (kein Ziel unter 44 pt, das sich nicht vergrößern ließe).
- `SearchField`: §2.4-4 (`.frame(maxHeight: .infinity)`) ist erhalten; das Textfeld meldet ≥ 44 pt Höhe, `search.` braucht daher keinen Allowlist-Eintrag.
- Grün: `TouchTargetUITests.testAllScreensMeetMinimumTargets` (1 Test, 0 Fehler, 458 s, iPhone 15); `cd scripts && node --test test/links.test.mjs` → 10 pass, 0 fail.
- Gate: Produktion in diesem Schritt unverändert → `ScreenSnapshotTests` nicht neu gelaufen; `scripts/diff-report … --baseline … --unstable …` Exit 0 (0 grün, 68 rot, 0 fehlt; wie Baseline).
- Nicht committet (Vorgabe des Laufs: nur Dateien und Tests ändern).

## Schritt 7a — Typografie-Einheiten

- Rot zuerst: fünf neue Tests in `TypographyTests` (`testLineBoxForCategoryIsIdenticalAtLarge`, `testLineBoxScalesAboveLarge`, `testInterTextSingleAtXLMatchesScaledBox`, `testSingleTruncatingIsSingleAtLarge`, `testGreedyLabelFontScalesAtXL`); Build schlug fehl an `LineHeight.box(for:category:)`, `TextLines.singleTruncating`, `GreedyLabel.font(weight:size:category:)`.
- Einheiten in `Typography.swift`: `LineHeight.box(for:category:)` (bei `.large` und darunter exakt `box(for:)`), `InterMetrics.scaledSize`, `UIContentSizeCategory(DynamicTypeSize)`, `TextLines.singleTruncating` (Switch ohne `default`), `InterTextModifier` (bei `.large` dieselbe Kette wie zuvor; darüber `.single` mit `minHeight` + `fixedSize(vertical)`), `interText` delegiert mit unveränderter Signatur, `scaledFrame(height:alignment:)` (bei `.large` wörtlich `.frame(height:alignment:)`, darüber `minHeight`). `GreedyLabel`: `UIFontMetrics.scaledFont` nur über `.large`, Zeilenbox über `box(for:category:)`.
- Abweichung vom Plantext (zweite): `GreedyLabel` verwendet immer einen `UIFontMetrics`-Font plus `adjustsFontForContentSizeCategory = true` (Plan §5.7: schlichter `UIFont` bei `.large`), weil das Audit einen schlichten `UIFont` als nicht skalierend meldet; bei `.large` ist der Faktor 1, die Darstellung gleich. Unbelegt: UIKit skaliert nach der eigenen Trait, die SwiftUI-Umgebung wählt die Größe; ein `GreedyLabel` unter fester `.dynamicTypeSize(.large)` könnte wachsen (keine solche Stelle im Code).
- Abweichung vom Plantext: Die skalierte Größe wird auf ganze Punkte gerundet (`scaledValue(...).rounded()`). Gemessen auf iPhone 15: `Font.custom` und `scaledFont` rendern gerundet (15 → 16, 20 → 22, 28 → 31), `scaledValue` liefert 16,33 / 21,67 / 30,67; ohne Rundung passte die Zeilenbox nicht zu den Glyphen.
- `testInterTextSingleAtXLMatchesScaledBox` prüft `box ≤ Höhe < box + 1`, nicht exakte Gleichheit: bei 20 pt und 28 pt ist der ungerundete Text (26,62 / 37,51) höher als Chromes gerundete Box (26 / 37), und `.single` ist über `.large` nur `minHeight`. Unterschied < 1 pt.
- Gate: `ScreenSnapshotTests` + `GallerySnapshotTests` + `ComponentMetrics{Inspection,Structure,Tile,StatusInput}Tests`: 115 Tests, 68 rote Screen-Snapshots (wie Baseline, 0 unerwartet), Galerie-Goldens unverändert; `scripts/diff-report … --baseline … --unstable …` → Exit 0 (0 grün, 68 rot, 0 fehlt); `cd scripts && node --test test/` → 143 pass.
- Nicht committet (Vorgabe des Laufs: nur Dateien und Tests ändern).

## Schritt 7b — Large Text Prüfung

- Portiert aus `fe905f7`: `LargeTextRenderTests` (`testRenderAllScreensXL` / `…XXXL`, schreibt `build/snapshots/ios-large/<Name>_xl.png` / `_xxxl.png`, nur `checkSize`, 2 Tests grün, 68 PNGs) und `LargeTextUITests.testNoClippedOrOverlappingTextAtXL` (`xcodegen generate` danach). Der UI-Test lädt `AuditAllowlist` aus `AccessibilityAuditUITests` und loggt jedes nicht gefilterte Finding als `XL-FINDING screen=… type=… frame=… | Element | Beschreibung`. `TEST_RUNNER_LARGE_TEXT_SCREENS=Erd,Login` beschränkt einen Lauf auf diese Screens (Entwicklungshilfe; ohne Variable alle 34).
- `IntendedEllipsis.entries` (Literal in `LargeTextUITests.swift`, `(screen:, identifier:, prototypeRef:)`) ist leer: Kein XL-Finding ist eine Kürzung, die der Prototyp selbst mit `nowrap` + `ellipsis` vorsieht (Kandidaten IosDokumente:63–64, IosZeitplan:57 melden keine `.textClipped`-Findings). Der neue Guard `every IntendedEllipsis entry cites an existing prototype line` in `scripts/test/links.test.mjs` (11 Tests grün) liest das Literal, verlangt die Syntax, eine existierende `Ios<Screen>.dc.html`-Datei und in der zitierten Zeile `text-overflow: ellipsis` oder `white-space: nowrap`; mit einem Eintrag auf `IosHome.dc.html:1` schlägt er rot fehl (geprüft).
- Überlappungsprüfung: Container-Elemente („Gewerk, Estrich“) und ihr eigener Text unterscheiden sich um Float-Rauschen; `overlaps` lässt deshalb 0,5 pt Spiel bei „enthält“ (Schnitt > 1 pt in beiden Achsen bleibt).

### Findings und Behandlung

| Screen | Pass | Finding | Ursache | Behandlung |
|---|---|---|---|---|
| Login, Erd, Rohbau, ElRaum, ElUebersicht, ElVerteiler, ElRauchmelder, Mitteilungen, Zimmerer, Es*, Fl*, Ma*, San*, … (alle Screens mit `GreedyLabel`) | oben + gescrollt | `.dynamicType` „partially unsupported“ auf Titeln/Texten | `GreedyLabel` setzte bis `.large` eine einfache `UIFont`; das Audit zählt sie nicht als Dynamic Type | `GreedyLabel.font` liefert immer eine `UIFontMetrics`-Schrift (Faktor 1 bei `.large` und darunter), `adjustsFontForContentSizeCategory = true` (Fehler in `p8t1`, belegt durch den roten XL-Test) |
| Login | oben + gescrollt | `.textClipped` Einleitungstext | feste Höhe `introHeight` (3 Zeilen) | `scaledFrame(height: introHeight, alignment: .top)`, Prototyp-Kommentar unverändert |
| Home | oben + gescrollt | Überlappung „Einfamilienhaus · Musterweg 12“ × „Übersicht“ (≈ 2 pt) | `header` zieht die 44-pt-Titelzeile mit `lineBox(32)` unter den Kicker; über `.large` wächst die Titelbox | Zug nach oben aus `LineHeight.normal.box(for: 32, category:)`, `max(44, Titelbox)`; bei `.large` identisch |
| DokViewer | (vorsorglich, Vorgabe P5) | — (der XL-Lauf meldet für die Vorschau kein Finding) | `role="img"` in `IosDokViewer.dc.html:35`: ein Bild behält seinen Text-Maßstab | P5: `.environment(\.dynamicTypeSize, .large)` am Ende von `DocumentPreview.body`, zitiert |
| RohbauMasse | oben + gescrollt | `.dynamicType` auf den Kacheln F1–F7 (`CodeTile`) | feste Höhe 40 | `scaledFrame(height: 40)` (IosRohbauMasse:58); Finding bleibt (siehe offen) |
| Abn*, Dokumente, Gewerk, Zeitplan, DokViewer, ElRauchmelder, ElVerteiler | oben + gescrollt | Überlappung Container × Kindtext | Float-Rauschen in `contains` | Spiel 0,5 pt im Test (kein Produktionscode) |

### Offen (nicht versteckt, nicht in `AuditAllowlist`/`IntendedEllipsis`)

- **Login „E-Mail“, Dokumente „Dokumente durchsuchen“** (`.textClipped`, beide `TextField`): bleibt mit festem 48/44-pt-Rahmen, mit `scaledFrame`/größerem Rahmen, ohne Schrift am Prompt und sogar mit Systemschrift (`.font(.body)`) bestehen; es ist ein Befund des Audits für jedes einzeilige SwiftUI-`TextField`, kein Layoutfehler. `SecureField` meldet nichts. Ein Versuch mit `TextField(axis: .vertical)` + `lineLimit(1)` beseitigt das Audit-Finding, verschiebt aber die Galerie-Goldens (Pixelübereinstimmung 0,99488 / 0,99498 < 0,995, hell und dunkel): verworfen, Default-Darstellung bleibt unverändert (Gate vor Finding). Kein Prototypbezug auf `nowrap`/`ellipsis`, daher kein `IntendedEllipsis`-Eintrag; ein `AuditAllowlist`-Eintrag würde in `testAuditAllScreens` als veraltet scheitern.
- **Gewerk, Profil, EsEbenheit** (`.dynamicType` + `.textClipped`, Element `nil`, „SwiftUI.AccessibilityNode“): Profil verschwindet, wenn die Karten mit den `SettingsRow`s ausgeblendet werden (Avatar-Karte, Kopf, Abschnittstitel nicht schuld); `IosToggle`-Label/-Wert als `String` statt `Text` ändert nichts. Das Element ist nicht benennbar (`issue.element == nil`), die Ursache auf Zeilenebene (vermutlich Knoten mit festem Rahmen und nur Label) ist nicht belegt.
- **RohbauMasse F1–F7** (`CodeTile` in `LazyVGrid`): `.dynamicType` bleibt auch ohne festen Rahmen, mit `lineLimit(1)` und mit `minHeight`; Ursache nicht belegt.
- **Vergleich** (Überlappung „Fließestrich Hahn“ × „Preis brutto …“, 4 pt): die Spaltenkopf-Zelle verlängert ihre Trefferfläche um 14 pt nach unten (44-pt-Regel aus Schritt 6); der Rahmen des Elements enthält diese Fläche. Kein Textüberlapp, kein Default-Unterschied (14 pt Trefferfläche, 10 pt Abstand). **Erledigt in Schritt 7c** (`MinimumHitShape`).
- Letzter vollständiger Lauf von `testNoClippedOrOverlappingTextAtXL` (≈ 25 min für 34 Screens): rot nur für die vier Punkte oben (Home war danach mit der Korrektur in einem Einzellauf sauber).

### Prüfungen

- `cd scripts && node --test test/links.test.mjs`: 11 pass, 0 fail.
- Gate: `ScreenSnapshotTests` + `GallerySnapshotTests` + `TypographyTests` + `LargeTextRenderTests` (iPhone 15): Galerie-Goldens unverändert, `TypographyTests` grün, 68 rote Screen-Snapshots wie in der Baseline; `scripts/diff-report --all --platform ios --baseline build/m7/ios/baseline/summary.json --unstable build/m7/ios/baseline/unstable.json` Exit 0 (0 grün, 68 rot, 0 fehlt).
- Nicht committet (Vorgabe des Laufs: nur Dateien und Tests ändern).
- Gate nach Rücknahme von `TextField(axis: .vertical)` (`FormField`, `SearchField`): `GallerySnapshotTests` (Goldens unverändert), `TypographyTests` 13 grün, `ScreenSnapshotTests` 68 rot wie Baseline. Im Volllauf wich `FlUebersicht_light` ab (0,97743 → 0,97449, `diff-report` Exit 4); der Einzellauf von `testFlUebersichtLight` liefert wieder exakt 0,97743225 (Baseline), Das ist kein Rauschen, sondern eine reproduzierbare, reihenfolgeabhängige Verschiebung (siehe „FlUebersicht_light: reihenfolgeabhängige Verschiebung“ unten); der Einzellauf ist kein Gate-Ersatz. `links.test.mjs` 11 grün.

## Schritt 7c — Audit und Large-Text-Sichtprüfung

### Sichtprüfung bei XL (`build/snapshots/ios-large/<Name>_xl.png`, 34 PNGs einzeln geöffnet; iPhone 15, 1170 × 2532)

Neu gerendert mit `LargeTextRenderTests` (2 Tests grün) und danach jede `_xl`-Datei einzeln angesehen. „Überlapp“ = Text über Text oder Text über einer fremden Fläche. „Kürzung“ = ein Wort/Satz mit `…` oder abgeschnittene Zeile. Kein Screen kürzt Text mit `…`; `IntendedEllipsis.entries` bleibt leer (der Prototyp sieht nur dort `ellipsis` vor, wo Schritt 7b keine `.textClipped`-Meldung fand).

| # | Screen | Überlapp | Kürzung (Prototypgrund) | Beobachtung |
|---|---|---|---|---|
| 1 | Login | nein | keine | Einleitung 4 Zeilen, Feature-Zeilen umbrechen, `introHeight` wächst mit |
| 2 | Home | nein | keine | „Baubeginn März 2026“ und „Einzug geplant Sep 2027“ stoßen ohne Abstand aneinander; „Rechnungen 4“ eng, nichts verdeckt |
| 3 | Zeitplan | nein | keine | „Sanitär & Heizung“ zweizeilig; Monatsbeschriftung „Mär 26 Jul“ eng (Diagramm ist ein Bild, D9) |
| 4 | Profil | nein | keine | — |
| 5 | Gewerk | nein | keine | — |
| 6 | Dokumente | nein | keine; Filterleiste läuft rechts aus dem Bild („Rechnu…“) | Das ist der waagerechte Scroll der Chip-Leiste (IosDokumente, `overflow-x: auto`), kein Kürzen; „· offen“ rutscht in eine dritte Zeile |
| 7 | DokViewer | nein | keine | Vorschau bleibt bei `.large` (Bild, `role="img"`, IosDokViewer.dc.html:35) |
| 8 | Vergleich | nein (Text); der Test meldete 4 pt Überlapp der Elementrahmen, behoben | keine | Spaltenköpfe zweizeilig; im PNG kein Überlapp sichtbar, der Rahmen der Kopfzelle reichte durch die 14-pt-Trefferfläche in die erste Zeile |
| 9 | Mitteilungen | nein | keine | — |
| 10 | AbnGewerke | nein | keine | Zeilen mit festem Raster: Titel liegt direkt an der Kartenoberkante, Untertitel („abgenommen 14.04.“) an der Trennlinie; kein Überlapp, kein Abstand |
| 11 | AbnElemente | nein | keine | — |
| 12 | AbnPruefung | nein | keine | „Foto oben“ liegt wie im Prototyp auf dem Foto (Beschriftung auf Foto); Schrittzeilen eng |
| 13 | AbnMangel | nein | keine | „Abbrechen“ und „Mangel erfassen“ berühren sich ohne Lücke; „Handwerker per E-Mail informieren“ zweizeilig im festen Rahmen, eng |
| 14 | AbnSignatur | nein | keine | „Hier unterschreiben“ umbricht |
| 15 | AbnMaengel | nein | keine | — |
| 16 | Erd | nein | keine | — |
| 17 | Rohbau | nein | keine | — |
| 18 | RohbauMasse | nein | keine | Zeile „Wände im Lot“ (3 Zeilen) füllt die Karte bis zum Rand |
| 19 | Zimmerer | nein | keine | — |
| 20 | ElUebersicht | nein | keine | Dreizeilige Zeilen („Verteilerplan & Stromkreisliste“) füllen ihre Zeile bis an Trennlinie/Kartenrand |
| 21 | ElRaum | nein | keine | Schrittzeilen 3–5 eng |
| 22 | ElVerteiler | nein | keine | „WC, Außen“ liegt auf der Trennlinie; Inhalt läuft unten aus dem Bild (scrollbar) |
| 23 | ElRauchmelder | nein | keine | — |
| 24 | SanUebersicht | nein | keine | „Dämmung Warmwasser“ + Untertitel füllen die Zeile; kein Überlapp |
| 25 | SanGefaelle | nein | keine | Wert „2,5 cm“ im Stepper umbricht auf zwei Zeilen (fester Stepper-Rahmen im Prototyp) |
| 26 | SanDruck | nein | keine | Titel zweizeilig |
| 27 | SanBad | nein | keine | Umriss-Pillen (Dusche, Heizkörper, Lüfter) zeigen einen feinen senkrechten Randstrich neben der Kappe; kein Textbezug, Ursache nicht untersucht |
| 28 | EsUebersicht | nein | keine | „CM-Messung“-Untertitel eng an der Karte |
| 29 | EsEbenheit | nein | keine | Untertitel bricht nach „2-“ (Trennung am Bindestrich) |
| 30 | EsBelegreife | nein | keine | Inhalt läuft unten aus dem Bild (scrollbar); „35 °C Vorlauf“ umbricht |
| 31 | FlUebersicht | nein | keine | Untertitel „4 Fotos“ / „Fotos“ sitzen an Trennlinie bzw. Kartenrand |
| 32 | FlBad | nein | keine | „Keine Hohlstellen“ füllt die Schaltfläche bis zum Rand; Inhalt läuft unten aus dem Bild (scrollbar) |
| 33 | MaUebersicht | nein | keine | „3 bemustert“ und „3 von 9“ ohne Abstand |
| 34 | MaRaum | nein | keine | Umriss-Pillen (West, Decke) wie bei SanBad |

Ergebnis: bei XL in **keinem** der 34 Screens ein Überlapp und keine Kürzung. Einzige Auffälligkeiten sind fehlender Luftraum in Zeilen mit fester Prototyphöhe, die kein Test als Überlapp meldet — mit einer Ausnahme, die der Test meldete (Vergleich, siehe „Audit bei `.all`“ unten).

#### Zusatz: xxxL (`_xxxl.png`, Stichprobe AbnGewerke, Home, Zeitplan, SanBad)

- Home: „Installateur“ bricht mitten im Wort („Installateu / r“), Baubeginn/Einzug umbrechen — lesbar, kein Überlapp.
- Zeitplan: „Mär 26“ und „Jul“ berühren sich („26Jul“, „27Jul“) — keine Überdeckung.
- AbnGewerke: Titel, Untertitel und Plakette füllen die festen Zeilen ganz aus; letzte Zeile verschwindet scrollbar hinter der Tab-Leiste (normal).
- SanBad: Zeilenumbrüche, kein Überlapp.
- Die Tab-Leiste kürzt bei xxxL nichts („Übersicht … Dokumente“ eng, aber vollständig).

### Audit bei `.all`

- `AccessibilityAuditUITests.auditTypes` ist `.all` (inklusive `.dynamicType`, `.textClipped`, `.hitRegion`). Entwicklungshilfe: `TEST_RUNNER_AUDIT_SCREENS=ElRauchmelder,Login` (Umgebungsvariable, nicht als Build-Setting) beschränkt `testAuditAllScreens` auf diese Screens; der Veraltet-Check entfällt dann. `AUDIT-FINDING` loggt jetzt auch `detailedDescription`.
- Vollständiger Lauf (`AccessibilityAuditUITests` + `LargeTextUITests`, iPhone 15, 34 Screens × hell/dunkel × oben/gescrollt, ≈ 22 min je Klasse). Findings:

| Screen | Typ | Finding | Behandlung |
|---|---|---|---|
| ElRauchmelder (dunkel) | `.contrast` | „Contrast nearly passed“ auf der Schaltfläche `step.ok` („Piept – OK“): `okInk` (weiß) auf `ok` (dunkles Grün) = 3,26:1 (IosElRauchmelder.dc.html:80, dunkler Zweig von `t`) | `AuditAllowlist`-Eintrag (Token-Paar, nicht änderbar, CLAUDE.md). Das Audit meldet denselben Befund mal mit Element `step.ok`, mal mit `issue.element == nil`; der Eintrag trägt `orNilElement: true` und trifft beide Fälle, aber nur auf ElRauchmelder, `.contrast`, dunkel; jeder Treffer wird als `AUDIT-ALLOWED … path=named|nil-element` geloggt. Ein Element ohne Namen trifft nie (nur `issue.element == nil`). Abweichung vom Plan (§5.5/P7: nur exakte Treffer): der element-lose Pfad ist nur aus Logs, nicht anders belegt |
| Login | `.textClipped` | im Vollauf nicht mehr gemeldet (Einzellauf ebenfalls sauber) | veralteter Allowlist-Eintrag „E-Mail“ entfernt |
| Dokumente | `.textClipped` | Suchfeld „Dokumente durchsuchen“ (einzeiliges `TextField`) | `AuditAllowlist`-Einträge bleiben (hell und dunkel, je einer; das Finding tritt in beiden Modi auf, wie der ungeteilte Lauf zeigte): `TextField(axis: .vertical)` beseitigt es, verschiebt den Text um 1,3 pt (IosDokumente.dc.html, Suchzeile 44 pt, 16 pt) und ändert das StatusInput-Galerie-Golden |
| alle übrigen 32 | — | keine Findings (Hit-Region, Dynamic Type, Textkürzung, Kontrast, Element-Erkennung) | — |

- Test-Befund aus der Sichtprüfung (Abgleich PNG ↔ `LargeTextUITests`): **Vergleich** meldete bei XL „Fließestrich Hahn“ × „Preis brutto …“ (4 pt, oben und gescrollt). Ursache war die feste Trefferflächen-Verlängerung `padding(±14)` in `ComparisonTable.HeaderCell`, die bei zweizeiligem XL-Text in die erste Zeile reicht. Behebung an der Ursache: `contentShape(MinimumHitShape())` (wächst nur bis 44 pt, eine höhere Zelle behält ihre Höhe); `hitExpansion` entfällt. Bei Standardgröße unverändert (Zelle < 44 pt wird auf 44 pt zentriert verlängert, kein Layoutunterschied). `testComparisonColumnHeaderHitArea44` prüft jetzt Mindesthöhe 44 und „höhere Zelle bleibt 66“. Der Punkt „Vergleich“ unter „Offen“ in Schritt 7b ist damit erledigt. Alle anderen PNG-Beobachtungen („eng“, kein Luftraum) sind kein Überlapp und wurden von keinem Test gemeldet — kein weiterer Befund.
- **Endstand `AuditAllowlist.entries`** (2 Einträge, beide mit Prototyp- bzw. Token-Begründung):
  1. `(Dokumente, .textClipped, „Dokumente durchsuchen“)`, je ein Eintrag für hell und dunkel — Prototypgeometrie 44 pt / 16 pt, Golden-Gate. Gemessen (Review-Zyklus 1, Punkt 8): ohne `.frame(maxHeight: .infinity)` und `.contentShape` am `SearchField` bleibt das Finding in beiden Modi; der Modifier ist also nicht die Ursache und bleibt (er trägt die 44-pt-Trefferfläche).
  2. `(ElRauchmelder, .contrast, „step.ok“, dunkel, orNilElement)` — Token-Paar `okInk`/`ok` der Seite, dunkel, 3,26:1.
- **AC3 ist nicht erfüllt**: AC3 gilt nur bei leerer `AuditAllowlist`; zwei Einträge bleiben. Beide sind durch Projektregeln blockiert (Token-Paar bzw. 1:1-Prototypgeometrie mit Golden-Toleranz). Entscheidung für den Operator: Token-Paar `ok`/`okInk` in `design/tokens.json` (dunkel) ändern bzw. die Suchfeld-Goldens neu freigeben — beides außerhalb dieses Auftrags. Der manuelle Inspector-Lauf bleibt Operator-Schritt (R16).
- Prüfungen: `AccessibilityAuditUITests` (Einzellauf ElRauchmelder, Login, Vergleich) und `testScheduleChartIsOneImageElement` grün; `LargeTextUITests` Vergleich grün nach der Korrektur (im Vollauf davor war Vergleich der einzige Befund); `TouchTargetUITests` und `ComponentMetricsListRowTests` grün. Gate: `ScreenSnapshotTests` + `GallerySnapshotTests` (68 rote Screen-Snapshots wie Baseline, Galerie unverändert); im Volllauf wich `FlUebersicht_light` wieder ab (0,97743 → 0,97449, bekannte Streuung aus 7b), der Einzellauf liefert den Baseline-Wert (zählt nicht als Gate, siehe „FlUebersicht_light: reihenfolgeabhängige Verschiebung“). `cd scripts && node --test test/` → 144 pass, 0 fail.
- Nicht committet (Vorgabe des Laufs: nur Dateien und Tests ändern).

## Schritt 8 — Snapshot-Hebel

Messung jeweils: voller `ScreenSnapshotTests`-Lauf (68 Tests), danach `scripts/diff-report --all --platform ios --baseline build/m7/ios/baseline/summary.json --unstable build/m7/ios/baseline/unstable.json`. Vergleichsgröße ist die Summe der Sidecar-`value` über alle 68 Zeilen. Toleranzen 0.995 / 0.98 und alle Referenzen unverändert. Nichts committet (Vorgabe des Laufs: nur Dateien und Tests ändern); kein Hebel wurde übernommen, daher auch keine `build/m7/ios/after-8*`-Zustände und kein `testWholePointBaselinesMatchChromeRounding`.

Startwert (vor jedem Spike, Lauf mit unverändertem Baum): **0 grün, 68 rot, Summe 66,400551**; alle 68 Zeilen identisch zur Baseline (`changed` leer).

### Spikes

| Spike | Änderung | Grün | Summe `value` | Zeilen verändert | Schlechter als −0,0005 | Entscheidung (D17) |
|---|---|---|---|---|---|---|
| 8a-1 | `interText(..., wholePoint:)`: Pitch (`lineSpacing`) im `.multi`-Fall aus ganzzahliger Zeilenbox; nur `InfoCallout` | 0 → 0 | 66,400551 → 66,400551 | 0 (alle 68 `actualSha256` gleich) | 0 | **verworfen** — keine Wirkung: Die Screens, die `InfoCallout` nutzen (EsBelegreife, SanBad, SanGefaelle), rufen es mit `.greedy` bzw. `.block` auf; der `.multi`-Pfad wird dort nicht durchlaufen. Auch `CurrentStepCard.body` (StepRow, 15 × 1,45 = 21,75) hat im Code keinen Aufrufer. |
| 8a-2 | wie 8a-1, aber im `.block`/`.greedy`-Pfad (`BlockTextLayout`, Zeilenbox auf ganze Punkte gerundet); weiter nur `InfoCallout` | 0 → 0 | 66,400551 → 66,344292 (−0,056259) | 10 (Vergleich, ElVerteiler, SanGefaelle, SanBad, EsUebersicht je hell/dunkel) | 6: ElVerteiler dunkel −0,026550, ElVerteiler hell −0,022278, Vergleich dunkel −0,003632, Vergleich hell −0,003586, SanBad dunkel −0,001068, SanBad hell −0,000732 | **verworfen** — Summe sinkt, aber mehrere Zeilen werden um mehr als 0,0005 schlechter (ElVerteiler Lage-Anteil 0,41 → 0,75); nur EsUebersicht (+0,0009) und SanGefaelle hell (+0,0003) werden besser; keine Zeile wird grün |
| 8b | `drawHierarchyInKeyWindow: true` in `ReferenceSnapshot.render` (`Snapshotting.image(drawHierarchyInKeyWindow:precision:…)`) | 0 → 0 | 66,400551 → 66,136437 (−0,264114) | 67 (1 unverändert) | 3: Login dunkel −0,127060, Login hell −0,124985, SanBad dunkel −0,000519; 0 Zeilen > +0,0005 besser, größter Gewinn +0,000137 (Mitteilungen dunkel) | **verworfen** — die Summe sinkt nur, weil Login einbricht; alle übrigen Zeilen bewegen sich im Rauschen (≤ 0,00014). Die Galerie-Goldens wurden deshalb nicht neu aufgenommen |

Hinweis zur Messung: Ein erster 8b-Lauf scheiterte am Übersetzen (Argumentreihenfolge: `drawHierarchyInKeyWindow` steht vor `precision`) und lieferte die Sidecar-Dateien des 8a-2-Laufs; er wurde verworfen und mit korrekter Reihenfolge wiederholt. Ebenso war der erste 8a-Vergleich eine veraltete `summary-ios.json` — `scripts/diff-report` muss nach jedem Testlauf neu laufen.

Die 8a-Zahlen für 8a-2 stammen aus dem Lauf vor dem Zurücksetzen; die Diff-Ausgabe (Zeile je `geändert:`) wurde ausgewertet, die Sidecar-Dateien dieses Laufs sind überschrieben.

### Stop-Regel

8a: Die ersten beiden Spikes zeigen keinen Gewinn nach D17 (8a-1 ohne jede Wirkung, 8a-2 mit sechs Verschlechterungen über 0,0005 und ohne neue grüne Zeile) → 8a nach zwei Komponenten-Spikes beendet, kein dritter Versuch. Kein geänderter Messwert, daher keine Prototyp-Zeile zu zitieren. 8b: ein Spike, verworfen.

### Endstand und Vergleich mit der Baseline (I10)

- Baum ist zurückgesetzt: `Typography.swift`, `InfoCallout.swift`, `StepRow.swift`, `ReferenceSnapshot.swift` haben keine Spike-Änderung (`wholePoint` kommt nirgends vor, kein `drawHierarchyInKeyWindow`).
- Schlusslauf (`ScreenSnapshotTests` + `TypographyTests` + `GallerySnapshotTests`, 83 Tests): 68 erwartete rote Screen-Snapshots (0 unexpected), die 13 Typografie-/Galerie-Tests grün, Galerie-Goldens unverändert. Im Volllauf wich damals `FlUebersicht_light` ab (0,97743 → 0,97449); das war keine Streuung, sondern eine reproduzierbare, reihenfolgeabhängige Verschiebung (`diff-report` Exit 4). Sie ist seit Review-Zyklus 1 behoben (Ursache `ScaledFrameModifier`, siehe „FlUebersicht_light: reihenfolgeabhängige Verschiebung (behoben)“); das Gate in dieser Reihenfolge ergibt Exit 0.
- `cd scripts && node --test test/` → 144 pass, 0 fail.
- **AC1 (68/68 grün) bleibt unerfüllt: 0 von 68.** Bester Wert MaUebersicht hell 0,985497, schlechtester SanBad dunkel 0,962921 (gegen 0,995).

### Zeilenbelege (Baseline = Endstand)

Spalten: `value` (Anteil übereinstimmender Pixel, Soll ≥ 0,995), Lage-Anteil (`positionShare` aus `diff-report`: Anteil der abweichenden Pixel, der durch Verschieben um wenige Pixel verschwindet), Ursachenklasse. Die Klasse ist aus dem Lage-Anteil abgeleitet, nicht je Zeile einzeln analysiert: Lage-Anteil ≥ 0,4 → `Lage-Versatz (R17)`, darunter → `Glyphen-Raster (R1)`. Die Klassen `Layout <Komponente>` und `Umbruch` wurden in Schritt 7b/7c nur an einzelnen Screens beobachtet (SanGefaelle, EsEbenheit, AbnSignatur …) und sind hier nicht je Zeile zugeordnet. Hebel je Zeile: 8a-1 ohne Wirkung, 8a-2 und 8b verworfen (siehe oben); die Spalte „Hebel-Wirkung“ nennt, wo ein Spike die Zeile veränderte (8a-2 / 8b), sonst „—“.

| # | Screen | Modus | value | Lage-Anteil | Ursachenklasse | Hebel-Wirkung (8a-2 / 8b) |
|---|---|---|---|---|---|---|
| 1 | Login | dunkel | 0.980209 | 0.166 | Glyphen-Raster (R1) | — / -0.127060 |
| 1 | Login | hell | 0.981430 | 0.179 | Glyphen-Raster (R1) | — / -0.124985 |
| 2 | Home | dunkel | 0.980164 | 0.250 | Glyphen-Raster (R1) | — / -0.000229 |
| 2 | Home | hell | 0.981140 | 0.238 | Glyphen-Raster (R1) | — / -0.000015 |
| 3 | Zeitplan | dunkel | 0.984055 | 0.151 | Glyphen-Raster (R1) | — / -0.000229 |
| 3 | Zeitplan | hell | 0.984680 | 0.145 | Glyphen-Raster (R1) | — / -0.000145 |
| 4 | Profil | dunkel | 0.983292 | 0.111 | Glyphen-Raster (R1) | — / -0.000015 |
| 4 | Profil | hell | 0.984055 | 0.101 | Glyphen-Raster (R1) | — / -0.000076 |
| 5 | Gewerk | dunkel | 0.978012 | 0.186 | Glyphen-Raster (R1) | — / -0.000214 |
| 5 | Gewerk | hell | 0.979050 | 0.171 | Glyphen-Raster (R1) | — / -0.000153 |
| 6 | Dokumente | dunkel | 0.975983 | 0.083 | Glyphen-Raster (R1) | — / -0.000305 |
| 6 | Dokumente | hell | 0.976807 | 0.085 | Glyphen-Raster (R1) | — / -0.000122 |
| 7 | DokViewer | dunkel | 0.982758 | 0.246 | Glyphen-Raster (R1) | — / -0.000061 |
| 7 | DokViewer | hell | 0.983185 | 0.229 | Glyphen-Raster (R1) | — / -0.000061 |
| 8 | Vergleich | dunkel | 0.977020 | 0.095 | Glyphen-Raster (R1) | -0.003632 / +0.000076 |
| 8 | Vergleich | hell | 0.977982 | 0.088 | Glyphen-Raster (R1) | -0.003586 / -0.000061 |
| 9 | Mitteilungen | dunkel | 0.974731 | 0.362 | Glyphen-Raster (R1) | — / +0.000137 |
| 9 | Mitteilungen | hell | 0.976273 | 0.378 | Glyphen-Raster (R1) | — / -0.000061 |
| 10 | AbnGewerke | dunkel | 0.972183 | 0.034 | Glyphen-Raster (R1) | — / -0.000122 |
| 10 | AbnGewerke | hell | 0.973495 | 0.031 | Glyphen-Raster (R1) | — / +0.000000 |
| 11 | AbnElemente | dunkel | 0.973785 | 0.165 | Glyphen-Raster (R1) | — / -0.000214 |
| 11 | AbnElemente | hell | 0.977005 | 0.182 | Glyphen-Raster (R1) | — / -0.000015 |
| 12 | AbnPruefung | dunkel | 0.969879 | 0.395 | Glyphen-Raster (R1) | — / -0.000198 |
| 12 | AbnPruefung | hell | 0.971314 | 0.408 | Lage-Versatz (R17) | — / -0.000214 |
| 13 | AbnMangel | dunkel | 0.979980 | 0.202 | Glyphen-Raster (R1) | — / -0.000351 |
| 13 | AbnMangel | hell | 0.981216 | 0.194 | Glyphen-Raster (R1) | — / -0.000153 |
| 14 | AbnSignatur | dunkel | 0.978424 | 0.312 | Glyphen-Raster (R1) | — / -0.000122 |
| 14 | AbnSignatur | hell | 0.979919 | 0.304 | Glyphen-Raster (R1) | — / +0.000015 |
| 15 | AbnMaengel | dunkel | 0.983139 | 0.128 | Glyphen-Raster (R1) | — / -0.000275 |
| 15 | AbnMaengel | hell | 0.983841 | 0.121 | Glyphen-Raster (R1) | — / -0.000137 |
| 16 | Erd | dunkel | 0.974350 | 0.159 | Glyphen-Raster (R1) | — / -0.000198 |
| 16 | Erd | hell | 0.975159 | 0.152 | Glyphen-Raster (R1) | — / -0.000198 |
| 17 | Rohbau | dunkel | 0.975143 | 0.131 | Glyphen-Raster (R1) | — / -0.000168 |
| 17 | Rohbau | hell | 0.975754 | 0.124 | Glyphen-Raster (R1) | — / -0.000214 |
| 18 | RohbauMasse | dunkel | 0.973267 | 0.355 | Glyphen-Raster (R1) | — / -0.000351 |
| 18 | RohbauMasse | hell | 0.975708 | 0.368 | Glyphen-Raster (R1) | — / -0.000107 |
| 19 | Zimmerer | dunkel | 0.977371 | 0.107 | Glyphen-Raster (R1) | — / -0.000168 |
| 19 | Zimmerer | hell | 0.978409 | 0.103 | Glyphen-Raster (R1) | — / -0.000168 |
| 20 | ElUebersicht | dunkel | 0.978912 | 0.000 | Glyphen-Raster (R1) | — / -0.000214 |
| 20 | ElUebersicht | hell | 0.979965 | 0.000 | Glyphen-Raster (R1) | — / -0.000137 |
| 21 | ElRaum | dunkel | 0.971664 | 0.308 | Glyphen-Raster (R1) | — / -0.000427 |
| 21 | ElRaum | hell | 0.973495 | 0.305 | Glyphen-Raster (R1) | — / -0.000351 |
| 22 | ElVerteiler | dunkel | 0.965851 | 0.415 | Lage-Versatz (R17) | -0.026550 / -0.000092 |
| 22 | ElVerteiler | hell | 0.968353 | 0.418 | Lage-Versatz (R17) | -0.022278 / -0.000275 |
| 23 | ElRauchmelder | dunkel | 0.974686 | 0.454 | Lage-Versatz (R17) | — / -0.000275 |
| 23 | ElRauchmelder | hell | 0.975586 | 0.462 | Lage-Versatz (R17) | — / -0.000275 |
| 24 | SanUebersicht | dunkel | 0.972580 | 0.097 | Glyphen-Raster (R1) | — / -0.000244 |
| 24 | SanUebersicht | hell | 0.973480 | 0.097 | Glyphen-Raster (R1) | — / -0.000153 |
| 25 | SanGefaelle | dunkel | 0.972244 | 0.392 | Glyphen-Raster (R1) | -0.000458 / -0.000427 |
| 25 | SanGefaelle | hell | 0.973984 | 0.410 | Lage-Versatz (R17) | +0.000259 / -0.000214 |
| 26 | SanDruck | dunkel | 0.975403 | 0.251 | Glyphen-Raster (R1) | — / -0.000320 |
| 26 | SanDruck | hell | 0.976364 | 0.246 | Glyphen-Raster (R1) | — / -0.000290 |
| 27 | SanBad | dunkel | 0.962921 | 0.454 | Lage-Versatz (R17) | -0.001068 / -0.000519 |
| 27 | SanBad | hell | 0.965424 | 0.467 | Lage-Versatz (R17) | -0.000732 / -0.000366 |
| 28 | EsUebersicht | dunkel | 0.975738 | 0.050 | Glyphen-Raster (R1) | +0.000885 / -0.000168 |
| 28 | EsUebersicht | hell | 0.976639 | 0.051 | Glyphen-Raster (R1) | +0.000900 / -0.000153 |
| 29 | EsEbenheit | dunkel | 0.983658 | 0.001 | Glyphen-Raster (R1) | — / -0.000275 |
| 29 | EsEbenheit | hell | 0.984726 | 0.000 | Glyphen-Raster (R1) | — / -0.000168 |
| 30 | EsBelegreife | dunkel | 0.969665 | 0.300 | Glyphen-Raster (R1) | — / +0.000107 |
| 30 | EsBelegreife | hell | 0.970856 | 0.294 | Glyphen-Raster (R1) | — / -0.000168 |
| 31 | FlUebersicht | dunkel | 0.976379 | 0.129 | Glyphen-Raster (R1) | — / -0.000214 |
| 31 | FlUebersicht | hell | 0.977432 | 0.126 | Glyphen-Raster (R1) | — / -0.000168 |
| 32 | FlBad | dunkel | 0.966370 | 0.473 | Lage-Versatz (R17) | — / -0.000275 |
| 32 | FlBad | hell | 0.967590 | 0.483 | Lage-Versatz (R17) | — / -0.000244 |
| 33 | MaUebersicht | dunkel | 0.984207 | 0.000 | Glyphen-Raster (R1) | — / -0.000168 |
| 33 | MaUebersicht | hell | 0.985496 | 0.000 | Glyphen-Raster (R1) | — / -0.000023 |
| 34 | MaRaum | dunkel | 0.974579 | 0.322 | Glyphen-Raster (R1) | — / -0.000381 |
| 34 | MaRaum | hell | 0.976135 | 0.324 | Glyphen-Raster (R1) | — / -0.000305 |

---

# Abschluss (Schritt 9)

Die folgenden Abschnitte fassen die Schritte 0–8 oben zusammen und verweisen auf sie; sie ersetzen sie nicht. Alle Zahlen sind am Stand dieses Schritts mit den genannten Befehlen beobachtet (Stand: Arbeitsbaum auf `feat/m7-ios`, HEAD `608e6d3`, Änderungen nicht committet).

## Baseline (Zusammenfassung Schritt 0/1)

- Details: „Baseline (Schritt 0)“ und „Schritt 1 — Sidecar und Baseline“ oben. Kerndaten: `xcodebuild test` auf HEAD → 530 Tests, 462 bestanden, 68 fehlgeschlagen (alle 68 sind `ScreenSnapshotTests`); Skript-Prüfungen (`gen-tokens --check`, `gen-icons --check`, `lint-hex`, `design-freeze --check`) Exit 0.
- Baseline-Dateien (P10): `build/m7/ios/baseline/summary.json` und `unstable.json`, git-ignoriert, nicht committet. Zweiter Lauf: `changed` leer, die Messung ist deterministisch.
- Statische Testzahl an HEAD `608e6d3` (`git grep -E '^[[:space:]]*func test'`): 492 Unit-/Snapshot-Methoden und 38 UI-Methoden; das deckt sich mit den 492 bzw. 38 aus dem `xcodebuild`-Lauf und macht die Zählung unten vergleichbar.

## Herkunft

- Der Stand ist ein **Port** von Commit `fe905f7` (Branch `feat/m7`), kein Neuschreiben (P1): je Schritt `git checkout fe905f7 -- <explizite Pfadliste>`, Tests zuerst (P2). `docs/m7-offene-punkte.md` aus `feat/m7` wurde **nicht** portiert (P9); sie diente in Schritt 0 nur als Vergleichsquelle.
- Mitgebracht (Schritte 1–7b, Pfade siehe dort): `ReferenceSnapshot` + Sidecar-Test, `DebugScreens`, `ScreenCopy`, 15 Komponenten + 16 Screens (Schritt 3), `LinkCoverageUITests`, `PlaceholderUITests`, `AccessibilityAuditUITests`, `TouchTargetUITests`, `LargeTextRenderTests`, `LargeTextUITests`, Typografie-Einheiten.
- **Änderungen nach dem Port** (nicht identisch mit `fe905f7`), belegt durch `git diff --stat fe905f7 -- ios` (Arbeitsbaum gegen `fe905f7`): **28 Dateien, 423 Einfügungen, 93 Löschungen**. Nach Bereich:
  - Typografie / Large Text (Schritt 7a/7b): `Typography.swift` (+102), `TypographyTests.swift` (+124, fünf neue Tests) und die Aufrufstellen mit festen Höhen bzw. Zeilenboxen: `LoginScreen`, `HomeScreen`, `RohbauMasseScreen`, `CodeTile`, `IconTile`, `SettingsRow`, `ChecklistRow`, `MeasureChip`, `EsEbenheitScreen`, `ProfileScreen`.
  - Audit-Befunde an der Ursache (Schritt 5): `DocumentPreview`, `PhotoTile`, `SegmentedProgress` (Inhalt `.accessibilityHidden`, Element als `Color.clear`-Overlay mit `.isImage`).
  - Vergleich bei XL (Schritt 7c): `ComparisonTable` (`MinimumHitShape` statt `padding(±14)`), `ComponentMetricsListRowTests`.
  - §2.4-Korrekturen (Schritt 3): `SectionHeader` (optionaler `identifier` am Titeltext), `TileGrid` (`Tile.identifier` entfernt) samt vier Aufrufstellen (`GalleryPage`, `FlBadScreen`, `ComponentMetricsTileTests`, `ComponentMetricsLaterTradesTests`), `SanGefaelleScreen` (leere Label-Teile gefiltert).
  - UI-Tests: `AccessibilityAuditUITests` (+46), `TouchTargetUITests` (+44), `LargeTextUITests` (+47), `LinkCoverageUITests` (1 Zeile: `home.schedule`-Lookup).
- Zur Einordnung: `git diff --stat fe905f7 HEAD -- ios` (HEAD ohne die uncommitteten Port-Änderungen) zeigt 50 Dateien, 360 Einfügungen, 1824 Löschungen, d. h. den gesamten noch nicht auf HEAD liegenden Port aus Sicht von `fe905f7`.
- `scripts/`: nur `link-coverage.json` (`platforms.ios` `false` → `true`) und `scripts/test/links.test.mjs` (drei neue Guards, ein erweiterter `node:fs`-Import); `git diff HEAD --stat -- scripts` → 2 Dateien, 46 Einfügungen, 2 Löschungen (I13, siehe „Invarianten“).

## Entscheidungen

**Basisplan** `docs/plans/10-11-26-m7-v3-ios.md`: D1, D3–D18, D20, D21 gelten wie dort geschrieben. Verwendet wurden insbesondere D1 (frische Baseline), D3 (Diff-Report je Plattform, 68 Zeilen), D4/D5 (Katalog `DebugScreens`, Link-Tests ab Quellscreen), D6 (Guard für Link-Manifest), D7/D8 (Gefälle-Karte und Raster als je ein Element), D11 (SignaturePad „leer“/„unterschrieben“), D12 (zusammengeführte Labels aus sichtbaren Texten), D14 (`performAccessibilityAudit`), D15 (XL), D16 (Large-Text-Fixes ändern den Standard nicht), D17/D18 (Snapshot-Hebel als Spikes mit festen Annahmeregeln; Render-Pfad-Spike 8b), D20 (Platzhalter-Identifier `ph.<Screen>.<slug>`), D21 (dieses Dokument + README).

**Entscheidungen dieser Umsetzung (P1–P10):**

| # | Entscheidung |
|---|---|
| P1 | Port, nicht neu schreiben (`git checkout fe905f7 -- <explizite Pfadliste>`). |
| P2 | Rot zuerst: Tests vor Produktion. |
| P3 | Die vier §2.4-Abweichungen werden beim Port korrigiert; `SearchField` bleibt durch `--baseline` abgesichert (Schritt 3: Gate ohne geänderte Zeile). |
| P4 | Large Text: `InterTextModifier` liest `@Environment(\.dynamicTypeSize)`; bei `.large` entsteht dieselbe Kette wie vorher (Schritt 7a). |
| P5 | Prototyp-Bilder behalten einen festen Textmaßstab (`DocumentPreview`, `role="img"`, `IosDokViewer.dc.html:35`). |
| P6 | Beabsichtigte Kürzung ist kein Fehler (`IntendedEllipsis`); AC3 gilt nur bei leerer `AuditAllowlist`. |
| P7 | Die 10 Audit-Findings aus `feat/m7` werden neu hergeleitet, nicht geerbt (`AuditAllowlist` startet leer). |
| P8 | Audit-Hygiene: Appearance wird zurückgesetzt, kein Start in `setUp`, `.hitRegion` auf beiden Durchläufen. |
| P9 | Doku: `docs/m7-ios-offene-punkte.md`; `feat/m7:docs/m7-offene-punkte.md` wird nicht portiert. |
| P10 | Baseline-Dateien liegen in `build/m7/ios/baseline/` (git-ignoriert, nicht committet); Zahlen stehen in diesem Dokument. |

**Beobachtete Abweichung vom Plantext (Schritt 7a):** Die skalierte Schriftgröße wird auf ganze Punkte gerundet (`scaledValue(...).rounded()`), weil `Font.custom`/`scaledFont` auf iPhone 15 gerundet rendern; dokumentiert in „Schritt 7a“.

## Bekannte Lücken

| Risiko | Ergebnis | Status |
|---|---|---|
| R1 — iOS erreicht 0.995 nicht (Glyphen-Raster) | Eingetreten: **0 von 68** Snapshots grün; bester Wert 0.985497 (MaUebersicht hell), schlechtester 0.962921 (SanBad dunkel). Spikes 8a-1, 8a-2, 8b nach D17 verworfen (Schritt 8). | **AC1 unerfüllt** |
| R4 — Audit-Kontrastbefunde an Design-Token-Paaren | Ein Fall: `okInk`/`ok` auf ElRauchmelder dunkel, 3,26 : 1 (`IosElRauchmelder.dc.html:80`); als `AuditAllowlist`-Eintrag gemeldet, nicht geändert (CLAUDE.md). | **AC3 offen** (siehe „Audit“) |
| R5 — `XCUIDevice.shared.appearance` nicht verfügbar | Nicht eingetreten: `appearance = .dark` funktioniert; `-AppleInterfaceStyle Dark` bleibt als Code-Fallback (Schritt 5). | erledigt |
| R13 — Koordinaten-Tap auf zusammengeführtes Kachelraster kann Kachel 3 verfehlen | Bekannte Schwäche: ein Tap ohne Wirkung kann nicht beweisen, dass er getroffen hat. `tiles.tile.tile-3` wird in `LaterTradesUITests` als Nicht-Existenz geprüft (Schritt 4). | **offen, dokumentiert** |
| R16 — Manueller Accessibility-Inspector-Lauf nicht durch einen Agenten möglich | Nicht durchgeführt. | **offen bis ein Mensch ihn ausführt** (siehe „Operator-Checkliste“) |
| R20 — `IntendedEllipsis` versteckt ein echtes Abschneiden | `IntendedEllipsis.entries` ist leer; der Guard in `links.test.mjs` verlangt je Eintrag eine Prototypzeile mit `ellipsis`/`nowrap`. | kein Risiko eingetreten |

Weitere offene Punkte (Details in „Schritt 7b — Offen“): `.textClipped` auf einzeiligen `TextField`s (Login „E-Mail“, im Vollauf nicht mehr gemeldet; Dokumente „Dokumente durchsuchen“ als Allowlist-Eintrag), `.dynamicType`/`.textClipped` mit Element `nil` auf Gewerk, Profil, EsEbenheit sowie `.dynamicType` auf RohbauMasse F1–F7 (Ursache nicht belegt; im Vollauf der Audit-Prüfung bei `.all` nicht mehr gemeldet, siehe „Audit bei `.all`“). `FlUebersicht_light` (Verschiebung in der Gate-Reihenfolge) ist behoben, siehe „FlUebersicht_light: reihenfolgeabhängige Verschiebung (behoben)“.

## Messwerte der 68 iOS-Snapshots

- Quelle: `scripts/diff-report --all --platform ios --baseline build/m7/ios/baseline/summary.json --unstable build/m7/ios/baseline/unstable.json` (in diesem Schritt erneut ausgeführt): Ausgabe `ios: 0 grün, 68 rot, 0 fehlt`, keine `geändert:`-Zeile; `reports/summary-ios.md` hat 68 Zeilen, alle ✗, `reports/summary-ios.json` → `changed` leer. Summe aller `value`: 66,400551 (wie Startwert in Schritt 8).
- Die vollständige 68-Zeilen-Tabelle mit **value**, **Lage-Anteil**, **Ursachenklasse** und **Hebel-Wirkung** steht oben unter „Zeilenbelege (Baseline = Endstand)“ (Schritt 8); hier nicht dupliziert. Abgleich: alle 68 `value` und `Lage-Anteil` dieser Tabelle stimmen mit `reports/summary-ios.json` überein (Abweichung ≤ 1e-6 bzw. ≤ 1e-3; 0 Abweichungen).
- Ursachenklassen (aus dem Lage-Anteil abgeleitet, siehe Erläuterung dort): `Glyphen-Raster (R1)` **58** Zeilen, `Lage-Versatz (R17)` **10** Zeilen (AbnPruefung hell, ElVerteiler dunkel/hell, ElRauchmelder dunkel/hell, SanGefaelle hell, SanBad dunkel/hell, FlBad dunkel/hell). Die Klassen `Layout <Komponente>` und `Umbruch` kommen in der Tabelle **nicht** vor; folglich gibt es keine ✗-Zeile mit Ursache „Layout“, die hier als „nicht behebbar“ zu begründen wäre. Layout-Beobachtungen aus der Sichtprüfung stehen unter „Schritt 7b/7c“.
- Verteilung der `value` (Soll ≥ 0,995): 0 Zeilen ≥ 0,995; 17 Zeilen ≥ 0,98; 8 Zeilen < 0,97; Spanne 0.962921 … 0.985497.
- Hebel (D17/D18, alle verworfen, siehe „Schritt 8“): 8a-1 (`wholePoint` im `.multi`-Pfad) ohne Wirkung; 8a-2 (`.block`/`.greedy`) Summe −0,056259, aber 6 Zeilen um mehr als 0,0005 schlechter; 8b (`drawHierarchyInKeyWindow`) Summe −0,264114, nur durch Login (−0,127060 / −0,124985); keine Zeile wird grün. Keine Prototyp-Zeile zu zitieren, da kein Maß geändert wurde (I5).
- Ursachenvermutung: Glyphen-Raster-Unterschiede zwischen Chrome (Referenz) und CoreText (SwiftUI) lassen sich mit den erlaubten Mitteln nicht beseitigen; Toleranzen bleiben unverändert (CLAUDE.md, I1).

## Large-Text-Sichtprüfung (34 Zeilen)

Siehe „Schritt 7c — Sichtprüfung bei XL“ oben: 34 PNGs (`build/snapshots/ios-large/<Name>_xl.png`) einzeln geöffnet, Tabelle mit je Screen Überlapp / Kürzung / Beobachtung; Ergebnis: in keinem Screen ein Überlapp und keine Kürzung. Zusatz xxxL (Stichprobe AbnGewerke, Home, Zeitplan, SanBad) ebenda. Ein von `LargeTextUITests` gemeldeter Befund (Vergleich, 4 pt) wurde an der Ursache behoben (`ComparisonTable.HeaderCell`, `MinimumHitShape`).

## Audit

- `AccessibilityAuditUITests.auditTypes` = `.all` (inklusive `.dynamicType`, `.textClipped`, `.hitRegion`), 34 Screens × hell/dunkel × oben/gescrollt; Hell/Dunkel über `XCUIDevice.shared.appearance`, am Ende auf `.light` zurückgesetzt (P8; R5-Fallback nicht gebraucht). `testScheduleChartIsOneImageElement` pinnt den Zeitplan als ein Bild-Element (D9).
- **`AuditAllowlist.entries` (Endstand: 2 Einträge)**, beide mit Begründung im Quelltext:
  1. `(Dokumente, .textClipped, „Dokumente durchsuchen“)`, hell und dunkel (zwei Einträge im Quelltext) — Prototypgeometrie 44 pt / 16 pt; ohne `.frame(maxHeight: .infinity)` am `SearchField` bleibt das Finding (gemessen); `TextField(axis: .vertical)` beseitigt das Finding, verschiebt aber den Text um 1,3 pt und ändert das StatusInput-Golden.
  2. `(ElRauchmelder, .contrast, „step.ok“, dunkel, orNilElement)` — dunkel, `okInk` auf `ok` 3,26 : 1, generiertes Token-Paar (R4).
- Der Test `testAuditAllScreens` scheitert, wenn ein Eintrag nicht getroffen wird (Veraltet-Check); so kann die Allowlist nicht unbemerkt wachsen. Der Veraltet-Check läuft nur ohne `AUDIT_SCREENS`.
- **Ungeteilter Lauf (Review-Zyklus 1, Punkt 3):** `testAuditAllScreens` ohne `TEST_RUNNER_AUDIT_SCREENS`, 34 Screens × hell/dunkel × oben/gescrollt, `Executed 1 test, with 0 failures` in 1240 s, `TEST SUCCEEDED`, Veraltet-Check bestanden (alle drei Einträge getroffen). Ein erster ungeteilter Lauf zeigte, dass der Dokumente-Eintrag auch im dunklen Modus gebraucht wird (die Scheiben-Läufe davor hatten das nicht gezeigt); er wurde um einen dunklen Eintrag ergänzt. `TEST_RUNNER_AUDIT_SCREENS=Nope` → 1 Fehler („selects no screen or an unknown one“), `LARGE_TEXT_SCREENS` nutzt dieselbe Funktion `selectedScreens(variable:)`.
- `IntendedEllipsis.entries` (`LargeTextUITests.swift`): leer. Guard `every IntendedEllipsis entry cites an existing prototype line` in `scripts/test/links.test.mjs` prüft Syntax, Prototypdatei und `ellipsis`/`nowrap` in der zitierten Zeile (R20, P6).
- Behoben an der Ursache (Schritt 5): DokViewer-Vorschau (`DocumentPreview`), „Foto oben“ (`PhotoTile`), Fortschrittsbalken (`SegmentedProgress`). Schritt 5 senkte die 10 neu hergeleiteten Findings auf einen Allowlist-Eintrag (`step.ok`); der Vollauf bei `.all` (Schritt 7c) ergänzte den Dokumente-Eintrag.
- **AC3 ist nicht erfüllt** (zwei Einträge). Entscheidung für den Operator: Token-Paar `ok`/`okInk` (dunkel) in `design/tokens.json` ändern bzw. die Suchfeld-Goldens neu freigeben — beides außerhalb dieses Auftrags.

## Touch-Ziele

- `TouchTargetUITests.testAllScreensMeetMinimumTargets`: läuft alle 34 Screens hell/dunkel, `.hitRegion`-Audit auf **beiden** Durchläufen (oben und gescrollt), Findings werden gesammelt.
- `HitShapeAllowlist.entries` ist **leer**. Die Prüfung vergleicht mit `44 − 0,01 pt` (Float-Rauschen); der Frame eines Elements enthält die `MinimumHitShape`-Vergrößerung (Profil-Switch: gezeichnet 51 × 31, gemessen 51 × 44). Der Guard `every HitShapeAllowlist test exists` in `links.test.mjs` akzeptiert ein leeres Literal.
- **Lücken mit Maßen:** Mit leerer Allowlist meldeten genau zwei Elemente weniger als 44 pt, beide nur ein Rundungsrest (jetzt von der Toleranz abgedeckt, nicht mehr von einer Allowlist): Link „Alle 6“ (`ph.AbnElemente.more.1`) 44.00000000000006 × 43.99999999999994 pt, Switch „Wochenrückblick“ (Profil) 51 × 43.99999999999994 pt. Kein Ziel unter 44 pt, das sich nicht vergrößern ließe; das `.hitRegion`-Audit meldete nichts.
- Weitere Hit-Shape-Unit-Tests (nicht in der Allowlist, bleiben bestehen): `testFilterChipHitShapeIs44`, `testBannerDocumentButtonIs36TallWithA44HitArea`, `testComparisonColumnHeaderHitArea44`.

## FlUebersicht_light: reihenfolgeabhängige Verschiebung (behoben)

**Befund.** In der Gate-Reihenfolge (`GallerySnapshotTests` unmittelbar vor `ScreenSnapshotTests`) rendert `FlUebersicht_light` die vier Status-Badges der RÄUME-Karte 9 px (3 pt) weiter rechts (0,97743225 → 0,97448730); `diff-report` endete mit Exit 4. Kein Rauschen: in zwei Läufen in dieser Reihenfolge identisch (Review-Zyklus 1, Läufe `gate1`/`gate2`: gleiche `actualSha256`, gleicher Wert). Die frühere Aussage „FlUebersicht nutzt keine geänderte Komponente“ war falsch: der Screen nutzt `GreedyLabel`, und alle Texte laufen durch `interText`. Ein Einzellauf von `testFlUebersichtLight` zeigt den Effekt nicht und ist kein Gate-Ersatz (I3 / Schritt 1.4).

**Eingrenzung (iPhone 15, Gate-Reihenfolge `GallerySnapshotTests` + `ScreenSnapshotTests`, je Versuch `build-for-testing` + Lauf + `FlUebersicht_light.result.json`):**
- Die sieben Screens `Home`, `InspectionStep`, `Inspections`, `Login`, `Notifications`, `Profile`, `Schedule` auf `608e6d3` zurückgesetzt: Basiswert 0,97743225. Nur `Home`, `Login`, `Profile` zurückgesetzt: Basiswert. Nur `Home` zurückgesetzt: weiter 0,97448730. Nur `Login` zurückgesetzt: Basiswert.
- In `LoginScreen` nur `.scaledFrame(height: introHeight, …)` durch `.frame(height: introHeight, …)` ersetzt: Basiswert. Auslöser ist also der `scaledFrame` des Login-Intro-Texts, genauer die Form von `ScaledFrameModifier.body` (`@ViewBuilder` mit `if`/`else` über zwei verschiedene Ansichtstypen). Der Mechanismus, über den ein früher gerenderter Screen die Badge-Position eines späteren verschiebt, ist **nicht** geklärt; belegt ist nur der Zusammenhang (Rücknahme und Umbau beseitigen ihn, Wiederherstellung bringt ihn zurück).

**Ursache entfernt.** `ScaledFrameModifier.body` besteht jetzt aus einer einzigen Modifier-Kette ohne Verzweigung: `frame(minHeight: height, maxHeight: grows ? nil : height, alignment:)` und `fixedSize(horizontal: false, vertical: grows)`. Bei `.large` und darunter ist das dieselbe Höhe `height` wie zuvor, darüber ein Mindestmaß mit `fixedSize`; `testScaledFrameDoesNotCompressWrappedTextAtXL` bleibt grün. Der Kommentar am Modifier nennt den Grund.

**Gate danach** (`GallerySnapshotTests` + `ScreenSnapshotTests` + `TypographyTests` + `LargeTextRenderTests` + `ComponentMetricsListRowTests` + `ComponentMetricsTileTests`, 105 Tests): 68 erwartete rote Screen-Snapshots, 0 unerwartet, alle übrigen grün; `scripts/diff-report --all --platform ios --baseline build/m7/ios/baseline/summary.json --unstable build/m7/ios/baseline/unstable.json` → `ios: 0 grün, 68 rot, 0 fehlt`, **Exit 0**, keine geänderte Zeile. Ein Eintrag in `unstable.json` wäre kein Ausweg gewesen: `diff-report` nimmt dort gelistete Zeilen nur vom Hash-Vergleich aus, Wertänderungen meldet es weiter (`diff-report.test.mjs`, „--unstable skips the hash … but still reports their value changes“).


## Nachbesserung nach Review (Zyklus 1)

- **FlUebersicht_light (Punkt 1):** Ursache eingegrenzt und entfernt (`ScaledFrameModifier` ohne `if`/`else`), Gate in der Reihenfolge Galerie → Screens Exit 0; Details im eigenen Abschnitt. Die Aussage „FlUebersicht nutzt keine geänderte Komponente“ ist dort widerrufen.
- **Dokumente-Eintrag (Punkt 8):** `SearchField` ohne `.frame(maxHeight: .infinity)` versucht: Finding bleibt in beiden Modi; Modifier wiederhergestellt; der ungeteilte Lauf zeigte den Befund auch dunkel, daher zwei Einträge (hell/dunkel).
- **Ungeteilter Audit-Lauf (Punkt 3):** grün, siehe „Audit“.
- `.scratch/*.log` aus dem Index und dem Arbeitsbaum entfernt; Logs liegen im Scratch-Ordner des Laufs außerhalb des Repos.
- `AuditAllowlist`: Einträge tragen `dark`; `alsoUnnamed` ist durch `orNilElement` ersetzt (nur `issue.element == nil`, nur auf dem Screen, Typ und Modus des Eintrags, Log `AUDIT-ALLOWED … path=`). Leere Namen treffen nie.
- `selectedScreens(variable:)` (`AccessibilityAuditUITests`, auch von `LargeTextUITests` genutzt): eine Variable `TEST_RUNNER_AUDIT_SCREENS` / `TEST_RUNNER_LARGE_TEXT_SCREENS`, die nichts oder einen unbekannten Screen wählt, ist ein Fehlschlag statt eines leeren Durchlaufs.
- Touch-Walk: Toleranz `44 − 0,01 pt`, `HitShapeAllowlist.entries` leer (siehe „Touch-Ziele“).
- `SegmentedProgress`: `.accessibilityHidden(true)` statt `.isImage` (der Prototyp-Balken hat weder Rolle noch aria-Label; der Screen-Text nennt „7 von 12 Elementen geprüft“ bereits).
- `testInterTextSingleAtXLMatchesScaledBox`: misst jetzt den reinen `Text` bei XL und vergleicht ihn mit `naturalHeight` der gerundeten Größe (Toleranz 1/3 pt, Pixelraster), danach `max(box, Text)`.
- `RohbauMasseScreen.tileColumns`: Prototypbezug `IosRohbauMasse.dc.html:58`. `LaterTradesUITests`: `valueBefore` muss nicht leer sein.
- Nicht geändert: (a) Dokumente-`.textClipped`-Eintrag: bleibt (zwei Einträge), nach gemessenem Versuch ohne `.frame(maxHeight: .infinity)` (Finding bleibt); die Begründung im Eintrag nennt weiter das Golden-Gate als Plan-Regel und bleibt eine Entscheidung für den Operator. (b) `testComparisonColumnHeaderHitArea44` prüft die statische `HeaderCell.hitShape`, die die Zelle über `.contentShape(Self.hitShape)` nutzt; ein Unit-Test kann das Entfernen des Modifiers nicht erkennen, nur die UI-Prüfung (`.hitRegion` im Touch-Walk). (c) `UIContentSizeCategory.init(_: DynamicTypeSize)` in `Typography.swift` bleibt (die SDK-Variante ist für `DynamicTypeSize` nicht belegt).

## Link-Manifest

Beobachtet mit `python3 -I` über `scripts/link-coverage.json`:

- `platforms`: `ios` `true`, `android` `true`.
- **Links: 100 Einträge, 100 mit iOS-Test (100/100).**
- **Platzhalter: 101 Einträge, davon 96 mit iOS-Testverweis**; 82 davon verweisen auf `PlaceholderUITests` (31 Methoden), 14 auf bestehende Flow-Tests (`AppNavigationUITests`, `EarlyTradesUITests`, `InspectionFlowUITests`, `LaterTradesUITests`, `TradeFlowUITests`). 5 Einträge haben kein iOS-Feld (`platforms: ["android"]`: `ph.Home.alle`, `ph.Home.dokument`, `ph.Profil.row.<n>`, `ph.DokViewer.change`, `ph.AbnElemente.chip.<n>`; Guard „the real manifest covers every pair and placeholder“ grün).
- Insgesamt 79 verschiedene iOS-Testverweise. `LinkCoverageUITests`: 18 Methoden; Platzhalter bleiben sichtbar und ohne Aktion (I4).
- Guard: `cd scripts && node --test test/links.test.mjs` (11 Tests; `ios: true` aktiviert die iOS-Prüfungen).

## Testzahlen (beobachtet)

Statische Zählung der Testmethoden (`grep -E '^[[:space:]]*func test'`), vergleichbar mit der Baseline (HEAD: 492 / 38, deckt sich mit dem `xcodebuild`-Lauf):

| Gruppe | Baseline (HEAD) | jetzt |
|---|---|---|
| `BaupilotTests` (Unit + Snapshots) | 492 | 516 |
| – davon `ScreenSnapshotTests` | 68 | 68 |
| `BaupilotUITests` | 38 | 93 |
| – davon `LinkCoverageUITests` / `PlaceholderUITests` | 0 / 0 | 18 / 31 |
| – davon `AccessibilityAuditUITests` / `TouchTargetUITests` / `LargeTextUITests` | 0 / 0 / 0 | 2 / 1 / 1 |
| `cd scripts && node --test test/` | 141 pass | **144 pass, 0 fail, 0 cancelled, 0 skipped** (in diesem Schritt ausgeführt) |

Die letzten vollständigen `xcodebuild`-Zahlen je Schritt stehen oben in den Schritt-Abschnitten; ein Gesamtlauf am Endstand gehört zur Verifikation (siehe dort).

## Invarianten (Plan §8) — beobachtete Prüfung

| # | Invariante | Prüfung / Beleg |
|---|---|---|
| I1 | Toleranzen 0.995 / 0.98 unverändert | `ReferenceSnapshot.swift:30–31` `precision = 0.995`, `perceptualPrecision = 0.98` |
| I2 | Referenzen, `design/`, `fixtures/`, `scripts/icons.json` unverändert | `git status --short design fixtures scripts/icons.json` leer; `git diff HEAD --stat` für diese Pfade leer |
| I3 | Schritte 2–7 ändern kein gerendertes Pixel | Gate nach jedem Schritt: `diff-report … --baseline … --unstable …` Exit 0, `changed` leer (in der Gate-Reihenfolge `GallerySnapshotTests` + `ScreenSnapshotTests` nach Review-Zyklus 1 erfüllt, Exit 0; vorher durch `FlUebersicht_light` verletzt, siehe „FlUebersicht_light: reihenfolgeabhängige Verschiebung (behoben)“; der Einzellauf ist kein Ersatz) |
| I4 | Platzhalter bleiben sichtbar und ohne Aktion | `PlaceholderUITests` (31) + `testPlaceholderLinkHasNoAction`; Manifest 96 iOS-Verweise |
| I5 | Keine Maßänderung ohne Prototypzitat | Schritt 8: kein Hebel übernommen; Schritt 7: Zitate in den Tabellen (z. B. `IosDokViewer.dc.html:35`, `IosRohbauMasse:58`) |
| I6 | Farben nur über Tokens | `scripts/lint-hex` Exit 0 (Schritt 0; Produktionsfarben seither nicht angefasst) |
| I7 | DEBUG-Hooks fehlen im Release | `DebugScreens.swift` beginnt mit `#if DEBUG`; `RootView.swift` hat die Hooks in `#if DEBUG` (Zeilen 8, 26); Release-Binary: 0 Treffer für `BaupilotScreen` (Schritt 2) |
| I8 | Kein sichtbarer Text hinzugefügt; nur D11 („leer“ / „unterschrieben“) | `git diff 608e6d3 --stat -- ios/Baupilot/Screens/ScreenCopy.swift` → 1 Datei, 6 Einfügungen (nur `Signature`) |
| I9 | Testurteil hängt nie vom Sidecar-Parsing ab | Sidecar wird nur geschrieben (`ReferenceSnapshotSidecarTests`), das Urteil kommt aus `assertSnapshot`; `diff-report` liest den Sidecar nur zum Berichten |
| I10 | Kein grüner Snapshot wird rot | Baseline hatte 0 grüne Zeilen; Endstand 0 grün, 68 rot, kein `passed true → false` |
| I11 | Alle bestehenden M2–M6-iOS-UI-Tests laufen weiter | Läufe je Schritt (Schritt 3: 40 UI-Tests, 0 Fehler; Schritt 4: 49 neue); Gesamtlauf am Endstand: Verifikation |
| I12 | Nichts unter `android/` ändert sich | `git status --short android` leer |
| I13 | Gemeinsame Skripte nur additiv | `git diff HEAD --stat -- scripts` → `link-coverage.json` (1 Zeile `ios: false → true`) und `links.test.mjs` (+45 / −1: drei neue Guards, ein `node:fs`-Import erweitert); `link-coverage.json` (+1 / −1); `diff-report`, `gen-*`, `lint-hex` unverändert |
| I14 | Plattformübergreifende Ids/Strings = Android | `ph.<Screen>.<slug>` (D20) und D11-Wörter wie `ScreenCopy.kt:283–288`; Manifest-Guard prüft Android-Elemente gegen die Quellen |

## Verifikation

Gesamtlauf am Endstand (Schritt 13, `p13t1`), beobachtet; Logs je Abschnitt unter `.scratch/p13-*.log` (Scratch-Ordner des Laufs). Der Arbeitsbaum enthält Schritte 0–9 als staged/geänderte Dateien, **nicht als Commits** (`git log` endet bei `608e6d3`); Vergleiche „gegen HEAD“ sind daher Vergleiche gegen `608e6d3` im Arbeitsbaum.

### Befehle und beobachtete Ergebnisse

| Befehl | Ergebnis |
|---|---|
| `cd scripts && node --test test/` | Exit 0: **144 pass, 0 fail, 0 cancelled, 0 skipped** |
| `scripts/gen-tokens --check` / `scripts/gen-icons --check` | je Exit 0 |
| `scripts/lint-hex` | Exit 0, „sauber“ |
| `scripts/design-freeze --check` | Exit 0, „design/ matches scripts/design-v1.sha256 (276 files)“ |
| `xcodegen generate` | Projekt erzeugt (`p13-xcodegen.log`) |
| `xcodebuild test` (Unit, `BaupilotTests`) | 516 Tests, **448 bestanden, 68 fehlgeschlagen**; alle 68 Fehlschläge sind `ScreenSnapshotTests` (0 in anderen Klassen). Der Gesamtlauf brach nach dem Unit-Teil beim UI-Start ab (Zeitgrenze, `p13-xctest.log`), deshalb wurde die UI-Suite in Scheiben gelaufen (nächste Zeilen). |
| `xcodebuild test` UI, je Klasse/Scheibe | `AppNavigationUITests` 8, `EarlyTradesUITests` 5, `InspectionFlowUITests` 9, `LaterTradesUITests` 7, `TradeFlowUITests` 11, `LinkCoverageUITests` 18, `PlaceholderUITests` 31 (4 Scheiben: 8/8/8/7): **alle 0 Fehler**. `AccessibilityAuditUITests`: `testAuditAllScreens` **ungeteilt** (ohne `TEST_RUNNER_AUDIT_SCREENS`, mit Veraltet-Check; 34 Screens × hell/dunkel × oben/gescrollt, 1240 s) grün, `testScheduleChartIsOneImageElement` grün. `LargeTextUITests` in 6 Scheiben (6/6/6/6/6/4 Screens) grün. `TouchTargetUITests` (`testAllScreensMeetMinimumTargets`): erster Lauf durch Simulator-Absturz („Mach error -308, server died“, kein Testurteil) abgebrochen, Wiederholung grün (560 s, ein einzelner Test, nicht weiter teilbar); nach den Review-Korrekturen (Toleranz `44 − 0,01 pt`, leere `HitShapeAllowlist`) erneut zusammen mit `LaterTradesUITests`: 8 Tests, 0 Fehler (664 s). |
| `xcodebuild build -configuration Release` (generic iOS Simulator) | `BUILD SUCCEEDED`, Exit 0 |
| `strings …/Release-iphonesimulator/Baupilot.app/Baupilot \| grep -c BaupilotScreen` | **0** |
| `scripts/diff-report --all --platform ios` | Exit 0: **0 grün, 68 rot, 0 fehlt** |
| `scripts/diff-report … --baseline build/m7/ios/baseline/summary.json --unstable build/m7/ios/baseline/unstable.json` | Exit 0 (0 grün, 68 rot, 0 fehlt) nach `ScreenSnapshotTests` allein und nach `GallerySnapshotTests` + `ScreenSnapshotTests` (Gate-Reihenfolge, nach der `ScaledFrameModifier`-Korrektur; vorher Exit 4 durch `FlUebersicht_light`) |
| `git status --porcelain -- design fixtures scripts/icons.json` (zusätzlich `android`) | leer |
| `git diff --stat 608e6d3 -- android` | leer |
| `git diff --stat fe905f7 HEAD -- ios` | 50 Dateien, +360/−1824: **nicht aussagekräftig**, weil der iOS-Arbeitsstand nicht committet ist (HEAD = `608e6d3`); `fe905f7` ist der kombinierte `feat/m7`-Stand, aus dem iOS portiert wurde. Der Herkunftsbeweis ist `git diff --stat fe905f7 -- ios` gegen den Arbeitsbaum (siehe „Herkunft“, 28 Dateien). Aussagekräftig ist `git diff 608e6d3 --shortstat -- ios`: 67 Dateien, +2203/−409 (die dokumentierten Schritte 0–9). |

Die Unit-Tests wurden im Gesamtlauf `p13-xctest.log` einmal komplett gelaufen; die UI-Klassen danach einzeln in Scheiben (je Aufruf ≤ ca. 5 Minuten, außer dem einzelnen `testAllScreensMeetMinimumTargets`).

### Invarianten I1–I14 (beobachtete Prüfung am Endstand)

- **I1** `ReferenceSnapshot.swift:30–31` `precision = 0.995`, `perceptualPrecision = 0.98`; `git diff 608e6d3` dieser Datei enthält keine Zeile an diesen Konstanten; `design-system.test.mjs` im Node-Lauf grün.
- **I2** `design-freeze --check` Exit 0; `git status --porcelain -- design fixtures scripts/icons.json` leer.
- **I3** Gate `diff-report --baseline … --unstable …` Exit 0, keine geänderte Zeile.
- **I7** Release-Binary: 0 Treffer für `BaupilotScreen`.
- **I8** `git diff 608e6d3 -- ios/Baupilot/Screens/ScreenCopy.swift`: 1 Datei, +6, ausschließlich `enum Signature` (`leer`, `unterschrieben`).
- **I9** `ReferenceSnapshotSidecarTests` im Unit-Lauf grün (Teil der 448); das Urteil kommt aus `assertSnapshot`.
- **I10** Baseline-Gate: 0 grün → 0 grün, kein `passed true → false`.
- **I11** Alle bestehenden M2–M6-UI-Klassen (`AppNavigation`, `EarlyTrades`, `InspectionFlow`, `LaterTrades`, `TradeFlow`) in diesem Lauf mit 0 Fehlern.
- **I12** `git diff --stat 608e6d3 -- android` leer.
- **I13** `git diff 608e6d3 --stat -- scripts`: `link-coverage.json` (1 Zeile, `ios: false → true`) und `test/links.test.mjs` (+46/−2, drei Guards); Android-Einträge unverändert.
- **I14** `ScreenCopy.Signature`-Wörter und `ph.<Screen>.<slug>`-Ids unverändert gegenüber dem Stand von Schritt 9 (dort gegen `ScreenCopy.kt:283–288` geprüft); in diesem Lauf nicht neu verglichen, nur der Manifest-Guard (`links.test.mjs`, im Node-Lauf grün).
- I4–I6: keine Änderung seit Schritt 9; I6 durch `lint-hex` (Exit 0) erneut bestätigt, I4 durch `PlaceholderUITests` (31/31) und `LinkCoverageUITests` (18/18).

### Abnahme

- **AC1 (68/68 Snapshots): nicht erfüllt, teilweise.** 0 von 68 grün, 68 rot, 0 fehlen (R1; Zeile für Zeile mit Begründung in der Tabelle „Messwerte der 68 iOS-Snapshots“ und in „Zeilenbelege“). Toleranzen und Referenzen wurden nicht angetastet.
- **AC2 (UI-Tests grün):** erfüllt: alle `BaupilotUITests`-Klassen in Scheiben grün (siehe Tabelle; 0 Fehler, `TouchTargetUITests` nach Simulator-Absturz im Wiederholungslauf).
- **AC3 (Audit ohne Fehler):** `performAccessibilityAudit(.all)` auf 34 Screens × 2 Modi grün, Touch-Ziele und XL-Prüfung grün; erfüllt nur mit der dokumentierten `AuditAllowlist` (zwei Einträge), also **nicht** mit leerer Allowlist; manueller Inspector-Lauf (R16) offen.
- **AC4 (Skripte grün):** erfüllt: `node --test test/` 144 pass, 0 fail; `gen-tokens`/`gen-icons --check`, `lint-hex`, `design-freeze --check` Exit 0.
- Zusätzlich beobachtet: Link-Manifest 100/100 Links, 96 iOS-Platzhalter-Verweise; Release ohne DEBUG-Hooks (0 Treffer); `android/` unverändert.

### Offen / Hinweise

- **R16** (Operator): manueller Accessibility-Inspector-Lauf, siehe „Operator-Checkliste“.
- Nichts wurde committet; keine Integrationskorrekturen waren nötig.

## Operator-Checkliste

Der manuelle Accessibility-Inspector-Lauf (R16) kann ein Agent nicht ausführen; er ist offen, bis ein Mensch ihn macht.

**Vorbereitung:** `xcodegen generate`, Debug-Build auf iPhone 15 (Simulator). Screens direkt öffnen mit `xcrun simctl launch booted <bundle-id> -BaupilotScreen <Name>` (Namen und Reihenfolge: `design/screens.md`, 34 Screens). Xcode → Open Developer Tool → Accessibility Inspector, Ziel Simulator.

**Pro Screen (34), hell und dunkel** (Dunkel: `xcrun simctl ui booted appearance dark`; danach wieder `light`):
- Audit starten (Inspector → Audit → Run Audit): keine neuen Findings über die zwei bekannten Allowlist-Fälle (Dokumente „Dokumente durchsuchen“ hell und dunkel, ElRauchmelder dunkel `step.ok`) hinaus.
- Fokusreihenfolge mit dem Pfeil-Navigator: von oben nach unten, Titel zuerst; keine Stopps auf reinen Dekorationen.
- Labels: jedes bedienbare Element hat ein Label aus dem Prototyp (`aria-label`); kein „Button“ ohne Namen, keine doppelten Stopps.
- Touch-Ziele: im Inspector Frame prüfen, ≥ 44 × 44 pt (bekannt: „Alle 6“ und Switch „Wochenrückblick“ liegen bei 44 bzw. 43.99999999999994 pt).

**Besondere Fokus-Stopps prüfen:**
- Gefälle-Karte (`SlopeResult`, SanGefaelle): **ein** Stopp, Label „Prozent, Urteil · Soll …“ (D7), kein Einzelstopp je Textzeile.
- Kachelraster (`TileGrid`, FlBad): **ein** Stopp mit `gridAria`, Wert = Hohlstellen-Kacheln (D8); Hinweis R13 (Kachel 3).
- Zeitplan-Diagramm (Zeitplan): **ein** Bild-Stopp (D9, durch `testScheduleChartIsOneImageElement` gepinnt).
- Unterschriftsfeld (AbnSignatur): Wert „leer“ bzw. „unterschrieben“ (D11).
- Vorschau Seite 1 von 2 (DokViewer) und „Foto oben“ (AbnPruefung): als Bild, ohne Kindtexte.
- Vergleichstabelle (Vergleich): keine Tabellensemantik erwartet (D13, A5).

**XL-Prüfung:** Einstellungen → Bedienungshilfen → Anzeige & Textgröße → Größerer Text auf „XL“ (bzw. im Simulator `xcrun simctl ui booted content_size extra-large`, danach `large`): alle 34 Screens ansehen — kein Textüberlapp, keine Kürzung mit „…“, nichts abgeschnitten; Stichprobe xxxL (`accessibility-extra-extra-extra-large`): AbnGewerke, Home, Zeitplan, SanBad. Abgleich mit der Tabelle „Sichtprüfung bei XL“ oben.

**Ergebnis festhalten:** Datum, Xcode-/iOS-Version, Liste der Screens mit Befund (Screen, hell/dunkel, Element, Beschreibung). Neue Befunde als Findings in diesem Dokument ergänzen; AC3 gilt erst nach diesem Lauf und mit leerer `AuditAllowlist` als erfüllt.
