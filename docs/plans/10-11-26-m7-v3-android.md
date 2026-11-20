# M7 · Android · Interaktion, Barrierefreiheit & Feinschliff — Implementation plan

Android half of `10-11-26-m7-v3.md`, split so it runs on its own. Plan for `docs/milestones/M7-android.md`. Repository: `repos/2270495f` (baupilot), branch `feat/m7-android` from `feat/m7` (`fe905f7`). Rules: `CLAUDE.md` (tolerances never loosened, references never re-recorded, values 1:1 from the prototype, colours only through tokens, texts verbatim, touch targets ≥ 48 dp, labels from `aria-label`, placeholders visible without action).

**Independence from iOS:** this plan changes nothing under `ios/`, needs no Xcode and no simulator, and never waits for the iOS plan (`10-11-26-m7-v3-ios.md`). Shared files (`scripts/`, `scripts/link-coverage.json`) are only extended in a way that keeps the other platform's state valid (§5.0). Step numbers are the ones of the original plan; iOS-only steps (4, 12, 13) are left out.

**Existing work:** `feat/m7` already holds a partial run of the combined plan (`docs/m7-offene-punkte.md` Baseline and Kalibrierung, `scripts/lib/offbytwo.mjs`, `band-shift.mjs`, `links.mjs`, `scripts/link-coverage.json`, some Android component changes). Before each step, check whether its contract is already met on the branch; if so, run only its tests and gate and record that it was taken over.

Read before implementing: `docs/m6-offene-punkte.md` (latest measurements, R1/R11/G1), `docs/m7-offene-punkte.md` (baseline of the combined run), `README.md` (commands, JDK 21).

---

## 1. Goal and scope

**Goal (M7-android.md):** all 68 Android snapshots and all Android click paths green; the Android app is accessible.

**In scope (the five M7 tasks, Android only, in this plan's order):**

1. **Links:** a Robolectric UI test for every link with a target listed in the "Links" sections of M2–M6 (100 screen→target pairs over 34 screens, counted by parsing the `- Links:` lines; iOS and Android lists are identical), plus a "does nothing" test for every placeholder (`href="#"`, `null` route).
2. **TalkBack:** labels from `aria-label`; the diagrams Zeitplan, Gefälle and Raster each one element with a description; the Android semantics gaps found in research (§2.3) fixed.
3. **Touch targets:** an automated check over all 34 screens (≥ 48 dp).
4. **Diff report:** one machine-made report over 34 screens × 2 modes (68 Android rows), a fresh baseline, honest attempts at the remaining residue, and a justification per row that stays red.
5. **Large text:** renders at font scale 130 % for all 34 screens, an automated overlap/clipping check, fixes, and a recorded visual review.

**Acceptance (M7-android.md)** — mapped in §10: 68/68 Android snapshots green; all Android UI tests green; Android accessibility audit without errors.

**Out of scope:** everything under `ios/`; real data and persistence (M8); new screens; any change to `design/`, `fixtures/`, references, tolerances or tokens in `design/tokens.json`; new visible text. Placeholder links stay placeholders.

**Expectation:** Android is close: 12/68 green at the last baseline and 11 more at ≤ 0.55 % (§2.6). Steps 11a–c are the levers. Any row that stays red is **reported with evidence, never hidden**.

---

## 2. What exists (grounding)

### 2.1 Test and harness infrastructure

- **Android screen snapshots:** `android/app/src/test/java/de/baupilot/snapshot/ScreenSnapshotTests.kt` has 68 classes (one per screen × mode; `forkEvery = 1`, `app/build.gradle.kts:65`), each building an `AppNavigationState` inline.
  - Comparison: `StagedSnapshotHandler.kt:39` `SnapshotVerifier(MAX_PERCENT_DIFFERENCE, …)`, with `MAX_PERCENT_DIFFERENCE = 0.5` at `BaupilotPaparazzi.kt:11`. `newFrameHandler` returns an **anonymous** `FrameHandler` (:42–55: write artifact, check size with `IllegalStateException("Größe …")`, `verifier.handle(image)`); Staged knows neither screen name nor mode. It is shared by `ReferenceSnapshotHandler` (screens) and `GoldenSnapshotHandler` (gallery, `build/snapshots/android-gallery/`).
  - `class ReferenceSnapshotHandler(name: String, mode: SnapshotMode, writeArtifact: Boolean = true)` (`ReferenceSnapshotHandler.kt:28–32`): the three are **plain constructor parameters, not properties**, and the artifact path (`<repoRoot>/build/snapshots/android/<name>_<mode>.png`, `null` when `writeArtifact = false` as in the canary) is built inside the `artifactFile` lambda passed to Staged. `Snapshot.name` is null for all screen tests, so the name comes only from the constructor. Test classes `ReferenceSnapshotHandlerTest` and `GoldenSnapshotHandlerTest` **already exist** (5 tests each).
  - **Failure:** Paparazzi throws `java.lang.AssertionError("Images differ (by %f%%) - see details in file://…\n…")` (plus an "accept the changes" hint), formatted with the JVM default locale (`de_DE` on this machine → `0,612345`; no `user.language` is pinned). Nothing is reported on a pass.
  - The differ is Paparazzi's default **OffByTwo**, because the JVM property `app.cash.paparazzi.differ` is unset (verified by bytecode). OffByTwo skips pixels whose every channel differs by ≤ 2, sums |ΔR|+|ΔG|+|ΔB| over the rest and reports `delta·100/(w·h·3·256)`.
- **Tolerance guard:** T-DS-3 in `scripts/test/design-system.test.mjs:229–316` scans `android/app/src/test` and `src/testDebug` (`.kt`), excluding `BaupilotPaparazzi.kt`. Planted-fixture tests already exist (`mkdtempSync` + `put`, :286–316). It does **not** guard `app.cash.paparazzi.differ` (Step 1 closes this).
- **`scripts/diff-report.mjs`:**
  - `run(options)` at :73 (injectable `actualDir`, `referenceDir`, `out`; no screens list: it lists `*_(light|dark).png` in `actualDir`). Defaults: actuals `build/snapshots/<platform>`, references `design/reference/<platform>`, out `reports/<platform>` (both `/build/` and `/reports/` are git-ignored). Per platform it writes `<Name>_<mode>.png` panels and `summary.json` rows `{name, mode, reference: {w,h}, actual: {w,h}, sizeMismatch, differentPixels, percentDifferent}`.
  - It measures exact byte inequality, not comparable to the test metric. No whole-run view, no missing-actual detection, no pass/fail.
  - Tests: `scripts/test/diff-report.test.mjs` (6). `scripts/lib/screens.mjs` `parseScreens` yields the 34 screens (`nr, name, title, iosFile, androidFile, route, milestone`) from `design/screens.md`. `lib/png.mjs` exports `decodePng`, `encodePng`, `PngError`. Exit codes today: 0 (also with no actuals), 1 reference missing, 2 usage **or `PngError`**.
- **Android UI tests** (`android/app/src/test/java/de/baupilot/app/*UiTest.kt`, 42 tests, Robolectric + `createAndroidComposeRule<MainActivity>`, `@Config(qualifiers = "w411dp-h891dp")`):
  - helpers `exists`, `awaitScreen`, `logIn`, `systemBack`, `tap`, `scrollAndTap`, `assertAbsent`, differing per class; `systemBack` (only `AppNavigationUiTest.kt:47–49`, `TradeFlowUiTest.kt:44–46`) is `rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }`, so it needs an `AndroidComposeTestRule`; **no test uses `useUnmergedTree`**; no Robolectric test selects dark mode today;
  - screen identifiers `screen.<tab>` / `screen.<route with dots>` as `testTag`s; `tab.home|schedule|inspections|documents|profile`;
  - `nav.close` (`TopAppBar.kt:239`) instead of `nav.back` on 12, 13, 21, 22, 26, 32, 34.
  - **Entry:** `MainActivity.kt:11` is `setContent { BaupilotRoot() }`, with no start-state hook. `BaupilotAppContent(state, onStateChange, data)` is public (`BaupilotRoot.kt:63`); the snapshot tests host it with `TestAppData.load()` (`snapshot/TestAppData.kt:10`, package `de.baupilot.snapshot`). The **only `BackHandler` is `BaupilotRoot.kt:38`** (`BackHandler(enabled = state.canPop) { state = state.pop() }`); `nav.back`/`nav.close` pop via `AppShell.kt:149`. Dark mode follows `isSystemInDarkTheme()` (`Tokens.kt`), so Robolectric `+night` selects it.

### 2.2 Link coverage today (verified count)

- **Coverage of the 100 pairs:** Android UI tests cover 75.
- **Uncovered on Android (25):**
  - 3 Zeitplan → 2, 4, 5, 6
  - 4 Profil → 3, 6, 10
  - 6 Dokumente → 3, 10 (Android's only Documents → `tab.inspections` tap, `InspectionFlowUiTest.kt:134–137`, lands on the Mängel view = screen 15)
  - 10 AbnGewerke → 2, 3, 4, 19
  - 11 AbnElemente → 10
  - 12 Prüfschritt → 11
  - 15 AbnMängel → 2, 3, 4
  - 19 Zimmerer → 10
  - 20 ElÜbersicht → 10
  - 22 ElVerteiler → 13
  - 28 EsÜbersicht → 10
  - 29 EsEbenheit → 13 (`flatness.defect` is asserted, never tapped)
  - 33 MaÜbersicht → 10
  - 34 MaRaum → 13
- **Trap:** `tab.inspections` restores the last Abnahmen view, so a tap meant for screen 10 can land on 15; every X → 10 test asserts `inspections.view.trades`.
- **Link elements with a target that are never tapped:** Home trade rows Elektro and Sanitär; Home document tiles 2–4. None has an identifier (`HomeScreen.kt:79/89`; ask cards `ScheduleScreen.kt:71–74`). Android Home has no inline "Zeitplan" link; its "Alle" is a placeholder (`HomeScreen.kt:70`).
- **Placeholder "no action" tests exist only for a few elements** (Gewerk Lorenz offer, Mitteilungen "Termin bestätigt", AbnGewerke Dachdecker row, some null-route protocol rows, FlBad tile). The full list is in §6.2.
- **Machine-readable route data:** `fixtures/baupilot.json` holds `schedule.asks[].target`, `inspections.overview.rows[].route`, `inspections.byTrade.<t>.groups[g].rows[r].route`, `…windows.checks.elements.floors[].items[].route`, `notifications[].items[].target`, `screens.<Name>.android.tabs[].route`. Back, bell, CTA and `href="#"` links are not in the fixture.

### 2.3 Accessibility today

- **Labels:** all 73 Android prototype `aria-label` occurrences reach the code (`ScreenCopy.kt`, fixture keys `ariaLabel`, `gridAria`, `aria`, `chart.label`, `timer.progressLabel`, …, design-system components `TabBar`, `TopAppBar`, `ValueStepper`, …). **No missing string was found.**
- **Diagrams:**
  - **Zeitplan:** already one element: `ScheduleChart.kt:79–81` (`clearAndSetSemantics { contentDescription; Role.Image }`).
  - **Gefälle (`SlopeResult`):** percent (`slope.percent`) and verdict (`slope.verdict`) are separate elements. `SlopeSchema` (`SlopeResult.kt:84–113`) draws its two labels as `SvgText` nodes TalkBack reads, although the prototype svg is `aria-hidden` (AndSanGefaelle:62). The card is a plain `<div>` without role or `aria-label`. **Does not meet M7.** The UI test reads the two identifiers at `LaterTradesUiTest.kt:97–101` (`assertText`, merged tree).
  - **Raster (FlBad `TileGrid`):** a non-merged container description (`TileGrid.kt:54`) plus tiles with `clearAndSetSemantics { contentDescription; role = Role.Button }` and **no click action** (:79–82). Grid constants: padding 8, gap 4, tile height 44; 8 columns × 3 rows from `ScreenCopy.FlBad` (`ScreenCopy.kt:555/558`), matching the prototype. **Does not meet M7.** The tile tap is tested via `tiles.tile.tile-3` (`LaterTradesUiTest.kt:159`). Fixture `checks.bath` tiles are `{id, status, label?, aria}` (24; `tile-3`/`tile-4` have `status: "defect"`, e.g. `aria: "Fliese 3: hohl"`); "hollow" means `status == "defect"`.
- **Semantics gaps:**
  - **A1:** `TimerRing.kt:38–41` puts `semantics { role = Image; contentDescription }` on a Box whose Text child is focusable on its own, so TalkBack likely reads "12:40" and never the description.
  - **A4:** the SanGefaelle section tile label is `"A3 jetzt"` (`SanGefaelleScreen.kt:91`). The prototype tile is a plain `<div>` without a label. The tile is a `SocketTile` (`SocketGrid.kt:57–59`).
  - **I3:** `SignaturePad` exposes the English value `"strokes: n"` (`SignaturePad.kt:58`; m4-offene-punkte:88 says "in M7 prüfen"). `InspectionFlowUiTest` never reads it.
  - **A2:** container descriptions on non-merged containers (`Hauptnavigation`, `Ansicht`, …) are in the semantics tree but not spoken as their own stop.
  - **A3:** placeholders are a plain-text node.
  - **A5:** `ComparisonTable` has no table semantics.
- **Audit tooling:** Compose `ui-test` is 1.11.4 (BOM 2026.06.01), Robolectric 4.17, Paparazzi 2.0.0-alpha05.1. The BOM manages `androidx.compose.ui:ui-test-junit4-accessibility` 1.11.4 (it pulls ATF). It is not declared and not in the Gradle cache (a download); `enableAccessibilityChecks()` comes only from it; whether it runs under Robolectric is unverified. There is no `ScreenCopy` unit test.

### 2.4 Touch targets today

- No `minimumInteractiveComponentSize` anywhere. All targets are custom `Modifier.clickable`, and Compose extends the pointer hit test to 48 dp. `touchBoundsInRoot` includes this; it is already used by `TextLinkTouchTargetTest` in `ComponentMetricsLaterTradesTest.kt:366–382`.
- The m5/m6 docs wrongly say `minimumInteractiveComponentSize` (m5:74, m6:80); Step 14 corrects this.
- **Known sub-minimum targets:** FlBad tiles 35.25×44 dp (m6 R11; no action). `VerdictToggle` cells 48×40 (`ObjectCheckRow.kt:80–133`; touch bounds reach 48).

### 2.5 Large text today

- `BaupilotText` sizes and line boxes are `sp` (`Typography.kt:152–219`), so they scale. About 36 fixed `.height(N.dp)` containers clip grown text: `Buttons.kt:98/189–190`, `Chips.kt:45`, `TabRow.kt:59`, `TextField.kt:56/60`, `FactRow.kt:23`, `SegmentedButtons.kt:46`, `ProtocolBanner.kt:89`, `TileGrid.kt:72`, `SocketGrid.kt:54`, `ProfileScreen.kt:113`, ….
- **Line boxes:** `robotoLineBox` (`Typography.kt:200`, body :203–204) / `cssLineHeightBaseline` (:222, body :225) convert `lineBox(size).sp.roundToPx()`. Above font scale 1.05 Compose scales sp **non-linearly** (API 34+) and resolves an sp line height in proportion to the font size, so at 1.3 these helpers yield a smaller box than Compose's line height: clipping that `heightIn` alone does not fix.
- **Existing typography tests:** pure-JVM `test/…/designsystem/TypographyTest.kt` (6 tests). The new Robolectric one is a new class (§5.10).
- **Test tooling:** Robolectric 4.17 `@Config(fontScale)` exists; `@GraphicsMode` and parameterised runners are unused so far; Paparazzi `DeviceConfig` has `fontScale`, but `baupilotPaparazzi(handler, mode)` (`BaupilotPaparazzi.kt:19–37`) takes none.
- **Overlap logic:** none exists.

### 2.6 Snapshot state (numbers are stale, see Step 0)

- **Android:** 12/68 green, and 11 more at ≤ 0.55 %: Home light 0.5017, FlUebersicht light 0.5040, RohbauMasse dark 0.5046, Vergleich dark 0.5206, RohbauMasse light 0.5214, ElUebersicht light 0.5227, Mitteilungen dark 0.5227, Zeitplan light 0.5274, Profil dark 0.5385, AbnGewerke dark 0.5424, Mitteilungen light 0.5483.
- **G1:** fixed at the end of M6 (`m6-offene-punkte.md:85`: `Step.trailing` parameter order; gallery green again, golden not re-recorded). Step 0 confirms it.
- **Stale numbers:** commit `3fcbbd6` changed FlBad, MaRaum, MaUebersicht, `VerticalStepper`, `IconTile`, `StepRow`, `ListRow` and `Buttons` *after* the M6 measurements. Every comparison starts from a fresh baseline (Step 0).
- **Lever never tried:** `TextMotion.Animated` (fractional glyph advances; named in M3 as an untested lever).

---

## 3. Decisions

Numbers are those of the combined plan; iOS-only decisions (D15 iOS part, D18) are left out. Reviewers may bounce any of them.

| # | Decision | Why |
|---|---|---|
| D1 | **Fresh baseline first.** Step 0 runs every Android suite and `diff-report --all --platform android`. It stores `build/m7/android/baseline/summary.json` (not committed) and puts the numbers into `docs/m7-android-offene-punkte.md`. Every later "unchanged" claim compares against this file. | The M6 numbers predate `3fcbbd6` (§2.6). |
| D2 | **Tolerance pin extended.** "Tolerance unchanged" also means that no `app.cash.paparazzi.differ` system property is set anywhere (gradle files, `gradle.properties`, test sources). T-DS-3 learns to flag it. | Setting it silently swaps OffByTwo for another differ. |
| D3 | **One whole-run diff report, made by a script, per platform.** `scripts/diff-report --all --platform android` covers 34 screens × {light, dark} = 68 rows. Per row: the **test verdict and value** (from a sidecar JSON), exact-pixel %, OffByTwo % (a Node replica), a **position share** (band-shift method of M5/M6), and `actualSha256`. It also writes a Markdown table. `--baseline <file>` lists changed rows; `--unstable <file>` exempts rows proven non-deterministic (Step 0.5) from the hash comparison only. | M7 task 4; the per-row evidence is the "begründen" part. The `--platform` filter keeps this plan independent of iOS artifacts. |
| D4 | **Screen catalog.** `ScreenCatalog` in the test source set (name → `AppNavigationState`), used by the 68 snapshot classes and the new Robolectric tests. | Audits, the touch-target walk and the large-text checks need all 34 screens. Switching the snapshot classes to it proves it right: the baseline must not change. |
| D5 | **Link tests start at the source screen** via a test-only `LinkHost` (§5.4) over `ScreenCatalog.state(name)`; no production hook. They tap the link element and assert the target screen identifier. Existing end-to-end flows stay. | Starting at X keeps the new tests fast and independent. |
| D6 | **Link coverage is enforced by a guard.** `scripts/link-coverage.json` maps each of the 100 pairs and each placeholder to an Android test name (the `ios` field belongs to the iOS plan). `scripts/test/links.test.mjs` fails when a pair is missing, a mapped test method does not exist, or the manifest lists a pair the docs do not have — **per enabled platform** (§5.0). | "alle Links mit Ziel" becomes checkable in-process. |
| D7 | **Gefälle = the whole `SlopeResult` card as one element.** Label from the visible texts only: percentage, then verdict, e.g. "0,75 %, Zu flach · Soll ≥ 1,0 %". `Modifier.semantics(mergeDescendants = true)` on the card with `testTag("slope.result")`; `SlopeSchema` gets `clearAndSetSemantics {}` so its sketch labels are hidden like the prototype's `aria-hidden` svg; the child tags `slope.percent` / `slope.verdict` stay reachable only with `useUnmergedTree = true`. | "Diagramme … als ein Element mit Beschreibung"; no new text. Same contract as the iOS plan. |
| D8 | **Raster = the FlBad tile grid (`TileGrid`) as one element.** Label: `gridAria` ("Fliesenraster Boden, oben die Wand mit Dusche, unten die Tür"). State: the `aria` strings of the hollow tiles, joined with ", ". `clearAndSetSemantics { contentDescription; stateDescription }` plus `testTag("tiles.grid")`; tiles stop being separate elements and keep no action. **Supersedes the accessibility part of m6 D12 (tiles as buttons without action); its behaviour part (a tap does nothing) is kept.** The ElRaum socket grid keeps one button per socket. | M7 names "Raster" among the diagrams. Same contract as the iOS plan. Closes m6 R11. |
| D9 | **Zeitplan is unchanged** (already one element). A test pins it. | §2.3. |
| D10 | **`TimerRing`** gets `clearAndSetSemantics { contentDescription; role = Image }` like `HeatingBarChart` (A1). | A real TalkBack miss. |
| D11 | **SignaturePad state (I3):** "strokes: n" is replaced by **"leer"** (no stroke) and **"unterschrieben"** (≥ 1 stroke) as `stateDescription`. The words live in `ScreenCopy.kt` with the comment "M7 D11, no prototype text (role=img); state for VoiceOver/TalkBack". **Use exactly these two words: the iOS plan uses the same ones.** | m4 deferred it to M7. **Only non-visible text added in M7.** |
| D12 | **Merged labels without an `aria-label` use the visible texts in reading order, joined with ", ".** A4: the section tile becomes "A3, jetzt" (same as iOS). | The visible text is the only prototype source. |
| D13 | **Kept as is, documented:** A2, A3, A5. | Lowest-risk; recorded as open items. |
| D14 | **Audit = the ATF checks behind Accessibility Scanner**, via `ui-test-junit4-accessibility` **if** the Step 8 spike shows it runs under Robolectric; otherwise the semantics-based fallback (§5.8). An issue that cannot be fixed without breaking a project rule (e.g. contrast of a design-token colour pair) goes into an **explicit allowlist in the test** (screen, check, element, reason), reported in the doc; **AC3 counts as met only if the allowlist is empty.** A manual Accessibility Scanner run on a device is an operator step. | Closest reproducible check. |
| D15 | **Large text = `fontScale 1.3`.** | M7 names "Schriftgröße 130 %". |
| D16 | **Large-text fixes must not change the default rendering.** The line-box helpers keep today's expression at `fontScale == 1f` (§5.10). Text-bearing fixed `.height(N.dp)` becomes `.heightIn(min = N.dp)` with unchanged alignment. **Proof:** `diff-report --all --platform android --baseline` shows no changed `actualSha256`, and the gallery goldens stay green, after Step 10. | "Ändere keine Maße …". |
| D17 | **Snapshot levers are spikes with fixed adoption rules (Step 11).** Kept only if, against the baseline: (a) no green snapshot turns red; (b) the number of green Android snapshots rises, **or** the summed `offByTwoPercent` falls and no single row worsens by more than 0.005 %; (c) every changed measure cites its prototype line. Otherwise reverted and documented with numbers. Gallery goldens may be re-recorded only by a kept lever, after the new golden has been looked at and explained. | "offene Reste beheben oder begründen". |
| D19 | **No LazyColumn or scroll-clipping workaround in production code.** The touch-target walk runs in a tall Robolectric window (`h3000dp`). | `touchBoundsInRoot` is clipped by the scroll viewport. |
| D20 | **Placeholder identifiers.** Placeholders without identifier get `ph.<ReferenceName>.<slug>` as `testTag` (same names as iOS). Link elements with a target and no identifier get `home.trade.<tradeId>`, `home.doc.<n>`, `schedule.ask.<n>`. Rendering unchanged (pinned by `--baseline`). | Needed to tap them deterministically. |
| D21 | **Docs.** `docs/m7-android-offene-punkte.md`, m6 structure, in German. README gets an "M7 · Android" section with the commands. The m5/m6 R11 correction goes in as a note; m5/m6 files are not rewritten. | Project convention; a per-platform doc avoids write conflicts with the iOS plan. |

---

## 4. Order of work

Red-first where behaviour can break. Each step ends with its tests green (snapshot rows excepted as stated) and its own commit `M7 step N (android): …`.

| Step | Content |
|---|---|
| 0 | Preflight: fresh baseline of the Android suites and the 68 rows (D1) |
| 1 | Tolerance pin: T-DS-3 flags `app.cash.paparazzi.differ` (D2) |
| 2 | Android result sidecar; `diff-report --all --platform`, OffByTwo replica, band-shift, `--baseline` (D3) |
| 3 | `ScreenCatalog`; snapshot tests switched to it (D4) |
| 5 | Link coverage manifest + guard (Android enabled); missing identifiers; missing link tests (D5, D6, D20) |
| 6 | Placeholder "no action" tests (D20) |
| 7 | Semantics: Gefälle, Raster, TimerRing, SignaturePad, A4 (D7–D12) |
| 8 | Accessibility audit (ATF spike first) (D14) |
| 9 | Touch-target walk + fixes |
| 10 | Large text: renders, automated checks, fixes (D15, D16) |
| 11 | Snapshot levers: `TextMotion.Animated` spike, near-threshold screens, G1 (D17) |
| 14 | Docs: `m7-android-offene-punkte.md` incl. the 68-row table; README section |
| 15 | Verification (§12) |

Steps 5–10 must not change any row of the diff report (`--baseline` → no changes). Step 11 is the only step allowed to change rows, under D17.

---

## 5. Contracts

### 5.0 Shared files (both platform plans touch them)

- **`scripts/`:** only additive, backwards-compatible changes. If the iOS plan already added a module or option this plan needs, reuse it; never change its behaviour. `cd scripts && node --test test/` is green at the end of every step.
- **`scripts/link-coverage.json`:** gets a top-level `"platforms": { "ios": <bool>, "android": <bool> }`. A platform's mappings are validated only when its flag is `true`. This plan sets `"android": true` at the start of Step 5 and never touches `"ios"` or any `ios` field (a missing `ios` field is allowed while `"ios": false`).
- **Gallery goldens, references, `design/`:** unchanged unless D17 allows (Android goldens only).

### 5.1 Tolerance pin (Step 1)

- **File:** `scripts/test/design-system.test.mjs`. Extend the T-DS-3 scan without changing the existing literal patterns.
- **New pattern:** any occurrence of `app.cash.paparazzi.differ` in `android/**/*.kts`, `android/gradle.properties`, `android/**/src/test/**` and `android/**/src/main/**` is a hit, formatted like the existing hits as `path:line: text`.
- **Tests (red-first):** `T-DS-3: a planted paparazzi differ property is reported` plants `systemProperty("app.cash.paparazzi.differ", "mssim")` in a temp copy and expects the hit; the existing `T-DS-3: no tolerance literals…` stays green.

### 5.2 Harness result sidecar (Step 2)

The sidecar is the only way the test verdict and value reach the report. **Only screen snapshots write one**; gallery goldens and the canary do not (I13).

- **Location:** `ReferenceSnapshotHandler`, which receives name and mode — not `StagedSnapshotHandler` (shared with `GoldenSnapshotHandler`, does not know the name). Reference computes `private val artifact: File?` once (today's lambda body), passes `artifactFile = { artifact }` to Staged and keeps `name`/`mode` as `private val`. `newFrameHandler` wraps the frame handler Staged returns: `try { delegate.handle(image) } catch (t: Throwable) { write(passed = false, message = t.message); throw t }`, then `write(passed = true)`. The **same instance** is rethrown (`AssertionError`; the size check's `IllegalStateException` gives `passed = false`, `value = null`). No sidecar when `artifact` is null (canary). Path: `artifact` with `.png` → `.result.json`.
- **T-DS-3 trap:** no `MAX_PERCENT_DIFFERENCE =` / `maxPercentDifference = <digit>` in new Kotlin code (read the constant, never assign or name-pass it).
- **Shape:**
  ```json
  { "platform": "android", "name": "SanGefaelle", "mode": "light"|"dark",
    "passed": true|false, "message": "<verbatim failure message or empty>",
    "value": <number|null>, "required": <number>, "unit": "percent" }
  ```
- **`value`:** pure function `ReferenceSnapshotHandler.result(passed, message)`: `/Images differ \(by ([0-9]+[.,][0-9]+)%\)/`, comma replaced by a dot (JVM locale `de_DE`). Null on pass. **The parse never changes the verdict.**
- **`required`:** read from `MAX_PERCENT_DIFFERENCE`, never a new literal.
- **Write failure:** the `IOException` propagates. `--all` reports a missing sidecar as `missing-result`.
- **Tests (red-first):** `ReferenceSnapshotHandlerTest` (extended): `sidecarWrittenOnFailureAndSameErrorRethrown`, `sidecarOnPassHasNullValue`, `sidecarParsesDecimalComma`, `canaryWithoutArtifactWritesNoSidecar`. `GoldenSnapshotHandlerTest` (extended): `goldenWritesNoSidecar`.

### 5.3 `scripts/diff-report` whole-run mode (Step 2)

New modules (reuse if already on the branch; `feat/m7` has both):
- `scripts/lib/offbytwo.mjs` exports `offByTwoPercent(refRGBA, actRGBA, w, h) -> number`. Per pixel: if `max(|ΔR|,|ΔG|,|ΔB|) ≤ 2`, skip; otherwise `delta += |ΔR|+|ΔG|+|ΔB|`. Result `delta·100/(w·h·3·256)`. Alpha ignored.
- `scripts/lib/band-shift.mjs` exports `bandShift(refRGBA, actRGBA, w, h, { bandHeight, maxShift = 6 }) -> { positionShare, bandsImproved, bands: [{ y0, best, dev0, devBest }] }`. Per band, `dev(s)` is the mean absolute RGB deviation of `act` rows `y+s` against `ref` rows `y`; `best = argmin_s dev(s)` for s ∈ [−6, 6]; `positionShare = Σ(dev0 − devBest) / Σ dev0` (0 when Σ dev0 = 0); `bandsImproved` = bands with `devBest ≤ 0.8 · dev0`. `bandHeight` for Android is `ceil(2532/14)` px (14 bands); for iOS 60 px.

`scripts/diff-report.mjs` gains:
- **`--all [--platform ios|android]`:** the expected set is `parseScreens(design/screens.md)` × {light, dark} × the given platform (both when `--platform` is absent; this plan always passes `--platform android`). Per row:
  ```json
  { "nr", "name", "platform", "mode", "status": "ok"|"missing-actual"|"missing-result"|"size-mismatch",
    "test": { "passed", "value", "required", "unit" } | null,
    "percentDifferent", "offByTwoPercent" (android only, else null),
    "positionShare", "bandsImproved", "actualSha256" }
  ```
  `actualSha256` is the SHA-256 (`node:crypto`) of the decoded actual RGBA. Status precedence: `missing-actual` > `size-mismatch` > `missing-result` > `ok`. The expected set comes from a `screens` option on `run()` (default `parseScreens` of `design/screens.md`, which takes the markdown text).
  Output: with `--platform p`, `reports/summary-<p>.json` (`{ generatedAt, rows: [...68], totals: { <p>: {green, red, missing} } }`) and `reports/summary-<p>.md`; without, `reports/summary.json`/`.md` over 136 rows. Table columns: Nr · Screen · Plattform · Modus · Test (✓/✗ + value) · Diff % · OffByTwo % · Lagenanteil · Bänder, ordered by `nr`, platform, mode. The per-platform panels and the existing `summary.json` are still written.
- **`--baseline <path>`** (only with `--all`): `changed: [{ name, platform, mode, field, before, after }]` for every row whose `actualSha256`, `test.passed`, `test.value`, `percentDifferent` or `offByTwoPercent` differs (`|a−b| > 1e-6`). Rows of a platform outside the current `--platform` are ignored.
- **`--unstable <path>`** (only with `--baseline`): rows listed in its `changed` are compared without `actualSha256`.
- **Exit codes:** existing unchanged (1 reference missing, 2 usage or `PngError`); **3** when an expected actual or sidecar is missing under `--all`; **4** when `--baseline` finds changes. Red tests alone do not change the exit code.
- **Usage errors (exit 2):** `--all` with `--name`/`--mode`/`--actual`; `--baseline` without `--all`; `--unstable` without `--baseline`; an unreadable baseline/unstable file. Each prints `diff-report: <reason>`.

**Tests** (extend `scripts/test/diff-report.test.mjs`, synthetic PNGs via `lib/png.mjs`, red-first; skip any that already exist):
- `offByTwo ignores pixels within 2 levels on every channel`
- `offByTwo sums all three channels of a differing pixel`
- `offByTwo on a known 4×4 pair equals the hand-computed percentage`
- `bandShift finds a 3 px vertical shift and reports positionShare 1 for a pure shift`
- `bandShift reports 0 for pure noise`
- `--all reports missing-actual for an absent screen and exits 3`
- `--all reads the sidecar verdict and value`
- `--all --platform android writes summary-android.md with 68 rows for a full synthetic tree` (stub screens injected through `screens`)
- `--all --platform android ignores missing ios actuals`
- `--baseline lists a changed value and exits 4; identical inputs exit 0`
- `--baseline reports actualSha256 for a one-pixel change that keeps every count`
- `--unstable skips the hash of listed rows but still reports their value changes`
- `--all reports size-mismatch before missing-result`
- `usage errors exit 2`

**Calibration check (Step 2, by the implementer):** on the Step 0 artifacts, the replica's `offByTwoPercent` agrees with every red sidecar `value` within 0.001 and is ≤ 0.5 on every green one. `docs/m7-offene-punkte.md` § "Kalibrierung der Replik" already records a pass on `feat/m7`; re-run it only if `offbytwo.mjs` or the Android harness changed. Record the result.

### 5.4 Screen catalog and link host (Step 3)

New `android/app/src/test/java/de/baupilot/snapshot/ScreenCatalog.kt`:
```kotlin
object ScreenCatalog {
    val all: List<Pair<String, AppNavigationState>>   // 34, design/screens.md order
    fun state(name: String): AppNavigationState      // throws IllegalArgumentException("unknown screen <name>")
}
```
- The 68 snapshot classes call `ScreenCatalog.state("<Name>")`.
- **Link and placeholder tests** (`MainActivity` has no start-state hook) use a test-only `LinkHost` in the `de.baupilot.app` test sources: rule `createAndroidComposeRule<ComponentActivity>()` (the copied `systemBack` helper needs `rule.activityRule`; the activity comes from `ui-test-manifest`, already `debugImplementation`); content `var state by remember { mutableStateOf(start) }`; `BackHandler(enabled = state.canPop) { state = state.pop() }` copied from `BaupilotRoot.kt:38`; then `BaupilotAppContent(state, onStateChange = { state = it }, data = TestAppData.load())` (import from `de.baupilot.snapshot`). No production hook.

**Tests:** `ScreenCatalogTest`: `has34NamesMatchingReferences`, `unknownNameThrows`.
**Equivalence:** after Step 3, `diff-report --all --platform android --baseline build/m7/android/baseline/summary.json` must report **no changes**.

### 5.5 Link coverage guard and link tests (Step 5)

**`scripts/lib/links.mjs`** (reuse if present):
- `parseLinks(markdownByMilestone) -> [{ from: nr, to: nr }]`. Reads `### <nr> · <title>` headings and `- Links:` lines with `(Nr. N)`, deduplicates the per-platform lines and throws `links: screen <nr> has differing iOS/Android link lines` if they differ.
- `validateCoverage(pairs, manifest, sources) -> string[]`, checking only platforms whose `platforms` flag is true: `missing pair 3→2`, `unknown pair 3→9`, `android test LinkCoverageUiTest#x not found`, `placeholder <id> has no android test`.

**`scripts/link-coverage.json`:**
```json
{ "platforms": { "ios": false, "android": true },
  "links": [ { "from": 3, "to": 2, "element": "tab.home", "android": "LinkCoverageUiTest#scheduleTabs" } ],
  "placeholders": [ { "screen": 1, "element": "login.forgot", "android": "PlaceholderUiTest#login" } ] }
```
(If the iOS plan already ran, entries carry an `ios` field too and `"ios"` is `true`: leave both untouched.) Existing tests are referenced where they already cover a pair. `sources` for Android are read from `android/app/src/test/java/de/baupilot/app/*.kt` (`fun <name>(`), within the named class's file.

**`scripts/test/links.test.mjs`:**
- `parseLinks finds 100 pairs over 34 screens in M2–M6`
- `parseLinks throws on differing platform lines` (fixture)
- `validateCoverage reports a missing pair, an unknown pair and a missing test method` (fixture)
- `validateCoverage ignores a disabled platform` (fixture)
- `the real manifest covers every pair and placeholder for every enabled platform` (real tree; red until Steps 5–6 finish)

**New test class:** `android/app/src/test/java/de/baupilot/app/LinkCoverageUiTest.kt`, hosted by `LinkHost` (D5). It taps the element and asserts the target screen identifier; back links assert the screen below; every X → 10 tab test also asserts `inspections.view.trades`. Back/close element: `nav.close` on 12, 13, 21, 22, 26, 32, 34. Helpers copied into the class (project convention).

| Test (camelCase) | Pairs | Element(s) |
|---|---|---|
| `scheduleTabsOpenEveryTab` | 3→2, 3→4, 3→6 | `tab.home`, `tab.profile`, `tab.documents` |
| `scheduleAskOpensScreedTrade` | 3→5 | `schedule.ask.0` (fixture `schedule.asks[0].target = trade/screed`) |
| `profileTabsOpenEveryTab` | 4→3, 4→6, 4→10 | tabs |
| `documentsTabsOpenScheduleAndInspections` | 6→3, 6→10 | tabs; 6→10 asserts `inspections.view.trades` |
| `inspectionsTabsOpenHomeScheduleProfile` | 10→2, 10→3, 10→4 | tabs |
| `carpentryRowOpensZimmererAndBack` | 10→19, 19→10 | `inspections.trade.carpentry`, `nav.back` |
| `elementsBackReturnsToInspections` | 11→10 | `nav.back` |
| `stepCloseReturnsToElements` | 12→11 | `nav.close` |
| `defectsViewTabsOpenEveryTab` | 15→2, 15→3, 15→4 | tabs from the Mängel view (15→6 is covered already) |
| `electricalBackReturnsToInspections` | 20→10 | `nav.back` |
| `panelDefectOpensDefectForm` | 22→13 | the "Löst nicht aus" button's identifier |
| `screedBackReturnsToInspections` | 28→10 | `nav.back` |
| `flatnessDefectOpensDefectForm` | 29→13 | `flatness.defect` |
| `paintingBackReturnsToInspections` | 33→10 | `nav.back` |
| `paintingRoomDefectOpensDefectForm` | 34→13 | "Mangel" |
| `homeTradeRowsOpenTheirTargets` | 2→5 (all rows) | `home.trade.<id>` for every Home trade row with a target |
| `homeDocumentTilesOpenDocuments` | 2→6 (all tiles) | `home.doc.<n>` |
| `fixtureRowsOpenTheirRoutes` | rows of 17, 20, 24, 28, 31, 33 with non-null routes | data-driven literal table with fixture-line comments; the guard keeps it honest |

Rule for the expected identifier: `"screen." + route.replace("/", ".")`, except `trade/x/compare` → `screen.compare.x`, `defects` → `screen.inspections` plus `inspections.view.defects` selected, and tab routes → `screen.<tab>`.

- **Identifiers added in production code (D20)** — rendering must not change; `--baseline` pins it: `home.trade.<tradeId>` (`HomeScreen.kt:79`), `home.doc.<n>` (:89), `schedule.ask.<n>` (`ScheduleScreen.kt:71–74`), identifiers for the 22 defect button if missing.

### 5.6 Placeholder tests (Step 6)

New `android/app/src/test/java/de/baupilot/app/PlaceholderUiTest.kt`, hosted by `LinkHost`. One test per screen with placeholders. For each placeholder: tap, then `assertAbsent` of every other `screen.*` tag and the current one still present, no tab changed. List in §6.2. Existing tests that already cover one are referenced in the manifest, not duplicated.

### 5.7 Semantics changes (Step 7)

| Unit | File | New contract | Test |
|---|---|---|---|
| `SlopeResult` | `android/…/designsystem/components/SlopeResult.kt` | `semantics(mergeDescendants = true)` + `testTag("slope.result")`; `SlopeSchema` gets `clearAndSetSemantics {}`. Label = percent, verdict (D7) | `LaterTradesUiTest`'s `assertText` gets a `useUnmergedTree = true` variant for the child tags, **and** a merged-tree assertion `onNodeWithTag("slope.result").assert(hasText("0,75 %") and hasText("Zu flach · Soll ≥ 1,0 %"))`, then "1,00 %" / "Ausreichend · Soll ≥ 1,0 %". New `SlopeResultSemanticsTest`: the merged node carries none of the sketch labels |
| `TileGrid` | `TileGrid.kt` | One element (D8): description `gridAria`, stateDescription = hollow-tile arias joined with ", ", tag `tiles.grid`; tiles without own semantics and click | `TileGridSemanticsTest` (Robolectric): one node with description and state, no `Role.Button` children. `LaterTradesUiTest` tile test: coordinate tap inside `tiles.grid` at tile 3's centre (column 2, row 0 of 8 × 3, gap 4), same "nothing happens" assertion (I11) plus unchanged state |
| `TimerRing` | `TimerRing.kt:38–41` | `clearAndSetSemantics { contentDescription = label; role = Role.Image }` | `TimerRingSemanticsTest`: exactly one node carries the description, no child text node reachable in the merged tree |
| `SignaturePad` | `SignaturePad.kt:58` | `stateDescription` "leer" / "unterschrieben" from `ScreenCopy` (D11) | `InspectionFlowUiTest.signatureStateFollowsStrokes` (new; unmerged tree, `SemanticsProperties.StateDescription`); new `ScreenCopyTest` pins the two strings |
| SanGefaelle section tile | `SanGefaelleScreen.kt:91` | Label = visible texts joined with ", " ("A3, jetzt") (D12) | `LaterTradesUiTest.sectionTileLabelMatchesVisibleText` |
| Zeitplan, HeatingBarChart | unchanged | — | `ScheduleChartSemanticsTest`: exactly one node with role Image and the fixture `ariaLabel` |

### 5.8 Accessibility audit (Step 8)

- **Spike first (Step 8a).** Add `testImplementation("androidx.compose.ui:ui-test-junit4-accessibility")` (version from the BOM) to `android/gradle/libs.versions.toml` + `app/build.gradle.kts`. Write `AccessibilityAuditTest` for **one** screen (Login) under Robolectric with `createComposeRule`, `enableAccessibilityChecks()`, then `onRoot().tryPerformAccessibilityChecks()`.
  - **Pass criteria:** it runs, and a planted defect (a clickable Box without a label, in a test-only composable) makes it fail.
  - If either fails, remove the dependency and use the fallback. Record the spike result in the doc.
- **`AccessibilityAuditLightTest` / `AccessibilityAuditDarkTest`** (`android/…/app/AccessibilityAuditTest.kt`, one shared abstract base), each parameterised over `ScreenCatalog.all` (`ParameterizedRobolectricTestRunner`), with class-level `@Config(qualifiers = "w411dp-h3000dp")` and `"w411dp-h3000dp-night"`. **Not** `RuntimeEnvironment.setQualifiers("+night")`: the activity already exists when the test body runs, so `isSystemInDarkTheme()` (`Tokens.kt:19`) would stay light. Each test asserts inside `setContent` that `LocalConfiguration.current.uiMode and UI_MODE_NIGHT_MASK` matches its mode.
  - **ATF path:** fail on ERROR. Allowlisted results are suppressed through ATF's result matcher with the shape `(screen, check, element, reason)`. Touch-target and text checks are suppressed with reason `pending Step 9/10` until those steps remove the suppression.
  - **Fallback path:** `SemanticsAudit.check(root: SemanticsNode): List<Finding>` in test sources. Rules: (a) every node with `OnClick` has a non-empty merged label; (b) every `Role.Image` node has a ContentDescription; (c) every `OnClick` node's `touchBoundsInRoot` is ≥ 48 × 48 dp; (d) no two `OnClick` nodes share identical bounds. `Finding(rule, testTag?, label?, boundsDp)`. Unit tests in `SemanticsAuditTest` with a planted composable per rule.
  - **Stale entries:** the base class asserts at the end of the run that every allowlist entry matched at least once.
- **Contrast:** ATF's contrast check needs a screenshot and may be skipped under Robolectric. Record whether it ran. If not, note in the doc that Android contrast is covered only by the manual Scanner run (operator step).

### 5.9 Touch-target walk (Step 9)

`TouchTargetTest` (parameterised over `ScreenCatalog.all`, `w411dp-h3000dp`, light only):
- `onAllNodes(hasClickAction(), useUnmergedTree = true)`; for each node `touchBoundsInRoot.width ≥ 48.dp && height ≥ 48.dp`. The failure message lists `testTag`/label and size.
- It stays a separate explicit test even with ATF (the M7 task names an automated touch check).
- **Expected:** green; FlBad tiles never had a click action (D8 only removes `Role.Button`), and the no-op `VerdictCell` clickables (48 × 40, `ObjectCheckRow.kt:115–134`) have touch bounds that reach 48.
- **Fixes for real misses:** enlarge the touch bounds without changing the visible size (e.g. `Modifier.minimumInteractiveComponentSize()` only where it does not shift layout, proven by `--baseline`), plus a metric test; if a target cannot reach 48 without overlapping a neighbour, it is a documented gap with its measured size.

### 5.10 Large text (Step 10)

**Renders (artifacts, not compared):** `LargeFontRenderTest` (Paparazzi via `baupilotPaparazzi(handler, mode, fontScale = 1.3f)`, a new defaulted parameter applied to `DeviceConfig`, with an artifact-writing handler like `NoopSnapshotHandler`) writes `build/snapshots/android-large/<Name>_130.png`. A single parameterised class if Paparazzi accepts it under `forkEvery = 1`; otherwise 34 generated classes like `ScreenSnapshotTests.kt`.

**Automated check:** `LargeFontLayoutTest`, parameterised over `ScreenCatalog.all` with `@Config(fontScale = 1.3f, qualifiers = "w411dp-h3000dp")` and `@GraphicsMode(GraphicsMode.Mode.NATIVE)`.
- (1) For every node with `SemanticsActions.GetTextLayoutResult`: `!didOverflowHeight && !didOverflowWidth`, unless the node is an intended one-line ellipsis (allowlist by tag with the prototype line).
- (2) Pairwise overlap of text nodes in the unmerged tree, excluding ancestor/descendant pairs, 1 dp² tolerance.
- **Fallback:** if NATIVE graphics does not measure text under Robolectric 4.17 (check one screen first), run (1) and (2) inside Paparazzi via `onGloballyPositioned`/`onTextLayout` collectors. Record which one is used.

**Fixes:**
- **Line boxes:** `robotoLineBox` / `cssLineHeightBaseline` keep `lineBox(size).sp.roundToPx()` when `fontScale == 1f`; otherwise box = `round(lineBox(size) / size × size.sp.toPx())` (baseline likewise), matching Compose's proportional line height under non-linear scaling.
- **Fixed heights:** text-bearing `Modifier.height(N.dp)` containers (list in §2.5; grep `\.height\(` in `designsystem/` and `screens/`, decide per site) become `heightIn(min = N.dp)` with the existing `contentAlignment`/`verticalArrangement`. Intended one-line ellipsis texts stay `maxLines = 1`. The hash gate proves scale 1 is identical.
- **Unit tests (red-first):** `LargeFontLineBoxTest` (Robolectric, `@Config(fontScale = 1.3f)`): `lineBoxMatchesComposeLineHeightAt130` and `lineBoxUnchangedAtScale1` (exact equality with today's expression for every size in use). Not in the pure-JVM `TypographyTest`; no "1.3 × box" assertion.

**Visual review:** open every `android-large` PNG (34) and record one row per screen in the doc (`keine Überlappung` / what was fixed / what remains).

**Proof the default rendering is unchanged:** after Step 10, `diff-report --all --platform android --baseline` → exit 0, and `Gallery*SnapshotTest` stay green without re-recording.

### 5.11 Snapshot levers (Step 11)

Every lever is spiked on a scratch commit, measured with the full Android screen-snapshot run plus `diff-report --all --platform android --baseline <current kept state>`, and judged by D17. Kept → committed with numbers; not kept → reverted, numbers under "Versucht" in the doc.

- **11a: `TextMotion.Animated`.** Set `textMotion = TextMotion.Animated` in the central `BaupilotText` `TextStyle` (`Typography.kt:173–188`, the fourth overload the other three delegate to) and the separate styles in `TextField.kt:78`, `SearchBar.kt:70`, `TextArea.kt:59`. Check side effects: the `wholeDpWidth` chip approximation (M3) and the D14 weight pairs (M6) were calibrated on whole-pixel advances; metric tests that fail are re-derived from the prototype (cite lines), never re-numbered. Compare any wrap change with the reference image.
- **11b: near-threshold rows** (the ≤ 0.55 % rows of the Step 0 baseline, whatever they are after 11a). Per row, use the `reports/android/<Name>_<mode>.png` panel and the band list to find the worst band, compare that region with the prototype's inline styles, and fix only a measurable, prototype-backed layout cause (box edge, baseline, advance). Pure glyph raster is left and justified.
- **11c: G1** (`GalleryDarkSnapshotTest`, `GalleryElectricalSteps2`): skip if Step 0 shows the gallery green; otherwise isolate the page, check against the prototype and re-record the golden only for a real component change (D17), or investigate light-vs-dark noise. The gallery suite must end green.

**Exit of Step 11:** the doc table lists, for each of the 68 rows: test value, ✓/✗, Lagenanteil, cause class (`Glyphen-Raster (R1)`, `Layout <component>`, `Umbruch`), and for every ✗ the levers tried and their effect. A ✗ row with cause "Layout" needs a named reason why the fix was not done.

---

## 6. Inventories

### 6.1 Link pairs (100) — target state

Every pair is covered on Android, listed in `scripts/link-coverage.json` (`android` field), and checked by the guard. The new entries are in the §5.5 table; the others map to existing tests.

**Intended differences from the prototype, kept and referenced in the manifest's `note` field:** the Gewerk Hahn offer has no action (m3 D5); "Sichern" opens the sign-off (m4 D3); M5/M6 defect openers return to the opener (m5 D4).

### 6.2 Placeholders — a "no action" test on Android

- **Login:** "Passwort vergessen?" (new); "Neues Bauvorhaben anlegen".
- **Home:** "Alle", "Dokument" (exist).
- **Zeitplan:** ask card "Fliesen & Maler" (`target = null`).
- **Profil:** "Bearbeiten".
- **Gewerk:** Lorenz offer (exists); Hahn offer (D5).
- **Dokumente:** hint card, "Nach Gewerk", invoice/contract/plan rows, "Hochladen".
- **DokViewer:** "Gewerk oder Typ ändern".
- **Vergleich:** Hahn column (new), "Erinnern", "Rückfrage", "Wagner beauftragen".
- **Mitteilungen:** "Termin bestätigt" (exists).
- **AbnGewerke:** Dachdecker row (exists), "Vorlagen".
- **AbnElemente:** "Alle 6" ×2, rows F1/F3/F7/F9.
- **AbnMängel:** "Vorlagen", "Behobene Mängel".
- **Erd, Zimmerer:** "PDF".
- **Null-route rows:** Rohbau 0.0, 0.1, 1.1, 1.2, 1.3, 2.0, 2.1; ElÜbersicht 1.1, 1.2, 2.0, 2.1; SanÜbersicht 0.2, 1.0, 1.1, 2.1; EsÜbersicht 0.0, 0.1, 0.3; FlÜbersicht 0.0, 0.1, 1.1, 1.2, 1.3, 2.0; MaÜbersicht 0.1, 0.2, 1.0, 1.1, 1.2.
- **Buttons "Ohne Aktion" from m4/m5/m6:** photo, "Speichern", "In Ordnung", "Keine Hohlstellen", "Tag 6 eintragen", SanBad tabs and ✓/✕, MaRaum wall chips, Gefälle section tiles, Google sign-in. Covered in `PlaceholderUiTest`, listed in the manifest's `placeholders` with `kind: "button"`.

Confirm each against the Android prototype's `href="#"` / `renderVals()` while building the manifest. An element found that is not in this list is added; one the prototype shows with a real `href` moves to §6.1 (and is reported).

---

## 7. Steps

### Step 0: Preflight (D1)
1. Clean worktree on `feat/m7-android`.
2. Run the blocks of §12 (scripts, design checks, Android). Android uses JDK 21 and `--continue`.
3. Run `scripts/diff-report --platform android` (existing mode).
4. Write the counts into `docs/m7-android-offene-punkte.md` § "Baseline (Step 0)", observed values only. Compare with the Android rows of `docs/m7-offene-punkte.md` and note differences.
5. **Step 0.5 (after Step 2 exists):** re-run the Android screen-snapshot suite, run `diff-report --all --platform android` and copy `reports/summary-android.json` to `build/m7/android/baseline/summary.json`. Its values must equal Step 0. Run the suite a second time with `--baseline build/m7/android/baseline/summary.json` and copy the result to `build/m7/android/baseline/unstable.json`: its `changed` rows are non-deterministic, listed in the doc, and every later gate passes `--unstable build/m7/android/baseline/unstable.json`. If `changed` lists a value change rather than only a hash change, stop and record it. (The combined run on `feat/m7` found 0 unstable rows.)

### Step 1: Tolerance pin (red-first)
Add the T-DS-3 fixture test (red), then the scan pattern (green).

### Step 2: Sidecar and `diff-report --all` (red-first)
1. Node tests of §5.3 first (red), skipping existing ones.
2. Implement or reuse `offbytwo.mjs`, `band-shift.mjs`; add `--all --platform`, `--baseline`, `--unstable`.
3. Sidecar unit tests red, then the sidecar.
4. Calibration check (§5.3), recorded.
5. Baseline (Step 0.5).

### Step 3: Screen catalog (D4)
Add `ScreenCatalog` and its test, then switch the 68 snapshot classes. **Gate:** `diff-report --all --platform android --baseline … --unstable …` → exit 0 (every later gate uses the same two files).

### Step 5: Links (red-first through the guard)
1. Add or reuse `links.mjs`; add `platforms` to the manifest with `"android": true`; add the existing Android mappings and the guard test. Red with the missing pairs.
2. Add the identifiers (D20).
3. Write the `LinkCoverageUiTest` methods until guard and class are green.
4. **Gate:** `--baseline` exit 0.

### Step 6: Placeholders
Extend the manifest's `placeholders` (red guard), add `PlaceholderUiTest`, then green. **Gate:** as Step 5.

### Step 7: Semantics (red-first)
1. Change the UI tests to the new contracts first (red); add the semantics tests.
2. Change the components.
3. **Gate:** `--baseline` exit 0. Semantics modifiers draw nothing; a changed row means the change was not semantics-only.

### Step 8: Audit
1. Spike 8a, recorded.
2. Add the audit tests (§5.8), red at first.
3. Fix each finding at its cause; allowlist only rule-blocked findings (D14), each with a reason.
4. **Gate:** both audit tests green; allowlist recorded.

### Step 9: Touch targets
Add the walk, fix per §5.9, add a metric test for every enlargement, remove the `pending Step 9` suppression. **Gate:** walk and audits green; `--baseline` exit 0.

### Step 10: Large text
1. `LargeFontLineBoxTest` (red).
2. Render test and `LargeFontLayoutTest`.
3. Fixes (§5.10).
4. Visual review, recorded.
5. Remove the `pending Step 10` suppression. **Gate:** checks and audits green; `--baseline` exit 0; gallery green without re-recording.

### Step 11: Levers (D17)
11a, 11b, 11c in order, each measured. Store the summary after each kept lever (`build/m7/android/after-11a/summary.json`, …); keep the Step 0 baseline for the final comparison.

### Step 14: Docs (D21)
Write `docs/m7-android-offene-punkte.md`: Baseline; Entscheidungen (D1–D21 short, Android-relevant); Bekannte Lücken (R-entries of §8 with outcomes); Messwerte der 68 Android-Snapshots (from `reports/summary-android.md`, with cause class and levers tried); the large-text review table; the audit allowlist and spike result; link manifest summary (100/100 Android, placeholder count); Testzahlen; Invarianten (§11); Verifikation; operator checklist for the manual Accessibility Scanner run. Add a README section "M7 · Android: Screen-Snapshots, UI-Tests und Barrierefreiheit" with the commands from §12.

### Step 15: Verification
Run §12 in full and write the results into "Verifikation", observed values only.

---

## 8. Risks and edge cases

| # | Risk | Handling |
|---|---|---|
| R2 | `TextMotion.Animated` changes wrapping or breaks calibrated metric tests (M3 `wholeDpWidth`, M6 D14 weights) | D17; failing metric tests re-derived from the prototype with a citation; reverted if net-negative. |
| R3 | `ui-test-junit4-accessibility` does not run under Robolectric, or cannot be fetched | Spike 8a with a planted defect; fallback `SemanticsAudit`. |
| R4 | The audit reports contrast issues on design-token colours | Allowlist with reason (D14); AC3 "met" only with an empty allowlist. **Known gap if it occurs.** |
| R8 | Robolectric does not measure text realistically | `@GraphicsMode(NATIVE)`; one screen first; Paparazzi fallback. |
| R9 | `touchBoundsInRoot` clipped by the scroll viewport | Tall window `h3000dp` (D19). |
| R13 | Coordinate tap on the merged tile grid misses tile 3 | Compute from the grid bounds (8 × 3, gap 4); also assert the state description and the screen set are unchanged. **Known weakness:** a no-op tap cannot prove it hit the tile. |
| R14 | Paparazzi parameterised class + `forkEvery = 1` | Generate per-screen classes. |
| R15 | Run time: 34 × 2 audit, 34 touch, 34 large-font classes | Accepted; record durations. |
| R16 | The baseline goes stale between steps | Every gate compares with the latest kept baseline; the final run with Step 0. |
| R17 | The link manifest drifts from the milestone docs | `links.test.mjs` parses the docs on every run. |
| R18 | D8 overrides part of a user-answered M6 decision (D12) | Recorded; behaviour (no action) unchanged. |
| R19 | D11 adds two non-prototype words | Recorded as the only new text, in `ScreenCopy` with the reason; non-visible. |
| R20 | The manual Accessibility Scanner run cannot be done by an agent | Operator checklist; **known gap until a person runs it.** |
| R21 | Sidecar parse breaks with a library update, or the locale changes the decimal mark | `value` null on non-match; comma-tolerant regex; verdict from `passed` only. |
| R22 | A screen renders non-deterministically | Step 0.5 double run; value-only rows. |
| R23 | Non-linear font scaling: line-box helpers disagree with Compose at 130 % | Font-proportional box above scale 1, today's path at 1; `LargeFontLineBoxTest`. |
| R24 | The iOS plan runs on its own branch at the same time and edits the same `scripts/` files or the manifest | §5.0 rules (additive only, own platform flag and fields); merge `feat/m7-ios` and `feat/m7-android` one after the other and re-run `node --test test/` after the second merge. |

---

## 9. Tests

### scripts (`cd scripts && node --test test/`)
- `design-system.test.mjs`: **T-DS-3: a planted paparazzi differ property is reported** (red-first).
- `diff-report.test.mjs`: the §5.3 list.
- `links.test.mjs`: parseLinks finds 100 pairs over 34 screens; differing platform lines throw; validateCoverage reports missing pair / unknown pair / missing method; disabled platform ignored; real manifest covers every pair and placeholder for every enabled platform.

### Android (`:app:testDebugUnitTest`)
- `ScreenCatalogTest`: 34 names; unknown throws.
- `ReferenceSnapshotHandlerTest` (extended): sidecar on failure + same error rethrown; sidecar on pass; decimal comma; canary writes none. `GoldenSnapshotHandlerTest` (extended): golden writes no sidecar.
- `LinkCoverageUiTest`, `PlaceholderUiTest` (hosted by `LinkHost`).
- `LaterTradesUiTest`: slope merged node (new assertion), tile-grid no-action (changed), section tile label (new).
- `InspectionFlowUiTest.signatureStateFollowsStrokes` (new); `ScreenCopyTest` (new).
- `TileGridSemanticsTest`, `TimerRingSemanticsTest`, `ScheduleChartSemanticsTest`, `SlopeResultSemanticsTest`.
- `AccessibilityAuditLightTest` / `AccessibilityAuditDarkTest` (34 each, mode asserted); `SemanticsAuditTest` (fallback path only).
- `TouchTargetTest` (34).
- `LargeFontLayoutTest` (34); `LargeFontRenderTest` (artifacts).
- `LargeFontLineBoxTest`: `lineBoxMatchesComposeLineHeightAt130`, `lineBoxUnchangedAtScale1`.
- Metric tests re-derived under Step 11a, only if kept.
- 68 `*SnapshotTest` classes via `ScreenCatalog`.

---

## 10. Acceptance mapping

| Criterion | Step | Observable behaviour | Proof |
|---|---|---|---|
| Task 1: UI tests for all links with target; placeholders without action | 5, 6 | Every one of the 100 pairs is tapped and reaches its target; every placeholder tap leaves the screen set unchanged | `links.test.mjs` with `"android": true`; `LinkCoverageUiTest`, `PlaceholderUiTest` green |
| Task 2: labels from aria-label; Zeitplan, Gefälle, Raster one element with description | 7 | TalkBack focuses the slope card, the tile grid and the schedule as one stop each and speaks the description; TimerRing speaks its description | `LaterTradesUiTest` changes, `SlopeResultSemanticsTest`, `TileGridSemanticsTest`, `TimerRingSemanticsTest`, `ScheduleChartSemanticsTest`; audit |
| Task 3: automatic touch-target check | 9 | Every clickable node on every screen is ≥ 48 dp | `TouchTargetTest` |
| Task 4: diff report 34 × 2; fix or justify residue | 2, 11, 14 | `reports/summary-android.md` with 68 rows; every red row has a cause class, a position share and the levers tried | `diff-report --all --platform android` (exit 0, no missing rows); doc table |
| Task 5: large text, nothing overlaps | 10 | At 130 % no text is clipped or overlaps; default size renders unchanged | `LargeFontLayoutTest`; visual-review table; `--baseline` exit 0 |
| AC1: 68/68 Android snapshots green | 11 | Counted by `reports/summary-android.json` `totals` | Screen-snapshot suite + `diff-report --all --platform android`; any remaining red row reported with evidence |
| AC2: all Android UI tests green | 5–10 | All `*UiTest` and Robolectric classes pass | §12 Android block |
| AC3: accessibility audit without errors | 8 | ATF/fallback passes on all 34 screens in both modes | `AccessibilityAuditLightTest` / `DarkTest`; **met only with an empty allowlist**; manual Scanner run is an operator step (R20) |

---

## 11. Invariants

| # | Invariant | Pinned by |
|---|---|---|
| I1 | Tolerance unchanged: 0.5 (`BaupilotPaparazzi.kt:11`), OffByTwo differ (no `app.cash.paparazzi.differ`) | T-DS-3 incl. the new pattern |
| I2 | References and `design/` unchanged; nothing re-recorded against references | `scripts/design-freeze --check`; `git status --porcelain -- design fixtures scripts/icons.json` empty |
| I3 | Steps 3–10 do not change any rendered pixel of the 68 Android screens | `diff-report --all --platform android --baseline … --unstable …` exit 0 at each gate |
| I4 | Placeholders stay visible and do nothing | `PlaceholderUiTest`; I3 |
| I5 | No measure changes without a prototype citation | Every changed numeric layout literal in `android/app/src/main` in Steps 9–11 carries a `// <Prototype>.dc.html:<line>` comment |
| I6 | Colours only through tokens; no fixture/token change | `scripts/lint-hex`, `gen-tokens --check`, `gen-icons --check` |
| I8 | No visible text added; the only new non-visible text is D11 | `git diff` of `ScreenCopy.kt` shows only D11 |
| I9 | The test verdict never depends on sidecar parsing | Sidecar unit tests |
| I10 | No snapshot green at Step 0 turns red | Final `--baseline build/m7/android/baseline/summary.json`: no `passed true → false` |
| I11 | Every existing M2–M6 Android UI test still passes (changed ones only as listed in §9) | §12 |
| I12 | No write to `BackendRepository` | `git diff --stat` on the Android `BackendRepository` file is empty |
| I13 | Gallery goldens and the canary write no sidecar; their verdicts are unchanged | `goldenWritesNoSidecar`, `canaryWithoutArtifactWritesNoSidecar` |
| I14 | Nothing under `ios/` changes | `git diff --stat feat/m7 -- ios` empty |

---

## 12. Verification

Run from the repo root. Android uses JDK 21 (`JAVA_HOME=<jdk21>`) and `--continue`. Record the observed counts; do not copy numbers from this plan.

```sh
cd scripts && node --test test/ && cd ..
scripts/gen-tokens --check && scripts/gen-icons --check && scripts/lint-hex && scripts/design-freeze --check

# Android
cd android && JAVA_HOME=<jdk21> ./gradlew --continue :app:testDebugUnitTest :app:compileReleaseKotlin && cd ..

# Whole-run report and the invariant comparison
scripts/diff-report --all --platform android
scripts/diff-report --all --platform android --baseline build/m7/android/baseline/summary.json --unstable build/m7/android/baseline/unstable.json   # only kept-lever rows may change; no green→red

git status --porcelain -- design fixtures scripts/icons.json   # empty
git diff --stat feat/m7 -- ios                                  # empty
```

**Focused runs:** `--tests 'de.baupilot.app.LinkCoverageUiTest'`, `'*PlaceholderUiTest'`, `'*AccessibilityAuditTest'`, `'*TouchTargetTest'`, `'*LargeFont*'`, `'de.baupilot.snapshot.*SnapshotTest'`.

**Success means:** all non-snapshot Android suites and the scripts are green; the guard reports 100/100 pairs and every placeholder for Android; the audit is green with the allowlist reported; the touch and large-text checks are green; `diff-report --all --platform android` lists 68 rows with no missing row; I1–I14 hold; the 68-row table with a justification for every red row is in `docs/m7-android-offene-punkte.md`.

---

## 13. Handoff

Plan file: `10-11-26-m7-v3-android.md` (split from `10-11-26-m7-v3.md`; the iOS half is `10-11-26-m7-v3-ios.md`). The next phase implements Steps 0–3, 5–11, 14–15 on branch `feat/m7-android`.

## Clarifications (Q&A)

- Split requested by the user: one Android and one iOS plan, each runnable on its own.
