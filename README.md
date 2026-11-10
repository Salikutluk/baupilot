# Baupilot

Native iOS (SwiftUI) and Android (Jetpack Compose) app, built pixel-exact from the frozen design package. Milestone 0 (foundation) provides the token generator, the design freeze, the fixture, the platform skeletons with fonts and data layer, and the snapshot harness. Milestone plan: [`docs/milestones/M0.md`](docs/milestones/M0.md). Open items: [`docs/m0-offene-punkte.md`](docs/m0-offene-punkte.md).

## Layout

```
design/                 frozen design package (tag design-v1; never edited)
docs/                   implementierungsplan.md, milestones/M0.md … M8.md, m0-offene-punkte.md
fixtures/baupilot.json  the only data source until M7 (read through MockRepository)
scripts/                Node ESM, zero npm dependencies
  gen-tokens            sh wrapper → design/tokens.json to Tokens.swift / Tokens.kt
  diff-report           sh wrapper → PNG diff reports
  extract-render-vals   sh wrapper → renderVals() extraction from the prototype
  design-freeze         sh wrapper → checks design/ against design-v1.sha256
  lib/                  shared modules
  test/                 node --test suites
ios/                    XcodeGen project.yml, Baupilot/ (app), BaupilotTests/
android/                Gradle KTS, app/, third_party/roboto/
.github/workflows/ci.yml
```

## Prerequisites

- Xcode 26.3 and XcodeGen: `brew install xcodegen`
- An iPhone 15 simulator on iOS 26.2 (one-time):
  `xcrun simctl create "iPhone 15" com.apple.CoreSimulator.SimDeviceType.iPhone-15 com.apple.CoreSimulator.SimRuntime.iOS-26-2`
- JDK 21. The local default JDK 25 breaks Paparazzi, so pass `JAVA_HOME` explicitly.
- Android SDK 36, with `android/local.properties` containing `sdk.dir=…` (git-ignored)
- Node ≥ 22

## Commands

```sh
# Scripts tests
cd scripts && node --test test/

# Token generation and check
scripts/gen-tokens
scripts/gen-tokens --check

# Design freeze check
scripts/design-freeze --check

# Hex literal lint (no colour literals outside the generated tokens)
scripts/lint-hex

# Icon generation and check
scripts/gen-icons
scripts/gen-icons --check

# iOS tests
cd ios && xcodegen generate && xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15'

# Android tests
cd android && JAVA_HOME=<jdk21> ./gradlew :app:testDebugUnitTest

# Diff reports (output in reports/<platform>/)
scripts/diff-report --platform ios
scripts/diff-report --platform android
```

### Design-system gallery

Record the gallery goldens (run from the repo root). The record run fails by design; the goldens are written, then run the tests again without the record flag.

```sh
# iOS
cd ios && TEST_RUNNER_BAUPILOT_RECORD_GOLDENS=1 xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15' -only-testing:BaupilotTests/GallerySnapshotTests

# Android
cd android && JAVA_HOME=<jdk21> ./gradlew :app:testDebugUnitTest --tests 'de.baupilot.snapshot.Gallery*' -Pbaupilot.recordGoldens=true
```

Launch the gallery (Debug builds only):

- iOS: launch argument `-BaupilotGallery YES`
- Android: `adb shell am start -n de.baupilot/.gallery.GalleryActivity`

### Screen snapshots and UI tests (M2)

The 16 screen snapshots (Login, Übersicht, Zeitplan, Profil; light and dark; iOS and Android) compare the real app content with `design/reference/<ios|android>/<Name>_<light|dark>.png`. They are ordinary tests and must pass; the tolerances never change.

```sh
# iOS: screen snapshots (class ScreenSnapshotTests, in BaupilotTests)
cd ios && xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15' -only-testing:BaupilotTests/ScreenSnapshotTests

# iOS: UI tests (XCUITest target BaupilotUITests: login, tab switching, push hides the tab bar, back)
cd ios && xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15' -only-testing:BaupilotUITests

# Android: screen snapshots (Paparazzi, classes *SnapshotTest in de.baupilot.snapshot)
cd android && JAVA_HOME=<jdk21> ./gradlew :app:testDebugUnitTest --tests 'de.baupilot.snapshot.*SnapshotTest'

# Android: UI test (Compose UI test on Robolectric, JVM only, no emulator)
cd android && JAVA_HOME=<jdk21> ./gradlew :app:testDebugUnitTest --tests 'de.baupilot.app.AppNavigationUiTest'
```

The Android UI test needs Robolectric (`org.robolectric:robolectric`) and `androidx.compose.ui:ui-test-junit4` in `src/test`; both are declared in `android/app/build.gradle.kts`. Under JDK 21 Robolectric needs `--add-exports=java.base/jdk.internal.access=ALL-UNNAMED`, which `tasks.withType<Test>` sets. Robolectric and Paparazzi do not share one JVM, so `testDebugUnitTest` runs with `forkEvery = 1` (each test class in its own JVM; the task takes about 1.5 minutes longer). Details: [`docs/m2-offene-punkte.md`](docs/m2-offene-punkte.md). No emulator is needed locally or in CI.

### Screen snapshots and UI tests (M3)

M3 adds five screens (Gewerk, Dokumente, Dokumentenansicht, Angebote vergleichen, Mitteilungen) to `ScreenSnapshotTests` on iOS and to the `*SnapshotTest` classes on Android: 5 screens × 2 platforms × light/dark = 20 snapshots against `design/reference/`. The tolerances never change. Measured results and decisions: [`docs/m3-offene-punkte.md`](docs/m3-offene-punkte.md).

```sh
# iOS: screen snapshots (the five M3 screens are part of ScreenSnapshotTests)
cd ios && xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15' -only-testing:BaupilotTests/ScreenSnapshotTests

# iOS: trade flow UI tests
cd ios && xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15' -only-testing:BaupilotUITests/TradeFlowUITests

# Android: screen snapshots
cd android && JAVA_HOME=<jdk21> ./gradlew :app:testDebugUnitTest --tests 'de.baupilot.snapshot.*SnapshotTest'

# Android: trade flow UI test (Compose UI test on Robolectric)
cd android && JAVA_HOME=<jdk21> ./gradlew :app:testDebugUnitTest --tests 'de.baupilot.app.TradeFlowUiTest'
```

To record a screen golden of a gallery page you own, use the record flags above (`TEST_RUNNER_BAUPILOT_RECORD_GOLDENS=1` on iOS, `-Pbaupilot.recordGoldens=true` on Android), then view the image and run again without the flag. The screen snapshots compare against `design/reference/` and are never recorded.

### Screen snapshots and UI tests (M4)

M4 adds the window inspection flow (Abnahme-Kernflow) with six screens: Abnahmen je Gewerk (`AbnGewerke`), Fenster – Elemente (`AbnElemente`), Prüfschritt F8 (`AbnPruefung`), Mangel erfassen (`AbnMangel`), Abnahme & Unterschrift (`AbnSignatur`) and Offene Mängel & Fristen (`AbnMaengel`). 6 screens × 2 platforms × light/dark = 24 snapshots against `design/reference/`, in `ScreenSnapshotTests` (iOS) and the `Abn*SnapshotTest` classes (Android). The tolerances never change. Measured results, decisions and known gaps: [`docs/m4-offene-punkte.md`](docs/m4-offene-punkte.md).

| Screen | Route |
|---|---|
| Abnahmen je Gewerk | `inspections` (Tab 3 root, view "Gewerke") |
| Fenster – Elemente | `inspection/windows` |
| Prüfschritt F8 | `inspection/windows/{el}/step/{n}` |
| Mangel erfassen | `defect/new` |
| Abnahme & Unterschrift | `inspection/{id}/sign` (`{id}` is the trade id, `windows`) |
| Offene Mängel & Fristen | `defects` (Tab 3 root, view "Offene Mängel") |

The Abnahmen tab root has two views, "Gewerke" and "Offene Mängel", switched by the "Ansicht" segment control; the selected view is part of the navigation state. The target `defects` (for example from a notification, or "Protokoll abschließen") selects the Abnahmen tab, clears its stack and shows the Mängel view. Other trades' rows open a pushed placeholder (their screens come in M5/M6); Dachdecker has no route and no action.

**How to reach the signature screen (decision D3):** no prototype links into Abnahme & Unterschrift, but the flow Elemente → Prüfschritt → Mangel → Unterschrift → Offene Mängel must be clickable. "Sichern" on Mangel erfassen therefore opens the sign-off on top of the Elemente screen (`[…, inspection/windows, inspection/windows/sign]`); the signature screen's back button returns to Elemente. "Abbrechen" (iOS) / the close button (Android) returns to the screen below. This is a plan decision, not a prototype link. Nothing is persisted: "Sichern", severity, deadline, the e-mail switch, the signature and "Protokoll abschließen" change only UI and navigation state (M8).

```sh
# iOS: screen snapshots (the six M4 screens are part of ScreenSnapshotTests)
cd ios && xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15' -only-testing:BaupilotTests/ScreenSnapshotTests

# iOS: inspection flow UI tests (including the acceptance flow Elemente → Prüfschritt → Mangel → Unterschrift → Offene Mängel)
cd ios && xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15' -only-testing:BaupilotUITests/InspectionFlowUITests

# Android: screen snapshots
cd android && JAVA_HOME=<jdk21> ./gradlew :app:testDebugUnitTest --tests 'de.baupilot.snapshot.*SnapshotTest'

# Android: inspection flow UI test (Compose UI test on Robolectric)
cd android && JAVA_HOME=<jdk21> ./gradlew :app:testDebugUnitTest --tests 'de.baupilot.app.InspectionFlowUiTest'
```

### Screen snapshots and UI tests (M5)

M5 adds the early trades and the electrical screens (Erdarbeiten, Rohbau, Zimmerer, Elektro) with eight screens. 8 screens × 2 platforms × light/dark = 32 reference snapshot tests against `design/reference/<ios|android>/<Name>_<light|dark>.png`, in `ScreenSnapshotTests` (iOS) and the `*SnapshotTest` classes (Android). The tolerances never change. Measured results, decisions and known gaps: [`docs/m5-offene-punkte.md`](docs/m5-offene-punkte.md).

| Nr. | Screen | Route | Reference name |
|---|---|---|---|
| 16 | Erdarbeiten – Protokoll | `inspection/earthworks` | `Erd` |
| 17 | Rohbau – Übersicht | `inspection/shell` | `Rohbau` |
| 18 | Rohbau – Maße & Lot | `inspection/shell/measure` | `RohbauMasse` |
| 19 | Zimmerer – Dachstuhl | `inspection/carpentry` | `Zimmerer` |
| 20 | Elektro – Übersicht | `inspection/electrical` | `ElUebersicht` |
| 21 | Raumprüfung Küche | `inspection/electrical/room/{id}` (`kitchen`) | `ElRaum` |
| 22 | Zählerschrank & FI-Test | `inspection/electrical/panel` | `ElVerteiler` |
| 23 | Rauchwarnmelder | `inspection/electrical/smoke` | `ElRauchmelder` |

**Defect form from an M5 screen (decision D4):** the buttons "Mangel" (Maße & Lot, Raumprüfung), "Löst nicht aus" (Zählerschrank) and "Kein Piepton" (Rauchwarnmelder) open the one fixture defect form (`defect/new`). "Sichern" from such an M5 opener returns to the opener; from the windows openers (Prüfschritt, Elemente) the M4 flow continues (sign-off above Elemente). Nothing is persisted (M8).

Maße & Lot (`MeasureViewModel`, a pure value type): a ±5 mm stepper for Breite and Höhe, tolerance from `measurements.shellOpening.parameter` (12 mm, range 1–30) and a live result ("Innerhalb der Toleranz"). A decrement below 0 is a no-op.

```sh
# iOS: screen snapshots (the eight M5 screens are part of ScreenSnapshotTests), then the raw diff report
cd ios && xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15' -only-testing:BaupilotTests/ScreenSnapshotTests
scripts/diff-report --platform ios   # from the repository root

# iOS: early trades UI tests (M5 links, stepper, defect-form entry points, reminder switch)
cd ios && xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15' -only-testing:BaupilotUITests/EarlyTradesUITests

# iOS: MeasureViewModel unit tests
cd ios && xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15' -only-testing:BaupilotTests/MeasureViewModelTests

# Android: screen snapshots, then the raw diff report
cd android && JAVA_HOME=<jdk21> ./gradlew :app:testDebugUnitTest --tests 'de.baupilot.snapshot.*SnapshotTest'
scripts/diff-report --platform android   # from the repository root

# Android: early trades UI test (Compose UI test on Robolectric)
cd android && JAVA_HOME=<jdk21> ./gradlew :app:testDebugUnitTest --tests '*EarlyTradesUiTest'

# Android: MeasureViewModel unit tests
cd android && JAVA_HOME=<jdk21> ./gradlew :app:testDebugUnitTest --tests '*MeasureViewModelTest'
```

## Font provenance

Fonts are embedded as TTF and also used in the test path.

| File | Source | SHA-256 |
|---|---|---|
| `ios/Baupilot/Resources/Fonts/Inter-Regular.ttf` | Inter v4.1, `Inter-4.1.zip` → `extras/ttf/` | `40d692fce188e4471e2b3cba937be967878f631ad3ebbbdcd587687c7ebe0c82` |
| `ios/Baupilot/Resources/Fonts/Inter-Medium.ttf` | same | `97ad806f526e41546d46365bb3a393145f75b7b1568913db74549ad8b8dba872` |
| `ios/Baupilot/Resources/Fonts/Inter-SemiBold.ttf` | same | `78a843fade9d4612a5567302fb595b56976eb5fcebf4fea5a5912d638bafcde3` |
| `ios/Baupilot/Resources/Fonts/Inter-Bold.ttf` | same | `288316099b1e0a47a4716d159098005eef7c0066921f34e3200393dbdb01947f` |
| `android/app/src/main/res/font/roboto_regular.ttf` | Roboto v3.015, `Roboto_v3.015.zip` → `unhinted/static/Roboto-Regular.ttf` | `867580681a6272f69537e220ae9049f082286daf46c1a4a4d21fa55b2cee89fc` |
| `android/app/src/main/res/font/roboto_medium.ttf` | same, `Roboto-Medium.ttf` | `5f4b733aa0214e1610d2870765f9a68b0380b1bfa5a871f0b1eb20e2a86bee59` |
| `android/app/src/main/res/font/roboto_bold.ttf` | same, `Roboto-Bold.ttf` | `f02bf7a9869c58b9bb61130f55c6b1a489ea85f2ac1de75b3421dcb133bc57f3` |

Download URLs:

- Inter: `https://github.com/rsms/inter/releases/download/v4.1/Inter-4.1.zip`
- Roboto (repository `googlefonts/roboto-3-classic`): `https://github.com/googlefonts/roboto-3-classic/releases/download/v3.015/Roboto_v3.015.zip`

## Licences

Both font families are under the SIL Open Font License 1.1.

- Inter: `ios/Baupilot/Resources/Fonts/OFL.txt`
- Roboto: `android/third_party/roboto/OFL.txt`
