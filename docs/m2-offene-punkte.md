# M2 – Offene Punkte

Bekannte Lücken und offene Entscheidungen im Zuge von M2.

## D12 outcome

Spike aus Step 0.2 des Plans: Robolectric und Paparazzi in einem Gradle-Test-Task.

**Ergebnis: Leiter-Stufe (b), `forkEvery = 1` auf `testDebugUnitTest`.** Die Stufen (a) und (c) entfallen.

| Schritt | Befund |
|---|---|
| Auflösung der Abhängigkeiten | Gelungen (Netz verfügbar). `ui-test-junit4` und `ui-test-manifest` kommen aus dem Compose-BOM 2026.06.01 (unverändert), `org.robolectric:robolectric` 4.17 (letzte stabile Version) ist unabhängig davon. Kein Konflikt, kein Classpath-Problem, also kein Grund für Stufe (c). |
| SDK 36 | Robolectric 4.17 unterstützt SDK 36. Auf den Testklassen ist kein `@Config(sdk = [...])` nötig. |
| JDK-Zugriff | Robolectric 4.17 braucht unter JDK 21 `--add-exports=java.base/jdk.internal.access=ALL-UNNAMED`, sonst scheitert `ApplicationSharedMemory.create` mit `IllegalAccessException`. Der Schalter steht in `tasks.withType<Test>` in `android/app/build.gradle.kts`. |
| Stufe (a): eine JVM | Der Robolectric-Test (`Text("x")` rendern und prüfen) läuft grün. Danach scheitern alle Paparazzi-Klassen, die in derselben JVM folgen (`Failed to init Bridge`, danach `UninitializedPropertyAccessException`): Galerie hell und dunkel, Referenz-Kanarienvogel, Login-Beispiel und alle `ComponentMetrics*`-Tests. Eine dieser Klassen (`ComponentMetricsBarsTest`) läuft allein grün, mit demselben JVM-Schalter. Ursache ist also gemeinsamer JVM-Zustand (Layoutlib), kein Konflikt der Abhängigkeiten. |
| Stufe (b): `forkEvery = 1` | `:app:testDebugUnitTest` ist vollständig grün: Robolectric und alle Paparazzi-Tests, jede Klasse in einer eigenen JVM. Damit läuft Paparazzi auch nach der Robolectric-Klasse (jede Klasse hat eine frische JVM). |

**Zeitkosten.** Der Lauf von `:app:testDebugUnitTest` dauert mit `forkEvery = 1` rund 2:50 min gegenüber rund 1:15 min ohne (beide Läufe mit Kompilierung, gleiche Maschine), also etwa +1,5 min. Der Hauptanteil ist der Neustart von Layoutlib je Paparazzi-Klasse.

**Folgen für spätere Aufgaben.** Robolectric-Compose-Tests (`createComposeRule`, `@RunWith(RobolectricTestRunner::class)`) liegen in `android/app/src/test` und laufen im bestehenden `:app:testDebugUnitTest`, also auch in der CI ohne Emulator. Der Task-Zuschnitt `forkEvery = 1` gilt nur für `testDebugUnitTest`. Der Wegwerf-Test des Spikes ist gelöscht.

## Entscheidungen

Aus dem Plan (§3). Die Maßnachweise zu D1 stehen im nächsten Abschnitt.

| # | Entscheidung | Folge |
|---|---|---|
| D1 | Die Textzeilenbox ist Chromes gerundete „normal“-Zeilenhöhe: `L(s) = floor(A·s/U + 0,5) + floor(D·s/U + 0,5)`, Grundlinie bei `floor(A·s/U + 0,5)` ab Oberkante. Inter: A 1984, D 494, U 2048. Roboto (v3.015): A 1900, D 500, U 2048. Ein ausdrückliches CSS-`line-height` (nur Login) bleibt `Größe × Faktor`, ungerundet. Das ist der Standard des gemeinsamen Textstils auf beiden Plattformen; die M1-Komponenten übernehmen ihn. | `docs/m1-masspruefung.md:12` (1,21 / 1,171875) gilt für die Referenzen nicht. Die M1-Goldens wurden selbst aufgenommen, M1 hat die Abweichung deshalb nicht bemerkt. |
| D2 | Der Zeilenabstand im Diagramm ist **34** (33 Inhalt + 1 Rahmen oben, content-box), auf beiden Plattformen. | Prototyp-CSS `height: 33px; border-top: 1px` ohne `box-sizing`. „Zeilenhöhe 33“ in `M2.md` ist die Inhaltshöhe. Gemessen: Zeilenränder bei 340 + 34·i (iOS) und 314,0 + 34·i (Android). |
| D5 | Die Tab-Leiste zeichnet die Shell, und nur solange der Stapel des gewählten Tabs an der Wurzel steht. Gepushte Routen (in M2: Platzhalter) zeigen keine Tab-Leiste und haben eine Zurück-Bedienung. | Ein Tab lässt sich nur verlassen, solange sein Stapel leer ist. „Jeder Tab behält seinen eigenen Stapel“ ist in M2 deshalb eine Eigenschaft des Navigationsmodells (Unit-Tests) und **über die UI nicht beobachtbar**. Die UI-Tests prüfen Tab-Wechsel, dass Push die Leiste ausblendet, und dass Zurück zur Tab-Wurzel führt. |
| D7 | Links auf M3/M4-Screens führen zu **Platzhalterzielen**. `href="#"`-Links und Buttons ohne Aktion im Prototyp bleiben sichtbar und tun nichts (`CLAUDE.md`). Die Gewerke-Zeilen der Übersicht pushen `trade(<row.tradeId>)` aus der Fixture (Elektro, Sanitär, Estrich), obwohl der Prototyp alle drei auf das Estrich-Board verlinkt. | M3 legt die Route `trade/{id}` an und entscheidet über den Detailinhalt. |
| D9 | Screen-Texte sind zweigeteilt. Datenähnliche Texte kommen aus der Fixture (`screens.<Name>.<platform>`, `project`, `trades`, `schedule`) über typisierte Modelle. Template-Chrome, den die Fixture nicht trägt, steht je Plattform in einer Konstantendatei `ScreenCopy`, jeder Eintrag mit `Datei:Zeile` des Prototyps. Android nutzt ein Kotlin-`object`, kein `strings.xml`. | Die Regel „Daten nur aus Fixtures über MockRepository“ gilt für Daten. Die Fixture enthält keine Chrome-Texte (der Anti-Erfindungs-Test `fixture.test` würde sie verbieten). Ein Kotlin-`object` hält die Parität zu iOS und braucht in Tests keinen `Context`. |
| D10 | Die Snapshot-Tests rendern den **echten App-Inhalt** (Login oder die Shell mit einem Tab) mit vorgeladenen Daten, nicht einen Screen allein. | Der Snapshot beweist damit, was die App zeigt, einschließlich der Tab-Leiste. **Fallback nicht genommen:** weder iOS noch Android verwendet `TabRootFrame`. Es gibt daher nichts zu dokumentieren; wäre der `NavigationStack` im Snapshot-Renderer verschoben gewesen, stünde der Fallback hier. |
| D12 | Der Android-UI-Test läuft auf der JVM (Robolectric + `ui-test-junit4` in `src/test`) im bestehenden `:app:testDebugUnitTest`. | Ergebnis des Spikes: Leiter-Stufe (b), `forkEvery = 1` (Abschnitt „D12 outcome“ oben). Der Fallback (c) mit Emulator-Job wurde **nicht** genommen, `.github/workflows/ci.yml` bleibt unverändert; die iOS-Scheme führt `BaupilotUITests` aus. |
| D15 | Das Android-Login zeichnet die statischen Feldoptiken des Prototyps: E-Mail im Fokus-Look (2 dp `primary`), Passwort im Ruhe-Look (1 dp `outline`), unabhängig vom echten Fokus. | Der Prototyp zeigt diesen Zustand, und Paparazzi rendert keinen Fokus. Echte Fokus-Gestaltung ist M7. |
| D16 | Die Profil-Schalter wechseln lokalen, nicht gespeicherten Zustand (Startwerte aus der Fixture). Einstellungszeilen, Profilkarte, „Bearbeiten“ und „Abmelden“ haben keine Aktion. | Im Prototyp sind die Zeilen einfache `div`s, Karte und „Bearbeiten“ sind `href="#"`. |
| D21 | Mehrzeiliger iOS-Text, dessen gerundete Box kleiner ist als Inters natürliche Höhe (17, 18, 20, 22, 24 pt), lässt sich unter iOS 17 nicht mit `lineSpacing` enger setzen. | Kein M2-Screen hat solchen Text: die mehrzeiligen Texte sind die Login-Features (15 pt) sowie Login-h1 und -Absatz (ausdrückliches `line-height`). Bekannte Lücke (R2). |
| D22 | **Boxkanten werden gerastert, das Layout nicht.** `ScheduleLayout` behält die ungerundeten Prototypwerte (AC3 prüft sie). Zusätzlich liefert ein Helfer `snap(x)` (kaufmännisch gerundet, Hälften weg von null) die Zeichenwerte: Balken `drawX = snap(barX)`, `drawWidth = snap(barX + barWidth) − drawX`; `todayDrawX = snap(todayX)`; Raute `drawCenterX = snap(diamondCenterX)`. Die x-Lage von Monats- und „Heute“-Beschriftung bleibt ungerundet. | Die Referenzen zeichnen Boxkanten auf ganzen pt/dp und Text an gebrochenem x. Die Rundung liegt im reinen Layout, die Views rechnen nicht (I9). |

## Maßnachweis der Zeilenbox (D1)

- Die hhea-Werte (Inter A 1984 / D 494 / U 2048; Roboto A 1900 / D 500 / U 2048) wurden in jedem gebündelten Schnitt bestätigt. Inters typo-/win-Metriken stimmen mit hhea überein; bei Roboto ist `USE_TYPO_METRICS` aus.
- Entscheidend: die Oberkante der iOS-Zeitplan-Karte bei 129,0 und der Rahmen der ersten Diagrammzeile bei 340,0. Das 1,21×-Modell verfehlt beide um rund 1,3 pt; das lässt sich nicht mit dem Einrasten von Boxkanten (D22) erklären. Das gerundete Modell trifft sie.
- Stützend, nicht unterscheidend: die iOS-Phasenkarte der Übersicht mit 141 pt und die Oberkante der Android-Spur der Übersicht bei 192,0 dp. Beide stimmen mit dem gerundeten Modell überein, ließen sich aber auch durch Einrasten erreichen.
- Die Android-Zeilenränder bei 314,0 + 34·i ergeben sich aus dem D1-Layout: 96 + 8 + 152 + 12 + 14 + 12 + 20 (D2).
- `docs/m1-masspruefung.md:12` nennt weiterhin 1,21 bzw. 1,171875 und ist für die Referenzen falsch (siehe die dort ergänzte Korrektur).

## Korrektur der Grundlinie (Review-Zyklus 1, C1/M3)

Die Review fand 1/3-pt-Nudges auf jedem 13-pt-Text und an den Leisten und führte sie auf einen Rest-Fehler im Zeilenmodell zurück. Er ist behoben, die Nudges sind entfernt.

- **Ursache 1, Grundlinie.** Blink (Linux-Fontmetriken, der Renderer der Referenzen) nimmt dem Ascent eine Einheit und gibt sie dem Descent, wenn das Runden den Descent verkleinert (`descent_gerundet < descent_exakt`). Die Boxhöhe bleibt, die Grundlinie steigt um 1. Inter: 10, 13, 14, 17, 18, 22 pt; Roboto: 10, 13, 14, 17, 18, 22 sp (u. a. alle 13- und 14-Größen, die in den Nudges steckten). Umgesetzt in `InterMetrics.baselineOffset` und `RobotoMetrics.baselineOffset`; `lineBox` ist unverändert. Tests: `testBaselineRisesWhereDescentRoundsDown`, `baselineRisesWhereDescentRoundsDown`.
- **Ursache 2, ausdrückliches `line-height`.** LayoutNG rundet den halben Zeilenabstand **ab** (`FontHeight::AddLeading`). Die erste Grundlinie liegt bei `baselineOffset + floor((line-height − lineBox) / 2)`. iOS: `LineHeight.factor`. Android: `BaupilotText` setzt `LineHeightStyle.Alignment.Top` und verschiebt die erste Grundlinie per `cssLineHeightBaseline` (vorher zentrierte Compose die ungerundete Fonthöhe und lag bis 0,5 px daneben). Damit entfällt `INTRO_BASELINE_SHIFT`.
- **Entfernt:** alle `.offset(y: …)`-Nudges der iOS-Screens (Home, Zeitplan, Profil, Login) und der Komponenten `FormField`, `SettingsRow`, `AskRow`, `ScheduleChart`, `ButtonHeight.tall`/`textOffset`, `FeatureRow.textOffset`; auf Android alle `*_SHIFT`-Konstanten der Screens (`Login`, `Home`, `Zeitplan`, `Profil`) und der Komponenten `SectionHeader`, `DocumentTile`, `AskRow`, `ScheduleChart`.
- **M4 erledigt:** Die privaten Kopien `SettingsRow`, `EditPill` und `LoginChip` sind gelöscht. Profil nutzt `BaupilotListItem(H56, Leading.Icon, Trailing.Switch)` und `TonalButton`, Login `AssistChip`. Dafür musste `ChipFrame` den Rahmen von innen nach CSS-border-box zählen (Inhalt beginnt bei Rahmen 1 + Padding 12 = 13 dp; vorher 12).
- **Galerie-Goldens neu aufgenommen** (beide Plattformen), weil Grundlinie, Zeilenabstand und der Chip-Rahmen alle Komponenten berühren, die Text zeigen. Android: alle 16 vorhandenen Goldens änderten sich schon im ersten M2-Lauf (Icons 6,5 %, Inputs 3,3 %, Buttons 1 % der Pixel um mehr als 16 Stufen). Ursache war das gerundete Zeilenmodell (D1), das in M1 noch 1,171875 × Größe war, plus jetzt die Grundlinienkorrektur. Die iOS-Bilder und die Android-Bilder Inputs, Lists, Buttons, Chips, Cards und Bars (dunkel) wurden nach der Aufnahme angesehen (Zyklus 2 des Fixes); die übrigen Android-Bilder wurden nicht einzeln betrachtet, nur ihr Lauf ohne Flag ist grün.

## Bekannte Lücken

- **R2 / D21:** iOS-Mehrzeilentext mit negativem Zeilenabstand (Boxen kleiner als Inters natürliche Höhe) ist nicht möglich. In M2 gibt es keinen solchen Text.
- **R11 / AC1 (offen):** Die Screen-Snapshots sind nach der Korrektur oben nicht alle grün. Gemessen (Python, Referenz gegen `build/snapshots`):
  - **Lage stimmt.** Die Ink-Ausdehnungen der Textzeilen von Home (iOS) fallen bis auf ≤ 1/3 pt mit den Referenzen zusammen; ohne Nudges liegen Titel, Kicker, Karten und Beschriftungen auf der Referenz.
  - **Rest 1: Textraster.** Chrome (Skia) und CoreText/Layoutlib setzen die Glyphen an unterschiedlichen Subpixel-Positionen (viele Zeilen brauchen ±1/3 pt in x). Das ist kein Layoutfehler und lässt sich nicht an der Ursache beheben.
  - **Rest 2: Zeilen in Karten.** Auf iOS liegen Inhalte in Listenzeilen (Home: Gewerke, Dokumente) 1/3 pt höher als in der Referenz (Mittelpunkt mit halbem Pixel); eine Ursache dafür ist offen. Es wurden dafür keine Nudges eingebaut.
  - **Messung nach Review-Zyklus 2 (Login hell, iOS).** Die x-Ausdehnungen aller Textzeilen stimmen auf ≤ 1/3 pt mit der Referenz überein, die y-Lage der Zeilen auf ≤ 0,02 pt (Zeilenschwerpunkte, Python); Karten, Tiles und Buttons liegen pixelgleich. Zwei Layoutfehler wurden gefunden und behoben (`GreedyLabel`: UIKit setzt die Grundlinie einer festen Zeilenhöhe bei `Zeilenhöhe − Descender`, CSS bei der gerundeten Ascent, Differenz 0,38 pt bei 15 pt; der Intro-Absatz zählt für das Layout 65 pt, nicht 65,25 pt). Übereinstimmung Login hell damit 0,9760 → 0,9814. Der Rest liegt in den Kanten der Glyphen (CoreText/Layoutlib gegen Skia: Hinting, Subpixel-Positionen, rund 1–2 % weniger Tinte pro Zeile), nicht im Layout. Ein Versuch, die Mittenrundung von Chrome (Halbpixel-Ties nach unten) über `alignmentGuide` nachzubilden, hatte keine Wirkung und ist verworfen.
  - Die Tab-Leiste (254 gegen 255) liegt unter der Wahrnehmungsschwelle (`perceptualPrecision` 0,98).
  Eine rote CI wird über die hochgeladenen Artefakte `build/snapshots/*` untersucht. Die Toleranzen ändern sich nie.
- **R17:** Eine echte Statusleiste auf einem Gerät überdeckt den oberen Bereich von 32 dp bzw. 58 pt. Offen bis M7.
- **R18:** Touch-Ziele unter 44/48: Android „Bearbeiten“ 40, „Alle“ 36 in einer 40er-Zeile, „Neues Bauvorhaben anlegen“ 44. Gleiche Konvention wie M1 (`m1-offene-punkte` §3: Compose erweitert klickbare Elemente auf das Mindestziel). Geräteprüfung in M7.
- **D5:** Die Stapel je Tab sind in M2 nur im Navigationsmodell getestet, nicht über die UI.
- **D12:** `testDebugUnitTest` läuft mit `forkEvery = 1` etwa 1,5 Minuten länger als ohne.

## Verifikation

Stand nach Review-Zyklus 1 (Fix). Befehle wie im Plan §12.

| # | Befehl | Exit | Ergebnis |
|---|--------|------|----------|
| 1 | `cd scripts && node --test test/` und die vier Prüfskripte | 0 | unverändert (kein Skript und keine Referenz berührt) |
| 2 | `xcodebuild test … -only-testing:BaupilotTests` (iPhone 15) | 65 | 105 Tests, 97 bestanden, 8 fehlgeschlagen: alle `ScreenSnapshotTests`. Übereinstimmung (Stand Review-Zyklus 2, Fix) Login 0,9802 / 0,9814, Home 0,9802 / 0,9811, Profil 0,9833 / 0,9841, Zeitplan 0,9841 / 0,9847 (gefordert 0,995). `GalleryForms` hell/dunkel neu aufgenommen (Merkmalszeilen 1 px höher, angesehen); Galerie grün. |
| 3 | `xcodebuild test … -only-testing:BaupilotUITests` | 0 | 7 Tests, 0 Fehler (neu: `testProfileSwitchSurvivesTabSwitch`). |
| 4a | `./gradlew :app:testDebugUnitTest --tests de.baupilot.app.BaupilotRootLoadTest` | 0 | 3 Tests grün (D11: `loadOrNull` liefert bei fehlender oder nicht lesbarer Fixture null, bei guter Daten). Das Laden läuft jetzt auf `Dispatchers.Default`. |
| 4 | `./gradlew :app:testDebugUnitTest` (JDK 21 aus Android Studio) | 1 | 109 Tests, 6 fehlgeschlagen: Home, Profil, Zeitplan je hell/dunkel. Abweichung Home 0,578 / 0,614 %, Profil 0,718 / 0,748 %, Zeitplan 0,504 / 0,562 % (erlaubt 0,5 %). **Login hell/dunkel jetzt grün.** `AppNavigationUiTest` (neu: `profileSwitchSurvivesTabSwitch`) und Galerie grün. |

**Offen (AC1 nicht erfüllt):** iOS 8/8 und Android 6/8 Screen-Snapshots liegen außerhalb der Toleranz, siehe R11 / AC1 oben. Zu M1 der Review: `AppShell` hält die Profil-Schalter jetzt selbst (`profileToggles`) und blendet inaktive Stapel weiter per `.hidden()` aus (nur so verschwinden ihre `screen.*`-Identifier aus dem Zugriffsbaum; `accessibilityHidden` allein genügt für XCUITest nicht). Ein ausgeblendeter Tab verliert dadurch auf iOS seine Scrollposition; Android behält Schalter über `rememberSaveable`.
